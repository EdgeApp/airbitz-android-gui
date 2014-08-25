package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.airbitz.api.tABC_CC;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.models.Wallet;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.Calculator;

/**
 * Created on 2/21/14.
 */
public class SendConfirmationFragment extends Fragment {

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
    private long mAmountToSendSatoshi;

    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    private Wallet mSourceWallet, mToWallet;

    private boolean mAutoUpdatingTextFields = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();


        mActivity = (NavigationActivity) getActivity();

        bundle = this.getArguments();
        if (bundle == null) {
            System.out.println("Send confirmation bundle is null");
        } else {
            mUUIDorURI = bundle.getString(SendFragment.UUID);
            mLabel = bundle.getString(SendFragment.LABEL);
            mAmountToSendSatoshi = bundle.getLong(SendFragment.AMOUNT_SATOSHI);
            mIsUUID = bundle.getBoolean(SendFragment.IS_UUID);
            mSourceWallet = mCoreAPI.getWallet(bundle.getString(SendFragment.FROM_WALLET_UUID));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_send_confirmation, container, false);

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);

        mDummyFocus = mView.findViewById(R.id.fragment_sendconfirmation_dummy_focus);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_parent);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_sendconfirmation_back_button);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_sendconfirmation_help_button);
        mConfirmSwipeButton = (ImageButton) mView.findViewById(R.id.button_confirm_swipe);

        mCalculator = ((NavigationActivity)getActivity()).getCalculatorView();

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
//        mBitcoinFeeLabel = (TextView) view.findViewById();
        mFiatField = (EditText) mView.findViewById(R.id.button_dollar_balance);
//        mDollarFeeLabel = (TextView) view.findViewById();

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

        String balance = mCoreAPI.getUserBTCSymbol()+" "+mCoreAPI.FormatDefaultCurrency(mSourceWallet.getBalanceSatoshi(), true, false);
        mFromEdittext.setText(mSourceWallet.getName()+" ("+balance+")");
        if(mIsUUID) {
            mToWallet = mCoreAPI.getWallet(mUUIDorURI);
            mToEdittext.setText(mToWallet.getName());
        }
        else {
            String temp = mUUIDorURI;
            if(mUUIDorURI.length()>20) {
                temp = mUUIDorURI.substring(0, 5) + "..." + mUUIDorURI.substring(mUUIDorURI.length()-5, mUUIDorURI.length());
            }
            mToEdittext.setText(temp);
        }

        if(mAmountToSendSatoshi==0) {
            mBitcoinField.setText("");
            mFiatField.setText("");
        } else {
            String out = mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mSourceWallet.getCurrencyNum(), false, false);
            mFiatField.setText(out);
            mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false, 2));
            mPinEdittext.requestFocus();
        }

        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()>=4) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
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
                Log.d("SendConfirmationFragment", "PIN field focus changed");
                if(hasFocus) {
                    mAutoUpdatingTextFields = true;
                    showPINkeyboard();
                } else {
                    mAutoUpdatingTextFields = false;
                }
            }
        });

        mBTCTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!mAutoUpdatingTextFields && !mBitcoinField.getText().toString().isEmpty()) {
                    updateTextFieldContents(true);
                    mBitcoinField.setSelection(mBitcoinField.getText().toString().length());
                }
            }
        };
        mBitcoinField.addTextChangedListener(mBTCTextWatcher);

        mFiatTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!mAutoUpdatingTextFields && !mFiatField.getText().toString().isEmpty()) {
                    updateTextFieldContents(false);
                    mFiatField.setSelection(mFiatField.getText().toString().length());
                }
            }
        };
        mFiatField.addTextChangedListener(mFiatTextWatcher);

        mBitcoinField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d("SendConfirmationFragment", "Bitcoin field focus changed");
                if (hasFocus) {
                    resetFiatAndBitcoinFields();
                    mCalculator.setEditText(mBitcoinField);
                    ((NavigationActivity) getActivity()).showCalculator();
                } else {
                    ((NavigationActivity) getActivity()).hideCalculator();
                }
            }
        });

        mFiatField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d("SendConfirmationFragment", "Fiat field focus changed");
                if (hasFocus) {
                    resetFiatAndBitcoinFields();
                    mCalculator.setEditText(mFiatField);
                    ((NavigationActivity) getActivity()).showCalculator();
                } else {
                    ((NavigationActivity) getActivity()).hideCalculator();
                }
            }
        });

        TextView.OnEditorActionListener tvListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (keyEvent!=null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode()==KeyEvent.KEYCODE_ENTER) {
                    mPinEdittext.requestFocus();
                    return true;
                }
                return false;
            }
        };

        View.OnTouchListener preventOSKeyboard = new View.OnTouchListener() {
            public boolean onTouch (View v, MotionEvent event) {
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
                if(mMaxAmountTask!=null)
                    mMaxAmountTask.cancel(true);
                mMaxAmountTask = new MaxAmountTask();
                mMaxAmountTask.execute();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).pushFragment(new HelpFragment(HelpFragment.SEND_CONFIRMATION), NavigationActivity.Tabs.SEND.ordinal());
            }
        });

        mDummyFocus.requestFocus();

        if(mAmountToSendSatoshi>0) {
            mPinEdittext.requestFocus();
        }
        return mView;
    }

    private void resetFiatAndBitcoinFields() {
        mAutoUpdatingTextFields = true;
        mFiatField.setText("");
        mBitcoinField.setText("");
        mConversionTextView.setTextColor(Color.WHITE);
        mBitcoinField.setTextColor(Color.WHITE);
        mFiatField.setTextColor(Color.WHITE);
        mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mSourceWallet.getCurrencyNum()));
        mAutoUpdatingTextFields = false;
    }

    private void showPINkeyboard() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mPinEdittext, 0);
    }


    public void touchEventsEnded() {
        int successThreshold = mLeftThreshold + (mSlideLayout.getWidth() / 4);
        if (mConfirmSwipeButton.getX() <= successThreshold) {
            attemptInitiateSend();
        } else {
            resetSlider();
        }
    }

    private void updateTextFieldContents(boolean btc)
    {
        double currency;
        long satoshi;

        mAutoUpdatingTextFields = true;
        if (btc) {
            mAmountToSendSatoshi = mCoreAPI.denominationToSatoshi(mBitcoinField.getText().toString());
            mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mSourceWallet.getCurrencyNum(), false, false));
       }
        else {
            try
            {
                currency = Double.valueOf(mFiatField.getText().toString());
                satoshi = mCoreAPI.CurrencyToSatoshi(currency, mSourceWallet.getCurrencyNum());
                mAmountToSendSatoshi = satoshi;
                int currencyDecimalPlaces = 2; //TODO where does this come from?
                mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false, currencyDecimalPlaces));
            }
            catch(NumberFormatException e) {  } //not a double, ignore
        }
        mAutoUpdatingTextFields = false;
        if(mCalculateFeesTask != null)
            mCalculateFeesTask.cancel(true);
        mCalculateFeesTask = new CalculateFeesTask();
        mCalculateFeesTask.execute();
    }

    /**
     * Wrap the fee calculation in an AsyncTask
     */
    private MaxAmountTask mMaxAmountTask;
    public class MaxAmountTask extends AsyncTask<Void, Void, Long> {

        MaxAmountTask() { }

        @Override
        protected Long doInBackground(Void... params) {
            String dest = mIsUUID ? mToWallet.getUUID() : mUUIDorURI;
            return mCoreAPI.maxSpendable(mSourceWallet.getUUID(), dest, mIsUUID);
        }

        @Override
        protected void onPostExecute(final Long max) {
            mMaxAmountTask = null;
            if(max<0) {
                Log.d("SendConfirmationFragment", "Max calculation error");
            }
            mAmountToSendSatoshi = max;
            mAutoUpdatingTextFields = true;
            mFiatField.setText(mCoreAPI.FormatCurrency(mAmountToSendSatoshi, mSourceWallet.getCurrencyNum(), false, false));
            mBitcoinField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false, 2));
            mAutoUpdatingTextFields = false;
        }

        @Override
        protected void onCancelled() {
            mMaxAmountTask = null;
        }
    }

    private CalculateFeesTask mCalculateFeesTask;
    public class CalculateFeesTask extends AsyncTask<Void, Void, Long> {

        CalculateFeesTask() { }

        @Override
        protected Long doInBackground(Void... params) {
            String dest = mIsUUID ? mToWallet.getUUID() : mUUIDorURI;
            return mCoreAPI.calcSendFees(mSourceWallet.getUUID(), dest, mAmountToSendSatoshi, mIsUUID);
        }

        @Override
        protected void onPostExecute(final Long fees) {
            if(getActivity()==null)
                return;
            mCalculateFeesTask = null;
            UpdateFeeFields(fees);
        }

        @Override
        protected void onCancelled() {
            mCalculateFeesTask = null;
        }
    }

    private void UpdateFeeFields(Long fees) {
        mAutoUpdatingTextFields = true;
        if(fees<0) {
            Log.d("SendConfirmationFragment", "Fee calculation error");
        }
        else if ((fees+mAmountToSendSatoshi) <= mSourceWallet.getBalanceSatoshi())
        {
            mConversionTextView.setTextColor(Color.WHITE);
            mBitcoinField.setTextColor(Color.WHITE);
            mFiatField.setTextColor(Color.WHITE);

            String coinFeeString = "+ " + mCoreAPI.formatSatoshi(fees, false);
            mBTCDenominationTextView.setText(coinFeeString+" "+mCoreAPI.getDefaultBTCDenomination());

            double fiatFee = mCoreAPI.SatoshiToCurrency(fees, mSourceWallet.getCurrencyNum());
            String fiatFeeString = "+ "+mCoreAPI.formatCurrency(fiatFee, mSourceWallet.getCurrencyNum(), false);
            mFiatDenominationTextView.setText(fiatFeeString+" "+mCoreAPI.getUserCurrencyAcronym());
            mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mSourceWallet.getCurrencyNum()));
        }
        else
        {
            mConversionTextView.setText(getActivity().getResources().getString(R.string.fragment_send_confirmation_insufficient_funds));
            mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
            mFiatDenominationTextView.setText(mCoreAPI.getUserCurrencyAcronym());
            mConversionTextView.setTextColor(Color.RED);
            mBitcoinField.setTextColor(Color.RED);
            mFiatField.setTextColor(Color.RED);
        }
        mAutoUpdatingTextFields = false;
    }

    private void attemptInitiateSend() {
        //make sure PIN is good
        String enteredPIN = mPinEdittext.getText().toString();
        String userPIN = mCoreAPI.GetUserPIN();
        mAmountToSendSatoshi = mCoreAPI.denominationToSatoshi(mBitcoinField.getText().toString());
        if( !mBitcoinField.getText().toString().isEmpty() && Float.valueOf(mBitcoinField.getText().toString())<0) {
            resetSlider();
            ((NavigationActivity)getActivity()).ShowOkMessageDialog("Invalid Amount", "Invalid Amount");
        }else if(mAmountToSendSatoshi==0) {
            resetSlider();
            ((NavigationActivity)getActivity()).ShowOkMessageDialog(getResources().getString(R.string.fragment_send_no_satoshi_title), getResources().getString(R.string.fragment_send_no_satoshi_message));
        } else if (userPIN!=null && userPIN.equals(enteredPIN)) {
            mSendOrTransferTask = new SendOrTransferTask(mSourceWallet, mUUIDorURI, mAmountToSendSatoshi);
            mSendOrTransferTask.execute();
            finishSlider();
        } else {
            resetSlider();
            ((NavigationActivity)getActivity()).ShowOkMessageDialog(getResources().getString(R.string.fragment_send_incorrect_pin_title), getResources().getString(R.string.fragment_send_incorrect_pin_message));
        }
    }

    private void resetSlider() {
        Animator animator = ObjectAnimator.ofFloat(mConfirmSwipeButton, "translationX",-(mRightThreshold-mConfirmSwipeButton.getX()),0);
        animator.setDuration(300);
        animator.setStartDelay(0);
        animator.start();
    }

    private void finishSlider(){
        Animator animator = ObjectAnimator.ofFloat(mConfirmSwipeButton, "translationX",-(mRightThreshold-mConfirmSwipeButton.getX()),-(mRightThreshold-(mLeftThreshold - mConfirmCenter)));
        animator.setDuration(300);
        animator.setStartDelay(0);
        animator.start();
    }

    /**
     * Represents an asynchronous creation of the first wallet
     */
    private SendOrTransferTask mSendOrTransferTask;

    public class SendOrTransferTask extends AsyncTask<Void, Void, CoreAPI.TxResult> {
        private Wallet mFromWallet;
        private final String mAddress;
        private final long mSatoshi;
        private SuccessFragment mSuccessFragment;
        private String failInsufficientMessage = getResources().getString(R.string.fragment_send_failure_insufficient_funds);
        private String failOtherMessage = getResources().getString(R.string.fragment_send_failure_other_error);

        SendOrTransferTask(Wallet fromWallet, String address, long amount) {
            mFromWallet = fromWallet;
            mAddress = address;
            mSatoshi = amount;

            Bundle bundle = new Bundle();
            bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);

            mSuccessFragment = new SuccessFragment();
            mSuccessFragment.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(mSuccessFragment, NavigationActivity.Tabs.SEND.ordinal());
        }

        @Override
        protected CoreAPI.TxResult doInBackground(Void... params) {
            return mCoreAPI.InitiateTransferOrSend(mFromWallet, mAddress, mSatoshi);
        }

        @Override
        protected void onPostExecute(final CoreAPI.TxResult txResult) {
            mSendOrTransferTask = null;
            tABC_CC result = txResult.getError();
            String message;
            if (txResult.getError() != null) {
                if (result == tABC_CC.ABC_CC_InsufficientFunds) {
                    message = failInsufficientMessage;
                } else if (result == tABC_CC.ABC_CC_ServerError) {
                    message = (txResult.getString());
                } else {
                    message = failOtherMessage;
                }

                mSuccessFragment.revokeSend(message);
            }
        }

        @Override
        protected void onCancelled() {
            mSendOrTransferTask = null;
        }
    }

    @Override public void onResume() {
        mActivity.showNavBar(); // in case we came from backing out of SuccessFragment
        mParentLayout.requestFocus(); //Take focus away first
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        String btc = mBitcoinField.getText().toString();
        String fiat = mFiatField.getText().toString();
        if(btc.isEmpty() && fiat.isEmpty()) {
            mFiatField.requestFocus();
        } else if(mPinEdittext.getText().toString().isEmpty()) {
            mPinEdittext.requestFocus();
        }
        mBTCSignTextview.setText(mCoreAPI.getUserBTCSymbol());
        mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
        mFiatDenominationTextView.setText(mCoreAPI.getUserCurrencyAcronym());
        mFiatSignTextView.setText(mCoreAPI.getUserCurrencyDenomination());
        mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mSourceWallet.getCurrencyNum()));
        super.onResume();
    }

    @Override public void onPause() {
        super.onPause();
        if(mCalculateFeesTask !=null)
            mCalculateFeesTask.cancel(true);
    }
}
