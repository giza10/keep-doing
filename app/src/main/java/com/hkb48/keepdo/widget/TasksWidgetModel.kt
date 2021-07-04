package com.hkb48.keepdo.widget

import android.content.Context
import com.hkb48.keepdo.DatabaseAdapter
import com.hkb48.keepdo.DateChangeTimeUtil
import com.hkb48.keepdo.TaskInfo
import java.util.*

internal class TasksWidgetModel(private val mContext: Context) {
    private val mTaskInfoList: MutableList<TaskInfo> = ArrayList()
    fun reload() {
        mTaskInfoList.clear()
        val fullTaskList = DatabaseAdapter.getInstance(mContext).taskInfoList
        val today = todayDate
        for (task in fullTaskList) {
            if (getDoneStatus(task.taskId, today).not() && isValidDay(task, today)) {
                mTaskInfoList.add(task)
            }
        }
    }

    val itemCount: Int
        get() = mTaskInfoList.size

    fun getTaskId(position: Int): Int {
        return mTaskInfoList[position].taskId
    }

    fun getTaskName(position: Int): String? {
        return mTaskInfoList[position].name
    }

    fun getDoneStatus(taskId: Int, date: Date): Boolean {
        return DatabaseAdapter.getInstance(mContext).getDoneStatus(taskId, date)
    }

    val todayDate: Date
        get() = DateChangeTimeUtil.dateTime

    private fun isValidDay(taskInfo: TaskInfo, date: Date?): Boolean {
        return if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
            taskInfo.recurrence.isValidDay(dayOfWeek)
        } else {
            false
        }
    }
}