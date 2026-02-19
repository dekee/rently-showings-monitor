package com.example.rently.notify

import com.example.rently.RentlyProperties
import com.example.rently.model.ShowingRow
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
@ConditionalOnProperty(prefix = "notify.ntfy", name = ["enabled"], havingValue = "true")
class NtfyNotifier(
  private val props: NtfyProperties,
  private val rently: RentlyProperties
) : Notifier {
  private val log = LoggerFactory.getLogger(NtfyNotifier::class.java)
  private val client = RestClient.create()

  override fun notifyNewShowings(newShowings: List<ShowingRow>) {
    val topic = props.topic.trim()
    if (topic.isBlank()) {
      log.warn("ntfy is enabled but notify.ntfy.topic is not set; skipping.")
      return
    }

    val title = "New Rently showing(s): ${newShowings.size}"
    val message = buildString {
      newShowings.take(5).forEachIndexed { idx, s ->
        if (idx > 0) append('\n')
        append("${s.name} | ${s.showingDate} | ${s.source}")
      }
      if (newShowings.size > 5) append("\n… +${newShowings.size - 5} more")
    }

    try {
      val server = props.server.trimEnd('/')
      val req = client.post()
        .uri("$server/$topic")
        .contentType(MediaType.TEXT_PLAIN)
        .header("Title", title)

      props.priority?.let { req.header("Priority", it.toString()) }
      props.tags?.trim()?.takeIf { it.isNotEmpty() }?.let { req.header("Tags", it) }
      rently.url.trim().takeIf { it.isNotEmpty() }?.let {
        req.header("Click", it)
        req.header("Actions", "view, Open Rently, $it")
      }

      val resp = req.body(message)
        .retrieve()
        .toBodilessEntity()

      log.info("ntfy notification sent (status={})", resp.statusCode.value())
    } catch (e: Exception) {
      log.warn("ntfy notification failed: {}", e.message)
    }
  }
}
