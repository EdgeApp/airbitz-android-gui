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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.BleDevice;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.BluetoothListView;
import com.airbitz.objects.CameraSurfacePreview;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.List;


/**
 * Created on 2/22/14.
 */
public class SendFragment extends Fragment implements
        Camera.PreviewCallback,
        Camera.AutoFocusCallback,
        BluetoothListView.OnPeripheralSelected,
        BluetoothListView.OnBitcoinURIReceived,
        BluetoothListView.OnOneScanEnded {
    public static final String AMOUNT_SATOSHI = "com.airbitz.Sendfragment_AMOUNT_SATOSHI";
    public static final String LABEL = "com.airbitz.Sendfragment_LABEL";
    public static final String UUID = "com.airbitz.Sendfragment_UUID";
    public static final String IS_UUID = "com.airbitz.Sendfragment_IS_UUID";
    public static final String FROM_WALLET_UUID = "com.airbitz.Sendfragment_FROM_WALLET_UUID";
    private static int RESULT_LOAD_IMAGE = 678;
    private final int FOCUS_MILLIS = 2000;
    private final String TAG = getClass().getSimpleName();
    Runnable cameraDelayRunner = new Runnable() {
        @Override
        public void run() {
            startCamera();
        }
    };
    Runnable cameraFocusRunner = new Runnable() {
        @Override
        public void run() {
            if(mCamera != null) {
                mCamera.autoFocus(SendFragment.this);
            }
            mHandler.postDelayed(cameraFocusRunner, FOCUS_MILLIS);
            mFocused = false;
        }
    };
    private Handler mHandler;
    private EditText mToEdittext;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;
    private HighlightOnPressImageButton mHelpButton;
    private ImageButton mFlashButton;
    private ImageButton mGalleryButton;
    private ImageButton mBluetoothButton;
    private ListView mListingListView;
    private RelativeLayout mListviewContainer;
    private RelativeLayout mCameraLayout;
    private RelativeLayout mBluetoothLayout;
    private RelativeLayout mBluetoothScanningLayout;
    private BluetoothListView mBluetoothListView;
    private Camera mCamera;
    private CameraSurfacePreview mPreview;
    private FrameLayout mPreviewFrame;
    private View dummyFocus;
    private HighlightOnPressSpinner walletSpinner;
    private HighlightOnPressButton mScanQRButton;
    private List<Wallet> mWalletOtherList;//NAMES
    private List<Wallet> mWallets;//Actual wallets
    private Wallet mFromWallet;
    private List<Wallet> mCurrentListing;
    private WalletPickerAdapter listingAdapter;
    private int BACK_CAMERA_INDEX = 0;
    private boolean mFlashOn = false;
    private boolean mFocused = true;
    private boolean mForcedBluetoothScanning = false;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mCoreAPI == null)
            mCoreAPI = CoreAPI.getApi();

        mActivity = (NavigationActivity) getActivity();
        mWallets = mCoreAPI.getCoreActiveWallets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_send, container, false);

        mBluetoothLayout = (RelativeLayout) mView.findViewById(R.id.fragment_send_bluetooth_layout);
        mBluetoothScanningLayout = (RelativeLayout) mView.findViewById(R.id.fragment_send_bluetooth_scanning_layout);
        mCameraLayout = (RelativeLayout) mView.findViewById(R.id.fragment_send_layout_camera);

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);

        mFlashButton = (ImageButton) mView.findViewById(R.id.button_flash);
        mGalleryButton = (ImageButton) mView.findViewById(R.id.button_gallery);
        mBluetoothButton = (ImageButton) mView.findViewById(R.id.button_bluetooth);
        mScanQRButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_send_scanning_button);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.send_title);

        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) mView.findViewById(R.id.textview_scan_qrcode);

        mToEdittext = (EditText) mView.findViewById(R.id.edittext_to);

        mListviewContainer = (RelativeLayout) mView.findViewById(R.id.listview_container);
        mListingListView = (ListView) mView.findViewById(R.id.listing_listview);

        mCurrentListing = new ArrayList<Wallet>();
        listingAdapter = new WalletPickerAdapter(getActivity(), mCurrentListing, WalletPickerEnum.SendTo);
        mListingListView.setAdapter(listingAdapter);

        dummyFocus = mView.findViewById(R.id.dummy_focus);

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mQRCodeTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        walletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.from_wallet_spinner);
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.SendFrom);
        walletSpinner.setAdapter(dataAdapter);

        walletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mFromWallet = mWallets.get(i);
                updateWalletOtherList();
                goAutoCompleteWalletListing();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickAPicture();
            }
        });

        mBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mForcedBluetoothScanning = true;
                ViewBluetoothPeripherals(true);
            }
        });

        mScanQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mForcedBluetoothScanning = false;
                ViewBluetoothPeripherals(false);
            }
        });

        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCamera == null) {
                    return;
                }
                if (!mFlashOn) {
                    mFlashButton.setImageResource(R.drawable.btn_flash_on);
                    mFlashOn = true;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                } else {
                    mFlashButton.setImageResource(R.drawable.btn_flash_off);
                    mFlashOn = false;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                }
            }
        });

        mToEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkAndSendAddress(mToEdittext.getText().toString());
                    return true;
                }
                return false;
            }
        });

        mToEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                goAutoCompleteWalletListing();
            }
        });

        mToEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    goAutoCompleteWalletListing();
                } else {
                    mListviewContainer.setVisibility(View.GONE);
                }
            }
        });

        mToEdittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListviewContainer.setVisibility(View.VISIBLE);
            }
        });

        mToEdittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mListviewContainer.getVisibility() == View.VISIBLE) {
                        mListviewContainer.setVisibility(View.GONE);
                        return true;
                    }
                }
                return false;
            }
        });

        mListingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GotoSendConfirmation(mCurrentListing.get(i).getUUID(), 0, " ", true);
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.SEND), NavigationActivity.Tabs.SEND.ordinal());
            }
        });

        mPreviewFrame = (FrameLayout) mView.findViewById(R.id.layout_camera_preview);


        if (!mWallets.isEmpty()) {
            mFromWallet = mWallets.get(0);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            String uuid = bundle.getString(UUID); // From a wallet with this UUID
            if (uuid != null) {
                mFromWallet = mCoreAPI.getWalletFromUUID(uuid);
                if (mFromWallet != null) {
                    for (int i = 0; i < mWallets.size(); i++) {
                        if (mFromWallet.getUUID().equals(mWallets.get(i).getUUID()) && !mWallets.get(i).isArchived()) {
                            final int finalI = i;
                            walletSpinner.post(new Runnable() {
                                @Override
                                public void run() {
                                    walletSpinner.setSelection(finalI);
                                }
                            });
                            break;
                        }
                    }
                }
            } else if (bundle.getString(WalletsFragment.FROM_SOURCE).equals(NavigationActivity.URI_SOURCE)) {
                String uriData = bundle.getString(NavigationActivity.URI_DATA);
                bundle.putString(NavigationActivity.URI_DATA, ""); //to clear the URI_DATA after reading once
                if (!uriData.isEmpty()) {
                    CoreAPI.BitcoinURIInfo info = mCoreAPI.CheckURIResults(uriData);
                    if (info != null && info.getSzAddress() != null) {
                        GotoSendConfirmation(info.address, info.amountSatoshi, info.label, false);
                    }
                }
            }
        }
        // if BLE is supported on the device, enable
        if (mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (SettingFragment.getBLEPref()) {
                mBluetoothListView = new BluetoothListView(mActivity);
                mBluetoothLayout.addView(mBluetoothListView, 0);
                mBluetoothButton.setVisibility(View.VISIBLE);
            }
            else {
                // Bluetooth is not enabled - ask for enabling?
            }
        }


        updateWalletOtherList();

        return mView;
    }

    private void checkAndSendAddress(String strTo) {
        dummyFocus.requestFocus();

        if (strTo == null || strTo.isEmpty()) {
            ((NavigationActivity) getActivity()).hideSoftKeyboard(mToEdittext);
            mListviewContainer.setVisibility(View.GONE);
            return;
        }

        CoreAPI.BitcoinURIInfo results = mCoreAPI.CheckURIResults(strTo);
        if (results.address != null) {
            GotoSendConfirmation(results.address, results.amountSatoshi, results.label, false);
        } else {
            ((NavigationActivity) getActivity()).hideSoftKeyboard(mToEdittext);
            ((NavigationActivity) getActivity()).ShowOkMessageDialog(getResources().getString(R.string.fragment_send_failure_title), getString(R.string.fragment_send_confirmation_invalid_bitcoin_address));
        }
    }

    public void stopCamera() {
        Log.d(TAG, "stopCamera");
        if (mCamera != null) {
            mHandler.removeCallbacks(cameraFocusRunner);
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mPreviewFrame.removeView(mPreview);
            mCamera.release();
        }
        mCamera = null;
    }

    public void startCamera() {
        //Get back camera unless there is none, then try the front camera - fix for Nexus 7
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.d(TAG, "No cameras!");
            return;
        }

        int cameraIndex = 0;
        while (cameraIndex < numCameras) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraIndex, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                break;
            }
            cameraIndex++;
        }

        if (cameraIndex >= numCameras)
            cameraIndex = 0; //Front facing camera if no other camera index returned

        try {
            Log.d(TAG, "Opening Camera");
            mCamera = Camera.open(cameraIndex);
        } catch (Exception e) {
            Log.d(TAG, "Camera Does Not exist");
            return;
        }

        mPreview = new CameraSurfacePreview(getActivity(), mCamera);
        mPreviewFrame.removeView(mPreview);
        mPreviewFrame.addView(mPreview);
        if (mCamera != null) {
            mCamera.setPreviewCallback(SendFragment.this);
            Camera.Parameters params = mCamera.getParameters();
            if (params != null) {
                List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
                if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mHandler.post(cameraFocusRunner);
                }
                mCamera.setParameters(params);
            }
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (mListviewContainer.getVisibility() == View.GONE && mFocused) {
            CoreAPI.BitcoinURIInfo info = AttemptDecodeBytes(bytes, camera);
            if (info != null && info.address != null) {
                Log.d(TAG, "Bitcoin found");
                stopCamera();
                GotoSendConfirmation(info.address, info.amountSatoshi, info.label, false);
            } else if (info != null) {
                stopCamera();
                ShowMessageAndStartCameraDialog(getString(R.string.send_title), getString(R.string.fragment_send_send_bitcoin_invalid));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

            CoreAPI.BitcoinURIInfo info = AttemptDecodePicture(thumbnail);
            if (info != null && info.address != null) {
                Log.d(TAG, "Bitcoin found");
                stopCamera();
                GotoSendConfirmation(info.address, info.amountSatoshi, info.label, false);
            } else if (info != null) {
                stopCamera();
                ShowMessageAndStartCameraDialog(getString(R.string.send_title), getString(R.string.fragment_send_send_bitcoin_invalid));
            }
        }
    }

    // Select a picture from the Gallery
    private void PickAPicture() {
        mToEdittext.clearFocus();
        Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, RESULT_LOAD_IMAGE);
    }

    private CoreAPI.BitcoinURIInfo AttemptDecodeBytes(byte[] bytes, Camera camera) {
        Result rawResult = null;
        Reader reader = new QRCodeReader();
        int w = camera.getParameters().getPreviewSize().width;
        int h = camera.getParameters().getPreviewSize().height;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(bytes, w, h, 0, 0, w, h, false);
        if (source.getMatrix() != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            try {
                rawResult = reader.decode(bitmap);
            } catch (ReaderException re) {
                // nothing to do here
            } finally {
                reader.reset();
            }
        }
        if (rawResult != null) {
            Log.d(TAG, "QR code found " + rawResult.getText());
            return mCoreAPI.CheckURIResults(rawResult.getText());
        } else {
//            Log.d(TAG, "No QR code found");
            return null;
        }
    }

    private CoreAPI.BitcoinURIInfo AttemptDecodePicture(Bitmap thumbnail) {
        if (thumbnail == null) {
            Log.d(TAG, "No picture selected");
        } else {
            Log.d(TAG, "Picture selected");
            Result rawResult = null;
            Reader reader = new QRCodeReader();
            int w = thumbnail.getWidth();
            int h = thumbnail.getHeight();
            int maxOneDimension = 500;
            if(w * h > maxOneDimension * maxOneDimension) { //too big, reduce
                float bitmapRatio = (float)w / (float) h;
                if (bitmapRatio > 0) {
                    w = maxOneDimension;
                    h = (int) (w / bitmapRatio);
                } else {
                    h = maxOneDimension;
                    w = (int) (h * bitmapRatio);
                }
                thumbnail = Bitmap.createScaledBitmap(thumbnail, w, h, true);

            }
            int[] pixels = new int[w * h];
            thumbnail.getPixels(pixels, 0, w, 0, 0, w, h);
            RGBLuminanceSource source = new RGBLuminanceSource(w, h, pixels);
            if (source.getMatrix() != null) {
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    rawResult = reader.decode(bitmap);
                } catch (ReaderException re) {
                    re.printStackTrace();
                } finally {
                    reader.reset();
                }
            }
            if (rawResult != null) {
                Log.d(TAG, "QR code found " + rawResult.getText());
                return mCoreAPI.CheckURIResults(rawResult.getText());
            } else {
                Log.d(TAG, "Picture No QR code found");
            }
        }
        return null;
    }

    public void GotoSendConfirmation(String uuid, long amountSatoshi, String label, boolean isUUID) {
        if (mToEdittext != null) {
            mActivity.hideSoftKeyboard(mToEdittext);
        }
        Fragment fragment = new SendConfirmationFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_UUID, isUUID);
        bundle.putString(UUID, uuid);
        bundle.putLong(AMOUNT_SATOSHI, amountSatoshi);
        bundle.putString(LABEL, label);
        if (mFromWallet == null) {
            if (mCoreAPI == null) {
                mCoreAPI = CoreAPI.getApi();
            }
            mFromWallet = mCoreAPI.getCoreWallets(false).get(0);
        }
        bundle.putString(FROM_WALLET_UUID, mFromWallet.getUUID());
        fragment.setArguments(bundle);
        if (mActivity != null)
            mActivity.pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
    }

    @Override
    public void onResume() {
        super.onResume();
        mWallets = mCoreAPI.getCoreActiveWallets();

        dummyFocus.requestFocus();
        if (mHandler == null) {
            mHandler = new Handler();
        }

        if(mBluetoothButton.getVisibility() == View.VISIBLE) {
            ViewBluetoothPeripherals(true);
            mBluetoothListView.setOnOneScanEndedListener(this);
        }
        else {
            ViewBluetoothPeripherals(false);
        }

        if (walletSpinner != null && walletSpinner.getAdapter() != null) {
            ((WalletPickerAdapter) walletSpinner.getAdapter()).notifyDataSetChanged();
        }

        final NfcManager nfcManager = (NfcManager) mActivity.getSystemService(Context.NFC_SERVICE);
        NfcAdapter mNfcAdapter = nfcManager.getDefaultAdapter();

        if (mNfcAdapter != null && mNfcAdapter.isEnabled() && SettingFragment.getNFCPref()) {
            mQRCodeTextView.setText(getString(R.string.send_scan_text_nfc));
        }
        else {
            mQRCodeTextView.setText(getString(R.string.send_scan_text));
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mHandler.postDelayed(cameraDelayRunner, 500);
        } else {
            if (mCamera != null) {
                stopCamera();
            }
        }
    }

    public void updateWalletOtherList() {
        mWalletOtherList = new ArrayList<Wallet>();
        for (Wallet wallet : mWallets) {
            if (mFromWallet != null && mFromWallet.getUUID() != null && !wallet.getUUID().equals(mFromWallet.getUUID())) {
                mWalletOtherList.add(wallet);
            }
        }
    }

    public void goAutoCompleteWalletListing() {
        String text = mToEdittext.getText().toString();
        mCurrentListing.clear();
        if (text.isEmpty()) {
            for (Wallet w : mWalletOtherList) {
                if (!w.isArchived()) {
                    mCurrentListing.add(w);
                }
            }
        } else {
            for (Wallet w : mWalletOtherList) {
                if (!w.isArchived() && w.getName().toLowerCase().contains(text.toLowerCase())) {
                    mCurrentListing.add(w);
                }
            }
        }
        if (mCurrentListing.isEmpty() || !mToEdittext.hasFocus()) {
            mListviewContainer.setVisibility(View.GONE);
        } else {
            mListviewContainer.setVisibility(View.VISIBLE);
        }
        listingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler != null)
            mHandler.removeCallbacks(cameraDelayRunner);
        stopCamera();
        if(mBluetoothListView != null) {
            mBluetoothListView.close();
            mBluetoothListView.setOnOneScanEndedListener(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCamera();
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

    @Override
    public void onAutoFocus(boolean b, Camera camera) {
        mFocused = true;
    }

    //************** Bluetooth support
    // Show Bluetooth peripherals
    private void ViewBluetoothPeripherals(boolean bluetooth) {
        if(bluetooth) {
            mCameraLayout.setVisibility(View.GONE);
            mBluetoothLayout.setVisibility(View.VISIBLE);
            startBluetoothSearch();
        }
        else {
            stopBluetoothSearch();
            mCameraLayout.setVisibility(View.VISIBLE);
            mBluetoothLayout.setVisibility(View.GONE);
            mHandler.postDelayed(cameraDelayRunner, 500);
        }
    }

    // Start the Bluetooth search
    private void startBluetoothSearch() {
        if(mBluetoothListView != null && mBluetoothListView.isAvailable()) {
            mBluetoothScanningLayout.setVisibility(View.VISIBLE);
            mBluetoothListView.setOnPeripheralSelectedListener(this);
            mBluetoothListView.scanForBleDevices(true);
            stopCamera();
        }
    }

    // Stop the Bluetooth search
    private void stopBluetoothSearch() {
        if(mBluetoothListView != null && mBluetoothListView.isAvailable()) {
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
        mToEdittext.post(new Runnable() {
            @Override
            public void run() {
                mToEdittext.setText(bitcoinAddress);
                checkAndSendAddress(bitcoinAddress);
            }
        });
    }

    @Override
    public void onOneScanEnded(boolean hasDevices) {
        if(!hasDevices) {
            if(mForcedBluetoothScanning) {
                mBluetoothLayout.setVisibility(View.VISIBLE);
            }
            else {
                Log.d(TAG, "No bluetooth devices, switching to guns...");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewBluetoothPeripherals(false);
                    }
                });
            }
        }
        else {
//            mBluetoothLayout.setVisibility(View.GONE);
//            mBluetoothScanningLayout.setVisibility(View.GONE);
        }
    }
}
