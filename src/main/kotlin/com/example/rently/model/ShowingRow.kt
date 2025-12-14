package com.example.rently.model

data class ShowingRow(
  val name: String,
  val showingDate: String,
  val feedback: String,
  val source: String
) {
  fun fingerprint(): String = listOf(name.trim(), showingDate.trim(), source.trim()).joinToString("|")
  override fun toString(): String = "${name.trim()} | ${showingDate.trim()} | ${feedback.trim()} | ${source.trim()}"
}
