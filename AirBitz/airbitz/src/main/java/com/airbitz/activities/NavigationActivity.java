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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.adapters.AccountsAdapter;
import com.airbitz.adapters.NavigationAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.directory.BusinessDirectoryFragment;
import com.airbitz.fragments.directory.DirectoryDetailFragment;
import com.airbitz.fragments.directory.MapBusinessDirectoryFragment;
import com.airbitz.fragments.login.LandingFragment;
import com.airbitz.fragments.login.SetupUsernameFragment;
import com.airbitz.fragments.login.SignUpFragment;
import com.airbitz.fragments.login.TransparentFragment;
import com.airbitz.fragments.request.AddressRequestFragment;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.fragments.send.SendConfirmationFragment;
import com.airbitz.fragments.send.SendFragment;
import com.airbitz.fragments.send.SuccessFragment;
import com.airbitz.fragments.settings.ImportFragment;
import com.airbitz.fragments.settings.PasswordRecoveryFragment;
import com.airbitz.fragments.settings.SettingFragment;
import com.airbitz.fragments.settings.twofactor.TwoFactorScanFragment;
import com.airbitz.fragments.wallet.TransactionListFragment;
import com.airbitz.fragments.wallet.WalletsFragment;
import com.airbitz.models.AirbitzNotification;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.AirbitzAlertReceiver;
import com.airbitz.objects.AudioPlayer;
import com.airbitz.objects.Numberpad;
import com.airbitz.objects.RememberPasswordCheck;
import com.airbitz.objects.UserReview;
import com.airbitz.plugins.BuySellFragment;
import com.airbitz.utils.Common;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class NavigationActivity extends ActionBarActivity
        implements NavigationBarFragment.OnScreenSelectedListener,
        CoreAPI.OnIncomingBitcoin,
        CoreAPI.OnExchangeRatesChange,
        CoreAPI.OnDataSync,
        CoreAPI.OnBlockHeightChange,
        CoreAPI.OnRemotePasswordChange,
        CoreAPI.OnOTPError,
        CoreAPI.OnOTPResetRequest,
        AddressRequestFragment.OnAddressRequest,
        TwoFactorScanFragment.OnTwoFactorQRScanResult,
        AccountsAdapter.OnButtonTouched {
    private final int DIALOG_TIMEOUT_MILLIS = 120000;

    public final String INCOMING_COUNT = "com.airbitz.navigation.incomingcount";

    public static final String LAST_MESSAGE_ID = "com.airbitz.navigation.LastMessageID";
    private Map<Integer, AirbitzNotification> mNotificationMap;

    public static final String URI_DATA = "com.airbitz.navigation.uri";
    public static final String URI_SOURCE = "URI";
    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;
    final Runnable delayedShowNavBar = new Runnable() {
        @Override
        public void run() {
            mActionButton.setVisibility(View.VISIBLE);
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
    private Numberpad mNumberpadView;
    private View mFragmentContainer;
    private LinearLayout mFragmentLayout;
    private ViewPager mViewPager;
    private int mNavThreadId;
    private Fragment[] mNavFragments = {
            new BusinessDirectoryFragment(),
            new RequestFragment(),
            new SendFragment(),
            new TransactionListFragment(),
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

    public Stack<AsyncTask> mAsyncTasks = new Stack<AsyncTask>();

    private DrawerLayout mDrawer;
    private FrameLayout mDrawerView;
    private RelativeLayout mDrawerBuySellLayout;
    private TextView mDrawerAccount;
    private ImageView mDrawerAccountArrow;
    private TextView mDrawerExchange;
    private TextView mDrawerBuySell;
    private TextView mDrawerImport;
    private TextView mDrawerSettings;
    private TextView mDrawerLogout;
    private ListView mOtherAccountsListView;
    private AccountsAdapter mOtherAccountsAdapter;
    private List<String> mOtherAccounts;

    private RememberPasswordCheck mPasswordCheck;

    private boolean activityInForeground = false;

    private FloatingActionButton mActionButton;
    private FloatingActionMenu mActionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = initiateCore(this);
        setContentView(R.layout.activity_navigation);


        Resources r = getResources();
        int menuPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
        int menuWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, r.getDisplayMetrics());
        FrameLayout.LayoutParams menuLayout = new FrameLayout.LayoutParams(menuWidth, menuWidth);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_menu_add_black);
        mActionButton = new FloatingActionButton.Builder(this)
                                                .setContentView(icon)
                                                .build();
        mActionButton.setVisibility(View.GONE);

        ImageView requestButton = new ImageView(this);
        ImageView sendButton = new ImageView(this);
        ImageView txButton = new ImageView(this);
        requestButton.setImageResource(R.drawable.ic_request);
        requestButton.setPadding(menuPadding, menuPadding, menuPadding, menuPadding);
        sendButton.setImageResource(R.drawable.ic_send);
        sendButton.setPadding(menuPadding, menuPadding, menuPadding, menuPadding);
        txButton.setImageResource(R.drawable.ico_nav_wallets_selected);
        txButton.setPadding(menuPadding, menuPadding, menuPadding, menuPadding);

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        SubActionButton receiveAction = itemBuilder.setLayoutParams(menuLayout).setContentView(requestButton).build();
        SubActionButton sendAction = itemBuilder.setLayoutParams(menuLayout).setContentView(sendButton).build();
        SubActionButton txAction = itemBuilder.setLayoutParams(menuLayout).setContentView(txButton).build();

        mActionMenu =
            new FloatingActionMenu.Builder(this)
                                  .addSubActionView(receiveAction)
                                  .addSubActionView(sendAction)
                                  .addSubActionView(txAction)
                                  .attachTo(mActionButton)
                                  .build();

        receiveAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switchFragmentThread(Tabs.REQUEST.ordinal());
                mActionMenu.close(true);
            }
        });

        sendAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switchFragmentThread(Tabs.SEND.ordinal());
                mActionMenu.close(true);
            }
        });

        txAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switchFragmentThread(Tabs.WALLET.ordinal());
                mActionMenu.close(true);
            }
        });

        mFragmentContainer = findViewById(R.id.fragment_container);
        mFragmentLayout = (LinearLayout) findViewById(R.id.activityLayout);
        mNumberpadView = (Numberpad) findViewById(R.id.navigation_numberpad_layout);

        Common.addStatusBarPadding(this, mFragmentContainer);

        setTypeFaces();

        for (int i = 0; i < mNavFragments.length; i++) {
            mNavStacks[i] = new Stack<Fragment>();
            mNavStacks[i].push(mNavFragments[i]);
        }

        // for keyboard hide and show
        final View activityRootView = findViewById(R.id.activity_navigation_root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean mLastKeyBoardUp = false;
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    keyBoardUp = true;
                    if(keyBoardUp != mLastKeyBoardUp) {
                        hideNavBar();
                    }
                } else {
                    keyBoardUp = false;
                    if(keyBoardUp != mLastKeyBoardUp) {
                        if (AirbitzApplication.isLoggedIn()) {
                            showNavBar();
                        }
                        else {
                            if(mNavStacks[mNavThreadId].peek() instanceof BusinessDirectoryFragment ||
                                    mNavStacks[mNavThreadId].peek() instanceof MapBusinessDirectoryFragment ||
                                    mNavStacks[mNavThreadId].peek() instanceof DirectoryDetailFragment ) {
                                Log.d(TAG, "Keyboard down, not logged in, in directory");
                                showNavBar();
                            }
                        }
                    }
                }
                mLastKeyBoardUp = keyBoardUp;
            }
        });

        // Setup top screen - the Landing - that swipes away if no login
        mViewPager = (ViewPager) findViewById(R.id.navigation_view_pager);
        mViewPager.setVisibility(View.GONE);
        setViewPager();

        // Navigation Drawer slideout
        setupDrawer();
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment frag = mNavStacks[mNavThreadId].peek();
        if (frag != null) {
            frag.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setCoreListeners(NavigationActivity activity) {
        mCoreAPI.setOnOTPErrorListener(activity);
        mCoreAPI.setOTPResetRequestListener(activity);
        mCoreAPI.setOnIncomingBitcoinListener(activity);
        mCoreAPI.setOnDataSyncListener(activity);
        mCoreAPI.setOnBlockHeightChangeListener(activity);
        mCoreAPI.setOnOnRemotePasswordChangeListener(activity);
    }

    public static CoreAPI initiateCore(Context context) {
        CoreAPI api = CoreAPI.getApi(context);
        String seed = CoreAPI.getSeedData();
        api.Initialize(context, seed, seed.length());
        return api;
    }

    public void DisplayLoginOverlay(boolean overlay) {
        DisplayLoginOverlay(overlay, false);
    }

    public void DisplayLoginOverlay(boolean overlay, boolean animate) {
        setViewPager();
        if (overlay) {
            mViewPager.setCurrentItem(1, false);
            if (animate) {
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(250);
                mViewPager.startAnimation(anim);
            }
            mViewPager.setVisibility(View.VISIBLE);
            if(mOverlayFragments != null && mOverlayFragments.size()==2) {
                mOverlayFragments.get(1).setUserVisibleHint(true);
            }
        } else {
            mViewPager.setCurrentItem(0, animate);
            if(mOverlayFragments != null && mOverlayFragments.size()==2) {
                mOverlayFragments.get(1).setUserVisibleHint(false);
            }
            mViewPager.setVisibility(View.GONE);
        }
    }

    private void setViewPager() {
        mOverlayFragments.clear();
//        if(mOverlayFragments.size() == 0) {
            mOverlayFragments.add(new TransparentFragment());
            mOverlayFragments.add(new LandingFragment());
//        }

        NavigationAdapter pageAdapter = new NavigationAdapter(getFragmentManager(), mOverlayFragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Disappear if transparent page shows
                if ((position == 0) && positionOffsetPixels == 0) {
                    mViewPager.setVisibility(View.GONE);
                }
            }

            public void onPageSelected(int position) {
                // Disappear if transparent page shows
                Log.d(TAG, "page selected = " + position);
                if (position == 0) {
                    mViewPager.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setTypeFaces() {
        latoRegularTypeFace = Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace = Typeface.createFromAsset(getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getAssets(), "font/Lato-Regular.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getAssets(), "font/HelveticaNeue.ttf");
    }

    /*
        Implements interface to receive navigation changes from the bottom nav bar
     */
    public void onNavBarSelected(int position) {

        if (AirbitzApplication.isLoggedIn()) {
            hideSoftKeyboard(mFragmentLayout);
            if (position != mNavThreadId) {
                if(findViewById(R.id.modal_indefinite_progress).getVisibility() == View.VISIBLE) {
                    showModalProgress(false);
                }
                AirbitzApplication.setLastNavTab(position);
                if(position != Tabs.MORE.ordinal()) {
                    switchFragmentThread(position);
                }
                else {
                    openDrawer();
                }
            }
            else if(position == Tabs.MORE.ordinal()) {
                openDrawer();
            }
            else if(!isAtNavStackEntry()) {
                onBackPressed();
            }
        } else {
            if (position != Tabs.BD.ordinal()) {
                AirbitzApplication.setLastNavTab(position);
                DisplayLoginOverlay(true, true);
            }
        }
    }

    public void switchFragmentThread(int id) {
        if (mActionButton.getVisibility() != View.VISIBLE && AirbitzApplication.isLoggedIn()) {
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

    public void pushFragment(Fragment fragment, FragmentTransaction transaction) {
        mNavStacks[mNavThreadId].push(fragment);
        transaction.replace(R.id.activityLayout, fragment);
        transaction.commitAllowingStateLoss();
    }

    public void pushFragment(Fragment fragment) {
        pushFragment(fragment, mNavThreadId);
    }

    public void pushFragment(Fragment fragment, int threadID) {
        mNavStacks[threadID].push(fragment);

        // Only show visually if we're displaying the thread
        if (mNavThreadId == threadID) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (mNavStacks[threadID].size() != 0) {
                if (fragment instanceof HelpFragment) {
                    transaction.setCustomAnimations(R.animator.fade_in, 0);
                } else {
                    transaction.setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_out_left);
                }
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

    public void popFragment(FragmentTransaction transaction) {
        hideSoftKeyboard(mFragmentLayout);
        Fragment fragment = mNavStacks[mNavThreadId].pop();
        getFragmentManager().executePendingTransactions();

        transaction.replace(R.id.activityLayout, mNavStacks[mNavThreadId].peek());
        transaction.commitAllowingStateLoss();
    }

    public void popFragment() {
        hideSoftKeyboard(mFragmentLayout);
        Fragment fragment = mNavStacks[mNavThreadId].pop();
        getFragmentManager().executePendingTransactions();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mNavStacks[mNavThreadId].size() != 0) {
            if (fragment instanceof HelpFragment) {
                transaction.setCustomAnimations(0, R.animator.fade_out);
            } else {
                transaction.setCustomAnimations(R.animator.slide_in_from_left, R.animator.slide_out_right);
            }
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

    public void hideNavBar() {
        if (mActionButton.getVisibility() == View.VISIBLE) {
            mActionButton.setVisibility(View.GONE);
        }
    }

    public void showNavBar() {
        if (mActionButton.getVisibility() == View.GONE) {
            mHandler.postDelayed(delayedShowNavBar, 50);
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

        showModalProgress(false);

        if (isAtNavStackEntry()) {
                ShowExitMessageDialog("", getString(R.string.string_exit_app_question));
        } else {
            popFragment();
        }

        showNavBar();
    }

    public boolean isAtNavStackEntry() {
        return mNavStacks[mNavThreadId].size() == 1;
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

        //Look for Connection change events
        registerReceiver(ConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        mNavThreadId = AirbitzApplication.getLastNavTab();

        if (loginExpired() || !AirbitzApplication.isLoggedIn()) {
            DisplayLoginOverlay(true);
            mNavThreadId = Tabs.BD.ordinal();
        } else {
            DisplayLoginOverlay(false);
            mCoreAPI.restoreConnectivity();
        }
        switchFragmentThread(mNavThreadId);

        AirbitzAlertReceiver.CancelNextAlertAlarm(this, AirbitzAlertReceiver.ALERT_NOTIFICATION_CODE);
        AirbitzAlertReceiver.CancelNextAlertAlarm(this, AirbitzAlertReceiver.ALERT_NEW_BUSINESS_CODE);

        checkNotifications();

        if (SettingFragment.getNFCPref()) {
            setupNFCForegrounding();
        }

        Intent intent = getIntent();
        if(intent != null) {
            Uri data = intent.getData();
            if(data != null) {
                processUri(data);
            }
        }

        if (null != mPasswordCheck) {
            mPasswordCheck.onResume();
        }

        mCoreAPI.addExchangeRateChangeListener(this);

        setCoreListeners(this);

        activityInForeground = true;

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        setCoreListeners(null);

        activityInForeground = false;
        unregisterReceiver(ConnectivityChangeReceiver);
        mCoreAPI.lostConnectivity();
        AirbitzApplication.setBackgroundedTime(System.currentTimeMillis());
        AirbitzAlertReceiver.SetAllRepeatingAlerts(this);
        if(SettingFragment.getNFCPref()) {
            disableNFCForegrounding();
        }
        if (null != mPasswordCheck) {
            mPasswordCheck.onPause();
        }
        mCoreAPI.removeExchangeRateChangeListener(this);
        mOTPResetRequestDialog = null; // To allow the message again if foregrounding
    }

    /*
     * this only gets called from sent funds, or a request comes through
     */
    public void switchToWallets(Bundle bundle) {
        Fragment frag = new TransactionListFragment();
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

    //********************** NFC support
    @Override
    protected void onNewIntent(final Intent intent)
    {
        final String action = intent.getAction();
        final Uri intentUri = intent.getData();
        final String type = intent.getType();
        final String scheme = intentUri != null ? intentUri.getScheme() : null;

        Log.d(TAG, "New Intent action=" + action + ", data=" + intentUri + ", type=" + type + ", scheme=" + scheme);

        if (intentUri != null && action != null && (Intent.ACTION_VIEW.equals(action) ||
                (SettingFragment.getNFCPref() && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)))) {
            processUri(intentUri);
        } else if(type != null && type.equals(AirbitzAlertReceiver.ALERT_NOTIFICATION_TYPE)) {
            Log.d(TAG, "Notification type found");
                mNotificationTask = new NotificationTask();
                mNotificationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void setupNFCForegrounding() {
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addDataScheme("bitcoin");
        IntentFilter[] filters = new IntentFilter[] { ndef };

        final NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter nfcAdapter = nfcManager.getDefaultAdapter();

        if (nfcAdapter != null && nfcAdapter.isEnabled() && SettingFragment.getNFCPref()) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
        }
    }

    public void disableNFCForegrounding() {
        final NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter nfcAdapter = nfcManager.getDefaultAdapter();

        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void processUri(Uri uri) {
        if(uri == null || uri.getScheme() == null) {
            Log.d(TAG, "Null uri or uri.scheme");
            return;
        }

        if (!AirbitzApplication.isLoggedIn()) {
            mDataUri = uri;
            return;
        }

        String scheme =uri.getScheme();
        if ("airbitz".equals(scheme) && "plugin".equals(uri.getHost())) {
            List<String> path = uri.getPathSegments();
            if (2 == path.size()) {
                launchBuySell(path.get(1), path.get(0));
            }
        } else if ("bitcoin".equals(scheme) || "airbitz".equals(scheme)) {
            handleBitcoinUri(uri);
        }
        else if("bitcoin-ret".equals(scheme) || "x-callback-url".equals(scheme)) {
            handleRequestForPaymentUri(uri);
        }
        else if (ImportFragment.getHiddenBitsToken(uri.toString()) != null) {
            gotoImportNow(uri);
        }
    }

    /*
     * Handle bitcoin-ret or x-callback-url Uri's coming from OS
     */
    private void handleRequestForPaymentUri(Uri uri) {
        AddressRequestFragment fragment = new AddressRequestFragment();
        fragment.setOnAddressRequestListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(AddressRequestFragment.URI, uri.toString());
        fragment.setArguments(bundle);
        pushFragment(fragment);
    }

    @Override
    public void onAddressRequest() {
        popFragment();
        mDataUri = null;
    }

    private void launchBuySell(String country, String provider) {
        BuySellFragment buySell = null;
        if (!(mNavStacks[Tabs.MORE.ordinal()].get(0) instanceof BuySellFragment)) {
            mNavStacks[Tabs.MORE.ordinal()].clear();

            buySell = new BuySellFragment();
            pushFragmentNoAnimation(buySell, NavigationActivity.Tabs.MORE.ordinal());
        } else {
            buySell = (BuySellFragment) mNavStacks[Tabs.MORE.ordinal()].get(0);
        }
        switchFragmentThread(Tabs.MORE.ordinal());
        mDrawer.closeDrawer(mDrawerView);

        buySell.launchPluginByCountry(country, provider);
    }

    /*
     * Handle bitcoin:<address> Uri's coming from OS
     */
    private void handleBitcoinUri(Uri dataUri) {
        Log.d(TAG, "Received onBitcoin with uri = " + dataUri.toString());

        resetFragmentThreadToBaseFragment(Tabs.SEND.ordinal());

        if (mNavThreadId != Tabs.SEND.ordinal()) {
            Bundle bundle = new Bundle();
            bundle.putString(WalletsFragment.FROM_SOURCE, URI_SOURCE);
            bundle.putString(URI_DATA, dataUri.toString());
            switchFragmentThread(Tabs.SEND.ordinal(), bundle);
        } else {
            CoreAPI.SpendTarget target = mCoreAPI.getNewSpendTarget();
            switchFragmentThread(Tabs.SEND.ordinal());
            SendConfirmationFragment fragment = new SendConfirmationFragment();
            fragment.setSpendTarget(target);
            Bundle bundle = new Bundle();
            bundle.putString(SendFragment.FROM_WALLET_UUID, mCoreAPI.getCoreWallets(false).get(0).getUUID());
            fragment.setArguments(bundle);
        }
    }

    @Override
    public void onIncomingBitcoin(String walletUUID, String txId) {
        Log.d(TAG, "onIncomingBitcoin uuid, txid = " + walletUUID + ", " + txId);
        mUUID = walletUUID;
        mTxId = txId;

        // If in merchant donation mode, stay on QR screen and show amount
        RequestFragment fragment = requestMatchesDonation();
        if(fragment != null) {
            showIncomingDialog(walletUUID, txId, false);
            return;
        }

        /* If showing QR code, launch receiving screen*/
        RequestFragment f = requestMatchesQR(mUUID, mTxId);
        Log.d(TAG, "RequestFragment? " + f);
        if (f != null) {
            long diff = f.requestDifference(mUUID, mTxId);
            if (diff <= 0) {
                // sender paid exact amount
                AudioPlayer.play(this, R.raw.bitcoin_received);
                handleReceiveFromQR();
            }
            else {
                // Request the remainder of the funds
                f.updateWithAmount(diff);
                AudioPlayer.play(this, R.raw.bitcoin_received_partial);
            }
        } else {
            Transaction tx = mCoreAPI.getTransaction(walletUUID, txId);
            if (tx.getAmountSatoshi() > 0) {
                AudioPlayer.play(this, R.raw.bitcoin_received);
                showIncomingBitcoinDialog();
                updateWalletListener();
            }
        }
    }

    private void handleReceiveFromQR() {
        if (!SettingFragment.getMerchantModePref()) {
            Bundle bundle = new Bundle();
            bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_REQUEST);
            bundle.putString(Transaction.TXID, mTxId);
            bundle.putString(Wallet.WALLET_UUID, mUUID);
            switchToWallets(bundle);
            resetFragmentThreadToBaseFragment(Tabs.REQUEST.ordinal());
        }
        else {
            hideSoftKeyboard(mFragmentLayout);
            Bundle bundle = new Bundle();
            bundle.putString(RequestFragment.MERCHANT_MODE, "merchant");
            resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.REQUEST.ordinal());
            switchFragmentThread(NavigationActivity.Tabs.REQUEST.ordinal(), bundle);
        }
        showIncomingDialog(mUUID, mTxId, true);
    }

    private void showIncomingDialog(String uuid, String txId, boolean withTeaching) {
        Wallet wallet = mCoreAPI.getWalletFromUUID(uuid);
        Transaction transaction = mCoreAPI.getTransaction(uuid, txId);
        String coinValue = mCoreAPI.formatSatoshi(transaction.getAmountSatoshi(), true);
        String currencyValue = null;
        // If no value set, then calculate it
        if (transaction.getAmountFiat() == 0.0) {
            currencyValue = mCoreAPI.FormatCurrency(transaction.getAmountSatoshi(), wallet.getCurrencyNum(),
                    false, true);
        } else {
            currencyValue = mCoreAPI.formatCurrency(transaction.getAmountFiat(),
                    wallet.getCurrencyNum(), true);
        }
        String message = String.format(getString(R.string.received_bitcoin_fading_message), coinValue, currencyValue);
        int delay = 4000;
        if(withTeaching) {
            SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
            int count = prefs.getInt(INCOMING_COUNT, 1);
            if(count <= 2 && !SettingFragment.getMerchantModePref()) {
                count++;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(INCOMING_COUNT, count);
                editor.apply();
                message += " " + getString(R.string.received_bitcoin_fading_message_teaching);
                delay = 5000;
            }
        }
        ShowFadingDialog(message, delay);
    }

    private RequestFragment requestMatchesQR(String uuid, String txid) {
        Fragment f = mNavStacks[mNavThreadId].peek();
        if (!(f instanceof RequestFragment)) {
            return null;
        }
        RequestFragment qr = (RequestFragment) f;
        if (qr.isShowingQRCodeFor(uuid, txid)) {
            return qr;
        } else {
            return null;
        }
    }

    private RequestFragment requestMatchesDonation() {
        Fragment f = mNavStacks[mNavThreadId].peek();
        if (!(f instanceof RequestFragment)) {
            return null;
        }
        RequestFragment qr = (RequestFragment) f;
        if (qr.isMerchantDonation()) {
            return qr;
        } else {
            return null;
        }
    }

    public void onSentFunds(String walletUUID, String txId, String returnUrl) {
        Log.d(TAG, "onSentFunds uuid, txid = " + walletUUID + ", " + txId);

        FragmentManager manager = getFragmentManager();
        if(manager != null) {
            manager.executePendingTransactions();
        }

        Bundle bundle = new Bundle();
        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);
        bundle.putBoolean(WalletsFragment.CREATE, true);
        bundle.putString(Transaction.TXID, txId);
        bundle.putString(Wallet.WALLET_UUID, walletUUID);
        bundle.putString(SendFragment.RETURN_URL, returnUrl);

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

    private void gotoDetailsNow() {
        Bundle bundle = new Bundle();
        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_REQUEST);
        bundle.putString(Transaction.TXID, mTxId);
        bundle.putString(Wallet.WALLET_UUID, mUUID);
        switchToWallets(bundle);

        resetFragmentThreadToBaseFragment(Tabs.REQUEST.ordinal());
    }

    private void gotoImportNow(Uri uri) {
        resetFragmentThreadToBaseFragment(Tabs.REQUEST.ordinal());
        switchFragmentThread(Tabs.REQUEST.ordinal());
        Fragment fragment = new ImportFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ImportFragment.URI, uri.toString());
        fragment.setArguments(bundle);
        pushFragment(fragment, Tabs.REQUEST.ordinal());
    }

    public void resetFragmentThreadToBaseFragment(int threadId) {
        mNavStacks[threadId].clear();
        mNavStacks[threadId].add(getNewBaseFragement(threadId));
    }

    private void showIncomingBitcoinDialog() {
        if (!this.isFinishing()) {
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
    }

    private void showRemotePasswordChangeDialog() {
        if (!this.isFinishing()) {
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
    }

    public void UserJustLoggedIn(boolean passwordLogin) {
        showNavBar();
        checkDailyLimitPref();
        mCoreAPI.setupAccountSettings();
        mCoreAPI.startAllAsyncUpdates();
        if (mDataUri != null) {
            processUri(mDataUri);
            mDataUri = null;
        } else {
            resetFragmentThreadToBaseFragment(mNavThreadId);
            AirbitzApplication.setLastNavTab(Tabs.WALLET.ordinal());
            resetFragmentThreadToBaseFragment(Tabs.WALLET.ordinal());
            switchFragmentThread(Tabs.WALLET.ordinal());
        }
        checkFirstWalletSetup();
        if(!mCoreAPI.coreSettings().getBDisablePINLogin() && passwordLogin) {
            mCoreAPI.PinSetup();
        }
        DisplayLoginOverlay(false, true);

        boolean checkPassword = false;
        // if the user has a password, increment PIN login count
        if (mCoreAPI.PasswordExists()) {
            checkPassword = mCoreAPI.incrementPinCount();
        }

        if(!passwordLogin && !mCoreAPI.PasswordExists()) {
            showPasswordSetAlert();
        }
        else if (!passwordLogin && checkPassword) {
            mPasswordCheck = new RememberPasswordCheck(this);
            mPasswordCheck.showPasswordCheckAlert();
        }
        else {
            new UserReviewTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public class UserReviewTask extends AsyncTask<Void, Void, Boolean> {

        UserReviewTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            return UserReview.offerUserReview();
        }

        @Override
        protected void onPostExecute(final Boolean offerReview) {
            if(offerReview) {
                UserReview.ShowUserReviewDialog(NavigationActivity.this);
            }
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

    public void startSignUp(String userName) {
        hideSoftKeyboard(mFragmentLayout);
        hideNavBar();
        Bundle bundle = new Bundle();
        bundle.putString(SetupUsernameFragment.USERNAME, userName);
        Fragment frag = new SetupUsernameFragment();
        frag.setArguments(bundle);
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
        if(mNavStacks[mNavThreadId].size()>1) { // ensure onPause called
            Fragment fragment = mNavStacks[mNavThreadId].peek();
            getFragmentManager().executePendingTransactions();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.remove(fragment);
            transaction.commitAllowingStateLoss();
        }
        mHandler.postDelayed(mAttemptLogout, 100);
        DisplayLoginOverlay(true);

        resetApp();
        AirbitzApplication.Logout();
        mCoreAPI.logout();
        mActionButton.setVisibility(View.GONE);

        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    Runnable mAttemptLogout = new Runnable() {
        @Override
        public void run() {
            if(mAsyncTasks.isEmpty()) {
                startActivity(new Intent(NavigationActivity.this, NavigationActivity.class));
            }
            else {
                mHandler.postDelayed(this, 100);
            }
        }
    };

    private void resetApp() {
        for(int i=0; i<mNavFragments.length; i++) {
            resetFragmentThreadToBaseFragment(i);
        }
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
                return new TransactionListFragment();
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

    private boolean loginExpired() {
        if (AirbitzApplication.getmBackgroundedTime() == 0 || !AirbitzApplication.isLoggedIn())
            return true;

        long milliDelta = (System.currentTimeMillis() - AirbitzApplication.getmBackgroundedTime());

        Log.d(TAG, "delta logout time = " + milliDelta);
        if (milliDelta > mCoreAPI.coreSettings().getMinutesAutoLogout() * 60 * 1000) {
            Logout();
            return true;
        }
        return false;
    }

    @Override
    public void OnExchangeRatesChange() {
        mDrawerExchange.setText(mCoreAPI.BTCtoFiatConversion(mCoreAPI.coreSettings().getCurrencyNum()));
    }

    public enum Tabs {BD, REQUEST, SEND, WALLET, MORE}

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
        UserJustLoggedIn(password != null);
        setViewPager();
        mDrawerAccount.setText(username);
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
        if (!this.isFinishing()) {
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
    }

    public void ShowOkMessageDialog(String title, String message, int timeoutMillis) {
        mHandler.postDelayed(mMessageDialogKiller, timeoutMillis);
        ShowOkMessageDialog(title, message);
    }

    private AlertDialog mExitDialog;
    public void ShowExitMessageDialog(String title, String message) {
        if (!this.isFinishing() && mExitDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
            builder.setMessage(message)
                    .setTitle(title)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.string_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.string_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mExitDialog = null;
                                    dialog.cancel();
                                }
                            });
            mExitDialog = builder.create();
            mExitDialog.show();
        }
    }

    public void ShowMessageDialogBackPress(String title, String reason) {
        if (!this.isFinishing()) {
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
        ShowFadingDialog(message, 3000);
    }

    public void ShowFadingDialog(String message, int timeout) {
        ShowFadingDialog(message, timeout, true);
    }

    public void ShowFadingDialog(String message, int timeout, boolean cancelable) {
        ShowFadingDialog(message, null, timeout, cancelable);
    }

    private Dialog mFadingDialog = null;
    public void ShowFadingDialog(final String message, final String thumbnail, final int timeout, final boolean cancelable) {
        if (!this.isFinishing()) {
            NavigationActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (timeout == 0) {
                        mFadingDialog.dismiss();
                        return;
                    }
                    if (mFadingDialog != null) {
                        mFadingDialog.dismiss();
                    }
                    mFadingDialog = new Dialog(NavigationActivity.this);
                    mFadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    mFadingDialog.setCancelable(cancelable);
                    mFadingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    View view = NavigationActivity.this.getLayoutInflater().inflate(R.layout.fading_alert, null);
                    TextView tv = ((TextView) view.findViewById(R.id.fading_alert_text));
                    tv.setText(message);
                    tv.setTypeface(NavigationActivity.latoRegularTypeFace);
                    ProgressBar progress = ((ProgressBar) view.findViewById(R.id.fading_alert_progress));
                    if (!cancelable) {
                        progress.setVisibility(View.VISIBLE);
                    }
                    if (thumbnail != null) {
                        view.findViewById(R.id.fading_alert_image_layout).setVisibility(View.VISIBLE);
                        if (!thumbnail.isEmpty()) {
                            ((ImageView) view.findViewById(R.id.fading_alert_image)).setImageURI(Uri.parse(thumbnail));
                        }
                    }
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mFadingDialog != null) {
                                mFadingDialog.dismiss();
                            }
                        }
                    });
                    mFadingDialog.setContentView(view);
                    AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                    fadeOut.setStartOffset(timeout);
                    fadeOut.setDuration(2000);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mFadingDialog.dismiss();
                            updateFadingDialogFinished();
                        }

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });

                    view.setAnimation(fadeOut);
                    mFadingDialog.show();
                    view.startAnimation(fadeOut);
                }
            });
        }
    }

    public void showPrivateKeySweepTransaction(String txid, String uuid, long amount) {
        if (amount > 0 && !txid.isEmpty()) {
            onSentFunds(uuid, txid, "");
            ShowOkMessageDialog(getString(R.string.import_wallet_swept_funds_title),
                    getString(R.string.import_wallet_swept_funds_message));
        }
        else if (amount == 0) {
            ShowOkMessageDialog(getString(R.string.import_wallet_hidden_bits_error_title),
                    getString(R.string.import_wallet_hidden_bits_error_message));
        }

    }

    public void showHiddenBitsTransaction(String txid, String uuid, long amount,
                String message, String zeroMessage, String tweet) {
        if(txid != null) {
            onSentFunds(uuid, txid, "");
        }

        if (amount == 0 && !zeroMessage.isEmpty()) {
            ShowHiddenBitsTweet(getString(R.string.import_wallet_hidden_bits_claimed), zeroMessage, tweet);
        }
        else if (!message.isEmpty()) {
            ShowHiddenBitsTweet(getString(R.string.import_wallet_hidden_bits_not_claimed), message, tweet);
        }
    }

    public void ShowHiddenBitsTweet(String title, String reason, final String tweet) {
        if (!this.isFinishing()) {
            if (mMessageDialog != null) {
                mMessageDialog.dismiss();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
            builder.setMessage(reason)
                    .setTitle(title)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.string_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // invoke Twitter to send tweet
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("http://twitter.com/post?message=" + Uri.encode(tweet)));
                                    startActivity(i);
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.string_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
            mMessageDialog = builder.create();
            mMessageDialog.show();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(reason)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // invoke Twitter to send tweet
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("http://twitter.com/post?message=" + Uri.encode(tweet)));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.string_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        mMessageDialog = builder.create();
        mMessageDialog.show();
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

    //************** Notification support

    private void checkNotifications() {
        if(mNotificationTask == null) {
            mNotificationTask = new NotificationTask();
            mNotificationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private NotificationTask mNotificationTask;
    public class NotificationTask extends AsyncTask<Void, Void, String> {
        String mMessageId;
        String mBuildNumber;

        NotificationTask() { }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            AirbitzAPI api = AirbitzAPI.getApi();
            PackageInfo pInfo;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                mMessageId = String.valueOf(getMessageIDPref());
                mBuildNumber = String.valueOf(pInfo.versionCode);
                return api.getMessages(mMessageId, mBuildNumber);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(final String response) {
            Log.d(TAG, "Notification response of "+mMessageId+","+mBuildNumber+": " + response);
            if(response != null && response.length() != 0) {
                mNotificationMap = getAndroidMessages(response);
                if(mNotificationMap.size() > 0) {
                    showNotificationAlert();
                }
            }
            else {
                Log.d(TAG, "No Notification response");
            }
            mNotificationTask = null;
        }

        @Override
        protected void onCancelled() {
            mNotificationTask = null;
        }
    }

    public int getMessageIDPref() {
        SharedPreferences prefs = getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(LAST_MESSAGE_ID, 0); // default to Automatic
    }

    private void saveMessageIDPref(int id) {
        SharedPreferences.Editor editor = getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(LAST_MESSAGE_ID, id);
        editor.apply();
    }

    Map<Integer, AirbitzNotification> getAndroidMessages(String input) {
        Map<Integer, AirbitzNotification> map = new HashMap<Integer, AirbitzNotification>();
        try {
            JSONObject json = new JSONObject(input);
            int count = json.getInt("count");
            if(count > 0) {
                JSONArray notifications = json.getJSONArray("results");
                for(int i=0; i<count; i++) {
                    JSONObject notification = notifications.getJSONObject(i);

                    int id = Integer.valueOf(notification.getString("id"));
                    String title = notification.getString("title");
                    String message = notification.getString("message");

                    map.put(id, new AirbitzNotification(title, message));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    private Dialog mAlertNotificationDialog;
    private void showNotificationAlert() {
        if (!this.isFinishing()) {
            if (mNotificationMap == null || mNotificationMap.size() == 0)
                return;

            if (mAlertNotificationDialog != null)
                mAlertNotificationDialog.dismiss();

            StringBuilder s = new StringBuilder();
            int max = -1;
            for (Integer i : mNotificationMap.keySet()) {
                if (max < i) {
                    max = i;
                }
                String title = mNotificationMap.get(i).mTitle;
                String message = mNotificationMap.get(i).mMessage;

                s.append(title).append("\n");
                s.append(message).append("\n\n");
            }

            final int saveInt = max;

            mAlertNotificationDialog = new Dialog(this);
            mAlertNotificationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mAlertNotificationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            mAlertNotificationDialog.setContentView(R.layout.dialog_notification);
            mAlertNotificationDialog.setCancelable(false);
            WebView wv = (WebView) mAlertNotificationDialog.findViewById(R.id.dialog_notification_webview);
            wv.setVisibility(View.VISIBLE);
            wv.loadData(s.toString(), "text/html; charset=UTF-8", null);
            Button ok = (Button) mAlertNotificationDialog.findViewById(R.id.dialog_notification_ok_button);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAlertNotificationDialog.cancel();
                    saveMessageIDPref(saveInt);
                }
            });
            mAlertNotificationDialog.show();
        }
    }

    private Dialog mPasswordSetDialog;
    private void showPasswordSetAlert() {
            if (!NavigationActivity.this.isFinishing() && mPasswordSetDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(NavigationActivity.this, R.style.AlertDialogCustom));
                builder.setMessage(getString(R.string.password_set_message))
                        .setTitle(getString(R.string.password_set_title))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.password_set_skip),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        mPasswordSetDialog = null;
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.string_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        mPasswordSetDialog = null;
                                        launchChangePassword();
                                    }
                                });
                mPasswordSetDialog = builder.create();
                mPasswordSetDialog.show();
        }
    }

    private void launchChangePassword() {
        Bundle bundle = new Bundle();

        Fragment frag = new SettingFragment();
        bundle.putBoolean(SettingFragment.START_CHANGE_PASSWORD, true);
        frag.setArguments(bundle);
        mNavStacks[Tabs.MORE.ordinal()].clear();
        mNavStacks[Tabs.MORE.ordinal()].add(frag);
        switchFragmentThread(Tabs.WALLET.ordinal());
    }

    private void checkDailyLimitPref() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);

        // On first install/load, copy synchronized to local setting
        if(!prefs.contains(AirbitzApplication.DAILY_LIMIT_SETTING_PREF + AirbitzApplication.getUsername())) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(AirbitzApplication.DAILY_LIMIT_PREF + AirbitzApplication.getUsername(), mCoreAPI.GetDailySpendLimit());
            editor.putBoolean(AirbitzApplication.DAILY_LIMIT_SETTING_PREF + AirbitzApplication.getUsername(), mCoreAPI.GetDailySpendLimitSetting());
            editor.apply();
        }
    }

    //********************  OTP support
    @Override
    public void onOTPError(String secret) {
        if(secret != null) {
            mHandler.post(mShowOTPSkew);
        }
        else {
            mHandler.post(mShowOTPRequired);
        }
    }

    AlertDialog mOTPAlertDialog;
    final Runnable mShowOTPRequired = new Runnable() {
        @Override
        public void run() {
            if (!NavigationActivity.this.isFinishing() && mOTPAlertDialog == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(NavigationActivity.this, R.style.AlertDialogCustom));
                    builder.setMessage(getString(R.string.twofactor_required_message))
                            .setTitle(getString(R.string.twofactor_required_title))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.twofactor_enable),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            launchTwoFactorScan();
                                        }
                                    })
                            .setNegativeButton(getResources().getString(R.string.twofactor_remind_later),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            mOTPAlertDialog = null;
                                        }
                                    });
                    mOTPAlertDialog = builder.create();
                    mOTPAlertDialog.show();
            }
        }
    };

    final Runnable mShowOTPSkew = new Runnable() {
        @Override
        public void run() {
            if (!NavigationActivity.this.isFinishing() && mOTPAlertDialog == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(NavigationActivity.this, R.style.AlertDialogCustom));
                    builder.setMessage(getString(R.string.twofactor_invalid_message))
                            .setTitle(getString(R.string.twofactor_invalid_title))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.string_ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            mOTPAlertDialog = null;
                                        }
                                    });
                    mOTPAlertDialog = builder.create();
                    mOTPAlertDialog.show();
            }
        }
    };

    private void launchTwoFactorScan() {
        Fragment fragment = new TwoFactorScanFragment();
        ((TwoFactorScanFragment)fragment).setOnTwoFactorQRScanResult(this);
        Bundle bundle = new Bundle();
        bundle.putBoolean(TwoFactorScanFragment.STORE_SECRET, true);
        bundle.putBoolean(TwoFactorScanFragment.TEST_SECRET, true);
        bundle.putString(TwoFactorScanFragment.USERNAME, AirbitzApplication.getUsername());
        fragment.setArguments(bundle);
        pushFragment(fragment);
    }

    @Override
    public void onTwoFactorQRScanResult(boolean success, String result) {
        mOTPAlertDialog = null;
    }

    AlertDialog mOTPResetRequestDialog;
    @Override
    public void onOTPResetRequest() {
        if (!NavigationActivity.this.isFinishing() && mOTPResetRequestDialog == null) {
            String message = String.format(getString(R.string.twofactor_reset_message), AirbitzApplication.getUsername());
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(NavigationActivity.this, R.style.AlertDialogCustom));
            builder.setMessage(message)
                    .setTitle(getString(R.string.twofactor_reset_title))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.string_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
            mOTPResetRequestDialog = builder.create();
            mOTPResetRequestDialog.show();
        }
    }

    boolean mDrawerExchangeUpdated = false;
    // Navigation Drawer (right slideout)
    private void setupDrawer() {
        mDrawer = (DrawerLayout) findViewById(R.id.activityDrawer);
        mDrawerView = (FrameLayout) findViewById(R.id.activityDrawerView);
        Common.addStatusBarPadding(this, mDrawerView);

        mDrawerExchange = (TextView) findViewById(R.id.item_drawer_exchange_rate);
        mDrawerBuySellLayout = (RelativeLayout) findViewById(R.id.layout_drawer_bottom_buttons);
        mDrawerBuySell = (TextView) findViewById(R.id.item_drawer_buy_sell);
        mDrawerBuySell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(mNavStacks[Tabs.MORE.ordinal()].get(0) instanceof BuySellFragment)) {
                    mNavStacks[Tabs.MORE.ordinal()].clear();
                    pushFragment(new BuySellFragment(), Tabs.MORE.ordinal());
                }
                switchFragmentThread(Tabs.MORE.ordinal());
                mDrawer.closeDrawer(mDrawerView);
            }
        });

        mDrawerImport = (TextView) findViewById(R.id.item_drawer_import);
        mDrawerImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(mDrawerView);
                mDrawerSettings.performClick();
                pushFragment(new ImportFragment(), Tabs.MORE.ordinal());
            }
        });

        mDrawerLogout = (TextView) findViewById(R.id.item_drawer_logout);
        mDrawerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(mDrawerView);
                Logout();
            }
        });

        mDrawerSettings = (TextView) findViewById(R.id.item_drawer_settings);
        mDrawerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(mDrawerView);
                resetFragmentThreadToBaseFragment(Tabs.MORE.ordinal());
                switchFragmentThread(Tabs.MORE.ordinal());
            }
        });

        mDrawerAccount = (TextView) findViewById(R.id.item_drawer_account);
        mDrawerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(!otherAccounts(AirbitzApplication.getUsername()).isEmpty()) {
                if(mOtherAccountsListView.getVisibility() != View.VISIBLE) {
                    showOthersList(AirbitzApplication.getUsername(), true);
                }
                else {
                    showOthersList(AirbitzApplication.getUsername(), false);
                }
            }
            }
        });
        mDrawerAccountArrow = (ImageView) findViewById(R.id.item_drawer_account_arrow);

        mOtherAccounts = new ArrayList<String>();
        mOtherAccountsAdapter = new AccountsAdapter(this, mOtherAccounts, true);
        mOtherAccountsAdapter.setButtonTouchedListener(this); // for close account button
        mOtherAccountsListView = (ListView) findViewById(R.id.drawer_account_list);
        mOtherAccountsListView.setAdapter(mOtherAccountsAdapter);
        mOtherAccountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String username = mOtherAccounts.get(position);
                saveCachedLoginName(username);
                mDrawer.closeDrawer(mDrawerView);
                Logout();
            }
        });

        mDrawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                mOtherAccountsListView.setVisibility(View.GONE);
                mDrawerExchangeUpdated = false;
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if(!mDrawerExchangeUpdated) {
                    tABC_AccountSettings settings = mCoreAPI.coreSettings();
                    if (settings != null) {
                        mDrawerExchange.setText(mCoreAPI.BTCtoFiatConversion(settings.getCurrencyNum()));
                        mDrawerExchangeUpdated = true;
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }

    public void openDrawer() {
        mDrawerAccount.setText(AirbitzApplication.getUsername());
        showOthersList(AirbitzApplication.getUsername(), false);
        mDrawer.openDrawer(mDrawerView);
    }

    public void closeDrawer() {
        mDrawer.closeDrawer(mDrawerView);
    }

    public boolean isDrawerOpen() {
        return mDrawer.isDrawerOpen(mDrawerView);
    }

    private void showOthersList(String username, boolean show)
    {
        mOtherAccounts.clear();
        mOtherAccounts.addAll(otherAccounts(username));
        mOtherAccountsAdapter.notifyDataSetChanged();
        if(show && !mOtherAccounts.isEmpty()) {
            mOtherAccountsListView.setVisibility(View.VISIBLE);
            mDrawerBuySellLayout.setVisibility(View.GONE);
            mDrawerAccountArrow.animate()
                    .rotation(180)
                    .start();
        }
        else {
            mOtherAccountsListView.setVisibility(View.GONE);
            mDrawerBuySellLayout.setVisibility(View.VISIBLE);
            mDrawerAccountArrow.animate()
                    .rotation(0)
                    .start();
        }
    }

    private List<String> otherAccounts(String username) {
        List<String> accounts = mCoreAPI.listAccounts();
        List<String> others = new ArrayList<String>();
        for(int i=0; i< accounts.size(); i++) {
            if(!accounts.get(i).equals(username)) {
                others.add(accounts.get(i));
            }
        }
        return others;
    }

    private void saveCachedLoginName(String name) {
        SharedPreferences.Editor editor = getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putString(AirbitzApplication.LOGIN_NAME, name);
        editor.apply();
    }

    @Override
    public void onButtonTouched(final String account) {
        String message = String.format(getString(R.string.fragment_landing_account_delete_message), account);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(getString(R.string.fragment_landing_account_delete_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!mCoreAPI.deleteAccount(account)) {
                                    ShowFadingDialog("Account could not be deleted.");
                                }
                                showOthersList("", false);
                                showOthersList(AirbitzApplication.getUsername(), true);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.string_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showOthersList("", false);
                                dialog.dismiss();
                            }
                        });
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }
}
