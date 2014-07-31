package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.HighlightOnPressButton;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.utils.CalculatorBrain;
import com.airbitz.utils.Common;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created on 2/21/14.
 */
public class SendConfirmationFragment extends Fragment implements View.OnClickListener {

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

    private boolean mConfirmChecked = false;

    private View mDummyFocus;

    private EditText mFiatField;
    private EditText mBitcoinField;
    private TextView mBitcoinFeeLabel;
    private TextView mDollarFeeLabel;

    private ImageButton mHelpButton;
    private ImageButton mBackButton;
    private ImageButton mConfirmSwipeButton;

    private float mConfirmCenter;
    private float dX = 0;
    private float rX = 0;

    private Boolean userIsInTheMiddleOfTypingANumber = false;
    private CalculatorBrain mCalculatorBrain;
    private static final String DIGITS = "0123456789.";

    DecimalFormat mDF = new DecimalFormat("@###########");
    private boolean doSet = false;

    private RelativeLayout mSlideLayout;


    private RelativeLayout mParentLayout;

    private ScrollView mScrollView;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mRightThreshold;
    private int mLeftThreshold;

    private boolean mSuccess = false;

    private String mUUIDorURI;
    private String mLabel;
    private Boolean mIsUUID;
    private long mAmountToSendSatoshi;

    private CoreAPI mCoreAPI;
    private Wallet mSourceWallet, mToWallet;

    private boolean mAutoUpdatingTextFields = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();

        bundle = this.getArguments();
        if (bundle == null) {
            System.out.println("Send confirmation bundle is null");
        } else {
            mUUIDorURI = bundle.getString(SendFragment.UUID);
            mLabel = bundle.getString(SendFragment.LABEL);
            mAmountToSendSatoshi = bundle.getLong(SendFragment.AMOUNT_SATOSHI);
            mIsUUID = bundle.getBoolean(SendFragment.IS_UUID);
            mSourceWallet = mCoreAPI.getWalletFromName(bundle.getString(SendFragment.FROM_WALLET_NAME));
        }
    }

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_send_confirmation, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);

        mDummyFocus = mView.findViewById(R.id.fragment_sendconfirmation_dummy_focus);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_parent);

        mBackButton = (ImageButton) mView.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.button_help);
        mConfirmSwipeButton = (ImageButton) mView.findViewById(R.id.button_confirm_swipe);

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
        mScrollView = (ScrollView) mView.findViewById(R.id.layout_scroll);

        mConfirmCenter = mConfirmSwipeButton.getWidth() / 2;

        mCalculatorBrain = new CalculatorBrain();
        mDF.setMinimumFractionDigits(0);
        mDF.setMaximumFractionDigits(6);
        mDF.setMinimumIntegerDigits(1);
        mDF.setMaximumIntegerDigits(8);

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

        mPinEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d("SendConfirmationFragment", "PIN field focus changed");
                if(hasFocus) {
                    mAutoUpdatingTextFields = true;
                    showKeyboard();
                } else {
                    mAutoUpdatingTextFields = false;
                }
            }
        });

        mBitcoinField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d("SendConfirmationFragment", "Bitcoin field focus changed");
                if (hasFocus) {
                    EditText edittext = (EditText) view;
                    int inType = edittext.getInputType();
                    edittext.setInputType(InputType.TYPE_NULL);
                    edittext.setInputType(inType);
                    mAutoUpdatingTextFields = true;
                    mFiatField.setText("");
                    mBitcoinField.setText("");
                    mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mSourceWallet.getCurrencyNum()));
                    mConversionTextView.setTextColor(Color.WHITE);
                    mAutoUpdatingTextFields = false;
                    showCustomKeyboard(view);
                } else {
                    hideCustomKeyboard();
                }
            }
        });

        mFiatField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d("SendConfirmationFragment", "Fiat field focus changed");
                EditText edittext = (EditText) view;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.setInputType(inType);
                if (hasFocus) {
                    mAutoUpdatingTextFields = true;
                    mFiatField.setText("");
                    mBitcoinField.setText("");
                    mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mSourceWallet.getCurrencyNum()));
                    mConversionTextView.setTextColor(Color.WHITE);
                    mAutoUpdatingTextFields = false;
                    showCustomKeyboard(view);
                } else {
                    hideCustomKeyboard();
                }
            }
        });

        View.OnTouchListener preventOSKeyboard = new View.OnTouchListener() {
            public boolean onTouch (View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true; // the listener has consumed the event
            }
        };

        mBitcoinField.setOnTouchListener(preventOSKeyboard);
        mFiatField.setOnTouchListener(preventOSKeyboard);

        Shader textShader = new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"), Color.parseColor("#addff1")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mSlideTextView.getPaint().setShader(textShader);


        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });


        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        mSlideLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mSuccess) {
                    mLeftThreshold = (int) mSlideLayout.getX();
                }
            }
        });

        /*mConfirmSwipeButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mSuccess) {
                    mRightThreshold = (int) mConfirmSwipeButton.getX();
                }
            }
        });*/

        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mActivePointerId = event.getPointerId(0);
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
                        //mConfirmSwipeButton.invalidate();
                        return false;
                    case MotionEvent.ACTION_UP:
                        touchEventsEnded();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        resetSlider();
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                        /*pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                        final int pointerId = event.getPointerId(pointerIndex);
                        if (pointerId == mActivePointerId) {
                            touchEventsEnded();
                        }*/
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
                final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() > 100) {
                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(0, 0);
                }
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        mDummyFocus.requestFocus();

        return mView;
    }

    private void showKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, 0);
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

            String coinFeeString = "+ " + mCoreAPI.formatSatoshi(fees, false) + " " + mCoreAPI.getUserCurrencyDenomination();

            double fiatFee = mCoreAPI.SatoshiToCurrency(fees, mSourceWallet.getCurrencyNum());
            String fiatFeeString = "+ "+mCoreAPI.formatDefaultCurrency(fiatFee)+" "+mCoreAPI.getUserCurrencyAcronym();

            if(mBitcoinFeeLabel!=null) mBitcoinFeeLabel.setText(coinFeeString);
            if(mDollarFeeLabel!=null) mDollarFeeLabel.setText(fiatFeeString);
            mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mSourceWallet.getCurrencyNum()));
        }
        else
        {
            mConversionTextView.setText(getActivity().getResources().getString(R.string.fragment_send_confirmation_insufficient_funds));
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
            showMessageAlert("Invalid Amount", "Invalid Amount");
        }else if(mAmountToSendSatoshi==0) {
            resetSlider();
            showMessageAlert(getResources().getString(R.string.fragment_send_no_satoshi_title), getResources().getString(R.string.fragment_send_no_satoshi_message));
        } else if (userPIN!=null && userPIN.equals(enteredPIN)) {
            mSendOrTransferTask = new SendOrTransferTask(mSourceWallet, mUUIDorURI, mAmountToSendSatoshi);
            mSendOrTransferTask.execute();
            finishSlider();
        } else {
            resetSlider();
            showMessageAlert(getResources().getString(R.string.fragment_send_incorrect_pin_title), getResources().getString(R.string.fragment_send_incorrect_pin_message));
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
    public class SendOrTransferTask extends AsyncTask<Void, Void, String> {
        private Wallet mFromWallet;
        private final String mAddress;
        private final long mSatoshi;

        SendOrTransferTask(Wallet fromWallet, String address, long amount) {
            mFromWallet = fromWallet;
            mAddress = address;
            mSatoshi = amount;
        }

        @Override
        protected String doInBackground(Void... params) {
            return mCoreAPI.InitiateTransferOrSend(mFromWallet, mAddress, mSatoshi);
        }

        @Override
        protected void onPostExecute(final String txid) {
            mSendOrTransferTask = null;
           if (txid==null) {
                Log.d("SendConfirmationFragment", "Send or Transfer failed");
            } else {
               Bundle bundle = new Bundle();
               bundle.putString(WalletsFragment.FROM_SOURCE,"SEND");
               bundle.putString(Transaction.TXID, txid);
               bundle.putString(Wallet.WALLET_UUID, mFromWallet.getUUID());

               Fragment frag = new ReceivedSuccessFragment();
               frag.setArguments(bundle);
               ((NavigationActivity)getActivity()).pushFragment(frag);
            }
        }

        @Override
        protected void onCancelled() {
            mSendOrTransferTask = null;
        }
    }



    private void showMessageAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setupCalculator(View l) {
        l.findViewById(R.id.button_calc_0).setOnClickListener(this);
        l.findViewById(R.id.button_calc_1).setOnClickListener(this);
        l.findViewById(R.id.button_calc_2).setOnClickListener(this);
        l.findViewById(R.id.button_calc_3).setOnClickListener(this);
        l.findViewById(R.id.button_calc_4).setOnClickListener(this);
        l.findViewById(R.id.button_calc_5).setOnClickListener(this);
        l.findViewById(R.id.button_calc_6).setOnClickListener(this);
        l.findViewById(R.id.button_calc_7).setOnClickListener(this);
        l.findViewById(R.id.button_calc_8).setOnClickListener(this);
        l.findViewById(R.id.button_calc_9).setOnClickListener(this);

        l.findViewById(R.id.button_calc_plus).setOnClickListener(this);
        l.findViewById(R.id.button_calc_minus).setOnClickListener(this);
        l.findViewById(R.id.button_calc_multiply).setOnClickListener(this);
        l.findViewById(R.id.button_calc_division).setOnClickListener(this);
        l.findViewById(R.id.button_calc_percent).setOnClickListener(this);
        l.findViewById(R.id.button_calc_equal).setOnClickListener(this);
        l.findViewById(R.id.button_calc_c).setOnClickListener(this);
        l.findViewById(R.id.button_calc_dot).setOnClickListener(this);
        l.findViewById(R.id.button_calc_done).setOnClickListener(this);
        l.findViewById(R.id.button_calc_back).setOnClickListener(this);
    }

    private void removeCalculator(View l) {
        l.findViewById(R.id.button_calc_0).setOnClickListener(null);
        l.findViewById(R.id.button_calc_1).setOnClickListener(null);
        l.findViewById(R.id.button_calc_2).setOnClickListener(null);
        l.findViewById(R.id.button_calc_3).setOnClickListener(null);
        l.findViewById(R.id.button_calc_4).setOnClickListener(null);
        l.findViewById(R.id.button_calc_5).setOnClickListener(null);
        l.findViewById(R.id.button_calc_6).setOnClickListener(null);
        l.findViewById(R.id.button_calc_7).setOnClickListener(null);
        l.findViewById(R.id.button_calc_8).setOnClickListener(null);
        l.findViewById(R.id.button_calc_9).setOnClickListener(null);

        l.findViewById(R.id.button_calc_plus).setOnClickListener(null);
        l.findViewById(R.id.button_calc_minus).setOnClickListener(null);
        l.findViewById(R.id.button_calc_multiply).setOnClickListener(null);
        l.findViewById(R.id.button_calc_division).setOnClickListener(null);
        l.findViewById(R.id.button_calc_percent).setOnClickListener(null);
        l.findViewById(R.id.button_calc_equal).setOnClickListener(null);
        l.findViewById(R.id.button_calc_c).setOnClickListener(null);
        l.findViewById(R.id.button_calc_dot).setOnClickListener(null);
        l.findViewById(R.id.button_calc_done).setOnClickListener(null);
        l.findViewById(R.id.button_calc_back).setOnClickListener(null);
    }

    @Override
    public void onClick(View v) {
        View focusCurrent = getActivity().getWindow().getCurrentFocus();
        if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;
        EditText display = (EditText) focusCurrent;
        Editable editable = display.getText();
        int start = display.getSelectionStart();
        // delete the selection, if chars are selected:
        int end = display.getSelectionEnd();
        if (end > start) {
            editable.delete(start, end);
        }
        String buttonTag = v.getTag().toString();

        if(buttonTag.equals("done")) {
            hideCustomKeyboard();
            mPinEdittext.requestFocus();
        } else if(buttonTag.equals("back")) {
            String s = display.getText().toString();
            if(s.length() == 1) { // 1 character, just set to 0
                mCalculatorBrain.performOperation(CalculatorBrain.CLEAR);
                display.setText("0");
            } else if (s.length() > 1) {
                display.setText(s.substring(0, s.length()-1));
            }

        } else if (DIGITS.contains(buttonTag)) {

            // digit was pressed
            if (userIsInTheMiddleOfTypingANumber) {
                if (buttonTag.equals(".") && display.getText().toString().contains(".")) {
                    // ERROR PREVENTION
                    // Eliminate entering multiple decimals
                } else {
                    display.append(buttonTag);
                }
            } else {
                if (buttonTag.equals(".")) {
                    // ERROR PREVENTION
                    // This will avoid error if only the decimal is hit before an operator, by placing a leading zero
                    // before the decimal
                    display.setText(0 + buttonTag);
                } else {
                    display.setText(buttonTag);
                }
                userIsInTheMiddleOfTypingANumber = true;
            }

        } else {
            // operation was pressed
            if (userIsInTheMiddleOfTypingANumber) {
                try {
                    mCalculatorBrain.setOperand(Double.parseDouble(display.getText().toString()));
                } catch(NumberFormatException e) { // ignore any non-double
                }
                userIsInTheMiddleOfTypingANumber = false;
            }

            mCalculatorBrain.performOperation(buttonTag);
            display.setText(mDF.format(mCalculatorBrain.getResult()));
            if(buttonTag.equals("=")) {
//                updateTextFieldContents(display.equals(mBitcoinField));
            }
        }
    }

    public void hideCustomKeyboard() {
        ((NavigationActivity) getActivity()).hideCalculator();
    }

    public void showCustomKeyboard(View v) {
//        hideKeyboard();
        final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
        if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() > 100) {
            final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0, 0);
        }
        ((NavigationActivity) getActivity()).showCalculator();
    }

    @Override public void onResume() {
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
        setupCalculator(((NavigationActivity) getActivity()).getCalculatorView());
        mConversionTextView.setText(mCoreAPI.BTCtoFiatConversion(mSourceWallet.getCurrencyNum()));
        super.onResume();
    }

    @Override public void onPause() {
        super.onPause();
        if(mCalculateFeesTask !=null)
            mCalculateFeesTask.cancel(true);
        removeCalculator(((NavigationActivity) getActivity()).getCalculatorView());
    }
}
