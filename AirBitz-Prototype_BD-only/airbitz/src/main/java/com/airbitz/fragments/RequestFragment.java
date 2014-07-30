package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.HighlightOnPressButton;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.CalculatorBrain;
import com.airbitz.utils.Common;

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
    private HighlightOnPressButton mImportWalletButton;

    private View dummyFocus;

    private List<Wallet> mWallets;
    private List<String> mWalletNames;
    private Wallet mSelectedWallet;

    private HighlightOnPressSpinner pickWalletSpinner;

    private RelativeLayout mButtonGroup;
    private HighlightOnPressButton mExpandButton;

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

    private CoreAPI mCoreAPI;
    int mFromIndex =0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();

        mWallets = new ArrayList<Wallet>();
        List<Wallet> temp = mCoreAPI.getCoreWallets();
        for(Wallet wallet: temp){
            if(!wallet.isArchived()){
                mWallets.add(wallet);
            }
        }
        mWalletNames = new ArrayList<String>();
        String uuid = null;
        Bundle bundle = getArguments();
        if(bundle!=null && bundle.getString(FROM_UUID)!=null) {
            uuid = bundle.getString(FROM_UUID);
        }
        for(int i=0; i<mWallets.size(); i++) {
            if(!mWallets.get(i).isArchived()) {
                mWalletNames.add(mWallets.get(i).getName());
                if (mWallets.get(i).getUUID().equals(uuid)) {
                    mFromIndex = i;
                    mSelectedWallet = mWallets.get(i);
                }
            }
        }
    }

    private View mView;
//    @Override public void onDestroyView() {
//        super.onDestroyView();
//        ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
//        if( null != parentViewGroup ) {
//            parentViewGroup.removeView( mView );
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if(mView!=null)
//            return mView;
        mView = inflater.inflate(R.layout.fragment_request, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_parent);
        mNavigationLayout = (RelativeLayout) mView.findViewById(R.id.navigation_layout);
        mFocusDistractorLayout = (LinearLayout) mView.findViewById(R.id.layout_focus_distractor);
        mFocusDistractorLayout.requestFocus();

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_amount);

        mCalculatorBrain = new CalculatorBrain();
        mDF.setMinimumFractionDigits(0);
        mDF.setMaximumFractionDigits(6);
        mDF.setMinimumIntegerDigits(1);
        mDF.setMaximumIntegerDigits(8);

        dummyFocus = mView.findViewById(R.id.fragment_request_dummy_focus);

        mNavigationLayout = (RelativeLayout) mView.findViewById(R.id.navigation_layout);

        mBitcoinField = (EditText) mView.findViewById(R.id.edittext_btc);
        mFiatField = (EditText) mView.findViewById(R.id.edittext_dollar);

        mBackButton = (ImageButton) mView.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.button_help);
        mImportWalletButton = (HighlightOnPressButton) mView.findViewById(R.id.button_import_wallet);

        mButtonGroup = (RelativeLayout) mView.findViewById(R.id.button_group);

        mExpandButton = (HighlightOnPressButton) mView.findViewById(R.id.button_expand);

        pickWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.new_wallet_spinner);
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.Request);
        pickWalletSpinner.setAdapter(dataAdapter);
        pickWalletSpinner.post(new Runnable() {
            @Override
            public void run() {
                pickWalletSpinner.setSelection(mFromIndex);
            }
        });

        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);
        mWalletTextView = (TextView) mView.findViewById(R.id.textview_wallet);
        mConverterTextView = (TextView) mView.findViewById(R.id.textview_converter);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mWalletTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitcoinField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mFiatField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mConverterTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mBTCDenominationTextView = (TextView) mView.findViewById(R.id.request_btc_denomination);
        mFiatDenominationTextView = (TextView) mView.findViewById(R.id.request_fiat_denomination);

        mSelectedWallet = mWallets.get(pickWalletSpinner.getSelectedItemPosition());

        mExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mBitcoinField.getText().toString().isEmpty() && Float.valueOf(mBitcoinField.getText().toString())<0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom));
                    builder.setMessage("Invalid Amount")
                            .setCancelable(false)
                            .setNeutralButton(getResources().getString(R.string.string_ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }else{
                    Fragment frag = new RequestQRCodeFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Wallet.WALLET_NAME, ((Wallet)pickWalletSpinner.getSelectedItem()).getName());
                    bundle.putString(BITCOIN_VALUE, mBitcoinField.getText().toString());
                    bundle.putString(FIAT_VALUE, mFiatField.getText().toString());
                    frag.setArguments(bundle);
                    ((NavigationActivity) getActivity()).pushFragment(frag);
                }
            }
        });

        mImportWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ImportFragment();
                ((NavigationActivity) getActivity()).pushFragment(frag);
            }
        });

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

        return mView;
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
        String bitcoin = mBitcoinField.getText().toString();
        String fiat = mFiatField.getText().toString();
        if (btc && !bitcoin.isEmpty()) {
            if(!mCoreAPI.TooMuchBitcoin(bitcoin)) {
                satoshi = mCoreAPI.denominationToSatoshi(bitcoin);
                mFiatField.setText(mCoreAPI.FormatCurrency(satoshi, wallet.getCurrencyNum(), false, false));
            } else {
                //TODO ???
                Log.d("RequestFragment", "Too much bitcoin");
            }
        }else if(btc && bitcoin.isEmpty()){
            mFiatField.setText("0.00");
        }else if(!btc && fiat.isEmpty()){
            String s = mCoreAPI.getDefaultBTCDenomination();
            if(s.equals("mBTC")) {
                mBitcoinField.setText("0.00000");
            }else if(s.equals("μBTC")){
                mBitcoinField.setText("0.00");
            }else if(s.equals("BTC")){
                mBitcoinField.setText("0.00000000");
            }
        }else if (!btc && !fiat.isEmpty()) {
            try
            {
                if(!mCoreAPI.TooMuchFiat(fiat, wallet.getCurrencyNum())) {
                    currency = Double.parseDouble(fiat);
                    satoshi = mCoreAPI.CurrencyToSatoshi(currency, wallet.getCurrencyNum());
                    mBitcoinField.setText(mCoreAPI.FormatCurrency(satoshi, wallet.getCurrencyNum(), true, false));
                } else {
                    Log.d("RequestFragment", "Too much fiat");
                }
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
        mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
        mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronyms()[mCoreAPI.CurrencyIndex(mSelectedWallet.getCurrencyNum())]);
        setConversionText(mSelectedWallet.getCurrencyNum());
        setupCalculator(((NavigationActivity) getActivity()).getCalculatorView());
        mCoreAPI.addExchangeRateChangeListener(this);
    }

    @Override public void onPause() {
        super.onPause();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCoreAPI.removeExchangeRateChangeListener(this);
        removeCalculator(((NavigationActivity) getActivity()).getCalculatorView());
    }

    @Override
    public void OnExchangeRatesChange() {
        if(mSelectedWallet!=null) {
            setConversionText(mSelectedWallet.getCurrencyNum());
            updateTextFieldContents(true);
        }
    }
}
