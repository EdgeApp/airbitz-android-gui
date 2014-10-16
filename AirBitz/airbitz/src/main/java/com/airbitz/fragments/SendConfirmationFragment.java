package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

/**
 * Created on 2/21/14.
 */
public class SendConfirmationFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private final String SATOSHIS = "satoshisToSave";

    private TextView mFromEdittext;
    private TextView mToEdittext;
    private EditText mPinEdittext;

    private TextView mTitleTextView;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mSlideTextView;
    private TextView mPinTextView;
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

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);

        mDummyFocus = mView.findViewById(R.id.fragment_sendconfirmation_dummy_focus);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_parent);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_sendconfirmation_back_button);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_sendconfirmation_help_button);
        mConfirmSwipeButton = (ImageButton) mView.findViewById(R.id.button_confirm_swipe);

        mCalculator = mActivity.getCalculatorView();

        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);
        mSlideTextView = (TextView) mView.findViewById(R.id.textview_slide);
        mPinTextView = (TextView) mView.findViewById(R.id.textview_pin);
        mConversionTextView = (TextView) mView.findViewById(R.id.textview_conversion);
        mBTCSignTextview = (TextView) mView.findViewById(R.id.send_confirmation_btc_sign);
        mBTCDenominationTextView = (TextView) mView.findViewById(R.id.send_confirmation_btc_denomination);
        mFiatDenominationTextView = (TextView) mView.findViewById(R.id.send_confirmation_fiat_denomination);
        mFiatSignTextView = (TextView) mView.findViewById(R.id.send_confirmation_fiat_sign);
        mMaxButton = (HighlightOnPressButton) mView.findViewById(R.id.button_max);

        mFromEdittext = (TextView) mView.findViewById(R.id.textview_from_name);
        mToEdittext = (TextView) mView.findViewById(R.id.textview_to_name);
        mPinEdittext = (EditText) mView.findViewById(R.id.edittext_pin);

        mBitcoinField = (EditText) mView.findViewById(R.id.button_bitcoin_balance);
        mFiatField = (EditText) mView.findViewById(R.id.button_dollar_balance);

        mSlideLayout = (RelativeLayout) mView.findViewById(R.id.layout_slide);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mFromEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mConversionTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPinTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mSlideTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

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
                if (editable.length() >= 4) {
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mPinEdittext.getWindowToken(), 0);
                    mParentLayout.requestFocus();
                }
            }
        };
        mPinEdittext.addTextChangedListener(mPINTextWatcher);

        mPinEdittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        mPinEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d(TAG, "PIN field focus changed");
                if (hasFocus) {
                    mAutoUpdatingTextFields = true;
                    showPINkeyboard();
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

        TextView.OnEditorActionListener tvListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    mPinEdittext.requestFocus();
                    return true;
                }
                return false;
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
                if (mMaxAmountTask != null)
                    mMaxAmountTask.cancel(true);
                mMaxAmountTask = new MaxAmountTask();
                mMaxAmountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    if(event.getRawX() >= (mConversionTextView.getRight() - mConversionTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        mActivity.pushFragment(new HelpFragment(HelpFragment.SEND_CONFIRMATION_INSUFFICIENT_FUNDS), NavigationActivity.Tabs.SEND.ordinal());
                        return true;
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
        mFiatField.setText("");
        mBitcoinField.setText("");
        mConversionTextView.setTextColor(Color.WHITE);
        mConversionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mBitcoinField.setTextColor(Color.WHITE);
        mFiatField.setTextColor(Color.WHITE);
        mAutoUpdatingTextFields = false;
    }

    private void showPINkeyboard() {
        ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mPinEdittext, 0);
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
        calculateFees();
    }

    private void calculateFees() {
        if (mCalculateFeesTask != null) {
            mCalculateFeesTask.cancel(true);
        }
        mCalculateFeesTask = new CalculateFeesTask();
        mCalculateFeesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        //make sure PIN is good
        String enteredPIN = mPinEdittext.getText().toString();
        String userPIN = mCoreAPI.GetUserPIN();
        if ((mFees + mAmountToSendSatoshi) > mSourceWallet.getBalanceSatoshi()) {
            mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_send_confirmation_send_error_title), getResources().getString(R.string.fragment_send_confirmation_insufficient_funds_message));
            resetSlider();
        } else if (mAmountToSendSatoshi == 0) {
            resetSlider();
            mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_send_no_satoshi_title), getResources().getString(R.string.fragment_send_no_satoshi_message));
        } else if (enteredPIN != null && userPIN != null && userPIN.equals(enteredPIN)) {
            // show the sending screen
            SuccessFragment mSuccessFragment = new SuccessFragment();
            Bundle bundle = new Bundle();
            bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);
            mSuccessFragment.setArguments(bundle);
            mActivity.pushFragment(mSuccessFragment, NavigationActivity.Tabs.SEND.ordinal());

            mSendOrTransferTask = new SendOrTransferTask(mSourceWallet, mUUIDorURI, mAmountToSendSatoshi, mLabel);
            mSendOrTransferTask.execute();
            finishSlider();
        } else {
            resetSlider();
            mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_send_incorrect_pin_title), getResources().getString(R.string.fragment_send_incorrect_pin_message));
            mPinEdittext.requestFocus();
        }
    }

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
        mPinEdittext = (EditText) mView.findViewById(R.id.edittext_pin);

        mAutoUpdatingTextFields = true;

        if (mSavedBitcoin > -1) {
            mAmountToSendSatoshi = mSavedBitcoin;
            mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false));
            if (mWalletForConversions != null) {
                mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mWalletForConversions.getCurrencyNum(), false, false));
            }
            mPinEdittext.requestFocus();
        } else {
            mFiatField.setText("");
            mBitcoinField.setText("");
            if (mActivity.isLargeDpi()) {
                mFiatField.requestFocus();
            }
        }

        mPinTextView = (TextView) mView.findViewById(R.id.textview_pin);
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
                mAmountMax = max;
                mAmountToSendSatoshi = max;
                mAutoUpdatingTextFields = true;
                mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mWalletForConversions.getCurrencyNum(), false, false));
                mFiatSignTextView.setText(mCoreAPI.getCurrencyDenomination(mWalletForConversions.getCurrencyNum()));
                mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mWalletForConversions.getCurrencyNum()));
                mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false));

                calculateFees();
                mPinEdittext.requestFocus();
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
                if (mActivity != null)
                    mActivity.onSentFunds(mFromWallet.getUUID(), txResult.getTxId());
            }
        }

        @Override
        protected void onCancelled() {
            mActivity.popFragment(); // stop the sending screen
            mSendOrTransferTask = null;
        }
    }
}
