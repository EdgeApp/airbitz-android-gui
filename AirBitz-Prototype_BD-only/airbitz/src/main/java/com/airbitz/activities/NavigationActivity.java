package com.airbitz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.airbitz.R;
import com.airbitz.adapters.NavigationAdapter;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.fragments.DirectoryDetailFragment;
import com.airbitz.fragments.MapBusinessDirectoryFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.RequestFragment;
import com.airbitz.fragments.SendFragment;
import com.airbitz.fragments.SettingFragment;
import com.airbitz.fragments.WalletFragment;
import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Stack;

/**
 * The main Navigation activity holding fragments for anything controlled with
 * the custom Navigation Bar for Airbitz
 * Created by Thomas Baker on 4/22/14.
 */
public class NavigationActivity extends FragmentActivity
implements NavigationBarFragment.OnScreenSelectedListener {

    private RelativeLayout mViewPager;
    private Fragment[] mFragments = {
        new BusinessDirectoryFragment(),
                new RequestFragment(),
                new SendFragment(),
                new WalletFragment(),
                new SettingFragment(),
                new MapBusinessDirectoryFragment(),
                new DirectoryDetailFragment() };

    private String[] mFragmentNames = {
        "BusinessDirectory",
                "Request",
                "Send",
                "Wallet",
                "Setting",
                "MapBusinessDirectory",
                "DirectoryDetail"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_navigation);

        mViewPager = (RelativeLayout)findViewById(R.id.activityLayout);

        setFragment(0);
    }

    /*
        Implements interface to receive navigation changes from the bottom nav bar
     */
    public void onNavBarSelected(int position) {
        if(userLoggedIn()) {
            clearBackStack();
            setFragment(position);
        } else {
            startActivity(new Intent(this, LandingActivity.class));
            finish();
        }
    }

    public void setFragment(int id) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityLayout, mFragments[id]);
        transaction.commit();
    }

    public void pushFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityLayout, fragment);
        transaction.addToBackStack("name");
        transaction.commit();
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
