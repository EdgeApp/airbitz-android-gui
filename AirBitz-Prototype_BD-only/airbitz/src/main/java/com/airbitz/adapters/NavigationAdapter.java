package com.airbitz.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 4/23/14.
 */
public class NavigationAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments;

        public NavigationAdapter(FragmentManager manager, List<Fragment> fragments) {
            super(manager);
            mFragments = fragments;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
