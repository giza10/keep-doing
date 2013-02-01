package com.hkb48.keepdo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        setTitle("AlartSetting");

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new AlertPreferences();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }
}
