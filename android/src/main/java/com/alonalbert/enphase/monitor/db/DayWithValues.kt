package com.alonalbert.enphase.monitor.db

import androidx.room3.Embedded
import androidx.room3.Relation

data class DayWithValues(
  @Embedded
  val day: Day,

  @Relation(
    parentColumns = ["id"],
    entityColumns = ["day_id"],
  )
  val values: List<DayValues>,
)