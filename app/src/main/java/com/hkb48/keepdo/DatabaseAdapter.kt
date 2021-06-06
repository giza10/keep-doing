package com.hkb48.keepdo

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Environment
import android.provider.BaseColumns
import android.util.Log
import com.hkb48.keepdo.KeepdoProvider.TaskCompletion
import com.hkb48.keepdo.KeepdoProvider.Tasks
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DatabaseAdapter private constructor(context: Context) {
    private val mDatabaseHelper: DatabaseHelper =
        DatabaseHelper.getInstance(context.applicationContext)
    private val mContentResolver: ContentResolver = context.contentResolver

    @Synchronized
    fun close() {
        mDatabaseHelper.close()
    }

    val taskList: MutableList<Task>
        get() {
            val tasks: MutableList<Task> = mutableListOf()
            val sortOrder = Tasks.TASK_LIST_ORDER + " asc"
            val cursor = mContentResolver.query(
                Tasks.CONTENT_URI, null, null,
                null, sortOrder
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        tasks.add(getTask(cursor))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            return tasks
        }

    fun addTask(task: Task): Long {
        val taskName = task.name
        val taskContext = task.context
        val recurrence = task.recurrence
        val reminder = task.reminder
        return if (taskName == null || taskName.isEmpty()) {
            Task.INVALID_TASKID
        } else try {
            val contentValues = ContentValues()
            contentValues.put(Tasks.TASK_NAME, taskName)
            contentValues.put(Tasks.FREQUENCY_MON, recurrence.monday.toString())
            contentValues.put(Tasks.FREQUENCY_TUE, recurrence.tuesday.toString())
            contentValues.put(Tasks.FREQUENCY_WEN, recurrence.wednesday.toString())
            contentValues.put(Tasks.FREQUENCY_THR, recurrence.thursday.toString())
            contentValues.put(Tasks.FREQUENCY_FRI, recurrence.friday.toString())
            contentValues.put(Tasks.FREQUENCY_SAT, recurrence.saturday.toString())
            contentValues.put(Tasks.FREQUENCY_SUN, recurrence.sunday.toString())
            contentValues.put(Tasks.TASK_CONTEXT, taskContext)
            contentValues.put(Tasks.REMINDER_ENABLED, reminder.enabled.toString())
            contentValues.put(Tasks.REMINDER_TIME, reminder.timeInMillis.toString())
            contentValues.put(Tasks.TASK_LIST_ORDER, task.order)
            val uri = mContentResolver.insert(Tasks.CONTENT_URI, contentValues)!!
            uri.lastPathSegment!!.toLong()
        } catch (e: SQLiteException) {
            Log.e(TAG, "Exception: insert, e = $e")
            Task.INVALID_TASKID
        }
    }

    fun editTask(task: Task) {
        val taskID = task.taskID
        val taskName = task.name
        val taskContext = task.context
        val recurrence = task.recurrence
        val reminder = task.reminder
        if (taskName == null || taskName.isEmpty()) {
            return
        }
        val contentValues = ContentValues()
        contentValues.put(Tasks.TASK_NAME, taskName)
        contentValues.put(Tasks.FREQUENCY_MON, recurrence.monday.toString())
        contentValues.put(Tasks.FREQUENCY_TUE, recurrence.tuesday.toString())
        contentValues.put(Tasks.FREQUENCY_WEN, recurrence.wednesday.toString())
        contentValues.put(Tasks.FREQUENCY_THR, recurrence.thursday.toString())
        contentValues.put(Tasks.FREQUENCY_FRI, recurrence.friday.toString())
        contentValues.put(Tasks.FREQUENCY_SAT, recurrence.saturday.toString())
        contentValues.put(Tasks.FREQUENCY_SUN, recurrence.sunday.toString())
        contentValues.put(Tasks.TASK_CONTEXT, taskContext)
        contentValues.put(Tasks.REMINDER_ENABLED, reminder.enabled.toString())
        contentValues.put(Tasks.REMINDER_TIME, reminder.timeInMillis.toString())
        contentValues.put(Tasks.TASK_LIST_ORDER, task.order)
        val uri = Uri.withAppendedPath(Tasks.CONTENT_URI, taskID.toString())
        try {
            mContentResolver.update(uri, contentValues, null, null)
        } catch (e: SQLiteException) {
            Log.e(TAG, "Exception: update, e = $e")
        }
    }

    fun deleteTask(taskID: Long) {
        // Delete task from TASKS_TABLE_NAME
        var uri = Uri.withAppendedPath(Tasks.CONTENT_URI, taskID.toString())!!
        mContentResolver.delete(uri, null, null)

        // Delete records of deleted task from TASK_COMPLETION_TABLE_NAME
        uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())!!
        mContentResolver.delete(uri, null, null)
    }

    fun setDoneStatus(taskID: Long, date: Date, doneSwitch: Boolean?) {
        val dateFormat = SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN)
        if (doneSwitch != null && doneSwitch) {
            val contentValues = ContentValues()
            contentValues.put(TaskCompletion.TASK_NAME_ID, taskID)
            contentValues.put(TaskCompletion.TASK_COMPLETION_DATE, dateFormat.format(date))
            try {
                mContentResolver.insert(TaskCompletion.CONTENT_URI, contentValues)
            } catch (e: SQLiteException) {
                Log.e(TAG, "Exception: insert, e = $e")
            }
        } else {
            val whereClause = TaskCompletion.TASK_COMPLETION_DATE + "=?"
            val whereArgs = arrayOf(dateFormat.format(date))
            val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
            mContentResolver.delete(uri, whereClause, whereArgs)
        }
    }

    fun getDoneStatus(taskID: Long, date: Date): Boolean {
        val dateFormat = SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN)
        return getDoneStatus(taskID, dateFormat.format(date))
    }

    fun getDoneStatus(taskID: Long, date: String): Boolean {
        var isDone = false
        val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
        val selection = TaskCompletion.TASK_COMPLETION_DATE + "=?"
        val selectionArgs = arrayOf(date)
        val cursor = mContentResolver.query(
            uri, null, selection,
            selectionArgs, null
        )
        if (cursor != null) {
            if (cursor.count > 0) {
                isDone = true
            }
            cursor.close()
        }
        return isDone
    }

    fun getNumberOfDone(taskID: Long): Int {
        var numberOfDone = 0
        val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
        val cursor = mContentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            numberOfDone = cursor.count
            cursor.close()
        }
        return numberOfDone
    }

    fun getFirstDoneDate(taskID: Long): Date? {
        val sdf = SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN)
        var date: Date? = null
        val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
        val selection = TaskCompletion.TASK_COMPLETION_DATE + "<=?"
        val selectionArgs = arrayOf(todayDate)
        val cursor = mContentResolver.query(
            Uri.withAppendedPath(uri, "min"),
            null,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null) {
            cursor.moveToFirst()
            val dateString = cursor.getString(0)
            if (dateString != null) {
                try {
                    date = sdf.parse(dateString)
                } catch (e: ParseException) {
                    Log.e(TAG, e.message + " in getFirstDoneDate() :" + dateString)
                }
            }
            cursor.close()
        }
        return date
    }

    fun getLastDoneDate(taskID: Long): Date? {
        val sdf = SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN)
        var date: Date? = null
        val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
        val selection = TaskCompletion.TASK_COMPLETION_DATE + "<=?"
        val selectionArgs = arrayOf(todayDate)
        val cursor = mContentResolver.query(
            Uri.withAppendedPath(uri, "max"),
            null,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null) {
            cursor.moveToFirst()
            val dateString = cursor.getString(0)
            if (dateString != null) {
                try {
                    date = sdf.parse(dateString)
                } catch (e: ParseException) {
                    Log.e(TAG, e.message + " in getLastDoneDate() :" + dateString)
                }
            }
            cursor.close()
        }
        return date
    }

    fun getTask(taskID: Long): Task? {
        var task: Task? = null
        val uri = Uri.withAppendedPath(Tasks.CONTENT_URI, taskID.toString())
        val cursor = mContentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            task = getTask(cursor)
            cursor.close()
        }
        return task
    }

    fun getHistoryInMonth(taskID: Long, month: Date): ArrayList<Date> {
        val dateList = ArrayList<Date>()
        val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
        val projection = arrayOf(TaskCompletion.TASK_COMPLETION_DATE)
        val cursor = mContentResolver.query(
            uri.buildUpon().appendQueryParameter("distinct", "true").build(),
            projection,
            null,
            null,
            null
        )
        val sdf = SimpleDateFormat(SDF_PATTERN_YM, Locale.JAPAN)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val date = getDate(cursor)
                    if (date != null) {
                        if (sdf.format(date) == sdf.format(month)) {
                            dateList.add(date)
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return dateList
    }

    fun getComboCount(taskID: Long): Int {
        var count = 0
        val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
        val projection = arrayOf(TaskCompletion.TASK_COMPLETION_DATE)
        val sortOrder = TaskCompletion.TASK_COMPLETION_DATE + " desc"
        val selection = TaskCompletion.TASK_COMPLETION_DATE + "<=?"
        val selectionArgs = arrayOf(todayDate)
        val cursor = mContentResolver.query(
            uri.buildUpon().appendQueryParameter("distinct", "true").build(),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val calToday = getCalendar(DateChangeTimeUtil.date)
                var calDone = getCalendar(getDate(cursor)!!)
                val calIndex = calToday.clone() as Calendar
                val recurrence = getTask(taskID)!!.recurrence
                while (true) {
                    if (calIndex == calDone) {
                        // count up combo
                        count++
                        calDone = if (cursor.moveToNext()) {
                            getCalendar(getDate(cursor)!!)
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
            cursor.close()
        }
        return count
    }

    fun getMaxComboCount(taskID: Long): Int {
        var currentCount = 0
        var maxCount = 0
        val uri = Uri.withAppendedPath(TaskCompletion.CONTENT_URI, taskID.toString())
        val projection = arrayOf(TaskCompletion.TASK_COMPLETION_DATE)
        val sortOrder = TaskCompletion.TASK_COMPLETION_DATE + " asc"
        val cursor = mContentResolver.query(
            uri.buildUpon().appendQueryParameter("distinct", "true").build(),
            projection,
            null,
            null,
            sortOrder
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val calToday = getCalendar(DateChangeTimeUtil.date)
                var calDone = getCalendar(getDate(cursor)!!)
                var calIndex = calDone.clone() as Calendar
                val recurrence = getTask(taskID)!!.recurrence
                var isCompleted = false
                do {
                    if (calIndex == calDone) {
                        // count up combo
                        currentCount++
                        if (currentCount > maxCount) {
                            maxCount = currentCount
                        }
                        if (cursor.moveToNext()) {
                            calDone = getCalendar(getDate(cursor)!!)
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
                            if (!isCompleted) {
                                calIndex = calDone.clone() as Calendar
                            } else {
                                calIndex.add(Calendar.DAY_OF_MONTH, 1)
                            }
                        } else {
                            calIndex.add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                } while (!calIndex.after(calToday))
            }
            cursor.close()
        }
        return maxCount
    }

    val todayDate: String
        get() {
            var date = ""
            val cursor = mContentResolver.query(
                KeepdoProvider.DateChangeTime.CONTENT_URI, null, null,
                null, null
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val dateColIndex =
                        cursor.getColumnIndex(KeepdoProvider.DateChangeTime.ADJUSTED_DATE)
                    date = cursor.getString(dateColIndex)
                }
                cursor.close()
            }
            return date
        }
    val nextDateChangeTime: Long
        get() {
            var nextAlarmTime: Long = -1
            val cursor = mContentResolver.query(
                KeepdoProvider.DateChangeTime.CONTENT_URI, null, null,
                null, null
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val colIndex =
                        cursor.getColumnIndex(KeepdoProvider.DateChangeTime.NEXT_DATE_CHANGE_TIME)
                    nextAlarmTime = cursor.getLong(colIndex)
                }
                cursor.close()
            }
            return nextAlarmTime
        }

    private fun getDate(cursor: Cursor): Date? {
        val dateString =
            cursor.getString(cursor.getColumnIndex(TaskCompletion.TASK_COMPLETION_DATE))
        val sdf = SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN)
        var date: Date? = null
        if (dateString != null) {
            try {
                date = sdf.parse(dateString)
            } catch (e: ParseException) {
                Log.e(TAG, e.message + " in getDate() :" + dateString)
            }
        }
        return date
    }

    private fun getCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    private fun getTask(cursor: Cursor): Task {
        val taskID = cursor.getString(cursor.getColumnIndex(BaseColumns._ID)).toLong()
        val taskName = cursor.getString(cursor.getColumnIndex(Tasks.TASK_NAME))
        val taskContext = cursor.getString(cursor.getColumnIndex(Tasks.TASK_CONTEXT))
        val recurrence = Recurrence(
            java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_MON))),
            java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_TUE))),
            java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_WEN))),
            java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_THR))),
            java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_FRI))),
            java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_SAT))),
            java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_SUN)))
        )
        val reminder: Reminder
        val reminderEnabled = cursor.getString(cursor.getColumnIndex(Tasks.REMINDER_ENABLED))
        val reminderTime = cursor.getString(cursor.getColumnIndex(Tasks.REMINDER_TIME))
        val taskOrder = cursor.getInt(cursor.getColumnIndex(Tasks.TASK_LIST_ORDER)).toLong()
        reminder = if (reminderEnabled == null || reminderTime == null) {
            Reminder()
        } else {
            Reminder(java.lang.Boolean.parseBoolean(reminderEnabled), reminderTime.toLong())
        }
        val task = Task(taskName, taskContext, recurrence)
        task.reminder = reminder
        task.taskID = taskID
        task.order = taskOrder
        return task
    }

    val maxSortOrderId: Int
        get() {
            var maxOrderId = 0
            val uri = Uri.withAppendedPath(Tasks.CONTENT_URI, "max_order")
            val cursor = mContentResolver.query(
                uri, null, null,
                null, null
            )
            if (cursor != null) {
                cursor.moveToFirst()
                val idString = cursor.getString(0)
                if (idString != null) {
                    maxOrderId = idString.toInt()
                }
                cursor.close()
            }
            return maxOrderId
        }
    private val backupFilePath: String
        get() = BACKUP_DIR_PATH + BACKUP_FILE_NAME

    fun backupDataBase(): Boolean {
        val dir = File(BACKUP_DIR_PATH)
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "backup Database failed.")
                }
                return false
            }
        }
        return copyDataBase(mDatabaseHelper.databasePath(), backupFilePath)
    }

    fun restoreDatabase(): Boolean {
        val success = copyDataBase(backupFilePath, mDatabaseHelper.databasePath())
        if (success) {
            mContentResolver.notifyChange(KeepdoProvider.BASE_CONTENT_URI, null)
        }
        return success
    }

    @Synchronized
    private fun copyDataBase(fromPath: String, toPath: String): Boolean {
        var success = false
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = FileInputStream(fromPath)
            outputStream = FileOutputStream(toPath)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            success = true
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Exception: OutputStream, e = $e")
            }
        } finally {
            try {
                outputStream?.close()
                inputStream?.close()
            } catch (e: IOException) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Exception: InputStream/OutputStream, e = $e")
                }
            }
        }
        return success
    }

    companion object {
        private const val TAG = "#KEEPDO_DB_ADAPTER: "

        // Backup & Restore
        private const val BACKUP_FILE_NAME = "/keepdo.db"
        private const val BACKUP_DIR_NAME = "/keepdo"
        private val BACKUP_DIR_PATH =
            Environment.getExternalStorageDirectory().path + BACKUP_DIR_NAME
        private const val SDF_PATTERN_YMD = "yyyy-MM-dd"
        private const val SDF_PATTERN_YM = "yyyy-MM"
        private var INSTANCE: DatabaseAdapter? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): DatabaseAdapter {
            if (INSTANCE == null) {
                INSTANCE = DatabaseAdapter(context)
            }
            return INSTANCE as DatabaseAdapter
        }

        @JvmStatic
        fun backupFileName(): String {
            return BACKUP_FILE_NAME
        }

        @JvmStatic
        fun backupDirName(): String {
            return BACKUP_DIR_NAME
        }

        @JvmStatic
        fun backupDirPath(): String {
            return BACKUP_DIR_PATH
        }
    }

}