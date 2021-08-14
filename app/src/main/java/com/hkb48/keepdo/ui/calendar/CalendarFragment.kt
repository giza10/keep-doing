package com.hkb48.keepdo.ui.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hkb48.keepdo.R
import com.hkb48.keepdo.databinding.FragmentCalendarBinding
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import com.hkb48.keepdo.ui.settings.Settings
import com.hkb48.keepdo.util.CompatUtil
import com.hkb48.keepdo.util.DateChangeTimeUtil
import com.hkb48.keepdo.util.DoneHistoryUtil
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CalendarFragment : Fragment() {
    private val viewModel: CalendarViewModel by viewModels()
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val args: CalendarFragmentArgs by navArgs()

    private lateinit var viewPager: ViewPager2
    private lateinit var taskWithDoneHistory: TaskWithDoneHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        subscribeToModel()
        viewPager = binding.viewPager
        viewPager.adapter = CalendarPageAdapter(this, args.taskId)
        viewPager.setCurrentItem(INDEX_OF_THIS_MONTH, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding.tabLayout
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = getPageTitle(position)
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_calendar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val taskId = args.taskId
        return when (item.itemId) {
            R.id.menu_info -> {
                val action =
                    CalendarFragmentDirections.actionCalendarFragmentToTaskDetailFragment(
                        taskId,
                        args.title
                    )
                findNavController().navigate(action)
                true
            }
            R.id.menu_share -> {
                shareDisplayedCalendarView()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun subscribeToModel() {
        viewModel.getTaskWithDoneHistory(args.taskId)
            .observe(viewLifecycleOwner, { taskWithDoneHistory ->
                this.taskWithDoneHistory = taskWithDoneHistory
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    taskWithDoneHistory.task.name
            })
    }

    private fun getPageTitle(position: Int): CharSequence {
        val pageNumber = position - INDEX_OF_THIS_MONTH
        val current = DateChangeTimeUtil.dateTimeCalendar
        current.add(Calendar.MONTH, pageNumber)
        current[Calendar.DAY_OF_MONTH] = 1
        val sdf = SimpleDateFormat("yyyy/MM", Locale.JAPAN)
        return sdf.format(current.time)
    }

    private fun shareDisplayedCalendarView() {
        val calendarRoot = binding.calendarRoot
        getBitmapFromView(calendarRoot, requireActivity(), callback = {
            var contentUri: Uri? = null
            try {
                val imageDir = File(requireContext().cacheDir, "images").apply {
                    if (exists().not()) {
                        mkdir()
                    }
                }
                val file = File(imageDir, "shared_image.png")
                val fos = FileOutputStream(file)
                it.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
                contentUri = FileProvider.getUriForFile(requireContext(), FILE_PROVIDER, file)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            val util = DoneHistoryUtil(taskWithDoneHistory)
            val comboCount = util.getComboCount()
            val taskName = taskWithDoneHistory.task.name
            var extraText = ""
            val monthOffset = viewPager.currentItem - INDEX_OF_THIS_MONTH
            if (monthOffset == 0 && comboCount > 1) {
                extraText += requireContext().getString(
                    R.string.share_combo,
                    taskName,
                    comboCount
                )
            } else {
                val current = DateChangeTimeUtil.dateTimeCalendar
                current.add(Calendar.MONTH, monthOffset)
                current[Calendar.DAY_OF_MONTH] = 1
                val doneDateList = util.getHistoryInMonth(
                    current[Calendar.YEAR], current[Calendar.MONTH]
                )
                extraText += requireContext().getString(
                    R.string.share_non_combo,
                    taskName,
                    doneDateList.size
                )
            }
            extraText += " " + requireContext().getString(R.string.share_app_url)

            startActivity(
                Intent(Intent.ACTION_SEND).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, extraText)
                }
            )
        })
    }

    private fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.window?.let { window ->
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val locationOfViewInWindow = IntArray(2)
                view.getLocationInWindow(locationOfViewInWindow)
                try {
                    PixelCopy.request(
                        window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ),
                        bitmap,
                        { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            }
                            // possible to handle other result codes ...
                        },
                        Handler(Looper.getMainLooper())
                    )
                } catch (e: IllegalArgumentException) {
                    // PixelCopy may throw IllegalArgumentException, make sure to handle it
                    e.printStackTrace()
                }
            }
        } else {
            @Suppress("DEPRECATION")
            view.isDrawingCacheEnabled = true

            @Suppress("DEPRECATION")
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            val baseBitmap = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
            Canvas(baseBitmap).apply {
                drawColor(
                    CompatUtil.getColor(
                        activity,
                        R.color.calendar_bg_fargment
                    )
                )
                drawBitmap(bitmap, 0f, 0f, null)
            }
            callback(baseBitmap)
            bitmap.recycle()
            baseBitmap.recycle()
            @Suppress("DEPRECATION")
            view.isDrawingCacheEnabled = false
        }
    }

    class CalendarPageAdapter internal constructor(
        fragment: Fragment?,
        private val taskId: Int
    ) : FragmentStateAdapter(fragment!!) {
        override fun getItemCount(): Int {
            return if (Settings.enableFutureDate) NUM_MAXIMUM_MONTHS else NUM_MAXIMUM_MONTHS_PAST
        }

        override fun createFragment(position: Int): Fragment {
            return CalendarGridFragment.newInstance(taskId, position)
        }
    }

    companion object {
        /**
         * The maximum number of months to be paging up (Past: 10 years, Future: 1 year)
         */
        private const val NUM_MAXIMUM_MONTHS_PAST = 10 * 12 // 10 years
        const val INDEX_OF_THIS_MONTH = NUM_MAXIMUM_MONTHS_PAST - 1
        private const val NUM_MAXIMUM_MONTHS_FUTURE = 12 // 12 months
        private const val NUM_MAXIMUM_MONTHS = NUM_MAXIMUM_MONTHS_PAST + NUM_MAXIMUM_MONTHS_FUTURE
        private const val FILE_PROVIDER = "com.hkb48.keepdo.fileprovider"
    }
}