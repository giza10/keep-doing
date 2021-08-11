package com.hkb48.keepdo

import android.app.Application
import com.hkb48.keepdo.db.TaskDatabase
import com.hkb48.keepdo.ui.settings.Settings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KeepdoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Settings.initialize(this)
    }

    fun getDatabase(): TaskDatabase {
        return TaskDatabase.getInstance(this)
    }

    fun getDateChangeTimeManager(): DateChangeTimeManager {
        return DateChangeTimeManager(this)
    }
}