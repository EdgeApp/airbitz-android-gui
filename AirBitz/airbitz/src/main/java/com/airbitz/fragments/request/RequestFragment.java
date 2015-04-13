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

package com.airbitz.fragments.request;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class RequestFragment extends BaseFragment implements
        CoreAPI.OnExchangeRatesChange,
        CoreAPI.OnWalletLoaded {
    public static final String BITCOIN_VALUE = "com.airbitz.request.bitcoin_value";
    public static final String SATOSHI_VALUE = "com.airbitz.request.satoshi_value";
    public static final String FIAT_VALUE = "com.airbitz.request.fiat_value";
    public static final String BITCOIN_ID = "com.airbitz.request.bitcoinid";
    public static final String BITCOIN_ADDRESS = "com.airbitz.request.bitcoinaddress";
    public static final String FROM_UUID = "com.airbitz.request.from_uuid";
    public static final String MERCHANT_MODE = "com.airbitz.request.merchant_mode";
    private final String TAG = getClass().getSimpleName();
    int mFromIndex = 0;
    private String mUUID = null;
    private EditText mBitcoinField;
    private EditText mFiatField;
    private boolean mAutoUpdatingTextFields = false;
    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressButton mImportWalletButton;
    private List<Wallet> mWallets;
    private Wallet mSelectedWallet;
    private HighlightOnPressSpinner pickWalletSpinner;
    private HighlightOnPressButton mNextButton;
    private TextView mTitleTextView;
    private TextView mWalletTextView;
    private TextView mConverterTextView;
    private TextView mBTCDenominationTextView;
    private TextView mFiatDenominationTextView;
    private Calculator mCalculator;
    private CoreAPI mCoreAPI;
    private View mView;

    private Long mSavedSatoshi;
    private int mSavedIndex;
    private boolean mBtc = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_request, container, false);

        mCalculator = ((NavigationActivity) getActivity()).getCalculatorView();

        mBitcoinField = (EditText) mView.findViewById(R.id.edittext_btc);
        mFiatField = (EditText) mView.findViewById(R.id.edittext_dollar);

        if (((NavigationActivity) getActivity()).isLargeDpi()) {
            if (!mBtc) {
                focus(mFiatField);
            }
            mCalculator.hideDoneButton();
        }

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.request_title);

        mImportWalletButton = (HighlightOnPressButton) mView.findViewById(R.id.button_import_wallet);

        mNextButton = (HighlightOnPressButton) mView.findViewById(R.id.button_expand);

        pickWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.new_wallet_spinner);
        mWalletTextView = (TextView) mView.findViewById(R.id.textview_wallet);
        mConverterTextView = (TextView) mView.findViewById(R.id.textview_converter);

        mWalletTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitcoinField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mFiatField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mConverterTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mBTCDenominationTextView = (TextView) mView.findViewById(R.id.request_btc_denomination);
        mFiatDenominationTextView = (TextView) mView.findViewById(R.id.request_fiat_denomination);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
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
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mAutoUpdatingTextFields) {
                    updateTextFieldContents();
                    mBitcoinField.setSelection(mBitcoinField.getText().toString().length());
                }
            }
        };

        final TextWatcher mFiatTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mAutoUpdatingTextFields) {
                    updateTextFieldContents();
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
                    mBtc = true;

                    mFiatField.setText("");
                    mBitcoinField.setText("");
                    mAutoUpdatingTextFields = true;
                    mBitcoinField.addTextChangedListener(mBTCTextWatcher);
                    mCalculator.setEditText(mBitcoinField);
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
                mBtc = false;

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

        TextView.OnEditorActionListener calcDoneListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if(((NavigationActivity) getActivity()).isLargeDpi()) {
                        goNext();
                    }
                    return true;
                }
                else {
                    return false;
                }
            }
        };

        mBitcoinField.setOnEditorActionListener(calcDoneListener);
        mFiatField.setOnEditorActionListener(calcDoneListener);

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

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.REQUEST), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        pickWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedWallet = mWallets.get(i);
                mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronyms()[mCoreAPI.CurrencyIndex(mSelectedWallet.getCurrencyNum())]);
                setConversionText(mSelectedWallet.getCurrencyNum());
                updateTextFieldContents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return mView;
    }

    private void goNext() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        float f = 0f;
        try {
            f = format.parse(mBitcoinField.getText().toString()).floatValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!mBitcoinField.getText().toString().isEmpty() && f < 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
            builder.setMessage(getString(R.string.request_invalid_amount))
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
            mSavedSatoshi = mCoreAPI.denominationToSatoshi(mBitcoinField.getText().toString());
            mSavedIndex = pickWalletSpinner.getSelectedItemPosition();

            Wallet wallet = (Wallet) pickWalletSpinner.getSelectedItem();
            String id = mCoreAPI.createReceiveRequestFor(wallet, "", "", mSavedSatoshi);
            String requestAddress = mCoreAPI.getRequestAddress(wallet.getUUID(), id);

            Fragment frag = new RequestQRCodeFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Wallet.WALLET_UUID, wallet.getUUID());
            bundle.putString(BITCOIN_VALUE, mBitcoinField.getText().toString());
            bundle.putLong(SATOSHI_VALUE, mSavedSatoshi);
            bundle.putString(FIAT_VALUE, mFiatField.getText().toString());
            bundle.putString(BITCOIN_ID, id);
            bundle.putString(BITCOIN_ADDRESS, requestAddress);
            frag.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag, NavigationActivity.Tabs.REQUEST.ordinal());
        }
    }


    private void setConversionText(int currencyNum) {
        mConverterTextView.setText(mCoreAPI.BTCtoFiatConversion(currencyNum));
    }

    private void updateTextFieldContents() {
        updateTextFieldContents(mBtc);
    }

    private void updateTextFieldContents(boolean btc) {
        double currency;
        long satoshi;

        int walletPosition = pickWalletSpinner.getSelectedItemPosition();
        if(walletPosition < 0) {
            return;
        }
        mAutoUpdatingTextFields = true;
        Wallet wallet = mWallets.get(walletPosition);
        String bitcoin = mBitcoinField.getText().toString();
        String fiat = mFiatField.getText().toString();
        if (btc) {
            if (!mCoreAPI.TooMuchBitcoin(bitcoin)) {
                satoshi = mCoreAPI.denominationToSatoshi(bitcoin);
                mFiatField.setText(mCoreAPI.FormatCurrency(satoshi, wallet.getCurrencyNum(), false, false));
            } else {
                Log.d(TAG, "Too much bitcoin");
            }
        } else {
            try {
                if (!mCoreAPI.TooMuchFiat(fiat, wallet.getCurrencyNum())) {
                    currency = Double.parseDouble(fiat);
                    satoshi = mCoreAPI.CurrencyToSatoshi(currency, wallet.getCurrencyNum());
                    mBitcoinField.setText(mCoreAPI.formatSatoshi(satoshi, false));
                } else {
                    Log.d(TAG, "Too much fiat");
                }
            } catch (NumberFormatException e) {
                //not a double, ignore
            }
        }
        mAutoUpdatingTextFields = false;
    }

    private void focus(EditText view) {
        view.requestFocus();
        mCalculator.setEditText(view);
        mBtc = mBitcoinField == view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If large display, keep the calculator open
        ((NavigationActivity) getActivity()).lockCalculator();

        mCoreAPI.setOnWalletLoadedListener(this);


    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCoreAPI.removeExchangeRateChangeListener(this);

        // If calculator is locked open, unlock it
        ((NavigationActivity) getActivity()).unlockCalculator();

        mCoreAPI.setOnWalletLoadedListener(null);
    }

    @Override
    public void OnExchangeRatesChange() {
        if (mSelectedWallet != null) {
            setConversionText(mSelectedWallet.getCurrencyNum());
            updateTextFieldContents();
        }
    }

    @Override
    public void onWalletsLoaded() {
        mWallets = mCoreAPI.getCoreActiveWallets();

        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.Request);
        pickWalletSpinner.setAdapter(dataAdapter);
        if (!mWallets.isEmpty()) {
            mSelectedWallet = mWallets.get(pickWalletSpinner.getSelectedItemPosition());

            Bundle bundle = getArguments();
            if (mUUID == null && bundle != null && bundle.getString(FROM_UUID) != null) {
                mUUID = bundle.getString(FROM_UUID);
                mSelectedWallet = mCoreAPI.getWalletFromUUID(mUUID);
                for (int i = 0; i < mWallets.size(); i++) {
                    if (mSelectedWallet.getUUID().equals(mWallets.get(i).getUUID())) {
                        pickWalletSpinner.setSelection(i);
                        mFromIndex = i;
                    }
                }
            } else if (bundle != null && bundle.getString(MERCHANT_MODE) != null) {
                focus(mFiatField);
            } else {
                mFromIndex = 0;
                if (mWallets != null && !mWallets.isEmpty())
                    mSelectedWallet = mWallets.get(mFromIndex);
            }

            if(mSelectedWallet==null) {
                mSelectedWallet = mWallets.get(0);
            }
        }

        mBTCDenominationTextView.setText(mCoreAPI.getDefaultBTCDenomination());
        mFiatDenominationTextView.setText(mCoreAPI.getCurrencyAcronyms()[mCoreAPI.CurrencyIndex(mSelectedWallet.getCurrencyNum())]);
        setConversionText(mSelectedWallet.getCurrencyNum());
        mCoreAPI.addExchangeRateChangeListener(this);

        mAutoUpdatingTextFields = true;
        if (mSavedSatoshi != null) {
            mFromIndex = mSavedIndex;
            if(mFromIndex < mWallets.size()) {
                mBitcoinField.setText(mCoreAPI.formatSatoshi(mSavedSatoshi, false));
                mSelectedWallet = mWallets.get(mFromIndex);
                mFiatField.setText(mCoreAPI.FormatCurrency(mSavedSatoshi, mSelectedWallet.getCurrencyNum(), false, false));
                pickWalletSpinner.setSelection(mFromIndex);
                mSavedSatoshi = null;
            }
        } else {
            mFiatField.setText("");
            mBitcoinField.setText("");
            pickWalletSpinner.setSelection(mFromIndex);
        }
        mAutoUpdatingTextFields = false;
    }
}
