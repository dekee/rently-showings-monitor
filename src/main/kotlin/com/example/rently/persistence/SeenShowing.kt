package com.example.rently.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "seen_showings")
class SeenShowing(
  @Id
  @Column(name = "fingerprint", nullable = false, length = 512)
  val fingerprint: String,

  @Column(name = "first_seen_at", nullable = false)
  val firstSeenAt: OffsetDateTime = OffsetDateTime.now()
)
