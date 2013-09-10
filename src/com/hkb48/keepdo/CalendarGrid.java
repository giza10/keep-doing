package com.hkb48.keepdo;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.GridLayout;
import android.widget.ImageView;
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
    private static final int NUM_OF_MAX_WEEKS_IN_MONTH = 6;

    private static DatabaseAdapter mDatabaseAdapter;
    private static Task mTask;
    private static GridLayout mGridLayout;
    private static volatile View mPressedView;

    private CheckSoundPlayer mCheckSound;
    private int mMonthOffset;
    private int mCalendarCellWidth;
    private int mCalendarCellHeight;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        mGridLayout = (GridLayout)view.findViewById(R.id.gridLayout);

        buildCalendar(view);

        setOnGlobalLayoutListener(mGridLayout);

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
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        if (view == null) {
            Log.e(TAG, "view in onCreateContextMenu() was null!!");
            return;
        }

        mPressedView = view;
        Date date = (Date) mPressedView.getTag();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
        menu.setHeaderTitle(sdf.format(date));

        ImageView imageView = (ImageView) mPressedView.findViewById(R.id.imageViewDone);
        int visibility = imageView.getVisibility();
        if (visibility == View.VISIBLE) {
            menu.add(0, CONTEXT_MENU_UNCHECK_DONE, 0, R.string.uncheck_done);
        } else {
            menu.add(0, CONTEXT_MENU_CHECK_DONE, 0, R.string.check_done);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        if (mPressedView == null) {
            Log.e(TAG, "mPressedView is null!");
            return false;
        }

        ImageView imageView = (ImageView) mPressedView.findViewById(R.id.imageViewDone);
        Date selectedDate = (Date) mPressedView.getTag();

        switch (item.getItemId()) {
            case CONTEXT_MENU_CHECK_DONE:
                showDoneIcon(imageView);
                mDatabaseAdapter.setDoneStatus(mTask.getTaskID(), selectedDate, true);
                mCheckSound.play();
                break;
            case CONTEXT_MENU_UNCHECK_DONE:
                hideDoneIcon(imageView);
                mDatabaseAdapter.setDoneStatus(mTask.getTaskID(), selectedDate, false);
                break;
            default:
                break;
        }

        Date today = DateChangeTimeUtil.getDate();
        if (selectedDate.compareTo(today) == 0) {
            ReminderManager.getInstance().setNextAlert(getContext());
        }

        // Set result of this activity as OK to inform that the done status is updated
        Intent returnIntent = new Intent();
        getContext().setResult(TaskActivity.RESULT_OK, returnIntent);

        return super.onContextItemSelected(item);
    }

    final FragmentActivity getContext() {
        return this.getActivity();
    }

    private void addDayOfWeek() {
        String[] weeks = getResources().getStringArray(R.array.week_names);
        for (int i = 0; i < 7; i++) {
            int dayOfWeek = getDayOfWeek(i);
            View child = getContext().getLayoutInflater().inflate(R.layout.calendar_week, null);

            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            textView1.setText(weeks[dayOfWeek - 1]);
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    textView1.setBackgroundResource(R.drawable.bg_calendar_sunday);
                    break;
                case Calendar.SATURDAY:
                    textView1.setBackgroundResource(R.drawable.bg_calendar_saturday);
                    break;
                default:
                    textView1.setBackgroundResource(R.drawable.bg_calendar_weekday);
                    break;
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = mCalendarCellWidth;
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child, params);
        }
    }

    private void buildCalendar(final View view) {
        Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
        current.add(Calendar.MONTH, mMonthOffset);
        current.set(Calendar.DAY_OF_MONTH, 1);

        mGridLayout.removeAllViews();

        calculateCalendarViewSize(view);

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
        final int today = DateChangeTimeUtil.getDateTimeCalendar().get(Calendar.DAY_OF_MONTH);

        ArrayList<Date> doneDateList = mDatabaseAdapter.getHistory(mTask.getTaskID(), calendar.getTime());
        SimpleDateFormat sdf_d = new SimpleDateFormat("dd", Locale.JAPAN);

        // Fill the days of previous month in the first week with blank rectangle
        final int blankDaysInFirstWeek = (week - startDayOfWeek + 7) % 7;
        for (int i = 0; i < blankDaysInFirstWeek; i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(1);
            params.columnSpec = GridLayout.spec(i);
            params.width = mCalendarCellWidth;
            params.height = mCalendarCellHeight;

            View child = getContext().getLayoutInflater().inflate(R.layout.calendar_date, null);
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
            mGridLayout.addView(child, params);
        }

        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        for (int day = 1; day <= maxDate; day++) {
            View child = getContext().getLayoutInflater().inflate(R.layout.calendar_date, null);
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            ImageView imageView1 = (ImageView) child.findViewById(R.id.imageViewDone);

            // Register context menu to change done status of past days.
            if ((mMonthOffset < 0) ||
                    ((mMonthOffset == 0) && (day <= today))) {
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

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = mCalendarCellWidth;
            params.height = mCalendarCellHeight;
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child, params);

            calendar.add(Calendar.DAY_OF_MONTH, 1);

        }

        // Fill the days of next month in the last week with blank rectangle
        final int blankDaysInLastWeek = (7 - week + (startDayOfWeek - 1)) % 7;
        for (int i = 0; i < blankDaysInLastWeek; i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = mCalendarCellWidth;
            params.height = mCalendarCellHeight;
            View child = getContext().getLayoutInflater().inflate(R.layout.calendar_date, null);
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
            mGridLayout.addView(child, params);
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
        return (indexOfWeek + (startDayOfWeek - 1)) % 7 + 1;
    }

    private int getStartDayOfWeek() {
        return Settings.getWeekStartDay();
    }

    private void calculateCalendarViewSize(View view) {
        final Display display = getContext().getWindowManager().getDefaultDisplay();
        final Point displaySize = new Point();
        display.getSize(displaySize);
        final Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        int cellWidth;
        int cellHeight;
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            cellWidth = (displaySize.x - resources.getDimensionPixelSize(R.dimen.calendar_padding_width)) / NUM_OF_DAYS_IN_WEEK;
            cellHeight = cellWidth + resources.getDimensionPixelSize(R.dimen.calendar_date_margin_top);

        } else {
            Rect rect = new Rect();
            view.getWindowVisibleDisplayFrame(rect);
            final int visibleWindowTop = rect.top;
            final int calendarTop = view.getTop();
            final int calendarPaddingHeight = resources.getDimensionPixelSize(R.dimen.calendar_padding_height);
            final int calendarHeight = displaySize.y - calendarPaddingHeight - calendarTop - visibleWindowTop;// - buttonHeight;
            cellHeight = calendarHeight / NUM_OF_MAX_WEEKS_IN_MONTH;
            cellWidth = cellHeight + resources.getDimensionPixelSize(R.dimen.calendar_date_margin_left);
        }

        mCalendarCellWidth = cellWidth;
        mCalendarCellHeight = cellHeight;
    }

    private void setOnGlobalLayoutListener(final View view) {
        final OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                ViewTreeObserver observer = view.getViewTreeObserver();
                if ((observer != null) && (observer.isAlive())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        observer.removeOnGlobalLayoutListener(this);
                    } else {
                        observer.removeGlobalOnLayoutListener(this);
                    }
                }

                if (getContext() == null) {
                    Log.d(TAG, this.getClass().getName() + "setOnGlobalLayoutListener() : getContext() null.");
                    return;
                }

                calculateCalendarViewSize(view);
                buildCalendar(view);
            }
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    private void shareDisplayedCalendarView() {
        final String BITMAP_PATH = mDatabaseAdapter.backupDirPath() + "/temp_share_image.png";

        View calendarRoot = getContext().findViewById(R.id.calendar_root);
        calendarRoot.setDrawingCacheEnabled(true);

        File bitmapFile = new File(BITMAP_PATH);
        bitmapFile.getParentFile().mkdir();
        Bitmap bitmap = Bitmap.createBitmap(calendarRoot.getDrawingCache());

        Bitmap baseBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas bmpCanvas = new Canvas(baseBitmap);
        bmpCanvas.drawColor(getResources().getColor(R.color.calendar_bg_fargment));
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
        ComboCount comboCount = mDatabaseAdapter.getComboCount(mTask.getTaskID());
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
        final int pageNumber = position - CalendarFragment.NUM_MAXIMUM_MOUNTHS + 1;

        Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
        current.add(Calendar.MONTH, pageNumber);
        current.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM", Locale.JAPAN);

        return sdf.format(current.getTime());
    }
}