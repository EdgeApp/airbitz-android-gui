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
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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
    Handler mHandler;
    CameraHandler mCameraHandler;
    boolean mFlashOn = false;
    boolean mPreviewing = false;

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
            mCameraHandler.sendEmptyMessage(AUTO_FOCUS);
            mHandler.postDelayed(cameraFocusRunner, FOCUS_MILLIS);
        }
    };

    public QRCamera(Fragment frag, View cameraLayout) {
        mFragment = frag;
        mCameraLayout = cameraLayout;

        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        mCameraHandler = new CameraHandler(ht.getLooper());
        mHandler = new Handler();

        mPreviewFrame = (FrameLayout) mCameraLayout.findViewById(R.id.layout_camera_preview);
        mPreviewObscura = (FrameLayout) mCameraLayout.findViewById(R.id.layout_camera_obscura);
    }

    Runnable mCameraStartRunner = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null) {
                if (mPreviewing) {
                    stopCamera();
                }
                mHandler.postDelayed(mCameraStartRunner, 200);
            } else {
                startupTheCamera();
            }
        }
    };

    public void startScanning() {
        mCameraHandler.sendEmptyMessage(PREVIEW_ON);
    }

    public void stopScanning() {
        mCameraHandler.sendEmptyMessage(PREVIEW_OFF);
    }

    public void startCamera() {
        mHandler.post(mCameraStartRunner);
    }

    private int pickCamera() {
        // Get back camera unless there is none, then try the front camera - fix for Nexus 7
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.d(TAG, "No cameras!");
            return -1;
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

        if (cameraIndex >= numCameras) {
            cameraIndex = 0; //Front facing camera if no other camera index returned
        }
        return cameraIndex;
    }

    private void startupTheCamera() {
        if (mFragment.getActivity() == null) {
            return;
        }
        final int cameraIndex = pickCamera();
        if (cameraIndex < 0) {
            return;
        }
        mCameraHandler.post(new Runnable() {
            public void run() {
                try {
                    Log.d(TAG, "Opening Camera");
                    mCamera = Camera.open(cameraIndex);
                    finishOpenCamera();
                } catch (Exception e) {
                    Log.e(TAG, "Camera Does Not exist", e);
                    mHandler.postDelayed(mCameraStartRunner, 500);
                }
            }
        });
    }

    private void finishOpenCamera() {
        mPreview = new CameraSurfacePreview(mFragment.getActivity(), mCamera);
        mCameraHandler.sendEmptyMessage(SETUP);
        mPreviewing = true;
        mHandler.post(new Runnable() {
            public void run() {
                mPreviewFrame.addView(mPreview);
            }
        });
    }

    public void stopCamera() {
        if (mCamera != null) {
            mPreviewFrame.removeView(mPreview);
        }
        Log.d(TAG, "stopping camera");
        mCameraHandler.sendEmptyMessage(STOP_PREVIEW);
        mCameraHandler.sendEmptyMessage(RELEASE);
        mHandler.removeCallbacks(cameraFocusRunner);
        mPreviewing = false;
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
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1
                && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }
        return true;
    }

    public boolean isFlashOn() {
        return mFlashOn;
    }

    public void setFlashOn(boolean on) {
        if (on) {
            mFlashOn = true;
            mCameraHandler.sendEmptyMessage(FLASH_ON);
        } else {
            mFlashOn = false;
            mCameraHandler.sendEmptyMessage(FLASH_OFF);
        }
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

            String info = attemptDecodePicture(thumbnail);
            if (mOnScanResult != null) {
                mOnScanResult.onScanResult(info);
            }
        }
    }

    // Select a picture from the Gallery
    private void PickAPicture() {
        Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mFragment.startActivityForResult(in, RESULT_LOAD_IMAGE);
    }

    // Post Result on main thread
    private void receivedQrCode(final String info) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mOnScanResult != null) {
                    mOnScanResult.onScanResult(info);
                }
            }
        });
    }

    private static final int RELEASE = 1;
    private static final int UNLOCK = 2;
    private static final int LOCK = 3;
    private static final int SETUP = 4;
    private static final int START_PREVIEW = 5;
    private static final int STOP_PREVIEW = 6;
    private static final int AUTO_FOCUS = 7;
    private static final int CANCEL_AUTO_FOCUS = 8;
    private static final int FLASH_ON = 9;
    private static final int FLASH_OFF = 10;
    private static final int PREVIEW_ON = 11;
    private static final int PREVIEW_OFF = 12;

    private class CameraHandler extends Handler {
        CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            try {
                Log.d(TAG, "handleMessage");
                if (mCamera == null) {
                    return;
                }
                switch (msg.what) {
                    case RELEASE:
                        Log.d(TAG, "RELEASE");
                        mCamera.setPreviewCallback(null);
                        mCamera.release();
                        mCamera = null;
                        break;

                    case UNLOCK:
                        Log.d(TAG, "UNLOCK");

                        mCamera.unlock();
                        break;

                    case LOCK:
                        Log.d(TAG, "LOCK");

                        mCamera.lock();
                        break;

                    case SETUP:
                        Log.d(TAG, "SETUP");

                        setupCameraParams();
                        break;

                    case START_PREVIEW:
                        Log.d(TAG, "START_PREVIEW");

                        mCamera.startPreview();
                        return;

                    case STOP_PREVIEW:
                        Log.d(TAG, "STOP_PREVIEW");

                        mCamera.stopPreview();
                        break;

                    case AUTO_FOCUS:
                        Log.d(TAG, "AUTO_FOCUS");

                        mCamera.autoFocus(null);
                        break;

                    case CANCEL_AUTO_FOCUS:
                        Log.d(TAG, "CANCEL_AUTO_FOCUS");

                        mCamera.cancelAutoFocus();
                        break;

                    case FLASH_ON: {
                        Log.d(TAG, "FLASH_ON");
                        Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(parameters);
                        break;
                    }

                    case FLASH_OFF: {
                        Log.d(TAG, "FLASH_OFF");
                        Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(parameters);
                        break;
                    }

                    case PREVIEW_ON:
                        Log.d(TAG, "PREVIEW_ON");
                        mPreviewing = true;
                        break;

                    case PREVIEW_OFF:
                        Log.d(TAG, "PREVIEW_OFF");
                        mPreviewing = false;
                        break;

                    default:
                        throw new RuntimeException("Invalid CameraProxy message=" + msg.what);
                }
            } catch (RuntimeException e) {
                if (msg.what != RELEASE && mCamera != null) {
                    try {
                        mCamera.release();
                    } catch (Exception ex) {
                        Log.e(TAG, "Fail to release the camera.");
                    }
                    mCamera = null;
                }
                throw e;
            }
        }
    }

    private void setupCameraParams() {
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
    }

    private void obscuraUp() {
        mHandler.post(new Runnable() {
            public void run() {
                if (mPreviewObscura.getVisibility() != View.VISIBLE) {
                    mPreviewObscura.setAlpha(0f);
                    mPreviewObscura.animate().alpha(1f).setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mPreviewObscura.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mPreviewFrame.removeView(mPreview);
                            }
                        });
                }
            }
        });
    }

    private void obscuraDown() {
        mHandler.post(new Runnable() {
            public void run() {
                if (mPreviewObscura.getVisibility() != View.GONE) {
                    mPreviewObscura.animate().alpha(0f).setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mPreviewObscura.setVisibility(View.GONE);
                            }
                        });
                }
            }
        });
    }

    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (!mPreviewing) {
            return;
        }
        Log.d(TAG, "onPreviewFrame " + mPreviewing);
        obscuraDown();
        tryBytes(bytes, camera);
    }

    private void tryBytes(final byte[] bytes, final Camera camera) {
        if (mCamera == null || !mPreviewing) {
            return;
        }
        String info = attemptDecodeBytes(bytes, camera);
        if (info == null) {
            return;
        }
        mPreviewing = false;
        receivedQrCode(info);
    }

    public String attemptDecodeBytes(byte[] bytes, Camera camera) {
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
            return null;
        }
    }

    public String attemptDecodePicture(Bitmap thumbnail) {
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

}
