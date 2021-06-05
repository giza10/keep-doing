package com.hkb48.keepdo

import java.io.Serializable
import java.util.*

class Reminder : Serializable {
    var enabled: Boolean
    var hourOfDay = 0
    var minute = 0

    constructor() {
        enabled = false
        hourOfDay = 0
        minute = 0
    }

    constructor(enabled: Boolean, timeInMillis: Long) {
        this.enabled = enabled
        this.timeInMillis = timeInMillis
    }

    var timeInMillis: Long
        get() {
            val time = Calendar.getInstance()
            time[Calendar.HOUR_OF_DAY] = hourOfDay
            time[Calendar.MINUTE] = minute
            return time.timeInMillis
        }
        private set(milliseconds) {
            val time = Calendar.getInstance()
            time.timeInMillis = milliseconds
            hourOfDay = time[Calendar.HOUR_OF_DAY]
            minute = time[Calendar.MINUTE]
        }
}