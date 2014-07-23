package com.airbitz.fragments;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.utils.CalculatorBrain;
import com.airbitz.utils.Common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class RequestFragment extends Fragment implements View.OnClickListener, CoreAPI.OnExchangeRatesChange {

    public static final String BITCOIN_VALUE = "com.airbitz.request.bitcoin_value";
    public static final String FIAT_VALUE = "com.airbitz.request.fiat_value";
    public static final String FROM_UUID = "com.airbitz.request.from_uuid";

    private EditText mBitcoinField;
    private EditText mFiatField;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Button mImportWalletButton;
    private Button mEmailButton;
    private Button mSmsButton;
    private Button mQRCodeButton;
    private Button mBLEButton;

    private View dummyFocus;

    private List<Wallet> mWallets;
    private List<String> mWalletNames;
    private Wallet mSelectedWallet;

    private Spinner pickWalletSpinner;

    private RelativeLayout mButtonGroup;
    private Button mExpandButton;
    private Button mUnExpandButton;

    private TextView mTitleTextView;
    private TextView mWalletTextView;
    private TextView mConverterTextView;
    private TextView mBTCDenominationTextView;
    private TextView mFiatDenominationTextView;

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

    private CoreAPI mCoreAPI;
    int mFromIndex =0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();

        mWallets = mCoreAPI.getCoreWallets();
        mWalletNames = new ArrayList<String>();
        String uuid = null;
        Bundle bundle = getArguments();
        if(bundle!=null && bundle.getString(FROM_UUID)!=null) {
            uuid = bundle.getString(FROM_UUID);
        }
        for(int i=0; i<mWallets.size(); i++) {
            mWalletNames.add(mWallets.get(i).getName());
            if(mWallets.get(i).getUUID().equals(uuid)) {
                mFromIndex = i;
                mSelectedWallet = mWallets.get(i);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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

        dummyFocus = view.findViewById(R.id.fragment_request_dummy_focus);

        setupCalculator(((NavigationActivity) getActivity()).getCalculatorView());

        mNavigationLayout = (RelativeLayout) view.findViewById(R.id.navigation_layout);

        mBitcoinField = (EditText) view.findViewById(R.id.edittext_btc);
        mFiatField = (EditText) view.findViewById(R.id.edittext_dollar);

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
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.Request);
        pickWalletSpinner.setAdapter(dataAdapter);
        pickWalletSpinner.post(new Runnable() {
            @Override
            public void run() {
                pickWalletSpinner.setSelection(mFromIndex);
            }
        });

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
        mWalletTextView = (TextView) view.findViewById(R.id.textview_wallet);
        mConverterTextView = (TextView) view.findViewById(R.id.textview_converter);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mWalletTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitcoinField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mFiatField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mConverterTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mBTCDenominationTextView = (TextView) view.findViewById(R.id.request_btc_denomination);
        mFiatDenominationTextView = (TextView) view.findViewById(R.id.request_fiat_denomination);

        mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
        mSelectedWallet = mWallets.get(pickWalletSpinner.getSelectedItemPosition());
        mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronyms()[mCoreAPI.CurrencyIndex(mSelectedWallet.getCurrencyNum())]);

        mExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new WalletQRCodeFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Wallet.WALLET_NAME, ((Wallet)pickWalletSpinner.getSelectedItem()).getName());
                bundle.putString(BITCOIN_VALUE, mBitcoinField.getText().toString());
                bundle.putString(FIAT_VALUE, mFiatField.getText().toString());
                frag.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(frag);
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
                sendSMS();
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        mQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new WalletQRCodeFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Wallet.WALLET_NAME, ((Wallet)pickWalletSpinner.getSelectedItem()).getName());
                bundle.putString(BITCOIN_VALUE, mBitcoinField.getText().toString());
                bundle.putString(FIAT_VALUE, mFiatField.getText().toString());
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

        final TextWatcher mBTCTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                updateTextFieldContents(true);
                mBitcoinField.setSelection(mBitcoinField.getText().toString().length());
            }
        };

        final TextWatcher mDollarTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                updateTextFieldContents(false);
                mFiatField.setSelection(mFiatField.getText().toString().length());
            }
        };

        mBitcoinField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    EditText edittext = (EditText) view;
                    int inType = edittext.getInputType();
                    edittext.setInputType(InputType.TYPE_NULL);
                    edittext.setInputType(inType);
                    mFiatField.removeTextChangedListener(mDollarTextWatcher);
                    mBitcoinField.setText("");
                    mFiatField.setText("");
                    mBitcoinField.addTextChangedListener(mBTCTextWatcher);
                    showCustomKeyboard(view);
                } else {
                    hideCustomKeyboard();
                }
            }
        });

        mFiatField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText edittext = (EditText) view;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.setInputType(inType);
                if (hasFocus) {
                    mBitcoinField.removeTextChangedListener(mBTCTextWatcher);
                    mFiatField.setText("");
                    mBitcoinField.setText("");
                    mFiatField.addTextChangedListener(mDollarTextWatcher);
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

        pickWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedWallet = mWallets.get(i);
                mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronyms()[mCoreAPI.CurrencyIndex(mSelectedWallet.getCurrencyNum())]);
                setConversionText(mSelectedWallet.getCurrencyNum());
                updateTextFieldContents(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        setConversionText(mSelectedWallet.getCurrencyNum());

        return view;
    }

    private void sendSMS() {
        String address="";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain"); // if no qr code available

        String id = mCoreAPI.createReceiveRequestFor(mSelectedWallet, "", "", mBitcoinField.getText().toString());
        if(id!=null) {
            address = mCoreAPI.getRequestAddress(mSelectedWallet.getUUID(), id);
        }
        String strBody = "Bitcoin Request:\n" + address;
        intent.putExtra("sms_body", strBody);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bm = mCoreAPI.getQRCodeBitmap(mSelectedWallet.getUUID(), id);
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 0, bos);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, bos.toByteArray());
        } else {
            Log.d("RequestFragment", "Could not attach qr code to mms");
        }
        intent.setData(Uri.parse("sendto:")); // only sms or mms should handle this

        startActivity(intent);
    }

    private void sendEmail() {
        String address="";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this

        String id = mCoreAPI.createReceiveRequestFor(mSelectedWallet, "", "", mBitcoinField.getText().toString());
        if(id!=null) {
            address = mCoreAPI.getRequestAddress(mSelectedWallet.getUUID(), id);
        }
        String strBody = "Bitcoin Request:\n" + address;


        intent.putExtra(Intent.EXTRA_TEXT, strBody);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bm = mCoreAPI.getQRCodeBitmap(mSelectedWallet.getUUID(), id);
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 0, bos);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, bos.toByteArray());
        } else {
            Log.d("RequestFragment", "Could not attach qr code to email");
        }

        startActivity(Intent.createChooser(intent, "Email:"));
    }


    private void setConversionText(int currencyNum) {
        mConverterTextView.setText(mCoreAPI.BTCtoFiatConversion(currencyNum));
    }

    private void updateTextFieldContents(boolean btc)
    {
        double currency;
        long satoshi;

        int walletPosition = pickWalletSpinner.getSelectedItemPosition();
        Wallet wallet = mWallets.get(walletPosition);
        if (btc && !mBitcoinField.getText().toString().isEmpty()) {
            satoshi = mCoreAPI.denominationToSatoshi(mBitcoinField.getText().toString());
            mFiatField.setText(mCoreAPI.FormatCurrency(satoshi, wallet.getCurrencyNum(), false, false));
        }else if(btc && mBitcoinField.getText().toString().isEmpty()){
            mFiatField.setText("0.00");
        }else if(!btc && mFiatField.getText().toString().isEmpty()){
            String s = mCoreAPI.getDefaultBTCDenomination();
            if(s.equals("mBTC")) {
                mBitcoinField.setText("0.00000");
            }else if(s.equals("μBTC")){
                mBitcoinField.setText("0.00");
            }else if(s.equals("BTC")){
                mBitcoinField.setText("0.00000000");
            }
        }else if (!btc && !mFiatField.getText().toString().isEmpty()) {
            try
            {
                currency = Double.parseDouble(mFiatField.getText().toString());
                satoshi = mCoreAPI.CurrencyToSatoshi(currency, wallet.getCurrencyNum());
                mBitcoinField.setText(mCoreAPI.FormatCurrency(satoshi, wallet.getCurrencyNum(), true, false));
            }
            catch(NumberFormatException e)
            {
                //not a double, ignore
            }
        }
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
                display.setText("");
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
                    if(display.equals(mBitcoinField)) {
                        updateTextFieldContents(true);
                    } else {
                        updateTextFieldContents(false);
                    }
                }
        }

    }

    public void hideCustomKeyboard() {
        ((NavigationActivity) getActivity()).hideCalculator();
        dummyFocus.requestFocus();
    }

    public void showCustomKeyboard(View v) {
        ((NavigationActivity) getActivity()).showCalculator();
    }

    @Override public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCoreAPI.addExchangeRateChangeListener(this);
    }

    @Override public void onPause() {
        super.onPause();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCoreAPI.removeExchangeRateChangeListener(this);
    }

    @Override
    public void OnExchangeRatesChange() {
        if(mSelectedWallet!=null) {
            setConversionText(mSelectedWallet.getCurrencyNum());
            updateTextFieldContents(true);
        }
    }
}
