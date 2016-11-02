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
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore.Constants;
import co.airbitz.core.AirbitzCore.PasswordRulesCheck;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Settings;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.settings.PasswordRecoveryFragment;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/10/14.
 */
public class SignUpFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    public static final int MIN_PIN_LENGTH = 4;
    public static String MODE = "com.airbitz.signup.mode";
    public static int CHANGE_PASSWORD = 1;
    public static int CHANGE_PASSWORD_VIA_QUESTIONS = 2;
    public static int CHANGE_PIN = 3;
    public static int CHANGE_PASSWORD_NO_VERIFY = 4;
    private int mMode = 0;
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
    private LinearLayout mPopupContainer;
    private View mWidgetContainer;
    private ImageView mSwitchImage1;
    private TextView mSwitchText1;
    private ImageView mSwitchImage2;
    private TextView mSwitchText2;
    private ImageView mSwitchImage3;
    private TextView mSwitchText3;
    private ImageView mSwitchImage4;
    private TextView mSwitchText4;
    private TextView mTimeTextView;
    private AirbitzCore mCoreAPI;
    private Account mAccount;
    private View mView;
    private Handler mHandler = new Handler();

    private ChangeTask mChangeTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = AirbitzCore.getApi();
        mAccount = AirbitzApplication.getAccount();
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
            mView = i.inflate(R.layout.fragment_signup, container, false);
        } else {
            return mView;
        }

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.activity_signup_parent_layout);
        mNextButton = (Button) mView.findViewById(R.id.activity_signup_next_button);

        mUserNameEditText = (EditText) mView.findViewById(R.id.activity_signup_username_edittext);
        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_setup_password_edittext);
        mPasswordConfirmationEditText = (EditText) mView.findViewById(R.id.activity_signup_repassword_edittext);
        mWithdrawalPinEditText = (EditText) mView.findViewById(R.id.activity_signup_withdrawal_edittext);
        mHintTextView = (TextView) mView.findViewById(R.id.activity_signup_password_help);
        mWithdrawalLabel = (TextView) mView.findViewById(R.id.activity_signup_withdrawal_textview);

        mSwitchImage1 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_1);
        mSwitchText1 = (TextView) mView.findViewById(R.id.activity_signup_switch_text_1);
        mSwitchImage2 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_2);
        mSwitchText2 = (TextView) mView.findViewById(R.id.activity_signup_switch_text_2);
        mSwitchImage3 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_3);
        mSwitchText3 = (TextView) mView.findViewById(R.id.activity_signup_switch_text_3);
        mSwitchImage4 = (ImageView) mView.findViewById(R.id.activity_signup_switch_image_4);
        mSwitchText4 = (TextView) mView.findViewById(R.id.activity_signup_switch_text_4);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBackPress();
            default:
                return super.onOptionsItemSelected(item);
        }
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
                if(animatorWidget != null && (mMode == CHANGE_PASSWORD_VIA_QUESTIONS || mMode == CHANGE_PASSWORD_NO_VERIFY)) {
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
                if(animatorWidget != null && (mMode == CHANGE_PASSWORD_VIA_QUESTIONS || mMode == CHANGE_PASSWORD_NO_VERIFY)) {
                    animatorWidget.setDuration(300);
                    animatorWidget.start();
                }
            }
        }
    };

    @Override
    public String getTitle() {
        mMode = getArguments().getInt(MODE);
        if (mMode == CHANGE_PASSWORD_NO_VERIFY || (mMode == CHANGE_PASSWORD && !mAccount.passwordExists())) {
            return mActivity.getString(R.string.activity_signup_title_change_password);
        } else if (mMode == CHANGE_PASSWORD) {
            return mActivity.getString(R.string.activity_signup_title_change_password);
        } else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
            return mActivity.getString(R.string.activity_signup_title_change_password_via_questions);
        } else if (mMode == CHANGE_PIN) {
            return mActivity.getString(R.string.activity_signup_title_change_pin);
        }
        return mActivity.getString(R.string.activity_signup_title);
    }

    private void setupUI(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        mMode = bundle.getInt(MODE);
        if (mMode == CHANGE_PASSWORD_NO_VERIFY || (mMode == CHANGE_PASSWORD && !mAccount.passwordExists())) {
            // hide mUsername
            mUserNameEditText.setVisibility(View.GONE);
            mHintTextView.setVisibility(View.INVISIBLE);

            mPasswordEditText.setHint(getResources().getString(R.string.activity_signup_new_password));
            mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordEditText.setTypeface(Typeface.DEFAULT);
            mPasswordConfirmationEditText.setHint(getResources().getString(R.string.activity_signup_new_password_confirm));
            mPasswordConfirmationEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordConfirmationEditText.setTypeface(Typeface.DEFAULT);
            mPasswordConfirmationEditText.setHint(getResources().getString(R.string.activity_signup_new_password_confirm));
            mPasswordConfirmationEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mNextButton.setText(getResources().getString(R.string.string_done));
            // hide PIN
            mWithdrawalPinEditText.setVisibility(View.GONE);
            mWithdrawalLabel.setVisibility(View.GONE);
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    mPasswordEditText.requestFocus();
                    mActivity.showSoftKeyboard(mPasswordEditText);
                }
            }, 1000);
        } else if (mMode == CHANGE_PASSWORD) {
            // change mUsername label, title
            mUserNameEditText.setHint(getResources().getString(R.string.activity_signup_old_password));
            mUserNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mUserNameEditText.setTypeface(Typeface.DEFAULT);
            mPasswordEditText.setHint(getResources().getString(R.string.activity_signup_new_password));
            mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordEditText.setTypeface(Typeface.DEFAULT);
            mPasswordConfirmationEditText.setHint(getResources().getString(R.string.activity_signup_new_password_confirm));
            mPasswordConfirmationEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordConfirmationEditText.setTypeface(Typeface.DEFAULT);
            mNextButton.setText(getResources().getString(R.string.string_done));
            // hide PIN
            mWithdrawalPinEditText.setVisibility(View.GONE);
            mWithdrawalLabel.setVisibility(View.GONE);
        }
        else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
            mPasswordEditText.setHint(getResources().getString(R.string.activity_signup_new_password));
            mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordEditText.setTypeface(Typeface.DEFAULT);
            mPasswordConfirmationEditText.setHint(getResources().getString(R.string.activity_signup_new_password_confirm));
            mPasswordConfirmationEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordConfirmationEditText.setTypeface(Typeface.DEFAULT);
            mNextButton.setText(getResources().getString(R.string.string_done));
            // hide mUsername
            mUserNameEditText.setVisibility(View.GONE);
            mHintTextView.setVisibility(View.INVISIBLE);
        } else if (mMode == CHANGE_PIN) {
            // hide both mPassword fields
            mUserNameEditText.setHint(getResources().getString(R.string.activity_signup_password));
            mUserNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mUserNameEditText.setTypeface(Typeface.DEFAULT);
            mUserNameEditText.setVisibility(mAccount.passwordExists() ? View.VISIBLE : View.GONE);
            if (!mAccount.hasPin()) {
                mUserNameEditText.setVisibility(View.GONE);
            }
            mPasswordForPINEditText = mUserNameEditText;
            mPasswordForPINEditText.setHint(getResources().getString(R.string.activity_signup_password));
            mPasswordForPINEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordForPINEditText.setTypeface(Typeface.DEFAULT);
            mHintTextView.setVisibility(View.GONE);
            mPasswordEditText.setVisibility(View.GONE);
            mPasswordConfirmationEditText.setVisibility(View.GONE);
            mWithdrawalPinEditText.setHint(getResources().getString(R.string.activity_signup_new_pin));
            mWithdrawalPinEditText.setTypeface(Typeface.DEFAULT);
            mNextButton.setText(getResources().getString(R.string.string_done));
            mUserNameEditText.requestFocus();
        }
    }

    // checks the mPassword against the mPassword rules
    // returns YES if new mPassword fields are good, NO if the new mPassword fields failed the checks
    // if the new mPassword fields are bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
    private boolean checkPasswordRules(String password) {
		PasswordRulesCheck rules = mCoreAPI.passwordRulesCheck(password);
		List<String> fails = new ArrayList<String>();

        Constants constants = AirbitzCore.getApi().constants();

		mSwitchImage1.setImageResource(!rules.tooShort ? R.drawable.green_check : R.drawable.white_dot);
		mSwitchText1.setText(String.format(getString(R.string.password_rule_too_short), constants.MIN_PASSWORD_LENGTH));
		if (rules.tooShort) {
			fails.add(String.format(mActivity.getString(R.string.password_rule_too_short), constants.MIN_PASSWORD_LENGTH));
		}

		mSwitchImage2.setImageResource(!rules.noNumber ? R.drawable.green_check : R.drawable.white_dot);
        mSwitchText2.setText(R.string.password_rule_no_number);
		if (rules.noNumber) {
			fails.add(mActivity.getString(R.string.password_rule_no_number));
		}

		mSwitchImage3.setImageResource(!rules.noUpperCase ? R.drawable.green_check : R.drawable.white_dot);
        mSwitchText3.setText(R.string.password_rule_no_uppercase);
		if (rules.noUpperCase) {
			fails.add(mActivity.getString(R.string.password_rule_no_uppercase));
		}

		mSwitchImage4.setImageResource(!rules.noLowerCase ? R.drawable.green_check : R.drawable.white_dot);
        mSwitchText4.setText(R.string.password_rule_no_lowercase);
		if (rules.noLowerCase) {
			fails.add(mActivity.getString(R.string.password_rule_no_lowercase));
		}

        mTimeTextView.setText(Common.getCrackString(mActivity, rules.secondsToCrack));
        return fails.isEmpty();
    }

    private void goNext() {
        mActivity.hideSoftKeyboard(mParentLayout);
        // if they entered a valid mUsername or old mPassword
        if (userNameFieldIsValid() && newPasswordFieldsAreValid() && pinFieldIsValid()) {
            // if we are signing up a new account
            if (mChangeTask == null) {
                    mChangeTask = new ChangeTask();
                    mChangeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            getArguments().getString(PasswordRecoveryFragment.USERNAME));
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
        if (mMode == CHANGE_PASSWORD_NO_VERIFY || !(mAccount.passwordExists() && (mMode == CHANGE_PASSWORD ||
            mMode == CHANGE_PIN))) {
            bUserNameFieldIsValid = true;
        }
        else if (mMode == CHANGE_PIN && !mAccount.hasPin())
        {
            bUserNameFieldIsValid = true;
        }
        else if (mMode != CHANGE_PASSWORD_VIA_QUESTIONS) // the user name field is used for the old mPassword in this case
        {
            // if the mPassword is wrong
            if (!mAccount.checkPassword(mUserNameEditText.getText().toString())) {
                bUserNameFieldIsValid = false;
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_incorrect_current_password));
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
        if (mMode != CHANGE_PASSWORD && mMode != CHANGE_PASSWORD_NO_VERIFY) {
            // if the pin isn't long enough
            if (mAccount.passwordExists() && mWithdrawalPinEditText.getText().toString().length() < MIN_PIN_LENGTH) {
                bpinNameFieldIsValid = false;
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_insufficient_pin));
            }
        }

        return bpinNameFieldIsValid;
    }

    @Override
    public boolean onBackPress() {
        mActivity.hideSoftKeyboard(getView());
        mActivity.popFragment();
        return true;
    }

    private Dialog mAlertSuccess = null;
    public void ShowMessageDialogChangeSuccess(String title, String reason) {
        if(mAlertSuccess == null) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
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
            mAlertSuccess = builder.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI(getArguments());
    }

    public class ChangeTask extends AsyncTask<String, Void, Boolean> {
        AirbitzException mFailureException;
        String mUsername;
        String mPassword;
        String mPin;

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
            mPin = mWithdrawalPinEditText.getText().toString();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            mAccount.stopBackgroundTasks();
            mUsername = params[0];
            mPassword = mPasswordEditText.getText().toString();
            try {
                if (mMode == CHANGE_PASSWORD || mMode == CHANGE_PASSWORD_NO_VERIFY) {
                    mUsername = AirbitzApplication.getUsername();
                    mAccount.passwordChange(mPassword);
                } else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    mAccount.passwordChange(mPassword);
                    mAccount.pin(mPin);
                } else {
                    mAccount.pin(mPin);
                }
            } catch (AirbitzException e) {
                mFailureException = e;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                if (mMode == CHANGE_PASSWORD || mMode == CHANGE_PASSWORD_NO_VERIFY) {
                    ShowMessageDialogChangeSuccess(getResources().getString(R.string.activity_signup_password_change_title), getResources().getString(R.string.activity_signup_password_change_good));
                    mActivity.onBackPressed();
                } else if (mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    mActivity.UserJustLoggedIn(false);
                    mActivity.clearBD();
                    mActivity.switchFragmentThread(NavigationActivity.Tabs.WALLET.ordinal());
                } else {
                    ShowMessageDialogChangeSuccess(getResources().getString(R.string.activity_signup_pin_change_title), getResources().getString(R.string.activity_signup_pin_change_good));
                    mActivity.onBackPressed();
                }
            } else {
                if (mMode == CHANGE_PASSWORD || mMode == CHANGE_PASSWORD_NO_VERIFY || mMode == CHANGE_PASSWORD_VIA_QUESTIONS) {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_password_change_title), getResources().getString(R.string.activity_signup_password_change_bad));
                } else {
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_pin_change_title), getResources().getString(R.string.activity_signup_pin_change_bad));
                }
                mNextButton.setClickable(true);
            }
            mActivity.showModalProgress(false);
            mAccount.startBackgroundTasks();
            mChangeTask = null;
            super.onPostExecute(success);
        }

        @Override
        protected void onCancelled() {
            mChangeTask = null;
            mNextButton.setClickable(true);
            mAccount.startBackgroundTasks();
            mActivity.showModalProgress(false);
        }
    }
}
