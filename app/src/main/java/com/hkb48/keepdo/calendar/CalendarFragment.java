package com.hkb48.keepdo.calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hkb48.keepdo.DatabaseAdapter;
import com.hkb48.keepdo.DateChangeTimeUtil;
import com.hkb48.keepdo.R;
import com.hkb48.keepdo.TaskDetailActivity;
import com.hkb48.keepdo.settings.Settings;
import com.hkb48.keepdo.util.CompatUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    /**
     * The maximum number of months to be paging up (Past: 10 years, Future: 1 year)
     */
    private static final int NUM_MAXIMUM_MONTHS_PAST = 10 * 12;  // 10 years
    static final int INDEX_OF_THIS_MONTH = NUM_MAXIMUM_MONTHS_PAST - 1;
    private static final int NUM_MAXIMUM_MONTHS_FUTURE = 12;  // 12 months
    private static final int NUM_MAXIMUM_MONTHS = NUM_MAXIMUM_MONTHS_PAST + NUM_MAXIMUM_MONTHS_FUTURE;
    private static final String FILE_PROVIDER = "com.hkb48.keepdo.fileprovider";

    private ViewPager2 mViewPager;

    public CalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new CalendarPageAdapter(this));
        mViewPager.setCurrentItem(CalendarFragment.INDEX_OF_THIS_MONTH, false);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> tab.setText(getPageTitle(position))
        ).attach();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_task, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final long taskId = requireActivity().getIntent().getLongExtra("TASK-ID", -1);
        if (item.getItemId() == R.id.menu_info) {
            Intent intent = new Intent(requireActivity(), TaskDetailActivity.class);
            intent.putExtra("TASK-ID", taskId);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_share) {
            shareDisplayedCalendarView(taskId);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private CharSequence getPageTitle(int position) {
        final int pageNumber = position - INDEX_OF_THIS_MONTH;

        Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
        current.add(Calendar.MONTH, pageNumber);
        current.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM", Locale.JAPAN);
        return sdf.format(current.getTime());
    }

    private void shareDisplayedCalendarView(final long taskId) {
        final String BITMAP_DIR_PATH = DatabaseAdapter.backupDirPath();
        final String BITMAP_FILE_PATH = BITMAP_DIR_PATH + "/temp_share_image.png";

        Context context = requireContext();
        View calendarRoot = requireActivity().findViewById(R.id.calendar_root);
        calendarRoot.setDrawingCacheEnabled(true);

        File bitmapFile = new File(BITMAP_FILE_PATH);
        if (!new File(BITMAP_DIR_PATH).mkdir()) {
            Bitmap bitmap = Bitmap.createBitmap(calendarRoot.getDrawingCache());

            Bitmap baseBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_4444);
            Canvas bmpCanvas = new Canvas(baseBitmap);
            bmpCanvas.drawColor(CompatUtil.getColor(context,
                    R.color.calendar_bg_fargment));
            bmpCanvas.drawBitmap(bitmap, 0, 0, null);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(bitmapFile, false);
                baseBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                calendarRoot.setDrawingCacheEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bitmap.recycle();
            baseBitmap.recycle();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/png");
            Uri contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER, bitmapFile);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(context);
            int comboCount = dbAdapter.getComboCount(taskId);
            String taskName = dbAdapter.getTask(taskId).getName();
            String extraText = "";
            int monthOffset = mViewPager.getCurrentItem() - CalendarFragment.INDEX_OF_THIS_MONTH;
            if (monthOffset == 0 && comboCount > 1) {
                extraText += context.getString(R.string.share_combo, taskName, comboCount);
            } else {
                Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
                current.add(Calendar.MONTH, monthOffset);
                current.set(Calendar.DAY_OF_MONTH, 1);
                ArrayList<Date> doneDateList = dbAdapter.getHistoryInMonth(taskId, current.getTime());
                extraText += context.getString(R.string.share_non_combo, taskName, doneDateList.size());
            }
            extraText += " " + context.getString(R.string.share_app_url);
            intent.putExtra(Intent.EXTRA_TEXT, extraText);
            startActivity(intent);
        }
    }

    public static class CalendarPageAdapter extends FragmentStateAdapter {
        CalendarPageAdapter(Fragment fragment) {
            super(fragment);
        }

        @Override
        public int getItemCount() {
            return Settings.getEnableFutureDate() ? CalendarFragment.NUM_MAXIMUM_MONTHS : CalendarFragment.NUM_MAXIMUM_MONTHS_PAST;
        }

        @Override
        @NonNull
        public Fragment createFragment(int position) {
            return CalendarGrid.newInstance(position);
        }
    }
}