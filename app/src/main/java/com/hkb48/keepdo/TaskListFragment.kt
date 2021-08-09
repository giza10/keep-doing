package com.hkb48.keepdo

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hkb48.keepdo.calendar.TaskCalendarActivity
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.settings.Settings
import com.hkb48.keepdo.util.CompatUtil
import com.hkb48.keepdo.util.DateComparator
import com.hkb48.keepdo.viewmodel.TaskViewModel
import com.hkb48.keepdo.viewmodel.TaskViewModelFactory
import com.hkb48.keepdo.widget.TasksWidgetProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.collections.ArrayList

class TaskListFragment : Fragment() {
    private var mDataList: MutableList<TaskListItem> = ArrayList()
    private lateinit var mCheckSound: CheckSoundPlayer
    private val mAdapter = TaskAdapter()
    private lateinit var mTaskList: List<Task>
    private lateinit var mTaskListView: ListView

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
    private val mOnDateChangedListener: DateChangeTimeManager.OnDateChangedListener =
        object : DateChangeTimeManager.OnDateChangedListener {
            override fun onDateChanged() {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.date_changed)
                    .setPositiveButton(
                        R.string.dialog_ok
                    ) { _: DialogInterface?, _: Int ->
                        updateTaskListWithLifecycleScope()
                    }.setCancelable(false)
                    .create().show()
            }
        }
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    TaskSettingActivity::class.java
                )
            )
        }

        Settings.registerOnChangedListener(mSettingsChangedListener)

        // Cancel notification (if displayed)
        NotificationController.cancelReminder(requireContext())
        mTaskListView = view.findViewById(R.id.mainListView)
        mTaskListView.adapter = mAdapter
        mTaskListView.emptyView = view.findViewById(R.id.empty)
        mTaskListView.emptyView.visibility = View.INVISIBLE
        mTaskListView.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                // Show calendar view
                val item = mDataList[position]
                if (item.type == TYPE_ITEM) {
                    val task = item.data as Task
                    startActivity(Intent(
                        requireContext(), TaskCalendarActivity::class.java
                    ).apply {
                        putExtra(TaskCalendarActivity.EXTRA_TASK_ID, task._id)
                    })
                }
            }
        registerForContextMenu(mTaskListView)

        subscribeToModel(taskViewModel)

        mCheckSound = CheckSoundPlayer(requireContext())

        if (CompatUtil.isNotificationChannelSupported) {
            NotificationController.createNotificationChannel(requireContext())
        }

        (requireActivity().application as KeepdoApplication).getDateChangeTimeManager()
            .registerOnDateChangedListener(mOnDateChangedListener)
    }

    override fun onResume() {
        if (mContentsUpdated) {
            updateTaskListWithLifecycleScope()
        }
        mCheckSound.load()
        super.onResume()
    }

    override fun onPause() {
        mCheckSound.unload()
        super.onPause()
    }

    override fun onDestroyView() {
        (requireActivity().application as KeepdoApplication).getDateChangeTimeManager()
            .unregisterOnDateChangedListener(mOnDateChangedListener)
        super.onDestroyView()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        view: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val adapterInfo = menuInfo as AdapterView.AdapterContextMenuInfo
        val listView = view as ListView
        val taskListItem = listView
            .getItemAtPosition(adapterInfo.position) as TaskListItem
        val task = taskListItem.data as Task
        menu.setHeaderTitle(task.name)
        menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.edit_task)
        menu.add(0, CONTEXT_MENU_DELETE, 1, R.string.delete_task)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = (item.menuInfo as AdapterView.AdapterContextMenuInfo)
        val taskListItem = mAdapter.getItem(info.position) as TaskListItem
        val task = taskListItem.data as Task
        val taskId = task._id!!
        return when (item.itemId) {
            CONTEXT_MENU_EDIT -> {
                startActivity(Intent(
                    requireContext(), TaskSettingActivity::class.java
                ).apply {
                    putExtra(TaskSettingActivity.EXTRA_TASK_ID, taskId)
                })
                true
            }
            CONTEXT_MENU_DELETE -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.delete_confirmation)
                    .setPositiveButton(
                        R.string.dialog_ok
                    ) { _: DialogInterface?, _: Int ->
                        // Cancel the alarm for Reminder before deleting the task.
                        ReminderManager.cancelAlarm(requireContext(), taskId)
                        lifecycleScope.launch {
                            taskViewModel.deleteTask(taskId)
                            TasksWidgetProvider.notifyDatasetChanged(requireContext())
                        }
                    }
                    .setNegativeButton(
                        R.string.dialog_cancel
                    ) { _: DialogInterface?, _: Int -> }.setCancelable(true).create()
                    .show()
                true
            }
            else -> {
                Toast.makeText(requireContext(), "default", Toast.LENGTH_SHORT).show()
                super.onContextItemSelected(item)
            }
        }
    }

    private fun subscribeToModel(model: TaskViewModel) {
        model.getObservableTaskList().observe(viewLifecycleOwner, { taskList ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG_KEEPDO, "Task database updated")
            }
            if (taskList.isEmpty()) {
                mTaskListView.emptyView.visibility = View.VISIBLE
            }
            mTaskList = taskList
            updateTaskListWithLifecycleScope()
        })
        model.getObservableDoneStatusList().observe(viewLifecycleOwner, {
            updateTaskListWithLifecycleScope()
        })
    }

    private val mutex = Mutex()

    private fun updateTaskListWithLifecycleScope() = lifecycleScope.launch {
        updateTaskList()
    }

    /**
     * Update the task list view with latest DB information.
     */
    private suspend fun updateTaskList() {
        if (::mTaskList.isInitialized.not()) {
            return
        }

        mutex.withLock {
            val dataList: MutableList<TaskListItem> = ArrayList()
            val taskListToday: MutableList<Task> = ArrayList()
            val taskListNotToday: MutableList<Task> = ArrayList()
            val dayOfWeek = DateChangeTimeUtil.dateTimeCalendar[Calendar.DAY_OF_WEEK]

            for (task in mTaskList) {
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
                dataList.add(taskListItem)
                for (task in taskListToday) {
                    val taskId = task._id!!
                    val lastDoneDate = taskViewModel.getLastDoneDate(taskId)
                    val comboCount = taskViewModel.getComboCount(taskId)
                    taskListItem = TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount)
                    dataList.add(taskListItem)
                }
            }
            if (taskListNotToday.size > 0) {
                // Dummy Task for header on the ListView
                val header = TaskListHeader()
                header.title = getString(R.string.tasklist_header_other_task)
                var taskListItem = TaskListItem(TYPE_HEADER, header, null, 0)
                dataList.add(taskListItem)
                for (task in taskListNotToday) {
                    val taskId = task._id!!
                    val lastDoneDate = taskViewModel.getLastDoneDate(taskId)
                    val comboCount = taskViewModel.getComboCount(taskId)
                    taskListItem = TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount)
                    dataList.add(taskListItem)
                }
            }
            mDataList = dataList
            mAdapter.notifyDataSetChanged()
            mContentsUpdated = false
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
                    lifecycleScope.launch {
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
                        ReminderManager.setAlarm(requireContext(), taskId)
                        TasksWidgetProvider.notifyDatasetChanged(requireContext())
                        if (checked1) {
                            mCheckSound.play()
                        }
                    }
                }
            } else if (isTask.not() && (headerViewHolder != null)) {
                val taskListHeader = taskListItem.data as TaskListHeader
                headerViewHolder.textView1?.text = taskListHeader.title
            }
            return view
        }

        private suspend fun updateModel(task: Task, position: Int) {
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
                requireContext(),
                R.color.tasklist_last_donedate
            )
            lastDoneDateTextView?.setTextColor(colorLastDoneDate)
            if (checked) {
                imageView?.setImageResource(Settings.doneIconId)
                val comboCount = taskListItem.comboCount
                if (comboCount > 1) {
                    lastDoneDateTextView?.setTextColor(
                        CompatUtil.getColor(
                            requireContext(),
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
                                requireContext(),
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

        private const val TAG_KEEPDO = "#LOG_KEEPDO: "
    }
}