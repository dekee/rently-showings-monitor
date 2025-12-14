package com.example.rently.monitor

import com.example.rently.RentlyProperties
import com.example.rently.notify.Notifier
import com.example.rently.persistence.SeenShowing
import com.example.rently.persistence.SeenShowingRepository
import com.example.rently.scrape.RentlyShowingsScraper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ShowingsMonitor(
  private val props: RentlyProperties,
  private val scraper: RentlyShowingsScraper,
  private val seenRepo: SeenShowingRepository,
  private val notifiers: List<Notifier>
) {
  private val log = LoggerFactory.getLogger(ShowingsMonitor::class.java)

  @Scheduled(fixedDelayString = "\${rently.poll-delay-ms:300000}")
  fun poll() {
    try {
      val showings = scraper.fetchShowings()
      if (showings.isEmpty()) {
        log.info("No showings returned (0 rows). If the page shows rows, the selector may need tweaking.")
        return
      }

      val newOnes = showings.filter { s -> !seenRepo.existsById(s.fingerprint()) }

      if (newOnes.isNotEmpty()) {
        // Save first to avoid duplicate alerts if notifier fails
        newOnes.forEach { s -> seenRepo.save(SeenShowing(s.fingerprint())) }
        if (notifiers.isEmpty()) {
          log.warn("No Notifier beans configured; new showings will not be alerted.")
        }
        notifiers.forEach { n ->
          try {
            n.notifyNewShowings(newOnes)
          } catch (e: Exception) {
            log.warn("Notifier {} failed: {}", n.javaClass.simpleName, e.message)
          }
        }
      } else {
        log.info("No new showings.")
      }
    } catch (e: Exception) {
      log.error("Poll failed: {}", e.message, e)
    }
  }
}
