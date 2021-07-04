package com.hkb48.keepdo

import android.content.ContentResolver
import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hkb48.keepdo.data.Task
import com.hkb48.keepdo.data.TaskCompletion
import com.hkb48.keepdo.data.TaskDatabase
import java.io.*
import java.util.*

class DatabaseAdapter private constructor(context: Context) {
    private val mTaskDatabase = TaskDatabase.getInstance(context)
    private val mContentResolver: ContentResolver = context.contentResolver

    val taskInfoList: MutableList<TaskInfo>
        get() {
            val taskInfoList: MutableList<TaskInfo> = mutableListOf()
            val taskList = mTaskDatabase.taskDao().getTaskListByOrder()
            for (task in taskList) {
                taskInfoList.add(getTaskInfo(task))
            }
            return taskInfoList
        }

    fun addTask(taskInfo: TaskInfo): Int {
        val taskName = taskInfo.name
        val taskContext = taskInfo.context
        val recurrence = taskInfo.recurrence
        val reminder = taskInfo.reminder
        return if (taskName == null || taskName.isEmpty()) {
            TaskInfo.INVALID_TASKID
        } else try {
            val task = Task(
                null, taskName,
                recurrence.monday,
                recurrence.tuesday,
                recurrence.wednesday,
                recurrence.thursday,
                recurrence.friday,
                recurrence.saturday,
                recurrence.sunday,
                taskContext,
                reminder.enabled,
                reminder.timeInMillis,
                taskInfo.order
            )
            mTaskDatabase.taskDao().add(task).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            TaskInfo.INVALID_TASKID
        }
    }

    fun editTask(taskInfo: TaskInfo) {
        val taskId = taskInfo.taskId
        val taskName = taskInfo.name
        val taskContext = taskInfo.context
        val recurrence = taskInfo.recurrence
        val reminder = taskInfo.reminder
        if (taskName == null || taskName.isEmpty()) {
            return
        }
        val task = Task(
            taskId, taskName,
            recurrence.monday,
            recurrence.tuesday,
            recurrence.wednesday,
            recurrence.thursday,
            recurrence.friday,
            recurrence.saturday,
            recurrence.sunday,
            taskContext,
            reminder.enabled,
            reminder.timeInMillis,
            taskInfo.order
        )
        try {
            mTaskDatabase.taskDao().update(task)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun deleteTask(taskId: Int) {
        // Delete task from TASKS_TABLE_NAME
        // Records corresponding to the deleted task are also removed from TASK_COMPLETION_TABLE_NAME
        mTaskDatabase.taskDao().delete(taskId)
    }

    fun setDoneStatus(taskId: Int, date: Date, doneSwitch: Boolean) {
        try {
            if (doneSwitch) {
                val taskCompletion = TaskCompletion(null, taskId, date)
                mTaskDatabase.taskCompletionDao().insert(taskCompletion)
            } else {
                mTaskDatabase.taskCompletionDao().delete(taskId, date)
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun getDoneStatus(taskId: Int, date: Date): Boolean {
        return mTaskDatabase.taskCompletionDao().getByDate(taskId, date).count() > 0
    }

    fun getNumberOfDone(taskId: Int): Int {
        return mTaskDatabase.taskCompletionDao().getAll(taskId).count()
    }

    fun getFirstDoneDate(taskId: Int): Date? {
        return mTaskDatabase.taskCompletionDao().getFirstCompletionDate(taskId, todayDate)
    }

    fun getLastDoneDate(taskId: Int): Date? {
        return mTaskDatabase.taskCompletionDao().getLastCompletionDate(taskId, todayDate)
    }

    fun getTask(taskId: Int): TaskInfo? {
        return mTaskDatabase.taskDao().getTask(taskId)?.let {
            getTaskInfo(it)
        }
    }

    fun getHistoryInMonth(taskId: Int, year: Int, month: Int): ArrayList<Date> {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        val firstDayInMonth = calendar.clone() as Calendar
        firstDayInMonth[Calendar.DAY_OF_MONTH] = 1
        val lastDayInMonth = calendar.clone() as Calendar
        lastDayInMonth[Calendar.DAY_OF_MONTH] = lastDayInMonth.getActualMaximum(Calendar.DATE)

        val doneList = mTaskDatabase.taskCompletionDao().getCompletionHistoryBetween(
            taskId, firstDayInMonth.time, lastDayInMonth.time
        )
        return ArrayList(doneList)
    }

    fun getComboCount(taskId: Int): Int {
        var count = 0
        val mDoneHistory =
            mTaskDatabase.taskCompletionDao().getCompletionHistoryDesc(taskId, todayDate)
        if (mDoneHistory.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            val calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(mDoneHistory[0])
            val recurrence = getTask(taskId)!!.recurrence
            var listIndex = 0
            while (true) {
                if (calIndex == calDone) {
                    // count up combo
                    count++
                    calDone = if (++listIndex < mDoneHistory.size) {
                        getCalendar(mDoneHistory[listIndex])
                    } else {
                        break
                    }
                } else {
                    if (recurrence.isValidDay(calIndex[Calendar.DAY_OF_WEEK])) {
                        if (calIndex != calToday) {
                            break
                        }
                    }
                }
                calIndex.add(Calendar.DAY_OF_MONTH, -1)
            }
        }
        return count
    }

    fun getMaxComboCount(taskId: Int): Int {
        var currentCount = 0
        var maxCount = 0
        val mDoneHistory =
            mTaskDatabase.taskCompletionDao().getCompletionHistoryAsc(taskId, todayDate)
        if (mDoneHistory.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            var calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(mDoneHistory[0])
            val recurrence = getTask(taskId)!!.recurrence
            var listIndex = 0
            var isCompleted = false
            do {
                if (calIndex == calDone) {
                    // count up combo
                    currentCount++
                    if (currentCount > maxCount) {
                        maxCount = currentCount
                    }
                    if (++listIndex < mDoneHistory.size) {
                        calDone = getCalendar(mDoneHistory[listIndex])
                    } else {
                        isCompleted = true
                    }
                    calIndex.add(Calendar.DAY_OF_MONTH, 1)
                } else {
                    if (recurrence.isValidDay(calIndex[Calendar.DAY_OF_WEEK])) {
                        // stop combo
                        if (calIndex != calToday) {
                            currentCount = 0
                        }
                        if (isCompleted.not()) {
                            calIndex = calDone.clone() as Calendar
                        } else {
                            calIndex.add(Calendar.DAY_OF_MONTH, 1)
                        }
                    } else {
                        calIndex.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            } while (calIndex.after(calToday).not())
        }
        return maxCount
    }

    private val todayDate: Date
        get() {
            return DateChangeTimeUtil.dateTime
        }

    private fun getCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    private fun getTaskInfo(task: Task): TaskInfo {
        val recurrence = Recurrence(
            task.monFrequency,
            task.tueFrequency,
            task.wedFrequency,
            task.thrFrequency,
            task.friFrequency,
            task.satFrequency,
            task.sunFrequency
        )
        val taskInfo = TaskInfo(task.name, task.context, recurrence)
        taskInfo.reminder = if (task.reminderEnabled && task.reminderTime != null) {
            Reminder(task.reminderEnabled, task.reminderTime.toLong())
        } else {
            Reminder()
        }
        taskInfo.taskId = task._id ?: TaskInfo.INVALID_TASKID
        taskInfo.order = task.listOrder
        return taskInfo
    }

    val maxSortOrderId: Int
        get() {
            return mTaskDatabase.taskDao().getMaxOrder()
        }

    fun backupDataBase(outputFile: Uri): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var success = false
        try {
            // Execute checkpoint query to ensure all of the pending transactions are applied.
            mTaskDatabase.taskDao().checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))
            mTaskDatabase.taskCompletionDao()
                .checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))

            inputStream = FileInputStream(mTaskDatabase.databasePath)
            outputStream = mContentResolver.openOutputStream(outputFile)!!
            success = copyDataBase(inputStream, outputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return success
    }

    fun restoreDataBase(inputFile: Uri): Boolean {
        return if (isValidSQLite(inputFile)) {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            var success = false
            try {
                inputStream = mContentResolver.openInputStream(inputFile)!!
                outputStream = FileOutputStream(mTaskDatabase.databasePath)
                success = copyDataBase(inputStream, outputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    outputStream?.close()
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            success
        } else {
            false
        }
    }

    @Synchronized
    private fun copyDataBase(inputStream: InputStream, outputStream: OutputStream): Boolean {
        return try {
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun isValidSQLite(inputFile: Uri): Boolean {
        return try {
            val inputStream = mContentResolver.openInputStream(inputFile)!!
            val fr = InputStreamReader(inputStream)
            val buffer = CharArray(16)
            fr.read(buffer, 0, 16)
            val str = String(buffer)
            fr.close()
            inputStream.close()
            str == "SQLite format 3\u0000"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        // Backup & Restore
        const val BACKUP_FILE_NAME = "keepdo.db"

        @Volatile
        private var instance: DatabaseAdapter? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            DatabaseAdapter(context).also { instance = it }
        }
    }

}