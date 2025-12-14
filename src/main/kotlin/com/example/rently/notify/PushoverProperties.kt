package com.example.rently.notify

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notify.pushover")
data class PushoverProperties(
  /**
   * Enable/disable push notifications.
   */
  var enabled: Boolean = false,
  /**
   * Pushover application API token.
   */
  var token: String? = null,
  /**
   * Your Pushover user/group key (destination).
   */
  var user: String? = null,
  /**
   * Optional device name to target.
   */
  var device: String? = null,
  /**
   * Optional sound (e.g. "pushover", "bike", "cashregister").
   */
  var sound: String? = null
)


