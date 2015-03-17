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
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.core;
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_PasswordRule;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.settings.PasswordRecoveryFragment;
import com.airbitz.utils.Common;

import java.util.List;

/**
 * Created on 2/10/14.
 */
public class SignUpFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    public static final int MIN_PIN_LENGTH = 4;
    public static String MODE = "com.airbitz.signup.mode";
    public static int SIGNUP = 0;
    public static int CHANGE_PASSWORD = 1;
    public static int CHANGE_PASSWORD_VIA_QUESTIONS = 2;
    public static int CHANGE_PIN = 3;
    private int mMode = SIGNUP;
    private final String TAG = getClass().getSimpleName();
    private RelativeLayout mParentLayout;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmationEditText;
    private EditText mPasswordForPINEditText;
    private EditText mWithdrawalPinEditText;
    private TextView mWithdrawalLabel;
    private TextView mHintTextView;
    private Button mNextButton;
    private boolean mGoodPassword = false;
    private TextView mTitleTextView;
    private LinearLayout mPopupContainer;
    private View mWidgetContainer;
    private ImageView mSwitchImage1;
    private ImageView mSwitchImage2;
    private ImageView mSwitchImage3;
    private ImageView mSwitchImage4;
    private TextView mTimeTextView;
    private View mUserNameRedRingCover;
    private CreateAccountTask mCreateAccountTask;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
    private Handler mHandler = new Handler();

    /**
     * Represents an asynchronous account creation task
     */
    private ChangeTask mChangeTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_signup, container, false);
        } else {

            return mView;
        }

        mActivity = (NavigationActivity) getActivity();

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.activity_signup_parent_layout);
        mNextButton = (Button) mView.findViewById(R.id.activity_signup_next_button);

        mUserNameRedRingCover = mView.findViewById(R.id.activity_signup_username_redring);

        mUserNameEditText = (EditText) mView.findViewById(R.id.activity_signup_username_edittext);
        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_setup_password_edittext);
        mPasswordConfirmationEditText = (EditText) mView.findViewById(R.id.activity_signup_repassword_edittext);
        mWithdrawalPinEditText = (EditText) mView.findViewById(R.id.activity_signup_withdrawal_edittext);
        mHintTextView = (TextView) mView.findViewById(R.id.activity_signup_password_help);
        mWithdrawalLabel = (TextView) mView.findViewById(R.id.activity_signup_withdrawal_textview);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.activity_signup_title);

        mUserNameEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mHintTextView.setTypeface(NavigationActivity.latoRegularTypeFace);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPasswordConfirmationEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mWithdrawalLabel.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mWithdrawalPinEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        ImageButton mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mSwitchImage1 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_1);
        mSwitchImage2 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_2);
        mSwitchImage3 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_3);
        mSwitchImage4 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_4);

        mTimeTextView = (TextView) mView.findViewById(R.id.activity_signup_time_textview);

        mPopupContainer = (LinearLayout) mView.findViewById(R.id.activity_signup_popup_layout);
        mWidgetContainer = mView.findViewById(R.id.fragment_signup_widget_container);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNextButton.setClickable(false);
                goNext();
            }
        });

        mWithdrawalPinEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 4) {
                    mActivity.hideSoftKeyboard(mWithdrawalPinEditText);
                    mParentLayout.requestFocus();
                }
            }
        };
        mWithdrawalPinEditText.addTextChangedListener(mPINTextWatcher);

        mUserNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (mUserNameEditText.getText().toString().length() < 3 || mUserNameEditText.getText().toString().trim().length() < 3) {
                    mUserNameRedRingCover.setVisibility(View.VISIBLE);
                } else {
                    mUserNameRedRingCover.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                String password = mPasswordEditText.getText().toString();
                mGoodPassword = checkPasswordRules(password);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mHandler.post(animatePopupDown);
                } else {
                    mHandler.post(animatePopupUp);
                }
            }
        });

        mUserNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mUserNameEditText);
                }
            }
        });

        return mView;
    }

    Animator.AnimatorListener endListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animator) {
            mPopupContainer.setVisibility(View.GONE);
        }

        @Override public void onAnimationCancel(Animator animator) { }
        @Override public void onAnimationStart(Animator animator) { }
        @Override public void onAnimationRepeat(Animator animator) { }
    };

    Runnable animatePopupDown = new Runnable() {
        @Override
        public void run() {
            if(mPopupContainer != null && mWidgetContainer != null && isAdded()) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mPopupContainer, "translationY",
                        -getResources().getDimension(R.dimen.activity_signup_popup_height), 0);
                ObjectAnimator animatorWidget = ObjectAnimator.ofFloat(mWidgetContainer, "translationY",
                        0, getResources().getDimension(R.dimen.activity_signup_popup_height));
                if(animator != null) {
                    animator.setDuration(300);
                    animator.start();
                }
                if(animatorWidget != null && mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    animatorWidget.setDuration(300);
                    animatorWidget.start();
                }
                mPopupContainer.setVisibility(View.VISIBLE);
            }
        }
    };

    Runnable animatePopupUp = new Runnable() {
        @Override
        public void run() {
            if(mPopupContainer != null && mWidgetContainer != null && isAdded()) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mPopupContainer, "translationY",
                        0, -getResources().getDimension(R.dimen.activity_signup_popup_height));
                ObjectAnimator animatorWidget = ObjectAnimator.ofFloat(mWidgetContainer, "translationY",
                        getResources().getDimension(R.dimen.activity_signup_popup_height), 0);
                if(animator != null) {
                    animator.setDuration(300);
                    animator.addListener(endListener);
                    animator.start();
                }
                if(animatorWidget != null && mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    animatorWidget.setDuration(300);
                    animatorWidget.start();
                }
            }
        }
    };

    private void setupUI(Bundle bundle) {
        if (bundle == null)
            return;
        //Hide some elements if this is not a fresh signup
        mMode = bundle.getInt(MODE);
        if (mMode == CHANGE_PASSWORD) {
            // Reuse mUserNameEditText for old mPassword too
            mUserNameEditText.setHint(getResources().getString(R.string.activity_signup_old_password));
            mUserNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mUserNameRedRingCover.setVisibility(View.GONE);
            mPasswordEditText.setHint(getResources().getString(R.string.activity_signup_new_password));
            mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordConfirmationEditText.setHint(getResources().getString(R.string.activity_signup_new_password_confirm));
            mPasswordConfirmationEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordConfirmationEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mNextButton.setText(getResources().getString(R.string.string_done));
            // change title
            mTitleTextView.setText(R.string.activity_signup_title_change_password);
            // hide PIN
            mWithdrawalPinEditText.setVisibility(View.GONE);
            mWithdrawalLabel.setVisibility(View.GONE);
            mUserNameEditText.requestFocus();
        } else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
            // change mUsername label, title
            mTitleTextView.setText(R.string.activity_signup_title_change_password_via_questions);
            mPasswordEditText.setHint(getResources().getString(R.string.activity_signup_new_password));
            mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordConfirmationEditText.setHint(getResources().getString(R.string.activity_signup_new_password_confirm));
            mPasswordConfirmationEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mNextButton.setText(getResources().getString(R.string.string_done));
            // hide mUsername
            mUserNameRedRingCover.setVisibility(View.GONE);
            mUserNameEditText.setVisibility(View.GONE);
            mHintTextView.setVisibility(View.INVISIBLE);
        } else if (mMode == CHANGE_PIN) {
            // hide both mPassword fields
            mPasswordForPINEditText = mUserNameEditText;
            mPasswordForPINEditText.setHint(getResources().getString(R.string.activity_signup_password_hint));
            mPasswordForPINEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordEditText.setVisibility(View.GONE);
            mPasswordConfirmationEditText.setVisibility(View.GONE);
            mWithdrawalPinEditText.setHint(getResources().getString(R.string.activity_signup_new_pin));
            mNextButton.setText(getResources().getString(R.string.string_done));
            // change title
            mTitleTextView.setText(R.string.activity_signup_title_change_pin);
            mUserNameEditText.requestFocus();
        } else if (mMode == SIGNUP) {
            mUserNameEditText.setText(bundle.getString(PasswordRecoveryFragment.USERNAME));
            mUserNameEditText.requestFocus();
            mUserNameEditText.setSelection(mUserNameEditText.getText().length());
        }
    }

    // checks the mPassword against the mPassword rules
    // returns YES if new mPassword fields are good, NO if the new mPassword fields failed the checks
    // if the new mPassword fields are bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
    private boolean checkPasswordRules(String password) {
        List<tABC_PasswordRule> rules = mCoreAPI.GetPasswordRules(password);

        if (rules.isEmpty()) {
            return false;
        }

        boolean bNewPasswordFieldsAreValid = true;
        for (int i = 0; i < rules.size(); i++) {
            tABC_PasswordRule pRule = rules.get(i);
            boolean passed = pRule.getBPassed();
            if (!passed) {
                bNewPasswordFieldsAreValid = false;
            }

            int resource = passed ? R.drawable.green_check : R.drawable.white_dot;
            switch (i) {
                case 0:
                    mSwitchImage1.setImageResource(resource);
                    break;
                case 1:
                    mSwitchImage2.setImageResource(resource);
                    break;
                case 2:
                    mSwitchImage3.setImageResource(resource);
                    break;
                case 3:
                    mSwitchImage4.setImageResource(resource);
                    break;
                default:
                    break;
            }
        }
        mTimeTextView.setText(GetCrackString(mCoreAPI.GetPasswordSecondsToCrack(password)));
        return bNewPasswordFieldsAreValid;
    }

    private void goNext() {
        mActivity.hideSoftKeyboard(mParentLayout);
        // if they entered a valid mUsername or old mPassword
        if (userNameFieldIsValid() && newPasswordFieldsAreValid() && pinFieldIsValid()) {
            // if we are signing up a new account
            if (mMode == SIGNUP) {
                attemptSignUp();
            } else {
                if (mChangeTask == null) {
                    mChangeTask = new ChangeTask();
                    mChangeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getArguments().getString(PasswordRecoveryFragment.QUESTIONS),
                            getArguments().getString(PasswordRecoveryFragment.USERNAME));
                }
            }
        }
        mNextButton.setClickable(true);
    }

    // checks the mUsername field (non-blank or matches old mPassword depending on the mode)
    // returns YES if field is good
    // if the field is bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
    private boolean userNameFieldIsValid() {
        boolean bUserNameFieldIsValid = true;

        // if we are signing up for a new account
        if (mMode == SIGNUP) {
            // if nothing was entered
            if (mUserNameEditText.getText().toString().length() == 0) {
                bUserNameFieldIsValid = false;
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_enter_username));
            }
        } else if (mMode != CHANGE_PASSWORD_VIA_QUESTIONS) // the user name field is used for the old mPassword in this case
        {
            // if the mPassword is wrong
            if (!mCoreAPI.PasswordOK(AirbitzApplication.getUsername(), mUserNameEditText.getText().toString())) {
                bUserNameFieldIsValid = false;
                if (mMode == CHANGE_PIN) {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_change_pin_failed), getResources().getString(R.string.activity_signup_incorrect_password));
                } else if (mMode == CHANGE_PASSWORD) {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_change_password_failed), getResources().getString(R.string.activity_signup_incorrect_password));
                } else {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_incorrect_password));
                }
            }
        }
        return bUserNameFieldIsValid;
    }

    // checks the mPassword against the mPassword rules
    // returns YES if new mPassword fields are good, NO if the new mPassword fields failed the checks
    // if the new mPassword fields are bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
    private boolean newPasswordFieldsAreValid() {
        boolean bNewPasswordFieldsAreValid = true;

        // if we are signing up for a new account or changing our mPassword
        if ((mMode != CHANGE_PIN)) {
            if (!mGoodPassword) {
                bNewPasswordFieldsAreValid = false;
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_insufficient_password));
            } else if (!mPasswordConfirmationEditText.getText().toString().equals(mPasswordEditText.getText().toString())) {
                bNewPasswordFieldsAreValid = false;
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_passwords_dont_match));
            }
        }

        return bNewPasswordFieldsAreValid;
    }

    // checks the pin field
    // returns YES if field is good
    // if the field is bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
    private boolean pinFieldIsValid() {
        boolean bpinNameFieldIsValid = true;

        // if we are signing up for a new account
        if (mMode != CHANGE_PASSWORD) {
            // if the pin isn't long enough
            if (mWithdrawalPinEditText.getText().toString().length() < MIN_PIN_LENGTH) {
                bpinNameFieldIsValid = false;
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_insufficient_pin));
            }
        }

        return bpinNameFieldIsValid;
    }

    private String GetCrackString(double secondsToCrack) {
        String crackString = getResources().getString(R.string.activity_signup_time_to_crack);
        if (secondsToCrack < 60.0) {
            crackString += String.format("%.2f ", secondsToCrack);
            crackString += getResources().getString(R.string.activity_signup_seconds);
        } else if (secondsToCrack < 3600) {
            crackString += String.format("%.2f ", secondsToCrack / 60.0);
            crackString += getResources().getString(R.string.activity_signup_minutes);
        } else if (secondsToCrack < 86400) {
            crackString += String.format("%.2f ", secondsToCrack / 3600.0);
            crackString += getResources().getString(R.string.activity_signup_hours);
        } else if (secondsToCrack < 604800) {
            crackString += String.format("%.2f ", secondsToCrack / 86400.0);
            crackString += getResources().getString(R.string.activity_signup_days);
        } else if (secondsToCrack < 2419200) {
            crackString += String.format("%.2f ", secondsToCrack / 604800.0);
            crackString += getResources().getString(R.string.activity_signup_weeks);
        } else if (secondsToCrack < 29030400) {
            crackString += String.format("%.2f ", secondsToCrack / 2419200.0);
            crackString += getResources().getString(R.string.activity_signup_months);
        } else {
            crackString += String.format("%.2f ", secondsToCrack / 29030400.0);
            crackString += getResources().getString(R.string.activity_signup_years);
        }
        return crackString;
    }

    @Override
    public boolean onBackPress() {
        mActivity.hideSoftKeyboard(getView());
        if (mMode == SIGNUP)
            mActivity.noSignup();
        else
            mActivity.popFragment();
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptSignUp() {
        if (mCreateAccountTask != null) {
            return;
        }

        String username = mUserNameEditText.getText().toString();
        Editable pass = mPasswordEditText.getText();
        char[] password = new char[pass.length()];
        pass.getChars(0, pass.length(), password, 0);
        String pin = mWithdrawalPinEditText.getText().toString();

        // Reset errors.
        mPasswordEditText.setError(null);
        mUserNameEditText.setError(null);
        mPasswordConfirmationEditText.setError(null);
        mWithdrawalPinEditText.setError(null);

        mCreateAccountTask = new CreateAccountTask(username, password, pin);
        mCreateAccountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private AlertDialog mAlertSuccess = null;
    public void ShowMessageDialogChangeSuccess(String title, String reason) {
        if(mAlertSuccess == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
            builder.setMessage(reason)
                    .setTitle(title)
                    .setCancelable(false)
                    .setNeutralButton(getResources().getString(R.string.string_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mAlertSuccess.dismiss();
                                    mActivity.popFragment();
                                    mAlertSuccess = null;
                                }
                            }
                    );
            mAlertSuccess = builder.create();
            mAlertSuccess.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI(getArguments());
    }

    public class ChangeTask extends AsyncTask<String, Void, Boolean> {
        tABC_CC success;

        String mUsername;
        char[] mPassword;
        String mPin;

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
            mCoreAPI.stopAllAsyncUpdates();
            mPin = mWithdrawalPinEditText.getText().toString();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String answers = params[0];
            mUsername = params[1];
            Editable pass = mPasswordEditText.getText();
            mPassword = new char[pass.length()];
            pass.getChars(0, pass.length(), mPassword, 0);
            if (mMode == CHANGE_PASSWORD) {
                mUsername = AirbitzApplication.getUsername();
                success = mCoreAPI.ChangePassword(String.valueOf(mPassword));
            } else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                success = mCoreAPI.ChangePasswordWithRecoveryAnswers(mUsername, answers, String.valueOf(mPassword),
                        mPin);
            } else {
                success = mCoreAPI.SetPin(mPin);
                if(!mCoreAPI.coreSettings().getBDisablePINLogin()) {
                    mCoreAPI.PinSetup(AirbitzApplication.getUsername(), mPin);
                }
            }

            return success == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                if (mMode == CHANGE_PASSWORD) {
                    AirbitzApplication.Login(mUsername, mPassword);
                    mCoreAPI.PinSetup(AirbitzApplication.getUsername(), mCoreAPI.coreSettings().getSzPIN());
                    mActivity.showModalProgress(false);
                    ShowMessageDialogChangeSuccess(getResources().getString(R.string.activity_signup_password_change_title), getResources().getString(R.string.activity_signup_password_change_good));
                } else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    AirbitzApplication.Login(mUsername, mPassword);
                    mCoreAPI.SetPin(mPin);
                    mCoreAPI.PinSetup(AirbitzApplication.getUsername(), mPin);
                    mActivity.UserJustLoggedIn(false);
                    mActivity.clearBD();
                    mActivity.showModalProgress(false);
                    mActivity.switchFragmentThread(NavigationActivity.Tabs.MORE.ordinal());
                } else {
                    mActivity.showModalProgress(false);
                    ShowMessageDialogChangeSuccess(getResources().getString(R.string.activity_signup_pin_change_title), getResources().getString(R.string.activity_signup_pin_change_good));
                }
            } else {
                mActivity.showModalProgress(false);
                if (mMode == CHANGE_PASSWORD || mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_password_change_title), getResources().getString(R.string.activity_signup_password_change_bad));
                } else {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_pin_change_title), getResources().getString(R.string.activity_signup_pin_change_bad));
                }
                mNextButton.setClickable(true);
            }
            mCoreAPI.startAllAsyncUpdates();
            mChangeTask = null;
        }

        @Override
        protected void onCancelled() {
            mChangeTask = null;
            mNextButton.setClickable(true);
            mCoreAPI.startAllAsyncUpdates();
            mActivity.showModalProgress(false);
        }
    }

    /**
     * Represents an asynchronous account creation task
     */
    public class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final char[] mPassword;
        private final String mPin;
        tABC_Error pError = new tABC_Error();
        private String mFailureReason;

        CreateAccountTask(String email, char[] password, String pin) {
            mUsername = email;
            mPassword = password;
            mPin = pin;
            mActivity.ShowFadingDialog(getString(R.string.fragment_signup_creating_account), 2000000, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            tABC_CC code =
                core.ABC_CreateAccount(mUsername, String.valueOf(mPassword), pError);
            mFailureReason = Common.errorMap(mActivity, code);
            return code == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateAccountTask = null;
            if (success) {
                AirbitzApplication.Login(mUsername, mPassword);
                mCoreAPI.SetPin(mPin);

                mCoreAPI.setupAccountSettings();
                mCoreAPI.startAllAsyncUpdates();

                tABC_AccountSettings settings = mCoreAPI.coreSettings();
                settings.setRecoveryReminderCount(0);
                mCoreAPI.saveAccountSettings(settings);

                mActivity.UserJustLoggedIn(true);
            } else {
                mActivity.ShowFadingDialog(mFailureReason);
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAccountTask = null;
            mActivity.DismissFadingDialog();
        }
    }
}
