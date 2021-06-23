package com.hkb48.keepdo.util

import java.util.*

object DateComparator {
    fun equals(a: Date?, b: Date?): Boolean {
        if (a == null || b == null) {
            return false
        }
        return truncate(a) == truncate(b)
    }

    private fun truncate(datetime: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = datetime
        return GregorianCalendar(cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DATE])
            .time
    }
}