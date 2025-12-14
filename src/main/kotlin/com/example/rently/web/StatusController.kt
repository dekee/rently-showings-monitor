package com.example.rently.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusController {
  @GetMapping("/")
  fun status(): Map<String, String> = mapOf("status" to "ok")
}
