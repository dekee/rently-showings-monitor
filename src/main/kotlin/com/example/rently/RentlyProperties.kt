package com.example.rently

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "rently")
data class RentlyProperties(
  var url: String = "",
  var pollDelayMs: Long = 300_000,
  var headless: Boolean = true,
  var storageStatePath: String? = "storage-state.json"
)
