package com.hkb48.keepdo.db

import androidx.room.TypeConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Converters {
    @TypeConverter
    fun stringToDate(string: String?): Date? {
        return string?.let {
            try {
                SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN).parse(it)
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }
        }
    }

    @TypeConverter
    fun dateToString(date: Date?): String? {
        return date?.let {
            SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN).format(it)
        }
    }

    @TypeConverter
    fun booleanToString(boolean: Boolean): String {
        return boolean.toString()
    }

    @TypeConverter
    fun stringToBoolean(string: String): Boolean {
        return string.toBoolean()
    }

    companion object {
        private const val SDF_PATTERN_YMD = "yyyy-MM-dd"
    }
}