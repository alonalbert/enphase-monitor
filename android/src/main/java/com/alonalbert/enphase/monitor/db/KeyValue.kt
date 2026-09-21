package com.alonalbert.enphase.monitor.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
class KeyValue(
  @PrimaryKey
  val name: String,
  val value: String,
)
