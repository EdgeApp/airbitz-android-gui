package com.airbitz.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.airbitz.R;
import com.airbitz.adapters.NavigationAdapter;
import com.airbitz.api.CallbackAsyncBitCoinInfo;
import com.airbitz.api.CallbackRequestResults;
import com.airbitz.api.SWIGTYPE_p_f_p_q_const__struct_sABC_AsyncBitCoinInfo__void;
import com.airbitz.api.SWIGTYPE_p_f_p_q_const__struct_sABC_RequestResults__void;
import com.airbitz.api.SWIGTYPE_p_void;
import com.airbitz.api.core;
import com.airbitz.api.tABC_AsyncBitCoinInfo;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_RequestResults;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.fragments.LandingFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.RequestFragment;
import com.airbitz.fragments.SendFragment;
import com.airbitz.fragments.SettingFragment;
import com.airbitz.fragments.TransparentFragment;
import com.airbitz.fragments.WalletsFragment;
import com.crashlytics.android.Crashlytics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * The main Navigation activity holding fragments for anything controlled with
 * the custom Navigation Bar for Airbitz
 * Created by Thomas Baker on 4/22/14.
 */
public class NavigationActivity extends FragmentActivity
implements NavigationBarFragment.OnScreenSelectedListener {

    static {
        System.loadLibrary("airbitz");
    }

    public enum Tabs { BD, REQUEST, SEND, WALLET, SETTING }
    private NavigationBarFragment mNavBarFragment;
    private RelativeLayout mNavBarFragmentLayout;
    private LinearLayout mCalculatorView;
    private RelativeLayout mCalculatorLayout;
    private LinearLayout mFragmentLayout;
    private ViewPager mViewPager;

    private int mNavFragmentId;
    private Fragment[] mNavFragments = {
            new BusinessDirectoryFragment(),
            new RequestFragment(),
            new SendFragment(),
            new WalletsFragment(),
            new SettingFragment()};

    // These stacks are the five "threads" of fragments represented in mNavFragments
    private Stack<Fragment>[] mNavStacks = new Stack[mNavFragments.length];

    private boolean mUserLoggedIn = false;

    public static Typeface montserratBoldTypeFace;
    public static Typeface montserratRegularTypeFace;
    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;
    public static Typeface helveticaNeueTypeFace;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_navigation);
        mNavBarFragment = (NavigationBarFragment) getFragmentManager().findFragmentById(R.id.navigationFragment);
        mNavBarFragmentLayout = (RelativeLayout) findViewById(R.id.navigationLayout);
        mFragmentLayout = (LinearLayout) findViewById(R.id.activityLayout);
        mCalculatorView = (LinearLayout) findViewById(R.id.calculator_layout);
        mCalculatorLayout = (RelativeLayout) findViewById(R.id.navigation_calculator_layout);

        setTypeFaces();

        for(int i=0; i< mNavFragments.length; i++) {
            mNavStacks[i] = new Stack<Fragment>();
            mNavStacks[i].push(mNavFragments[i]);
        }
        switchFragmentThread(Tabs.BD.ordinal());

        // for keyboard hide and show
        final View activityRootView = findViewById(R.id.activity_navigation_root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    hideNavBar();
                } else {
                    showNavBar();
                }
            }
        });

        // Setup top screen - the Landing - that swipes away if no login
        mViewPager = (ViewPager) findViewById(R.id.navigation_view_pager);

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new LandingFragment());
        fragments.add(new TransparentFragment());

        NavigationAdapter pageAdapter = new NavigationAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                // Disappear if transparent page shows
                if(position==1) {
                    mViewPager.setVisibility(View.GONE);
                }
            }
        });
        setLoginView(!mUserLoggedIn);

        tABC_Error pError = new tABC_Error();
        SWIGTYPE_p_f_p_q_const__struct_sABC_AsyncBitCoinInfo__void infoCallback = new InfoCallback();

        String seed = getSeedData();

        SWIGTYPE_p_void pData =null;

        tABC_CC code = core.ABC_Initialize("test", infoCallback, pData, seed, seed.length(), pError);

//        core.dispatchInfo(666);

        SWIGTYPE_p_f_p_q_const__struct_sABC_RequestResults__void resultsCallback = new ResultsCallback();
//        core.dispatchRequest(999);

        code = core.ABC_SignIn("", "", resultsCallback, pData, pError);
        Log.d("Signin code", code.toString());
    }

    public class InfoCallback extends SWIGTYPE_p_f_p_q_const__struct_sABC_AsyncBitCoinInfo__void implements CallbackAsyncBitCoinInfo {
        public InfoCallback() {
            core.setInfoCallback(this);
            long ptr = core.getInfoCallback();
//            Log.d("InfoCallback long", String.valueOf(ptr));
            this.swigCPtr = ptr;
        }

        public void OnAsyncBitCoinInfo(int val) {
            Log.d("InfoCallback received ", String.valueOf(val));
        }
    }

    public class ResultsCallback extends SWIGTYPE_p_f_p_q_const__struct_sABC_RequestResults__void implements CallbackRequestResults {
        public ResultsCallback() {
            core.setRequestCallback(this);
            long ptr = core.getRequestCallback();
//            Log.d("RequestCallback long", String.valueOf(ptr));
            this.swigCPtr = ptr;
        }

        public void OnRequestResults(int val) {
            Log.d("RequestCallback received ", String.valueOf(val));
        }
    }

    public void setLoginView(boolean show) {
        if(show) {
            mViewPager.setVisibility(View.VISIBLE);
            mViewPager.setCurrentItem(0);
        } else {
            mViewPager.setCurrentItem(1);
        }
    }

    private void setTypeFaces() {
        montserratBoldTypeFace=Typeface.createFromAsset(getAssets(), "font/Montserrat-Bold.ttf");
        montserratRegularTypeFace=Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace=Typeface.createFromAsset(getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace=Typeface.createFromAsset(getAssets(), "font/Lato-RegIta.ttf");
        helveticaNeueTypeFace=Typeface.createFromAsset(getAssets(), "font/HelveticaNeue.ttf");
    }

    /*
        Implements interface to receive navigation changes from the bottom nav bar
     */
    public void onNavBarSelected(int position) {
        if(getUserLoggedIn()) {
            switchFragmentThread(position);
        } else {
            mNavBarFragment.unselectTab(position); // just needed for resetting mLastTab
            setLoginView(true);
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

    public void hideNavBar() {
        mNavBarFragmentLayout.setVisibility(View.GONE);
    }

    public void showNavBar() {
        mNavBarFragmentLayout.setVisibility(View.VISIBLE);
    }

    public LinearLayout getCalculatorView() {
        return mCalculatorView;
    }

    public void hideCalculator() {
        mCalculatorLayout.setVisibility(View.GONE);
        mCalculatorLayout.setEnabled(false);
        showNavBar();
    }

    public void showCalculator() {
        hideNavBar();
        mCalculatorLayout.setVisibility(View.VISIBLE);
        mCalculatorLayout.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(mNavStacks[mNavFragmentId].size() == 1)
            super.onBackPressed();
        else
            popFragment();
    }

    private boolean getUserLoggedIn() {
//        return true;  // uncomment only for debugging
        return mUserLoggedIn;
    }

    public void setUserLoggedIn(boolean state) {
        mUserLoggedIn = state;
    }

    private String getSeedData()
    {
        String strSeed = new String();

//        // add the advertiser identifier
//        if ([[UIDevice currentDevice] respondsToSelector:@selector(identifierForVendor)])
//        {
//            [strSeed appendString:[[[UIDevice currentDevice] identifierForVendor] UUIDString]];
//        }
//
//        // add the UUID
//        CFUUIDRef theUUID = CFUUIDCreate(NULL);
//        CFStringRef string = CFUUIDCreateString(NULL, theUUID);
//        CFRelease(theUUID);
//        [strSeed appendString:[[NSString alloc] initWithString:(__bridge NSString *)string]];
//        CFRelease(string);
//
//        // add the device name
//        [strSeed appendString:[[UIDevice currentDevice] name]];
//
//        // add the string to the data
//        //NSLog(@"seed string: %@", strSeed);
//        [data appendData:[strSeed dataUsingEncoding:NSUTF8StringEncoding]];

        long time = System.nanoTime();
        ByteBuffer bb1 = ByteBuffer.allocate(8);
        bb1.putLong(time);
        strSeed += bb1.array();

        Random r = new Random();
        ByteBuffer bb2 = ByteBuffer.allocate(4);
        bb2.putInt(r.nextInt());
        strSeed += bb2.array();

        return strSeed;
    }


}
