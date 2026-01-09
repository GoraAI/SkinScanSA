package com.skinscan.sa.data.db.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room type converters for Date objects
 */
class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
