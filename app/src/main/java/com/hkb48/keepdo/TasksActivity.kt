package com.hkb48.keepdo

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.hkb48.keepdo.db.BackupManager
import com.hkb48.keepdo.settings.SettingsActivity
import com.hkb48.keepdo.widget.TasksWidgetProvider


class TasksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private val mCreateBackupFileLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument()
        ) { uri ->
            if (uri != null) {
                try {
                    if (BackupManager.backup(applicationContext, uri)) {
                        Toast.makeText(
                            applicationContext,
                            R.string.backup_done, Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                }
            }
        }
    private val mPickRestoreFileLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                var success = false
                try {
                    success = BackupManager.restore(applicationContext, uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (success) {
                    // Todo: should trigger LiveData onChanged event thant recreating fragment
                    // Recreate the fragment to update the UI
                    createTaskListFragment()

                    ReminderManager.setAlarmForAll(applicationContext)
                    TasksWidgetProvider.notifyDatasetChanged(applicationContext)
                    Toast.makeText(
                        applicationContext,
                        R.string.restore_done, Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        R.string.restore_failed, Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        mDrawerLayout = findViewById(R.id.main_drawer_layout)
        mDrawerToggle = ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        findViewById<NavigationView>(R.id.main_drawer_view).setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            createTaskListFragment()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        Handler(Looper.getMainLooper()).postDelayed(
            { goToNavDrawerItem(itemId) },
            NAVDRAWER_LAUNCH_DELAY
        )
        mDrawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    private fun goToNavDrawerItem(itemId: Int) {
        when (itemId) {
            R.id.drawer_item_1 -> {
                startActivity(Intent(this, TaskSortingActivity::class.java))
            }
            R.id.drawer_item_2 -> {
                // Todo: Tentative implementation
                showBackupRestoreDeviceDialog()
            }
            R.id.drawer_item_3 -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    private fun createTaskListFragment() {
        val fragment = TaskListFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    /**
     * Backup & Restore
     */
    private fun showBackupRestoreDeviceDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.backup_restore))
            setSingleChoiceItems(
                R.array.dialog_choice_backup_restore, -1
            ) { dialog: DialogInterface, _: Int ->
                (dialog as AlertDialog).getButton(
                    AlertDialog.BUTTON_POSITIVE
                ).isEnabled = true
            }
            setNegativeButton(R.string.dialog_cancel, null)
            setPositiveButton(
                R.string.dialog_start
            ) { dialog: DialogInterface, _: Int ->
                when ((dialog as AlertDialog).listView
                    .checkedItemPosition) {
                    0 -> {
                        // execute backup
                        mCreateBackupFileLauncher.launch(BackupManager.backupFileName)
                    }
                    1 -> {
                        // execute restore
                        mPickRestoreFileLauncher.launch(("*/*"))
                    }
                    else -> {
                    }
                }
            }
            val alertDialog = show()
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
    }

    companion object {
        // Delay to launch nav drawer item, to allow close animation to play
        private const val NAVDRAWER_LAUNCH_DELAY: Long = 250
    }
}