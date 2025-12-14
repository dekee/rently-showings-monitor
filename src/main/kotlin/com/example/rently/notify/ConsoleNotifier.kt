package com.example.rently.notify

import com.example.rently.RentlyProperties
import com.example.rently.model.ShowingRow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConsoleNotifier(
  private val rently: RentlyProperties
) : Notifier {
  private val log = LoggerFactory.getLogger(ConsoleNotifier::class.java)

  override fun notifyNewShowings(newShowings: List<ShowingRow>) {
    log.info("NEW SHOWINGS: {}", newShowings.size)
    log.info("Source: {}", rently.url)
    newShowings.forEach { log.info("  {}", it.toString()) }
  }
}
