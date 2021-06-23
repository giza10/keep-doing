package com.hkb48.keepdo

import android.app.Application
import com.hkb48.keepdo.settings.Settings

class KeepdoApplication : Application() {
    val mDateChangeTimeManager = DateChangeTimeManager(this)
    override fun onCreate() {
        super.onCreate()
        Settings.initialize(this)
    }
}