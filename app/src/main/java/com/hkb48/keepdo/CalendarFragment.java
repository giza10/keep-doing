package com.hkb48.keepdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CalendarFragment extends Fragment {

    /**
     * The maximum number of months to be paging up (10 years)
     */
    static final int NUM_MAXIMUM_MONTHS = 10 * 12;

    public CalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager mViewPager = view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new CalendarPageAdapter(getChildFragmentManager()));
        mViewPager.setCurrentItem(CalendarFragment.NUM_MAXIMUM_MONTHS);
    }

    public static class CalendarPageAdapter extends FragmentPagerAdapter {
        CalendarPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return CalendarFragment.NUM_MAXIMUM_MONTHS;
        }

        @Override
        @NonNull
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