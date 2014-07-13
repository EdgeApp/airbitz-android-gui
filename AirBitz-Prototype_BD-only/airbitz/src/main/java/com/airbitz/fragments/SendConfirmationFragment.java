package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;

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
    private TextView mValueTextView;
    private TextView mSlideTextView;
    private TextView mConfirmTextView;
    private TextView mPinTextView;

    private Bundle bundle;

    private boolean mConfirmChecked = false;

    private EditText mDollarValueField;
    private EditText mBitcoinValueField;

    private ImageButton mHelpButton;
    private ImageButton mBackButton;
    private ImageButton mConfirmSwipeButton;

    private float mConfirmCenter;
    private float dX = 0;
    private float rX = 0;

    private boolean doSet = false;

    private RelativeLayout mSlideLayout;

    private ImageView mConfirmImageView;

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
    private long mAmountToSendSatoshi=0;

    private CoreAPI mCoreAPI;
    private Wallet mSourceWallet, mToWallet;

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
        if(mIsUUID) {
            mToWallet = mCoreAPI.getWallet(mUUIDorURI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_confirmation, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mConfirmSwipeButton = (ImageButton) view.findViewById(R.id.button_confirm_swipe);

        mFromTextView = (TextView) view.findViewById(R.id.textview_from);
        mToTextView = (TextView) view.findViewById(R.id.textview_to);
        mValueTextView = (TextView) view.findViewById(R.id.textview_value);
        mSlideTextView = (TextView) view.findViewById(R.id.textview_slide);
        mConfirmTextView = (TextView) view.findViewById(R.id.textview_confirm);
        mPinTextView = (TextView) view.findViewById(R.id.textview_pin);

        mFromEdittext = (TextView) view.findViewById(R.id.textview_from_name);
        mToEdittext = (TextView) view.findViewById(R.id.textview_to_name);
        mPinEdittext = (EditText) view.findViewById(R.id.edittext_pin);

        mDollarValueField = (EditText) view.findViewById(R.id.button_dollar_balance);
        mBitcoinValueField = (EditText) view.findViewById(R.id.button_bitcoin_balance);

        mSlideLayout = (RelativeLayout) view.findViewById(R.id.layout_slide);

        mConfirmImageView = (ImageView) view.findViewById(R.id.imageview_confirm);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mFromEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mValueTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPinTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mSlideTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mConfirmTextView.setTypeface(NavigationActivity.latoBlackTypeFace);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_root);
        mScrollView = (ScrollView) view.findViewById(R.id.layout_scroll);

        mConfirmCenter = mConfirmSwipeButton.getWidth() / 2;

        mFromEdittext.setText("todo");
        mToEdittext.setText("todo");


        Shader textShader = new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"), Color.parseColor("#addff1")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mSlideTextView.getPaint().setShader(textShader);

        mConfirmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConfirmChecked) {
                    mConfirmImageView.setImageResource(R.drawable.btn_confirm_off);
                    mConfirmChecked = false;
                } else {
                    mConfirmImageView.setImageResource(R.drawable.btn_confirm_on);
                    mConfirmChecked = true;
                }
            }
        });

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

        mConfirmSwipeButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mSuccess) {
                    mRightThreshold = (int) mConfirmSwipeButton.getX();
                }
            }
        });

        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mActivePointerId = event.getPointerId(0);
                        dX = (int) event.getX();
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
                        mConfirmSwipeButton.setX(mRightThreshold);
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

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        mDollarValueField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTextFieldContents(false);
            }
        });

        mBitcoinValueField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTextFieldContents(true);
            }
        });

        return view;
    }

    public void touchEventsEnded() {
        int successThreshold = mLeftThreshold + (mSlideLayout.getWidth() / 4);
        if (mConfirmSwipeButton.getX() <= successThreshold) {
            attemptInitiateSend();
        } else {
            mConfirmSwipeButton.setX(mRightThreshold);
        }
    }

    private void updateTextFieldContents(boolean btc)
    {
        double currency;
        long satoshi;
        tABC_Error error = new tABC_Error();

        if (btc) {
            mAmountToSendSatoshi = mCoreAPI.denominationToSatoshi(mBitcoinValueField.getText().toString());
            double value = mCoreAPI.SatoshiToCurrency(mAmountToSendSatoshi, mToWallet.getCurrencyNum());
            mDollarValueField.setText(String.valueOf(value));
       }
        else {
            currency = Double.valueOf(mDollarValueField.getText().toString());
            satoshi = mCoreAPI.CurrencyToSatoshi(currency, mToWallet.getCurrencyNum());
            mAmountToSendSatoshi = satoshi;
            int currencyDecimalPlaces = 2; //TODO where does this come from?
            mBitcoinValueField.setText(mCoreAPI.formatSatoshi(mAmountToSendSatoshi, false, currencyDecimalPlaces));
        }
        updateFeeFieldContents();
    }

    private void updateFeeFieldContents()
    {
//        long fees = 0;
//        tABC_Error error = new ;
//        NSString *dest = NULL;
//        if (self.bAddressIsWalletUUID) {
//            dest = self.destWallet.strUUID;
//        } else {
//            dest = self.sendToAddress;
//        }
//        if ([CoreBridge calcSendFees:self.wallet.strUUID
//        sendTo:dest
//        amountToSend:self.amountToSendSatoshi
//        storeResultsIn:&fees
//        walletTransfer:self.bAddressIsWalletUUID])
//        {
//            double currencyFees = 0.0;
//            self.conversionLabel.textColor = [UIColor whiteColor];
//            self.amountBTCTextField.textColor = [UIColor whiteColor];
//            self.amountUSDTextField.textColor = [UIColor whiteColor];
//
//            NSMutableString *coinFeeString = [[NSMutableString alloc] init];
//            NSMutableString *fiatFeeString = [[NSMutableString alloc] init];
//            [coinFeeString appendString:@"+ "];
//            [coinFeeString appendString:[CoreBridge formatSatoshi:fees withSymbol:false]];
//            [coinFeeString appendString:@" "];
//            [coinFeeString appendString:[User Singleton].denominationLabel];
//
//            if (ABC_SatoshiToCurrency([[User Singleton].name UTF8String], [[User Singleton].password UTF8String],
//            fees, &currencyFees, self.wallet.currencyNum, &error) == ABC_CC_Ok)
//            {
//                [fiatFeeString appendString:@"+ "];
//                [fiatFeeString appendString:[CoreBridge formatCurrency:currencyFees
//                withCurrencyNum:self.wallet.currencyNum
//                withSymbol:false]];
//                [fiatFeeString appendString:@" "];
//                [fiatFeeString appendString:self.wallet.currencyAbbrev];
//            }
//            self.amountBTCLabel.text = coinFeeString;
//            self.amountUSDLabel.text = fiatFeeString;
//            self.conversionLabel.text = [CoreBridge conversionString:self.wallet];
//        }
//        else
//        {
//            NSString *message = NSLocalizedString(@"Insufficient funds", nil);
//            self.conversionLabel.text = message;
//            self.conversionLabel.textColor = [UIColor redColor];
//            self.amountBTCTextField.textColor = [UIColor redColor];
//            self.amountUSDTextField.textColor = [UIColor redColor];
//        }
//        [self alineTextFields:self.amountBTCLabel alignWith:self.amountBTCTextField];
//        [self alineTextFields:self.amountUSDLabel alignWith:self.amountUSDTextField];
    }


    private void attemptInitiateSend() {
        //make sure PIN is good
        String enteredPIN = mPinEdittext.getText().toString();
        String userPIN = mCoreAPI.GetUserPIN();
        if (userPIN!=null && userPIN.equals(enteredPIN)) {
            mSendOrTransferTask = new SendOrTransferTask(mSourceWallet, mUUIDorURI, mAmountToSendSatoshi);
            mSendOrTransferTask.execute();
        } else {
            showIncorrectPINAlert();
        }
    }

    /**
     * Represents an asynchronous creation of the first wallet
     */
    private SendOrTransferTask mSendOrTransferTask;
    public class SendOrTransferTask extends AsyncTask<Void, Void, Boolean> {
        private Wallet mFromWallet;
        private final String mAddress;
        private final long mSatoshi;

        SendOrTransferTask(Wallet fromWallet, String address, long amount) {
            mFromWallet = fromWallet;
            mAddress = address;
            mSatoshi = amount;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mCoreAPI.InitiateTransferOrSend(mFromWallet, mAddress, mSatoshi);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSendOrTransferTask = null;
           if (!success) {
                Log.d("SendConfirmationFragment", "Send or Transfer failed");
            } else {
//                        [self showSendStatus]; //TODO
            }
        }

        @Override
        protected void onCancelled() {
            mSendOrTransferTask = null;
        }
    }



    private void showIncorrectPINAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getResources().getString(R.string.fragment_send_incorrect_pin_message))
                .setTitle(getResources().getString(R.string.fragment_send_incorrect_pin_title))
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
}
