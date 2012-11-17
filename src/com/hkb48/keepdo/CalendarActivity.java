package com.hkb48.keepdo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CalendarActivity extends MainActivity {
	private GridLayout mGridLayout;
    private int mPosition = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.calendar_activity);

        mGridLayout = (GridLayout) findViewById(R.id.gridLayout);

        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPosition--;
                buildCalendar();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPosition++;
                buildCalendar();
            }
        });

        buildCalendar();
    }

    /***
     * Set calendar tile with YYYY/MM format
     * 
     * @param calendar
     */
    private void setTitle(Calendar calendar) {
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

        setTitle(current);

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
            int fontColorOfWeek = getFontColorOfWeek(dayOfWeek);

            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            textView1.setText(weeks[i]);
            textView1.setTextColor(fontColorOfWeek);

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

        for (int i = 0; i < maxdate; i++) {
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            ImageView imageView1 = (ImageView) child.findViewById(R.id.imageView1);
            int week = calendar.get(Calendar.DAY_OF_WEEK);
            int fontColorOfWeek = getFontColorOfWeek(week);

            textView1.setText(Integer.toString(i + 1));
            textView1.setTextColor(fontColorOfWeek);

            // Tentative
            if (i%10 != 1) {
                imageView1.setVisibility(View.GONE);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            if (i == 0) {
                params.rowSpec = GridLayout.spec(1);
                params.columnSpec = GridLayout.spec(week - Calendar.SUNDAY);
            }
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child, params);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
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
