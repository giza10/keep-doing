package com.hkb48.keepdo.calendar

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hkb48.keepdo.CheckSoundPlayer
import com.hkb48.keepdo.DatabaseAdapter
import com.hkb48.keepdo.KeepdoProvider
import com.hkb48.keepdo.R

class TaskCalendarActivity : AppCompatActivity() {
    private var mTaskId: Long = 0
    private var mModelUpdated = false
    private val mCheckSound = CheckSoundPlayer(this)
    private val mContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            mModelUpdated = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_calendar)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mTaskId = intent.getLongExtra("TASK-ID", -1)
        contentResolver.registerContentObserver(
            KeepdoProvider.BASE_CONTENT_URI,
            true,
            mContentObserver
        )
        mModelUpdated = true
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, CalendarFragment()).commit()
    }

    public override fun onResume() {
        if (mModelUpdated) {
            mModelUpdated = false
            updateTitle()
        }
        mCheckSound.load()
        super.onResume()
    }

    override fun onPause() {
        mCheckSound.unload()
        super.onPause()
    }

    public override fun onDestroy() {
        contentResolver.unregisterContentObserver(mContentObserver)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    fun playCheckSound() {
        mCheckSound.play()
    }

    private fun updateTitle() {
        DatabaseAdapter.getInstance(this).getTask(mTaskId)?.let {
            title = it.name
        }
    }
}