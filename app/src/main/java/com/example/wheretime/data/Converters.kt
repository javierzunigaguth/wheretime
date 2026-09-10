package com.example.wheretime.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromSubcategory(value: Subcategory): String = value.name

    @TypeConverter
    fun toSubcategory(value: String): Subcategory = Subcategory.valueOf(value)

    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
}
