package com.airbitz.fragments;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.RequestDropDownAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.utils.CalculatorBrain;
import com.airbitz.utils.Common;
import com.google.zxing.qrcode.encoder.QRCode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class RequestFragment extends Fragment implements View.OnClickListener {

    private View mView;
    private EditText mBitcoinField;
    private EditText mDollarField;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Button mImportWalletButton;
    private Button mEmailButton;
    private Button mSmsButton;
    private Button mQRCodeButton;
    private Button mBLEButton;

    private ListView mDropDownListView;
    private RequestDropDownAdapter mDropDownAdapter;
    private List<String> mWalletList;

    private Spinner pickWalletSpinner;

    private RelativeLayout mButtonGroup;
    private Button mExpandButton;
    private Button mUnExpandButton;

    private TextView mTitleTextView;
    private TextView mWalletTextView;
    private TextView mConverterTextView;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;
    private LinearLayout mFocusDistractorLayout;

    private ScrollView mScrollView;

    private Keyboard mKeyboard;
    private KeyboardView mKeyboardView;

    private ClipboardManager clipboard;

    private Boolean userIsInTheMiddleOfTypingANumber = false;
    private CalculatorBrain mCalculatorBrain;
    private static final String DIGITS = "0123456789.";

    DecimalFormat mDF = new DecimalFormat("@###########");
    public final static int CodePrev = 55000;
    public final static int CodeAllLeft = 55001;
    public final static int CodeLeft = 55002;
    public final static int CodeRight = 55003;
    public final static int CodeAllRight = 55004;
    public final static int CodeNext = 55005;
    public final static int CodeClear = 55006;
    public final static int CodeConst = 55009;
    public final static int CodeLog = 55010;
    public final static int CodeConv = 55011; // Conversions like round or degrees
    public final static int CodeTrig = 55012;

    private float mBTCtoUSDConversion = 600.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        mView = view;

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mWalletList = new ArrayList<String>();
        addWalletNamesToList();

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);
        mNavigationLayout = (RelativeLayout) view.findViewById(R.id.navigation_layout);
        mFocusDistractorLayout = (LinearLayout) view.findViewById(R.id.layout_focus_distractor);
        mFocusDistractorLayout.requestFocus();

        mScrollView = (ScrollView) view.findViewById(R.id.layout_amount);

        mCalculatorBrain = new CalculatorBrain();
        mDF.setMinimumFractionDigits(0);
        mDF.setMaximumFractionDigits(6);
        mDF.setMinimumIntegerDigits(1);
        mDF.setMaximumIntegerDigits(8);

//        mKeyboardView = ((NavigationActivity) getActivity()).getCalculatorView(); //(KeyboardView) view.findViewById(R.id.layout_calculator);
////        mKeyboardView.setEnabled(true);
//        mKeyboard = new Keyboard(this.getActivity(), R.xml.layout_keyboard);
//        mKeyboardView.setKeyboard(mKeyboard);
//        mKeyboardView.setPreviewEnabled(false);
//        mKeyboardView.setOnKeyboardActionListener(RequestFragment.this);
//        mKeyboardView.setBackgroundResource(R.drawable.bg_calc);

        setupCalculator(((NavigationActivity) getActivity()).getCalculatorView());

        mNavigationLayout = (RelativeLayout) view.findViewById(R.id.navigation_layout);

        mBitcoinField = (EditText) view.findViewById(R.id.edittext_btc);
        mDollarField = (EditText) view.findViewById(R.id.edittext_dollar);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mImportWalletButton = (Button) view.findViewById(R.id.button_import_wallet);
        mEmailButton = (Button) view.findViewById(R.id.button_email);
        mSmsButton = (Button) view.findViewById(R.id.button_sms);
        mQRCodeButton = (Button) view.findViewById(R.id.button_qr_code);
        mBLEButton = (Button) view.findViewById(R.id.button_ble);

        mButtonGroup = (RelativeLayout) view.findViewById(R.id.button_group);

        mExpandButton = (Button) view.findViewById(R.id.button_expand);
        mUnExpandButton = (Button) view.findViewById(R.id.button_unexpand);

        pickWalletSpinner = (Spinner) view.findViewById(R.id.new_wallet_spinner);
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mWalletList);
        pickWalletSpinner.setAdapter(dataAdapter);


        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
        mWalletTextView = (TextView) view.findViewById(R.id.textview_wallet);
        mConverterTextView = (TextView) view.findViewById(R.id.textview_converter);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mWalletTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitcoinField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mDollarField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mConverterTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonGroup.setVisibility(View.VISIBLE);
            }
        });

        mUnExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonGroup.setVisibility(View.GONE);
            }
        });

        mImportWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ImportFragment();
                ((NavigationActivity) getActivity()).pushFragment(frag);
            }
        });

        mSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("sms:"));
                startActivity(smsIntent);
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setType("text/html");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Content description");
                startActivity(Intent.createChooser(emailIntent, "Email:"));
            }
        });

        mQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new WalletQRCodeFragment();
                Bundle bundle = new Bundle();
                bundle.putString("wallet_name",(String)pickWalletSpinner.getSelectedItem());
                bundle.putString("bitcoin_value",mBitcoinField.getText().toString());
                bundle.putString("fiat_value",mDollarField.getText().toString());
                frag.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(frag);
            }
        });


        /*mQRCodeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("private key", "SdfnsdjsdnfdsnofmsdfwemfyweorwekrewojfewmfoewmfwdpsajdfewormewjwpqodenwnfiwefjweofjewofnewnfoeiwjfewnfoiewfnewiofnewofewinfewpfSdfnsdjsdnfdsnofmsdfwemfyweorwekrewojfewmfoewmfwdpsajdfewormewjwpqodenwnfiwefjweofjewofnewnfoeiwjfewnfoiewfnewiofnewofewinfewpf");
                clipboard.setPrimaryClip(clip);

                return true;
            }
        });*/

        mBitcoinField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showCustomKeyboard(view);
                } else {
                    hideCustomKeyboard();
                }
            }
        });


        mDollarField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showCustomKeyboard(view);
                } else {
                    hideCustomKeyboard();
                }
            }
        });

        mBitcoinField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomKeyboard(view);
            }
        });


        mDollarField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomKeyboard(view);
            }
        });

        mBitcoinField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true;
            }
        });


        mDollarField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true;
            }
        });


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        return view;
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        int importWidth = mImportWalletButton.getWidth();
//        int emailWidth = mEmailButton.getWidth();
//        int smsWidth = mSmsButton.getWidth();
//        int qrCodeWidth = mQRCodeButton.getWidth();
//
//        mImportWalletButton.getLayoutParams().height = importWidth;
//        mEmailButton.getLayoutParams().height = emailWidth;
//        mSmsButton.getLayoutParams().height = smsWidth;
//        mQRCodeButton.getLayoutParams().height = qrCodeWidth;
//
//    }

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

                mCalculatorBrain.setOperand(Double.parseDouble(display.getText().toString()));
                userIsInTheMiddleOfTypingANumber = false;
            }

            mCalculatorBrain.performOperation(buttonTag);
            display.setText(mDF.format(mCalculatorBrain.getResult()));
                if(buttonTag.equals("=")) {
                    if(display.equals(mBitcoinField)) {
                        mDollarField.setText(mDF.format(getUSDfromBTC(mCalculatorBrain.getResult())));
                    } else {
                        mBitcoinField.setText(mDF.format(getBTCfromUSD(mCalculatorBrain.getResult())));
                    }
                }
        }

    }

    public void showQRCodePopUpDialog() {
        /*final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_qrcode);
        ImageView imageviewQRCode = (ImageView) dialog.findViewById(R.id.imageview_qrcode);
        imageviewQRCode.setImageResource(R.drawable.img_qr_code);
        Button okButton = (Button) dialog.findViewById(R.id.button_ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();*/

    }

    public void addWalletNamesToList(){
        CoreAPI api = CoreAPI.getApi();
        List<Wallet> tempWallets = api.getWallets();
        for(Wallet wallet: tempWallets){
            if(wallet.getName()!="xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL" && wallet.getName()!="SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd")
            mWalletList.add(wallet.getName());
        }
    }

//    @Override public void onBackPressed() {
//        if( isCustomKeyboardVisible() )
//            hideCustomKeyboard();
//        else
//            this.finish();
//    }

    private double getUSDfromBTC(double value) {
        return value*mBTCtoUSDConversion;
    }

    private double getBTCfromUSD(double value) {
        return value/mBTCtoUSDConversion;
    }

    public void hideCustomKeyboard() {
        ((NavigationActivity) getActivity()).hideCalculator();
    }

    public void showCustomKeyboard(View v) {
        ((NavigationActivity) getActivity()).showCalculator();
    }

    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

//
//    public void onKey(int primaryCode, int[] keyCodes) {
//
//        View focusCurrent = getActivity().getWindow().getCurrentFocus();
//        if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;
//        EditText display = (EditText) focusCurrent;
//        Editable editable = display.getText();
//        int start = display.getSelectionStart();
//        // delete the selection, if chars are selected:
//        int end = display.getSelectionEnd();
//        if (end > start) {
//            editable.delete(start, end);
//        }
//
//        // Apply the key to the edittext
//        if (primaryCode == Keyboard.KEYCODE_CANCEL) {
//            hideCustomKeyboard();
//        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
//            if (editable != null && start > 0) editable.delete(start - 1, start);
//        } else if (primaryCode == CodeClear) {
//            if (editable != null) editable.clear();
//        } else if (primaryCode == CodeLeft) {
//            if (start > 0) display.setSelection(start - 1);
//        } else if (primaryCode == CodeRight) {
//            if (start < display.length()) display.setSelection(start + 1);
//        } else if (primaryCode == CodeAllLeft) {
//            display.setSelection(0);
//        } else if (primaryCode == CodeAllRight) {
//            display.setSelection(display.length());
//        } else { // insert character
//            String s = Character.toString((char) primaryCode);
//
//            if (DIGITS.contains(s)) { // digit was pressed
//                if (userIsInTheMiddleOfTypingANumber) {
//                    if (s.equals(".") && display.getText().toString().contains(".")) {
//                        // ERROR PREVENTION Eliminate entering multiple decimals
//                    } else {
////                        editable.insert(start, s);
//                        display.append(s);
//                    }
//                } else {
//                    if (s.equals(".")) {
//                        // ERROR PREVENTION
//                        // This will avoid error if only the decimal is hit before an operator, by placing a leading zero
//                        // before the decimal
//                        display.setText(0 + s);
//                    } else {
//                        display.setText(s);
//                    }
//                    userIsInTheMiddleOfTypingANumber = true;
//                }
//            } else { // operation was pressed
//                if (userIsInTheMiddleOfTypingANumber) {
//                    mCalculatorBrain.setOperand(Double.parseDouble(display.getText().toString()));
//                    userIsInTheMiddleOfTypingANumber = false;
//                }
//                mCalculatorBrain.performOperation(s);
//                display.setText(mDF.format(mCalculatorBrain.getResult()));
//                if(s.equals("=")) {
//                    if(display.equals(mBitcoinField)) {
//                        mDollarField.setText(mDF.format(getUSDfromBTC(mCalculatorBrain.getResult())));
//                    } else {
//                        mBitcoinField.setText(mDF.format(getBTCfromUSD(mCalculatorBrain.getResult())));
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onText(CharSequence charSequence) {
//    }
//
//    @Override
//    public void swipeLeft() {
//    }
//
//    @Override
//    public void swipeRight() {
//    }
//
//    @Override
//    public void swipeDown() {
//    }
//
//    @Override
//    public void swipeUp() {
//    }
//
//    @Override
//    public void onPress(int arg0) {
//    }
//
//    @Override
//    public void onRelease(int primaryCode) {
//    }
}
