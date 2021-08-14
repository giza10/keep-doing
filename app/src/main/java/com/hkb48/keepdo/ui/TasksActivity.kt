package com.hkb48.keepdo.ui

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.hkb48.keepdo.CheckSoundPlayer
import com.hkb48.keepdo.R
import com.hkb48.keepdo.ReminderManager
import com.hkb48.keepdo.databinding.ActivityMainBinding
import com.hkb48.keepdo.db.BackupManager
import com.hkb48.keepdo.ui.tasklist.TaskListViewModel
import com.hkb48.keepdo.widget.TasksWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TasksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val viewModel: TaskListViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var mCheckSound: CheckSoundPlayer

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
                    // Triggers onChange event of liveData to reflect UI
                    viewModel.refresh()

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
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        drawerLayout = binding.mainDrawerLayout
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.mainDrawerView.setupWithNavController(navController)
        binding.mainDrawerView.setNavigationItemSelectedListener(this)
    }

    public override fun onResume() {
        super.onResume()
        mCheckSound.load()
    }

    override fun onPause() {
        mCheckSound.unload()
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
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
        drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    private fun goToNavDrawerItem(itemId: Int) {
        when (itemId) {
            R.id.drawer_item_1 -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.taskSortFragment)
            }
            R.id.drawer_item_2 -> {
                // Todo: Tentative implementation
                showBackupRestoreDeviceDialog()
            }
            R.id.drawer_item_3 -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment)
            }
        }
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

    fun playCheckSound() {
        mCheckSound.play()
    }

    companion object {
        // Delay to launch nav drawer item, to allow close animation to play
        private const val NAVDRAWER_LAUNCH_DELAY: Long = 250
    }
}