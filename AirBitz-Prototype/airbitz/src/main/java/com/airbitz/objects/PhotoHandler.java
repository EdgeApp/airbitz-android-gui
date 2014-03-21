package com.airbitz.objects;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.airbitz.activities.SendConfirmationActivity;
import com.airbitz.activities.WalletPasswordActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created on 12/27/13.
 */
@TargetApi(11)
public class PhotoHandler implements Camera.PictureCallback{

    private Context mContext;
    private String mClassName;

    private static String mMimeType = "image/jpg";

    public PhotoHandler(Context context, String className)
    {
        Log.d("TAG custom", "Constructing Photohandler...");
        this.mContext = context;
        mClassName = className;
        Log.d("TAG custom", "Finish Constructing Photohandler...  " + mContext);
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Camera.CameraInfo info =
                new Camera.CameraInfo();


        if(mClassName.equalsIgnoreCase("SendConfirmationActivity")){
            Intent capturedPicIntent = new Intent(mContext, SendConfirmationActivity.class);
            mContext.startActivity(capturedPicIntent);
        }
        else{
            Intent capturedPicIntent = new Intent(mContext, WalletPasswordActivity.class);
            mContext.startActivity(capturedPicIntent);
        }
    }

    private void saveData(String filename, byte[] data){

        try {
            FileOutputStream outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileInputStream inputStream = mContext.openFileInput(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            r.close();
            inputStream.close();
            Log.d("File", "File contents: " + total);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private File getDir() {
        String rootDir = "";
        if(Environment.isExternalStorageEmulated()){
            rootDir = Environment.getExternalStorageDirectory().toString();
        } else {
            rootDir = Environment.getDataDirectory().toString();
        }
        File photoDirectory = new File(rootDir+"/Selfie/");
        photoDirectory.mkdirs();
        return photoDirectory;
    }


    public static Bitmap decodeFile(String path) {

        int orientation;

        try {

            if(path==null){

                return null;
            }
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 4;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            Bitmap bm = BitmapFactory.decodeFile(path, o2);


            Bitmap bitmap = bm;

            ExifInterface exif = new ExifInterface(path);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.e("orientation", "" + orientation);
            Matrix m=new Matrix();

            if((orientation==3)){

                m.postRotate(180);
                m.postScale((float)bm.getWidth(), (float)bm.getHeight());

                Log.e("in orientation", "" + orientation);

                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return  bitmap;
            }
            else if(orientation==6){

                m.postRotate(90);

                Log.e("in orientation", "" + orientation);

                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return  bitmap;
            }

            else if(orientation==8){

                m.postRotate(270);

                Log.e("in orientation", "" + orientation);

                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return  bitmap;
            }
            return bitmap;
        }
        catch (Exception e) {
        }
        return null;
    }
}
