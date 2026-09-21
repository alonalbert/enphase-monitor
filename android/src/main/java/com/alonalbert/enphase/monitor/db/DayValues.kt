package com.alonalbert.enphase.monitor.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
  foreignKeys = [
    ForeignKey(
      entity = Day::class,
      parentColumns = ["id"],
      childColumns = ["day_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["day_id", "index"], unique = true)]
)
data class DayValues(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  @ColumnInfo(name = "day_id")
  val dayId: Long,

  val index: Int,

  val production: Double,
  val consumption: Double,
  val charge: Double,
  val discharge: Double,
  val import: Double,
  val export: Double,
  val battery: Int?,
)