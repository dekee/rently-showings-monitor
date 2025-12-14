package com.example.rently.notify

import com.example.rently.model.ShowingRow

interface Notifier {
  fun notifyNewShowings(newShowings: List<ShowingRow>)
}
