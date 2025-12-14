package com.example.rently.scrape

import com.example.rently.RentlyProperties
import com.example.rently.model.ShowingRow
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Frame
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.exists

@Service
class RentlyShowingsScraper(
  private val props: RentlyProperties
) {
  private val log = LoggerFactory.getLogger(RentlyShowingsScraper::class.java)
  private val debugTsFmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

  fun fetchShowings(): List<ShowingRow> {
    require(props.url.isNotBlank()) { "rently.url must be set in application.yml" }

    Playwright.create().use { playwright ->
      val browser = launchBrowser(playwright)
      browser.use {
        val context = newContext(browser)
        context.use {
          val page = context.newPage()
          // Be a bit more forgiving; Rently pages can be slow / JS-heavy.
          page.setDefaultTimeout(90_000.0)
          page.setDefaultNavigationTimeout(90_000.0)
          log.info("Navigating to {}", props.url)
          page.navigate(props.url)

          // Wait for JS frameworks to finish painting
          page.waitForLoadState(LoadState.NETWORKIDLE)

          // The Rently UI is embedded in an iframe. Scrape inside the iframe content, not the wrapper page.
          val frame = findPlatformFrame(page)
          val table = tryFindShowingsTable(frame)

          val rows = table.locator("tbody tr")
          val count = rows.count()
          log.info("Found {} showing rows", count)

          val results = mutableListOf<ShowingRow>()
          for (i in 0 until count) {
            val tr = rows.nth(i)
            val tds = tr.locator("td")
            if (tds.count() < 4) continue

            val name = tds.nth(0).innerText().trim()
            val showingDate = tds.nth(1).innerText().trim()
            val feedback = tds.nth(2).innerText().trim()
            val source = tds.nth(3).innerText().trim()

            if (name.isBlank() && showingDate.isBlank()) continue

            results.add(
              ShowingRow(
                name = name,
                showingDate = showingDate,
                feedback = feedback,
                source = source
              )
            )
          }

          // Persist storage state (cookies/localStorage) if configured
          saveStorageState(context)

          return results
        }
      }
    }
  }

  private fun findPlatformFrame(page: Page): Frame {
    // The wrapper HTML includes: <iframe id="platform-iframe" src="https://...cloudfront.net/activity_log?...">
    // Use the iframe element's contentFrame() to guarantee we grab the embedded UI frame (not the main frame).
    page.waitForSelector("iframe#platform-iframe")
    val iframeHandle = page.querySelector("iframe#platform-iframe")
    val frame = iframeHandle?.contentFrame()

    if (frame != null) {
      log.info("Using platform iframe: {}", frame.url())
      return frame
    }

    dumpDebug(page, "platform_iframe_not_found")
    error("platform iframe not found (iframe#platform-iframe has no content frame)")
  }

  private fun tryFindShowingsTable(frame: Frame): com.microsoft.playwright.Locator {
    // Strategy:
    // 1) Try the most specific selector (fast + stable when it matches).
    // 2) Fall back to "any table that has a header containing 'Showing'" so we can at least scrape,
    //    and if that also fails, dump debug artifacts for quick diagnosis.
    val strict = frame.locator(
      "table:has(th:has-text('Name')):has(th:has-text('Showing')):has(th:has-text('Source'))"
    ).first()

    try {
      strict.waitFor()
      return strict
    } catch (e: Exception) {
      log.warn("Strict showings table selector did not match (will try fallback): {}", e.message)
    }

    val fallback = frame.locator("table:has(th:has-text('Showing'))").first()
    try {
      fallback.waitFor()
      return fallback
    } catch (e: Exception) {
      dumpDebug(frame, "table_not_found")
      throw e
    }
  }

  private fun dumpDebug(page: Page, reason: String) {
    try {
      val ts = LocalDateTime.now().format(debugTsFmt)
      val debugDir = Path.of("debug")
      Files.createDirectories(debugDir)

      val safeReason = reason.replace(Regex("[^a-zA-Z0-9._-]"), "_")
      val png = debugDir.resolve("rently_${safeReason}_${ts}.png")
      val html = debugDir.resolve("rently_${safeReason}_${ts}.html")

      log.warn("Scrape debug dump (reason={}): url='{}' title='{}'", reason, page.url(), page.title())
      page.screenshot(Page.ScreenshotOptions().setPath(png).setFullPage(true))
      Files.writeString(html, page.content())
      log.warn("Saved debug artifacts: screenshot='{}' html='{}'", png.toAbsolutePath(), html.toAbsolutePath())
    } catch (e: Exception) {
      log.warn("Failed to write debug artifacts: {}", e.message)
    }
  }

  private fun dumpDebug(frame: Frame, reason: String) {
    try {
      val ts = LocalDateTime.now().format(debugTsFmt)
      val debugDir = Path.of("debug")
      Files.createDirectories(debugDir)

      val safeReason = reason.replace(Regex("[^a-zA-Z0-9._-]"), "_")
      val html = debugDir.resolve("rently_frame_${safeReason}_${ts}.html")

      log.warn("Scrape debug dump (frame, reason={}): url='{}' name='{}'", reason, frame.url(), frame.name())
      Files.writeString(html, frame.content())
      log.warn("Saved debug artifact: frame_html='{}'", html.toAbsolutePath())
    } catch (e: Exception) {
      log.warn("Failed to write frame debug artifacts: {}", e.message)
    }
  }

  private fun launchBrowser(playwright: Playwright): Browser {
    return playwright.chromium().launch(
      com.microsoft.playwright.BrowserType.LaunchOptions()
        .setHeadless(props.headless)
    )
  }

  private fun newContext(browser: Browser): BrowserContext {
    val pathStr = props.storageStatePath?.trim().takeUnless { it.isNullOrEmpty() }
    val path = pathStr?.let { Path.of(it) }

    return if (path != null && path.exists()) {
      log.info("Loading storage state from {}", path.toAbsolutePath())
      browser.newContext(
        Browser.NewContextOptions().setStorageStatePath(path)
      )
    } else {
      browser.newContext()
    }
  }

  private fun saveStorageState(context: BrowserContext) {
    val pathStr = props.storageStatePath?.trim().takeUnless { it.isNullOrEmpty() } ?: return
    val path = Path.of(pathStr)
    try {
      context.storageState(BrowserContext.StorageStateOptions().setPath(path))
      log.info("Saved storage state to {}", path.toAbsolutePath())
    } catch (e: Exception) {
      log.warn("Failed to save storage state: {}", e.message)
    }
  }
}
