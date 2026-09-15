package com.alonalbert.enphase.monitor.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
  tableName = "ChannelUsageValue",
  primaryKeys = ["day_id", "channel_id", "index"],
  foreignKeys = [
    ForeignKey(
      entity = Day::class,
      parentColumns = ["id"],
      childColumns = ["day_id"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = Channel::class,
      parentColumns = ["id"],
      childColumns = ["channel_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["day_id"]), Index(value = ["channel_id"])]
)
data class ChannelUsageValue(
  @ColumnInfo(name = "day_id")
  val dayId: Long,

  @ColumnInfo(name = "channel_id")
  val channelId: Long,

  @ColumnInfo(name = "index")
  val index: Int,

  val value: Double
)
