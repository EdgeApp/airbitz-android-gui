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
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.objects.AudioPlayer;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;

/**
 * Created on 2/21/14.
 */
public class SendConfirmationFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private final int INVALID_ENTRY_COUNT_MAX = 3;
    private final int INVALID_ENTRY_WAIT_MILLIS = 30000;
    private final int CALC_SEND_FEES_DELAY_MILLIS = 400;
    private static final String INVALID_ENTRY_PREF = "fragment_send_confirmation_invalid_entries";


    private TextView mFromEdittext;
    private TextView mToEdittext;
    private EditText mAuthorizationEdittext;

    private TextView mTitleTextView;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mSlideTextView;
    private TextView mAuthorizationTextView;
    private View mAuthorizationLayout;
    private TextView mBTCSignTextview;
    private TextView mBTCDenominationTextView;
    private TextView mFiatDenominationTextView;
    private TextView mFiatSignTextView;
    private TextView mConversionTextView;
    private HighlightOnPressButton mMaxButton;
    private TextWatcher mFiatTextWatcher;
    private TextWatcher mBTCTextWatcher;

    private Bundle bundle;

    private View mDummyFocus;

    private EditText mFiatField;
    private EditText mBitcoinField;
    private long mSavedBitcoin = -1;

    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;
    private ImageButton mConfirmSwipeButton;

    private float mConfirmCenter;
    private float dX = 0;
    private float rX = 0;

    private Calculator mCalculator;

    private RelativeLayout mSlideLayout;

    private RelativeLayout mParentLayout;

    private int mRightThreshold;
    private int mLeftThreshold;

    private boolean mSuccess = false;

    private String mUUIDorURI;
    private String mLabel;
    private Boolean mIsUUID;
    private long mAmountMax;
    private long mAmountToSendSatoshi = -1;
    private long mFees;
    private int mInvalidEntryCount = 0;
    private long mInvalidEntryStartMillis = 0;

    private boolean mPasswordRequired = false;
    private boolean mPinRequired = false;
    private boolean mMaxLocked = false;

    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    private Wallet mSourceWallet, mWalletForConversions, mToWallet;

    private boolean mAutoUpdatingTextFields = false;

    private View mView;
    /**
     * Wrap the fee calculation in an AsyncTask
     */
    private MaxAmountTask mMaxAmountTask;
    private CalculateFeesTask mCalculateFeesTask;
    /**
     * Represents an asynchronous send or transfer
     */
    private SendOrTransferTask mSendOrTransferTask;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();


        mActivity = (NavigationActivity) getActivity();

        bundle = this.getArguments();
        if (bundle == null) {
            Log.d(TAG, "Send confirmation bundle is null");
        } else {
            mUUIDorURI = bundle.getString(SendFragment.UUID);
            mLabel = bundle.getString(SendFragment.LABEL, "");
            mAmountToSendSatoshi = bundle.getLong(SendFragment.AMOUNT_SATOSHI);
            mIsUUID = bundle.getBoolean(SendFragment.IS_UUID);
            mSourceWallet = mCoreAPI.getWalletFromUUID(bundle.getString(SendFragment.FROM_WALLET_UUID));
            mWalletForConversions = mSourceWallet;
            if (mIsUUID) {
                mToWallet = mCoreAPI.getWalletFromUUID(mUUIDorURI);
            }
        }

        mAutoUpdatingTextFields = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_send_confirmation, container, false);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mTitleTextView.setText(R.string.send_confirmation_title);

        mDummyFocus = mView.findViewById(R.id.fragment_sendconfirmation_dummy_focus);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_parent);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);

        mConfirmSwipeButton = (ImageButton) mView.findViewById(R.id.button_confirm_swipe);

        mCalculator = mActivity.getCalculatorView();

        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);
        mSlideTextView = (TextView) mView.findViewById(R.id.textview_slide);
        mConversionTextView = (TextView) mView.findViewById(R.id.textview_conversion);
        mBTCSignTextview = (TextView) mView.findViewById(R.id.send_confirmation_btc_sign);
        mBTCDenominationTextView = (TextView) mView.findViewById(R.id.send_confirmation_btc_denomination);
        mFiatDenominationTextView = (TextView) mView.findViewById(R.id.send_confirmation_fiat_denomination);
        mFiatSignTextView = (TextView) mView.findViewById(R.id.send_confirmation_fiat_sign);
        mMaxButton = (HighlightOnPressButton) mView.findViewById(R.id.button_max);

        mFromEdittext = (TextView) mView.findViewById(R.id.textview_from_name);
        mToEdittext = (TextView) mView.findViewById(R.id.textview_to_name);
        mAuthorizationEdittext = (EditText) mView.findViewById(R.id.edittext_pin);

        mBitcoinField = (EditText) mView.findViewById(R.id.button_bitcoin_balance);
        mFiatField = (EditText) mView.findViewById(R.id.button_dollar_balance);

        mSlideLayout = (RelativeLayout) mView.findViewById(R.id.layout_slide);

        mFromEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mConversionTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSlideTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mAuthorizationTextView = (TextView) mView.findViewById(R.id.textview_pin);
        mAuthorizationTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mAuthorizationLayout = mView.findViewById(R.id.fragment_send_confirmation_layout_authorization);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_root);

        mConfirmCenter = mConfirmSwipeButton.getWidth() / 2;

        String balance = mCoreAPI.formatSatoshi(mSourceWallet.getBalanceSatoshi(), true);
        mFromEdittext.setText(mSourceWallet.getName() + " (" + balance + ")");
        if (mToWallet != null) {
            mToEdittext.setText(mToWallet.getName());
        } else {
            String temp = mUUIDorURI;
            if (mUUIDorURI.length() > 20) {
                temp = mUUIDorURI.substring(0, 5) + "..." + mUUIDorURI.substring(mUUIDorURI.length() - 5, mUUIDorURI.length());
            }
            mToEdittext.setText(temp);
        }

        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mPinRequired && editable.length() >= 4) {
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mAuthorizationEdittext.getWindowToken(), 0);
                    mParentLayout.requestFocus();
                }
            }
        };
        mAuthorizationEdittext.addTextChangedListener(mPINTextWatcher);

        mAuthorizationEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d(TAG, "PIN field focus changed");
                if (hasFocus) {
                    mAutoUpdatingTextFields = true;
                    mActivity.showSoftKeyboard(mAuthorizationEdittext);
                } else {
                    mAutoUpdatingTextFields = false;
                }
            }
        });

        mBTCTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mAutoUpdatingTextFields) {
                    updateTextFieldContents(true);
                    mBitcoinField.setSelection(mBitcoinField.getText().toString().length());
                }
            }
        };
        mBitcoinField.addTextChangedListener(mBTCTextWatcher);

        mBitcoinField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d(TAG, "Bitcoin field focus changed");
                if (hasFocus) {
                    resetFiatAndBitcoinFields();
                    mCalculator.setEditText(mBitcoinField);
                    mActivity.showCalculator();
                } else {
                    mActivity.hideCalculator();
                }
            }
        });

        mBitcoinField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCalculator.setEditText(mBitcoinField);
                mActivity.showCalculator();
            }
        });

        mFiatTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mAutoUpdatingTextFields) {
                    updateTextFieldContents(false);
                    mFiatField.setSelection(mFiatField.getText().toString().length());
                }
            }
        };
        mFiatField.addTextChangedListener(mFiatTextWatcher);

        mFiatField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d(TAG, "Fiat field focus changed");
                if (hasFocus) {
                    resetFiatAndBitcoinFields();
                    mCalculator.setEditText(mFiatField);
                    mActivity.showCalculator();
                } else {
                    mActivity.hideCalculator();
                }
            }
        });

        mFiatField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCalculator.setEditText(mFiatField);
                mActivity.showCalculator();
            }
        });

        TextView.OnEditorActionListener tvListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                        mAuthorizationLayout.getVisibility()==View.VISIBLE) {
                    mAuthorizationEdittext.requestFocus();
                    return true;
                }
                else {
                    mDummyFocus.requestFocus();
                    return true;
                }
            }
        };

        View.OnTouchListener preventOSKeyboard = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true; // the listener has consumed the event, no keyboard popup
            }
        };

        mBitcoinField.setOnTouchListener(preventOSKeyboard);
        mFiatField.setOnTouchListener(preventOSKeyboard);
        mBitcoinField.setOnEditorActionListener(tvListener);
        mFiatField.setOnEditorActionListener(tvListener);

        mSlideLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mSuccess) {
                    mLeftThreshold = (int) mSlideLayout.getX();
                }
            }
        });

        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = (int) event.getX();
                        mRightThreshold = (int) mConfirmSwipeButton.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        rX = event.getRawX();
                        float delta = rX - dX;
                        if (delta < mLeftThreshold - mConfirmCenter) {
                            mConfirmSwipeButton.setX(mLeftThreshold - mConfirmCenter);
                        } else if (delta > mRightThreshold) {
                            mConfirmSwipeButton.setX(mRightThreshold);
                        } else {
                            mConfirmSwipeButton.setX(delta);
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                        touchEventsEnded();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        resetSlider();
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    default:
                        break;
                }

                return false;
            }
        });

        mMaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSourceWallet != null && !mMaxLocked) {
                    mMaxLocked = true;
                    if (mMaxAmountTask != null)
                        mMaxAmountTask.cancel(true);
                    mMaxAmountTask = new MaxAmountTask();
                    mMaxAmountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.pushFragment(new HelpFragment(HelpFragment.SEND_CONFIRMATION), NavigationActivity.Tabs.SEND.ordinal());
            }
        });

        mConversionTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(mConversionTextView.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                        if (event.getRawX() >= (mConversionTextView.getRight() - mConversionTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            mActivity.pushFragment(new HelpFragment(HelpFragment.SEND_CONFIRMATION_INSUFFICIENT_FUNDS), NavigationActivity.Tabs.SEND.ordinal());
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        mDummyFocus.requestFocus();

        return mView;
    }

    private void resetFiatAndBitcoinFields() {
        mAutoUpdatingTextFields = true;
        mAmountToSendSatoshi = 0;
        mFiatField.setText("");
        mBitcoinField.setText("");
        mConversionTextView.setTextColor(Color.WHITE);
        mConversionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mBitcoinField.setTextColor(Color.WHITE);
        mFiatField.setTextColor(Color.WHITE);
        mAutoUpdatingTextFields = false;
        checkAuthorization();
        calculateFees();
    }

    public void touchEventsEnded() {
        int successThreshold = mLeftThreshold + (mSlideLayout.getWidth() / 4);
        if (mConfirmSwipeButton.getX() <= successThreshold) {
            attemptInitiateSend();
        } else {
            resetSlider();
        }
    }

    private void updateTextFieldContents(boolean btc) {
        double currency;
        long satoshi;

        mAutoUpdatingTextFields = true;
        if (btc) {
            mAmountToSendSatoshi = mCoreAPI.denominationToSatoshi(mBitcoinField.getText().toString());
            mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mWalletForConversions.getCurrencyNum(), false, false));
        } else {
            try {
                currency = Double.valueOf(mFiatField.getText().toString());
            } catch (NumberFormatException e) {
                currency = 0.0;
            }
            satoshi = mCoreAPI.CurrencyToSatoshi(currency, mWalletForConversions.getCurrencyNum());
            mAmountToSendSatoshi = satoshi;
            mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false));
        }
        mAutoUpdatingTextFields = false;
        checkAuthorization();
        calculateFees();
    }

    final Runnable delayCalcFees = new Runnable() {
        @Override
        public void run() {
            if (mCalculateFeesTask != null) {
                mCalculateFeesTask.cancel(true);
            }
            mCalculateFeesTask = new CalculateFeesTask();
            mCalculateFeesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    private void calculateFees() {
        mHandler.removeCallbacks(delayCalcFees);
        mHandler.postDelayed(delayCalcFees, CALC_SEND_FEES_DELAY_MILLIS);
    }

    private void UpdateFeeFields(Long fees) {
        mAutoUpdatingTextFields = true;
        int color = Color.WHITE;
        if (mAmountMax > 0 && mAmountToSendSatoshi == mAmountMax) {
            color = getResources().getColor(R.color.max_orange);
            mMaxButton.setBackgroundResource(R.drawable.bg_btn_orange);
        } else {
            color = Color.WHITE;
            mMaxButton.setBackgroundResource(R.drawable.bg_btn_green);
        }
        if (fees < 0) {
            mConversionTextView.setText(mActivity.getResources().getString(R.string.fragment_send_confirmation_insufficient_funds));
            mConversionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btn_help, 0);
            mConversionTextView.setCompoundDrawablePadding(10);
            mConversionTextView.setBackgroundResource(R.color.white_haze);
            mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
            mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronym(mWalletForConversions.getCurrencyNum()));
            mConversionTextView.setTextColor(Color.RED);
            mBitcoinField.setTextColor(Color.RED);
            mFiatField.setTextColor(Color.RED);
        } else if ((fees + mAmountToSendSatoshi) <= mSourceWallet.getBalanceSatoshi()) {
            mConversionTextView.setTextColor(color);
            mConversionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mConversionTextView.setBackgroundResource(android.R.color.transparent);
            mBitcoinField.setTextColor(color);
            mFiatField.setTextColor(color);

            String coinFeeString = "+ " + mCoreAPI.formatSatoshi(fees, false);
            mBTCDenominationTextView.setText(coinFeeString + " " + mCoreAPI.getDefaultBTCDenomination());

            double fiatFee = mCoreAPI.SatoshiToCurrency(fees, mWalletForConversions.getCurrencyNum());
            String fiatFeeString = "+ " + mCoreAPI.formatCurrency(fiatFee, mWalletForConversions.getCurrencyNum(), false);
            mFiatDenominationTextView.setText(fiatFeeString + " " + mCoreAPI.getCurrencyAcronym(mWalletForConversions.getCurrencyNum()));
            mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mWalletForConversions.getCurrencyNum()));
        }
        mAutoUpdatingTextFields = false;
    }

    private void attemptInitiateSend() {
        float remaining = (mInvalidEntryStartMillis + INVALID_ENTRY_WAIT_MILLIS - System.currentTimeMillis()) / 1000;
        // check if invalid entry timeout still active
        if(mInvalidEntryStartMillis > 0) {
            if(mPinRequired) {
                String message = String.format(getString(R.string.fragment_send_confirmation_pin_remaining), remaining);
                mActivity.ShowFadingDialog(message);
            } else {
                String message = String.format(getString(R.string.fragment_send_confirmation_password_remaining), remaining);
                mActivity.ShowFadingDialog(message);
            }
            resetSlider();
            return;
        }

        String enteredPIN = mAuthorizationEdittext.getText().toString();
        if(mPinRequired && enteredPIN.isEmpty()) {
            mActivity.ShowFadingDialog(getString(R.string.fragment_send_confirmation_please_enter_pin), 2000);
            mAuthorizationEdittext.requestFocus();
            resetSlider();
            return;
        }

        String userPIN = mCoreAPI.GetUserPIN();
         if (mPinRequired && enteredPIN != null && userPIN != null && !userPIN.equals(enteredPIN)) {
             mInvalidEntryCount += 1;
             saveInvalidEntryCount(mInvalidEntryCount);
             if(mInvalidEntryCount >= INVALID_ENTRY_COUNT_MAX) {
                 if(mInvalidEntryStartMillis == 0) {
                     mInvalidEntryStartMillis = System.currentTimeMillis();
                     mHandler.postDelayed(invalidEntryTimer, INVALID_ENTRY_WAIT_MILLIS);
                 }
                 remaining = (mInvalidEntryStartMillis + INVALID_ENTRY_WAIT_MILLIS - System.currentTimeMillis()) / 1000;
                 String message = String.format(getString(R.string.fragment_send_confirmation_pin_remaining), remaining);
                 mActivity.ShowFadingDialog(message);
             } else {
                 mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_send_incorrect_pin_message));
             }
             mAuthorizationEdittext.requestFocus();
             resetSlider();
        } else if (mPasswordRequired && !mCoreAPI.PasswordOK(AirbitzApplication.getUsername(), mAuthorizationEdittext.getText().toString())) {
             mInvalidEntryCount += 1;
             saveInvalidEntryCount(mInvalidEntryCount);
             if(mInvalidEntryCount >= INVALID_ENTRY_COUNT_MAX) {
                 if(mInvalidEntryStartMillis == 0) {
                     mInvalidEntryStartMillis = System.currentTimeMillis();
                     mHandler.postDelayed(invalidEntryTimer, INVALID_ENTRY_WAIT_MILLIS);
                 }
                 remaining = (mInvalidEntryStartMillis + INVALID_ENTRY_WAIT_MILLIS - System.currentTimeMillis()) / 1000;
                 String message = String.format(getString(R.string.fragment_send_confirmation_password_remaining), remaining);
                 mActivity.ShowFadingDialog(message);
             } else {
                 mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_send_incorrect_password_message));
             }
             mAuthorizationEdittext.requestFocus();
             resetSlider();
         } else if (mAmountToSendSatoshi == 0) {
            resetSlider();
            mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_send_no_satoshi_message));
        } else {
             // show the sending screen
             SuccessFragment mSuccessFragment = new SuccessFragment();
             Bundle bundle = new Bundle();
             bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);
             mSuccessFragment.setArguments(bundle);
             mActivity.pushFragment(mSuccessFragment, NavigationActivity.Tabs.SEND.ordinal());

             mSendOrTransferTask = new SendOrTransferTask(mSourceWallet, mUUIDorURI, mAmountToSendSatoshi, mLabel);
             mSendOrTransferTask.execute();
             finishSlider();
        }
    }

    final Runnable invalidEntryTimer = new Runnable() {
        @Override
        public void run() {
            mInvalidEntryStartMillis = 0;
        }
    };

    private void resetSlider() {
        Animator animator = ObjectAnimator.ofFloat(mConfirmSwipeButton, "translationX", -(mRightThreshold - mConfirmSwipeButton.getX()), 0);
        animator.setDuration(300);
        animator.setStartDelay(0);
        animator.start();
    }

    private void finishSlider() {
        Animator animator = ObjectAnimator.ofFloat(mConfirmSwipeButton, "translationX", -(mRightThreshold - mConfirmSwipeButton.getX()), -(mRightThreshold - (mLeftThreshold - mConfirmCenter)));
        animator.setDuration(300);
        animator.setStartDelay(0);
        animator.start();
    }

    private void checkAuthorization()
    {
        mPasswordRequired = false;
        mPinRequired = false;

        long dailyLimit = mCoreAPI.GetDailySpendLimit();
        boolean dailyLimitSetting = mCoreAPI.GetDailySpendLimitSetting();

        if (!mIsUUID && dailyLimitSetting
            && (mAmountToSendSatoshi + mCoreAPI.GetTotalSentToday(mSourceWallet) >= dailyLimit)) {
            // Show password
            mPasswordRequired = true;
            mAuthorizationLayout.setVisibility(View.VISIBLE);
            mAuthorizationTextView.setText(getString(R.string.send_confirmation_enter_send_password));
            mAuthorizationEdittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else if (!mIsUUID && mCoreAPI.GetPINSpendLimitSetting() && mAmountToSendSatoshi >= mCoreAPI.GetPINSpendLimit() && !AirbitzApplication.recentlyLoggedIn()) {
            // Show PIN pad
            mPinRequired = true;
            mAuthorizationLayout.setVisibility(View.VISIBLE);
            mAuthorizationTextView.setText(getString(R.string.send_confirmation_enter_send_pin));
            mAuthorizationEdittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            mAuthorizationLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        bundle = this.getArguments();
        if (bundle == null) {
            Log.d(TAG, "Send confirmation bundle is null");
        } else {
            mUUIDorURI = bundle.getString(SendFragment.UUID);
            mLabel = bundle.getString(SendFragment.LABEL, "");
            mAmountToSendSatoshi = bundle.getLong(SendFragment.AMOUNT_SATOSHI);
            mIsUUID = bundle.getBoolean(SendFragment.IS_UUID);
            mSourceWallet = mCoreAPI.getWalletFromUUID(bundle.getString(SendFragment.FROM_WALLET_UUID));
            mWalletForConversions = mSourceWallet;
            if (mIsUUID) {
                mToWallet = mCoreAPI.getWalletFromUUID(mUUIDorURI);
            }
        }

        mActivity.showNavBar(); // in case we came from backing out of SuccessFragment
        mParentLayout.requestFocus(); //Take focus away first
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mActivity.hideCalculator();

        mBitcoinField = (EditText) mView.findViewById(R.id.button_bitcoin_balance);
        mFiatField = (EditText) mView.findViewById(R.id.button_dollar_balance);
        mAuthorizationEdittext = (EditText) mView.findViewById(R.id.edittext_pin);

        mAutoUpdatingTextFields = true;

        if (mSavedBitcoin > 0) {
            mAmountToSendSatoshi = mSavedBitcoin;
        }

        checkAuthorization();

        if(mAmountToSendSatoshi > 0) {
            mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false));
            if (mWalletForConversions != null) {
                mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mWalletForConversions.getCurrencyNum(), false, false));
            }
            calculateFees();

            if(mAuthorizationLayout.getVisibility() == View.VISIBLE) {
                mAuthorizationEdittext.requestFocus();
            }
        } else {
            mFiatField.setText("");
            mBitcoinField.setText("");
            if (mActivity.isLargeDpi()) {
                mFiatField.requestFocus();
            }
        }

        mAuthorizationTextView = (TextView) mView.findViewById(R.id.textview_pin);
        mConversionTextView = (TextView) mView.findViewById(R.id.textview_conversion);
        mBTCSignTextview = (TextView) mView.findViewById(R.id.send_confirmation_btc_sign);
        mBTCDenominationTextView = (TextView) mView.findViewById(R.id.send_confirmation_btc_denomination);
        mFiatDenominationTextView = (TextView) mView.findViewById(R.id.send_confirmation_fiat_denomination);
        mFiatSignTextView = (TextView) mView.findViewById(R.id.send_confirmation_fiat_sign);

        mBTCSignTextview.setText(mCoreAPI.getUserBTCSymbol());
        mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
        mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronym(mWalletForConversions.getCurrencyNum()));
        mFiatSignTextView.setText(mCoreAPI.getCurrencyDenomination(mWalletForConversions.getCurrencyNum()));
        mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mWalletForConversions.getCurrencyNum()));

        mAutoUpdatingTextFields = false;

        mMaxLocked = false;

        mInvalidEntryCount = getInvalidEntryCount();

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSavedBitcoin = mAmountToSendSatoshi;
        if (mCalculateFeesTask != null)
            mCalculateFeesTask.cancel(true);
        if (mMaxAmountTask != null)
            mMaxAmountTask.cancel(true);
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



    public class MaxAmountTask extends AsyncTask<Void, Void, Long> {

        MaxAmountTask() {
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Max calculation called");
        }

        @Override
        protected Long doInBackground(Void... params) {
            Log.d(TAG, "Max calculation started");
            String dest = mIsUUID ? mWalletForConversions.getUUID() : mUUIDorURI;
            return mCoreAPI.maxSpendable(mSourceWallet.getUUID(), dest, mIsUUID);
        }

        @Override
        protected void onPostExecute(final Long max) {
            Log.d(TAG, "Max calculation finished");
            mMaxAmountTask = null;
            if (isAdded()) {
                if (max < 0) {
                    Log.d(TAG, "Max calculation error");
                }
                mMaxLocked = false;
                mAmountMax = max;
                mAmountToSendSatoshi = max;
                mAutoUpdatingTextFields = true;
                mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mWalletForConversions.getCurrencyNum(), false, false));
                mFiatSignTextView.setText(mCoreAPI.getCurrencyDenomination(mWalletForConversions.getCurrencyNum()));
                mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mWalletForConversions.getCurrencyNum()));
                mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false));

                checkAuthorization();
                calculateFees();
            }
            mAutoUpdatingTextFields = false;
        }

        @Override
        protected void onCancelled() {
            mMaxAmountTask = null;
        }
    }

    public class CalculateFeesTask extends AsyncTask<Void, Void, Long> {

        @Override
        protected void onPreExecute() {
            mSlideLayout.setEnabled(false);
        }

        @Override
        protected Long doInBackground(Void... params) {
            Log.d(TAG, "Fee calculation started");
            String dest = mIsUUID ? mWalletForConversions.getUUID() : mUUIDorURI;
            return mCoreAPI.calcSendFees(mSourceWallet.getUUID(), dest, mAmountToSendSatoshi, mIsUUID);
        }

        @Override
        protected void onPostExecute(final Long fees) {
            Log.d(TAG, "Fee calculation ended");
            if (isAdded()) {
                mCalculateFeesTask = null;
                mFees = fees;
                UpdateFeeFields(fees);
                mSlideLayout.setEnabled(true);
            }
        }

        @Override
        protected void onCancelled() {
            mCalculateFeesTask = null;
        }
    }

    public class SendOrTransferTask extends AsyncTask<Void, Void, CoreAPI.TxResult> {
        private final String mAddress;
        private final long mSatoshi;
        private final String mLabel;
        private Wallet mFromWallet;

        SendOrTransferTask(Wallet fromWallet, String address, long amount, String label) {
            mFromWallet = fromWallet;
            mAddress = address;
            mSatoshi = amount;
            mLabel = label;
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "SEND called");
        }

        @Override
        protected CoreAPI.TxResult doInBackground(Void... params) {
            Log.d(TAG, "Initiating SEND");
            return mCoreAPI.InitiateTransferOrSend(mFromWallet, mAddress, mSatoshi, mLabel);
        }

        @Override
        protected void onPostExecute(final CoreAPI.TxResult txResult) {
            Log.d(TAG, "SEND done");
            mSendOrTransferTask = null;
            if (txResult.getError() != null) {
                Log.d(TAG, "Error during send " + txResult.getError());
                if (mActivity != null) {
                    mActivity.popFragment(); // stop the sending screen
                    mActivity.getFragmentManager().executePendingTransactions();
                    mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_send_confirmation_send_error_title), txResult.getError());
                }
            } else {
                if (mActivity != null) {
                    saveInvalidEntryCount(0);
                    AudioPlayer.play(mActivity, R.raw.bitcoin_sent);
                    mActivity.onSentFunds(mFromWallet.getUUID(), txResult.getTxId());
                }
            }
        }

        @Override
        protected void onCancelled() {
            mActivity.popFragment(); // stop the sending screen
            mSendOrTransferTask = null;
        }
    }
}
