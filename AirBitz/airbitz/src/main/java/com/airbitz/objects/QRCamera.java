package com.airbitz.objects;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.List;

/**
 * Created by tom on 2/6/15.
 */
public class QRCamera implements
        Camera.PreviewCallback {
    final String TAG = getClass().getSimpleName();

    public static final int RESULT_LOAD_IMAGE = 678;
    final int FOCUS_MILLIS = 2000;
    Fragment mFragment;
    Camera mCamera;
    CameraSurfacePreview mPreview;
    FrameLayout mPreviewFrame, mPreviewObscura;
    View mCameraLayout;
    ImageButton mFlashButton, mGalleryButton, mBluetoothButton;
    Handler mHandler = new Handler();
    boolean mFlashOn = false;
    boolean mPreviewing = false;
    CoreAPI mCoreAPI;

    //************** Callback for notification of a QR code scan result
    OnScanResult mOnScanResult;
    public interface OnScanResult {
        public void onScanResult(String info);
    }
    public void setOnScanResultListener(OnScanResult listener) {
        mOnScanResult = listener;
    }

    Runnable cameraFocusRunner = new Runnable() {
        @Override
        public void run() {
            if(mCamera != null) {
                mCamera.autoFocus(null);
            }
            mHandler.postDelayed(cameraFocusRunner, FOCUS_MILLIS);
        }
    };

    public QRCamera(Fragment frag, View cameraLayout) {
        mFragment = frag;
        mCoreAPI = CoreAPI.getApi();
        mCameraLayout = cameraLayout;

        mPreviewFrame = (FrameLayout) mCameraLayout.findViewById(R.id.layout_camera_preview);
        mPreviewObscura = (FrameLayout) mCameraLayout.findViewById(R.id.layout_camera_obscura);

        mFlashButton = (ImageButton) mCameraLayout.findViewById(R.id.button_flash);
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

        mGalleryButton = (ImageButton) mCameraLayout.findViewById(R.id.button_gallery);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickAPicture();
            }
        });

        mBluetoothButton = (ImageButton) mCameraLayout.findViewById(R.id.button_bluetooth);
    }

    public ImageButton getBluetoothButton() {
        return mBluetoothButton;
    }

    Runnable cameraStartRunner = new Runnable() {
        @Override
        public void run() {
            if(mCamera != null) {
                if(mPreviewing) {
                    stopCamera();
                }
                mHandler.postDelayed(cameraStartRunner, 200);
            }
            else {
                startupTheCamera();
            }
        }
    };

    public void startCamera() {
        mHandler.post(cameraStartRunner);
    }

    private void startupTheCamera() {
        if(mFragment.getActivity() == null) {
            return;
        }

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
            mHandler.postDelayed(cameraStartRunner, 500);
            return;
        }

        mPreview = new CameraSurfacePreview(mFragment.getActivity(), mCamera);
        mPreviewFrame.addView(mPreview);
        if (mCamera != null) {
            mCamera.setPreviewCallback(this);
            Camera.Parameters params = mCamera.getParameters();
            if (params != null) {
                List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
                if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) &&
                        !supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mHandler.post(cameraFocusRunner);
                }
                mCamera.setParameters(params);
            }
        }
        checkCameraFlash();
        mPreviewing = true;
    }

    Thread mStopThread;
    public void stopCamera() {
        if(mCamera != null && mPreviewing) {
            mPreviewing = false;
            Log.d(TAG, "stopping camera");
            mCamera.setPreviewCallback(null);
            mHandler.removeCallbacks(cameraFocusRunner);
            mPreviewFrame.removeView(mPreview);
            if(mStopThread == null) {
                mStopThread = new Thread(new Runnable() {
                    public void run() {
                        mCamera.stopPreview();
                        mCamera.release();
                        Log.d(TAG, "camera released");
                        mCamera = null;
                        mStopThread = null;
                    }
                });
                mStopThread.start();
            }
        }
    }

    private void checkCameraFlash() {
        if(hasCameraFlash()) {
            mFlashButton.setVisibility(View.VISIBLE);
        }
        else {
            mFlashButton.setVisibility(View.GONE);
        }
    }

    public boolean hasCameraFlash() {
        if (mCamera == null) {
            return false;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getFlashMode() == null) {
            return false;
        }
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }
        return true;
    }

    // delegated from the containing fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = mFragment.getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

            String info = AttemptDecodePicture(thumbnail);
            stopCamera();
            if(mOnScanResult != null) {
                mOnScanResult.onScanResult(info);
            }
        }
    }

    // Select a picture from the Gallery
    private void PickAPicture() {
        Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mFragment.startActivityForResult(in, RESULT_LOAD_IMAGE);
    }

    private String AttemptDecodeBytes(byte[] bytes, Camera camera) {
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
            return rawResult.getText();
        } else {
//            Log.d(TAG, "No QR code found");
            return null;
        }
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
                return rawResult.getText();
            } else {
                Log.d(TAG, "Picture No QR code found");
            }
        }
        return null;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if(!mPreviewing) {
            return;
        }
        if(mPreviewObscura.getVisibility() != View.GONE) {
            mPreviewObscura.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mPreviewObscura.setVisibility(View.GONE);
                        }
                    });
        }
        String info = AttemptDecodeBytes(bytes, camera);
        if (info != null) {
            stopCamera();
            if(mOnScanResult != null) {
                mOnScanResult.onScanResult(info);
            }
        }
    }
}
