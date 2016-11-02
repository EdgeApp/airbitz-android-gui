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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.airbitz.bitbeacon.BeaconSend;
import co.airbitz.bitbeacon.BleDevice;
import co.airbitz.bitbeacon.BleUtil;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Settings;
import co.airbitz.core.Wallet;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.BluetoothSearchAdapter;
import com.airbitz.adapters.WalletOtherAdapter;
import com.airbitz.api.Constants;
import com.airbitz.api.CoreWrapper;
import com.airbitz.api.WalletWrapper;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.settings.SettingFragment;
import com.airbitz.fragments.wallet.WalletsFragment;
import com.airbitz.objects.QRCamera;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.ArrayList;
import java.util.List;

public abstract class ScanFragment
    extends WalletBaseFragment implements
        BeaconSend.OnPeripheralSelected,
        QRCamera.OnScanResult
{
    private final String TAG = getClass().getSimpleName();

    public static final String AMOUNT_SATOSHI = "com.airbitz.Sendfragment_AMOUNT_SATOSHI";
    public static final String AMOUNT_ADDITIONAL_FEES = "com.airbitz.Sendfragment_AMOUNT_ADDITIONAL_FEES";
    public static final String AMOUNT_FIAT = "com.airbitz.Sendfragment_AMOUNT_FIAT";
    public static final String LABEL = "com.airbitz.Sendfragment_LABEL";
    public static final String CATEGORY = "com.airbitz.Sendfragment_CATEGORY";
    public static final String RETURN_URL = "com.airbitz.Sendfragment_RETURN_URL";
    public static final String NOTES = "com.airbitz.Sendfragment_NOTES";
    public static final String LOCKED = "com.airbitz.Sendfragment_LOCKED";
    public static final String SIGN_ONLY = "com.airbitz.Sendfragment_SIGN_ONLY";
    public static final String UUID = "com.airbitz.Sendfragment_UUID";
    public static final String IS_UUID = "com.airbitz.Sendfragment_IS_UUID";
    public static final String FROM_WALLET_UUID = "com.airbitz.Sendfragment_FROM_WALLET_UUID";
    public static final String ADDRESS = "com.airbitz.Sendfragment_ADDRESS";

    protected enum RunMode { SEND, IMPORT };
    protected RunMode mMode;

    private Handler mHandler;
    private Button mTransferButton;
    private Button mAddressButton;
    private Button mGalleryButton;
    private View mFlashButton;
    private ListView mOtherWalletsListView;
    private RelativeLayout mListviewContainer;
    private RelativeLayout mCameraLayout;
    private RelativeLayout mBluetoothLayout;
    private ListView mBluetoothListView;
    private BeaconSend mBeaconSend;
    private List<WalletWrapper> mOtherWalletsList;//NAMES
    private String mReturnURL;
    private WalletOtherAdapter mOtherWalletsAdapter;
    protected View mView;
    protected View mButtonBar;
    protected QRCamera mQRCamera;
    protected AirbitzCore mCoreApi;
    protected ClipboardManager mClipboard;

    /**
     * Process text from a QR code, BLE request or file import
     */
    protected abstract void processText(String strTo);

    /**
     * Process text from a QR code, BLE request or file import
     */
    protected abstract void onCameraScanResult(String result);

    /**
     * Text to display when user clicks the address button.
     */
    protected abstract String getAddressDialogTitle();

    /**
     * Launch fragment specific Help.
     */
    protected abstract void launchHelp();

    /**
     * Only applicable if mMode == RunMode.SEND
     */
    protected abstract void transferWalletSelected(Wallet w);

    List<BleDevice> mPeripherals = new ArrayList<BleDevice>();
    BluetoothSearchAdapter mSearchAdapter;

    public ScanFragment(RunMode mode) {
        mMode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (SettingFragment.getBLEPref()) {
                mBeaconSend = new BeaconSend(mActivity);
                mBeaconSend.setOnPeripheralSelectedListener(this);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        mCoreApi = AirbitzCore.getApi();

        mView = inflater.inflate(R.layout.fragment_scan, container, false);

        mSearchAdapter = new BluetoothSearchAdapter(mActivity, mPeripherals);
        mBluetoothListView = (ListView) mView.findViewById(R.id.fragment_send_bluetooth_layout);
        mBluetoothListView.setAdapter(mSearchAdapter);
        if (mBeaconSend != null) {
            mBluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    BleDevice device = mPeripherals.get(i);
                    if (!device.hasErrors()) {
                        mSearchAdapter.selectItem(view);

                        showConnecting(device);
                        stopBluetoothSearch();
                        mBeaconSend.connectGatt(device);
                    } else {
                        hideProcessing();
                    }
                }
            });
        }

        mCameraLayout = (RelativeLayout) mView.findViewById(R.id.fragment_send_layout_camera);
        mQRCamera = new QRCamera(this, mCameraLayout);

        mFlashButton = mView.findViewById(R.id.fragment_send_button_flash);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQRCamera.setFlashOn(!mQRCamera.isFlashOn());
            }
        });

        mGalleryButton = (Button) mView.findViewById(R.id.fragment_send_button_photos);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in, QRCamera.RESULT_LOAD_IMAGE);
            }
        });

        mTransferButton = (Button) mView.findViewById(R.id.fragment_send_button_transfer);
        mTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOtherWallets();
            }
        });

        mAddressButton = (Button) mView.findViewById(R.id.fragment_send_button_address);
        mAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog();
            }
        });
        mButtonBar = mView.findViewById(R.id.fragment_send_buttons);

        mOtherWalletsList = new ArrayList<WalletWrapper>();
        mOtherWalletsAdapter = new WalletOtherAdapter(getActivity(), mOtherWalletsList);

        mOtherWalletsListView = (ListView) mView.findViewById(R.id.fragment_send_transfer_list);
        View headerView = inflater.inflate(R.layout.fragment_send_other_wallet_layout, null, true);
        ImageButton headerCloseButton = (ImageButton) headerView.findViewById(R.id.fragment_send_header_close_button);
        headerCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideOtherWallets();
            }
        });
        mOtherWalletsListView.addHeaderView(headerView);
        mOtherWalletsListView.setAdapter(mOtherWalletsAdapter);

        mOtherWalletsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WalletWrapper w = mOtherWalletsList.get(i-1);
                if (w.isSynced()) {
                    transferWalletSelected(w.wallet());
                }
            }
        });
        if (mMode == RunMode.IMPORT) {
            mTransferButton.setVisibility(View.GONE);
        }
        return mView;
    }

    // delegated from the containing fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == QRCamera.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            mQRCamera.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected float getFabTop() {
        return mActivity.getFabTop() - mButtonBar.getHeight();
    }

    public void startCamera() {
        mQRCamera.startCamera();
    }

    public void stopCamera() {
        mCoreApi.logi("stopCamera");
        mQRCamera.stopCamera();
    }

    public void stopScanning() {
        mQRCamera.stopScanning();
    }

    @Override
    public void onScanResult(String result) {
        onCameraScanResult(result);
    }

    @Override
    protected void walletChanged(Wallet newWallet) {
        super.walletChanged(newWallet);
        updateWalletOtherList();
    }

    @Override
    public boolean onBackPress() {
        if (super.onBackPress()) {
            return true;
        }
        return hideOtherWallets();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBeaconSend != null) {
            Settings settings = mAccount.settings();
            if (settings != null && settings.nameOnPayments()) {
                String name = settings.fullName();
                mBeaconSend.setBroadcastName(name);
            } else {
                mBeaconSend.setBroadcastName(
                    getResources().getString(R.string.request_qr_unknown));
            }
        }

        if (mHandler == null) {
            mHandler = new Handler();
        }

        startCamera();
        mQRCamera.setOnScanResultListener(this);

        startBluetoothSearch();

        Bundle bundle = getArguments();
        if (bundle != null && !TextUtils.isEmpty(bundle.getString(NavigationActivity.URI_DATA))) {
            showProcessing();
        }
        updateWalletOtherList();
        updatePasteDialog();

        mClipboard.addPrimaryClipChangedListener(mClipboardHandler);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mQRCamera.startScanning();
        } else {
            if (mQRCamera != null) {
                mQRCamera.stopScanning();
            }
        }
    }

    public void updateWalletOtherList() {
        mOtherWalletsList.clear();
        if (mWallets == null) {
            return;
        }
        List<WalletWrapper> filtered = new ArrayList<WalletWrapper>();
        for (WalletWrapper w :  CoreWrapper.wrap(mWallets)) {
            if (w.wallet() != null
                    && !w.wallet().id().equals(mWallet.id())) {
                filtered.add(w);
            }
        }
        mOtherWalletsList.addAll(filtered);
        mOtherWalletsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
        stopBluetoothSearch();
        hideProcessing();
        if (mBeaconSend != null) {
            mBeaconSend.close();
        }
        mClipboard.removePrimaryClipChangedListener(mClipboardHandler);
    }

    @Override
    protected void onAddOptions(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_standard, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isMenuExpanded()) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                return hideOtherWallets();
            case R.id.action_help:
                launchHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showMessageAndStartCameraDialog(int title, int message) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.content(message)
               .title(title)
               .cancelable(false)
               .theme(Theme.LIGHT)
               .neutralText(getResources().getString(R.string.string_ok))
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        mQRCamera.startScanning();
                        dialog.cancel();
                    }
               });
        builder.show();
    }

    private MaterialDialog mPasteDialog = null;
    protected void showAddressDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.alert_address_form, null);
        final EditText editText = (EditText) view.findViewById(R.id.address);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.title(getAddressDialogTitle())
               .customView(view, false)
               .cancelable(false)
               .positiveText(getResources().getString(R.string.string_done))
               .negativeText(getResources().getString(R.string.string_cancel))
               .neutralText(getResources().getString(R.string.string_paste))
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        processText(editText.getText().toString());
                        dialog.dismiss();
                        mQRCamera.startScanning();
                    }
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                        mQRCamera.startScanning();
                    }
                    public void onNeutral(MaterialDialog dialog) {
                        final String pasteData = fromClipboard();
                        editText.setText(pasteData);
                        processText(pasteData);
                        mQRCamera.startScanning();
                    }
                });
        mQRCamera.stopScanning();
        mPasteDialog = builder.show();

        updatePasteDialog();
    }

    // Start the Bluetooth search
    private void startBluetoothSearch() {
        if (mMode == RunMode.SEND
                && mBeaconSend != null
                && BleUtil.isBleAvailable(mActivity)) {
            mBeaconSend.scanForBleDevices(true);
        }
    }

    // Stop the Bluetooth search
    private void stopBluetoothSearch() {
        if (mMode == RunMode.SEND
                && mBeaconSend != null && BleUtil.isBleAvailable(mActivity)) {
            mBeaconSend.scanForBleDevices(false);
            mBeaconSend.close();
        }
    }

    @Override
    public void onPeripheralError(BleDevice device) {
        hideProcessing();
    }

    @Override
    public void onPeripheralConnected(BleDevice device) {
    }

    @Override
    public void onPeripheralDisconnect(BleDevice device) {
    }

    @Override
    public void onPeripheralFailedConnect(BleDevice device) {
        showBleError(R.string.bluetoothlistview_connection_failed);
    }

    @Override
    public void onPeripheralFailedDiscovery() {
        showBleError(R.string.bluetoothlistview_discovery_failed);
    }

    private void showBleError(final int errorMsg) {
        mHandler.post(new Runnable() {
            public void run() {
                hideProcessing();
                startBluetoothSearch();
                mActivity.ShowFadingDialog(getResources().getString(errorMsg));
            }
        });
    }

    private MaterialDialog mDialog = null;
    protected void showConnecting(BleDevice device) {
        String name = device.getName();
        String msg = String.format(mActivity.getString(R.string.fragment_send_connecting_to_device), name);
        showDialog(msg);
    }

    protected void showProcessing() {
        String msg = mActivity.getString(R.string.loading);
        showDialog(msg);
    }

    protected void hideProcessing() {
        if (null != mDialog) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected void showDialog(String message) {
        hideProcessing();
        MaterialDialog.Builder builder =
            new MaterialDialog.Builder(mActivity)
                    .content(message)
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false);
        mDialog = builder.build();
        mDialog.show();
    }

    @Override
    public void devicesUpdated(List<BleDevice> devices) {
        mPeripherals.clear();
        mPeripherals.addAll(devices);
        mSearchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBitcoinURIReceived(final String bitcoinAddress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                processText(bitcoinAddress);
            }
        });
    }

    @Override
    public void onBitcoinURIInvalid() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivity.ShowFadingDialog(getResources().getString(R.string.request_qr_ble_invalid_uri));
            }
        });
    }

    @Override
    public void onBitcoinURIMismatch(String response, final String partialAddress, final String partialAdvertisedAddress){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                hideProcessing();
                String title =
                    mActivity.getString(R.string.bluetoothlistview_address_mismatch_title);
                String message = String.format(getResources().getString(
                    R.string.bluetoothlistview_address_mismatch_message),
                    partialAddress, partialAdvertisedAddress);
                mActivity.ShowOkMessageDialog(title, message);
            }
        });
    }

    @Override
    public void onWalletsLoaded() {
        super.onWalletsLoaded();
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(Constants.WALLET_FROM, "").equals(NavigationActivity.URI_SOURCE)) {
                String uriData = bundle.getString(NavigationActivity.URI_DATA);
                bundle.putString(NavigationActivity.URI_DATA, ""); //to clear the URI_DATA after reading once
                if (!uriData.isEmpty()) {
                    processText(uriData);
                }
            }
        }
        updateWalletOtherList();
    }

    protected boolean isValidAddress(String address) {
        try {
            AirbitzCore.getApi().parseUri(address);
            return true;
        } catch (AirbitzException e) {
            return false;
        }
    }

    protected String fromClipboard() {
        return mClipboard.getText() != null ? mClipboard.getText().toString() : "";
    }

    private boolean toggleOtherWallets() {
        if (mOtherWalletsListView.getVisibility() == View.VISIBLE) {
            return hideOtherWallets();
        } else {
            return showOtherWallets();
        }
    }

    private boolean showOtherWallets() {
        if (mOtherWalletsListView.getVisibility() == View.VISIBLE) {
            return false;
        }

        if (mOtherWalletsList == null || 0 == mOtherWalletsList.size()) {
            mActivity.ShowFadingDialog(
                getString(R.string.fragment_send_create_wallet_to_transfer),
                getResources().getInteger(R.integer.alert_hold_time_help_popups));
            return false;
        }

        ObjectAnimator key =
            ObjectAnimator.ofFloat(mOtherWalletsListView, "translationY", mOtherWalletsListView.getHeight(), 0f);
        key.setDuration(250);
        key.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                finishShowOthers();
            }
        });
        key.start();
        mOtherWalletsListView.setVisibility(View.VISIBLE);
        return true;
    }

    public void finishShowOthers() {
        mOtherWalletsListView.setVisibility(View.VISIBLE);
        mActivity.invalidateOptionsMenu();
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private boolean hideOtherWallets() {
        if (mOtherWalletsListView.getVisibility() == View.INVISIBLE) {
            return false;
        }

        ObjectAnimator key =
            ObjectAnimator.ofFloat(mOtherWalletsListView, "translationY", 0f, mOtherWalletsListView.getHeight());
        key.setDuration(250);
        key.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                finishHideOthers();
            }

            @Override
            public void onAnimationStart(Animator animator) {
                mOtherWalletsListView.setVisibility(View.VISIBLE);
            }
        });
        key.start();
        return true;
    }

    private void finishHideOthers() {
        mOtherWalletsListView.setVisibility(View.INVISIBLE);
        mActivity.invalidateOptionsMenu();

        if (!mHomeEnabled) {
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    private void updatePasteDialog() {
        if (mPasteDialog == null) {
            return;
        }
        TextView view = (TextView) mPasteDialog.findViewById(R.id.buttonDefaultNeutral);
        if (view == null) {
            return;
        }
        String text = fromClipboard();
        if (isValidAddress(text)) {
            view.setVisibility(View.VISIBLE);
            view.setText(
                getResources().getString(R.string.string_paste_address, text.substring(0, 3)) + "...");
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private OnPrimaryClipChangedListener mClipboardHandler = new OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            updatePasteDialog();
        }
    };
}
