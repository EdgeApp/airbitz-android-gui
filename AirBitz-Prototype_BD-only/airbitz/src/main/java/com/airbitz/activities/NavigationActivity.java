package com.airbitz.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.adapters.NavigationAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.fragments.CategoryFragment;
import com.airbitz.fragments.HelpDialog;
import com.airbitz.fragments.LandingFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.RequestFragment;
import com.airbitz.fragments.RequestQRCodeFragment;
import com.airbitz.fragments.SendFragment;
import com.airbitz.fragments.SettingFragment;
import com.airbitz.fragments.SignUpFragment;
import com.airbitz.fragments.SuccessFragment;
import com.airbitz.fragments.TransparentFragment;
import com.airbitz.fragments.WalletsFragment;
import com.airbitz.models.FragmentSourceEnum;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.AirbitzService;
import com.airbitz.objects.Calculator;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * The main Navigation activity holding fragments for anything controlled with
 * the custom Navigation Bar for Airbitz
 * Created by Thomas Baker on 4/22/14.
 */
public class NavigationActivity extends BaseActivity
        implements NavigationBarFragment.OnScreenSelectedListener,
        CoreAPI.OnIncomingBitcoin,
        CoreAPI.OnDataSync,
        CoreAPI.OnBlockHeightChange,
        CoreAPI.OnRemotePasswordChange {

    public static final String URI = "com.airbitz.navigation.uri";
    private final String TAG = getClass().getSimpleName();

    private CoreAPI mCoreAPI;
    private boolean bdonly = false;//TODO SWITCH BETWEEN BD-ONLY and WALLET
    private Uri mDataUri;

    private boolean keyBoardUp = false;

    public enum Tabs { BD, REQUEST, SEND, WALLET, SETTING }
    private NavigationBarFragment mNavBarFragment;
    private RelativeLayout mNavBarFragmentLayout;
    private Calculator mCalculatorView;
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

    public static Typeface montserratBoldTypeFace;
    public static Typeface montserratRegularTypeFace;
    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;
    public static Typeface helveticaNeueTypeFace;

    // For Fragments to implement if they need to customize on back presses
    public interface OnBackPress {
        public void onBackPress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        String seed = mCoreAPI.getSeedData();

        mCoreAPI.Initialize(this, seed, seed.length());

        mCoreAPI.setOnIncomingBitcoinListener(this);
        mCoreAPI.setOnDataSyncListener(this);
        mCoreAPI.setOnBlockHeightChangeListener(this);
        mCoreAPI.setOnOnRemotePasswordChangeListener(this);

        setContentView(R.layout.activity_navigation);
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_app));
        mNavBarFragmentLayout = (RelativeLayout) findViewById(R.id.navigationLayout);
        mFragmentLayout = (LinearLayout) findViewById(R.id.activityLayout);
        mCalculatorView = (Calculator) findViewById(R.id.navigation_calculator_layout);

        setTypeFaces();

        for(int i=0; i< mNavFragments.length; i++) {
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
                    hideNavBar();
                    keyBoardUp = true;
                    if(mNavStacks[mNavThreadId].peek() instanceof CategoryFragment){
                        ((CategoryFragment)mNavStacks[mNavThreadId].get(mNavStacks[mNavThreadId].size() - 1)).hideDoneCancel();
                    }
                } else {
                    if(keyBoardUp && AirbitzApplication.isLoggedIn()) {
                        showNavBar();
                    }
                    if(mNavStacks[mNavThreadId].peek() instanceof CategoryFragment && keyBoardUp){
                        ((CategoryFragment)mNavStacks[mNavThreadId].get(mNavStacks[mNavThreadId].size() - 1)).showDoneCancel();
                    }
                    keyBoardUp = false;
                }
            }
        });

        // Setup top screen - the Landing - that swipes away if no login
        mViewPager = (ViewPager) findViewById(R.id.navigation_view_pager);

        mOverlayFragments.add(new TransparentFragment());
        mOverlayFragments.add(new LandingFragment());
        mOverlayFragments.add(new TransparentFragment());

        setViewPager();

        mNavBarFragment = (NavigationBarFragment) getSupportFragmentManager().findFragmentById(R.id.navigationFragment);
        if(bdonly){
            Common.LogD(TAG, "BD ONLY");
            mNavBarFragmentLayout.setVisibility(View.GONE);
            mNavBarFragment.hideNavBarFragment();
            mNavBarFragmentLayout.invalidate();
            RelativeLayout.LayoutParams lLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            mFragmentLayout.setLayoutParams(lLP);
        }

        if(AirbitzApplication.AUTOLOGIN) {
            attemptLogin(AirbitzApplication.autologinName, AirbitzApplication.autologinPassword);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Uri dataUri = getIntent().getData();
        if(dataUri != null && dataUri.getScheme().equals("bitcoin")) {
            onBitcoinUri(dataUri);
        }
    }

    public void DisplayLoginOverlay(boolean overlay) {
        setViewPager();
        if(overlay) {
            mViewPager.setVisibility(View.VISIBLE);
            mViewPager.setCurrentItem(1);
        } else {
            mViewPager.setVisibility(View.GONE);
        }
    }

    private void setViewPager() {
        NavigationAdapter pageAdapter = new NavigationAdapter(getSupportFragmentManager(), mOverlayFragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) { }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Disappear if transparent page shows
                if (position == 0 || position == 2) {
                    mViewPager.setVisibility(View.GONE);
                }
            }

            public void onPageSelected(int position) {
                // Disappear if transparent page shows
                if (position == 0 || position == 2) {
                    mViewPager.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setTypeFaces() {
        montserratBoldTypeFace=Typeface.createFromAsset(getAssets(), "font/Montserrat-Bold.ttf");
        montserratRegularTypeFace=Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace=Typeface.createFromAsset(getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace=Typeface.createFromAsset(getAssets(), "font/Lato-Regular.ttf");
        helveticaNeueTypeFace=Typeface.createFromAsset(getAssets(), "font/HelveticaNeue.ttf");
    }

    /*
        Implements interface to receive navigation changes from the bottom nav bar
     */
    public void onNavBarSelected(int position) {
        if(AirbitzApplication.isLoggedIn()) {
            hideSoftKeyboard(mFragmentLayout);
            switchFragmentThread(position);
        } else {
            if (position != Tabs.BD.ordinal()) {
                AirbitzApplication.setLastNavTab(position);
                DisplayLoginOverlay(true);
            }
        }
    }

    public void switchFragmentThread(int id) {
        Fragment frag = mNavStacks[id].peek();
        if(frag.isAdded()) {
            Common.LogD("NavFrag", "Fragment already added");
            return;
        }
        mNavBarFragment.unselectTab(mNavThreadId);
        mNavBarFragment.unselectTab(id); // just needed for resetting mLastTab
        mNavBarFragment.selectTab(id);
        mNavThreadId = id;
        AirbitzApplication.setLastNavTab(id);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityLayout, mNavStacks[id].peek()).commitAllowingStateLoss();
    }

    public void switchFragmentThread(int id, Bundle bundle) {
        if(bundle!=null)
            mNavFragments[id].setArguments(bundle);
        switchFragmentThread(id);
    }

    public void pushFragment(Fragment fragment, int threadID) {
        mNavStacks[threadID].push(fragment);

        // Only show visually if we're displaying the thread
        if(mNavThreadId==threadID) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (mNavStacks[threadID].size() != 0 && !(fragment instanceof HelpDialog)) {
                transaction.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_left, R.anim.slide_in_from_right, R.anim.slide_out_left);
            }
            transaction.replace(R.id.activityLayout, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    public void pushFragmentNoAnimation(Fragment fragment, int threadID) {
        mNavStacks[threadID].push(fragment);

        // Only show visually if we're displaying the thread
        if(mNavThreadId==threadID) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.activityLayout, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    public void popFragment() {
        Fragment fragment = mNavStacks[mNavThreadId].pop();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(mNavStacks[mNavThreadId].size()!=0 && !(fragment instanceof HelpDialog)) {
                transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_right, R.anim.slide_in_from_left, R.anim.slide_out_right);
        }
        transaction.replace(R.id.activityLayout, mNavStacks[mNavThreadId].peek());
        transaction.commitAllowingStateLoss();
    }

    public void hideNavBar() {
        mNavBarFragmentLayout.setVisibility(View.GONE);
        mNavBarFragment.hideNavBarFragment();
        mNavBarFragmentLayout.invalidate();
        RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mFragmentLayout.setLayoutParams(rLP);
        mFragmentLayout.invalidate();
    }

    public void showNavBar() {
        if(!bdonly) {
            mNavBarFragmentLayout.setVisibility(View.VISIBLE);
            mNavBarFragment.showNavBarFragment();
            mNavBarFragmentLayout.invalidate();
            RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rLP.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.nav_bar_height));
            mFragmentLayout.setLayoutParams(rLP);
            mFragmentLayout.invalidate();
        }
    }

    public Calculator getCalculatorView() {
        return mCalculatorView;
    }

    public void hideCalculator() {
        if(mCalculatorView.getVisibility()==View.VISIBLE) {
            mCalculatorView.setVisibility(View.GONE);
            mCalculatorView.setEnabled(false);
            showNavBar();
        }
    }

    public void showCalculator() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mFragmentLayout.getWindowToken(), 0);

        if(mCalculatorView.getVisibility()!=View.VISIBLE) {
            hideNavBar();
            mCalculatorView.setVisibility(View.VISIBLE);
            mCalculatorView.setEnabled(true);
        }
    }

    public void onCalculatorButtonClick(View v) {
        mCalculatorView.onButtonClick(v);
        if(v.getTag().toString().equals("done")) {
            hideCalculator();
        }
    }

    @Override
    public void onBackPressed() {
        // If fragments want the back key, they can have it
        Fragment fragment = mNavStacks[mNavThreadId].peek();
        if(fragment instanceof OnBackPress) {
            ((OnBackPress) fragment).onBackPress();
            return;
        }


        boolean calcVisible = (mCalculatorView.getVisibility() == View.VISIBLE);

        hideCalculator();

        if (mNavStacks[mNavThreadId].size() == 1) {
            if(!calcVisible) {
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
        super.onResume();

        registerServiceReceiver();
        startAirbitzService();

        mNavThreadId = AirbitzApplication.getLastNavTab();

        if(!AirbitzApplication.isLoggedIn()) {
            if(mDataUri!=null)
                DisplayLoginOverlay(true);

            mNavThreadId = Tabs.BD.ordinal();
            askCredentialsFromService(); // if service is running, it has the credentials probably
        } else {
            DisplayLoginOverlay(false);
            mCoreAPI.startAllAsyncUpdates();
            mCoreAPI.startWatchers();
        }
        switchFragmentThread(mNavThreadId);
    }

    @Override public void onPause() {
        super.onPause();
        unregisterReceiver(AirbitzServiceReceiver);
        mCoreAPI.stopAllAsyncUpdates();
    }

    public void switchToWallets(FragmentSourceEnum fragmentSourceEnum, Bundle bundle){
        if(fragmentSourceEnum == FragmentSourceEnum.REQUEST){
            switchFragmentThread(Tabs.WALLET.ordinal());
            mNavThreadId = Tabs.WALLET.ordinal();
            while (!mNavStacks[mNavThreadId].isEmpty()){
                mNavStacks[mNavThreadId].pop();
            }
            Fragment frag = new WalletsFragment();
            bundle.putString(WalletsFragment.FROM_SOURCE, "REQUEST");
            bundle.putBoolean(WalletsFragment.CREATE, true);
            frag.setArguments(bundle);
            pushFragment(frag, Tabs.WALLET.ordinal());
        }else if(fragmentSourceEnum == FragmentSourceEnum.SEND){
            switchFragmentThread(Tabs.WALLET.ordinal());
            mNavThreadId = Tabs.WALLET.ordinal();
            while (!mNavStacks[mNavThreadId].isEmpty()){
                mNavStacks[mNavThreadId].pop();
            }
            Fragment frag = new WalletsFragment();
            bundle.putString(WalletsFragment.FROM_SOURCE, "SEND");
            bundle.putBoolean(WalletsFragment.CREATE, true);
            frag.setArguments(bundle);
            pushFragment(frag, Tabs.WALLET.ordinal());
        }
    }

    /*
     * Handle bitcoin:<address> Uri's coming from OS
     */
    private void onBitcoinUri(Uri dataUri) {
        Common.LogD(TAG, "Received onCreate intent = "+dataUri.toString());
        if(!AirbitzApplication.isLoggedIn()) {
            mDataUri = dataUri;
            return;
        }
        resetFragmentThreadToBaseFragment(Tabs.SEND.ordinal());

        if(mNavThreadId !=Tabs.SEND.ordinal()) {
            Bundle bundle = new Bundle();
            bundle.putString(WalletsFragment.FROM_SOURCE, "URI");
            bundle.putString(URI, dataUri.toString());
            switchFragmentThread(Tabs.SEND.ordinal(), bundle);
        } else {
            CoreAPI.BitcoinURIInfo info = mCoreAPI.CheckURIResults(dataUri.toString());
            if(info!=null && info.address!=null) {
                ((SendFragment) mNavFragments[Tabs.SEND.ordinal()]).GotoSendConfirmation(info.address, info.amountSatoshi, info.label, false);
            }
        }
    }


    String mUUID, mTxId;
    @Override
    public void onIncomingBitcoin(String walletUUID, String txId) {

        mUUID = walletUUID;
        mTxId = txId;
        /* If showing QR code, launch receiving screen*/
        Fragment f = mNavStacks[mNavThreadId].peek();
        if( f instanceof RequestQRCodeFragment) {
            startReceivedSuccess();
        } else {
            showIncomingBitcoinDialog();
        }
    }

    // Callback interface when a wallet could be updated
    private OnWalletUpdated mOnWalletUpdated;
    public interface OnWalletUpdated {
        public void onWalletUpdated();
    }
    public void setOnWalletUpdated(OnWalletUpdated listener) {
        mOnWalletUpdated = listener;
    }

    private void updateWalletListener() {
        if(mOnWalletUpdated!=null)
            mOnWalletUpdated.onWalletUpdated();
    }

    @Override
    public void OnDataSync() {
        Common.LogD("NavigationActivity", "Data Sync received");
        updateWalletListener();
        mCoreAPI.startWatchers();
    }

    @Override
    public void onBlockHeightChange() {
        Common.LogD("NavigationActivity", "Block Height received");
    }

    @Override
    public void OnRemotePasswordChange() {
        Common.LogD("NavigationActivity", "Remote Password received");
        showRemotePasswordChangeDialog();
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
        bundle.putString(WalletsFragment.FROM_SOURCE,"REQUEST");
        bundle.putString(Transaction.TXID, mTxId);
        bundle.putString(Wallet.WALLET_UUID, mUUID);
        switchToWallets(FragmentSourceEnum.REQUEST, bundle);

        resetFragmentThreadToBaseFragment(Tabs.REQUEST.ordinal());
    }

    public void resetFragmentThreadToBaseFragment(int threadId) {
        while(mNavStacks[threadId].size() > 1) {
            mNavStacks[threadId].pop();
        }
    }

    private AlertDialog mIncomingDialog;
    Handler mHandler = new Handler();
    final Runnable dialogKiller = new Runnable() {
        @Override
        public void run() {
            if(mIncomingDialog!=null) {
                updateWalletListener();
                mIncomingDialog.dismiss(); // hide dialog
            }
        }
    };

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
                        })
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
        DisplayLoginOverlay(false);
        showNavBar();
        if(mDataUri!=null) {
            onBitcoinUri(mDataUri);
            mDataUri = null;
        } else {
            switchFragmentThread(AirbitzApplication.getLastNavTab());
        }

        mCoreAPI.startAllAsyncUpdates();
        mCoreAPI.startWatchers();
        sendCredentialsToService(AirbitzApplication.getUsername(), AirbitzApplication.getPassword());
    }

    public void startSignUp() {
        hideNavBar();
        Fragment frag = new SignUpFragment();
        pushFragmentNoAnimation(frag, mNavThreadId);
        DisplayLoginOverlay(false);
    }

    public void Logout() {
        sendCredentialsToService(null, null);
        AirbitzApplication.Logout();
        DisplayLoginOverlay(false);
        startActivity(new Intent(this, NavigationActivity.class));
    }

    public void attemptLogin(String username, String password) {
        mUserLoginTask = new UserLoginTask();
        mUserLoginTask.execute(username, password);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private UserLoginTask mUserLoginTask;
    public class UserLoginTask extends AsyncTask {
        String mUsername, mPassword;

        @Override
        protected void onPreExecute() {
            showModalProgress(true);
        }
        @Override
        protected Boolean doInBackground(Object... params) {
            mUsername = (String) params[0];
            mPassword = (String) params[1];
            return mCoreAPI.SignIn(mUsername, mPassword);
        }

        @Override
        protected void onPostExecute(final Object success) {
            showModalProgress(false);
            mUserLoginTask = null;

            if ((Boolean) success){
                AirbitzApplication.Login(mUsername, mPassword);
                UserJustLoggedIn();
            } else {
                ShowOkMessageDialog(getResources().getString(R.string.activity_navigation_signin_failed), getResources().getString(R.string.error_invalid_credentials));
            }
        }

        @Override
        protected void onCancelled() {
            mUserLoginTask = null;
            ShowOkMessageDialog(getResources().getString(R.string.activity_navigation_signin_failed), getResources().getString(R.string.activity_navigation_signin_failed_unexpected));
        }
    }

        //************************** Service support

    private void sendCredentialsToService(String username, String password) {
        final Intent intent = new Intent(AirbitzService.SET_CREDENTIALS);
        intent.putExtra(AirbitzService.SERVICE_USERNAME, username);
        intent.putExtra(AirbitzService.SERVICE_PASSWORD, password);
        sendBroadcast(intent);
        Common.LogD(TAG, "Sending credentials");
    }

    private void askCredentialsFromService() {
        Intent appIntent = new Intent(AirbitzService.ASK_CREDENTIALS);
        sendBroadcast(appIntent);
        Common.LogD(TAG, "Asking for credentials");
    }

    private void registerServiceReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AirbitzService.REPLY_CREDENTIALS);
        registerReceiver(AirbitzServiceReceiver, filter);
    }

    private void startAirbitzService() {
        Intent i= new Intent(this, AirbitzService.class);
        startService(i);
    }

    // For receiving Service queries
    private BroadcastReceiver AirbitzServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Common.LogD(TAG, "Broadcast received: " + intent.getAction());
            if (intent.getAction().equals(AirbitzService.REPLY_CREDENTIALS)) {
                String username = intent.getStringExtra(AirbitzService.SERVICE_USERNAME);
                String password = intent.getStringExtra(AirbitzService.SERVICE_PASSWORD);
                Common.LogD(TAG, "Credentials received, logging in: "+username+", "+password);
                attemptLogin(username, password);
            }
        }
    };

}
