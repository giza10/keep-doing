package com.hkb48.keepdo.db

import android.content.Context
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.runBlocking
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BackupManager @Inject constructor(
    private val context: Context,
    private val database: TaskDatabase
) {
    fun backup(outputFile: Uri): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var success = false
        try {
            inputStream = FileInputStream(database.databasePath)
            outputStream = context.contentResolver.openOutputStream(outputFile)!!
            success = runBlocking {
                // Execute checkpoint query to ensure all of the pending transactions are applied.
                database.taskDao().checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))
                database.doneHistoryDao()
                    .checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))
                copy(inputStream, outputStream)
            }
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

    fun restore(inputFile: Uri): Boolean {
        return if (isValidSQLite(context, inputFile)) {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            var success = false
            try {
                inputStream = context.contentResolver.openInputStream(inputFile)!!
                outputStream = FileOutputStream(database.databasePath)
                success = copy(inputStream, outputStream)
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
    private fun copy(inputStream: InputStream, outputStream: OutputStream): Boolean {
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

    private fun isValidSQLite(context: Context, inputFile: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(inputFile)!!
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

    val backupFileName: String
        get() = SimpleDateFormat("'keepdo_db_'yyyyMMdd'.bin'", Locale.JAPAN).format(Date())
}
