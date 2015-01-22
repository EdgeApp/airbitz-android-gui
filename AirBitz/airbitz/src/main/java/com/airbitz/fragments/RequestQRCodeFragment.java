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

package com.airbitz.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.models.Contact;
import com.airbitz.objects.BleUtil;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RequestQRCodeFragment extends BaseFragment implements
        ContactPickerFragment.ContactSelection,
        NfcAdapter.CreateNdefMessageCallback,
        SwipeRefreshLayout.OnRefreshListener
{
    private final String TAG = getClass().getSimpleName();

    private final double BORDER_THICKNESS = 0.03;
    public static final int PARTIAL_PAYMENT_TIMEOUT = 10000;
    private ImageView mQRView;
    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressButton mCancelButton;
    private HighlightOnPressButton mSMSButton;
    private HighlightOnPressButton mEmailButton;
    private HighlightOnPressButton mCopyButton;
    private HighlightOnPressButton mRefreshButton;
    private TextView mBitcoinAmount;
    private TextView mBitcoinAddress;
    private TextView mTitleTextView;
    private Bitmap mQRBitmap;
    private String mID;
    private String mAddress;
    private String mContentURL;
    private String mRequestURI;
    private long mAmountSatoshi;
    private boolean emailType = false;
    private Bundle bundle;
    private Wallet mWallet;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
    private CreateBitmapTask mCreateBitmapTask;
    private AlertDialog mPartialDialog;
    private ImageView mNFCImageView;
    private ImageView mBLEImageView;
    private NfcAdapter mNfcAdapter;
    private SwipeRefreshLayout mSwipeLayout;

    final Runnable dialogKiller = new Runnable() {
        @Override
        public void run() {
            if (mPartialDialog != null) {
                mPartialDialog.dismiss();
                mPartialDialog = null;
            }
        }
    };
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        mCoreAPI = CoreAPI.getApi();
        mWallet = mCoreAPI.getWalletFromUUID(bundle.getString(Wallet.WALLET_UUID));
        mAmountSatoshi = bundle.getLong(RequestFragment.SATOSHI_VALUE, 0L);
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public void onPause() {
        mCoreAPI.prioritizeAddress(null, mWallet.getUUID());
        stopAirbitzAdvertise();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_request_qrcode, container, false);
        } else {
            return mView;
        }

        ((NavigationActivity) getActivity()).hideNavBar();

        mQRView = (ImageView) mView.findViewById(R.id.qr_code_view);
        mNFCImageView = (ImageView) mView.findViewById(R.id.fragment_request_qrcode_nfc_image);
        mBLEImageView = (ImageView) mView.findViewById(R.id.fragment_request_qrcode_ble_image);

        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.fragment_request_qrcode_swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);

        mRefreshButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_request_qr_refresh);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeLayout.setRefreshing(true);
                onRefresh();
            }
        });

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.request_qr_title);

        mBitcoinAmount = (TextView) mView.findViewById(R.id.textview_bitcoin_amount);
        mBitcoinAmount.setText(mCoreAPI.formatSatoshi(mAmountSatoshi, true));

        mBitcoinAddress = (TextView) mView.findViewById(R.id.textview_address);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);

        mCopyButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_qrcode_copy_button);
        mSMSButton = (HighlightOnPressButton) mView.findViewById(R.id.button_sms_address);
        mEmailButton = (HighlightOnPressButton) mView.findViewById(R.id.button_email_address);
        mCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_qrcode_cancel_button);

        mSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSMS();
            }
        });

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyToClipboard();
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEmail();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                ((NavigationActivity) getActivity()).showNavBar();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                ((NavigationActivity) getActivity()).showNavBar();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.REQUEST_QR), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCreateBitmapTask = new CreateBitmapTask();
        mCreateBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkNFC() {
        final NfcManager nfcManager = (NfcManager) mActivity.getSystemService(Context.NFC_SERVICE);
        mNfcAdapter = nfcManager.getDefaultAdapter();

        if (mNfcAdapter != null && mNfcAdapter.isEnabled() && SettingFragment.getNFCPref()) {
            mNFCImageView.setVisibility(View.VISIBLE);
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

    private Bitmap addWhiteBorder(Bitmap inBitmap) {
        Bitmap imageBitmap = Bitmap.createBitmap((int) (inBitmap.getWidth() * (1 + BORDER_THICKNESS * 2)),
                (int) (inBitmap.getHeight() * (1 + BORDER_THICKNESS * 2)), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageBitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        canvas.drawPaint(p);
        canvas.drawBitmap(inBitmap, (int) (inBitmap.getWidth() * BORDER_THICKNESS), (int) (inBitmap.getHeight() * BORDER_THICKNESS), null);
        return imageBitmap;
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
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.REQUEST.ordinal());
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

        mCoreAPI.finalizeRequest(contact, "SMS", mID, mWallet);
    }

    private void startEmail() {
        emailType = true;
        ContactPickerFragment fragment = new ContactPickerFragment();
        fragment.setContactSelectionListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(ContactPickerFragment.TYPE, ContactPickerFragment.EMAIL);
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.REQUEST.ordinal());
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

        mCoreAPI.finalizeRequest(contact, "Email", mID, mWallet);
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
        String amountMBTC = mCoreAPI.formatSatoshi(mAmountSatoshi, false, 5);

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
        searchList.add("[[abtag AMOUNT_MBTC]]");

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
        replaceList.add(amountMBTC);

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
        mBitcoinAmount.setText(
                String.format(getResources().getString(R.string.bitcoing_remaining),
                        mCoreAPI.formatSatoshi(mAmountSatoshi, true))
        );

        if (mCreateBitmapTask != null) {
            mCreateBitmapTask.cancel(true);
        }
        // Create a new request and qr code
        mCreateBitmapTask = new CreateBitmapTask();
        mCreateBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Alert the user
        alertPartialPayment();
    }

    private void alertPartialPayment() {
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
    public void onRefresh() {
        if(mWallet != null) {
            mCoreAPI.connectWatcher(mWallet.getUUID());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(false);
            }
        }, 1000);
    }

    public class CreateBitmapTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            ((NavigationActivity) getActivity()).showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "Starting Receive Request at:" + System.currentTimeMillis());
            mID = mCoreAPI.createReceiveRequestFor(mWallet, "", "", mAmountSatoshi);
            if (mID != null) {
                Log.d(TAG, "Starting Request Address at:" + System.currentTimeMillis());
                mAddress = mCoreAPI.getRequestAddress(mWallet.getUUID(), mID);
                try {
                    // data in barcode is like bitcoin:address?amount=0.001
                    Log.d(TAG, "Starting QRCodeBitmap at:" + System.currentTimeMillis());
                    mQRBitmap = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), mID);
                    mQRBitmap = addWhiteBorder(mQRBitmap);
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
            ((NavigationActivity) getActivity()).showModalProgress(false);
            mCreateBitmapTask = null;
            if(success) {
                checkNFC();
                checkBle();
                mBitcoinAddress.setText(mAddress);
                if (mQRBitmap != null) {
                    mQRView.setImageBitmap(mQRBitmap);
                }
                mCoreAPI.prioritizeAddress(mAddress, mWallet.getUUID());
            }
        }

        @Override
        protected void onCancelled() {
            mCreateBitmapTask = null;
            ((NavigationActivity) getActivity()).showModalProgress(false);
        }
    }

    //******************************** BLE support
    // See BluetoothListView for protocol explanation
    private BluetoothLeAdvertiser mBleAdvertiser;
    private BluetoothGattServer mGattServer;
    private AdvertiseCallback mAdvCallback;
    private String mData;

    private void checkBle() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && SettingFragment.getBLEPref() &&
                BleUtil.isBleAdvertiseAvailable(mActivity)) {
            mBLEImageView.setVisibility(View.VISIBLE);
            startAirbitzAdvertise(mRequestURI);
        }
    }

    // start Advertise
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startAirbitzAdvertise(String data) {
        BluetoothManager manager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();

        mData = data;
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
        bgsc.setupServices(mActivity, mGattServer, mData);

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
            };
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
    @TargetApi(21)
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
}
