package com.hkb48.keepdo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hkb48.keepdo.db.dao.TaskCompletionDao
import com.hkb48.keepdo.db.dao.TaskDao
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion

@Database(entities = [Task::class, TaskCompletion::class], version = 4)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    var databasePath: String? = null

    companion object {
        private const val DATABASE_NAME = "keepdo_tracker.db"

        @Volatile
        private var instance: TaskDatabase? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                TaskDatabase::class.java, DATABASE_NAME
            )
                .setJournalMode(JournalMode.TRUNCATE)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .build().also {
                    it.databasePath = context.getDatabasePath(DATABASE_NAME).path
                    instance = it
                }

        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE ${Task.TABLE_NAME} ADD COLUMN ${Task.DESCRIPTION} TEXT""".trimIndent())
                database.execSQL("""ALTER TABLE ${Task.TABLE_NAME} ADD COLUMN ${Task.REMINDER_ENABLED} TEXT""".trimIndent())
                database.execSQL("""ALTER TABLE ${Task.TABLE_NAME} ADD COLUMN ${Task.REMINDER_TIME} TEXT""".trimIndent())
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """ALTER TABLE ${Task.TABLE_NAME} ADD COLUMN ${Task.TASK_LIST_ORDER} INTEGER DEFAULT 0""".trimIndent()
                )
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS new_table_tasks (
                    _id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${Task.TASK_NAME} TEXT NOT NULL,
                    ${Task.FREQUENCY_MON} TEXT NOT NULL DEFAULT 'true',
                    ${Task.FREQUENCY_TUE} TEXT NOT NULL DEFAULT 'true',
                    ${Task.FREQUENCY_WED} TEXT NOT NULL DEFAULT 'true',
                    ${Task.FREQUENCY_THR} TEXT NOT NULL DEFAULT 'true',
                    ${Task.FREQUENCY_FRI} TEXT NOT NULL DEFAULT 'true',
                    ${Task.FREQUENCY_SAT} TEXT NOT NULL DEFAULT 'true',
                    ${Task.FREQUENCY_SUN} TEXT NOT NULL DEFAULT 'true',
                    ${Task.DESCRIPTION} TEXT,
                    ${Task.REMINDER_ENABLED} TEXT NOT NULL DEFAULT 'false',
                    ${Task.REMINDER_TIME} INTEGER,
                    ${Task.TASK_LIST_ORDER} INTEGER NOT NULL DEFAULT 0);""".trimIndent()
                )
                database.execSQL("""INSERT INTO new_table_tasks SELECT * FROM ${Task.TABLE_NAME}""".trimIndent())
                database.execSQL("""DROP TABLE ${Task.TABLE_NAME}""".trimIndent())
                database.execSQL("""ALTER TABLE new_table_tasks RENAME TO ${Task.TABLE_NAME}""".trimIndent())
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS new_table_completions (
                    _id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${TaskCompletion.TASK_NAME_ID} INTEGER NOT NULL CONSTRAINT 
                    ${TaskCompletion.TASK_NAME_ID} REFERENCES 
                    ${Task.TABLE_NAME} (_id) ON DELETE CASCADE,
                    ${TaskCompletion.TASK_COMPLETION_DATE} TEXT NOT NULL);""".trimIndent()
                )
                database.execSQL("""CREATE INDEX index_table_completions_task_id ON new_table_completions(${TaskCompletion.TASK_NAME_ID})""".trimIndent())
                database.execSQL("""INSERT INTO new_table_completions SELECT * FROM ${TaskCompletion.TABLE_NAME}""".trimIndent())
                database.execSQL("""DROP TABLE ${TaskCompletion.TABLE_NAME}""".trimIndent())
                database.execSQL("""ALTER TABLE new_table_completions RENAME TO ${TaskCompletion.TABLE_NAME}""".trimIndent())
            }
        }
    }
}
