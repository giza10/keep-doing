package com.hkb48.keepdo.db

import android.content.Context
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hkb48.keepdo.KeepdoApplication
import kotlinx.coroutines.runBlocking
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {
    fun backup(context: Context, outputFile: Uri): Boolean {
        val db = (context as KeepdoApplication).getDatabase()
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var success = false
        try {
            inputStream = FileInputStream(db.databasePath)
            outputStream = context.contentResolver.openOutputStream(outputFile)!!
            success = runBlocking {
                // Execute checkpoint query to ensure all of the pending transactions are applied.
                db.taskDao().checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))
                db.taskCompletionDao()
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

    fun restore(context: Context, inputFile: Uri): Boolean {
        return if (isValidSQLite(context, inputFile)) {
            val db = (context as KeepdoApplication).getDatabase()
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            var success = false
            try {
                inputStream = context.contentResolver.openInputStream(inputFile)!!
                outputStream = FileOutputStream(db.databasePath)
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
