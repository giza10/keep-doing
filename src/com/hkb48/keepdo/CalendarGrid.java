package com.hkb48.keepdo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarGrid extends Fragment {
    private static final String TAG = "#KEEPDO_CALENDARGRID: ";

    public static final String POSITION_KEY = "com.hkb48.keepdo.calendargrid.POSITION";

    private static final int CONTEXT_MENU_CHECK_DONE = 0;
    private static final int CONTEXT_MENU_UNCHECK_DONE = 1;
    private static final int NUM_OF_DAYS_IN_WEEK = 7;

    private static DatabaseAdapter mDatabaseAdapter;
    private static Task mTask;
    private static LinearLayout mCalendarGrid;
    private static volatile View mPressedView;

    private CheckSoundPlayer mCheckSound;
    private int mMonthOffset;
    private int mDoneIconId;
    private boolean mIsShareOnTop;

    private CalendarGrid() {
    }

    public static CalendarGrid newInstance(Bundle args) {
        CalendarGrid fragment = new CalendarGrid();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_sub_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getContext().getIntent();
        long taskId = intent.getLongExtra("TASK-ID", -1);
        mDatabaseAdapter = DatabaseAdapter.getInstance(getContext());
        mTask = mDatabaseAdapter.getTask(taskId);

        mDoneIconId = Settings.getDoneIconId();
        mCheckSound = new CheckSoundPlayer(getContext());
        mMonthOffset = (getArguments() != null) ? (getArguments().getInt(POSITION_KEY) - CalendarFragment.NUM_MAXIMUM_MOUNTHS  + 1) : (0);

        mCalendarGrid = (LinearLayout) view.findViewById(R.id.calendar_grid);

        buildCalendar();

        Intent returnIntent = new Intent();
        getContext().setResult(TaskActivity.RESULT_CANCELED, returnIntent);
    }

    public void onResume() {
        mCheckSound.load();
        mIsShareOnTop = false;
        setHasOptionsMenu(true);

        super.onResume();
    }

    @Override
    public void onPause() {
        mCheckSound.unload();
        super.onPause();
    }

    @Override
    public void onStop() {
        setHasOptionsMenu(false);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_task, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                if (!mIsShareOnTop) {
                    shareDisplayedCalendarView();
                    mIsShareOnTop = true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        if (view == null) {
            Log.e(TAG, "view in onCreateContextMenu() was null!!");
            return;
        }

        mPressedView = view;
        Date date = (Date) mPressedView.getTag();
        SimpleDateFormat sdf = new SimpleDateFormat(
                getString(R.string.date_format), Locale.getDefault());
        menu.setHeaderTitle(sdf.format(date));

        ImageView imageView = (ImageView) mPressedView
                .findViewById(R.id.imageViewDone);
        int visibility = imageView.getVisibility();
        if (visibility == View.VISIBLE) {
            menu.add(0, CONTEXT_MENU_UNCHECK_DONE, 0, R.string.uncheck_done);
        } else {
            menu.add(0, CONTEXT_MENU_CHECK_DONE, 0, R.string.check_done);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean consumed = false;
        if (mPressedView == null) {
            Log.e(TAG, "mPressedView is null!");
            return false;
        }

        ImageView imageView = (ImageView) mPressedView
                .findViewById(R.id.imageViewDone);
        Date selectedDate = (Date) mPressedView.getTag();

        switch (item.getItemId()) {
        case CONTEXT_MENU_CHECK_DONE:
            showDoneIcon(imageView);
            mDatabaseAdapter.setDoneStatus(mTask.getTaskID(), selectedDate,
                    true);
            mCheckSound.play();
            consumed = true;
            break;
        case CONTEXT_MENU_UNCHECK_DONE:
            hideDoneIcon(imageView);
            mDatabaseAdapter.setDoneStatus(mTask.getTaskID(), selectedDate,
                    false);
            consumed = true;
            break;
        default:
            break;
        }

        Date today = DateChangeTimeUtil.getDate();
        if (selectedDate.compareTo(today) == 0) {
            ReminderManager.getInstance().setNextAlert(getContext());
        }

        // Set result of this activity as OK to inform that the done status is
        // updated
        Intent returnIntent = new Intent();
        getContext().setResult(TaskActivity.RESULT_OK, returnIntent);

        if (consumed) {
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    final FragmentActivity getContext() {
        return this.getActivity();
    }

    private void addDayOfWeek() {
        String[] weeks = getResources().getStringArray(R.array.week_names);
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < NUM_OF_DAYS_IN_WEEK; i++) {
            int dayOfWeek = getDayOfWeek(i);
            View child = getContext().getLayoutInflater().inflate(R.layout.calendar_week, null);

            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            textView1.setText(weeks[dayOfWeek - 1]);
            switch (dayOfWeek) {
            case Calendar.SUNDAY:
                textView1.setBackgroundResource(R.drawable.bg_calendar_sunday);
                break;
            case Calendar.SATURDAY:
                textView1
                        .setBackgroundResource(R.drawable.bg_calendar_saturday);
                break;
            default:
                textView1.setBackgroundResource(R.drawable.bg_calendar_weekday);
                break;
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            row.addView(child, params);
        }
        mCalendarGrid.addView(row, rowParams);
    }

    private void buildCalendar() {
        Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
        current.add(Calendar.MONTH, mMonthOffset);
        current.set(Calendar.DAY_OF_MONTH, 1);

        mCalendarGrid.removeAllViews();

        addDayOfWeek();

        addDayOfMonth(current);
    }

    private void showDoneIcon(ImageView view) {
        view.setImageResource(mDoneIconId);
        view.setVisibility(View.VISIBLE);
    }

    private void hideDoneIcon(ImageView view) {
        view.setVisibility(View.INVISIBLE);
    }

    private void addDayOfMonth(Calendar calendar) {
        final int maxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int startDayOfWeek = getStartDayOfWeek();
        final int today = DateChangeTimeUtil.getDateTimeCalendar().get(
                Calendar.DAY_OF_MONTH);

        ArrayList<Date> doneDateList = mDatabaseAdapter.getHistory(
                mTask.getTaskID(), calendar.getTime());
        SimpleDateFormat sdf_d = new SimpleDateFormat("dd", Locale.JAPAN);

        // Fill the days of previous month in the first week with blank
        // rectangle
        LinearLayout row = new LinearLayout(this.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        final LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        final LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

        final int blankDaysInFirstWeek = (week - startDayOfWeek + NUM_OF_DAYS_IN_WEEK)
                % NUM_OF_DAYS_IN_WEEK;
        for (int i = 0; i < blankDaysInFirstWeek; i++) {
            View child = getContext().getLayoutInflater().inflate(
                    R.layout.calendar_date, null);
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
            row.addView(child, childParams);
        }

        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        int weekIndex = blankDaysInFirstWeek;
        for (int day = 1; day <= maxDate; day++) {
            View child = getContext().getLayoutInflater().inflate(
                    R.layout.calendar_date, null);
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            ImageView imageView1 = (ImageView) child
                    .findViewById(R.id.imageViewDone);

            // Register context menu to change done status of past days.
            if ((mMonthOffset < 0) || ((mMonthOffset == 0) && (day <= today))) {
                date.set(year, month, day);
                child.setTag(date.getTime());
                registerForContextMenu(child);
            }

            week = calendar.get(Calendar.DAY_OF_WEEK);
            boolean isValidDay = mTask.getRecurrence().isValidDay(week);
            if (isValidDay) {
                if ((mMonthOffset == 0) && (day == today)) {
                    child.setBackgroundResource(R.drawable.bg_calendar_day_today);
                }
            } else {
                if ((mMonthOffset == 0) && (day == today)) {
                    child.setBackgroundResource(R.drawable.bg_calendar_day_today_off);
                } else {
                    child.setBackgroundResource(R.drawable.bg_calendar_day_off);
                }
            }

            int fontColorOfWeek = getFontColorOfWeek(week);
            textView1.setText(Integer.toString(day));
            textView1.setTextColor(fontColorOfWeek);

            // Put done mark
            for (Date doneDate : doneDateList) {
                if (day == Integer.parseInt(sdf_d.format(doneDate))) {
                    showDoneIcon(imageView1);
                    break;
                }
            }

            row.addView(child, childParams);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            weekIndex = (weekIndex + 1) % NUM_OF_DAYS_IN_WEEK;

            if (weekIndex == 0) {
                // Go to next week
                mCalendarGrid.addView(row, rowParams);
                row = new LinearLayout(getContext());
            }
        }

        // Fill the days of next month in the last week with blank rectangle
        final int blankDaysInLastWeek = (NUM_OF_DAYS_IN_WEEK - week + (startDayOfWeek - 1))
                % NUM_OF_DAYS_IN_WEEK;
        if (blankDaysInLastWeek > 0) {
            for (int i = 0; i < blankDaysInLastWeek; i++) {
                View child = getContext().getLayoutInflater().inflate(
                        R.layout.calendar_date, null);
                child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
                row.addView(child, childParams);
            }
            mCalendarGrid.addView(row, rowParams);
        }
    }

    private int getFontColorOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
        case Calendar.SUNDAY:
            return Color.RED;
        case Calendar.SATURDAY:
            return Color.BLUE;
        default:
            return Color.BLACK;
        }
    }

    /**
     * Get dayOfWeek value from index of week.
     *
     * @param indexOfWeek The index of week.
     * @return dayOfWeek Value defined in Calendar class.
     */
    private int getDayOfWeek(int indexOfWeek) {
        int startDayOfWeek = getStartDayOfWeek();
        return (indexOfWeek + (startDayOfWeek - 1)) % NUM_OF_DAYS_IN_WEEK + 1;
    }

    private int getStartDayOfWeek() {
        return Settings.getWeekStartDay();
    }

    private void shareDisplayedCalendarView() {
        final String BITMAP_PATH = mDatabaseAdapter.backupDirPath()
                + "/temp_share_image.png";

        View calendarRoot = getContext().findViewById(R.id.calendar_root);
        calendarRoot.setDrawingCacheEnabled(true);

        File bitmapFile = new File(BITMAP_PATH);
        bitmapFile.getParentFile().mkdir();
        Bitmap bitmap = Bitmap.createBitmap(calendarRoot.getDrawingCache());

        Bitmap baseBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas bmpCanvas = new Canvas(baseBitmap);
        bmpCanvas.drawColor(getResources().getColor(
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
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(bitmapFile));
        ComboCount comboCount = mDatabaseAdapter.getComboCount(mTask
                .getTaskID());
        String extraText = "";
        if (mMonthOffset == 0 && comboCount.currentCount > 1) {
            extraText += getContext().getString(R.string.share_combo, mTask.getName(), comboCount.currentCount);
        } else {
            Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
            current.add(Calendar.MONTH, mMonthOffset);
            current.set(Calendar.DAY_OF_MONTH, 1);
            ArrayList<Date> doneDateList = mDatabaseAdapter.getHistory(mTask.getTaskID(), current.getTime());
            extraText += getContext().getString(R.string.share_non_combo, mTask.getName(), doneDateList.size());
        }
        extraText += " " + getContext().getString(R.string.share_app_url);
        intent.putExtra(Intent.EXTRA_TEXT, extraText);
        startActivity(intent);
    }

    public static CharSequence getPageTitle(int position) {
        final int pageNumber = position - CalendarFragment.NUM_MAXIMUM_MOUNTHS
                + 1;

        Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
        current.add(Calendar.MONTH, pageNumber);
        current.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM", Locale.JAPAN);

        return sdf.format(current.getTime());
    }
}