package com.airbitz.objects;

import android.hardware.Camera;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraManager {
    final String TAG = getClass().getSimpleName();

    private static CameraManager sCameraManager = new CameraManager();

    private ConditionVariable mSig = new ConditionVariable();
    private CameraHandler mCameraHandler;
    private Camera mCamera;
    private Camera.PictureCallback mJpegCallback;
    private Camera.ShutterCallback mShutterCallback;
    boolean mPreviewing = false;
    boolean mFlashOn = false;

    public static CameraManager instance() {
        return sCameraManager;
    }

    private CameraManager() {
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        mCameraHandler = new CameraHandler(ht.getLooper());
    }

    public void autoFocus() {
        mCameraHandler.sendEmptyMessage(AUTO_FOCUS);
    }

    public synchronized void previewOn() {
        mPreviewing = true;
    }

    public synchronized void previewOff() {
        mPreviewing = false;
    }

    public void startPreview() {
        mCameraHandler.sendEmptyMessage(START_PREVIEW);
    }

    public void stopPreview() {
        mCameraHandler.sendEmptyMessage(STOP_PREVIEW);
    }

    public void release() {
        mCameraHandler.sendEmptyMessage(RELEASE);
    }

    public void postToCameraThread(Runnable runnable) {
        mCameraHandler.post(runnable);
    }

    private Camera.Parameters mParameters;
    public Camera.Parameters getParameters() {
        mSig.close();
        mCameraHandler.sendEmptyMessage(GET_PARAMETERS);
        mSig.block();
        Camera.Parameters parameters = mParameters;
        mParameters = null;
        return parameters;
    }

    public void setParameters(Camera.Parameters params) {
        mSig.close();
        mCameraHandler.obtainMessage(SET_PARAMETERS, params).sendToTarget();
        mSig.block();
    }

    public void setPreviewDisplay(SurfaceHolder holder) {
        mSig.close();
        mCameraHandler.obtainMessage(SET_PREVIEW_DISPLAY, holder).sendToTarget();
        mSig.block();
    }

    public void takePicture() {
        mPreviewing = false;
        mCameraHandler.sendEmptyMessage(TAKE_PICTURE);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    public interface Callback {
        public void success(Camera camera);
        public void error(Exception e);
    }

    public void startCamera(final int cameraIndex, final Callback callback) {
        mCameraHandler.post(new Runnable() {
            public void run() {
                try {
                    mCamera = Camera.open(cameraIndex);
                    if (null != callback) {
                        callback.success(mCamera);
                    }
                } catch (Exception e) {
                    if (null != callback) {
                        callback.error(e);
                    }
                }
            }
        });
    }

    public boolean isPreviewing() {
        return mPreviewing;
    }

    public boolean isFlashOn() {
        return mFlashOn;
    }

    public void flash(boolean on) {
        if (on) {
            flashOn();
        } else {
            flashOff();
        }
    }

    public void flashOn() {
        if (hasCameraFlash()) {
            mCameraHandler.sendEmptyMessage(FLASH_ON);
        }
    }

    public void flashOff() {
        if (hasCameraFlash()) {
            mCameraHandler.sendEmptyMessage(FLASH_OFF);
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
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1
                && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }
        return true;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setPictureCallback(Camera.PictureCallback callback) {
        mJpegCallback = callback;
    }

    public void setShutterCallback(Camera.ShutterCallback callback) {
        mShutterCallback = callback;
    }

    private static final int RELEASE = 1;
    private static final int UNLOCK = 2;
    private static final int LOCK = 3;
    private static final int START_PREVIEW = 5;
    private static final int STOP_PREVIEW = 6;
    private static final int AUTO_FOCUS = 7;
    private static final int CANCEL_AUTO_FOCUS = 8;
    private static final int FLASH_ON = 9;
    private static final int FLASH_OFF = 10;
    private static final int PREVIEW_ON = 11;
    private static final int PREVIEW_OFF = 12;
    private static final int GET_PARAMETERS = 13;
    private static final int SET_PARAMETERS = 14;
    private static final int SET_PREVIEW_DISPLAY = 15;
    private static final int TAKE_PICTURE = 16;

    private class CameraHandler extends Handler {
        CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            try {
                Log.d(TAG, "handleMessage");
                if (mCamera == null) {
                    mSig.open();
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
                        mFlashOn = true;
                        break;
                    }

                    case FLASH_OFF: {
                        Log.d(TAG, "FLASH_OFF");
                        Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(parameters);
                        mFlashOn = false;
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

                    case GET_PARAMETERS:
                        Log.d(TAG, "GET_PARAMETERS");
                        mParameters = mCamera.getParameters();
                        mSig.open();
                        break;

                    case SET_PARAMETERS:
                        Log.d(TAG, "SET_PARAMETERS");
                        mCamera.setParameters((Camera.Parameters) msg.obj);
                        mSig.open();
                        break;

                    case SET_PREVIEW_DISPLAY:
                        try {
                            mCamera.setPreviewDisplay((SurfaceHolder) msg.obj);
                        } catch(IOException e) {
                            throw new RuntimeException(e);
                        }
                        mSig.open();

                    case TAKE_PICTURE:
                        Log.d(TAG, "TAKE_PICTURE");
                        mCamera.takePicture(mShutterCallback, null, null, mJpegCallback);
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
            }
        }
    }
}
