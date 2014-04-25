package com.airbitz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.airbitz.R;
import com.airbitz.adapters.NavigationAdapter;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.RequestFragment;
import com.airbitz.fragments.SendFragment;
import com.airbitz.fragments.SettingFragment;
import com.airbitz.fragments.WalletFragment;

/**
 * The main Navigation activity holding fragments for anything controlled with
 * the custom Navigation Bar for Airbitz
 * Created by Thomas Baker on 4/22/14.
 */
public class NavigationActivity extends FragmentActivity
implements NavigationBarFragment.OnScreenSelectedListener {

    private ViewPager mViewPager;
    private NavigationBarFragment mNavBarFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mViewPager = (ViewPager)findViewById(R.id.activityPager);

        NavigationAdapter pagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new BusinessDirectoryFragment());
        pagerAdapter.addFragment(new RequestFragment());
        pagerAdapter.addFragment(new SendFragment());
        pagerAdapter.addFragment(new WalletFragment());
        pagerAdapter.addFragment(new SettingFragment());

        mViewPager.setAdapter(pagerAdapter);
    }

    @Override
    public void onScreenSelected(int position) {
        if(userLoggedIn())
            mViewPager.setCurrentItem(position);
        else {
            startActivity(new Intent(this, LandingActivity.class));
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private boolean userLoggedIn() {
        return true;
    }
}
