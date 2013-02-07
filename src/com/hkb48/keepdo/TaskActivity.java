package com.hkb48.keepdo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TaskActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
				.setTabListener(this));
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_task, menu);
		return true;
	}

	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
//		Fragment fragment = new DummySectionFragment();
//		Bundle args = new Bundle();
//		args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
//				tab.getPosition() + 1);
//		fragment.setArguments(args);
//		getSupportFragmentManager().beginTransaction()
//				.replace(R.id.container, fragment).commit();
		if (tab.getPosition() == 0) {
			Fragment fragment = new CalendarFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment).commit();
		} else {
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
					tab.getPosition() + 1);
			fragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment).commit();
		}
	}

	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
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

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Create a new TextView and set its text to the fragment's section
			// number argument value.
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return textView;
		}
	}

	public static class CalendarFragment extends Fragment {
	    // ID of context menu items
	    private final static int CONTEXT_MENU_CHECK_DONE = 0;
	    private final static int CONTEXT_MENU_UNCHECK_DONE = 1;

	    private ViewFlipper mFlipper;
	    private int mNextPageIndex = 0;
	    private GridLayout mGridLayout;
	    private int mPosition = 0;
	    private Task mTask;
	    private View mPressedView;
	    private CheckSoundPlayer mCheckSound;
	    private DatabaseAdapter mDBAdapter;

		public CalendarFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
//	        setContentView(R.layout.calendar_activity);
			return inflater.inflate(R.layout.calendar_activity, container, false);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			mCheckSound = new CheckSoundPlayer(this.getActivity());

			Intent intent = getActivity().getIntent();
	        long taskId = intent.getLongExtra("TASK-ID", -1);
	        mDBAdapter = DatabaseAdapter.getInstance(this.getActivity());
	        mTask = mDBAdapter.getTask(taskId);

	        setActionBar();

	        mFlipper = (ViewFlipper) getActivity().findViewById(R.id.flipper);

	        getActivity().findViewById(R.id.button_prev).setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // Go to previous month
	                mPosition--;
	                buildCalendar();
	                mFlipper.showNext();
	            }
	        });

	        getActivity().findViewById(R.id.button_next).setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // Go to next month
	                mPosition++;
	                buildCalendar();
	                mFlipper.showNext();
	            }
	        });

	        Intent returnIntent = new Intent();
	        getActivity().setResult(RESULT_CANCELED, returnIntent);

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

//	    @Override
//	    public void onDestroy() {
//	        super.onDestroy();
//	    }

//	    @Override
//	    public boolean onOptionsItemSelected(MenuItem item) {
//	        switch (item.getItemId()) {
//	        case android.R.id.home:
//	            getActivity().finish();
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	        }
//	    }

	    @Override
	    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
	        super.onCreateContextMenu(menu, view, menuInfo);
	        mPressedView = view;
	        Date date = (Date) mPressedView.getTag();
	        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.JAPAN);
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

	        SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
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
	            getActivity().setResult(RESULT_OK, returnIntent);

	            ReminderManager.getInstance().setNextAlert(this.getActivity());
	        }

	        return super.onContextItemSelected(item);
	    }

	    /***
	     * Set action bar
	     */
	    private void setActionBar() {
//	    	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

	        if (mTask != null) {
	        	getActivity().setTitle(mTask.getName());
	        }
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
	            View child = getActivity().getLayoutInflater().inflate(R.layout.calendar_week, null);
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
	        SimpleDateFormat sdf_d = new SimpleDateFormat("dd", Locale.JAPAN);
	        SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);

	        // Fill the days of previous month in the first week with blank rectangle
	        for (int i = 0; i < (week - Calendar.SUNDAY); i++) {
	            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
	            params.rowSpec = GridLayout.spec(1);
	            params.columnSpec = GridLayout.spec(i);
	            View child = getActivity().getLayoutInflater().inflate(R.layout.calendar_date, null);
	            child.setBackgroundResource(R.drawable.bg_calendar_day_blank);
	            mGridLayout.addView(child, params);
	        }

	        for (int day = 1; day <= maxDate; day++) {
	            View child = getActivity().getLayoutInflater().inflate(R.layout.calendar_date, null);
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
	        View layout = getActivity().findViewById(pages[mNextPageIndex]);
	        mNextPageIndex = (mNextPageIndex + 1) % 2;
	        return layout;
	    }

	    private void setVisibilityOfNextButton() {
	        View button = getActivity().findViewById(R.id.button_next);
	        if (mPosition < 0) {
	            button.setVisibility(View.VISIBLE);
	        } else {
	            button.setVisibility(View.INVISIBLE);
	        }
	    }
	    
	}

}