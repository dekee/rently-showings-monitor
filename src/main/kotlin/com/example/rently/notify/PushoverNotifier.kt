package com.example.rently.notify

import com.example.rently.RentlyProperties
import com.example.rently.model.ShowingRow
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
@ConditionalOnProperty(prefix = "notify.pushover", name = ["enabled"], havingValue = "true")
class PushoverNotifier(
  private val props: PushoverProperties,
  private val rently: RentlyProperties
) : Notifier {
  private val log = LoggerFactory.getLogger(PushoverNotifier::class.java)
  private val client = RestClient.create("https://api.pushover.net")

  override fun notifyNewShowings(newShowings: List<ShowingRow>) {
    val token = props.token?.trim().orEmpty()
    val user = props.user?.trim().orEmpty()
    if (token.isBlank() || user.isBlank()) {
      log.warn("Pushover is enabled but notify.pushover.token/user is not set; skipping.")
      return
    }

    // Keep push concise; include count + first few showings.
    val title = "New Rently showing(s): ${newShowings.size}"
    val message = buildString {
      newShowings.take(5).forEachIndexed { idx, s ->
        if (idx > 0) append('\n')
        append("${s.name} | ${s.showingDate} | ${s.source}")
      }
      if (newShowings.size > 5) append("\n… +${newShowings.size - 5} more")
    }

    try {
      val body = linkedMapOf(
        "token" to token,
        "user" to user,
        "title" to title,
        "message" to message
      )
      // Add a direct link back to the source page for quick verification.
      rently.url.trim().takeIf { it.isNotEmpty() }?.let { url ->
        body["url"] = url
        body["url_title"] = "Open Rently Activity Log"
      }
      props.device?.trim()?.takeIf { it.isNotEmpty() }?.let { body["device"] = it }
      props.sound?.trim()?.takeIf { it.isNotEmpty() }?.let { body["sound"] = it }

      val resp = client.post()
        .uri("/1/messages.json")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(body.entries.joinToString("&") { (k, v) -> "${k}=${encode(v)}" })
        .retrieve()
        .toBodilessEntity()

      log.info("Pushover notification sent (status={})", resp.statusCode.value())
    } catch (e: Exception) {
      log.warn("Pushover notification failed: {}", e.message)
    }
  }

  private fun encode(v: String): String =
    java.net.URLEncoder.encode(v, Charsets.UTF_8)
}


