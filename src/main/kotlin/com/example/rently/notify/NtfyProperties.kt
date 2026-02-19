package com.example.rently.notify

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notify.ntfy")
data class NtfyProperties(
  var enabled: Boolean = false,
  var server: String = "https://ntfy.sh",
  var topic: String = "",
  var priority: Int? = null,
  var tags: String? = null
)
