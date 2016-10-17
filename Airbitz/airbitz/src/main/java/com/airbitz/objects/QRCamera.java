package com.airbitz.objects;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

import co.airbitz.core.AirbitzCore;
import com.airbitz.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.Code128Reader;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
        int w = camera.getParameters().getPreviewSize().width;
        int h = camera.getParameters().getPreviewSize().height;
        YuvImage yuvimage = new YuvImage(bytes, ImageFormat.NV21, w, h, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, w, h), 80, baos);
        byte[] data = baos.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Result rawResult = tryScan(bitmap);
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
            Result rawResult = tryScan(thumbnail);
            if (rawResult != null) {
                AirbitzCore.logi("QR code found " + rawResult.getText());
                return rawResult.getText();
            } else {
                AirbitzCore.logi("Picture No QR code found");
            }
        }
        return null;
    }

    private Result tryScan(Bitmap bitmap) {
        for (float angle : new float[] { 0f, 90f }) {
            Result result = doTryScan(rotate(bitmap, angle));
            if (result != null)  {
                return result;
            }
        }
        return null;
    }

    private Result doTryScan(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        RGBLuminanceSource source = new RGBLuminanceSource(w, h, pixels);
        if (source.getMatrix() == null) {
            return null;
        }
        BinaryBitmap binary = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        Map<DecodeHintType, ?> hints = hints();
        try {
            return reader.decode(binary, hints);
        } catch (ReaderException re) {
            re.printStackTrace();
        } finally {
            reader.reset();
        }
        return null;
    }

    private Bitmap rotate(Bitmap bitmap, float angle) {
        if (angle == 0f) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Map<DecodeHintType, ?> hints() {
        Map<DecodeHintType, List> hints = new HashMap<DecodeHintType, List>();
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.CODE_128);
        formats.add(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        return hints;
    }
}
