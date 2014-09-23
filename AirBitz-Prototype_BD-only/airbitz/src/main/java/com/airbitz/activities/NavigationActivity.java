package com.airbitz.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.ContextThemeWrapper;
import android.view.Display;
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
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.LandingFragment;
import com.airbitz.fragments.NavigationBarFragment;
import com.airbitz.fragments.PasswordRecoveryFragment;
import com.airbitz.fragments.RequestFragment;
import com.airbitz.fragments.RequestQRCodeFragment;
import com.airbitz.fragments.SendFragment;
import com.airbitz.fragments.SettingFragment;
import com.airbitz.fragments.SignUpFragment;
import com.airbitz.fragments.SuccessFragment;
import com.airbitz.fragments.TransparentFragment;
import com.airbitz.fragments.WalletsFragment;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.Calculator;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;


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

    private int mFragmentViewHeight = 0;
    private int mNavBarViewHeight = 0;


    //******************* HockeyApp support - COMMENT OUT FOR PRODUCTION!!!

    private void checkForCrashes() {
        CrashManager.register(this, "***REMOVED***");
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, "***REMOVED***");
    }
    //******************* end HockeyApp support




    // For Fragments to implement if they need to customize on back presses
    public interface OnBackPress {
        public boolean onBackPress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
                    keyBoardUp = true;
                    hideNavBar();
                    if(mNavStacks[mNavThreadId].peek() instanceof CategoryFragment){
                        ((CategoryFragment)mNavStacks[mNavThreadId].get(mNavStacks[mNavThreadId].size() - 1)).hideDoneCancel();
                    }
                } else {
                    keyBoardUp = false;
                    if(AirbitzApplication.isLoggedIn() ) {
                        showNavBar();
                    }
                    if(mNavStacks[mNavThreadId].peek() instanceof CategoryFragment){
                        ((CategoryFragment)mNavStacks[mNavThreadId].get(mNavStacks[mNavThreadId].size() - 1)).showDoneCancel();
                    }
                }
            }
        });

        // Setup top screen - the Landing - that swipes away if no login
        setViewPager();

        mNavBarFragment = (NavigationBarFragment) getFragmentManager().findFragmentById(R.id.navigationFragment);
        if(bdonly){
            Common.LogD(TAG, "BD ONLY");
            mNavBarFragmentLayout.setVisibility(View.GONE);
//            mNavBarFragment.hideNavBarFragment();
            mNavBarFragmentLayout.invalidate();
            RelativeLayout.LayoutParams lLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            mFragmentLayout.setLayoutParams(lLP);
        }

        if(AirbitzApplication.AUTOLOGIN) {
            attemptLogin(AirbitzApplication.autologinName, AirbitzApplication.autologinPassword);
        }
    }

    public void initiateCore() {
        mCoreAPI = CoreAPI.getApi();
        String seed = mCoreAPI.getSeedData();
        mCoreAPI.Initialize(this, seed, seed.length());
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
        mViewPager = (ViewPager) findViewById(R.id.navigation_view_pager);

        mOverlayFragments.add(new TransparentFragment());
        mOverlayFragments.add(new LandingFragment());
        mOverlayFragments.add(new TransparentFragment());

        NavigationAdapter pageAdapter = new NavigationAdapter(getFragmentManager(), mOverlayFragments);
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
            if(position!=mNavThreadId) {
                AirbitzApplication.setLastNavTab(position);
                switchFragmentThread(position);
            }
        } else {
            if (position != Tabs.BD.ordinal()) {
                AirbitzApplication.setLastNavTab(position);
                mNavBarFragment.unselectTab(position);
                mNavBarFragment.unselectTab(Tabs.BD.ordinal()); // to reset mLastTab
                mNavBarFragment.selectTab(Tabs.BD.ordinal());
                DisplayLoginOverlay(true);
            }
        }
    }


    public void switchFragmentThread(int id) {
        if(mNavBarFragmentLayout.getVisibility() != View.VISIBLE && AirbitzApplication.isLoggedIn()) {
            showNavBar();
        }

        Fragment frag = mNavStacks[id].peek();
        Fragment fragShown = getFragmentManager().findFragmentById(R.id.activityLayout);
        if(fragShown!=null)
            Common.LogD(TAG, "switchFragmentThread frag, fragShown is "+frag.getClass().getSimpleName()+", "+fragShown.getClass().getSimpleName());
        else
            Common.LogD(TAG, "switchFragmentThread no fragment showing yet ");

        getFragmentManager().executePendingTransactions();
        Common.LogD(TAG, "switchFragmentThread pending transactions executed ");

        FragmentTransaction transaction = getFragmentManager().beginTransaction().disallowAddToBackStack();
        if(frag.isAdded()) {
            Common.LogD(TAG, "Fragment already added, detaching and attaching");
            transaction.detach(mNavStacks[mNavThreadId].peek());
            transaction.attach(frag);
        } else {
            transaction.replace(R.id.activityLayout, frag);
            Common.LogD(TAG, "switchFragmentThread replace executed.");
        }
        transaction.commit();
        Common.LogD(TAG, "switchFragmentThread transactions committed.");
        fragShown = getFragmentManager().findFragmentById(R.id.activityLayout);
        if(fragShown!=null) {
            Common.LogD(TAG, "switchFragmentThread showing frag is " + fragShown.getClass().getSimpleName());
        } else {
            Common.LogD(TAG, "switchFragmentThread showing frag is null");
        }
        mNavBarFragment.unselectTab(mNavThreadId);
        mNavBarFragment.unselectTab(id); // just needed for resetting mLastTab
        mNavBarFragment.selectTab(id);
        AirbitzApplication.setLastNavTab(id);
        mNavThreadId = id;

        Common.LogD(TAG, "switchFragmentThread switch to threadId "+mNavThreadId);
    }

    public void switchFragmentThread(int id, Bundle bundle) {
        if(bundle!=null)
            mNavStacks[id].peek().setArguments(bundle);
        switchFragmentThread(id);
    }

    public void pushFragment(Fragment fragment, int threadID) {
        mNavStacks[threadID].push(fragment);

        // Only show visually if we're displaying the thread
        if(mNavThreadId==threadID) {
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
        if(mNavThreadId==threadID) {
            getFragmentManager().executePendingTransactions();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.activityLayout, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    public void popFragment() {
        hideSoftKeyboard(mFragmentLayout);
        Fragment fragment = mNavStacks[mNavThreadId].pop();
        getFragmentManager().executePendingTransactions();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if((mNavStacks[mNavThreadId].size() != 0) && !(fragment instanceof HelpFragment)) {
                transaction.setCustomAnimations(R.animator.slide_in_from_left, R.animator.slide_out_right);
        }
        transaction.replace(R.id.activityLayout, mNavStacks[mNavThreadId].peek());
        transaction.commitAllowingStateLoss();
    }

    public void popFragmentImmediate(int threadId) {
        hideSoftKeyboard(mFragmentLayout);
        Fragment fragment = mNavStacks[threadId].pop();
        getFragmentManager().executePendingTransactions();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }


    ViewGroup.LayoutParams mFragmentLayoutParams;
    private ViewGroup.LayoutParams getFragmentLayoutParams() {
        if(mFragmentLayoutParams==null) {
            mFragmentLayoutParams = mFragmentLayout.getLayoutParams();
        }
        return mFragmentLayoutParams;
    }

    ViewGroup.LayoutParams mNavBarFragmentLayoutParams;
    private ViewGroup.LayoutParams getNavBarFragmentLayoutParams() {
        if(mNavBarFragmentLayoutParams==null) {
            mNavBarFragmentLayoutParams = mNavBarFragmentLayout.getLayoutParams();
        }
        return mNavBarFragmentLayoutParams;
    }

    int mNavBarStart;
    int getNavBarStart() {
        if(mNavBarStart==0) {
            int loc[] = new int[2];
            mNavBarFragmentLayout.getLocationOnScreen(loc);
            mNavBarStart = loc[1];
        }
        return mNavBarStart;
    }

    public void hideNavBar() {
        if(mNavBarFragmentLayout.getVisibility()==View.VISIBLE) {
            mFragmentLayoutParams = getFragmentLayoutParams();
            mNavBarFragmentLayout.setVisibility(View.GONE);
            RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mFragmentLayout.setLayoutParams(rLP);
            mFragmentLayout.invalidate();
            int test = getNavBarStart();
        }
    }

    public void showNavBar() {
        if(!bdonly) {
            if(mNavBarFragmentLayout.getVisibility()==View.GONE && !keyBoardUp) {
                mHandler.postDelayed(delayedShowNavBar, 50);
            }
        }
    }

    final Runnable delayedShowNavBar = new Runnable() {
        @Override
        public void run() {
            mNavBarFragmentLayout.setVisibility(View.VISIBLE);
            mFragmentLayout.setLayoutParams(getFragmentLayoutParams());
            mFragmentLayout.invalidate();
        }
    };

    public Calculator getCalculatorView() {
        return mCalculatorView;
    }

    public void hideCalculator() {
        if(mCalculatorView.getVisibility()==View.VISIBLE) {
            mCalculatorView.setVisibility(View.GONE);
            mCalculatorView.setEnabled(false);
        }
    }

    public void showCalculator() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mFragmentLayout.getWindowToken(), 0);

        if(mCalculatorView.getVisibility()!=View.VISIBLE) {
            mHandler.postDelayed(delayedShowCalculator, 100);
        }
    }
    final Runnable delayedShowCalculator = new Runnable() {
        @Override
        public void run() {
            mCalculatorView.setVisibility(View.VISIBLE);
            mCalculatorView.setEnabled(true);
        }
    };


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
            boolean handled = ((OnBackPress) fragment).onBackPress();
            if(handled)
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

        checkForCrashes();
        checkForUpdates();

        //Look for Connection change events
        registerReceiver(ConnectivityChangeReceiver, new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION));

        mNavThreadId = AirbitzApplication.getLastNavTab();

        if(!AirbitzApplication.isLoggedIn()) {
            if(mDataUri!=null)
                DisplayLoginOverlay(true);

            mNavThreadId = Tabs.BD.ordinal();
        } else {
            DisplayLoginOverlay(false);
            mCoreAPI.restoreConnectivity();
        }
        switchFragmentThread(mNavThreadId);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(ConnectivityChangeReceiver);
        mCoreAPI.lostConnectivity();
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
        Common.LogD(TAG, "onIncomingBitcoin uuid, txid = "+walletUUID+", "+txId);
        mUUID = walletUUID;
        mTxId = txId;
        /* If showing QR code, launch receiving screen*/
        Fragment f = mNavStacks[mNavThreadId].peek();
        if( f instanceof RequestQRCodeFragment) {
            if(!SettingFragment.getMerchantModePref())
            {
                startReceivedSuccess();
            } else {
                ShowOkMessageDialog("", "** Payment Received **", 10000);
            }
        } else {
            showIncomingBitcoinDialog();
        }
    }



    // callback for funds sent
    private String mIncomingUUID, mIncomingTxID;

    public void onSentFunds(String walletUUID, String txId) {
        Common.LogD(TAG, "onSentFunds uuid, txid = " + walletUUID + ", " + txId);

        mIncomingUUID = walletUUID;
        mIncomingTxID = txId;

        getFragmentManager().executePendingTransactions();

        Bundle bundle = new Bundle();
        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);
        bundle.putBoolean(WalletsFragment.CREATE, true);
        bundle.putString(Transaction.TXID, mIncomingTxID);
        bundle.putString(Wallet.WALLET_UUID, mIncomingUUID);

        Common.LogD(TAG, "onSentFunds calling switchToWallets");
        switchToWallets(bundle);

        while (mNavStacks[Tabs.SEND.ordinal()].size() > 0) {
            Common.LogD(TAG, "Send thread removing " + mNavStacks[Tabs.SEND.ordinal()].peek().getClass().getSimpleName());
            mNavStacks[Tabs.SEND.ordinal()].pop();
        }
        Fragment frag = getNewBaseFragement(Tabs.SEND.ordinal());
        mNavStacks[Tabs.SEND.ordinal()].push(frag); // Set first fragment but don't show
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
        Common.LogD(TAG, "Data Sync received");
        updateWalletListener();
    }

    @Override
    public void onBlockHeightChange() {
        Common.LogD(TAG, "Block Height received");
        updateWalletListener();
    }

    @Override
    public void OnRemotePasswordChange() {
        Common.LogD(TAG, "Remote Password received");
        if(!(mNavStacks[mNavThreadId].peek() instanceof SignUpFragment)) {
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
        showNavBar();
        if(mDataUri!=null) {
            onBitcoinUri(mDataUri);
            mDataUri = null;
            DisplayLoginOverlay(false);
            mCoreAPI.setupAccountSettings();
            mCoreAPI.startAllAsyncUpdates();
        } else {
            DisplayLoginOverlay(false);
            mCoreAPI.setupAccountSettings();
            mCoreAPI.startAllAsyncUpdates();
            resetFragmentThreadToBaseFragment(Tabs.BD.ordinal());
            switchFragmentThread(AirbitzApplication.getLastNavTab());
        }
    }

    public void startRecoveryQuestions(String questions, String username) {
        hideNavBar();
        Bundle bundle = new Bundle();
        bundle.putInt(PasswordRecoveryFragment.MODE, PasswordRecoveryFragment.FORGOT_PASSWORD);
        bundle.putString(PasswordRecoveryFragment.ANSWERS, questions);
        bundle.putString(PasswordRecoveryFragment.USERNAME,  username);
        Fragment frag = new PasswordRecoveryFragment();
        frag.setArguments(bundle);
        pushFragmentNoAnimation(frag, mNavThreadId);
        DisplayLoginOverlay(false);
    }

    public void startSignUp() {
        hideSoftKeyboard(mFragmentLayout);
        hideNavBar();
        Fragment frag = new SignUpFragment();
        pushFragmentNoAnimation(frag, mNavThreadId);
        DisplayLoginOverlay(false);
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
//        sendCredentialsToService(null, null);
        AirbitzApplication.Logout();
        mCoreAPI.logout();
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
                setViewPager();
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


    private Fragment getNewBaseFragement(int id) {
        switch(id) {
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

    //************************ Connectivity support

    public  boolean networkIsAvailable() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    Common.LogD(TAG, "Connection is WIFI");
                    haveConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    Common.LogD(TAG, "Connection is MOBILE");
                    haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    BroadcastReceiver ConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if(extras!=null) {
                if(networkIsAvailable()) {
                    Common.LogD(TAG, "Connection available");
                    mCoreAPI.restoreConnectivity();
                } else { // has connection
                    Common.LogD(TAG, "Connection NOT available");
                    mCoreAPI.lostConnectivity();
                    ShowOkMessageDialog(getString(R.string.string_no_connection_title), getString(R.string.string_no_connection_message));
                }
            }
        }
    };
}
