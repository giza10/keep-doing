package com.hkb48.keepdo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class CalendarActivity extends Activity {
    // ID of context menu items
    private final static int CONTEXT_MENU_CHECK_DONE = 0;
    private final static int CONTEXT_MENU_UNCHECK_DONE = 1;

    private ViewFlipper mFlipper;
    private int mNextPageIndex = 0;
    private GridLayout mGridLayout;
    private int mPosition = 0;
    private Task mTask;
    private View mPressedView;
    private CheckSoundPlayer mCheckSound = new CheckSoundPlayer(this);
    private DatabaseAdapter mDBAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);

        Intent intent = getIntent();
        long taskId = intent.getLongExtra("TASK-ID", -1);
        mDBAdapter = DatabaseAdapter.getInstance(this);
        mTask = mDBAdapter.getTask(taskId);

        setActionBar();

        mFlipper = (ViewFlipper) findViewById(R.id.flipper);

        findViewById(R.id.button_prev).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Go to previous month
                mPosition--;
                buildCalendar();
                mFlipper.showNext();
            }
        });

        findViewById(R.id.button_next).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Go to next month
                mPosition++;
                buildCalendar();
                mFlipper.showNext();
            }
        });

        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);

        buildCalendar();
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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

    public boolean onContextItemSelected(MenuItem item) {
        ImageView imageView = (ImageView) mPressedView.findViewById(R.id.imageView1);
        Date selectedDate = (Date) mPressedView.getTag();

        switch (item.getItemId()) {
        case CONTEXT_MENU_CHECK_DONE:
            imageView.setVisibility(View.VISIBLE);
            mDBAdapter.setDoneStatus(mTask.getTaskID(), selectedDate, true);
            mCheckSound.play();
            break;
        case CONTEXT_MENU_UNCHECK_DONE:
            imageView.setVisibility(View.INVISIBLE);
            mDBAdapter.setDoneStatus(mTask.getTaskID(), selectedDate, false);
            break;
        default:
            break;
        }

        SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy/MM/dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Date today = null;
        try {
            today = sdf_ymd.parse(year + "/" + month + "/" + day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (selectedDate.compareTo(today) == 0) {
            // Set result of this activity as OK to inform that the today's done status is updated
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);

            ReminderManager.getInstance().setNextAlert(this);
        }

        return super.onContextItemSelected(item);
    }

    /***
     * Set action bar
     */
    private void setActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (mTask != null) {
            setTitle(mTask.getName());
        }
    }

    /***
     * Set calendar tile with YYYY/MM format
     * 
     * @param calendar
     */
    private void setCalendarTitle(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setText(sdf.format(calendar.getTime()));
    }

    /***
     * Build calendar view
     */
    private void buildCalendar() {
        Calendar current = Calendar.getInstance();
        current.add(Calendar.MONTH, mPosition);
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
        for (int i = 0; i < weeks.length; i++) {
            View child = getLayoutInflater().inflate(R.layout.calendar_week, null);
            int dayOfWeek = getDayOfWeek(i);

            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            textView1.setText(weeks[i]);
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
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child);
        }
    }

    /***
     * Add days to calendar view
     * 
     * @param calendar
     */
    private void addDayOfMonth(Calendar calendar) {
        int maxDate = calendar.getMaximum(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        ArrayList<Date> doneDateList = mDBAdapter.getHistory(mTask.getTaskID(), calendar.getTime());
        SimpleDateFormat sdf_d = new SimpleDateFormat("dd");
        SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy/MM/dd");

        // Fill the days of previous month in the first week with blank rectangle
        for (int i = 0; i < (week - Calendar.SUNDAY); i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(1);
            params.columnSpec = GridLayout.spec(i);
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
            mGridLayout.addView(child, params);
        }

        for (int day = 1; day <= maxDate; day++) {
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            ImageView imageView1 = (ImageView) child.findViewById(R.id.imageView1);

            // Register context menu to change done status of past days.
            if ((mPosition < 0) ||
                ((mPosition == 0) && (day <= today))) {
                Date date = null;
                try {
                    date = sdf_ymd.parse(year + "/" + month + "/" + day);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                child.setTag(date);
                registerForContextMenu(child);
            }

            week = calendar.get(Calendar.DAY_OF_WEEK);
            boolean isValidDay = mTask.getRecurrence().isValidDay(week);
            if (isValidDay) {
                if ((mPosition == 0) && (day == today)) {
                    child.setBackgroundResource(R.drawable.bg_calendar_day_today);
                }
            } else {
                if ((mPosition == 0) && (day == today)) {
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
                    imageView1.setVisibility(View.VISIBLE);
                    break;
                }
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child, params);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Fill the days of next month in the last week with blank rectangle
        for (int i = 0; i < (7 - week); i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
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
        return Calendar.SUNDAY + indexOfWeek;
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
        View layout = findViewById(pages[mNextPageIndex]);
        mNextPageIndex = (mNextPageIndex + 1) % 2;
        return layout;
    }

    private void setVisibilityOfNextButton() {
        View button = findViewById(R.id.button_next);
        if (mPosition < 0) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.INVISIBLE);
        }
    }
}
