/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.adapters.NavigationAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.fragments.CategoryFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.LandingFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.PasswordRecoveryFragment;
import com.airbitz.fragments.RequestFragment;
import com.airbitz.fragments.RequestQRCodeFragment;
import com.airbitz.fragments.SendConfirmationFragment;
import com.airbitz.fragments.SendFragment;
import com.airbitz.fragments.SettingFragment;
import com.airbitz.fragments.SignUpFragment;
import com.airbitz.fragments.SuccessFragment;
import com.airbitz.fragments.TransparentFragment;
import com.airbitz.fragments.WalletsFragment;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.Numberpad;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * The main Navigation activity holding fragments for anything controlled with
 * the custom Navigation Bar for Airbitz
 * Created by Thomas Baker on 4/22/14.
 */
public class NavigationActivity extends Activity
        implements NavigationBarFragment.OnScreenSelectedListener,
        CoreAPI.OnIncomingBitcoin,
        CoreAPI.OnDataSync,
        CoreAPI.OnBlockHeightChange,
        CoreAPI.OnRemotePasswordChange {
    private final int DIALOG_TIMEOUT_MILLIS = 120000;
    public static final int ALERT_PAYMENT_TIMEOUT = 20000;
    public static final int DOLLAR_CURRENCY_NUMBER = 840;

    public static final String URI_DATA = "com.airbitz.navigation.uri";
    public static final String URI_SOURCE = "URI";
    public static Typeface montserratBoldTypeFace;
    public static Typeface montserratRegularTypeFace;
    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;
    public static Typeface helveticaNeueTypeFace;
    final Runnable delayedShowNavBar = new Runnable() {
        @Override
        public void run() {
            mNavBarFragmentLayout.setVisibility(View.VISIBLE);
            mFragmentLayout.setLayoutParams(getFragmentLayoutParams());
            mFragmentLayout.invalidate();
        }
    };

    final Runnable delayedShowCalculator = new Runnable() {
        @Override
        public void run() {
            mCalculatorView.setVisibility(View.VISIBLE);
            mCalculatorView.setEnabled(true);
        }
    };

    final Runnable delayedShowNumberpad = new Runnable() {
        @Override
        public void run() {
            mNumberpadView.setVisibility(View.VISIBLE);
            mNumberpadView.setEnabled(true);
        }
    };

    private final String TAG = getClass().getSimpleName();
    BroadcastReceiver ConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                if (networkIsAvailable()) {
                    Log.d(TAG, "Connection available");
                    mCoreAPI.restoreConnectivity();
                } else { // has connection
                    Log.d(TAG, "Connection NOT available");
                    mCoreAPI.lostConnectivity();
                    ShowOkMessageDialog(getString(R.string.string_no_connection_title), getString(R.string.string_no_connection_message));
                }
            }
        }
    };
    ViewGroup.LayoutParams mFragmentLayoutParams;
    int mNavBarStart;
    String mUUID, mTxId;
    Handler mHandler = new Handler();
    private CoreAPI mCoreAPI;
    private Uri mDataUri;
    private boolean keyBoardUp = false;
    private boolean mCalcLocked = false;
    private NavigationBarFragment mNavBarFragment;
    private RelativeLayout mNavBarFragmentLayout;
    private Calculator mCalculatorView;
    private Numberpad mNumberpadView;
    private LinearLayout mFragmentLayout;
    private ViewPager mViewPager;
    private int mNavThreadId;
    private Fragment[] mNavFragments = {
            new BusinessDirectoryFragment(),
            new RequestFragment(),
            new SendFragment(),
            new WalletsFragment(),
            new SettingFragment()};
    // These stacks are the five "threads" of fragments represented in mNavFragments
    private Stack<Fragment>[] mNavStacks = new Stack[mNavFragments.length];
    private List<Fragment> mOverlayFragments = new ArrayList<Fragment>();
    // Callback interface when a wallet could be updated
    private OnWalletUpdated mOnWalletUpdated;
    private AlertDialog mIncomingDialog;
    final Runnable dialogKiller = new Runnable() {
        @Override
        public void run() {
            if (mIncomingDialog != null) {
                updateWalletListener();
                mIncomingDialog.dismiss(); // hide dialog
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initiateCore();

        mCoreAPI.setOnIncomingBitcoinListener(this);
        mCoreAPI.setOnDataSyncListener(this);
        mCoreAPI.setOnBlockHeightChangeListener(this);
        mCoreAPI.setOnOnRemotePasswordChangeListener(this);

        setContentView(R.layout.activity_navigation);
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_app));
        mNavBarFragmentLayout = (RelativeLayout) findViewById(R.id.navigationLayout);
        mFragmentLayout = (LinearLayout) findViewById(R.id.activityLayout);
        mCalculatorView = (Calculator) findViewById(R.id.navigation_calculator_layout);
        mNumberpadView = (Numberpad) findViewById(R.id.navigation_numberpad_layout);

        setTypeFaces();

        for (int i = 0; i < mNavFragments.length; i++) {
            mNavStacks[i] = new Stack<Fragment>();
            mNavStacks[i].push(mNavFragments[i]);
        }

        // for keyboard hide and show
        final View activityRootView = findViewById(R.id.activity_navigation_root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    keyBoardUp = true;
                    hideNavBar();
                    if (mNavStacks[mNavThreadId].peek() instanceof CategoryFragment) {
                        ((CategoryFragment) mNavStacks[mNavThreadId].get(mNavStacks[mNavThreadId].size() - 1)).hideDoneCancel();
                    }
                } else {
                    keyBoardUp = false;
                    if (AirbitzApplication.isLoggedIn()) {
                        showNavBar();
                    }
                    if (mNavStacks[mNavThreadId].peek() instanceof CategoryFragment) {
                        ((CategoryFragment) mNavStacks[mNavThreadId].get(mNavStacks[mNavThreadId].size() - 1)).showDoneCancel();
                    }
                }
            }
        });

        // Setup top screen - the Landing - that swipes away if no login
        mViewPager = (ViewPager) findViewById(R.id.navigation_view_pager);
        mViewPager.setVisibility(View.GONE);
        setViewPager();

        mNavBarFragment = (NavigationBarFragment) getFragmentManager().findFragmentById(R.id.navigationFragment);
    }

    public void initiateCore() {
        mCoreAPI = CoreAPI.getApi();
        String seed = CoreAPI.getSeedData();
        mCoreAPI.Initialize(this, seed, seed.length());
    }

    @Override
    public void onStart() {
        super.onStart();
        Uri dataUri = getIntent().getData();
        if (dataUri != null && dataUri.getScheme().equals("bitcoin")) {
            onBitcoinUri(dataUri);
        }
    }

    public void DisplayLoginOverlay(boolean overlay) {
        DisplayLoginOverlay(overlay, false);
    }

    public void DisplayLoginOverlay(boolean overlay, boolean animate) {
        setViewPager();
        if (overlay) {
            // We are already showing so don't bother
            if (mViewPager.getCurrentItem() == 1) {
                return;
            }
            mViewPager.setCurrentItem(1, false);
            if (animate) {
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(250);
                mViewPager.startAnimation(anim);
            }
            mViewPager.setVisibility(View.VISIBLE);
        } else {
            mViewPager.setCurrentItem(0, animate);
        }
    }

    private void setViewPager() {
        mOverlayFragments.clear();
        mOverlayFragments.add(new TransparentFragment());
        mOverlayFragments.add(new LandingFragment());

        NavigationAdapter pageAdapter = new NavigationAdapter(getFragmentManager(), mOverlayFragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Disappear if transparent page shows
                if ((position == 0) && positionOffsetPixels == 0) {
                    hideSoftKeyboard(mNavBarFragmentLayout);
                    mViewPager.setVisibility(View.GONE);
                }
            }

            public void onPageSelected(int position) {
                // Disappear if transparent page shows
                if (position == 0) {
                    hideSoftKeyboard(mNavBarFragmentLayout);
                }
            }
        });
    }

    private void setTypeFaces() {
        montserratBoldTypeFace = Typeface.createFromAsset(getAssets(), "font/Montserrat-Bold.ttf");
        montserratRegularTypeFace = Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace = Typeface.createFromAsset(getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getAssets(), "font/Lato-Regular.ttf");
        helveticaNeueTypeFace = Typeface.createFromAsset(getAssets(), "font/HelveticaNeue.ttf");
    }

    /*
        Implements interface to receive navigation changes from the bottom nav bar
     */
    public void onNavBarSelected(int position) {
        if (AirbitzApplication.isLoggedIn()) {
            hideSoftKeyboard(mFragmentLayout);
            if (position != mNavThreadId) {
                AirbitzApplication.setLastNavTab(position);
                switchFragmentThread(position);
            }
        } else {
            if (position != Tabs.BD.ordinal()) {
                AirbitzApplication.setLastNavTab(position);
                mNavBarFragment.unselectTab(position);
                mNavBarFragment.unselectTab(Tabs.BD.ordinal()); // to reset mLastTab
                mNavBarFragment.selectTab(Tabs.BD.ordinal());
                DisplayLoginOverlay(true, true);
            }
        }
    }

    public void switchFragmentThread(int id) {
        if (mNavBarFragmentLayout.getVisibility() != View.VISIBLE && AirbitzApplication.isLoggedIn()) {
            showNavBar();
        }

        Fragment frag = mNavStacks[id].peek();
        Fragment fragShown = getFragmentManager().findFragmentById(R.id.activityLayout);
        if (fragShown != null)
            Log.d(TAG, "switchFragmentThread frag, fragShown is " + frag.getClass().getSimpleName() + ", " + fragShown.getClass().getSimpleName());
        else
            Log.d(TAG, "switchFragmentThread no fragment showing yet ");

        Log.d(TAG, "switchFragmentThread pending transactions executed ");

        FragmentTransaction transaction = getFragmentManager().beginTransaction().disallowAddToBackStack();
        if (frag.isAdded()) {
            Log.d(TAG, "Fragment already added, detaching and attaching");
            transaction.detach(mNavStacks[mNavThreadId].peek());
            transaction.attach(frag);
        } else {
            transaction.replace(R.id.activityLayout, frag);
            Log.d(TAG, "switchFragmentThread replace executed.");
        }
        transaction.commit();
        Log.d(TAG, "switchFragmentThread transactions committed.");
        fragShown = getFragmentManager().findFragmentById(R.id.activityLayout);
        if (fragShown != null) {
            Log.d(TAG, "switchFragmentThread showing frag is " + fragShown.getClass().getSimpleName());
        } else {
            Log.d(TAG, "switchFragmentThread showing frag is null");
        }
        mNavBarFragment.unselectTab(mNavThreadId);
        mNavBarFragment.unselectTab(id); // just needed for resetting mLastTab
        mNavBarFragment.selectTab(id);
        AirbitzApplication.setLastNavTab(id);
        mNavThreadId = id;

        Log.d(TAG, "switchFragmentThread switch to threadId " + mNavThreadId);

        getFragmentManager().executePendingTransactions();
    }

    public void switchFragmentThread(int id, Bundle bundle) {
        if (bundle != null)
            mNavStacks[id].peek().setArguments(bundle);
        switchFragmentThread(id);
    }

    public void pushFragment(Fragment fragment) {
        pushFragment(fragment, mNavThreadId);
    }

    public void pushFragment(Fragment fragment, int threadID) {
        mNavStacks[threadID].push(fragment);

        // Only show visually if we're displaying the thread
        if (mNavThreadId == threadID) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            if (mNavStacks[threadID].size() != 0 && !(fragment instanceof HelpFragment)) {
                transaction.setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_out_left);
            }
            transaction.replace(R.id.activityLayout, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    public void pushFragmentNoAnimation(Fragment fragment, int threadID) {
        mNavStacks[threadID].push(fragment);

        // Only show visually if we're displaying the thread
        if (mNavThreadId == threadID) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.activityLayout, fragment);
            transaction.commitAllowingStateLoss();
        }
        getFragmentManager().executePendingTransactions();
    }

    public void popFragment() {
        hideSoftKeyboard(mFragmentLayout);
        Fragment fragment = mNavStacks[mNavThreadId].pop();
        getFragmentManager().executePendingTransactions();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if ((mNavStacks[mNavThreadId].size() != 0) && !(fragment instanceof HelpFragment)) {
            transaction.setCustomAnimations(R.animator.slide_in_from_left, R.animator.slide_out_right);
        }
        transaction.replace(R.id.activityLayout, mNavStacks[mNavThreadId].peek());
        transaction.commitAllowingStateLoss();
    }

    private ViewGroup.LayoutParams getFragmentLayoutParams() {
        if (mFragmentLayoutParams == null) {
            mFragmentLayoutParams = mFragmentLayout.getLayoutParams();
        }
        return mFragmentLayoutParams;
    }

    int getNavBarStart() {
        if (mNavBarStart == 0) {
            int loc[] = new int[2];
            mNavBarFragmentLayout.getLocationOnScreen(loc);
            mNavBarStart = loc[1];
        }
        return mNavBarStart;
    }

    public void hideNavBar() {
        if (mNavBarFragmentLayout.getVisibility() == View.VISIBLE) {
            mFragmentLayoutParams = getFragmentLayoutParams();
            mNavBarFragmentLayout.setVisibility(View.GONE);
            RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mFragmentLayout.setLayoutParams(rLP);
            mFragmentLayout.invalidate();
            int test = getNavBarStart();
        }
    }

    public void showNavBar() {
        if (mNavBarFragmentLayout.getVisibility() == View.GONE && !keyBoardUp) {
            mHandler.postDelayed(delayedShowNavBar, 50);
        }
    }

    public Calculator getCalculatorView() {
        return mCalculatorView;
    }

    public Numberpad getNumberpadView() {
        return mNumberpadView;
    }

    public boolean isLargeDpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return !(metrics.densityDpi <= DisplayMetrics.DENSITY_HIGH);
    }

    public void lockCalculator() {
        if (!isLargeDpi()) {
            return;
        }
        int tbHeight = getResources().getDimensionPixelSize(R.dimen.tabbar_height);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mCalculatorView.getLayoutParams();

        // Move calculator above the tab bar
        params.setMargins(0, 0, 0, tbHeight);
        mCalculatorView.setLayoutParams(params);
        mCalcLocked = true;
        showCalculator();
    }

    public void unlockCalculator() {
        if (!mCalcLocked) {
            return;
        }
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mCalculatorView.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        mCalculatorView.setLayoutParams(params);
        mCalcLocked = false;
        if (isLargeDpi()) {
            mCalculatorView.showDoneButton();
        }
        hideCalculator();
    }

    public void hideCalculator() {
        mHandler.removeCallbacks(delayedShowCalculator);
        mCalculatorView.setVisibility(View.GONE);
        mCalculatorView.setEnabled(false);
    }

    public void showCalculator() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mFragmentLayout.getWindowToken(), 0);

        if (mCalculatorView.getVisibility() != View.VISIBLE) {
            mHandler.postDelayed(delayedShowCalculator, 100);
        }
    }

    public void hideNumberpad() {
        mHandler.removeCallbacks(delayedShowNumberpad);
        mNumberpadView.setVisibility(View.GONE);
        mNumberpadView.setEnabled(false);
    }

    public void showNumberpad() {
        mHandler.postDelayed(delayedShowNumberpad, 100);
    }

    public void onCalculatorButtonClick(View v) {
        mCalculatorView.onButtonClick(v);
        if (v.getTag().toString().equals("done")) {
            hideCalculator();
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getVisibility() == View.VISIBLE) {
            View v = findViewById(R.id.modal_indefinite_progress);
            if (v.getVisibility() != View.VISIBLE) {
                DisplayLoginOverlay(false, true);
            }
            return;
        }

        // If fragments want the back key, they can have it
        Fragment fragment = mNavStacks[mNavThreadId].peek();
        if (fragment instanceof OnBackPress) {
            boolean handled = ((OnBackPress) fragment).onBackPress();
            if (handled)
                return;
        }


        boolean calcVisible = (mCalculatorView.getVisibility() == View.VISIBLE);

        if (!mCalcLocked) {
            hideCalculator();
        }

        if (mNavStacks[mNavThreadId].size() == 1) {
            if (!calcVisible || mCalcLocked) {
                // This emulates user pressing Home button, rather than finish this activity
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        } else {
            if (fragment instanceof RequestQRCodeFragment) {
                popFragment();
                showNavBar();
            } else {//needed or show nav before switching fragments
                popFragment();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11 in Support Package
    }

    @Override
    public void onResume() {
        //******************* HockeyApp support
        // Always check for crashes and send to Hockey if user chooses to
        String hockeyKey = getString(R.string.hockey_key);
        CrashManager.register(this, hockeyKey);

        // Only allow updates for debug builds
        if (AirbitzApplication.isDebugging()) {
            UpdateManager.register(this, hockeyKey);
        }
        //******************* end HockeyApp support

        checkLoginExpired();

        //Look for Connection change events
        registerReceiver(ConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        mNavThreadId = AirbitzApplication.getLastNavTab();

        if (!AirbitzApplication.isLoggedIn()) {
            if (mDataUri != null)
                DisplayLoginOverlay(true);

            mNavThreadId = Tabs.BD.ordinal();
        } else {
            DisplayLoginOverlay(false);
            mCoreAPI.restoreConnectivity();
        }
        switchFragmentThread(mNavThreadId);

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(ConnectivityChangeReceiver);
        mCoreAPI.lostConnectivity();
        AirbitzApplication.setBackgroundedTime(System.currentTimeMillis());
    }

    /*
     * this only gets called from sent funds, or a request comes through
     */
    public void switchToWallets(Bundle bundle) {
        Fragment frag = new WalletsFragment();
        bundle.putBoolean(WalletsFragment.CREATE, true);
        frag.setArguments(bundle);
        mNavStacks[Tabs.WALLET.ordinal()].clear();
        mNavStacks[Tabs.WALLET.ordinal()].add(frag);

        switchFragmentThread(Tabs.WALLET.ordinal());
    }

    public void clearBD() {
        Fragment top = mNavStacks[Tabs.BD.ordinal()].peek();
        while (!(top instanceof BusinessDirectoryFragment)) {
            mNavStacks[Tabs.BD.ordinal()].pop();
            top = mNavStacks[Tabs.BD.ordinal()].peek();
        }
    }

    /*
     * Handle bitcoin:<address> Uri's coming from OS
     */
    private void onBitcoinUri(Uri dataUri) {
        Log.d(TAG, "Received onBitcoin with uri = " + dataUri.toString());
        if (!AirbitzApplication.isLoggedIn()) {
            mDataUri = dataUri;
            return;
        }
        resetFragmentThreadToBaseFragment(Tabs.SEND.ordinal());

        if (mNavThreadId != Tabs.SEND.ordinal()) {
            Bundle bundle = new Bundle();
            bundle.putString(WalletsFragment.FROM_SOURCE, URI_SOURCE);
            bundle.putString(URI_DATA, dataUri.toString());
            switchFragmentThread(Tabs.SEND.ordinal(), bundle);
        } else {
            CoreAPI.BitcoinURIInfo info = mCoreAPI.CheckURIResults(dataUri.toString());
            if (info != null && info.address != null) {
                switchFragmentThread(Tabs.SEND.ordinal());
                Fragment fragment = new SendConfirmationFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(SendFragment.IS_UUID, false);
                bundle.putString(SendFragment.UUID, info.address);
                bundle.putLong(SendFragment.AMOUNT_SATOSHI, info.amountSatoshi);
                bundle.putString(SendFragment.LABEL, info.label);
                bundle.putString(SendFragment.FROM_WALLET_UUID, mCoreAPI.getCoreWallets(false).get(0).getUUID());
                fragment.setArguments(bundle);
                pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
            }
        }
    }

    @Override
    public void onIncomingBitcoin(String walletUUID, String txId) {
        Log.d(TAG, "onIncomingBitcoin uuid, txid = " + walletUUID + ", " + txId);
        mUUID = walletUUID;
        mTxId = txId;
        /* If showing QR code, launch receiving screen*/
        RequestQRCodeFragment f = requestMatchesQR(mUUID, mTxId);
        Log.d(TAG, "RequestFragment? " + f);
        if (f != null) {
            long diff = f.requestDifference(mUUID, mTxId);
            if (diff == 0) {
                // sender paid exact amount
                handleReceiveFromQR();
            } else if (diff < 0) {
                // sender paid too much
                handleReceiveFromQR();
            } else {
                // Request the remainer of the funds
                f.updateWithAmount(diff);
            }
        } else {
            showIncomingBitcoinDialog();
        }
    }

    private void handleReceiveFromQR() {
        if (!SettingFragment.getMerchantModePref()) {
            startReceivedSuccess();
        } else {
            hideSoftKeyboard(mFragmentLayout);
            Bundle bundle = new Bundle();
            bundle.putString(RequestFragment.MERCHANT_MODE, "merchant");
            resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.REQUEST.ordinal());
            switchFragmentThread(NavigationActivity.Tabs.REQUEST.ordinal(), bundle);
            ShowFadingDialog(getString(R.string.string_payment_received), ALERT_PAYMENT_TIMEOUT);
        }
    }

    private RequestQRCodeFragment requestMatchesQR(String uuid, String txid) {
        Fragment f = mNavStacks[mNavThreadId].peek();
        if (!(f instanceof RequestQRCodeFragment)) {
            return null;
        }
        RequestQRCodeFragment qr = (RequestQRCodeFragment) f;
        if (qr.isShowingQRCodeFor(uuid, txid)) {
            return qr;
        } else {
            return null;
        }
    }

    public void onSentFunds(String walletUUID, String txId) {
        Log.d(TAG, "onSentFunds uuid, txid = " + walletUUID + ", " + txId);

        getFragmentManager().executePendingTransactions();

        Bundle bundle = new Bundle();
        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);
        bundle.putBoolean(WalletsFragment.CREATE, true);
        bundle.putString(Transaction.TXID, txId);
        bundle.putString(Wallet.WALLET_UUID, walletUUID);

        Log.d(TAG, "onSentFunds calling switchToWallets");
        switchToWallets(bundle);

        while (mNavStacks[Tabs.SEND.ordinal()].size() > 0) {
            Log.d(TAG, "Send thread removing " + mNavStacks[Tabs.SEND.ordinal()].peek().getClass().getSimpleName());
            mNavStacks[Tabs.SEND.ordinal()].pop();
        }
        Fragment frag = getNewBaseFragement(Tabs.SEND.ordinal());
        mNavStacks[Tabs.SEND.ordinal()].push(frag); // Set first fragment but don't show
    }

    public void setOnWalletUpdated(OnWalletUpdated listener) {
        mOnWalletUpdated = listener;
    }

    private void updateWalletListener() {
        if (mOnWalletUpdated != null)
            mOnWalletUpdated.onWalletUpdated();
    }

    @Override
    public void OnDataSync() {
        Log.d(TAG, "Data Sync received");
        updateWalletListener();
    }

    @Override
    public void onBlockHeightChange() {
        Log.d(TAG, "Block Height received");
        updateWalletListener();
    }

    @Override
    public void OnRemotePasswordChange() {
        Log.d(TAG, "Remote Password received");
        if (!(mNavStacks[mNavThreadId].peek() instanceof SignUpFragment)) {
            showRemotePasswordChangeDialog();
        }
    }

    private void startReceivedSuccess() {
        Bundle bundle = new Bundle();
        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_REQUEST);
        bundle.putString(Transaction.TXID, mTxId);
        bundle.putString(Wallet.WALLET_UUID, mUUID);

        Fragment frag = new SuccessFragment();
        frag.setArguments(bundle);
        pushFragment(frag, mNavThreadId);

        resetFragmentThreadToBaseFragment(Tabs.REQUEST.ordinal());
    }

    private void gotoDetailsNow() {
        Bundle bundle = new Bundle();
        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_REQUEST);
        bundle.putString(Transaction.TXID, mTxId);
        bundle.putString(Wallet.WALLET_UUID, mUUID);
        switchToWallets(bundle);

        resetFragmentThreadToBaseFragment(Tabs.REQUEST.ordinal());
    }

    public void resetFragmentThreadToBaseFragment(int threadId) {
        mNavStacks[threadId].clear();
        mNavStacks[threadId].add(getNewBaseFragement(threadId));
    }

    private void showIncomingBitcoinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(getResources().getString(R.string.received_bitcoin_message))
                .setTitle(getResources().getString(R.string.received_bitcoin_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.received_bitcoin_positive),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                gotoDetailsNow();
                            }
                        }
                )
                .setNegativeButton(getResources().getString(R.string.received_bitcoin_negative),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                resetFragmentThreadToBaseFragment(Tabs.REQUEST.ordinal());
                                updateWalletListener();
                                dialog.cancel();
                            }
                        }
                );
        mIncomingDialog = builder.create();
        mIncomingDialog.show();
        mHandler.postDelayed(dialogKiller, 5000);
    }

    private void showRemotePasswordChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(getResources().getString(R.string.remote_password_change_message))
                .setTitle(getResources().getString(R.string.remote_password_change_title))
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Logout();
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void UserJustLoggedIn() {
        showNavBar();
        mCoreAPI.setupAccountSettings();
        mCoreAPI.startAllAsyncUpdates();
        if (mDataUri != null) {
            onBitcoinUri(mDataUri);
            mDataUri = null;
        } else {
            switchFragmentThread(AirbitzApplication.getLastNavTab());
        }
        DisplayLoginOverlay(false, true);
        checkFirstWalletSetup();
        if(!mCoreAPI.coreSettings().getBDisablePINLogin()) {
            mCoreAPI.PinSetup(AirbitzApplication.getUsername(), mCoreAPI.coreSettings().getSzPIN());
        }
    }

    public void startRecoveryQuestions(String questions, String username) {
        hideNavBar();
        Bundle bundle = new Bundle();
        bundle.putInt(PasswordRecoveryFragment.MODE, PasswordRecoveryFragment.FORGOT_PASSWORD);
        bundle.putString(PasswordRecoveryFragment.QUESTIONS, questions);
        bundle.putString(PasswordRecoveryFragment.USERNAME, username);
        Fragment frag = new PasswordRecoveryFragment();
        frag.setArguments(bundle);
        pushFragmentNoAnimation(frag, mNavThreadId);
        DisplayLoginOverlay(false, true);
    }

    public void startSignUp() {
        hideSoftKeyboard(mFragmentLayout);
        hideNavBar();
        Fragment frag = new SignUpFragment();
        pushFragmentNoAnimation(frag, mNavThreadId);
        DisplayLoginOverlay(false, true);
    }

    public void noSignup() {
        popFragment();
        showNavBar();
        DisplayLoginOverlay(true);
    }

    public void finishSignup() {
        showNavBar();
        switchFragmentThread(AirbitzApplication.getLastNavTab());
    }

    public void Logout() {
        if (AirbitzApplication.getUsername() != null) {
            mCoreAPI.PINLoginDelete(AirbitzApplication.getUsername());
        }
        AirbitzApplication.Logout();
        mCoreAPI.logout();
        DisplayLoginOverlay(false);
        startActivity(new Intent(this, NavigationActivity.class));
        finish();
    }

    private Fragment getNewBaseFragement(int id) {
        switch (id) {
            case 0:
                return new BusinessDirectoryFragment();
            case 1:
                return new RequestFragment();
            case 2:
                return new SendFragment();
            case 3:
                return new WalletsFragment();
            case 4:
                return new SettingFragment();
            default:
                return null;
        }
    }

    public boolean networkIsAvailable() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    Log.d(TAG, "Connection is WIFI");
                    haveConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    Log.d(TAG, "Connection is MOBILE");
                    haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void checkLoginExpired() {
        if (AirbitzApplication.getmBackgroundedTime() == 0 || !AirbitzApplication.isLoggedIn())
            return;

        long milliDelta = (System.currentTimeMillis() - AirbitzApplication.getmBackgroundedTime());

        Log.d(TAG, "delta logout time = " + milliDelta);
        if (milliDelta > mCoreAPI.coreSettings().getMinutesAutoLogout() * 60 * 1000) {
            Logout();
        }
    }


    public enum Tabs {BD, REQUEST, SEND, WALLET, SETTING}

    //************************ Connectivity support

    // For Fragments to implement if they need to customize on back presses
    public interface OnBackPress {
        public boolean onBackPress();
    }

    public interface OnWalletUpdated {
        public void onWalletUpdated();
    }

    public void LoginNow(String username, char[] password) {
        AirbitzApplication.Login(username, password);
        UserJustLoggedIn();
        setViewPager();
    }

    Runnable mProgressDialogKiller = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.modal_indefinite_progress).setVisibility(View.INVISIBLE);
            ShowOkMessageDialog(getResources().getString(R.string.string_connection_problem_title), getResources().getString(R.string.string_no_connection_response));
        }
    };

    AlertDialog mMessageDialog;
    Runnable mMessageDialogKiller = new Runnable() {
        @Override
        public void run() {
            if (mMessageDialog.isShowing()) {
                mMessageDialog.dismiss();
            }
        }
    };

    public void showModalProgress(final boolean show) {
        View v = findViewById(R.id.modal_indefinite_progress);
        if (show) {
            v.setVisibility(View.VISIBLE);
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true; // intercept all touches
                }
            });
            if (mHandler == null)
                mHandler = new Handler();
            mHandler.postDelayed(mProgressDialogKiller, DIALOG_TIMEOUT_MILLIS);
        } else {
            mHandler.removeCallbacks(mProgressDialogKiller);
            v.setVisibility(View.INVISIBLE);
        }
    }

    public void ShowOkMessageDialog(String title, String message) {
        if (mMessageDialog != null) {
            mMessageDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        mMessageDialog = builder.create();
        mMessageDialog.show();
    }

    public void ShowOkMessageDialog(String title, String message, int timeoutMillis) {
        mHandler.postDelayed(mMessageDialogKiller, timeoutMillis);
        ShowOkMessageDialog(title, message);
    }

    public void ShowMessageDialogBackPress(String title, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(reason)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                NavigationActivity.this.onBackPressed();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    //**************** Fading Dialog

    public interface OnFadingDialogFinished { public void onFadingDialogFinished();
    }

    private OnFadingDialogFinished mOnFadingDialogFinished;
    public void setFadingDialogListener(OnFadingDialogFinished listener) {
        mOnFadingDialogFinished = listener;
    }

    private void updateFadingDialogFinished() {
        if (mOnFadingDialogFinished != null)
            mOnFadingDialogFinished.onFadingDialogFinished();
    }

    public void DismissFadingDialog() {
        ShowFadingDialog("", 0);
    }

    public void ShowFadingDialog(String message) {
        ShowFadingDialog(message, 1000);
    }

    public void ShowFadingDialog(String message, int timeout) {
        ShowFadingDialog(message, timeout, true);
    }

    private Dialog mFadingDialog = null;
    public void ShowFadingDialog(String message, int timeout, boolean cancelable) {
        if(timeout == 0) {
            mFadingDialog.dismiss();
            return;
        }
        if(mFadingDialog != null) {
            mFadingDialog.dismiss();
        }
        mFadingDialog = new Dialog(this);
        mFadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mFadingDialog.setCancelable(cancelable);
        mFadingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        View view = this.getLayoutInflater().inflate(R.layout.fading_alert, null);
        ((TextView)view.findViewById(R.id.fading_alert_text)).setText(message);
        mFadingDialog.setContentView(view);
        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setStartOffset(timeout);
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mFadingDialog.dismiss();
                updateFadingDialogFinished();
            }

            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
        });

        view.setAnimation(fadeOut);
        mFadingDialog.show();
        view.startAnimation(fadeOut);
    }

    public void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void showSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    private void checkFirstWalletSetup() {
        List<String> wallets = mCoreAPI.loadWalletUUIDs();
        if (wallets.size() <= 0) {
            mWalletSetup = new SetupFirstWalletTask();
            mWalletSetup.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    /**
     * Represents an asynchronous creation of the first wallet
     */
    private SetupFirstWalletTask mWalletSetup;
    public class SetupFirstWalletTask extends AsyncTask<Void, Void, Boolean> {

        SetupFirstWalletTask() { }

        @Override
        protected void onPreExecute() {
            NavigationActivity.this.ShowFadingDialog(
                    getString(R.string.fragment_signup_creating_wallet),
                    200000, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Set default currency
            mCoreAPI.SetupDefaultCurrency();

            // Create the Wallet
            String walletName =
                getResources().getString(R.string.activity_recovery_first_wallet_name);
            return mCoreAPI.createWallet(
                    AirbitzApplication.getUsername(),
                    AirbitzApplication.getPassword(),
                    walletName, mCoreAPI.coreSettings().getCurrencyNum());
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mWalletSetup = null;
            if (!success) {
                NavigationActivity.this.ShowFadingDialog(
                    getResources().getString(R.string.activity_signup_create_wallet_fail));
            } else {
                // Update UI
                updateWalletListener();
                // Add categories
                createDefaultCategories();
                // Dismiss dialog
                NavigationActivity.this.DismissFadingDialog();
            }
            mCoreAPI.setupAccountSettings();
            mCoreAPI.startAllAsyncUpdates();
        }

        @Override
        protected void onCancelled() {
            mWalletSetup = null;
            NavigationActivity.this.DismissFadingDialog();
        }
    }

    private void createDefaultCategories() {
        String[] defaults =
            getResources().getStringArray(R.array.category_defaults);

        for (String cat : defaults)
            mCoreAPI.addCategory(cat);

        List<String> cats = mCoreAPI.loadCategories();
        if (cats.size() == 0 || cats.get(0).equals(defaults)) {
            Log.d(TAG, "Category creation failed");
        }
    }
}
