package com.alonalbert.enphase.monitor.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.LocalDate

@Entity(
  indices = [Index(value = ["date"], unique = true)]
)
data class Day(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  val date: LocalDate,
)
