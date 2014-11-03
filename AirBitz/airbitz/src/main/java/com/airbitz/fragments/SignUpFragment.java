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

package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.airbitz.api.SWIGTYPE_p_void;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_PasswordRule;
import com.airbitz.api.tABC_RequestResults;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created on 2/10/14.
 */
public class SignUpFragment extends Fragment implements NavigationActivity.OnBackPress {
    public static final int DOLLAR_CURRENCY_NUMBER = 840;
    public static final int MIN_PIN_LENGTH = 4;
    public static String MODE = "com.airbitz.signup.mode";
    public static int SIGNUP = 0;
    private int mMode = SIGNUP;
    public static int CHANGE_PASSWORD = 1;
    public static int CHANGE_PASSWORD_VIA_QUESTIONS = 2;
    public static int CHANGE_PIN = 3;
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
    private ImageView mSwitchImage1;
    private ImageView mSwitchImage2;
    private ImageView mSwitchImage3;
    private ImageView mSwitchImage4;
    private TextView mTimeTextView;
    private View mUserNameRedRingCover;
    private CreateAccountTask mCreateAccountTask;
    private CreateFirstWalletTask mCreateFirstWalletTask;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
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

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.activity_signup_parent_layout);
        mNextButton = (Button) mView.findViewById(R.id.activity_signup_next_button);

        mUserNameRedRingCover = mView.findViewById(R.id.activity_signup_username_redring);

        mUserNameEditText = (EditText) mView.findViewById(R.id.activity_signup_username_edittext);
        mPasswordEditText = (EditText) mView.findViewById(R.id.activity_signup_password_edittext);
        mPasswordConfirmationEditText = (EditText) mView.findViewById(R.id.activity_signup_repassword_edittext);
        mWithdrawalPinEditText = (EditText) mView.findViewById(R.id.activity_signup_withdrawal_edittext);
        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mHintTextView = (TextView) mView.findViewById(R.id.activity_signup_password_help);
        mWithdrawalLabel = (TextView) mView.findViewById(R.id.activity_signup_withdrawal_textview);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
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
        mTimeTextView.setTypeface(NavigationActivity.latoRegularTypeFace);

        mPopupContainer = (LinearLayout) mView.findViewById(R.id.activity_signup_popup_layout);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    LayoutTransition lt = new LayoutTransition();
                    Animator animator = ObjectAnimator.ofFloat(null, "translationY", -(getResources().getDimension(R.dimen.activity_signup_popup_height)), 0);
                    lt.setAnimator(LayoutTransition.APPEARING, animator);
                    lt.setStartDelay(LayoutTransition.APPEARING, 0);
                    lt.setDuration(300);
                    mParentLayout.setLayoutTransition(lt);
                    mPopupContainer.setVisibility(View.VISIBLE);
                } else {
                    LayoutTransition lt = new LayoutTransition();
                    Animator animator = ObjectAnimator.ofFloat(null, "translationY", 0, -(getResources().getDimension(R.dimen.activity_signup_popup_height)));
                    lt.setAnimator(LayoutTransition.DISAPPEARING, animator);
                    lt.setStartDelay(LayoutTransition.DISAPPEARING, 0);
                    lt.setDuration(300);
                    mParentLayout.setLayoutTransition(lt);
                    mPopupContainer.setVisibility(View.GONE);
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

    private void setupUI(Bundle bundle) {
        if (bundle == null)
            return;
        //Hide some elements if this is not a fresh signup
        mMode = bundle.getInt(MODE);
        if (mMode == SIGNUP) {
            mTitleTextView.setText(R.string.activity_signup_title);
        } else if (mMode == CHANGE_PASSWORD) {
            // Reuse mUserNameEditText for old mPassword too
            mUserNameEditText.setHint(getResources().getString(R.string.activity_signup_old_password));
            mUserNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordEditText.setHint(getResources().getString(R.string.activity_signup_new_password));
            mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordConfirmationEditText.setHint(getResources().getString(R.string.activity_signup_new_password_confirm));
            mPasswordConfirmationEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mNextButton.setText(getResources().getString(R.string.string_done));
            // change title
            mTitleTextView.setText(R.string.activity_signup_title_change_password);
            // hide PIN
            mWithdrawalPinEditText.setVisibility(View.GONE);
            mWithdrawalLabel.setVisibility(View.GONE);
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
            mUserNameEditText.setVisibility(View.INVISIBLE);
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
            String description = pRule.getSzDescription();
            if (!passed) {
                bNewPasswordFieldsAreValid = false;
            }
            //TODO variable length list of items instead of fixed # of items
            int resource = passed ? R.drawable.green_check : R.drawable.red_x;
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
            if (!AirbitzApplication.getPassword().equals(mUserNameEditText.getText().toString())) {
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

    public void ShowMessageDialogChangeSuccess(String title, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(reason)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mActivity.popFragment();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI(getArguments());
        mUserNameEditText.requestFocus();
    }

    public class ChangeTask extends AsyncTask<String, Void, Boolean> {
        tABC_CC success;

        String mUsername;
        char[] mPassword;

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
            mCoreAPI.stopAllAsyncUpdates();
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
                        mWithdrawalPinEditText.getText().toString());
            } else {
                mCoreAPI.SetUserPIN(mWithdrawalPinEditText.getText().toString());
                success = tABC_CC.ABC_CC_Ok;
            }

            return success == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mActivity.showModalProgress(false);
            mChangeTask = null;
            if (success) {
                if (mMode == CHANGE_PASSWORD) {
                    AirbitzApplication.Login(mUsername, mPassword);
                    ShowMessageDialogChangeSuccess(getResources().getString(R.string.activity_signup_password_change_title), getResources().getString(R.string.activity_signup_password_change_good));
                } else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    AirbitzApplication.Login(mUsername, mPassword);
                    mActivity.UserJustLoggedIn();
                    mActivity.clearBD();
                    mActivity.switchFragmentThread(NavigationActivity.Tabs.SETTING.ordinal());
                } else {
                    ShowMessageDialogChangeSuccess(getResources().getString(R.string.activity_signup_pin_change_title), getResources().getString(R.string.activity_signup_pin_change_good));
                }
            } else {
                if (mMode == CHANGE_PASSWORD || mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_password_change_title), getResources().getString(R.string.activity_signup_password_change_bad));
                } else {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_pin_change_title), getResources().getString(R.string.activity_signup_pin_change_bad));
                }
            }
            mCoreAPI.startAllAsyncUpdates();
        }

        @Override
        protected void onCancelled() {
            mChangeTask = null;
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
        tABC_RequestResults pData = new tABC_RequestResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pData);
        private String mFailureReason;

        CreateAccountTask(String email, char[] password, String pin) {
            mUsername = email;
            mPassword = password;
            mPin = pin;
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            tABC_CC code = core.ABC_CreateAccount(mUsername, String.valueOf(mPassword), mPin, null, pVoid, pError);
            mFailureReason = pError.getSzDescription();
            return code == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateAccountTask = null;
            mActivity.showModalProgress(false);
            if (success) {
                AirbitzApplication.Login(mUsername, mPassword);
                mCreateFirstWalletTask = new CreateFirstWalletTask();
                mCreateFirstWalletTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
            } else {
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), mFailureReason);
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAccountTask = null;
            mActivity.showModalProgress(false);
        }
    }

    /**
     * Represents an asynchronous creation of the first wallet
     */
    public class CreateFirstWalletTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername, mPin;
        private final char[] mPassword;

        CreateFirstWalletTask() {
            mUsername = mUserNameEditText.getText().toString();
            Editable pass = mPasswordEditText.getText();
            mPassword = new char[pass.length()];
            pass.getChars(0, pass.length(), mPassword, 0);
            mPin = mWithdrawalPinEditText.getText().toString();
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String walletName = getResources().getString(R.string.activity_recovery_first_wallet_name);
            return mCoreAPI.createWallet(mUsername, String.valueOf(mPassword), walletName, DOLLAR_CURRENCY_NUMBER);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateFirstWalletTask = null;
            mActivity.showModalProgress(false);
            if (!success) {
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_create_wallet_fail));
            } else {
                CreateDefaultCategories();

                AirbitzApplication.Login(mUsername, mPassword);
                mCoreAPI.SetUserPIN(mPin);

                mCoreAPI.setupAccountSettings();
                mCoreAPI.startAllAsyncUpdates();

                mCoreAPI.coreSettings().setRecoveryReminderCount(0); // set reminder count
                mCoreAPI.saveAccountSettings(mCoreAPI.coreSettings());

                ((NavigationActivity)getActivity()).popFragment();
                ((NavigationActivity)getActivity()).DisplayLoginOverlay(false);
                ((NavigationActivity)getActivity()).switchFragmentThread(NavigationActivity.Tabs.WALLET.ordinal());
            }
        }

        @Override
        protected void onCancelled() {
            mCreateFirstWalletTask = null;
            mActivity.showModalProgress(false);
        }
    }

    private void CreateDefaultCategories() {
        String[] defaults = getResources().getStringArray(R.array.category_defaults);

        for (String cat : defaults)
            mCoreAPI.addCategory(cat);

        List<String> cats = mCoreAPI.loadCategories();
        if (cats.size() == 0 || cats.get(0).equals(defaults)) {
            Log.d(TAG, "Category creation failed");
        }
    }

}
