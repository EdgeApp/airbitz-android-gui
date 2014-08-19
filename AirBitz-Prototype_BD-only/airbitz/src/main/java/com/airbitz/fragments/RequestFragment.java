package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.objects.Calculator;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class RequestFragment extends Fragment implements CoreAPI.OnExchangeRatesChange {
    private final String TAG = getClass().getSimpleName();
    public static final String BITCOIN_VALUE = "com.airbitz.request.bitcoin_value";
    public static final String FIAT_VALUE = "com.airbitz.request.fiat_value";
    public static final String FROM_UUID = "com.airbitz.request.from_uuid";

    private EditText mBitcoinField;
    private EditText mFiatField;
    private boolean mAutoUpdatingTextFields = false;


    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressButton mImportWalletButton;


    private List<Wallet> mWallets;
    private List<String> mWalletNames;
    private Wallet mSelectedWallet;

    private HighlightOnPressSpinner pickWalletSpinner;

    private HighlightOnPressButton mExpandButton;

    private TextView mTitleTextView;
    private TextView mWalletTextView;
    private TextView mConverterTextView;
    private TextView mBTCDenominationTextView;
    private TextView mFiatDenominationTextView;

    private ScrollView mScrollView;

    private Calculator mCalculator;

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
        if(uuid!=null && mWallets!=null) {
            for(int i=0; i<mWallets.size(); i++) {
                if(!mWallets.get(i).isArchived()) {
                    mWalletNames.add(mWallets.get(i).getName());
                    if (mWallets.get(i).getUUID().equals(uuid)) {
                        mFromIndex = i;
                        mSelectedWallet = mWallets.get(i);
                    }
                }
            }
        } else {
            mFromIndex=0;
            mSelectedWallet = mWallets.get(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_request, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_amount);

        mCalculator = ((NavigationActivity) getActivity()).getCalculatorView();

        mBitcoinField = (EditText) mView.findViewById(R.id.edittext_btc);
        mFiatField = (EditText) mView.findViewById(R.id.edittext_dollar);

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_request_help_button);
        mImportWalletButton = (HighlightOnPressButton) mView.findViewById(R.id.button_import_wallet);

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

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
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
                if (!mBitcoinField.getText().toString().isEmpty() && Float.valueOf(mBitcoinField.getText().toString()) < 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                    builder.setMessage("Invalid Amount")
                            .setCancelable(false)
                            .setNeutralButton(getResources().getString(R.string.string_ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }
                            );
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Fragment frag = new RequestQRCodeFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Wallet.WALLET_NAME, ((Wallet) pickWalletSpinner.getSelectedItem()).getName());
                    bundle.putString(BITCOIN_VALUE, mBitcoinField.getText().toString());
                    bundle.putString(FIAT_VALUE, mFiatField.getText().toString());
                    frag.setArguments(bundle);
                    ((NavigationActivity) getActivity()).pushFragment(frag, NavigationActivity.Tabs.REQUEST.ordinal());
                }
            }
        });

        mImportWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ImportFragment();
                ((NavigationActivity) getActivity()).pushFragment(frag, NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        final TextWatcher mBTCTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mAutoUpdatingTextFields) {
                    updateTextFieldContents(true);
                    mBitcoinField.setSelection(mBitcoinField.getText().toString().length());
                }
            }
        };

        final TextWatcher mFiatTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!mAutoUpdatingTextFields) {
                    updateTextFieldContents(false);
                    mFiatField.setSelection(mFiatField.getText().toString().length());
                }
            }
        };

        mBitcoinField.addTextChangedListener(mBTCTextWatcher);
        mFiatField.addTextChangedListener(mFiatTextWatcher);

        mBitcoinField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    EditText edittext = (EditText) view;
                    int inType = edittext.getInputType();
                    edittext.setInputType(InputType.TYPE_NULL);
                    edittext.setInputType(inType);
                    mAutoUpdatingTextFields = true;
                    mFiatField.setText("");
                    mBitcoinField.addTextChangedListener(mBTCTextWatcher);
                    mCalculator.setEditText(mBitcoinField);
                    mBitcoinField.setText("");
                    mAutoUpdatingTextFields = false;
                    ((NavigationActivity) getActivity()).showCalculator();
                } else {
                    ((NavigationActivity) getActivity()).hideCalculator();
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
                    mAutoUpdatingTextFields = true;
                    mFiatField.setText("");
                    mBitcoinField.setText("");
                    mFiatField.addTextChangedListener(mFiatTextWatcher);
                    mCalculator.setEditText(mFiatField);
                    mAutoUpdatingTextFields = false;
                    ((NavigationActivity) getActivity()).showCalculator();
                } else {
                    ((NavigationActivity) getActivity()).hideCalculator();
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

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).pushFragment(new HelpDialog(HelpDialog.REQUEST), NavigationActivity.Tabs.REQUEST.ordinal());
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
        mConverterTextView.setText(mCoreAPI.BTCtoFiatConversion(currencyNum, true));
    }

    private void updateTextFieldContents(boolean btc)
    {
        double currency;
        long satoshi;

        mAutoUpdatingTextFields = true;
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
                Common.LogD(TAG, "Too much bitcoin");
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
                    Common.LogD(TAG, "Too much fiat");
                }
            }
            catch(NumberFormatException e)
            {
                //not a double, ignore
            }
        }
        mAutoUpdatingTextFields = false;
    }

    @Override public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
        mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronyms()[mCoreAPI.CurrencyIndex(mSelectedWallet.getCurrencyNum())]);
        setConversionText(mSelectedWallet.getCurrencyNum());
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
