package com.hkb48.keepdo

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log
import com.hkb48.keepdo.settings.Settings
import java.text.SimpleDateFormat
import java.util.*

class KeepdoProvider : ContentProvider() {
    companion object {
        private const val TAG = "KeepdoProvider"
        private const val AUTHORITY = "com.hkb48.keepdo.keepdoprovider"

        val BASE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private val sTasksProjectionMap = HashMap<String, String>()
        private val sMaxTaskProjectionMap = HashMap<String, String>()
        private val sTaskCompletionProjectionMap = HashMap<String, String>()
        private val sMaxTaskCompletionProjectionMap = HashMap<String, String>()
        private val sMinTaskCompletionProjectionMap = HashMap<String, String>()

        init {
            sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI, Tasks.TABLE_LIST)
            sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI + "/#", Tasks.TABLE_ID)
            sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI + "/max_order", Tasks.MAX_ORDER_ID)
            sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI, TaskCompletion.TABLE_LIST)
            sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI + "/#", TaskCompletion.TABLE_ID)
            sURIMatcher.addURI(
                AUTHORITY,
                TaskCompletion.TABLE_URI + "/#/max",
                TaskCompletion.MAX_COMPLETION_DATE_ID
            )
            sURIMatcher.addURI(
                AUTHORITY,
                TaskCompletion.TABLE_URI + "/#/min",
                TaskCompletion.MIN_COMPLETION_DATE_ID
            )
            sURIMatcher.addURI(AUTHORITY, DateChangeTime.TABLE_URI, DateChangeTime.TABLE_LIST)
            sURIMatcher.addURI(AUTHORITY, DateChangeTime.TABLE_URI + "/#", DateChangeTime.TABLE_ID)
        }

        init {
            sTasksProjectionMap[BaseColumns._ID] = BaseColumns._ID
            sTasksProjectionMap[Tasks.TASK_NAME] = Tasks.TASK_NAME
            sTasksProjectionMap[Tasks.FREQUENCY_MON] = Tasks.FREQUENCY_MON
            sTasksProjectionMap[Tasks.FREQUENCY_TUE] = Tasks.FREQUENCY_TUE
            sTasksProjectionMap[Tasks.FREQUENCY_WEN] = Tasks.FREQUENCY_WEN
            sTasksProjectionMap[Tasks.FREQUENCY_THR] = Tasks.FREQUENCY_THR
            sTasksProjectionMap[Tasks.FREQUENCY_FRI] = Tasks.FREQUENCY_FRI
            sTasksProjectionMap[Tasks.FREQUENCY_SAT] = Tasks.FREQUENCY_SAT
            sTasksProjectionMap[Tasks.FREQUENCY_SUN] = Tasks.FREQUENCY_SUN
            sTasksProjectionMap[Tasks.TASK_CONTEXT] = Tasks.TASK_CONTEXT
            sTasksProjectionMap[Tasks.REMINDER_ENABLED] = Tasks.REMINDER_ENABLED
            sTasksProjectionMap[Tasks.REMINDER_TIME] = Tasks.REMINDER_TIME
            sTasksProjectionMap[Tasks.TASK_LIST_ORDER] = Tasks.TASK_LIST_ORDER
        }

        init {
            sMaxTaskProjectionMap["MAX(" + Tasks.TASK_LIST_ORDER + ")"] =
                "MAX(" + Tasks.TASK_LIST_ORDER + ")"
        }

        init {
            sTaskCompletionProjectionMap[BaseColumns._ID] = BaseColumns._ID
            sTaskCompletionProjectionMap[TaskCompletion.TASK_NAME_ID] = TaskCompletion.TASK_NAME_ID
            sTaskCompletionProjectionMap[TaskCompletion.TASK_COMPLETION_DATE] =
                TaskCompletion.TASK_COMPLETION_DATE
        }

        init {
            sMaxTaskCompletionProjectionMap["MAX(" + TaskCompletion.TASK_COMPLETION_DATE + ")"] =
                "MAX(" + TaskCompletion.TASK_COMPLETION_DATE + ")"
        }

        init {
            sMinTaskCompletionProjectionMap["MIN(" + TaskCompletion.TASK_COMPLETION_DATE + ")"] =
                "MIN(" + TaskCompletion.TASK_COMPLETION_DATE + ")"
        }
    }

    private lateinit var mContentResolver: ContentResolver
    private lateinit var mOpenHelper: DatabaseHelper
    override fun onCreate(): Boolean {
        // Assumes that any failures will be reported by a thrown exception.
        val context = context!!
        mContentResolver = context.contentResolver
        mOpenHelper = DatabaseHelper(context.applicationContext)
        Settings.initialize(context)
        return false
    }

    override fun getType(uri: Uri): String {
        //TODO: have to be corrected
        return when (sURIMatcher.match(uri)) {
            Tasks.TABLE_LIST -> Tasks.TABLE_LIST.toString()
            Tasks.TABLE_ID -> Tasks.TABLE_ID.toString()
            TaskCompletion.TABLE_LIST -> TaskCompletion.TABLE_LIST.toString()
            TaskCompletion.TABLE_ID -> TaskCompletion.TABLE_ID.toString()
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "insert: uri=$uri  values=[$values]")
        }
        // Constructs a new query builder and sets its table name
        val tableName: String = when (sURIMatcher.match(uri)) {
            Tasks.TABLE_LIST, Tasks.TABLE_ID -> Tasks.TABLE_NAME
            TaskCompletion.TABLE_LIST, TaskCompletion.TABLE_ID -> TaskCompletion.TABLE_NAME
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        val db = mOpenHelper.writableDatabase!!
        val newRowId = db.insertOrThrow(tableName, null, values)
        val newUri = ContentUris.withAppendedId(uri, newRowId)
        mContentResolver.notifyChange(newUri, null)
        return newUri
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(
                TAG, "query: uri=" + uri + "  projection=" + Arrays.toString(projection) +
                        "  selection=[" + selection + "]  args=" + Arrays.toString(selectionArgs) +
                        "  order=[" + sortOrder + "]"
            )
        }

        // Constructs a new query builder and sets its table name
        val qb = SQLiteQueryBuilder()
        if (uri.getQueryParameter("distinct") != null) {
            qb.isDistinct = true
        }
        when (sURIMatcher.match(uri)) {
            Tasks.TABLE_LIST -> {
                qb.tables = Tasks.TABLE_NAME
                qb.projectionMap = sTasksProjectionMap
                qb.isStrict = true
            }
            Tasks.TABLE_ID -> {
                qb.tables = Tasks.TABLE_NAME
                qb.appendWhere(BaseColumns._ID + "=" + parseTaskIdFromUri(uri))
                qb.projectionMap = sTasksProjectionMap
                qb.isStrict = true
            }
            Tasks.MAX_ORDER_ID -> {
                qb.tables = Tasks.TABLE_NAME
                qb.projectionMap = sMaxTaskProjectionMap
                qb.isStrict = true
            }
            TaskCompletion.TABLE_LIST -> {
                qb.tables = TaskCompletion.TABLE_NAME
                qb.projectionMap = sTaskCompletionProjectionMap
                qb.isStrict = true
            }
            TaskCompletion.TABLE_ID -> {
                qb.tables = TaskCompletion.TABLE_NAME
                qb.appendWhere(TaskCompletion.TASK_NAME_ID + "=" + parseTaskIdFromUri(uri))
                qb.projectionMap = sTaskCompletionProjectionMap
                qb.isStrict = true
            }
            TaskCompletion.MAX_COMPLETION_DATE_ID -> {
                qb.tables = TaskCompletion.TABLE_NAME
                qb.appendWhere(TaskCompletion.TASK_NAME_ID + "=" + parseTaskIdFromUri(uri))
                qb.projectionMap = sMaxTaskCompletionProjectionMap
                qb.isStrict = true
            }
            TaskCompletion.MIN_COMPLETION_DATE_ID -> {
                qb.tables = TaskCompletion.TABLE_NAME
                qb.appendWhere(TaskCompletion.TASK_NAME_ID + "=" + parseTaskIdFromUri(uri))
                qb.projectionMap = sMinTaskCompletionProjectionMap
                qb.isStrict = true
            }
            DateChangeTime.TABLE_LIST, DateChangeTime.TABLE_ID -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
                val mc = MatrixCursor(
                    arrayOf(
                        DateChangeTime.ADJUSTED_DATE,
                        DateChangeTime.NEXT_DATE_CHANGE_TIME
                    )
                )
                val today = DateChangeTimeUtil.dateTime
                val dateChangeTime = DateChangeTimeUtil.dateChangeTime
                val dateChangeTimeCalendar = DateChangeTimeUtil.dateTimeCalendar
                dateChangeTimeCalendar.add(Calendar.DATE, 1)
                dateChangeTimeCalendar[Calendar.HOUR_OF_DAY] = dateChangeTime.hourOfDay
                dateChangeTimeCalendar[Calendar.MINUTE] = dateChangeTime.minute
                dateChangeTimeCalendar[Calendar.SECOND] = 0
                dateChangeTimeCalendar[Calendar.MILLISECOND] = 0
                mc.addRow(
                    arrayOf<Any>(
                        dateFormat.format(today),
                        dateChangeTimeCalendar.timeInMillis
                    )
                )
                return mc
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        val db = mOpenHelper.readableDatabase!!
        val c = qb.query(
            db, projection, selection, selectionArgs,
            null /* no group */, null /* no filter */, sortOrder
        )!!
        c.setNotificationUri(mContentResolver, uri)
        return c
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(
                TAG, "update: uri=" + uri + "  selection=[" + selection + "]  args="
                        + Arrays.toString(selectionArgs) + "  values=[" + values + "]"
            )
        }
        if (sURIMatcher.match(uri) == Tasks.TABLE_ID) {
            val db = mOpenHelper.writableDatabase!!
            val id = db.update(
                Tasks.TABLE_NAME,
                values,
                BaseColumns._ID + "=?",
                arrayOf(parseTaskIdFromUri(uri).toString())
            )
            mContentResolver.notifyChange(uri, null)
            return id
        }
        throw IllegalArgumentException("Unknown URI $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(
                TAG, "delete: uri=" + uri + "  selection=[" + selection + "]  args="
                        + Arrays.toString(selectionArgs)
            )
        }
        val db = mOpenHelper.writableDatabase!!
        val id: Int
        return when (sURIMatcher.match(uri)) {
            Tasks.TABLE_ID -> {
                id = db.delete(
                    Tasks.TABLE_NAME,
                    BaseColumns._ID + "=?",
                    arrayOf(parseTaskIdFromUri(uri).toString())
                )
                mContentResolver.notifyChange(uri, null)
                id
            }
            TaskCompletion.TABLE_ID -> {
                id = if (selection != null) {
                    db.delete(
                        TaskCompletion.TABLE_NAME,
                        "(" + selection + ") AND (" + TaskCompletion.TASK_NAME_ID + "=?)",
                        arrayOf(
                            selectionArgs!![0], parseTaskIdFromUri(uri).toString()
                        )
                    )
                } else {
                    db.delete(
                        TaskCompletion.TABLE_NAME,
                        TaskCompletion.TASK_NAME_ID + "=?",
                        arrayOf(parseTaskIdFromUri(uri).toString())
                    )
                }
                mContentResolver.notifyChange(uri, null)
                id
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    /**
     * Parses the call Id from the given uri, assuming that this is a uri that
     * matches TABLE_ID. For other uri types the behaviour is undefined.
     *
     * @throws IllegalArgumentException if the id included in the Uri is not a valid long value.
     */
    private fun parseTaskIdFromUri(uri: Uri): Long {
        return try {
            uri.pathSegments[1].toLong()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid call id in uri: $uri", e)
        }
    }

    // Task table
    internal object Tasks : BaseColumns {
        // Incoming URI matches the main table URI pattern
        const val TABLE_LIST = 10

        // Incoming URI matches the main table row ID URI pattern
        const val TABLE_ID = 11
        const val MAX_ORDER_ID = 12
        const val TABLE_NAME = "table_tasks"
        const val TABLE_URI = "table_task_uri"

        @JvmField
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI)
        const val TASK_NAME = "task_name"
        const val FREQUENCY_MON = "mon_frequency"
        const val FREQUENCY_TUE = "tue_frequency"
        const val FREQUENCY_WEN = "wen_frequency"
        const val FREQUENCY_THR = "thr_frequency"
        const val FREQUENCY_FRI = "fri_frequency"
        const val FREQUENCY_SAT = "sat_frequency"
        const val FREQUENCY_SUN = "sun_frequency"
        const val TASK_CONTEXT = "task_context"
        const val REMINDER_ENABLED = "reminder_enabled"
        const val REMINDER_TIME = "reminder_time"
        const val TASK_LIST_ORDER = "task_list_order"
    }

    // Task Completion table
    internal object TaskCompletion : BaseColumns {
        // Incoming URI matches the main table URI pattern
        const val TABLE_LIST = 20

        // Incoming URI matches the main table row ID URI pattern
        const val TABLE_ID = 21
        const val MAX_COMPLETION_DATE_ID = 23
        const val MIN_COMPLETION_DATE_ID = 24
        const val TABLE_NAME = "table_completions"
        const val TABLE_URI = "table_completion_uri"

        @JvmField
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI)
        const val TASK_NAME_ID = "task_id"
        const val TASK_COMPLETION_DATE = "completion_date"
    }

    object DateChangeTime : BaseColumns {
        const val NEXT_DATE_CHANGE_TIME = "next_date_change_time"

        // Incoming URI matches the main table URI pattern
        const val TABLE_LIST = 30

        // Incoming URI matches the main table row ID URI pattern
        const val TABLE_ID = 31
        const val TABLE_URI = "table_datechangetime_uri"

        @JvmField
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI)
        const val ADJUSTED_DATE = "date"
    }
}