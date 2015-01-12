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
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
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
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created on 3/3/14.
 */
public class ImportFragment extends Fragment
        implements Camera.PreviewCallback,
        Camera.AutoFocusCallback,
        CoreAPI.OnWalletSweep
{
    public static String URI = "com.airbitz.importfragment.uri";

    private static int RESULT_LOAD_IMAGE = 876;
    private final int FOCUS_MILLIS = 2000;
    private final String TAG = getClass().getSimpleName();
    private EditText mToEdittext;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private ImageButton mGalleryButton;
    private HighlightOnPressSpinner mWalletSpinner;
    private HighlightOnPressButton mSubmitButton;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;
    private ImageButton mFlashButton;
    private NfcAdapter mNfcAdapter;
    private Camera mCamera;
    private CameraSurfacePreview mPreview;
    private int cameraIndex;
    private FrameLayout mPreviewFrame;
    private Camera.Parameters mCamParam;
    private int BACK_CAMERA_INDEX = 0;
    private boolean mFlashOn = false;
    private boolean mFocused = true;
    private CoreAPI mCoreAPI;
    private View mView;
    private List<Wallet> mWallets;//Actual wallets
    private Wallet mFromWallet;
    private Handler mHandler = new Handler();
    private NavigationActivity mActivity;
    private LinearLayout mImportLayout;
    private RelativeLayout mBusyLayout;
    private TextView mBusyText;

    private String mTweet, mToken, mMessage, mZeroMessage;
    String mSweptID;
    long mSweptAmount = -1;
    private String mSweptAddress;

    Runnable cameraFocusRunner = new Runnable() {
        @Override
        public void run() {
            mCamera.autoFocus(ImportFragment.this);
            mHandler.postDelayed(cameraFocusRunner, FOCUS_MILLIS);
            mFocused = false;
        }
    };
    Runnable sweepNotFoundRunner = new Runnable() {
        @Override
        public void run() {
            showBusyLayout(false);
            if(isVisible()) {
                ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.import_wallet_timeout_message));
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
        mWallets = mCoreAPI.getCoreActiveWallets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_import_wallet, container, false);
        } else {
            return mView;
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mPreviewFrame = (FrameLayout) mView.findViewById(R.id.layout_camera_preview);

        mImportLayout = (LinearLayout) mView.findViewById(R.id.fragment_import_layout);
        mBusyLayout = (RelativeLayout) mView.findViewById(R.id.fragment_import_busy_layout);
        mBusyText = (TextView) mView.findViewById(R.id.fragment_import_busy_text);
        mBusyText.setTypeface(NavigationActivity.latoBlackTypeFace);

        mToEdittext = (EditText) mView.findViewById(R.id.edittext_to);

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) mView.findViewById(R.id.textview_scan_qrcode);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.import_title);

        mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.IMPORT_WALLET), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        mSubmitButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_import_enter_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSubmit();
            }
        });

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mQRCodeTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_import_from_wallet_spinner);
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.SendFrom);
        mWalletSpinner.setAdapter(dataAdapter);

        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mFromWallet = mWallets.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mFlashButton = (ImageButton) mView.findViewById(R.id.button_flash);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        mGalleryButton = (ImageButton) mView.findViewById(R.id.button_gallery);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickAPicture();
            }
        });

        mToEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        mToEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!textView.getText().toString().isEmpty()) {
                        attemptSubmit();
                    }
                    return true;
                }
                return false;
            }
        });


        return mView;
    }

    private void showBusyLayout(boolean on) {
        if(on) {
            mImportLayout.setVisibility(View.GONE);
            mBusyLayout.setVisibility(View.VISIBLE);
            stopCamera();
        }
        else {
            mImportLayout.setVisibility(View.VISIBLE);
            mBusyLayout.setVisibility(View.GONE);
            startCamera();
        }
    }

    private void scanQRCodes() {
        mSubmitButton.setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.fragment_import_layout_camera).setVisibility(View.VISIBLE);
        cameraIndex = BACK_CAMERA_INDEX;

        try {
            mCamera = Camera.open(cameraIndex);
            mCamParam = mCamera.getParameters();
            Log.d(TAG, "Camera Does exist");
        } catch (Exception e) {
            Log.d(TAG, "Camera Does Not exist");
        }

        mPreview = new CameraSurfacePreview(getActivity(), mCamera);
        mPreviewFrame.removeView(mPreview);
        mPreviewFrame.addView(mPreview);

        mCamera.setPreviewCallback(ImportFragment.this);

        if (mCamera != null) {
            stopCamera();
        }
        startCamera();

        final NfcManager nfcManager = (NfcManager) mActivity.getSystemService(Context.NFC_SERVICE);
        mNfcAdapter = nfcManager.getDefaultAdapter();

        if (mNfcAdapter != null && mNfcAdapter.isEnabled() && SettingFragment.getNFCPref()) {
            mQRCodeTextView.setText(getString(R.string.send_scan_text_nfc));
        }
        else {
            mQRCodeTextView.setText(getString(R.string.send_scan_text));
        }
    }

    private void stopScanningQRCodes() {
        mSubmitButton.setVisibility(View.VISIBLE);
        mView.findViewById(R.id.fragment_import_layout_camera).setVisibility(View.GONE);
        if (mCamera != null) {
            stopCamera();
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
            mCamera.setPreviewCallback(ImportFragment.this);
            Camera.Parameters params = mCamera.getParameters();
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

    @Override
    public void onResume() {
        super.onResume();

        Bundle args = getArguments();

        if(args != null && args.getString(URI) != null && getHiddenBitsToken(args.getString(URI)) != null) {
            mToEdittext.setText(args.getString(URI));
            mToEdittext.clearFocus();
            mActivity.hideSoftKeyboard(mToEdittext);
            stopScanningQRCodes();
        }
        else {
            scanQRCodes();
        }

        mFromWallet = mWallets.get(0);
        mCoreAPI.setOnWalletSweepListener(this);

        checkCameraFlash();
    }

    private void checkCameraFlash() {
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mFlashButton.setVisibility(View.VISIBLE);
        }
        else {
            mFlashButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        showBusyLayout(false);
        stopCamera();
        mHandler.removeCallbacks(sweepNotFoundRunner);
        mCoreAPI.setOnWalletSweepListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCamera();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if(!mFocused)
            return;

        String result = AttemptDecodeBytes(bytes, camera);
        if (result != null) {
            Log.d(TAG, "HiddenBits found");
            stopCamera();
            mToEdittext.setText(result);
            attemptSubmit();
        }
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {
        mFocused = true;
    }

    // Select a picture from the Gallery
    private void PickAPicture() {
        mToEdittext.clearFocus();
        Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, RESULT_LOAD_IMAGE);
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

            String result = AttemptDecodePicture(thumbnail);
            if (result != null ) {
                stopCamera();
                mToEdittext.setText(result);
                attemptSubmit();
            }
        }
    }

    private String AttemptDecodeBytes(byte[] bytes, Camera camera) {
        Result rawResult = null;
        Reader reader = new QRCodeReader();
        int w = camera.getParameters().getPreviewSize().width;
        int h = camera.getParameters().getPreviewSize().height;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(bytes, w, h, 0, 0, w, h, false);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = reader.decode(bitmap);
            } catch (ReaderException re) {
                // nothing to do here
            } finally {
                reader.reset();
            }
        }
        if (rawResult != null) {
            return rawResult.getText();
        } else {
//            Log.d(TAG, "No QR code found");
        }
        return null;
    }

    private String AttemptDecodePicture(Bitmap thumbnail) {
        if (thumbnail == null) {
            Log.d(TAG, "No picture selected");
        } else {
            Log.d(TAG, "Picture selected");
            Result rawResult = null;
            Reader reader = new QRCodeReader();
            int w = thumbnail.getWidth();
            int h = thumbnail.getHeight();
            int[] pixels = new int[w * h];
            thumbnail.getPixels(pixels, 0, w, 0, 0, w, h);
            RGBLuminanceSource source = new RGBLuminanceSource(w, h, pixels);
            if (source != null) {
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    rawResult = reader.decode(bitmap);
                } catch (ReaderException re) {
                    // nothing to do here
                } finally {
                    reader.reset();
                }
            }
            if (rawResult != null) {
                return rawResult.getText();
            } else {
//            Log.d(TAG, "No QR code found");
            }
            return null;
        }
        return null;
    }

    private void attemptSubmit() {
        String uriString = mToEdittext.getText().toString().trim();
        String token = getHiddenBitsToken(uriString);

        String entry = token != null ? token : uriString;

        mBusyText.setText(String.format(getString(R.string.import_wallet_busy_text), entry));
        showBusyLayout(true);
        mSweptAddress = mCoreAPI.SweepKey(mFromWallet.getUUID(), entry);

        if(mSweptAddress != null && !mSweptAddress.isEmpty()) {
            mHandler.postDelayed(sweepNotFoundRunner, 30000);

            if(token != null) { // also issue hidden bits
                int hBitzIDLength = 4;
                if(mSweptAddress.length() >= hBitzIDLength) {
                    String lastFourChars = mSweptAddress.substring(mSweptAddress.length() - hBitzIDLength, mSweptAddress.length());
                    HiddenBitsApiTask task = new HiddenBitsApiTask();
                    task.execute(lastFourChars);
                }
                else {
                    Log.d(TAG, "HiddenBits token error");
                }
            }
        }
        else {
            showBusyLayout(false);
            mActivity.ShowFadingDialog(getString(R.string.import_wallet_private_key_invalid));
        }
    }

    // Returns null if not a HiddenBits token
    public static String getHiddenBitsToken(String uriIn)
    {
        final String HBITS_SCHEME = "hbits";
        if(uriIn == null)
            return null;

        Uri uri = Uri.parse(uriIn);
        String scheme = uri.getScheme();

        if(scheme != null && scheme.equalsIgnoreCase(HBITS_SCHEME)) {
            Log.d("ImportFragment", "Good HiddenBits URI");
            return uri.toString().substring(scheme.length()+3);
        }
        else {
            Log.d("ImportFragment", "HiddenBits failed for: "+uriIn);
            return null;
        }
    }

    public class HiddenBitsApiTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Getting HiddenBits API response");
        }

        @Override
        protected String doInBackground(String... params) {
            AirbitzAPI api = AirbitzAPI.getApi();
            return api.getHiddenBits(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) {
                return;
            }
            Log.d(TAG, "Got HiddenBits API response: " + result);

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                mTweet = jsonObject.getString("tweet");
                mToken = jsonObject.getString("token");
                mZeroMessage = jsonObject.getString("zero_message");
                mMessage = jsonObject.getString("message");

                // Check to see if both paths are done
                checkHiddenBitsAsyncData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            showBusyLayout(false);
        }
    }

    @Override
    public void OnWalletSweep(String txID, long amount) {
        Log.d(TAG, "OnWalletSweep called with ID:" + txID + " and satoshis:" + amount);

        showBusyLayout(false);

        mSweptID = txID;
        mSweptAmount = amount;

        String token = getHiddenBitsToken(mToEdittext.getText().toString());
        if(token != null) { // hidden bitz
            // Check to see if both paths are done
            checkHiddenBitsAsyncData();
            return;
        }

        // if a private address sweep
        mHandler.removeCallbacks(sweepNotFoundRunner);

        mActivity.showPrivateKeySweepTransaction(mSweptID, mFromWallet.getUUID(), mSweptAmount);

        // Clear out sweep info
        mSweptID = "";
        mSweptAmount = -1;
        mSweptAddress = "";
        mToEdittext.setText(mSweptAddress);
    }

    // This is only called for HiddenBits
    private void checkHiddenBitsAsyncData() {
        // both async paths are finished if both of these are not empty
        if (mSweptAmount != -1 && mTweet != null)
        {
            Log.d(TAG, "Both API and OnWalletSweep are finished");

            mHandler.removeCallbacks(sweepNotFoundRunner);
            showBusyLayout(false);

            mActivity.showHiddenBitsTransaction(mSweptID, mFromWallet.getUUID(), mSweptAmount,
                    mMessage, mZeroMessage, mTweet);

            mSweptAmount = -1;
        }
    }
}
