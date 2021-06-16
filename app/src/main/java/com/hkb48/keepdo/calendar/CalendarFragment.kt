package com.hkb48.keepdo.calendar

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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hkb48.keepdo.DatabaseAdapter
import com.hkb48.keepdo.DateChangeTimeUtil
import com.hkb48.keepdo.R
import com.hkb48.keepdo.TaskDetailActivity
import com.hkb48.keepdo.settings.Settings
import com.hkb48.keepdo.util.CompatUtil
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class CalendarFragment : Fragment() {
    private lateinit var mViewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewPager = view.findViewById(R.id.viewPager)
        mViewPager.adapter = CalendarPageAdapter(this)
        mViewPager.setCurrentItem(INDEX_OF_THIS_MONTH, false)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        TabLayoutMediator(
            tabLayout, mViewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = getPageTitle(position)
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_task, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val taskId = requireActivity().intent.getLongExtra("TASK-ID", -1)
        return when (item.itemId) {
            R.id.menu_info -> {
                val intent = Intent(requireActivity(), TaskDetailActivity::class.java)
                intent.putExtra("TASK-ID", taskId)
                startActivity(intent)
                true
            }
            R.id.menu_share -> {
                shareDisplayedCalendarView(taskId)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun getPageTitle(position: Int): CharSequence {
        val pageNumber = position - INDEX_OF_THIS_MONTH
        val current = DateChangeTimeUtil.dateTimeCalendar
        current.add(Calendar.MONTH, pageNumber)
        current[Calendar.DAY_OF_MONTH] = 1
        val sdf = SimpleDateFormat("yyyy/MM", Locale.JAPAN)
        return sdf.format(current.time)
    }

    private fun shareDisplayedCalendarView(taskId: Long) {
        val calendarRoot = requireActivity().findViewById<View>(R.id.calendar_root)
        getBitmapFromView(calendarRoot, requireActivity(), callback = {
            val imageDir = File(requireContext().cacheDir, "images")
            var contentUri: Uri? = null
            try {
                if(!imageDir.exists()) {
                    imageDir.mkdir()
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

            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            val dbAdapter = DatabaseAdapter.getInstance(requireContext())
            val comboCount = dbAdapter.getComboCount(taskId)
            val taskName = dbAdapter.getTask(taskId)!!.name
            var extraText = ""
            val monthOffset = mViewPager.currentItem - INDEX_OF_THIS_MONTH
            if (monthOffset == 0 && comboCount > 1) {
                extraText += requireContext().getString(R.string.share_combo, taskName, comboCount)
            } else {
                val current = DateChangeTimeUtil.dateTimeCalendar
                current.add(Calendar.MONTH, monthOffset)
                current[Calendar.DAY_OF_MONTH] = 1
                val doneDateList = dbAdapter.getHistoryInMonth(taskId, current.time)
                extraText += requireContext().getString(
                    R.string.share_non_combo,
                    taskName,
                    doneDateList.size
                )
            }
            extraText += " " + requireContext().getString(R.string.share_app_url)
            intent.putExtra(Intent.EXTRA_TEXT, extraText)
            startActivity(intent)
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
            val bmpCanvas = Canvas(baseBitmap)
            bmpCanvas.drawColor(
                CompatUtil.getColor(
                    activity,
                    R.color.calendar_bg_fargment
                )
            )
            bmpCanvas.drawBitmap(bitmap, 0f, 0f, null)
            callback(baseBitmap)
            bitmap.recycle()
            baseBitmap.recycle()
            @Suppress("DEPRECATION")
            view.isDrawingCacheEnabled = false
        }
    }

    class CalendarPageAdapter internal constructor(fragment: Fragment?) : FragmentStateAdapter(
        fragment!!
    ) {
        override fun getItemCount(): Int {
            return if (Settings.getEnableFutureDate()) NUM_MAXIMUM_MONTHS else NUM_MAXIMUM_MONTHS_PAST
        }

        override fun createFragment(position: Int): Fragment {
            return CalendarGrid.newInstance(position)
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