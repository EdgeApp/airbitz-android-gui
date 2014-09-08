package com.airbitz.adapters;

import android.app.FragmentManager;
import android.app.Fragment;

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
