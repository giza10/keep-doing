package com.hkb48.keepdo.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hkb48.keepdo.DateChangeTimeUtil
import com.hkb48.keepdo.R
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.ReminderManager
import com.hkb48.keepdo.databinding.CalendarDateBinding
import com.hkb48.keepdo.databinding.CalendarSubPageBinding
import com.hkb48.keepdo.databinding.CalendarWeekBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.settings.Settings
import com.hkb48.keepdo.viewmodel.TaskViewModel
import com.hkb48.keepdo.viewmodel.TaskViewModelFactory
import com.hkb48.keepdo.widget.TasksWidgetProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*


class CalendarGrid : Fragment() {
    private lateinit var mTask: Task
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(requireActivity().application)
    }

    private var mMonthOffset = 0
    private var _binding: CalendarSubPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CalendarSubPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = requireActivity().intent
        val taskId = intent.getIntExtra(TaskCalendarActivity.EXTRA_TASK_ID, Task.INVALID_TASKID)
        mMonthOffset =
            requireArguments().getInt(POSITION_KEY) - CalendarFragment.INDEX_OF_THIS_MONTH
        taskViewModel.getObservableTask(taskId).observe(viewLifecycleOwner, { task ->
            mTask = task
            buildCalendar()
        })
    }

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
    }

    override fun onStop() {
        setHasOptionsMenu(false)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, view: View,
        menuInfo: ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val date = view.tag as Date
        val sdf = SimpleDateFormat(
            getString(R.string.date_format), Locale.getDefault()
        )
        menu.setHeaderTitle(sdf.format(date))
        val imageView = CalendarDateBinding.bind(view).imageViewDone
        if (imageView.visibility == View.VISIBLE) {
            menu.add(0, CONTEXT_MENU_UNCHECK_DONE, 0, R.string.uncheck_done)
        } else {
            menu.add(0, CONTEXT_MENU_CHECK_DONE, 0, R.string.check_done)
        }
        menu.getItem(0).actionView = view
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        var consumed = false
        val view = item.actionView
        val imageView = CalendarDateBinding.bind(view).imageViewDone
        val selectedDate = view.tag as Date
        when (item.itemId) {
            CONTEXT_MENU_CHECK_DONE -> {
                showDoneIcon(imageView)
                lifecycleScope.launch {
                    taskViewModel.setDoneStatus(mTask._id!!, selectedDate, true)
                }
                (requireActivity() as TaskCalendarActivity).playCheckSound()
                consumed = true
            }
            CONTEXT_MENU_UNCHECK_DONE -> {
                hideDoneIcon(imageView)
                lifecycleScope.launch {
                    taskViewModel.setDoneStatus(mTask._id!!, selectedDate, false)
                }
                consumed = true
            }
            else -> {
            }
        }
        val today = DateChangeTimeUtil.date
        if (consumed && selectedDate.compareTo(today) == 0) {
            ReminderManager.setAlarm(requireContext(), mTask._id!!)
            TasksWidgetProvider.notifyDatasetChanged(requireContext())
        }
        return consumed || super.onContextItemSelected(item)
    }

    private fun addDayOfWeek() {
        val weeks = resources.getStringArray(R.array.week_names)
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        val rowParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        for (i in 0 until NUM_OF_DAYS_IN_WEEK) {
            val dayOfWeek = getDayOfWeek(i)
            val bindingWeek = CalendarWeekBinding.inflate(layoutInflater)
            val child = bindingWeek.root
            val textView1 = bindingWeek.textView1
            textView1.text = weeks[dayOfWeek - 1]
            when (dayOfWeek) {
                Calendar.SUNDAY -> textView1.setBackgroundResource(R.drawable.bg_calendar_sunday)
                Calendar.SATURDAY -> textView1
                    .setBackgroundResource(R.drawable.bg_calendar_saturday)
                else -> textView1.setBackgroundResource(R.drawable.bg_calendar_weekday)
            }
            val params = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
            )
            row.addView(child, params)
        }
        binding.calendarGrid.addView(row, rowParams)
    }

    private val mutex = Mutex()

    private fun buildCalendar() = lifecycleScope.launch {
        // Mutex lock to avoid concurrent view update because livedata notification sometimes
        // comes twice.
        mutex.withLock {
            binding.calendarGrid.removeAllViews()
            val current = DateChangeTimeUtil.dateTimeCalendar
            current.add(Calendar.MONTH, mMonthOffset)
            current[Calendar.DAY_OF_MONTH] = 1
            addDayOfWeek()
            addDayOfMonth(current)
        }
    }

    private fun showDoneIcon(view: ImageView) {
        view.setImageResource(Settings.doneIconId)
        view.visibility = View.VISIBLE
    }

    private fun hideDoneIcon(view: ImageView) {
        view.visibility = View.INVISIBLE
    }

    private suspend fun addDayOfMonth(calendar: Calendar) {
        val maxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var week = calendar[Calendar.DAY_OF_WEEK]
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val today = DateChangeTimeUtil.dateTimeCalendar[Calendar.DAY_OF_MONTH]
        val doneDateList = taskViewModel.getHistoryInMonth(
            mTask._id!!, year, month
        )
        val sdf = SimpleDateFormat("dd", Locale.JAPAN)

        // Fill the days of previous month in the first week with blank
        // rectangle
        var row = LinearLayout(requireContext())
        row.orientation = LinearLayout.HORIZONTAL
        val rowParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f
        )
        val childParams = LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f
        )
        val blankDaysInFirstWeek = ((week - startDayOfWeek + NUM_OF_DAYS_IN_WEEK)
                % NUM_OF_DAYS_IN_WEEK)
        for (i in 0 until blankDaysInFirstWeek) {
            val child = View.inflate(
                requireContext(),
                R.layout.calendar_date, null
            )
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank)
            row.addView(child, childParams)
        }
        val date = Calendar.getInstance()
        date[Calendar.HOUR_OF_DAY] = 0
        date[Calendar.MINUTE] = 0
        date[Calendar.SECOND] = 0
        date[Calendar.MILLISECOND] = 0
        var weekIndex = blankDaysInFirstWeek
        for (day in 1..maxDate) {
            val bindingDate = CalendarDateBinding.inflate(layoutInflater)
            val child = bindingDate.root
            val textView1 = bindingDate.textView1
            val imageView1 = bindingDate.imageViewDone
            var enableContextMenu = false
            if (Settings.enableFutureDate) {
                enableContextMenu = true
            } else if (mMonthOffset < 0 || mMonthOffset == 0 && day <= today) {
                // Enable context menu to change done status of past days.
                enableContextMenu = true
            }
            if (enableContextMenu) {
                date[year, month] = day
                child.tag = date.time
                registerForContextMenu(child)
            }
            week = calendar[Calendar.DAY_OF_WEEK]
            val isValidDay = Recurrence.getFromTask(mTask).isValidDay(week)
            if (isValidDay) {
                if (mMonthOffset == 0 && day == today) {
                    child.setBackgroundResource(R.drawable.bg_calendar_day_today)
                }
            } else {
                if (mMonthOffset == 0 && day == today) {
                    child.setBackgroundResource(R.drawable.bg_calendar_day_today_off)
                } else {
                    child.setBackgroundResource(R.drawable.bg_calendar_day_off)
                }
            }
            val fontColorOfWeek = getFontColorOfWeek(week)
            textView1.text = day.toString()
            textView1.setTextColor(fontColorOfWeek)

            // Put done mark
            for (doneDate in doneDateList) {
                if (day == sdf.format(doneDate).toInt()) {
                    showDoneIcon(imageView1)
                    break
                }
            }
            row.addView(child, childParams)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            weekIndex = (weekIndex + 1) % NUM_OF_DAYS_IN_WEEK
            if (weekIndex == 0) {
                // Go to next week
                binding.calendarGrid.addView(row, rowParams)
                row = LinearLayout(context)
            }
        }

        // Fill the days of next month in the last week with blank rectangle
        val blankDaysInLastWeek = ((NUM_OF_DAYS_IN_WEEK - week + (startDayOfWeek - 1))
                % NUM_OF_DAYS_IN_WEEK)
        if (blankDaysInLastWeek > 0) {
            for (i in 0 until blankDaysInLastWeek) {
                val bindingDate = CalendarDateBinding.inflate(layoutInflater)
                val child = bindingDate.root
                child.setBackgroundResource(R.drawable.bg_calendar_day_blank)
                row.addView(child, childParams)
            }
            binding.calendarGrid.addView(row, rowParams)
        }
    }

    private fun getFontColorOfWeek(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> Color.RED
            Calendar.SATURDAY -> Color.BLUE
            else -> Color.BLACK
        }
    }

    /**
     * Get dayOfWeek value from index of week.
     *
     * @param indexOfWeek The index of week.
     * @return dayOfWeek Value defined in Calendar class.
     */
    private fun getDayOfWeek(indexOfWeek: Int): Int {
        return (indexOfWeek + (startDayOfWeek - 1)) % NUM_OF_DAYS_IN_WEEK + 1
    }

    private val startDayOfWeek: Int
        get() = Settings.weekStartDay!!

    companion object {
        const val POSITION_KEY = "com.hkb48.keepdo.calendargrid.POSITION"
        private const val CONTEXT_MENU_CHECK_DONE = 0
        private const val CONTEXT_MENU_UNCHECK_DONE = 1
        private const val NUM_OF_DAYS_IN_WEEK = 7
        fun newInstance(position: Int): CalendarGrid {
            val fragment = CalendarGrid()
            val args = Bundle()
            args.putInt(POSITION_KEY, position)
            fragment.arguments = args
            return fragment
        }
    }
}