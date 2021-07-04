package com.hkb48.keepdo

import java.io.Serializable

data class TaskInfo(var name: String?, var context: String?, var recurrence: Recurrence) :
    Serializable {
    var taskId: Int = INVALID_TASKID
    var reminder = Reminder()
    var order: Int = INVALID_TASKID

    companion object {
        const val INVALID_TASKID = Int.MIN_VALUE
    }

}