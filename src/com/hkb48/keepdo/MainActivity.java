package com.hkb48.keepdo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	// Our application database
	protected DatabaseHelper mDatabase = null; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		mDatabase = new DatabaseHelper(this.getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mDatabase != null)
		{
			mDatabase.close();
		}
	}
}
