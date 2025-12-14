package com.example.rently

import com.example.rently.notify.PushoverProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RentlyProperties::class, PushoverProperties::class)
class RentlyShowingsMonitorApplication

fun main(args: Array<String>) {
  runApplication<RentlyShowingsMonitorApplication>(*args)
}
