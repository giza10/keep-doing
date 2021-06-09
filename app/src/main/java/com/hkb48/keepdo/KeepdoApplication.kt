package com.hkb48.keepdo

import android.app.Application
import com.hkb48.keepdo.settings.Settings

class KeepdoApplication : Application() {
    lateinit var mDateChangeTimeManager: DateChangeTimeManager
    override fun onCreate() {
        super.onCreate()
        Settings.initialize(applicationContext)
        mDateChangeTimeManager = DateChangeTimeManager(applicationContext)
    }
}