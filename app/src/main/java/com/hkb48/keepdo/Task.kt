package com.hkb48.keepdo

import java.io.Serializable

data class Task(var name: String?, var context: String?, var recurrence: Recurrence) :
    Serializable {
    var taskID: Long = INVALID_TASKID
    var reminder = Reminder()
    var order: Long = INVALID_TASKID

    companion object {
        const val INVALID_TASKID = Long.MIN_VALUE
    }

}