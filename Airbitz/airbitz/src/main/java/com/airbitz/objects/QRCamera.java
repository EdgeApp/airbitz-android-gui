package com.airbitz.objects;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

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

public class QRCamera extends PictureCamera {
    final String TAG = getClass().getSimpleName();

    OnScanResult mOnScanResult;
    public interface OnScanResult {
        public void onScanResult(String info);
    }

    public void setOnScanResultListener(OnScanResult listener) {
        mOnScanResult = listener;
    }

    public QRCamera(Fragment frag, View cameraLayout) {
        super(frag, cameraLayout);
    }

    @Override
    protected void receivedPicture(Bitmap picture) {
        String info = attemptDecodePicture(picture);
        if (mOnScanResult != null) {
            mOnScanResult.onScanResult(info);
        }
    }

    @Override
    protected void handlePreviewFrame(final byte[] bytes, final Camera camera) {
        String info = attemptDecodeBytes(bytes, camera);
        if (info == null) {
            return;
        }
        mCameraManager.previewOff();
        receivedQrCode(info);
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
            AirbitzCore.logi("QR code found " + rawResult.getText());
            return rawResult.getText();
        } else {
            return null;
        }
    }

    public String attemptDecodePicture(Bitmap thumbnail) {
        if (thumbnail == null) {
            AirbitzCore.logi("No picture selected");
        } else {
            AirbitzCore.logi("Picture selected");
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
                AirbitzCore.logi("QR code found " + rawResult.getText());
                return rawResult.getText();
            } else {
                AirbitzCore.logi("Picture No QR code found");
            }
        }
        return null;
    }
}
