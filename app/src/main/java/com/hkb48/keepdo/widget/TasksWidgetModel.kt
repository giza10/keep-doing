package com.hkb48.keepdo.widget

import android.content.Context
import com.hkb48.keepdo.DatabaseAdapter
import com.hkb48.keepdo.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class TasksWidgetModel(private val mContext: Context) {
    private val mTaskList: MutableList<Task> = ArrayList()
    fun reload() {
        mTaskList.clear()
        val fullTaskList = DatabaseAdapter.getInstance(mContext).taskList
        val today = todayDate
        for (task in fullTaskList) {
            if (!getDoneStatus(task.taskID, today) && isValidDay(task, today)) {
                mTaskList.add(task)
            }
        }
    }

    val itemCount: Int
        get() = mTaskList.size

    fun getTaskId(position: Int): Long {
        return mTaskList[position].taskID
    }

    fun getTaskName(position: Int): String? {
        return mTaskList[position].name
    }

    fun getDoneStatus(taskId: Long, date: String): Boolean {
        return DatabaseAdapter.getInstance(mContext).getDoneStatus(taskId, date)
    }

    val todayDate: String
        get() = DatabaseAdapter.getInstance(mContext).todayDate

    private fun isValidDay(task: Task, dateString: String?): Boolean {
        val sdf = SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN)
        var date: Date? = null
        if (dateString != null) {
            date = try {
                sdf.parse(dateString)
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }
        }
        if (date == null) {
            return false
        }
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
        return task.recurrence.isValidDay(dayOfWeek)
    }

    companion object {
        private const val SDF_PATTERN_YMD = "yyyy-MM-dd"
    }
}