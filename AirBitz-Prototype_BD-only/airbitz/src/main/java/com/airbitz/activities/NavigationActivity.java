package com.airbitz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.RequestFragment;
import com.airbitz.fragments.SendFragment;
import com.airbitz.fragments.SettingFragment;
import com.airbitz.fragments.WalletsFragment;
import com.crashlytics.android.Crashlytics;

import java.util.Stack;

/**
 * The main Navigation activity holding fragments for anything controlled with
 * the custom Navigation Bar for Airbitz
 * Created by Thomas Baker on 4/22/14.
 */
public class NavigationActivity extends FragmentActivity
implements NavigationBarFragment.OnScreenSelectedListener {

    public enum Tabs { BD, REQUEST, SEND, WALLET, SETTING }
    private NavigationBarFragment mNavBarFragment;

    private int mNavFragmentId;
    private Fragment[] mNavFragments = {
            new BusinessDirectoryFragment(),
            new RequestFragment(),
            new SendFragment(),
            new WalletsFragment(),
            new SettingFragment()};

    // These stacks are the five "threads" of fragments represented in mNavFragments
    private Stack<Fragment>[] mNavStacks = new Stack[mNavFragments.length];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_navigation);
        mNavBarFragment = (NavigationBarFragment) getFragmentManager().findFragmentById(R.id.navigationFragment);

        for(int i=0; i< mNavFragments.length; i++) {
            mNavStacks[i] = new Stack<Fragment>();
            mNavStacks[i].push(mNavFragments[i]);
        }
        switchFragmentThread(Tabs.BD.ordinal());
    }

    /*
        Implements interface to receive navigation changes from the bottom nav bar
     */
    public void onNavBarSelected(int position) {
        if(userLoggedIn()) {
            switchFragmentThread(position);
        } else {
            startActivity(new Intent(this, LandingActivity.class));
        }
    }

    public void switchFragmentThread(int id) {
        mNavBarFragment.unselectTab(mNavFragmentId);
        mNavBarFragment.unselectTab(id); // just needed for resetting mLastTab
        mNavBarFragment.selectTab(id);
        mNavFragmentId = id;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityLayout, mNavStacks[id].peek());
        transaction.commit();
    }

    public void pushFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityLayout, fragment);
        mNavStacks[mNavFragmentId].push(fragment);
        transaction.commit();
    }

    public void popFragment() {
        mNavStacks[mNavFragmentId].pop();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityLayout, mNavStacks[mNavFragmentId].peek());
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(mNavStacks[mNavFragmentId].size() == 1)
            super.onBackPressed();
        else
            popFragment();
    }

    private boolean userLoggedIn() {
        return true;
    }
}
