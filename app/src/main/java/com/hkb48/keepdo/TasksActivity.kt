package com.hkb48.keepdo

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.hkb48.keepdo.calendar.TaskCalendarActivity
import com.hkb48.keepdo.db.BackupManager
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.settings.Settings
import com.hkb48.keepdo.settings.SettingsActivity
import com.hkb48.keepdo.util.CompatUtil
import com.hkb48.keepdo.util.DateComparator
import com.hkb48.keepdo.widget.TasksWidgetProvider
import java.util.*


class TasksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    DateChangeTimeManager.OnDateChangedListener {
    private val mDataList: MutableList<TaskListItem> = ArrayList()
    private val mCheckSound = CheckSoundPlayer(this)
    private val mAdapter = TaskAdapter()

    private var mContentsUpdated = false
    private val mSettingsChangedListener: Settings.OnChangedListener =
        object : Settings.OnChangedListener {
            override fun onDoneIconSettingChanged() {
                mContentsUpdated = true
            }

            override fun onDateChangeTimeSettingChanged() {
                mContentsUpdated = true
            }

            override fun onWeekStartDaySettingChanged() {
            }
        }
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(application)
    }

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
                    Toast.makeText(
                        applicationContext,
                        R.string.restore_done, Toast.LENGTH_LONG
                    ).show()
                    updateTaskList(taskViewModel.getTaskList())
                    ReminderManager.setAlarmForAll(applicationContext)
                    TasksWidgetProvider.notifyDatasetChanged(applicationContext)
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
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(
                Intent(
                    this@TasksActivity,
                    TaskSettingActivity::class.java
                )
            )
        }

        Settings.registerOnChangedListener(mSettingsChangedListener)
        (application as KeepdoApplication).getDateChangeTimeManager()
            .registerOnDateChangedListener(this)

        // Cancel notification (if displayed)
        NotificationController.cancelReminder(this)
        val taskListView = findViewById<ListView>(R.id.mainListView)
        taskListView.adapter = mAdapter
        taskListView.emptyView = findViewById(R.id.empty)
        taskListView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                // Show calendar view
                val item = mDataList[position]
                if (item.type == TYPE_ITEM) {
                    val task = item.data as Task
                    startActivity(Intent(
                        this@TasksActivity, TaskCalendarActivity::class.java
                    ).apply {
                        putExtra(TaskCalendarActivity.EXTRA_TASK_ID, task._id)
                    })
                }
            }

        subscribeToModel(taskViewModel)

        if (CompatUtil.isNotificationChannelSupported) {
            NotificationController.createNotificationChannel(applicationContext)
        }
        registerForContextMenu(taskListView)
        updateTaskList(taskViewModel.getTaskList())
    }

    public override fun onResume() {
        if (mContentsUpdated) {
            updateTaskList(taskViewModel.getTaskList())
        }
        mCheckSound.load()
        super.onResume()
    }

    public override fun onPause() {
        mCheckSound.unload()
        super.onPause()
    }

    public override fun onDestroy() {
        (application as KeepdoApplication).getDateChangeTimeManager()
            .unregisterOnDateChangedListener(
                this
            )
        super.onDestroy()
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

    override fun onCreateContextMenu(
        menu: ContextMenu, view: View,
        menuInfo: ContextMenuInfo
    ) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val adapterInfo = menuInfo as AdapterContextMenuInfo
        val listView = view as ListView
        val taskListItem = listView
            .getItemAtPosition(adapterInfo.position) as TaskListItem
        val task = taskListItem.data as Task
        menu.setHeaderTitle(task.name)
        menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.edit_task)
        menu.add(0, CONTEXT_MENU_DELETE, 1, R.string.delete_task)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = (item.menuInfo as AdapterContextMenuInfo)
        val taskListItem = mAdapter.getItem(info.position) as TaskListItem
        val task = taskListItem.data as Task
        val taskId = task._id!!
        return when (item.itemId) {
            CONTEXT_MENU_EDIT -> {
                startActivity(Intent(
                    this@TasksActivity, TaskSettingActivity::class.java
                ).apply {
                    putExtra(TaskSettingActivity.EXTRA_TASK_ID, taskId)
                })
                true
            }
            CONTEXT_MENU_DELETE -> {
                AlertDialog.Builder(this)
                    .setMessage(R.string.delete_confirmation)
                    .setPositiveButton(
                        R.string.dialog_ok
                    ) { _: DialogInterface?, _: Int ->
                        // Cancel the alarm for Reminder before deleting the task.
                        ReminderManager.cancelAlarm(applicationContext, taskId)
                        taskViewModel.deleteTask(taskId)
                        updateTaskList(taskViewModel.getTaskList())
                        TasksWidgetProvider.notifyDatasetChanged(applicationContext)
                    }
                    .setNegativeButton(
                        R.string.dialog_cancel
                    ) { _: DialogInterface?, _: Int -> }.setCancelable(true).create()
                    .show()
                true
            }
            else -> {
                Toast.makeText(this, "default", Toast.LENGTH_SHORT).show()
                super.onContextItemSelected(item)
            }
        }
    }

    private fun subscribeToModel(model: TaskViewModel) {
        model.taskLiveData.observe(this, {
            updateTaskList(it)
        })
        model.doneStatusLiveData.observe(this, {
            updateTaskList(taskViewModel.getTaskList())
        })
    }

    /**
     * Update the task list view with latest DB information.
     */
    private fun updateTaskList(taskList: List<Task>) {
        val taskListToday: MutableList<Task> = ArrayList()
        val taskListNotToday: MutableList<Task> = ArrayList()
        val dayOfWeek = DateChangeTimeUtil.dateTimeCalendar[Calendar.DAY_OF_WEEK]
        mDataList.clear()
        for (task in taskList) {
            if (Recurrence.getFromTask(task).isValidDay(dayOfWeek)) {
                taskListToday.add(task)
            } else {
                taskListNotToday.add(task)
            }
        }
        if (taskListToday.size > 0) {
            // Dummy Task for header on the ListView
            val header = TaskListHeader()
            header.title = getString(R.string.tasklist_header_today_task)
            var taskListItem = TaskListItem(TYPE_HEADER, header, null, 0)
            mDataList.add(taskListItem)
            for (task in taskListToday) {
                val taskId = task._id!!
                val lastDoneDate = taskViewModel.getLastDoneDate(taskId)
                val comboCount = taskViewModel.getComboCount(taskId)
                taskListItem = TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount)
                mDataList.add(taskListItem)
            }
        }
        if (taskListNotToday.size > 0) {
            // Dummy Task for header on the ListView
            val header = TaskListHeader()
            header.title = getString(R.string.tasklist_header_other_task)
            var taskListItem = TaskListItem(TYPE_HEADER, header, null, 0)
            mDataList.add(taskListItem)
            for (task in taskListNotToday) {
                val taskId = task._id!!
                val lastDoneDate = taskViewModel.getLastDoneDate(taskId)
                val comboCount = taskViewModel.getComboCount(taskId)
                taskListItem = TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount)
                mDataList.add(taskListItem)
            }
        }
        mAdapter.notifyDataSetChanged()
        mContentsUpdated = false
    }

    override fun onDateChanged() {
        AlertDialog.Builder(this)
            .setMessage(R.string.date_changed)
            .setPositiveButton(
                R.string.dialog_ok
            ) { _: DialogInterface?, _: Int ->
                updateTaskList(taskViewModel.getTaskList())
            }.setCancelable(false)
            .create().show()
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
                        mCreateBackupFileLauncher.launch(BackupManager.BACKUP_FILE_NAME)
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

    private class TaskListItem(
        val type: Int,
        val data: Any,
        val lastDoneDate: Date?,
        val comboCount: Int
    )

    private class TaskListHeader {
        var title: String? = null
    }

    private inner class TaskAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return mDataList.size
        }

        override fun getItem(position: Int): Any {
            return mDataList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            val item = getItem(position) as TaskListItem
            return item.type
        }

        override fun isEnabled(position: Int): Boolean {
            return getItemViewType(position) == TYPE_ITEM
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val inflater = LayoutInflater.from(parent.context)
            var view = convertView
            val taskListItem = getItem(position) as TaskListItem
            val isTask = isEnabled(position)
            var createView = false
            var headerViewHolder: HeaderViewHolder? = null
            var itemViewHolder: ItemViewHolder? = null

            // Check if it's necessary to create view or re-use
            if (view == null) {
                createView = true
            } else {
                val viewHolder = view.tag as ViewHolder
                if (viewHolder.viewType != getItemViewType(position)) {
                    createView = true
                } else {
                    if (isTask) {
                        itemViewHolder = viewHolder as ItemViewHolder
                    } else {
                        headerViewHolder = viewHolder as HeaderViewHolder
                    }
                }
            }
            if (createView) {
                if (isTask) {
                    view = inflater.inflate(R.layout.task_list_row, parent, false)
                    itemViewHolder = ItemViewHolder()
                    itemViewHolder.viewType = TYPE_ITEM
                    itemViewHolder.imageView = view.findViewById(R.id.taskListItemCheck)
                    itemViewHolder.textView1 = view.findViewById(R.id.taskName)
                    itemViewHolder.textView2 = view.findViewById(R.id.taskContext)
                    itemViewHolder.recurrenceView = view.findViewById(R.id.recurrenceView)
                    itemViewHolder.lastDoneDateTextView = view.findViewById(R.id.taskLastDoneDate)
                    itemViewHolder.imageAlarm = view.findViewById(R.id.AlarmIcon)
                    itemViewHolder.textAlarm = view.findViewById(R.id.AlarmText)
                    view.tag = itemViewHolder
                } else {
                    headerViewHolder = HeaderViewHolder()
                    view = inflater.inflate(R.layout.task_list_header, parent, false)
                    headerViewHolder.viewType = TYPE_HEADER
                    headerViewHolder.textView1 = view.findViewById(R.id.textView1)
                    view.tag = headerViewHolder
                }
            }
            if (isTask && (itemViewHolder != null)) {
                val task = taskListItem.data as Task
                val textView1 = itemViewHolder.textView1
                val taskName = task.name
                textView1?.text = taskName
                val textView2 = itemViewHolder.textView2
                val taskContext = task.context
                if (taskContext != null && taskContext.isNotEmpty()) {
                    textView2?.text = taskContext
                    textView2?.visibility = View.VISIBLE
                } else {
                    textView2?.visibility = View.GONE
                }
                val recurrenceView = itemViewHolder.recurrenceView
                recurrenceView?.update(Recurrence.getFromTask(task))
                val imageAlarm = itemViewHolder.imageAlarm
                val textAlarm = itemViewHolder.textAlarm
                val reminder = Reminder(task.reminderEnabled, task.reminderTime ?: 0)
                if (reminder.enabled) {
                    val alarmStr = String.format(
                        Locale.getDefault(),
                        "%1$02d",
                        reminder.hourOfDay
                    ) + ":" + String.format(
                        Locale.getDefault(), "%1$02d", reminder.minute
                    )
                    textAlarm?.text = alarmStr
                    imageAlarm?.visibility = View.VISIBLE
                    textAlarm?.visibility = View.VISIBLE
                } else {
                    imageAlarm?.visibility = View.GONE
                    textAlarm?.visibility = View.GONE
                }
                val imageView = itemViewHolder.imageView
                val today = DateChangeTimeUtil.dateTime
                val checked = DateComparator.equals(taskListItem.lastDoneDate, today)
                updateView(taskListItem, checked, itemViewHolder)
                imageView?.tag = position
                imageView?.setOnClickListener { v: View ->
                    val parent1 = v.parent as View
                    val itemViewHolder1 = ItemViewHolder()
                    itemViewHolder1.imageView = v as ImageView
                    itemViewHolder1.lastDoneDateTextView =
                        parent1.findViewById(R.id.taskLastDoneDate)
                    val position1 = v.getTag() as Int
                    var taskListItem1 = getItem(position1) as TaskListItem
                    val task1 = taskListItem1.data as Task
                    val taskId = task1._id!!
                    var checked1 = DateComparator.equals(taskListItem1.lastDoneDate, today)
                    checked1 = !checked1
                    taskViewModel.setDoneStatus(taskId, today, checked1)
                    updateModel(task1, position1)
                    taskListItem1 = getItem(position1) as TaskListItem
                    updateView(taskListItem1, checked1, itemViewHolder1)
                    ReminderManager.setAlarm(applicationContext, taskId)
                    TasksWidgetProvider.notifyDatasetChanged(applicationContext)
                    if (checked1) {
                        mCheckSound.play()
                    }
                }
            } else if (isTask.not() && (headerViewHolder != null)) {
                val taskListHeader = taskListItem.data as TaskListHeader
                headerViewHolder.textView1?.text = taskListHeader.title
            }
            return view
        }

        private fun updateModel(task: Task, position: Int) {
            val taskId = task._id!!
            val lastDoneDate = taskViewModel.getLastDoneDate(taskId)
            val comboCount = taskViewModel.getComboCount(taskId)
            val newTaskListItem = TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount)
            mDataList[position] = newTaskListItem
        }

        private fun updateView(
            taskListItem: TaskListItem, checked: Boolean,
            holder: ItemViewHolder
        ) {
            val today = DateChangeTimeUtil.dateTime
            val imageView = holder.imageView
            val lastDoneDateTextView = holder.lastDoneDateTextView
            val colorLastDoneDate = CompatUtil.getColor(
                applicationContext,
                R.color.tasklist_last_donedate
            )
            lastDoneDateTextView?.setTextColor(colorLastDoneDate)
            if (checked) {
                imageView?.setImageResource(Settings.doneIconId)
                val comboCount = taskListItem.comboCount
                if (comboCount > 1) {
                    lastDoneDateTextView?.setTextColor(
                        CompatUtil.getColor(
                            applicationContext,
                            R.color.tasklist_combo
                        )
                    )
                    lastDoneDateTextView?.text = getString(
                        R.string.tasklist_combo, comboCount
                    )
                } else {
                    lastDoneDateTextView?.setText(R.string.tasklist_lastdonedate_today)
                }
            } else {
                imageView?.setImageResource(Settings.notDoneIconId)
                val lastDoneDate = taskListItem.lastDoneDate
                if (lastDoneDate != null) {
                    val comboCount = taskListItem.comboCount
                    if (comboCount > 1) {
                        lastDoneDateTextView?.setTextColor(
                            CompatUtil.getColor(
                                applicationContext,
                                R.color.tasklist_combo
                            )
                        )
                        lastDoneDateTextView?.text = getString(
                            R.string.tasklist_combo,
                            comboCount
                        )
                    } else {
                        val diffDays = ((today.time - lastDoneDate.time)
                                / (1000 * 60 * 60 * 24).toLong()).toInt()
                        if (diffDays == 1) {
                            lastDoneDateTextView?.text =
                                getString(R.string.tasklist_lastdonedate_yesterday)
                        } else {
                            lastDoneDateTextView?.text = getString(
                                R.string.tasklist_lastdonedate_diffdays,
                                diffDays
                            )
                        }
                    }
                } else {
                    lastDoneDateTextView?.setText(R.string.tasklist_lastdonedate_notyet)
                }
            }
        }

        private open inner class ViewHolder {
            var viewType = 0
        }

        private inner class HeaderViewHolder : ViewHolder() {
            var textView1: TextView? = null
        }

        private inner class ItemViewHolder : ViewHolder() {
            var textView1: TextView? = null
            var textView2: TextView? = null
            var imageView: ImageView? = null
            var recurrenceView: RecurrenceView? = null
            var lastDoneDateTextView: TextView? = null
            var imageAlarm: ImageView? = null
            var textAlarm: TextView? = null
        }
    }

    companion object {
        // ID of context menu items
        private const val CONTEXT_MENU_EDIT = 0
        private const val CONTEXT_MENU_DELETE = 1
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        // Delay to launch nav drawer item, to allow close animation to play
        private const val NAVDRAWER_LAUNCH_DELAY: Long = 250
    }
}