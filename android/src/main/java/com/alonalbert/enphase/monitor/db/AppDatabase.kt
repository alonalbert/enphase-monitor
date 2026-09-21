package com.alonalbert.enphase.monitor.db

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
  entities = [
    KeyValue::class,
    Day::class,
    DayExportValues::class,
    DayValues::class,
    Channel::class,
    ChannelUsageValue::class,
  ],
  version = 2,
  exportSchema = true,
)
@ColumnTypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun batteryDao(): BatteryDao
  abstract fun dayDao(): DayDao
  abstract fun configDao(): KeyValueDao
  abstract fun enphaseConfigDao(): EnphaseConfigDao
  abstract fun loginInfoDao(): LoginInfoDao

  companion object {
    fun getDatabase(context: Context, filename: String): AppDatabase {
      return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, filename)
        .fallbackToDestructiveMigration(true)
        .build()
    }
  }
}
