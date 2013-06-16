package com.hkb48.keepdo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final long taskId = getIntent().getLongExtra("TASK-ID", -1);
        final Task task = DatabaseAdapter.getInstance(this).getTask(taskId);
        if (task != null) {
            setTitle(task.getName());
        }

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

    public void onTabSelected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
        if (tab.getPosition() == 0) {
            Fragment fragment = new CalendarFragment();
            getSupportFragmentManager().beginTransaction()
                     .replace(R.id.container, fragment).commit();
        } else {
            Fragment fragment = new TaskDetailFragment();
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
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_task, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.menu_share:
        	shareDisplayedCalendarView();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	private void shareDisplayedCalendarView() {
    	final String BITMAP_PATH = DatabaseAdapter.getInstance(this).backupDirPath() + "/temp_share_image.png";

    	View calendarRoot = findViewById(R.id.calendar_root);
    	calendarRoot.setDrawingCacheEnabled(true);

    	File bitmapFile = new File(BITMAP_PATH);
    	bitmapFile.getParentFile().mkdir();
    	Bitmap bitmap = Bitmap.createBitmap(calendarRoot.getDrawingCache());

    	FileOutputStream fos = null;
    	try {
    		fos = new FileOutputStream(bitmapFile, false);
    		bitmap.compress(CompressFormat.PNG, 100, fos);
    		fos.flush();
    		fos.close();
    		calendarRoot.setDrawingCacheEnabled(false);
    	} catch (Exception e) {
		} finally {
			try {
    	         if (fos != null) {
    	        	 fos.close();
    	         }
			} catch (IOException e) {
			}
    	}

    	Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(bitmapFile));
        //intent.putExtra(Intent.EXTRA_TEXT, "10連続コンボ中です！ https://play.google.com/store/apps/details?id=com.hkb48.keepdo");
        startActivity(intent);
	}
}
