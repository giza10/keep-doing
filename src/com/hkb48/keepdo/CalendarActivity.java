package com.hkb48.keepdo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CalendarActivity extends MainActivity {
	private GridLayout mGridLayout;
    private int mPosition = 0;
    private Task task;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);

        Intent intent = getIntent();
        long taskId = intent.getLongExtra("TASK-ID", -1);
        task = getTask(taskId);

        setActionBar();

        mGridLayout = (GridLayout) findViewById(R.id.gridLayout);

        findViewById(R.id.button_prev).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Go to last month
                mPosition--;
                buildCalendar();
            }
        });

        findViewById(R.id.button_next).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Go to next month
                mPosition++;
                buildCalendar();
            }
        });

        buildCalendar();
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

    /***
     * Set action bar
     */
    private void setActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (task != null) {
            setTitle(task.getName());
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
        int maxdate = calendar.getMaximum(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);

        // Fill the days of previous month in the first week with blank rectangle
        for (int i = 0; i < (week - Calendar.SUNDAY); i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(1);
            params.columnSpec = GridLayout.spec(i);
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
            mGridLayout.addView(child, params);
        }

        for (int date = 1; date <= maxdate; date++) {
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            ImageView imageView1 = (ImageView) child.findViewById(R.id.imageView1);
            week = calendar.get(Calendar.DAY_OF_WEEK);
            int fontColorOfWeek = getFontColorOfWeek(week);

            textView1.setText(Integer.toString(date));
            textView1.setTextColor(fontColorOfWeek);

            // TODO
            if (date%10 == 1) {
                imageView1.setVisibility(View.VISIBLE);
            }
            if (! task.getRecurrence().isValidDay(week)) {
                child.setBackgroundResource(R.drawable.bg_calendar_day_off);
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
}
