package com.airbitz.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.CameraSurfacePreview;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/**
 * Created on 3/3/14.
 */
public class ImportFragment extends Fragment implements Camera.PreviewCallback {
    private static int RESULT_LOAD_IMAGE = 876;
    private final String TAG = getClass().getSimpleName();
    private EditText mToEdittext;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Button mFromButton;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;
    private ImageButton mFlashButton;
    private ImageButton mGalleryButton;
    private Camera mCamera;
    private CameraSurfacePreview mPreview;
    private int cameraIndex;
    private FrameLayout preview;
    private Camera.Parameters mCamParam;
    private int BACK_CAMERA_INDEX = 0;
    private boolean mFlashOn = false;
    private CoreAPI mCoreAPI;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_import_wallet, container, false);
        } else {

            return mView;
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mFromButton = (Button) mView.findViewById(R.id.button_from);
        mToEdittext = (EditText) mView.findViewById(R.id.edittext_to);

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) mView.findViewById(R.id.textview_scan_qrcode);

        mBackButton = (ImageButton) mView.findViewById(R.id.layout_airbitz_header_button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.layout_airbitz_header_button_help);

        mFlashButton = (ImageButton) mView.findViewById(R.id.button_flash);
        mGalleryButton = (ImageButton) mView.findViewById(R.id.button_gallery);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mFromButton.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mQRCodeTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickAPicture();
            }
        });

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

        mToEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        preview = (FrameLayout) mView.findViewById(R.id.layout_camera_preview);
        cameraIndex = BACK_CAMERA_INDEX;

        try {
            mCamera = Camera.open(cameraIndex);
            mCamParam = mCamera.getParameters();
            Log.d(TAG, "Camera Does exist");
        } catch (Exception e) {
            Log.d(TAG, "Camera Does Not exist");
        }

        mPreview = new CameraSurfacePreview(getActivity(), mCamera);
        preview.removeView(mPreview);
        preview.addView(mPreview);

        mCamera.setPreviewCallback(ImportFragment.this);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.IMPORT_WALLET), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        return mView;
    }

    public void startCamera(int cameraIndex) {

        try {
            Log.d(TAG, "Opening Camera");
            mCamera = Camera.open(cameraIndex);
        } catch (Exception e) {
            Log.d(TAG, "Camera Does Not exist");
        }

        mPreview = new CameraSurfacePreview(getActivity(), mCamera);
        SurfaceView msPreview = new SurfaceView(getActivity().getApplicationContext());
        Log.d(TAG, "removeView");
        preview.removeView(mPreview);
        preview = (FrameLayout) getView().findViewById(R.id.layout_camera_preview);
        Log.d(TAG, "addView");
        preview.addView(mPreview);
        Log.d(TAG, "setPreviewCallback");
        mCamera.setPreviewCallback(ImportFragment.this);
        Log.d(TAG, "end setPreviewCallback");

    }

    public void stopCamera() {
        Log.d(TAG, "stopCamera");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            preview.removeView(mPreview);
            mCamera.release();
        }
        mCamera = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera != null) {
            stopCamera();
        }
        startCamera(BACK_CAMERA_INDEX);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCamera();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        CoreAPI.BitcoinURIInfo info = AttemptDecodeBytes(bytes, camera);
        if (info != null && info.getSzAddress() != null) {
            Fragment fragment = new WalletPasswordFragment();
            ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
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
            if (info != null && info.getSzAddress() != null) {
                Fragment fragment = new WalletPasswordFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
            }
        }
    }

    // Select a picture from the Gallery
    private void PickAPicture() {
        Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, RESULT_LOAD_IMAGE);
    }

    private CoreAPI.BitcoinURIInfo AttemptDecodeBytes(byte[] bytes, Camera camera) {
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
            return mCoreAPI.CheckURIResults(rawResult.getText());
        } else {
            Log.d(TAG, "No QR code found");
        }
        return null;
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
                return mCoreAPI.CheckURIResults(rawResult.getText());
            } else {
                Log.d(TAG, "No QR code found");
            }
        }
        return null;
    }


}
