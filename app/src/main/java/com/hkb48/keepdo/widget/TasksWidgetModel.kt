package com.hkb48.keepdo.widget

import android.content.Context
import com.hkb48.keepdo.DateChangeTimeUtil
import com.hkb48.keepdo.KeepdoApplication
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.db.entity.Task
import kotlinx.coroutines.runBlocking
import java.util.*

internal class TasksWidgetModel(private val mContext: Context) {
    private val mTaskList: MutableList<Task> = ArrayList()

    fun reload() = runBlocking {
        mTaskList.clear()
        val fullTaskList =
            (mContext as KeepdoApplication).getDatabase().taskDao().getTaskListByOrder()
        for (task in fullTaskList) {
            if (getDoneStatus(task._id!!, todayDate).not() && isValidDay(task, todayDate)) {
                mTaskList.add(task)
            }
        }
    }

    val itemCount: Int
        get() = mTaskList.size

    fun getTaskId(position: Int): Int {
        return mTaskList[position]._id!!
    }

    fun getTaskName(position: Int): String {
        return mTaskList[position].name
    }

    private suspend fun getDoneStatus(taskId: Int, date: Date): Boolean {
        return (mContext as KeepdoApplication).getDatabase().taskCompletionDao()
            .getByDate(taskId, date).count() > 0
    }

    val todayDate: Date
        get() = DateChangeTimeUtil.dateTime

    private fun isValidDay(task: Task, date: Date?): Boolean {
        return if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            Recurrence.getFromTask(task).isValidDay(calendar[Calendar.DAY_OF_WEEK])
        } else {
            false
        }
    }
}