package com.airbitz.objects;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import co.airbitz.core.AirbitzCore;
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

public class PictureCamera implements Camera.PreviewCallback {

    final String TAG = getClass().getSimpleName();

    public static final int RESULT_LOAD_IMAGE = 678;
    final int FOCUS_MILLIS = 2000;

    CameraManager mCameraManager;
    Fragment mFragment;
    CameraSurfacePreview mPreview;
    FrameLayout mPreviewFrame, mPreviewObscura;
    View mCameraLayout;
    Handler mHandler;

    Runnable cameraFocusRunner = new Runnable() {
        @Override
        public void run() {
            mCameraManager.autoFocus();
            mHandler.postDelayed(cameraFocusRunner, FOCUS_MILLIS);
        }
    };

    public PictureCamera(Fragment frag, View cameraLayout) {
        mFragment = frag;
        mCameraLayout = cameraLayout;
        mCameraManager = CameraManager.instance();

        mHandler = new Handler();
        mPreviewFrame = (FrameLayout) mCameraLayout.findViewById(R.id.layout_camera_preview);
        mPreviewObscura = (FrameLayout) mCameraLayout.findViewById(R.id.layout_camera_obscura);
    }

    Runnable mCameraStartRunner = new Runnable() {
        @Override
        public void run() {
            if (mCameraManager.getCamera() != null) {
                if (mCameraManager.isPreviewing()) {
                    stopCamera();
                }
                mHandler.postDelayed(mCameraStartRunner, 200);
            } else {
                startupTheCamera();
            }
        }
    };

    public void startScanning() {
        mCameraManager.previewOn();
    }

    public void stopScanning() {
        mCameraManager.previewOff();
    }

    public void startCamera() {
        mHandler.post(mCameraStartRunner);
    }

    private int pickCamera() {
        // Get back camera unless there is none, then try the front camera - fix for Nexus 7
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            AirbitzCore.logi("No cameras!");
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
        mCameraManager.startCamera(cameraIndex, new CameraManager.Callback() {
            public void success(final Camera camera) {
                mHandler.post(new Runnable() {
                    public void run() {
                        finishOpenCamera();
                    }
                });
            }
            public void error(Exception e) {
                Log.e(TAG, "Camera Does Not exist", e);
                mHandler.postDelayed(mCameraStartRunner, 500);
            }
        });
    }

    private void finishOpenCamera() {
        if (mFragment.getActivity() == null || null == mCameraManager.getCamera()) {
            return;
        }
        mPreview = new CameraSurfacePreview(mFragment.getActivity());
        mCameraManager.postToCameraThread(new Runnable() {
            public void run() {
                setupCameraParams();
            }
        });
        mCameraManager.previewOn();
        mHandler.post(new Runnable() {
            public void run() {
                mPreviewFrame.addView(mPreview);
            }
        });
    }

    public void stopCamera() {
        if (mCameraManager.getCamera() != null) {
            mPreviewFrame.removeView(mPreview);
        }
        AirbitzCore.logi("stopping camera");
        mCameraManager.previewOff();
        mCameraManager.stopPreview();
        mCameraManager.release();
    }

    public boolean isFlashOn() {
        return mCameraManager.isFlashOn();
    }

    public void setFlashOn(boolean on) {
        mCameraManager.flash(on);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Bitmap picture = retrievePicture(data);
            receivedPicture(picture);
        }
    }

    public Bitmap retrievePicture(Intent data) {
        return retrievePicture(data, mFragment.getActivity());
    }

    public static Bitmap retrievePicture(Intent data, Context context) {
        return retrievePicture(data.getData(), context);
    }

    public static Bitmap retrievePicture(Uri image, Context context) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(image, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return (BitmapFactory.decodeFile(picturePath));
    }

    private void setupCameraParams() {
        if (mCameraManager.getCamera() != null) {
            Camera camera = mCameraManager.getCamera();
            camera.setPreviewCallback(this);
            Camera.Parameters params = camera.getParameters();
            if (params != null) {
                List<String> supportedFocusModes = camera.getParameters().getSupportedFocusModes();
                if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) &&
                        !supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mHandler.post(cameraFocusRunner);
                }
                camera.setParameters(params);
            }
        }
    }

    private void obscuraUp() {
        obscuraUp(200);
    }

    private void obscuraUp(final int duration) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mPreviewObscura.getVisibility() != View.VISIBLE) {
                    mPreviewObscura.setAlpha(0f);
                    mPreviewObscura.animate().alpha(1f).setDuration(duration)
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
        obscuraDown(200);
    }

    private void obscuraDown(final int duration) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mPreviewObscura.getVisibility() != View.GONE) {
                    mPreviewObscura.animate().alpha(0f).setDuration(duration)
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
        if (!mCameraManager.isPreviewing()) {
            return;
        }
        obscuraDown();
        if (mCameraManager.getCamera() == null || !mCameraManager.isPreviewing()) {
            return;
        }
        handlePreviewFrame(bytes, camera);
    }

    private void shutterAnimation() {
        mHandler.post(new Runnable() {
            public void run() {
                mPreviewObscura.setVisibility(View.VISIBLE);
                obscuraDown();
            }
        });
    }

    public void takePicture(Camera.PictureCallback callback) {
        mCameraManager.setPictureCallback(callback);
        mCameraManager.setShutterCallback(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                shutterAnimation();
            }
        });
        mCameraManager.takePicture();
    }

    protected void receivedPicture(Bitmap picture) {
    }

    protected void handlePreviewFrame(final byte[] bytes, final Camera camera) {
    }
}
