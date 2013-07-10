package com.hkb48.keepdo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CalendarFragment extends Fragment {

    /**
     * The maximum number of months to be paging up (10 years)
     */
    public static final int NUM_MAXIMUM_MOUNTHS = 10 * 12;
    private ViewPager mViewPager;

    public CalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = (ViewPager)view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new CalendarPageAdapter(getChildFragmentManager()));
        mViewPager.setCurrentItem(this.NUM_MAXIMUM_MOUNTHS);
    }

    public class CalendarPageAdapter extends FragmentPagerAdapter {
        public CalendarPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_MAXIMUM_MOUNTHS;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putInt(CalendarGrid.POSITION_KEY, position);
            return CalendarGrid.newInstance(args);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CalendarGrid.getPageTitle(position);
        }
    }

}