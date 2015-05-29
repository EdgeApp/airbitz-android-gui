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

package com.airbitz.fragments.send;

import android.animation.Animator;
import android.animation.ObjectAnimator;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.wallet.WalletsFragment;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.AudioPlayer;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created on 2/21/14.
 */
public class SendConfirmationFragment extends BaseFragment implements
        CoreAPI.OnWalletLoaded,
        NavigationActivity.OnBackPress,
        CoreAPI.OnPasswordCheckListener {
    private final String TAG = getClass().getSimpleName();

    private final int INVALID_ENTRY_COUNT_MAX = 3;
    private final int INVALID_ENTRY_WAIT_MILLIS = 30000;
    private final int CALC_SEND_FEES_DELAY_MILLIS = 400;
    private final int DUST_AMOUNT = 5340;
    private static final String INVALID_ENTRY_PREF = "fragment_send_confirmation_invalid_entries";

    private TextView mToEdittext;
    private EditText mAuthorizationEdittext;
    String mDelayedMessage;

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
    private HighlightOnPressSpinner mWalletSpinner;
    private ImageButton mConfirmSwipeButton;

    private float mSlideHalfWidth;
    private float moveX = 0;

    private Calculator mCalculator;

    private RelativeLayout mSlideLayout;

    private RelativeLayout mParentLayout;

    private int mRightThreshold;
    private int mLeftThreshold;

    private String mUUIDorURI;
    private String mLabel;
    private String mCategory;
    private String mNotes;
    private Boolean mLocked = false;
    private Boolean mIsUUID;
    private long mAmountMax;
    private long mAmountToSendSatoshi = -1;
    private double mAmountFiat = -1;
    private long mFees;
    private int mInvalidEntryCount = 0;
    private long mInvalidEntryStartMillis = 0;
    private boolean mFundsSent = false;

    private String _sendTo;
    private String _destUUID;

    private boolean mPasswordRequired = false;
    private boolean mPinRequired = false;
    private boolean mMaxLocked = false;

    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    private Wallet mSourceWallet, mWalletForConversions, mToWallet;
    private List<Wallet> mWallets;//Actual wallets

    private boolean mAutoUpdatingTextFields = false;

    private Typeface mBitcoinTypeface;

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

    private CoreAPI.SpendTarget mSpendTarget = null;

    public interface OnExitHandler {
        public void error();
        public void success(String txId);
    }

    private OnExitHandler exitHandler;

    public SendConfirmationFragment() {}

    public void setSpendTarget(CoreAPI.SpendTarget target) {
        mSpendTarget = target;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();

        mActivity = (NavigationActivity) getActivity();

        bundle = this.getArguments();

        mAutoUpdatingTextFields = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_send_confirmation, container, false);

        mBitcoinTypeface = Typeface.createFromAsset(getActivity().getAssets(), "font/Lato-Regular.ttf");

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

        mToEdittext = (TextView) mView.findViewById(R.id.textview_to_name);
        mAuthorizationEdittext = (EditText) mView.findViewById(R.id.edittext_pin);

        mBitcoinField = (EditText) mView.findViewById(R.id.button_bitcoin_balance);
        mFiatField = (EditText) mView.findViewById(R.id.button_dollar_balance);

        mSlideLayout = (RelativeLayout) mView.findViewById(R.id.layout_slide);

        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mConversionTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSlideTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mAuthorizationTextView = (TextView) mView.findViewById(R.id.textview_pin);
        mAuthorizationTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mAuthorizationLayout = mView.findViewById(R.id.fragment_send_confirmation_layout_authorization);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_root);

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.from_wallet_spinner);

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
                    mBitcoinField.selectAll();

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
                Log.d(TAG, "Bitcoin field clicked");
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

        View.OnTouchListener setCursorAtEnd = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "Prevent OS keyboard");
                final EditText edittext = (EditText) v;
                edittext.onTouchEvent(event);
                edittext.post(new Runnable() {
                    @Override
                    public void run() {
                        edittext.setSelection(edittext.getText().length()); // set cursor at end
                    }
                });
                return true;
            }
        };

        // Prevent OS keyboard from showing
        try {
            final Method method = EditText.class.getMethod(
                    "setShowSoftInputOnFocus"
                    , new Class[] { boolean.class });
            method.setAccessible(true);
            method.invoke(mBitcoinField, false);
            method.invoke(mFiatField, false);
        } catch (Exception e) {
            // ignore
        }

        mBitcoinField.setOnTouchListener(setCursorAtEnd);
        mFiatField.setOnTouchListener(setCursorAtEnd);
        mBitcoinField.setOnEditorActionListener(tvListener);
        mFiatField.setOnEditorActionListener(tvListener);

        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mSlideHalfWidth = mConfirmSwipeButton.getWidth() / 2;
                        mLeftThreshold = (int) (mSlideLayout.getX());
                        mRightThreshold = (int) (mSlideLayout.getX() + mSlideLayout.getWidth() - mConfirmSwipeButton.getWidth() - mLeftThreshold);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveX = event.getRawX() - mLeftThreshold;
                        float leftSlide = moveX - mSlideHalfWidth;
                        Log.d(TAG, "Move data: leftThreshold, rightThreshold, leftSlide, slideWidth, = "
                                + mLeftThreshold +", "+ mRightThreshold +", "+ leftSlide +", "+ mConfirmSwipeButton.getWidth());
                        if (leftSlide < 0) {
                            mConfirmSwipeButton.setX(0);
                        } else if (leftSlide > mRightThreshold) {
                            mConfirmSwipeButton.setX(mRightThreshold);
                        } else {
                            mConfirmSwipeButton.setX(leftSlide);
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

    @Override
    public boolean onBackPress() {
        if (null != exitHandler) {
            exitHandler.error();
        }
        return false;
    }

    public void setExitHandler(OnExitHandler handler) {
        this.exitHandler = handler;
    }

    private void resetFiatAndBitcoinFields() {
        mAutoUpdatingTextFields = true;
        mAmountToSendSatoshi = 0;
        mAmountFiat = 0.0;
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
        int successThreshold = (mSlideLayout.getWidth() / 8);
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
            mSpendTarget.setSpendAmount(mAmountToSendSatoshi);
            mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mWalletForConversions.getCurrencyNum(), false, false));
        } else {
            try {
                currency = Double.valueOf(mFiatField.getText().toString());
            } catch (NumberFormatException e) {
                currency = 0.0;
            }
            satoshi = mCoreAPI.CurrencyToSatoshi(currency, mWalletForConversions.getCurrencyNum());
            mAmountToSendSatoshi = satoshi;
            mSpendTarget.setSpendAmount(satoshi);
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
            mMaxButton.setBackgroundResource(R.drawable.bg_button_orange);
        } else {
            color = Color.WHITE;
            mMaxButton.setBackgroundResource(R.drawable.bg_button_green);
        }
        if (fees < 0) {
            if(mAmountToSendSatoshi > DUST_AMOUNT) {
                mConversionTextView.setText(mActivity.getResources().getString(R.string.fragment_send_confirmation_insufficient_funds));
            }
            else {
                mConversionTextView.setText(mActivity.getResources().getString(R.string.fragment_send_confirmation_insufficient_amount));
            }
            mConversionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btn_help, 0);
            mConversionTextView.setCompoundDrawablePadding(10);
            mConversionTextView.setBackgroundResource(R.color.white_haze);
            mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
            mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronym(mWalletForConversions.getCurrencyNum()));
            mConversionTextView.setTextColor(Color.RED);
            mBitcoinField.setTextColor(Color.RED);
            mFiatField.setTextColor(Color.RED);

            mSlideLayout.setVisibility(View.INVISIBLE);
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

            mSlideLayout.setVisibility(View.VISIBLE);
        }
        mAutoUpdatingTextFields = false;
    }

    private void attemptInitiateSend() {
        // If a send is currently executing, don't send again
        if (mSendOrTransferTask != null) {
            return;
        }
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
        } else if (mPasswordRequired) {
             mActivity.showModalProgress(true);
             mCoreAPI.SetOnPasswordCheckListener(this, mAuthorizationEdittext.getText().toString());
        } else {
             continueChecks();
         }
    }

    @Override
    public void onPasswordCheck(boolean passwordOkay) {
        mActivity.showModalProgress(false);

        if(passwordOkay) {
            continueChecks();
        }
        else {
            mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_send_incorrect_password_title));
            mAuthorizationEdittext.requestFocus();
        }
    }

    private void continueChecks() {
        if (mAmountToSendSatoshi == 0) {
            mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_send_no_satoshi_message));
        } else if (mAmountToSendSatoshi < DUST_AMOUNT) {
            showDustAlert();
        } else {
            // show the sending screen
            SuccessFragment mSuccessFragment = new SuccessFragment();
            Bundle bundle = new Bundle();
            bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);
            mSuccessFragment.setArguments(bundle);
            if (null != exitHandler) {
                mActivity.pushFragment(mSuccessFragment, NavigationActivity.Tabs.MORE.ordinal());
            } else {
                mActivity.pushFragment(mSuccessFragment, NavigationActivity.Tabs.SEND.ordinal());
            }

            mSendOrTransferTask = new SendOrTransferTask(mSourceWallet, mAmountFiat);
            mSendOrTransferTask.execute();
        }
        resetSlider();
    }

    private void showDustAlert() {
        double dustFiat = mCoreAPI.SatoshiToCurrency(DUST_AMOUNT, mSourceWallet.getCurrencyNum());
        String alertMessage = getString(R.string.fragment_send_confirmation_dust_alert);
        if(dustFiat != 0) {
           alertMessage += " " + String.format(getString(R.string.fragment_send_confirmation_dust_alert_more),
               mCoreAPI.formatSatoshi((long) DUST_AMOUNT), mCoreAPI.formatCurrency(dustFiat, mSourceWallet.getCurrencyNum(), true));
        }
        mActivity.ShowFadingDialog(alertMessage);
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

    private void checkAuthorization()
    {
        mPasswordRequired = false;
        mPinRequired = false;

        long dailyLimit = mCoreAPI.GetDailySpendLimit();
        boolean dailyLimitSetting = mCoreAPI.GetDailySpendLimitSetting();

        if (mToWallet == null && dailyLimitSetting
            && (mAmountToSendSatoshi + mCoreAPI.GetTotalSentToday(mSourceWallet) >= dailyLimit)) {
            // Show password
            mPasswordRequired = true;
            mAuthorizationLayout.setVisibility(View.VISIBLE);
            mAuthorizationTextView.setText(getString(R.string.send_confirmation_enter_send_password));
            mAuthorizationEdittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else if (mToWallet == null && mCoreAPI.GetPINSpendLimitSetting() && mAmountToSendSatoshi >= mCoreAPI.GetPINSpendLimit() && !AirbitzApplication.recentlyLoggedIn()) {
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
        super.onResume();
        if(mFundsSent) {
            return;
        }
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mCoreAPI.setOnWalletLoadedListener(this);

        bundle = this.getArguments();
        if (bundle == null) {
            Log.d(TAG, "Send confirmation bundle is null");
        } else {
            mUUIDorURI = bundle.getString(SendFragment.UUID);
            mLabel = bundle.getString(SendFragment.LABEL, "");
            mCategory = bundle.getString(SendFragment.CATEGORY, "");
            mNotes = bundle.getString(SendFragment.NOTES, "");
            mAmountToSendSatoshi = bundle.getLong(SendFragment.AMOUNT_SATOSHI);
            mAmountFiat = bundle.getDouble(SendFragment.AMOUNT_FIAT);
            mIsUUID = bundle.getBoolean(SendFragment.IS_UUID);
            mLocked = bundle.getBoolean(SendFragment.LOCKED);
            mSourceWallet = mCoreAPI.getWalletFromUUID(bundle.getString(SendFragment.FROM_WALLET_UUID));
            mWalletForConversions = mSourceWallet;
            if (mIsUUID) {
                mToWallet = mCoreAPI.getWalletFromUUID(mUUIDorURI);
            }
        }

        if(mSpendTarget != null) {
            _sendTo = mSpendTarget.getSpend().getSzName();
            mIsUUID = false;
            _destUUID = mSpendTarget.getSpend().getSzDestUUID();
            if (_destUUID != null) {
                mIsUUID = true;
                mToWallet = mCoreAPI.getWalletFromUUID(_destUUID);
            }
            mAmountToSendSatoshi = mSpendTarget.getSpendAmount();
            mLocked = !mSpendTarget.getSpend().getAmountMutable();
        }

        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSourceWallet = mWallets.get(i);
                updateTextFieldContents(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        mBitcoinField.setEnabled(!mLocked);
        mBitcoinField.setFocusable(!mLocked);
        mFiatField.setEnabled(!mLocked);
        mFiatField.setFocusable(!mLocked);
        mWalletSpinner.setEnabled(!mLocked);
        mWalletSpinner.setFocusable(!mLocked);
        if (mLocked) {
            mMaxButton.setVisibility(View.INVISIBLE);
        } else {
            mMaxButton.setVisibility(View.VISIBLE);
        }

        if (mToWallet != null) {
            mToEdittext.setText(mToWallet.getName());
        } else {
            String temp = _sendTo;
            if (_sendTo.length() > 20) {
                temp = _sendTo.substring(0, 5) + "..." + _sendTo.substring(_sendTo.length() - 5, _sendTo.length());
            }
            mToEdittext.setText(temp);
        }

        mActivity.showNavBar(); // in case we came from backing out of SuccessFragment
        mParentLayout.requestFocus(); //Take focus away first

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

        mBTCSignTextview.setTypeface(mBitcoinTypeface);
        mBTCSignTextview.setText(mCoreAPI.getUserBTCSymbol());
        mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
        mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronym(mWalletForConversions.getCurrencyNum()));
        mFiatSignTextView.setText(mCoreAPI.getCurrencyDenomination(mWalletForConversions.getCurrencyNum()));
        mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mWalletForConversions.getCurrencyNum()));

        mAutoUpdatingTextFields = false;

        mMaxLocked = false;

        mInvalidEntryCount = getInvalidEntryCount();

    }

    @Override
    public void onPause() {
        super.onPause();
        mSavedBitcoin = mAmountToSendSatoshi;
        if (mCalculateFeesTask != null)
            mCalculateFeesTask.cancel(true);
        if (mMaxAmountTask != null)
            mMaxAmountTask.cancel(true);
        mCoreAPI.setOnWalletLoadedListener(null);
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
            return mSpendTarget.maxSpendable(mSourceWallet.getUUID());
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
                mSpendTarget.setSpendAmount(max);
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
            return mSpendTarget.calcSendFees(mSourceWallet.getUUID());
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

    public class SendOrTransferTask extends AsyncTask<Void, Void, String> {
        private final double mAmountFiat;
        private Wallet mFromWallet;

        SendOrTransferTask(Wallet fromWallet, double amountFiat) {
            mFromWallet = fromWallet;
            mAmountFiat = amountFiat;
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "SEND called");
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Initiating SEND");
            return mSpendTarget.approve(mFromWallet.getUUID(), mAmountFiat);
        }

        @Override
        protected void onPostExecute(final String txResult) {
            Log.d(TAG, "SEND done");
            mSendOrTransferTask = null;
            if (txResult == null) {
                Log.d(TAG, "Error during send ");
                if (mActivity != null) {
                    mActivity.popFragment(); // stop the sending screen
                    mDelayedMessage = mActivity.getResources().getString(R.string.fragment_send_confirmation_send_error_title);
                    mHandler.postDelayed(mDelayedErrorMessage, 500);
                }
                if (null != exitHandler) {
                    exitHandler.error();
                }
            } else {
                if (mActivity != null) {
                    saveInvalidEntryCount(0);
                    AudioPlayer.play(mActivity, R.raw.bitcoin_sent);
                    mActivity.popFragment(); // stop sending screen
                    if (null != exitHandler) {
                        exitHandler.success(txResult);
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                mActivity.popFragment(); // confirmation screen
                            }
                        }, 500);
                    } else {
                        mFundsSent = true;
                        String returnUrl = mSpendTarget.getSpend().getSzRet();
                        mActivity.onSentFunds(mFromWallet.getUUID(), txResult, returnUrl);
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            mActivity.popFragment(); // stop the sending screen
            mSendOrTransferTask = null;
        }
    }

    Runnable mDelayedErrorMessage = new Runnable() {
        @Override
        public void run() {
            if (mDelayedMessage != null) {
                mActivity.ShowFadingDialog(mDelayedMessage);
            }
        }
    };

    @Override
    public void onWalletsLoaded() {
        mWallets = mCoreAPI.getCoreActiveWallets();
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.SendFrom, true);
        mWalletSpinner.setAdapter(dataAdapter);
        for(int i=0; i<mWallets.size(); i++) {
            if(mWallets.get(i).getName().equals(mSourceWallet.getName())) {
                mWalletSpinner.setSelection(i, false);
            }
        }
    }

}
