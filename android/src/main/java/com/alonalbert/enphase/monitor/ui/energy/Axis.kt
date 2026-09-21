package com.alonalbert.enphase.monitor.ui.energy

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter

val DecimalValueFormatter = CartesianValueFormatter.decimal(1)

fun timeOfDayAxisValueFormatter(pointsPerHour: Int) = CartesianValueFormatter { _, x, _ ->
  when (val h = x.toInt() / pointsPerHour) {
    0, 24 -> "12am"
    12 -> "12pm"
    else -> (h % 12).toString()
  }
}
