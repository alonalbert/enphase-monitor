package com.alonalbert.enphase.monitor.db

import androidx.room3.ColumnTypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

object LocalDateConverter {
  @ColumnTypeConverter
  fun fromLocalDate(date: LocalDate?): String? {
    return date?.format(ISO_LOCAL_DATE)
  }

  @ColumnTypeConverter
  fun toLocalDate(value: String?): LocalDate? {
    return value?.let { LocalDate.parse(it, ISO_LOCAL_DATE) }
  }
}
