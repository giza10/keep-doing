package com.hkb48.keepdo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class CalendarFragment extends Fragment {
    // ID of context menu items
    private static final int CONTEXT_MENU_CHECK_DONE = 0;
    private static final int CONTEXT_MENU_UNCHECK_DONE = 1;

    private static final int NUM_OF_DAYS_IN_WEEK = 7;
    private static final int NUM_OF_MAX_WEEKS_IN_MONTH = 6;

    private ViewFlipper mFlipper;
    private int mNextPageIndex = 0;
    private GridLayout mGridLayout;
    private int mMonthOffset = 0;
    private Task mTask;
    private View mPressedView;
    private CheckSoundPlayer mCheckSound;
    private DatabaseAdapter mDBAdapter;
    private int mCalendarCellWidth;
    private int mCalendarCellHeight;
    private int mDoneIconId;

    public CalendarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCheckSound = new CheckSoundPlayer(this.getActivity());

        Intent intent = getActivity().getIntent();
        long taskId = intent.getLongExtra("TASK-ID", -1);
        mDBAdapter = DatabaseAdapter.getInstance(this.getActivity());
        mTask = mDBAdapter.getTask(taskId);

        mFlipper = (ViewFlipper) getActivity().findViewById(R.id.flipper);

        getActivity().findViewById(R.id.button_prev).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Go to previous month
                mMonthOffset--;
                buildCalendar();
                mFlipper.showNext();
            }
        });

        getActivity().findViewById(R.id.button_next).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Go to next month
                mMonthOffset++;
                buildCalendar();
                mFlipper.showNext();
            }
        });

        Intent returnIntent = new Intent();
        getActivity().setResult(TaskActivity.RESULT_CANCELED, returnIntent);

        mDoneIconId = Settings.getDoneIconId();

        setOnGlobalLayoutListener(mFlipper);
    }

    @Override
    public void onResume() {
        mCheckSound.load();
        super.onResume();
    }

    @Override
    public void onPause() {
        mCheckSound.unload();
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        mPressedView = view;
        Date date = (Date) mPressedView.getTag();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format));
        menu.setHeaderTitle(sdf.format(date));

        ImageView imageView = (ImageView) mPressedView.findViewById(R.id.imageView1);
        int visibility = imageView.getVisibility();
        if (visibility == View.VISIBLE) {
            menu.add(0, CONTEXT_MENU_UNCHECK_DONE, 0, R.string.uncheck_done);
        } else {
            menu.add(0, CONTEXT_MENU_CHECK_DONE, 0, R.string.check_done);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ImageView imageView = (ImageView) mPressedView.findViewById(R.id.imageView1);
        Date selectedDate = (Date) mPressedView.getTag();

        switch (item.getItemId()) {
        case CONTEXT_MENU_CHECK_DONE:
            showDoneIcon(imageView);
            mDBAdapter.setDoneStatus(mTask.getTaskID(), selectedDate, true);
            mCheckSound.play();
            break;
        case CONTEXT_MENU_UNCHECK_DONE:
            hideDoneIcon(imageView);
            mDBAdapter.setDoneStatus(mTask.getTaskID(), selectedDate, false);
            break;
        default:
            break;
        }

        Date today = DateChangeTimeUtil.getDate();
        if (selectedDate.compareTo(today) == 0) {
            ReminderManager.getInstance().setNextAlert(this.getActivity());
        }

        // Set result of this activity as OK to inform that the done status is updated
        Intent returnIntent = new Intent();
        getActivity().setResult(TaskActivity.RESULT_OK, returnIntent);

        return super.onContextItemSelected(item);
    }

    /***
     * Set calendar tile with YYYY/MM format
     *
     * @param calendar
     */
    private void setCalendarTitle(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM", Locale.JAPAN);
        TextView textView1 = (TextView) getActivity().findViewById(R.id.textView1);
        textView1.setText(sdf.format(calendar.getTime()));
    }

    /***
     * Build calendar view
     */
    private void buildCalendar() {
        Calendar current = DateChangeTimeUtil.getDateTimeCalendar();
        current.add(Calendar.MONTH, mMonthOffset);
        current.set(Calendar.DAY_OF_MONTH, 1);

        setCalendarTitle(current);

        setVisibilityOfNextButton();

        View layout = getNextPageLayout();
        mGridLayout = (GridLayout) layout.findViewById(R.id.gridLayout);
        mGridLayout.removeAllViews();

        addDayOfWeek();

        addDayOfMonth(current);
    }

    /***
     * Add day of the week to calendar view
     */
    private void addDayOfWeek() {
        String[] weeks = getResources().getStringArray(R.array.week_names);
        for (int i = 0; i < 7; i++) {
            int dayOfWeek = getDayOfWeek(i);
            View child = getActivity().getLayoutInflater().inflate(R.layout.calendar_week, null);

            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            textView1.setText(weeks[dayOfWeek - 1]);
            textView1.setTextColor(Color.WHITE);
            switch(dayOfWeek) {
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

    /***
     * Add days to calendar view
     *
     * @param calendar
     */
    private void addDayOfMonth(Calendar calendar) {
        final int maxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int startDayOfWeek = getStartDayOfWeek();
        final int today = DateChangeTimeUtil.getDateTimeCalendar().get(Calendar.DAY_OF_MONTH);

        ArrayList<Date> doneDateList = mDBAdapter.getHistory(mTask.getTaskID(), calendar.getTime());
        SimpleDateFormat sdf_d = new SimpleDateFormat("dd", Locale.JAPAN);

        // Fill the days of previous month in the first week with blank rectangle
        final int blankDaysInFirstWeek = (week - startDayOfWeek + 7) % 7;
        for (int i = 0; i < blankDaysInFirstWeek; i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(1);
            params.columnSpec = GridLayout.spec(i);
            params.width = mCalendarCellWidth;
            params.height = mCalendarCellHeight;
            View child = getActivity().getLayoutInflater().inflate(R.layout.calendar_date, null);
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
            mGridLayout.addView(child, params);
        }

        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        for (int day = 1; day <= maxDate; day++) {
            View child = getActivity().getLayoutInflater().inflate(R.layout.calendar_date, null);
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            ImageView imageView1 = (ImageView) child.findViewById(R.id.imageView1);

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
            View child = getActivity().getLayoutInflater().inflate(R.layout.calendar_date, null);
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
            mGridLayout.addView(child, params);
        }
    }

    /***
     * Get dayOfWeek value from index of week.
     *
     * @param indexOfWeek
     * @return dayOfWeek value defined in Calendar class
     */
    private int getDayOfWeek(int indexOfWeek) {
        int startDayOfWeek = getStartDayOfWeek();
        return (indexOfWeek + (startDayOfWeek - 1)) % 7 + 1;
    }

    /***
     * Get font color for day of the week
     *
     * @param color font color for day of the week
     */
    private int getFontColorOfWeek(int dayOfWeek) {
        switch(dayOfWeek) {
        case Calendar.SUNDAY:
            return Color.RED;
        case Calendar.SATURDAY:
            return Color.BLUE;
        default:
            return Color.BLACK;
        }
    }

    private View getNextPageLayout() {
        final int pages[] = {R.id.page1, R.id.page2};
        View layout = getActivity().findViewById(pages[mNextPageIndex]);
        mNextPageIndex = (mNextPageIndex + 1) % 2;
        return layout;
    }

    private void setVisibilityOfNextButton() {
        View button = getActivity().findViewById(R.id.button_next);
        if (mMonthOffset < 0) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.INVISIBLE);
        }
    }

    private void showDoneIcon(ImageView view) {
        view.setImageResource(mDoneIconId);
        view.setVisibility(View.VISIBLE);
    }

    private void hideDoneIcon(ImageView view) {
        view.setVisibility(View.INVISIBLE);
    }

    private int getStartDayOfWeek() {
        return Settings.getWeekStartDay();
    }

    private void setOnGlobalLayoutListener(final View view) {
        final OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
            @TargetApi(16)
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                ViewTreeObserver observer = view.getViewTreeObserver();
                if (observer.isAlive()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        observer.removeOnGlobalLayoutListener(this);
                    } else {
                        observer.removeGlobalOnLayoutListener(this);
                    }
                }

                calculateCalendarViewSize(view);
                buildCalendar();
            }
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    private void calculateCalendarViewSize(View view) {
        final Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point displaySize = new Point();
        display.getSize(displaySize);
        final Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        int cellWidth = 0;
        int cellHeight = 0;
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            cellWidth = (displaySize.x - resources.getDimensionPixelSize(R.dimen.calendar_padding_width)) / NUM_OF_DAYS_IN_WEEK;
            cellHeight = cellWidth + resources.getDimensionPixelSize(R.dimen.calendar_date_margin_top);

        } else {
            Rect rect = new Rect();
            view.getWindowVisibleDisplayFrame(rect);
            final int visibleWindowTop = rect.top;
            final int calendarTop = view.getTop();
            final int buttonHeight = getActivity().findViewById(R.id.button_next).getHeight();
            final int calendarPaddingHeight = resources.getDimensionPixelSize(R.dimen.calendar_padding_height);
            final int calendarHeight = displaySize.y - calendarPaddingHeight - calendarTop - visibleWindowTop - buttonHeight;
            cellHeight = calendarHeight / NUM_OF_MAX_WEEKS_IN_MONTH;
            cellWidth = cellHeight + resources.getDimensionPixelSize(R.dimen.calendar_date_margin_left);
        }

        mCalendarCellWidth = cellWidth;
        mCalendarCellHeight= cellHeight;
    }
}
