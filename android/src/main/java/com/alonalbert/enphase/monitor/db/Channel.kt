package com.alonalbert.enphase.monitor.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
  indices = [Index(value = ["channelId"], unique = true)]
)
data class Channel(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  val channelId: String,
  val name: String,
)
