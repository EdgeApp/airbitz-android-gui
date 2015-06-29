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

package com.airbitz.fragments.send;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
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

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletChoiceAdapter;
import com.airbitz.adapters.WalletOtherAdapter;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.settings.SettingFragment;
import com.airbitz.fragments.wallet.WalletsFragment;
import com.airbitz.models.BleDevice;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.BleUtil;
import com.airbitz.objects.BluetoothListView;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.objects.QRCamera;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;


/**
 * Created on 2/22/14.
 */
public class SendFragment extends WalletBaseFragment implements
        BluetoothListView.OnPeripheralSelected,
        CoreAPI.OnWalletLoaded,
        BluetoothListView.OnBitcoinURIReceived,
        QRCamera.OnScanResult
{
    private final String TAG = getClass().getSimpleName();

    private final String FIRST_USAGE_COUNT = "com.airbitz.fragments.send.firstusagecount";
    private final String FIRST_BLE_USAGE_COUNT = "com.airbitz.fragments.send.firstusageblecount";

    public static final String AMOUNT_SATOSHI = "com.airbitz.Sendfragment_AMOUNT_SATOSHI";
    public static final String AMOUNT_FIAT = "com.airbitz.Sendfragment_AMOUNT_FIAT";
    public static final String LABEL = "com.airbitz.Sendfragment_LABEL";
    public static final String CATEGORY = "com.airbitz.Sendfragment_CATEGORY";
    public static final String RETURN_URL = "com.airbitz.Sendfragment_RETURN_URL";
    public static final String NOTES = "com.airbitz.Sendfragment_NOTES";
    public static final String LOCKED = "com.airbitz.Sendfragment_LOCKED";
    public static final String UUID = "com.airbitz.Sendfragment_UUID";
    public static final String IS_UUID = "com.airbitz.Sendfragment_IS_UUID";
    public static final String FROM_WALLET_UUID = "com.airbitz.Sendfragment_FROM_WALLET_UUID";

    private Handler mHandler;
    private boolean hasCheckedFirstUsage;
    private Button mTransferButton, mAddressButton, mFlashButton, mGalleryButton;
    private ListView mOtherWalletsListView;
    private RelativeLayout mListviewContainer;
    private RelativeLayout mCameraLayout;
    private RelativeLayout mBluetoothLayout;
    private BluetoothListView mBluetoothListView;
    private List<Wallet> mOtherWalletsList;//NAMES
    private List<Wallet> mWallets;//Actual wallets
    private Wallet mFromWallet;
    private String mReturnURL;
    private WalletOtherAdapter mOtherWalletsAdapter;
    private boolean mForcedBluetoothScanning = false;
    private View mView;
    QRCamera mQRCamera;
    private CoreAPI mCoreApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCoreApi = CoreAPI.getApi();

        mView = inflater.inflate(R.layout.fragment_send, container, false);

        mBluetoothLayout = (RelativeLayout) mView.findViewById(R.id.fragment_send_bluetooth_layout);
        mCameraLayout = (RelativeLayout) mView.findViewById(R.id.fragment_send_layout_camera);
        mQRCamera = new QRCamera(this, mCameraLayout);

//        final RelativeLayout header = (RelativeLayout) mView.findViewById(R.id.fragment_send_header);
//        mHelpButton = (HighlightOnPressButton) header.findViewById(R.id.layout_wallet_select_header_right);
//        mHelpButton.setVisibility(View.VISIBLE);
//        mHelpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mActivity.pushFragment(new HelpFragment(HelpFragment.SEND), NavigationActivity.Tabs.SEND.ordinal());
//            }
//        });

//        pickWalletSpinner = (HighlightOnPressSpinner) header.findViewById(R.id.layout_wallet_select_header_spinner);
//        pickWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                mFromWallet = mWallets.get(i);
//                AirbitzApplication.setCurrentWallet(mFromWallet.getUUID());
//                updateWalletOtherList();
////                goAutoCompleteWalletListing();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });

        final SegmentedGroup buttons = (SegmentedGroup) mView.findViewById(R.id.request_bottom_buttons);
        mFlashButton = (Button) buttons.findViewById(R.id.fragment_send_button_flash);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.clearCheck();
                mQRCamera.setFlashOn(!mQRCamera.isFlashOn());
            }
        });

        mGalleryButton = (Button) buttons.findViewById(R.id.fragment_send_button_photos);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.clearCheck();
                Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in, QRCamera.RESULT_LOAD_IMAGE);
            }
        });

        mTransferButton = (Button) buttons.findViewById(R.id.fragment_send_button_transfer);
        mTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.clearCheck();
                showOtherWallets();
            }
        });

        mAddressButton = (Button) buttons.findViewById(R.id.fragment_send_button_address);
        mAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.clearCheck();
                showAddressDialog();
            }
        });

        mOtherWalletsList = new ArrayList<Wallet>();
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
                CoreAPI.SpendTarget target = mCoreApi.getNewSpendTarget();
                target.newTransfer(mOtherWalletsList.get(i-1).getUUID());
                GotoSendConfirmation(target);
            }
        });

        // if BLE is supported on the device, enable
        if (mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (SettingFragment.getBLEPref()) {
                mBluetoothListView = new BluetoothListView(mActivity);
                mBluetoothLayout.addView(mBluetoothListView, 0);
            }
            else {
                // Bluetooth is not enabled - ask for enabling?
            }
        }

        return mView;
    }

    // delegated from the containing fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == QRCamera.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

            String info = mQRCamera.AttemptDecodePicture(thumbnail);
            stopCamera();
            onScanResult(info);
        }
    }

    private void checkAndSendAddress(String strTo) {
        newSpend(strTo);
    }

    public void stopCamera() {
        Log.d(TAG, "stopCamera");
        mQRCamera.stopCamera();
    }

    public void startCamera() {
        mQRCamera.startCamera();
        checkFirstUsage();
    }

    private void checkFirstUsage() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        int count = prefs.getInt(FIRST_USAGE_COUNT, 1);
        if(count <= 2) {
            count++;
            mActivity.ShowFadingDialog(getString(R.string.fragment_send_first_usage), 5000);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(FIRST_USAGE_COUNT, count);
            editor.apply();
        }
    }

    public void GotoSendConfirmation(CoreAPI.SpendTarget target) {
        SendConfirmationFragment fragment = new SendConfirmationFragment();
        fragment.setSpendTarget(target);
        Bundle bundle = new Bundle();
        if (mFromWallet == null) {
            mFromWallet = mWallets.get(0);
            AirbitzApplication.setCurrentWallet(mFromWallet.getUUID());
        }
        bundle.putString(FROM_WALLET_UUID, mFromWallet.getUUID());
        fragment.setArguments(bundle);
        if (mActivity != null)
            mActivity.pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
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

        mCoreApi.setOnWalletLoadedListener(this);

        hasCheckedFirstUsage = false;
        if (mHandler == null) {
            mHandler = new Handler();
        }

        startCamera();

        final NfcManager nfcManager = (NfcManager) mActivity.getSystemService(Context.NFC_SERVICE);
        NfcAdapter mNfcAdapter = nfcManager.getDefaultAdapter();

        if (mNfcAdapter != null && mNfcAdapter.isEnabled() && SettingFragment.getNFCPref()) {
//            mQRCodeTextView.setText(getString(R.string.send_scan_text_nfc));
        }
        else {
//            mQRCodeTextView.setText(getString(R.string.send_scan_text));
        }

        mQRCamera.setOnScanResultListener(this);

        checkFirstBLEUsage();
        startBluetoothSearch();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mQRCamera.startCamera();
        } else {
            if (mQRCamera != null) {
                mQRCamera.stopCamera();
            }
        }
    }

    public void updateWalletOtherList() {
        mOtherWalletsList.clear();
        for (Wallet wallet : mWallets) {
            if (mFromWallet != null && mFromWallet.getUUID() != null && !wallet.getUUID().equals(mFromWallet.getUUID())) {
                mOtherWalletsList.add(wallet);
            }
        }
        mOtherWalletsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
        stopBluetoothSearch();
        if(mBluetoothListView != null) {
            mBluetoothListView.close();
        }
        mQRCamera.setOnScanResultListener(null);
        mCoreApi.setOnWalletLoadedListener(null);
        hasCheckedFirstUsage = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isMenuExpanded()) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }
        inflater.inflate(R.menu.menu_standard, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                mActivity.pushFragment(
                        new HelpFragment(HelpFragment.SEND),
                        NavigationActivity.Tabs.SEND.ordinal());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void ShowMessageAndStartCameraDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startCamera();
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    //************** Bluetooth support

    private void checkFirstBLEUsage() {
        if(hasCheckedFirstUsage) {
            return;
        }
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        int count = prefs.getInt(FIRST_BLE_USAGE_COUNT, 1);
        if(count <= 2) {
            count++;
            mActivity.ShowFadingDialog(getString(R.string.fragment_send_first_usage_ble), 5000);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(FIRST_BLE_USAGE_COUNT, count);
            editor.apply();
        }
        hasCheckedFirstUsage = true;
    }

    // Start the Bluetooth search
    private void startBluetoothSearch() {
        if(mBluetoothListView != null && BleUtil.isBleAvailable(mActivity)) {
            mBluetoothListView.setOnPeripheralSelectedListener(this);
            mBluetoothListView.scanForBleDevices(true);
        }
    }

    // Stop the Bluetooth search
    private void stopBluetoothSearch() {
        if(mBluetoothListView != null && BleUtil.isBleAvailable(mActivity)) {
            mBluetoothListView.scanForBleDevices(false);
            mBluetoothListView.close();
        }
    }

    @Override
    public void onPeripheralSelected(BleDevice device) {
        stopBluetoothSearch();
        mBluetoothListView.setOnPeripheralSelectedListener(null);
        mBluetoothListView.setOnBitcoinURIReceivedListener(this);
        mBluetoothListView.connectGatt(device);
    }

    @Override
    public void onBitcoinURIReceived(final String bitcoinAddress) {
        if(mBluetoothListView != null) {
            mBluetoothListView.setOnBitcoinURIReceivedListener(null);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                checkAndSendAddress(bitcoinAddress);
            }
        });
    }

    @Override
    public void onScanResult(String result) {
        Log.d(TAG, "checking result = " + result);
        if (result != null) {
            newSpend(result);
        } else {
            ShowMessageAndStartCameraDialog(getString(R.string.send_title), getString(R.string.fragment_send_send_bitcoin_unscannable));
        }
    }

    @Override
    public void onWalletsLoaded() {
        super.onWalletsLoaded();
        mWallets = mCoreApi.getCoreActiveWallets();

        mFromWallet = mWallet;
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(WalletsFragment.FROM_SOURCE, "").equals(NavigationActivity.URI_SOURCE)) {
                String uriData = bundle.getString(NavigationActivity.URI_DATA);
                bundle.putString(NavigationActivity.URI_DATA, ""); //to clear the URI_DATA after reading once
                if (!uriData.isEmpty()) {
                    newSpend(uriData);
                }
            }
        }
        updateWalletOtherList();
    }

    private void newSpend(String text) {
        new NewSpendTask().execute(text);
    }

    public class NewSpendTask extends AsyncTask<String, Void, Boolean> {
        CoreAPI.SpendTarget target;

        NewSpendTask() {
            target = mCoreApi.getNewSpendTarget();
        }

        @Override
        protected Boolean doInBackground(String... text) {
            return target.newSpend(text[0]);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                GotoSendConfirmation(target);
            } else {
                ((NavigationActivity) getActivity()).ShowOkMessageDialog(
                        getResources().getString(R.string.fragment_send_failure_title),
                        getString(R.string.fragment_send_confirmation_invalid_bitcoin_address));
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    public void showAddressDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.alert_address_form, null);
        final EditText editText = (EditText) view.findViewById(R.id.address);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setTitle(getResources().getString(R.string.fragment_send_address_dialog_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_done),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                checkAndSendAddress(editText.getText().toString());
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(Html.fromHtml("<b>"+getResources().getString(R.string.string_cancel)+"</b>"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        // this changes the colors of the system's UI buttons we're using
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });
        dialog.show();
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
        if (mOtherWalletsListView.getVisibility() == View.GONE) {
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
        mOtherWalletsListView.setVisibility(View.GONE);
        mActivity.invalidateOptionsMenu();
        mExpanded = false;

        if (!mHomeEnabled) {
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }
}
