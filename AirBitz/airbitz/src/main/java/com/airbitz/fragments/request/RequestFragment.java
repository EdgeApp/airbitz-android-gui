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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.settings.SettingFragment;
import com.airbitz.models.Contact;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.BleUtil;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Created on 2/13/14.
 */
public class RequestFragment extends BaseFragment implements
        CoreAPI.OnExchangeRatesChange,
        CoreAPI.OnWalletLoaded,
        ContactPickerFragment.ContactSelection,
        NfcAdapter.CreateNdefMessageCallback,
        Calculator.OnCalculatorKey
{
    public static final String BITCOIN_VALUE = "com.airbitz.request.bitcoin_value";
    public static final String SATOSHI_VALUE = "com.airbitz.request.satoshi_value";
    public static final String FIAT_VALUE = "com.airbitz.request.fiat_value";
    public static final String BITCOIN_ID = "com.airbitz.request.bitcoinid";
    public static final String BITCOIN_ADDRESS = "com.airbitz.request.bitcoinaddress";
    public static final String FROM_UUID = "com.airbitz.request.from_uuid";
    public static final String MERCHANT_MODE = "com.airbitz.request.merchant_mode";

    private final String FIRST_USAGE_COUNT = "com.airbitz.fragments.requestqr.firstusagecount";
    private final int READVERTISE_REPEAT_PERIOD = 1000 * 60 * 2;
    public static final int PARTIAL_PAYMENT_TIMEOUT = 10000;

    private final String TAG = getClass().getSimpleName();
    int mFromIndex = 0;
    private String mUUID = null;
    private EditText mAmountField;
    private boolean mAutoUpdatingTextFields = false;
    private HighlightOnPressButton mHelpButton;
//    private HighlightOnPressButton mImportWalletButton;
    private List<Wallet> mWallets;
    private Wallet mSelectedWallet;
    private HighlightOnPressSpinner pickWalletSpinner;
    private TextView mConverterTextView;
    private TextView mDenominationTextView;
    private Calculator mCalculator;
    private CoreAPI mCoreAPI;
    private View mView;

    private Long mSavedSatoshi;
    private int mSavedIndex;
    private boolean mAmountIsBitcoin = false;

    private NavigationActivity mActivity;
    private Handler mHandler;
    private TextView mBitcoinAddress;
    private long mAmountSatoshi;
    private String mID;
    private String mAddress;
    private String mContentURL;
    private String mRequestURI;
    private NfcAdapter mNfcAdapter;
    private ImageView mNFCImageView;
    private ImageView mBLEImageView;
    private Button mSMSButton;
    private Button mEmailButton;
    private Button mCopyButton;

    private Button mFiatSelect;
    private Button mBitcoinSelect;

    private boolean emailType = false;
    private ImageView mQRView;
    private Bitmap mQRBitmap;
    private CreateBitmapTask mCreateBitmapTask;
    private AlertDialog mPartialDialog;
    final Runnable dialogKiller = new Runnable() {
        @Override
        public void run() {
            if (mPartialDialog != null) {
                mPartialDialog.dismiss();
                mPartialDialog = null;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_request, container, false);

        mAmountField = (EditText) mView.findViewById(R.id.request_amount);
        mAmountField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        final TextWatcher mAmountChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                amountChanged();
            }
        };

        mAmountField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCalculator.getVisibility() != View.VISIBLE) {
                    showCalculator(true);
                }
            }
        });
        mAmountField.addTextChangedListener(mAmountChangedListener);
        mAmountField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mAmountField.setText("");
                    showCalculator(true);
                    mCalculator.setEditText(mAmountField);
                } else {
                    showCalculator(false);
                }
            }
        });

        TextView.OnEditorActionListener amountEditorListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    amountChanged();
                    return true;
                }
                else {
                    return false;
                }
            }
        };

        mAmountField.setOnEditorActionListener(amountEditorListener);

        mCalculator = (Calculator) mView.findViewById(R.id.fragment_request_calculator_layout);
        mCalculator.setCalculatorKeyListener(this);
        mCalculator.setEditText(mAmountField);

//        mNFCImageView = (ImageView) mView.findViewById(R.id.fragment_request_qrcode_nfc_image);
//        mBLEImageView = (ImageView) mView.findViewById(R.id.fragment_request_qrcode_ble_image);

        mQRView = (ImageView) mView.findViewById(R.id.qr_code_view);

        RelativeLayout header = (RelativeLayout) mView.findViewById(R.id.fragment_request_header);
        mHelpButton = (HighlightOnPressButton) header.findViewById(R.id.layout_wallet_select_header_right);
        mHelpButton.setVisibility(View.VISIBLE);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.REQUEST), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        pickWalletSpinner = (HighlightOnPressSpinner) header.findViewById(R.id.layout_wallet_select_header_spinner);
        pickWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedWallet = mWallets.get(i);
                AirbitzApplication.setCurrentWallet(mSelectedWallet.getUUID());
                setConversionText(mSelectedWallet.getCurrencyNum());
                mFiatSelect.setText(mCoreAPI.getCurrencyAcronym(mSelectedWallet.getCurrencyNum()));
                updateAmount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

//        mImportWalletButton = (HighlightOnPressButton) mView.findViewById(R.id.button_import_wallet);
//        mImportWalletButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Fragment frag = new ImportFragment();
//                ((NavigationActivity) getActivity()).pushFragment(frag, NavigationActivity.Tabs.REQUEST.ordinal());
//            }
//        });

        mConverterTextView = (TextView) mView.findViewById(R.id.textview_converter);

        mConverterTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mDenominationTextView = (TextView) mView.findViewById(R.id.request_selected_denomination);
        mBitcoinAddress = (TextView) mView.findViewById(R.id.request_bitcoin_address);

        final SegmentedGroup buttons = (SegmentedGroup) mView.findViewById(R.id.request_bottom_buttons);
        mCopyButton = (Button) buttons.findViewById(R.id.fragment_triple_selector_left);
        mCopyButton.setText(getString(R.string.fragment_request_copy_title));
        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.clearCheck();
                copyToClipboard();
            }
        });
        mEmailButton = (Button) buttons.findViewById(R.id.fragment_triple_selector_center);
        mEmailButton.setText(getString(R.string.fragment_request_email_title));
        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.clearCheck();
                startEmail();
            }
        });
        mSMSButton = (Button) buttons.findViewById(R.id.fragment_triple_selector_right);
        mSMSButton.setText(getString(R.string.fragment_request_sms_title));
        mSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.clearCheck();
                startSMS();
            }
        });

        SegmentedGroup fiatSelectors = (SegmentedGroup) mView.findViewById(R.id.request_fiat_btc_select);
        mFiatSelect = (Button) fiatSelectors.findViewById(R.id.layout_double_selector_left);
        mFiatSelect.setSelected(true);
        mFiatSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapAmount();
            }
        });

        mBitcoinSelect = (Button) fiatSelectors.findViewById(R.id.layout_double_selector_right);
        mBitcoinSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapAmount();
            }
        });

        fiatSelectors.check(mFiatSelect.getId());

        // Prevent OS keyboard from showing
        try {
            final Method method = EditText.class.getMethod(
                    "setShowSoftInputOnFocus"
                    , new Class[] { boolean.class });
            method.setAccessible(true);
            method.invoke(mAmountField, false);
        } catch (Exception e) {
            // ignore
        }

        return mView;
    }

    private void setConversionText(int currencyNum) {
        mConverterTextView.setText(mCoreAPI.BTCtoFiatConversion(currencyNum));
    }

    private void swapAmount() {
        int walletPosition = pickWalletSpinner.getSelectedItemPosition();
        if(walletPosition < 0) {
            return;
        }
        Wallet wallet = mWallets.get(walletPosition);
        mAmountIsBitcoin = !mAmountIsBitcoin;
        if(mAmountIsBitcoin) {
            String fiat = mAmountField.getText().toString();
            try {
                if (!mCoreAPI.TooMuchFiat(fiat, wallet.getCurrencyNum())) {
                    double currency = Double.parseDouble(fiat);
                    mAmountSatoshi = mCoreAPI.CurrencyToSatoshi(currency, wallet.getCurrencyNum());
                    if(mAmountSatoshi == 0) {
                        mAmountField.setText("");
                    }
                    else {
                        mAmountField.setText(mCoreAPI.formatSatoshi(mAmountSatoshi, false));
                    }
                } else {
                    Log.d(TAG, "Too much fiat");
                }
            } catch (NumberFormatException e) {
                //not a double, ignore
            }
        }
        else {
            String bitcoin = mAmountField.getText().toString();
            if (!mCoreAPI.TooMuchBitcoin(bitcoin)) {
                mAmountSatoshi = mCoreAPI.denominationToSatoshi(bitcoin);
                if(mAmountSatoshi == 0) {
                    mAmountField.setText("");
                }
                else {
                    mAmountField.setText(mCoreAPI.FormatCurrency(mAmountSatoshi, mSelectedWallet.getCurrencyNum(), false, false));
                }
            } else {
                Log.d(TAG, "Too much bitcoin");
            }
        }
    }

    private void updateAmount() {

        int walletPosition = pickWalletSpinner.getSelectedItemPosition();
        if(walletPosition < 0) {
            return;
        }
        Wallet wallet = mWallets.get(walletPosition);
        if (mAmountIsBitcoin) {
            String bitcoin = mAmountField.getText().toString();
            if (!mCoreAPI.TooMuchBitcoin(bitcoin)) {
                mAmountSatoshi = mCoreAPI.denominationToSatoshi(bitcoin);
            } else {
                Log.d(TAG, "Too much bitcoin");
            }
        } else {
            String fiat = mAmountField.getText().toString();
            try {
                if (!mCoreAPI.TooMuchFiat(fiat, wallet.getCurrencyNum())) {
                    double currency = Double.parseDouble(fiat);
                    mAmountSatoshi = mCoreAPI.CurrencyToSatoshi(currency, wallet.getCurrencyNum());
                } else {
                    Log.d(TAG, "Too much fiat");
                }
            } catch (NumberFormatException e) {
                //not a double, ignore
            }
        }

        createNewQRBitmap();
    }

    @Override
    public void onResume() {
        super.onResume();

        mCoreAPI.setOnWalletLoadedListener(this);

        checkFirstUsage();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCoreAPI.removeExchangeRateChangeListener(this);

        mSavedSatoshi = mCoreAPI.denominationToSatoshi(mAmountField.getText().toString());

        mCoreAPI.setOnWalletLoadedListener(null);
    }

    @Override
    public void OnExchangeRatesChange() {
        if (mSelectedWallet != null) {
            setConversionText(mSelectedWallet.getCurrencyNum());
            updateAmount();
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
//                focus(mFiatField);
            } else if(AirbitzApplication.getCurrentWallet() != null) {
                mSelectedWallet = mCoreAPI.getWalletFromUUID(AirbitzApplication.getCurrentWallet());
            } else {
                mFromIndex = 0;
                if (mWallets != null && !mWallets.isEmpty())
                    mSelectedWallet = mWallets.get(mFromIndex);
            }

            if(mSelectedWallet==null) {
                mSelectedWallet = mWallets.get(0);
            }
        }

        mDenominationTextView.setText(mCoreAPI.currencyCodeLookup(mSelectedWallet.getCurrencyNum()));
        setConversionText(mSelectedWallet.getCurrencyNum());
        mCoreAPI.addExchangeRateChangeListener(this);

        mAutoUpdatingTextFields = true;
        if (mSavedSatoshi != null) {
            mFromIndex = mSavedIndex;
            if(mFromIndex < mWallets.size()) {
                mAmountField.setText(mCoreAPI.formatSatoshi(mSavedSatoshi, false));
                mSelectedWallet = mWallets.get(mFromIndex);
                pickWalletSpinner.setSelection(mFromIndex);
                mSavedSatoshi = null;
            }
        } else {
            mAmountField.setText("");
            pickWalletSpinner.setSelection(mFromIndex);
        }

        mFiatSelect.setText(mCoreAPI.getCurrencyAcronym(mSelectedWallet.getCurrencyNum()));
        mBitcoinSelect.setText(mCoreAPI.getDefaultBTCDenomination());
        mAutoUpdatingTextFields = false;

        // Set the wallet selection globally
        AirbitzApplication.setCurrentWallet(mSelectedWallet.getUUID());
    }

    private void checkFirstUsage() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        int count = prefs.getInt(FIRST_USAGE_COUNT, 1);
        if(count <= 2) {
            count++;
            mActivity.ShowFadingDialog(getString(R.string.request_qr_first_usage), 5000);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(FIRST_USAGE_COUNT, count);
            editor.apply();
        }
    }

    private void checkNFC() {
        final NfcManager nfcManager = (NfcManager) mActivity.getSystemService(Context.NFC_SERVICE);
        mNfcAdapter = nfcManager.getDefaultAdapter();

        if (mNfcAdapter != null && mNfcAdapter.isEnabled() && SettingFragment.getNFCPref()) {
//            mNFCImageView.setVisibility(View.VISIBLE);
            mNfcAdapter.setNdefPushMessageCallback(this, mActivity);
        }
    }

    @Override
    public void onContactSelection(Contact contact) {
        if (emailType) {
            if (mQRBitmap != null) {
                mContentURL = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), mQRBitmap, mAddress, null);
                if (mContentURL != null) {
                    finishEmail(contact, Uri.parse(mContentURL));
                } else {
                    showNoQRAttached(contact);
                }
            } else {
                mActivity.ShowOkMessageDialog("", getString(R.string.request_qr_bitmap_error));
            }
        } else {
            finishSMS(contact);
        }
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.request_qr_title), mAddress);
        clipboard.setPrimaryClip(clip);
        mActivity.ShowFadingDialog(getString(R.string.request_qr_ble_copied));
    }

    private void startSMS() {
        emailType = false;
        ContactPickerFragment fragment = new ContactPickerFragment();
        fragment.setContactSelectionListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(ContactPickerFragment.TYPE, ContactPickerFragment.SMS);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment, NavigationActivity.Tabs.REQUEST.ordinal());
    }

    private void finishSMS(Contact contact) {
        String defaultName = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defaultName = Telephony.Sms.getDefaultSmsPackage(mActivity); // Android 4.4 and up
        }

        String name = getString(R.string.request_qr_unknown);
        if (mCoreAPI.coreSettings().getBNameOnPayments()) {
            name = mCoreAPI.coreSettings().getSzFullName();
            if(name==null) {
                name = getString(R.string.request_qr_unknown);
            }
        }
        String textToSend = fillTemplate(R.raw.sms_template, name);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        if (defaultName != null) {
            intent.setPackage(defaultName);
        }
        intent.setData(Uri.parse("smsto:" + contact.getPhone()));  // This ensures only SMS apps respond
        intent.putExtra("sms_body", textToSend);

        startActivity(Intent.createChooser(intent, "SMS"));

        mCoreAPI.finalizeRequest(contact, "SMS", mID, mSelectedWallet);
    }

    private void startEmail() {
        emailType = true;
        ContactPickerFragment fragment = new ContactPickerFragment();
        fragment.setContactSelectionListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(ContactPickerFragment.TYPE, ContactPickerFragment.EMAIL);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment, NavigationActivity.Tabs.REQUEST.ordinal());
    }

    private void finishEmail(Contact contact, Uri uri) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        if (uri != null) {
            uris.add(Uri.parse(mContentURL));
        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getEmail()});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.request_qr_email_title));

        String name = getString(R.string.request_qr_unknown);
        if (mCoreAPI.coreSettings().getBNameOnPayments()) {
            name = mCoreAPI.coreSettings().getSzFullName();
        }

        String html = fillTemplate(R.raw.email_template, name);

        intent.putExtra(Intent.EXTRA_STREAM, uris);
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(html));
        intent.putExtra(Intent.EXTRA_HTML_TEXT, html);
        startActivity(Intent.createChooser(intent, "email"));

        mCoreAPI.finalizeRequest(contact, "Email", mID, mSelectedWallet);
    }

    private void showNoQRAttached(final Contact contact) {
        getString(R.string.request_qr_image_store_error);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.request_qr_image_store_error))
                .setTitle("")
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finishEmail(contact, null);
                                dialog.cancel();
                            }
                        }
                );
        builder.create().show();
    }

    private String fillTemplate(int id, String fullName) {
        String amountBTC = mCoreAPI.formatSatoshi(mAmountSatoshi, false, 8);
        String amountBits = mCoreAPI.formatSatoshi(mAmountSatoshi, false, 2);

        String bitcoinURL = "bitcoin://";
        String redirectURL = mRequestURI;

        if (mRequestURI.contains("bitcoin:")) {
            String[] typeAddress = mRequestURI.split(":");
            String address = typeAddress[1];

            bitcoinURL += address;
            redirectURL = "https://airbitz.co/blf/?address=" + address;
        }

        String content = Common.readRawTextFile(getActivity(), id);

        List<String> searchList = new ArrayList<String>();
        searchList.add("[[abtag FROM]]");
        searchList.add("[[abtag BITCOIN_URL]]");
        searchList.add("[[abtag REDIRECT_URL]]");
        searchList.add("[[abtag BITCOIN_URI]]");
        searchList.add("[[abtag ADDRESS]]");
        searchList.add("[[abtag AMOUNT_BTC]]");
        searchList.add("[[abtag AMOUNT_BITS]]");

        List<String> replaceList = new ArrayList<String>();
        if (fullName == null)
            replaceList.add("");
        else
            replaceList.add(fullName);
        replaceList.add(bitcoinURL);
        replaceList.add(redirectURL);
        replaceList.add(mRequestURI);
        replaceList.add(mAddress);
        replaceList.add(amountBTC);
        replaceList.add(amountBits);

        for (int i = 0; i < searchList.size(); i++) {
            content = content.replace(searchList.get(i), replaceList.get(i));
        }
        return content;
    }

    public boolean isShowingQRCodeFor(String walletUUID, String txId) {
        Log.d(TAG, "isShowingQRCodeFor: " + walletUUID + " " + txId);
        Transaction tx = mCoreAPI.getTransaction(walletUUID, txId);
        if (tx.getOutputs() == null || mAddress == null) {
            return false;
        }
        Log.d(TAG, "isShowingQRCodeFor: hasOutputs");
        for (CoreAPI.TxOutput output : tx.getOutputs()) {
            Log.d(TAG, output.getmInput() + " " + mAddress + " " + output.getAddress());
            if (!output.getmInput() && mAddress.equals(output.getAddress())) {
                return true;
            }
        }
        Log.d(TAG, "isShowingQRCodeFor: noMatch");
        return false;
    }

    public boolean isMerchantDonation() {
        return (mAmountSatoshi == 0 && SettingFragment.getMerchantModePref());
    }

    public long requestDifference(String walletUUID, String txId) {
        Log.d(TAG, "requestDifference: " + walletUUID + " " + txId);
        if (mAmountSatoshi > 0) {
            Transaction tx = mCoreAPI.getTransaction(walletUUID, txId);
            return mAmountSatoshi - tx.getAmountSatoshi();
        } else {
            return 0;
        }
    }

    public void updateWithAmount(long newAmount) {
        mAmountSatoshi = newAmount;
        mAmountField.setText(
                String.format(getResources().getString(R.string.bitcoing_remaining),
                        mCoreAPI.formatSatoshi(mAmountSatoshi, true))
        );

        createNewQRBitmap();

        // Alert the user
        alertPartialPayment();
    }

    public void amountChanged() {
        mAmountSatoshi = mCoreAPI.denominationToSatoshi(mAmountField.getText().toString());
        createNewQRBitmap();
    }

    private void createNewQRBitmap() {
        if (mCreateBitmapTask != null) {
            mCreateBitmapTask.cancel(true);
        }
        // Create a new request and qr code
        mID = null;
        mCreateBitmapTask = new CreateBitmapTask();
        mCreateBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void alertPartialPayment() {
        if(mPartialDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
            builder.setMessage(getResources().getString(R.string.received_partial_bitcoin_message))
                    .setTitle(getResources().getString(R.string.received_partial_bitcoin_title))
                    .setCancelable(true)
                    .setNeutralButton(getResources().getString(R.string.string_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mPartialDialog.cancel();
                                    mPartialDialog = null;
                                }
                            }
                    );
            mPartialDialog = builder.create();
            mPartialDialog.show();
            mHandler.postDelayed(dialogKiller, PARTIAL_PAYMENT_TIMEOUT);
        }
    }

    /*
     * Send an Ndef message when a device with NFC is detected
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        if(mRequestURI != null) {
            Log.d(TAG, "Creating NFC request: " + mRequestURI);
            return new NdefMessage(NdefRecord.createUri(mRequestURI));
        }
        else
            return null;
    }

    @Override
    public void OnCalculatorKeyPressed(String tag) {
        if (tag.equals("done")) {
            showCalculator(false);
        }
    }
    
    private void showCalculator(boolean show) {
        if(show) {
            mCalculator.setVisibility(View.VISIBLE);
        }
        else {
            mCalculator.setVisibility(View.GONE);
        }
    }

//    @Override
//    public void onRefresh() {
//        if(mSelectedWallet != null) {
//            mCoreAPI.connectWatcher(mSelectedWallet.getUUID());
//        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mSwipeLayout.setRefreshing(false);
//            }
//        }, 1000);
//    }

    public class CreateBitmapTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
//            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "Starting Receive Request at:" + System.currentTimeMillis());
            if(mID == null) {
                mID = mCoreAPI.createReceiveRequestFor(mSelectedWallet, "", "", mAmountSatoshi);
                mAddress = mCoreAPI.getRequestAddress(mSelectedWallet.getUUID(), mID);
                mQRBitmap = null;
            }
            if (mQRBitmap == null) {
                try {
                    // data in barcode is like bitcoin:address?amount=0.001
                    Log.d(TAG, "Starting QRCodeBitmap at:" + System.currentTimeMillis());
                    mQRBitmap = mCoreAPI.getQRCodeBitmap(mSelectedWallet.getUUID(), mID);
                    mQRBitmap = Common.AddWhiteBorder(mQRBitmap);
                    Log.d(TAG, "Ending QRCodeBitmap at:" + System.currentTimeMillis());
                    mRequestURI = mCoreAPI.getRequestURI();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(isAdded()) {
                onCancelled();
                if(success) {
                    checkNFC();
                    checkBle();
                    mBitcoinAddress.setText(mAddress);
                    if (mQRBitmap != null) {
                        mQRView.setImageBitmap(mQRBitmap);
                    }
                    mCoreAPI.prioritizeAddress(mAddress, mSelectedWallet.getUUID());
                }
            }
        }

        @Override
        protected void onCancelled() {
            mCreateBitmapTask = null;
//            mActivity.showModalProgress(false);
        }
    }

    //******************************** BLE support
    // See BluetoothListView for protocol explanation
    private BluetoothLeAdvertiser mBleAdvertiser;
    private BluetoothGattServer mGattServer;
    private AdvertiseCallback mAdvCallback;

    private void checkBle() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && SettingFragment.getBLEPref() &&
                BleUtil.isBleAdvertiseAvailable(mActivity)) {
//            mBLEImageView.setVisibility(View.VISIBLE);
            stopAirbitzAdvertise();
            startAirbitzAdvertise(mRequestURI);
            mHandler.postDelayed(mContinuousReAdvertiseRunnable, READVERTISE_REPEAT_PERIOD);
        }
    }

    // start Advertise
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startAirbitzAdvertise(String data) {
        BluetoothManager manager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();

        // The name maximum is 26 characters tested
        String[] separate = data.split(":");
        String address, name = " ";
        if(separate[1] != null && separate[1].length() >= 10) {
            address = separate[1].substring(0, 10);
        }
        else {
            address = data;
        }
        if (mCoreAPI.coreSettings().getBNameOnPayments()) {
            name = mCoreAPI.coreSettings().getSzFullName();
            if(name==null || name.isEmpty()) {
                name = " ";
            }
        }
        String advertiseText = address + name;
        advertiseText = advertiseText.length()>26 ?
                advertiseText.substring(0, 26) : advertiseText;
        Log.d(TAG, "AdvertiseText = "+adapter.getName());
        adapter.setName(advertiseText);

        mBleAdvertiser = adapter.getBluetoothLeAdvertiser();
        AirbitzGattServerCallback bgsc = new AirbitzGattServerCallback();
        mGattServer = BleUtil.getManager(mActivity).openGattServer(mActivity, bgsc);
        bgsc.setupServices(mActivity, mGattServer, data);

        mAdvCallback = new AdvertiseCallback() {
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                if (settingsInEffect != null) {
                    Log.d(TAG, "onStartSuccess TxPowerLv="
                            + settingsInEffect.getTxPowerLevel()
                            + " mode=" + settingsInEffect.getMode()
                            + " timeout=" + settingsInEffect.getTimeout());
                } else {
                    Log.d(TAG, "onStartSuccess, settingInEffect is null");
                }
            }

            public void onStartFailure(int errorCode) {
                mActivity.ShowFadingDialog(getString(R.string.request_qr_ble_advertise_start_failed));
            }
        };

        mBleAdvertiser.startAdvertising(
                createAirbitzAdvertiseSettings(true, 0),
                createAirbitzAdvertiseData(),
                createAirbitzScanResponseData(),
                mAdvCallback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopAirbitzAdvertise() {
        if (mGattServer != null) {
            mGattServer.clearServices();
            mGattServer.close();
            mGattServer = null;
        }
        if (mBleAdvertiser != null) {
            mBleAdvertiser.stopAdvertising(mAdvCallback);
            mBleAdvertiser = null;
            mAdvCallback = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseSettings createAirbitzAdvertiseSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setConnectable(connectable);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseData createAirbitzAdvertiseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(BleUtil.AIRBITZ_SERVICE_UUID)));
        AdvertiseData data = builder.build();
        return data;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseData createAirbitzScanResponseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(true);
        AdvertiseData data = builder.build();
        return data;
    }

    /*
    * Callback for BLE peripheral mode beacon
    */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public class AirbitzGattServerCallback extends BluetoothGattServerCallback {
        private String TAG = getClass().getSimpleName();

        private NavigationActivity mActivity;

        String mData;

        private BluetoothGattServer mGattServer;

        public void setupServices(NavigationActivity activity, BluetoothGattServer gattServer, String data) {
            mActivity = activity;
            if (gattServer == null || data == null) {
                throw new IllegalArgumentException("gattServer or data is null");
            }
            mGattServer = gattServer;
            mData = data;

            // setup Airbitz services
            {
                BluetoothGattService ias = new BluetoothGattService(
                        UUID.fromString(BleUtil.AIRBITZ_SERVICE_UUID),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);
                // alert level char.
                BluetoothGattCharacteristic alc = new BluetoothGattCharacteristic(
                        UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID),
                        BluetoothGattCharacteristic.PROPERTY_READ |
                                BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                                BluetoothGattCharacteristic.PERMISSION_WRITE);
                ias.addCharacteristic(alc);
                mGattServer.addService(ias);
            }
        }

        public void onServiceAdded(int status, BluetoothGattService service) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServiceAdded status=GATT_SUCCESS service="
                        + service.getUuid().toString());
            } else {
                mActivity.ShowFadingDialog(mActivity.getString(R.string.request_qr_ble_invalid_service));
            }
        }

        public void onConnectionStateChange(BluetoothDevice device, int status,
                                            int newState) {
            Log.d(TAG, "onConnectionStateChange status =" + status + "-> state =" + newState);
        }

        // ghost of didReceiveReadRequest
        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
            if (characteristic.getUuid().equals(UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID))) {
                Log.d(TAG, "AIRBITZ_CHARACTERISTIC_READ");
                characteristic.setValue(mData.substring(offset));
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }
        }

        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                                 boolean responseNeeded, int offset, byte[] value) {
            Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                    + Boolean.toString(preparedWrite) + " responseNeeded="
                    + Boolean.toString(responseNeeded) + " offset=" + offset
                    + " value=" + new String(value) );
            if (characteristic.getUuid().equals(UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID))) {
                Log.d(TAG, "Airbitz characteristic received");
                String displayName = new String(value);
                if(displayName.isEmpty()) {
                    displayName = getString(R.string.request_qr_unknown);
                }
                Contact nameInContacts = findMatchingContact(displayName);
                displayName += "\nConnected";
                if(nameInContacts != null) {
                    mActivity.ShowFadingDialog(displayName, nameInContacts.getThumbnail(), 2000, true);
                }
                else {
                    mActivity.ShowFadingDialog(displayName, "", 2000, true); // Show the default icon
                }

                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }
        }
    }

    private Contact findMatchingContact(String displayName) {
        String id = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,Uri.encode(displayName.trim()));
        Cursor mapContact = mActivity.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI}, null, null, null);
        if(mapContact.moveToNext())
        {
            id = mapContact.getString(mapContact.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            return new Contact(displayName, null, null, id);
        }
        else {
            return null;
        }
    }

    Runnable mContinuousReAdvertiseRunnable = new Runnable() {
        @Override
        public void run() {
            stopAirbitzAdvertise();
            startAirbitzAdvertise(mRequestURI);
            mHandler.postDelayed(this, READVERTISE_REPEAT_PERIOD);
        }
    };
}
