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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import com.afollestad.materialdialogs.AlertDialogWrapper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.AccountsAdapter;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.settings.PasswordRecoveryFragment;
import com.airbitz.fragments.settings.twofactor.TwoFactorMenuFragment;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.UploadLogAlert;
import com.airbitz.utils.Common;
import com.airbitz.utils.AccountDump;

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
    String mPassword;
    String mPin;

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
    private boolean mIgnorePinChange = false;
    private EditText mPinEditText;
    private View mPinLayout;
    private View mBlackoutView;

    private HighlightOnPressImageButton mBackButton;
    private Button mSignInButton;
    private Button mCreateAccountButton;
    private Button mExitPinLoginButton;
    private TextView mCurrentUserText;
    private TextView mForgotTextView;
    private TextView mLandingSubtextView;
    private LinearLayout mSwipeLayout;
    private LinearLayout mForgotPasswordButton;

    private PINLoginTask mPINLoginTask;
    private PasswordLoginTask mPasswordLoginTask;

    private AirbitzCore mCoreAPI;
    private NavigationActivity mActivity;
    private Handler mHandler = new Handler();

    private int mPinFailedCount;
    private boolean mPinLoginMode;

    private ImageView mLogo;
    private int mAccountTaps;
    private boolean mFirstLogin = false;

    static final int MAX_PIN_FAILS = 3;

    /**
     * Represents an asynchronous question fetch task
     */
    private GetRecoveryQuestionsTask mRecoveryQuestionsTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = AirbitzCore.getApi();
        mActivity = (NavigationActivity) getActivity();
        saveInvalidEntryCount(0);
        SharedPreferences prefs = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mUsername = prefs.getString(AirbitzApplication.LOGIN_NAME, "");
        mPositionNavBar = false;
    }

    View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_landing, container, false);

        Common.addStatusBarPadding(mActivity, mView);

        mBlackoutView = mView.findViewById(R.id.fragment_landing_black);
        mDetailTextView = (TextView) mView.findViewById(R.id.fragment_landing_detail_textview);
        mDetailTextView.setTypeface(NavigationActivity.latoRegularTypeFace);

        mSwipeLayout = (LinearLayout) mView.findViewById(R.id.fragment_landing_swipe_layout);

        mUserNameEditText = (EditText) mView.findViewById(R.id.fragment_landing_username_edittext);
        mUserNameEditText.setTypeface(NavigationActivity.latoRegularTypeFace);
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
        mUserNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mUserNameEditText.getText())) {
                    toggleAccountList();
                }
            }
        });
        mUserNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    showAccountsList(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mUsername = s.toString();
            }
        });

        mUserNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mPasswordLayout = mView.findViewById(R.id.fragment_landing_password_layout);
        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_landing_password_edittext);
        mPasswordEditText.setTypeface(NavigationActivity.latoRegularTypeFace);
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
                    mActivity.hideSoftKeyboard(mPasswordEditText);
                    return true;
                }
                return false;
            }
        });

        mAccountTaps = 0;
        mLogo = (ImageView) mView.findViewById(R.id.fragment_landing_logo_imageview);
        mLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (++mAccountTaps < 5) {
                    return;
                }
                AirbitzCore.logi("Uploading logs from Landing screen");
                UploadLogAlert uploadLogAlert = new UploadLogAlert(mActivity);
                uploadLogAlert.showUploadLogAlert();
                mAccountTaps = 0;

            }
        });

        mRightArrow = (ImageView) mView.findViewById(R.id.fragment_landing_arrowright_imageview);
        mLandingSubtextView = (TextView) mView.findViewById(R.id.fragment_landing_detail_textview);


        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_landing_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.hideSoftKeyboard(mPinEditText);
                mActivity.hideSoftKeyboard(mPasswordEditText);
                getActivity().onBackPressed();
            }
        });

        mCreateAccountButton = (Button) mView.findViewById(R.id.fragment_landing_create_account);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPinLayout.getVisibility() == View.VISIBLE) {
                    assert(false); // Should not happen
//                    mUserNameEditText.setText(mUsername);
//                    refreshView(false, false);
//                    mPasswordEditText.requestFocus();
//                    mHandler.postDelayed(delayedShowPasswordKeyboard, 100);
                } else {
                    if (mActivity.networkIsAvailable()) {
                        mActivity.startSignUp(mUserNameEditText.getText().toString());
                    } else {
                        mActivity.ShowFadingDialog(getActivity().getString(R.string.string_no_connection_message));
                    }
                }
            }
        });

        mExitPinLoginButton = (Button) mView.findViewById(R.id.fragment_landing_exit_pin);
        mExitPinLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPinLayout.getVisibility() == View.VISIBLE) {
                    mUserNameEditText.setText(mUsername);
                    refreshView(false, false);
                    mPasswordEditText.requestFocus();
                    mHandler.postDelayed(delayedShowPasswordKeyboard, 100);
                } else {
                    assert(false); // Should not happen
//                    if (mActivity.networkIsAvailable()) {
//                        mActivity.startSignUp(mUserNameEditText.getText().toString());
//                    } else {
//                        mActivity.ShowFadingDialog(getActivity().getString(R.string.string_no_connection_message));
//                    }
                }
            }
        });

        mCurrentUserText = (TextView) mView.findViewById(R.id.fragment_landing_current_user);
        mCurrentUserText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOthersList(mOtherAccountsListView.getVisibility() != View.VISIBLE);
            }
        });

        mAccountsListView = (ListView) mView.findViewById(R.id.fragment_landing_account_listview);
        mAccounts = new ArrayList<String>();
        mAccountsAdapter = new AccountsAdapter(getActivity(), mAccounts);
        mAccountsListView.setAdapter(mAccountsAdapter);
        mAccountsListView.bringToFront();
        mAccountsListView.invalidate();
        mAccountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUsername = mAccounts.get(position);
                if (mCoreAPI.accountHasPinLogin(mUsername)) {
                    saveUsername(mUsername);
                    refreshView(true, true);
                } else {
                    mUserNameEditText.setText(mUsername);
                    mPasswordEditText.requestFocus();
                }
            }
        });

        mOtherAccountsListView = (ListView) mView.findViewById(R.id.fragment_landing_other_account_listview);
        mOtherAccounts = new ArrayList<String>();
        mOtherAccountsAdapter = new AccountsAdapter(getActivity(), mOtherAccounts);
        mOtherAccountsListView.setAdapter(mOtherAccountsAdapter);
        mOtherAccountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUsername = mOtherAccounts.get(position);
                if (mCoreAPI.accountHasPinLogin(mUsername)) {
                    saveUsername(mUsername);
                    refreshView(true, true);
                } else {
                    mUserNameEditText.setText(mUsername);
                    refreshView(false, false);
                    mPasswordEditText.requestFocus();
                }
            }
        });

        mPinLayout = mView.findViewById(R.id.fragment_landing_pin_entry_layout);

        mPinEditText = (EditText) mView.findViewById(R.id.fragment_landing_pin_edittext);
        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mIgnorePinChange) {
                    String text = editable.toString();
                    mIgnorePinChange = true;
                    mPinEditText.setText(text);
                    mPinEditText.setSelection(text.length());
                    mIgnorePinChange = false;

                    // set views based on length
                    if (text.length() >= 4) {
                        if (mActivity.networkIsAvailable()) {
                            mActivity.hideSoftKeyboard(mPinEditText);
                            attemptPinLogin();
                        } else {
                            mActivity.ShowFadingDialog(getActivity().getString(R.string.string_no_connection_pin_message));
                            abortPermanently();
                        }
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

        mForgotTextView = (TextView) mView.findViewById(R.id.fragment_landing_forgot_text);

        mForgotPasswordButton = (LinearLayout) mView.findViewById(R.id.fragment_landing_forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchRecoveryPopup("");
            }
        });

        mSignInButton = (Button) mView.findViewById(R.id.fragment_landing_signin_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUserNameEditText.getVisibility() == View.GONE) {
                    mUserNameEditText.setVisibility(View.VISIBLE);
                    mPasswordEditText.setVisibility(View.VISIBLE);
                } else {
                    mActivity.hideSoftKeyboard(mPasswordEditText);
                    mActivity.hideSoftKeyboard(mUserNameEditText);
                    attemptPasswordLogin();
                }
            }
        });

        ObjectAnimator rightBounce = ObjectAnimator.ofFloat(mRightArrow, "translationX", 0, -25);
        rightBounce.setRepeatCount(3);
        rightBounce.setDuration(500);
        rightBounce.setRepeatMode(ValueAnimator.REVERSE);
        rightBounce.start();

        mAccounts.clear();
        mAccounts.addAll(mCoreAPI.listLocalAccounts());
        if (mAccounts.isEmpty())
        {
            mUserNameEditText.setVisibility(View.GONE);
            mPasswordEditText.setVisibility(View.GONE);
        }

        mView.setOnTouchListener(mActivity);

        AccountDump.cleanUp(getActivity());

        return mView;
    }

    private List<String> otherAccounts(String username) {
        List<String> accounts = mCoreAPI.listLocalAccounts();
        List<String> others = new ArrayList<String>();
        for(int i=0; i< accounts.size(); i++) {
            if(!accounts.get(i).equals(username)) {
                others.add(accounts.get(i));
            }
        }
        return others;
    }

    private void showOthersList(boolean show)
    {
        mOtherAccounts.clear();
        mOtherAccounts.addAll(mCoreAPI.listLocalAccounts());
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
            mOtherAccountsListView.bringToFront();
            mOtherAccountsListView.invalidate();
            mOtherAccountsAdapter.setButtonTouchedListener(this);
        }
        else {
            mOtherAccountsListView.setVisibility(View.GONE);
            mOtherAccountsAdapter.setButtonTouchedListener(null);
        }
    }

    @Override
    public void onButtonTouched(final String account) {
        String message = String.format(getString(R.string.fragment_landing_account_delete_message), account);
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(mActivity, R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(getString(R.string.fragment_landing_account_delete_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mUserNameEditText.setText("");
                                mUsername = "";
                                if (!mCoreAPI.accountDeleteLocal(account)) {
                                    mActivity.ShowFadingDialog("Account could not be deleted.");
                                }
                                showAccountsList(false);
                                showOthersList(false);
                                refreshView(false, false);
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
        Dialog confirmDialog = builder.create();
        confirmDialog.show();
    }

    private void toggleAccountList() {
        if (mAccountsListView.getVisibility() == View.VISIBLE) {
            showAccountsList(false);
        } else {
            showAccountsList(true);
        }
    }

    private void showAccountsList(boolean show) {
        mAccounts.clear();
        mAccounts.addAll(mCoreAPI.listLocalAccounts());
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

        SharedPreferences prefs = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mUsername = prefs.getString(AirbitzApplication.LOGIN_NAME, "");
        if(mActivity.networkIsAvailable()) {
            if(!AirbitzApplication.isLoggedIn() && mCoreAPI.accountHasPinLogin(mUsername)) {
                mPinEditText.setText("");
                AirbitzCore.logi("showing pin login for " + mUsername);
                refreshView(true, true, true);
                return;
            }
        } else if (!AirbitzApplication.isLoggedIn()) {
            mActivity.ShowFadingDialog(getActivity().getString(R.string.string_no_connection_pin_message));
        }

        AirbitzCore.logi("showing password login for " + mUsername);
        refreshView(false, false, true);
    }

    @Override
    public void onPause() {
        super.onPause();

        mActivity.hideSoftKeyboard(mPinEditText);
        mActivity.hideSoftKeyboard(mPasswordEditText);
    }

    public void refreshViewAndUsername() {
        SharedPreferences prefs = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mUsername = prefs.getString(AirbitzApplication.LOGIN_NAME, "");
        mPinLoginMode = mCoreAPI.accountHasPinLogin(mUsername);
        refreshView();
    }

    public void refreshView() {
        refreshView(mPinLoginMode, mPinLoginMode);
    }

    private void refreshView(boolean isPinLogin, boolean isKeyboardUp) {
        refreshView(isPinLogin, isKeyboardUp, false);
    }

    private void refreshView(boolean isPinLogin, boolean isKeyboardUp, boolean isTransition) {
        if (isTransition) {
            mBlackoutView.setVisibility(View.VISIBLE);
            mBlackoutView.setAlpha(1f);
            mBlackoutView.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mBlackoutView.setVisibility(View.GONE);
                        }
                    });
        }
        if (isPinLogin) {
            mHandler.postDelayed(delayedShowPinKeyboard, 500);
            mPinLoginMode = true;
            mPinFailedCount = 0;

            showOthersList(false);
            mPinLayout.setVisibility(View.VISIBLE);

            mPasswordLayout.setVisibility(View.GONE);
            mForgotPasswordButton.setVisibility(View.GONE);

            mCurrentUserText.setText(mUsername);
            mCreateAccountButton.setText(getString(R.string.fragment_landing_switch_user));
            mSwipeLayout.setVisibility(View.VISIBLE);
            if (isKeyboardUp) {
                mLandingSubtextView.setVisibility(View.GONE);
            } else {
                mLandingSubtextView.setVisibility(View.VISIBLE);
            }
            mPinEditText.requestFocus();
        } else {
            if(mUsername.isEmpty()) {
                mHandler.postDelayed(delayedShowUsernameKeyboard, 300);
            }
            else {
                if (mUserNameEditText.getText().length() == 0) {
                    mUserNameEditText.setText(mUsername);
                }

                mHandler.postDelayed(delayedShowPasswordKeyboard, 300);
            }

            mPinLoginMode = false;
            mPinLayout.setVisibility(View.GONE);
            mPasswordLayout.setVisibility(View.VISIBLE);
            mCreateAccountButton.setText(getString(R.string.fragment_landing_signup_button));
            mForgotPasswordButton.setVisibility(View.VISIBLE);
            mForgotTextView.setText(getString(R.string.fragment_landing_forgot_password));
            mLandingSubtextView.setVisibility(View.VISIBLE);
            mSwipeLayout.setVisibility(View.VISIBLE);
            if (isKeyboardUp) {
                mDetailTextView.setVisibility(View.GONE);
                mLandingSubtextView.setVisibility(View.GONE);
            } else {
                mDetailTextView.setVisibility(View.VISIBLE);
                mLandingSubtextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void launchRecoveryPopup(final String recoveryToken) {

        new MaterialDialog.Builder(mActivity)
                .title(R.string.activity_recovery_title)
                .content(R.string.enter_username_to_recover)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.string_next)
                .negativeText(R.string.string_cancel)
                .input("", mUserNameEditText.getText().toString(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        mRecoveryQuestionsTask = new GetRecoveryQuestionsTask();
                        mRecoveryQuestionsTask.execute(input.toString(), recoveryToken);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Attempts PIN based login
     */
    public void attemptPinLogin() {
        if(mActivity.networkIsAvailable()) {
            mPin = mPinEditText.getText().toString();
            mFirstLogin = isFirstLogin();
            mPINLoginTask = new PINLoginTask();
            mPINLoginTask.execute(mUsername, mPinEditText.getText().toString(), null);
        }
        else {
            mActivity.ShowFadingDialog(getString(R.string.server_error_no_connection));
        }
    }

    public class PINLoginTask extends AsyncTask<String, Void, Boolean> {
        String mUsername;
        String mPin;
        AirbitzException mFailureException;
        Account mAccount;

        @Override
        protected void onPreExecute() {
            mActivity.hideSoftKeyboard(mPinEditText);
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            mUsername = params[0];
            mPin = params[1];
            String secret = params[2];
            try {
                mAccount = mCoreAPI.pinLogin(mUsername, mPin, secret);
                AirbitzApplication.Login(mAccount);
                return true;
            } catch (AirbitzException e) {
                mFailureException = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mActivity.showModalProgress(false);
            mPINLoginTask = null;
            mPinEditText.setText("");

            if (success) {
                mPinEditText.clearFocus();
                mActivity.LoginNow(mAccount, mFirstLogin);
            } else if (mFailureException.isBadPassword()) {
                mActivity.setFadingDialogListener(LandingFragment.this);
                mActivity.ShowFadingDialog(getString(R.string.server_error_bad_pin));
                mPinEditText.requestFocus();
            } else if (mFailureException.isOtpError()) {
                AirbitzApplication.setOtpError(true);
                AirbitzApplication.setOtpResetDate(mFailureException.otpResetDate());
                AirbitzApplication.setOtpResetToken(mFailureException.otpResetToken());
                launchTwoFactorMenu();
            } else {
                mActivity.setFadingDialogListener(LandingFragment.this);
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, mFailureException));
                mPinFailedCount++;
                if (mPinFailedCount >= MAX_PIN_FAILS) {
                    abortPermanently();
                }
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
        mActivity.showSoftKeyboard(mPinEditText);
    }

    final Runnable delayedShowPinKeyboard = new Runnable() {
        @Override
        public void run() {
            mPinEditText.setText("");
            mActivity.showSoftKeyboard(mPinEditText);
        }
    };

    final Runnable delayedShowPasswordKeyboard = new Runnable() {
        @Override
        public void run() {
            mPasswordEditText.setText("");
            mActivity.showSoftKeyboard(mPasswordEditText);
        }
    };

    final Runnable delayedShowUsernameKeyboard = new Runnable() {
        @Override
        public void run() {
            mUserNameEditText.setText("");
            mActivity.showSoftKeyboard(mUserNameEditText);
        }
    };

    public class PasswordLoginTask extends AsyncTask<String, Void, Boolean> {
        AirbitzException mFailureException = null;
        Account mAccount;

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            mUsername = (String) params[0];
            mPassword = (String) params[1];
            String secret = (String) params[2];
            try {
                mAccount = mCoreAPI.passwordLogin(mUsername, mPassword, secret);
                AirbitzApplication.Login(mAccount);
                return true;
            } catch (AirbitzException e) {
                Log.e(TAG, "", e);
                mFailureException = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mActivity.showModalProgress(false);
            mPasswordLoginTask = null;
            signInComplete(mAccount, mFailureException);
        }

        @Override
        protected void onCancelled() {
            mPasswordLoginTask = null;
            mActivity.ShowFadingDialog(getResources().getString(R.string.activity_navigation_signin_failed_unexpected));
        }
    }

    private void signInComplete(Account account, AirbitzException error) {
        saveUsername(mUsername);
        if (error == null) {
            mActivity.hideSoftKeyboard(mPasswordEditText);
            mPassword = mPasswordEditText.getText().toString();
            mPasswordEditText.setText("");
            mActivity.LoginNow(account, mFirstLogin);
        } else if (error.isOtpError()) {
            AirbitzApplication.setOtpError(true);
            AirbitzApplication.setOtpResetDate(error.otpResetDate());
            AirbitzApplication.setOtpResetToken(error.otpResetToken());
            launchTwoFactorMenu();
        } else {
            mActivity.ShowFadingDialog(Common.errorMap(mActivity, error));
        }
    }

    private boolean isFirstLogin() {
        return !mCoreAPI.accountExistsLocal(mUsername);
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
        mFirstLogin = isFirstLogin();
        if (mPinLoginMode) {
            mPINLoginTask = new PINLoginTask();
            mPINLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUsername, mPin, secret);
        } else {
            mPasswordLoginTask = new PasswordLoginTask();
            mPasswordLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUsername, mPassword, secret);
        }
    }

    private void abortPermanently() {
        refreshView(false, false); // reset to password view
    }

    private void saveUsername(String name) {
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
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for empty username.
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_invalid_credentials));
            focusView = mUserNameEditText;
            cancel = true;
        }

        // Check for empty password.
        if (password.length() < 1) {
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
            mFirstLogin = isFirstLogin();
            mPasswordLoginTask = new PasswordLoginTask();
            mPasswordLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username, password, null);
        }
    }

    public class GetRecoveryQuestionsTask extends AsyncTask<String, Void, String[]> {
        AirbitzException mFailureException;
        int mRecoveryType;
        String mRecoveryToken = "";
        String mUsername = "";

        @Override
        public void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected String[] doInBackground(String... params) {
                String username = params[0];
                String recoveryToken = params[1];

            try {
                if (recoveryToken == null || recoveryToken.length() == 0)
                    recoveryToken = mCoreAPI.getRecovery2Token(username);
            } catch (AirbitzException e) {

            }

            try {
                if (recoveryToken != null && recoveryToken.length() > 0) {
                    // Recovery 2.0
                    mRecoveryType = PasswordRecoveryFragment.RECOVERY_TYPE_2;
                    mRecoveryToken = recoveryToken;
                    mUsername = username;
                    return mCoreAPI.getRecovery2Questions(username, recoveryToken);

                } else {
                    // Recovery 1.0
                    mRecoveryType = PasswordRecoveryFragment.RECOVERY_TYPE_1;
                    return mCoreAPI.recoveryQuestions(username);
                }
            } catch (AirbitzException e) {
                AirbitzCore.logi("GetRecoveryQuestionsTask error:");
                mFailureException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] questions) {
            mActivity.showModalProgress(false);
            mRecoveryQuestionsTask = null;

            if (mFailureException != null || questions == null || questions.length == 0) {
                new MaterialDialog.Builder(mActivity)
                        .title(R.string.activity_recovery_title)
                        .content(R.string.recovery_not_setup)
                        .positiveText(R.string.string_next)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new MaterialDialog.Builder(mActivity)
                                        .title(R.string.activity_recovery_title)
                                        .content(R.string.recovery_not_setup2)
                                        .positiveText(R.string.string_next)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                new MaterialDialog.Builder(mActivity)
                                                        .title(R.string.activity_recovery_title)
                                                        .content(R.string.recovery_not_setup3)
                                                        .positiveText(R.string.string_done)
                                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            }
                                                        }).show();
                                            }
                                        }).show();
                            }
                        }).show();


            } else { // Some message or questions
                mActivity.startRecoveryQuestions(questions, mUsername, mRecoveryType, mRecoveryToken);
            }
        }

        @Override
        protected void onCancelled() {
            mRecoveryQuestionsTask = null;
            mActivity.showModalProgress(false);
        }

    }
}
