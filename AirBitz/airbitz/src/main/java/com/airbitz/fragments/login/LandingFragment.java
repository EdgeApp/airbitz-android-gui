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

package com.airbitz.fragments.login;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.AccountsAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.login.twofactor.TwoFactorMenuFragment;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;

public class LandingFragment extends BaseFragment implements
    NavigationActivity.OnFadingDialogFinished,
    TwoFactorMenuFragment.OnTwoFactorMenuResult,
    AccountsAdapter.OnButtonTouched {
    private final String TAG = getClass().getSimpleName();

    private final int INVALID_ENTRY_COUNT_MAX = 3;
    private static final String INVALID_ENTRY_PREF = "fragment_landing_invalid_entries";

    String mUsername;
    char[] mPassword;

    private TextView mDetailTextView;
    private ImageView mRightArrow;
    private EditText mUserNameEditText;
    private ListView mAccountsListView;
    private ListView mOtherAccountsListView;
    private List<String> mAccounts;
    private List<String> mOtherAccounts;
    private AccountsAdapter mAccountsAdapter;
    private AccountsAdapter mOtherAccountsAdapter;
    private View mPasswordLayout;
    private EditText mPasswordEditText;
    private EditText mPinEditText;
    private View mPinLayout;
    private List<ImageView> mPinViews;

    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressButton mCreateAccountButton;
    private TextView mCurrentUserText;
    private TextView mForgotTextView;
    private TextView mLandingSubtextView;
    private LinearLayout mSwipeLayout;
    private LinearLayout mForgotPasswordButton;

    private PINLoginTask mPINLoginTask;
    private PasswordLoginTask mPasswordLoginTask;
    
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    private Handler mHandler = new Handler();

    /**
     * Represents an asynchronous question fetch task
     */
    private GetRecoveryQuestionsTask mRecoveryQuestionsTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
        saveInvalidEntryCount(0);
        SharedPreferences prefs = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mUsername = prefs.getString(AirbitzApplication.LOGIN_NAME, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        mDetailTextView = (TextView) view.findViewById(R.id.fragment_landing_detail_textview);
        mDetailTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mSwipeLayout = (LinearLayout) view.findViewById(R.id.fragment_landing_swipe_layout);

        mUserNameEditText = (EditText) view.findViewById(R.id.fragment_landing_username_edittext);
        mUserNameEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mUserNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showAccountsList(true);
                } else {
                    showAccountsList(false);
                }
            }
        });

        mPasswordLayout = view.findViewById(R.id.fragment_landing_password_layout);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_landing_password_edittext);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    refreshView(false, true);
                } else {
                    refreshView(false, false);
                }
            }
        });
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptPasswordLogin();
                    return true;
                }
                return false;
            }
        });

        mRightArrow = (ImageView) view.findViewById(R.id.fragment_landing_arrowright_imageview);
        mLandingSubtextView = (TextView) view.findViewById(R.id.fragment_landing_detail_textview);


        mBackButton = (HighlightOnPressImageButton) view.findViewById(R.id.fragment_landing_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mCreateAccountButton = (HighlightOnPressButton) view.findViewById(R.id.fragment_landing_create_account);
        mCreateAccountButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPinLayout.getVisibility() == View.VISIBLE) {
                    mUserNameEditText.setText(mUsername);
                    refreshView(false, false);
                } else {
                    if (mActivity.networkIsAvailable()) {
                        mActivity.startSignUp(mUserNameEditText.getText().toString());
                    } else {
                        mActivity.ShowFadingDialog(getActivity().getString(R.string.string_no_connection_message));
                    }
                }
            }
        });

        mCurrentUserText = (TextView) view.findViewById(R.id.fragment_landing_current_user);
        mCurrentUserText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOtherAccountsListView.getVisibility() == View.VISIBLE) {
                    showOthersList(mUsername, false);
                }
                else {
                    showOthersList(mUsername, true);
                }
            }
        });

        mAccountsListView = (ListView) view.findViewById(R.id.fragment_landing_account_listview);
        mAccounts = new ArrayList<String>();
        mAccountsAdapter = new AccountsAdapter(getActivity(), mAccounts);
        mAccountsListView.setAdapter(mAccountsAdapter);
        mAccountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUsername = mAccounts.get(position);
                if (mCoreAPI.PinLoginExists(mUsername)) {
                    saveCachedLoginName(mUsername);
                    refreshView(true, true);
                } else {
                    mUserNameEditText.setText(mUsername);
                    mPasswordEditText.requestFocus();
                }
            }
        });

        mOtherAccountsListView = (ListView) view.findViewById(R.id.fragment_landing_other_account_listview);
        mOtherAccounts = new ArrayList<String>();
        mOtherAccountsAdapter = new AccountsAdapter(getActivity(), mOtherAccounts);
        mOtherAccountsListView.setAdapter(mOtherAccountsAdapter);
        mOtherAccountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUsername = mOtherAccounts.get(position);
                if (mCoreAPI.PinLoginExists(mUsername)) {
                    saveCachedLoginName(mUsername);
                    refreshView(true, true);
                } else {
                    mUserNameEditText.setText(mUsername);
                    refreshView(false, false);
                    mPasswordEditText.requestFocus();
                }
            }
        });

        mPinLayout = view.findViewById(R.id.fragment_landing_pin_entry_layout);

        mPinEditText = (EditText) view.findViewById(R.id.fragment_landing_pin_edittext);
        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // set views based on length
                setPinViews(mPinEditText.length());
                if (mPinEditText.length() >= 4) {
                    if (mActivity.networkIsAvailable()) {
                        mActivity.hideSoftKeyboard(mPinEditText);
                        refreshView(true, false);
                        attemptPinLogin();
                    } else {
                        mActivity.ShowFadingDialog(getActivity().getString(R.string.string_no_connection_pin_message));
                        abortPermanently();
                    }
                }
            }
        };
        mPinEditText.addTextChangedListener(mPINTextWatcher);
        mPinEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPinEditText.setText("");
                mPinEditText.requestFocus();
                mActivity.showSoftKeyboard(mPinEditText);
            }
        });

        mForgotTextView = (TextView) view.findViewById(R.id.fragment_landing_forgot_text);

        mForgotPasswordButton = (LinearLayout) view.findViewById(R.id.fragment_landing_forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUserNameEditText.getText().toString().isEmpty()) {
                    mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_forgot_no_username_title));
                } else {
                    attemptForgotPassword();
                }
            }
        });

        HighlightOnPressButton mSignInButton = (HighlightOnPressButton) view.findViewById(R.id.fragment_landing_signin_button);
        mSignInButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.hideSoftKeyboard(mPasswordEditText);
                mActivity.hideSoftKeyboard(mUserNameEditText);
                attemptPasswordLogin();
            }
        });

        ObjectAnimator rightBounce = ObjectAnimator.ofFloat(mRightArrow, "translationX", 0, 50);
        rightBounce.setRepeatCount(3);
        rightBounce.setDuration(500);
        rightBounce.setRepeatMode(ValueAnimator.REVERSE);
        rightBounce.start();

        mPinViews = new ArrayList<ImageView>();
        mPinViews.add((ImageView) view.findViewById(R.id.fragment_landing_pin_one));
        mPinViews.add((ImageView) view.findViewById(R.id.fragment_landing_pin_two));
        mPinViews.add((ImageView) view.findViewById(R.id.fragment_landing_pin_three));
        mPinViews.add((ImageView) view.findViewById(R.id.fragment_landing_pin_four));
        setPinViews(0);

        return view;
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

    private void showOthersList(String username, boolean show)
    {
        mOtherAccounts.clear();
        mOtherAccounts.addAll(otherAccounts(username));
        mOtherAccountsAdapter.notifyDataSetChanged();
        if(show && !mOtherAccounts.isEmpty()) {
            if(mOtherAccountsAdapter.getCount() > 4) {
                View item = mOtherAccountsAdapter.getView(0, null, mOtherAccountsListView);
                item.measure(0, 0);
                ViewGroup.LayoutParams params = mOtherAccountsListView.getLayoutParams();
                params.height = 4 * item.getMeasuredHeight();
                mOtherAccountsListView.setLayoutParams(params);
            }
            mOtherAccountsListView.setVisibility(View.VISIBLE);
        }
        else {
            mOtherAccountsListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onButtonTouched(final String account) {
        String message = String.format(getString(R.string.fragment_landing_account_delete_message), account);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(getString(R.string.fragment_landing_account_delete_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mUserNameEditText.clearFocus();
                                if (!mCoreAPI.deleteAccount(account)) {
                                    mActivity.ShowFadingDialog("Account could not be deleted.");
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.string_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showAccountsList(false);
                                dialog.dismiss();
                            }
                        });
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }

    private void showAccountsList(boolean show) {
        mAccounts.clear();
        mAccounts.addAll(mCoreAPI.listAccounts());
        mAccountsAdapter.notifyDataSetChanged();
        if(show && !mAccounts.isEmpty()) {
            if(mAccountsAdapter.getCount() > 4) {
                View item = mAccountsAdapter.getView(0, null, mAccountsListView);
                item.measure(0, 0);
                ViewGroup.LayoutParams params = mAccountsListView.getLayoutParams();
                params.height = 4 * item.getMeasuredHeight();
                mAccountsListView.setLayoutParams(params);
            }
            mAccountsListView.setVisibility(View.VISIBLE);
            mAccountsAdapter.setButtonTouchedListener(this);
        }
        else {
            mAccountsListView.setVisibility(View.GONE);
            mAccountsAdapter.setButtonTouchedListener(null);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint())
        {
            return;
        }

        mUserNameEditText.setText(mUsername);
        refreshView(false, false);
        if(mActivity.networkIsAvailable()) {
            if(!AirbitzApplication.isLoggedIn()) {
                mPinEditText.setText("");
                if (mCoreAPI.PinLoginExists(mUsername)) {
                    refreshView(true, true);
                    mHandler.postDelayed(delayedShowPinKeyboard, 100);
                }
            }
        }
        else {
            mActivity.ShowFadingDialog(getActivity().getString(R.string.string_no_connection_pin_message));
            refreshView(false, false);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mActivity.hideSoftKeyboard(mPinEditText);
        mActivity.hideSoftKeyboard(mPasswordEditText);
    }

    private void refreshView(boolean isPinLogin, boolean isKeyboardUp) {
        if(isPinLogin) {
            showOthersList(mUsername, false);
            mPinLayout.setVisibility(View.VISIBLE);
            mPasswordLayout.setVisibility(View.GONE);
            mForgotPasswordButton.setVisibility(View.GONE);

            String out = String.format(getString(R.string.fragment_landing_please_enter_pin), mUsername);
            int start = out.indexOf(mUsername);

            SpannableStringBuilder s = new SpannableStringBuilder();
            s.append(out).setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_highlight)), start, start + mUsername.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mCurrentUserText.setText(s);

            mCreateAccountButton.setText(getString(R.string.fragment_landing_switch_user));

            if(isKeyboardUp) {
                mLandingSubtextView.setVisibility(View.GONE);
                mSwipeLayout.setVisibility(View.GONE);
            } else {
                mLandingSubtextView.setVisibility(View.VISIBLE);
                mSwipeLayout.setVisibility(View.VISIBLE);
            }
            mPinEditText.requestFocus();
        } else {
            mPinLayout.setVisibility(View.GONE);
            mPasswordLayout.setVisibility(View.VISIBLE);
            mCreateAccountButton.setText(getString(R.string.fragment_landing_signup_button));
            mForgotPasswordButton.setVisibility(View.VISIBLE);
            mForgotTextView.setText(getString(R.string.fragment_landing_forgot_password));
            mLandingSubtextView.setVisibility(View.VISIBLE);
            mSwipeLayout.setVisibility(View.VISIBLE);
            if(isKeyboardUp) {
                mDetailTextView.setVisibility(View.GONE);
                mLandingSubtextView.setVisibility(View.GONE);
                mSwipeLayout.setVisibility(View.GONE);
            }
            else {
                mDetailTextView.setVisibility(View.VISIBLE);
                mLandingSubtextView.setVisibility(View.VISIBLE);
                mSwipeLayout.setVisibility(View.VISIBLE);
            }
            showAccountsList(false);
        }
    }

    private void setPinViews(int length) {
        for(int i=0; i<mPinViews.size(); i++) {
            if(i >= length) {
                mPinViews.get(i).setVisibility(View.GONE);
            } else {
                mPinViews.get(i).setVisibility(View.VISIBLE);
            }
        }
    }

    private void attemptForgotPassword() {
        mRecoveryQuestionsTask = new GetRecoveryQuestionsTask();
        mRecoveryQuestionsTask.execute(mUserNameEditText.getText().toString());
    }

    /**
     * Attempts PIN based login
     */
    public void attemptPinLogin() {
        if(mActivity.networkIsAvailable()) {
            mPINLoginTask = new PINLoginTask();
            mPINLoginTask.execute(mUsername, mPinEditText.getText().toString());
        }
        else {
            mActivity.ShowFadingDialog(getString(R.string.server_error_no_connection));
        }
    }

    public class PINLoginTask extends AsyncTask {
        String mUsername;
        String mPin;

        @Override
        protected void onPreExecute() {
            mActivity.hideSoftKeyboard(mPinEditText);
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Object... params) {
            mUsername = (String) params[0];
            mPin = (String) params[1];
            if(mUsername == null || mPin == null) {
                return tABC_CC.ABC_CC_Error;
            }
            else {
                return mCoreAPI.PinLogin(mUsername, mPin);
            }
        }

        @Override
        protected void onPostExecute(final Object success) {
            mActivity.showModalProgress(false);
            mPINLoginTask = null;
            tABC_CC result = (tABC_CC) success;
            mPinEditText.setText("");

            if(result == tABC_CC.ABC_CC_Ok) {
                mPinEditText.clearFocus();
                mActivity.LoginNow(mUsername, null);
                return;
            }
            else if(result == tABC_CC.ABC_CC_BadPassword) {
                saveInvalidEntryCount(getInvalidEntryCount() + 1);
                if(getInvalidEntryCount() >= INVALID_ENTRY_COUNT_MAX) {
                    mActivity.ShowFadingDialog(getString(R.string.server_error_bad_pin_login));
                    saveInvalidEntryCount(0);
                    abortPermanently();
                    return;
                }
                else {
                    mActivity.setFadingDialogListener(LandingFragment.this);
                    mActivity.ShowFadingDialog(getString(R.string.server_error_bad_pin));
                    mPinEditText.requestFocus();
                }
            }
            else {
                mActivity.setFadingDialogListener(LandingFragment.this);
                mActivity.ShowFadingDialog(Common.errorMap(getActivity(), result));
                abortPermanently();
                return;
            }
        }

        @Override
        protected void onCancelled() {
            mPINLoginTask = null;
            mActivity.ShowFadingDialog(getResources().getString(R.string.activity_navigation_signin_failed_unexpected));
            mPinEditText.setText("");
        }
    }

    @Override
    public void onFadingDialogFinished() {
        mActivity.setFadingDialogListener(null);
        refreshView(true, true);
        mHandler.postDelayed(delayedShowPinKeyboard, 100);
    }

    final Runnable delayedShowPinKeyboard = new Runnable() {
        @Override
        public void run() {
            mPinEditText.setText("");
            mPinEditText.performClick();
        }
    };


    public class PasswordLoginTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_Error doInBackground(Object... params) {
            mUsername = (String) params[0];
            mPassword = (char[]) params[1];
            return mCoreAPI.SignIn(mUsername, mPassword);
        }

        @Override
        protected void onPostExecute(final Object error) {
            mActivity.showModalProgress(false);
            mPasswordLoginTask = null;
            signInComplete((tABC_Error) error);
        }

        @Override
        protected void onCancelled() {
            mPasswordLoginTask = null;
            mActivity.ShowFadingDialog(getResources().getString(R.string.activity_navigation_signin_failed_unexpected));
        }
    }

    private void signInComplete(tABC_Error error) {
        tABC_CC resultCode = error.getCode();
        mCoreAPI.otpSetError(resultCode);

        if(error.getCode() == tABC_CC.ABC_CC_Ok) {
            Editable pass = mPasswordEditText.getText();
            char[] password = new char[pass.length()];
            pass.getChars(0, pass.length(), password, 0);
            mActivity.LoginNow(mUsername, password);
        } else if (tABC_CC.ABC_CC_InvalidOTP == resultCode) {
            launchTwoFactorMenu();
        } else {
            if (tABC_CC.ABC_CC_InvalidOTP == resultCode) {
                launchTwoFactorMenu();
            } else {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, resultCode));
            }
        }
    }

    private void launchTwoFactorMenu() {
        TwoFactorMenuFragment fragment = new TwoFactorMenuFragment();
        fragment.setOnTwoFactorMenuResult(this);
        Bundle bundle = new Bundle();
        bundle.putBoolean(TwoFactorMenuFragment.STORE_SECRET, false);
        bundle.putBoolean(TwoFactorMenuFragment.TEST_SECRET, false);
        bundle.putString(TwoFactorMenuFragment.USERNAME, mUsername);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);
        mActivity.DisplayLoginOverlay(false);
    }

    @Override
    public void onTwoFactorMenuResult(boolean success, String secret) {
        mActivity.DisplayLoginOverlay(true);
        if(success) {
            twoFactorSignIn(secret);
        }
    }

    private void twoFactorSignIn(String secret) {
        mCoreAPI.OtpKeySet(mUsername, secret);
        mPasswordLoginTask = new PasswordLoginTask();
        mPasswordLoginTask.execute(mUsername, mPassword);
    }

    private void abortPermanently() {
        mCoreAPI.PINLoginDelete(mUsername);
        refreshView(false, false); // reset to password view
    }

    private void saveCachedLoginName(String name) {
        SharedPreferences.Editor editor = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putString(AirbitzApplication.LOGIN_NAME, name);
        editor.apply();
    }

    private void saveInvalidEntryCount(int entries) {
        SharedPreferences.Editor editor = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(INVALID_ENTRY_PREF, entries);
        editor.apply();
    }

    static public int getInvalidEntryCount() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(INVALID_ENTRY_PREF, 0); // default to Automatic
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptPasswordLogin() {

        // Reset errors.
        mPasswordEditText.setError(null);
        mUserNameEditText.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameEditText.getText().toString();
        Editable pass = mPasswordEditText.getText();
        char[] password = new char[pass.length()];
        pass.getChars(0, pass.length(), password, 0);

        boolean cancel = false;
        View focusView = null;

        // Check for empty username.
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_invalid_credentials));
            focusView = mUserNameEditText;
            cancel = true;
        }

        // Check for empty password.
        if (password.length<1) {
            mPasswordEditText.setError(getString(R.string.error_invalid_credentials));
            if (null == focusView) {
                focusView = mPasswordEditText;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mPasswordLoginTask = new PasswordLoginTask();
            mPasswordLoginTask.execute(username, password);
        }
    }

    public class GetRecoveryQuestionsTask extends AsyncTask<String, Void, String> {

        @Override
        public void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            return mCoreAPI.GetRecoveryQuestionsForUser(params[0]);
        }

        @Override
        protected void onPostExecute(String questionString) {
            mActivity.showModalProgress(false);

            mRecoveryQuestionsTask = null;

            if (questionString == null) {
                mActivity.ShowOkMessageDialog(getString(R.string.fragment_forgot_no_recovery_questions_title),
                        getString(R.string.fragment_forgot_no_recovery_questions_text));
            } else { // Some message or questions
                String[] questions = questionString.split("\n");
                if (questions.length > 1) { // questions came back
                    mActivity.startRecoveryQuestions(questionString, mUserNameEditText.getText().toString());
                } else if (questions.length == 1) { // Error string
                    Log.d(TAG, questionString);
                    mActivity.ShowFadingDialog(questions[0]);
                }
            }
        }

        @Override
        protected void onCancelled() {
            mRecoveryQuestionsTask = null;
            mActivity.showModalProgress(false);
        }

    }
}
