package com.hkb48.keepdo.ui.tasklist

import com.hkb48.keepdo.db.entity.Task

data class TaskListItem(
    val task: Task,
    var daysSinceLastDone: Int?,
    var comboCount: Int
)