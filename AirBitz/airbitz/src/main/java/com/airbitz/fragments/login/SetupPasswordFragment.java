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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_PasswordRule;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/26/15.
 */
public class SetupPasswordFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    public static final int MIN_PIN_LENGTH = 4;
    public static String USERNAME = "com.airbitz.setuppassword.username";

    private EditText mPasswordEditText;
    private EditText mWithdrawalPinEditText;
    private EditText mPasswordConfirmationEditText;
    private HighlightOnPressButton mNextButton;
    private HighlightOnPressButton mBackButton;
    private TextView mTitleTextView;
    private LinearLayout mPopupContainer;
    private LinearLayout mPopupBlank;
    private ImageView mRule1Image, mRule2Image, mRule3Image, mRule4Image;
    private TextView mRule1Text, mRule2Text, mRule3Text, mRule4Text;
    private TextView mTimeTextView;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup_password, container, false);

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.fragment_setup_titles);

        mBackButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_setup_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mNextButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_setup_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_setup_password_edittext);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mPasswordConfirmationEditText = (EditText) mView.findViewById(R.id.fragment_setup_password_repassword_edittext);
        mPasswordConfirmationEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mRule1Image = (ImageView) mView.findViewById(R.id.fragment_setup_password_switch_image_1);
        mRule2Image = (ImageView) mView.findViewById(R.id.fragment_setup_password_switch_image_2);
        mRule3Image = (ImageView) mView.findViewById(R.id.fragment_setup_password_switch_image_3);
        mRule4Image = (ImageView) mView.findViewById(R.id.fragment_setup_password_switch_image_4);

        mRule1Text = (TextView) mView.findViewById(R.id.fragment_setup_password_switch_text_1);
        mRule2Text = (TextView) mView.findViewById(R.id.fragment_setup_password_switch_text_2);
        mRule3Text = (TextView) mView.findViewById(R.id.fragment_setup_password_switch_text_3);
        mRule4Text = (TextView) mView.findViewById(R.id.fragment_setup_password_switch_text_4);

        mTimeTextView = (TextView) mView.findViewById(R.id.fragment_setup_password_time_textview);

        mPopupContainer = (LinearLayout) mView.findViewById(R.id.fragment_setup_password_popup_layout);
        mPopupBlank = (LinearLayout) mView.findViewById(R.id.fragment_setup_password_blank);

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                checkPasswordRules(mPasswordEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        };

        mPasswordEditText.addTextChangedListener(tw);
        mPasswordConfirmationEditText.addTextChangedListener(tw);

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mHandler.post(animatePopupDown);
                    mActivity.showSoftKeyboard(mPasswordEditText);
                } else {
                    mHandler.post(animatePopupUp);
                }
            }
        });

        mPasswordConfirmationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mWithdrawalPinEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mWithdrawalPinEditText = (EditText) mView.findViewById(R.id.fragment_password_pin_edittext);
        mWithdrawalPinEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mWithdrawalPinEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 4) {
                    mActivity.hideSoftKeyboard(mWithdrawalPinEditText);
                }
            }
        };
        mWithdrawalPinEditText.addTextChangedListener(mPINTextWatcher);

        mWithdrawalPinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mWithdrawalPinEditText);
                } else {
                }
            }
        });
        return mView;
    }

    Animator.AnimatorListener endListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animator) {
            mPopupContainer.setVisibility(View.GONE);
            mPopupBlank.setVisibility(View.GONE);
        }

        @Override public void onAnimationCancel(Animator animator) { }
        @Override public void onAnimationStart(Animator animator) { }
        @Override public void onAnimationRepeat(Animator animator) { }
    };

    Runnable animatePopupDown = new Runnable() {
        @Override
        public void run() {
            if(mPopupContainer != null && isAdded()) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mPopupContainer, "translationY",
                        -getResources().getDimension(R.dimen.activity_signup_popup_height), 0);
                if(animator != null) {
                    animator.setDuration(300);
                    animator.start();
                }
                mPopupContainer.setVisibility(View.VISIBLE);
                mPopupBlank.setVisibility(View.VISIBLE);
            }
        }
    };

    Runnable animatePopupUp = new Runnable() {
        @Override
        public void run() {
            if(mPopupContainer != null && isAdded()) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mPopupContainer, "translationY",
                        0, -getResources().getDimension(R.dimen.activity_signup_popup_height));
                if(animator != null) {
                    animator.setDuration(300);
                    animator.addListener(endListener);
                    animator.start();
                }
            }
        }
    };

    private void enableNextButton(boolean enable) {
        if(enable) {
            mNextButton.setBackgroundResource(R.drawable.setup_button_green);
            mNextButton.setClickable(true);
        }
        else {
            mNextButton.setBackgroundResource(R.drawable.setup_button_dark_gray);
            mNextButton.setClickable(false);
        }
    }

    // checks the mPassword against the mPassword rules
    // returns YES if new mPassword fields are good, NO if the new mPassword fields failed the checks
    // if the new mPassword fields are bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
    private List<String> checkPasswordRules(String password) {
        List<tABC_PasswordRule> rules = mCoreAPI.GetPasswordRules(password);
        List<String> ruleFails = new ArrayList<>();

        if (rules.isEmpty()) {
            return ruleFails;
        }

        for (int i = 0; i < rules.size(); i++) {
            tABC_PasswordRule pRule = rules.get(i);
            boolean passed = pRule.getBPassed();
            if (!passed) {
                ruleFails.add(pRule.getSzDescription());
            }

            int resource = passed ? R.drawable.green_check : R.drawable.white_dot;
            switch (i) {
                case 0:
                    mRule1Image.setImageResource(resource);
                    mRule1Text.setText(pRule.getSzDescription());
                    break;
                case 1:
                    mRule2Image.setImageResource(resource);
                    mRule2Text.setText(pRule.getSzDescription());
                    break;
                case 2:
                    mRule3Image.setImageResource(resource);
                    mRule3Text.setText(pRule.getSzDescription());
                    break;
                case 3:
                    mRule4Image.setImageResource(resource);
                    mRule4Text.setText(pRule.getSzDescription());
                    break;
                default:
                    break;
            }
        }
        mTimeTextView.setText(GetCrackString(mCoreAPI.GetPasswordSecondsToCrack(password)));
        return ruleFails;
    }

    private void goNext() {
        mActivity.hideSoftKeyboard(mWithdrawalPinEditText);
        // if they entered a valid mUsername or old mPassword
        if (newPasswordFieldsAreValid() && pinFieldIsValid()) {
            attemptSignUp();
        }
    }

    // checks the pin field
    // returns YES if field is good
    // if the field is bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
    private boolean pinFieldIsValid() {
        boolean bpinNameFieldIsValid = true;

        // if the pin isn't long enough
        if (mWithdrawalPinEditText.getText().toString().length() < MIN_PIN_LENGTH) {
            bpinNameFieldIsValid = false;
            mActivity.ShowFadingDialog(getResources().getString(R.string.activity_signup_insufficient_pin));
        }

        return bpinNameFieldIsValid;
    }

    // checks the mPassword against the mPassword rules
    // returns YES if new mPassword fields are good, NO if the new mPassword fields failed the checks
    // if the new mPassword fields are bad, an appropriate message box is displayed
    private boolean newPasswordFieldsAreValid() {
        if(mPasswordEditText.getText().toString().isEmpty()) {
            return true;
        }
        boolean bNewPasswordFieldsAreValid = true;
            List<String> fails = checkPasswordRules(mPasswordEditText.getText().toString());
            if (!fails.isEmpty()) {
                bNewPasswordFieldsAreValid = false;
                String message = getResources().getString(R.string.activity_signup_password_fails);
                for(String fail : fails) {
                    message += "\n" + fail + ".";
                }
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_insufficient_password), message);
            } else if (!mPasswordConfirmationEditText.getText().toString().equals(mPasswordEditText.getText().toString())) {
                bNewPasswordFieldsAreValid = false;
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_failed), getResources().getString(R.string.activity_signup_passwords_dont_match));
            }

        return bNewPasswordFieldsAreValid;
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

        char[] password = null;
        Editable pass = mPasswordEditText.getText();
        if(!pass.toString().isEmpty()) {
            password = new char[pass.length()];
            pass.getChars(0, pass.length(), password, 0);
        }

        // Reset errors.
        mPasswordEditText.setError(null);
        mPasswordConfirmationEditText.setError(null);

        mCreateAccountTask = new CreateAccountTask(password);
        mCreateAccountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPasswordEditText.setSelection(mPasswordEditText.getText().length());
                mPasswordEditText.requestFocus();
            }
        });
        enableNextButton(true);
    }

    CreateAccountTask mCreateAccountTask;
    public class CreateAccountTask extends AsyncTask<Void, Void, String> {

        private final char[] mPassword;
        private String mPasswordString;

        CreateAccountTask(char[] password) {
            mPassword = password;
            mActivity.ShowFadingDialog(getString(R.string.fragment_signup_creating_account), 2000000, false);
        }

        @Override
        protected String doInBackground(Void... params) {
            mPasswordString = null;
            if(mPassword != null) {
                mPasswordString = String.valueOf(mPassword);
            }
            return mCoreAPI.createAccountAndPin(getArguments().getString(USERNAME),
                    mPasswordString , mWithdrawalPinEditText.getText().toString());
        }

        @Override
        protected void onPostExecute(String errorMessage) {
            onCancelled();
            if (errorMessage == null) {
                    SetupWriteItDownFragment fragment = new SetupWriteItDownFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(SetupWriteItDownFragment.USERNAME, getArguments().getString(USERNAME));
                    bundle.putString(SetupWriteItDownFragment.PASSWORD, mPasswordString);
                    bundle.putString(SetupWriteItDownFragment.PIN, mWithdrawalPinEditText.getText().toString());
                    fragment.setArguments(bundle);
                    mActivity.pushFragment(fragment);
            } else {
                mActivity.ShowFadingDialog(errorMessage);
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAccountTask = null;
            mActivity.DismissFadingDialog();
        }
    }
}
