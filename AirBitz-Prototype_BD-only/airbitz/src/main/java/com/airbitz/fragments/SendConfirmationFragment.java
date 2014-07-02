package com.airbitz.fragments;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.airbitz.api.SWIGTYPE_p_int64_t;
import com.airbitz.api.SWIGTYPE_p_uint64_t;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_TxDetails;
import com.airbitz.api.tABC_WalletInfo;
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

    private String mQRResult;
    private CoreAPI mCoreAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        if (bundle == null) {
            System.out.println("Send confirmation bundle is null");
        } else {
            mQRResult = bundle.getString(SendFragment.QR_RESULT);
        }
        mCoreAPI = CoreAPI.getApi();
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

        mDollarValueField = (EditText) view.findViewById(R.id.button_bitcoin_balance);
        mBitcoinValueField = (EditText) view.findViewById(R.id.button_dollar_balance);

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

        mFromEdittext.setText(bundle.getString("wallet_name"));
        mToEdittext.setText(bundle.getString("to_name"));


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

            }
        });

        mBitcoinValueField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }

    public void touchEventsEnded() {
        int successThreshold = mLeftThreshold + (mSlideLayout.getWidth() / 4);
        if (mConfirmSwipeButton.getX() <= successThreshold) {
//            attemptInitiateSend();

            Fragment frag = new ReceivedSuccessFragment();
            frag.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag);
        } else {
            mConfirmSwipeButton.setX(mRightThreshold);
        }
    }

    private void attemptInitiateSend() {
        //make sure PIN is good
        String enteredPIN = "";
        if (!enteredPIN.isEmpty()) {
            //make sure the entered PIN matches the PIN stored in the Core
            tABC_Error error = new tABC_Error();
            String szPIN = null;

//            mCoreAPI.ABC_GetPIN(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
//                szPIN, error);

            if (szPIN!=null && szPIN.equals(enteredPIN)) {
                    InitiateSendRequest();
                    return;
            }
//            free(szPIN);
        }
        showIncorrectPINAlert();
//        [_confirmationSlider resetIn:1.0];
    }

    private void InitiateSendRequest() {
//            tABC_Error Error = new tABC_Error();
//            tABC_CC result;
//            int nCount;
//            double currency;
//
//        long satoshi = 1234; //TODO
//        int currencyNum = 840; //TODO
//            currency = mCoreAPI.SatoshiToCurrency(satoshi, currencyNum);
//            if (result == ABC_CC_Ok)
//            {
//                ABC_GetWallets([[User Singleton].name UTF8String], [[User Singleton].password UTF8String], &aWalletInfo, &nCount, &Error);
//
//                if (nCount)
//                {
//                    tABC_TxDetails Details;
//
//                    SWIGTYPE_p_int64_t pAmountSatoshi = core.longp_to
//                    Details.setAmountSatoshi(mBitcoinValueField.getText().toString());
//                    Details.amountCurrency = currency;
//                    // These will be calculated for us
//                    Details.amountFeesAirbitzSatoshi = 0;
//                    Details.amountFeesMinersSatoshi = 0;
//                    // If this is a transfer, populate the comments
//                    Details.szName = "Anonymous";
//                    Details.szCategory = "";
//                    Details.szNotes = "";
//                    Details.attributes = 0x2;
//
//                    tABC_WalletInfo *info = aWalletInfo[self.selectedWalletIndex];
//
//                    if (self.bAddressIsWalletUUID)
//                    {
//                        NSString *categoryText = NSLocalizedString(@"Transfer:Wallet:", nil);
//                        tABC_TransferDetails Transfer;
//                        Transfer.szSrcWalletUUID = strdup(info->szUUID);
//                        Transfer.szSrcName = strdup([self.destWallet.strName UTF8String]);
//                        Transfer.szSrcCategory = strdup([[NSString stringWithFormat:@"%@%@", categoryText, self.destWallet.strName] UTF8String]);
//
//                        Transfer.szDestWalletUUID = strdup([self.destWallet.strUUID UTF8String]);
//                        Transfer.szDestName = strdup([self.wallet.strName UTF8String]);
//                        Transfer.szDestCategory = strdup([[NSString stringWithFormat:@"%@%@", categoryText, self.wallet.strName] UTF8String]);
//
//                        result = ABC_InitiateTransfer([[User Singleton].name UTF8String],
//                        [[User Singleton].password UTF8String],
//                        &Transfer, &Details,
//                            ABC_SendConfirmation_Callback,
//                            (__bridge void *)self,
//                        &Error);
//
//                        free(Transfer.szSrcWalletUUID);
//                        free(Transfer.szSrcName);
//                        free(Transfer.szSrcCategory);
//                        free(Transfer.szDestWalletUUID);
//                        free(Transfer.szDestName);
//                        free(Transfer.szDestCategory);
//                    } else {
//                        result = ABC_InitiateSendRequest([[User Singleton].name UTF8String],
//                        [[User Singleton].password UTF8String],
//                        info->szUUID,
//                        [self.sendToAddress UTF8String],
//                        &Details,
//                                ABC_SendConfirmation_Callback,
//                                (__bridge void *)self,
//                        &Error);
//                    }
//                    if (result == ABC_CC_Ok)
//                    {
//                        [self showSendStatus];
//                    }
//                    else
//                    {
//                        [Util printABC_Error:&Error];
//                    }
//
////                    ABC_FreeWalletInfoArray(aWalletInfo, nCount);
//                }
//            }
        }

    private void showIncorrectPINAlert() {
//                    UIAlertView * alert =[[UIAlertView alloc]
//                    initWithTitle:
//                    NSLocalizedString( @ "Incorrect PIN", nil)
//                    message:
//                    NSLocalizedString( @
//                    "You must enter the correct withdrawl PIN in order to proceed", nil)
//                    delegate:
//                    self
//                    cancelButtonTitle:
//                    @ "OK"
//                    otherButtonTitles:
//                    nil];
//                    [alert show];
    }
}
