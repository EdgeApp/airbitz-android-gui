package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.objects.CameraSurfacePreview;
import com.airbitz.objects.PhotoHandler;
import com.airbitz.utils.Common;

/**
 * Created on 3/3/14.
 */
public class ImportActivity extends Activity implements Camera.PreviewCallback, GestureDetector.OnGestureListener{

    private EditText mToEdittext;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private Button mFromButton;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;
//    private ScrollView mScrollView;

    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;

    private ImageButton mFlashOnButton;
    private ImageButton mFlashOffButton;
    private ImageButton mAutoFlashButton;

    private OrientationEventListener orientationEventListener;
    private int deviceOrientation;
    private int presentOrientation;

    private Camera mCamera;
    private CameraSurfacePreview mPreview;
    private int cameraIndex;

    private FrameLayout preview;

    private Camera.Parameters mCamParam;

    private int BACK_CAMERA_INDEX = 0;

    private GestureDetector mGestureDetector;

    private Intent mIntent;

    private boolean mFlashAutoActive = true;
    private boolean mFlashOffInActive = false;
    private boolean mFlashOnInActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_wallet);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mGestureDetector = new GestureDetector(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_root);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);
//        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        mFromButton = (Button) findViewById(R.id.button_from);
        mToEdittext = (EditText) findViewById(R.id.edittext_to);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mFromTextView = (TextView) findViewById(R.id.textview_from);
        mToTextView = (TextView) findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) findViewById(R.id.textview_scan_qrcode);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mFlashOffButton = (ImageButton) findViewById(R.id.button_flash_off);
        mFlashOnButton = (ImageButton) findViewById(R.id.button_flash_on);
        mAutoFlashButton = (ImageButton) findViewById(R.id.button_flash_auto);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(LandingActivity.latoBlackTypeFace);
        mToTextView.setTypeface(LandingActivity.latoBlackTypeFace);
        mFromButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(LandingActivity.latoBlackTypeFace);
        mQRCodeTextView.setTypeface(LandingActivity.helveticaNeueTypeFace);

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"),Color.parseColor("#addff1")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mQRCodeTextView.getPaint().setShader(textShader);

        mFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(ImportActivity.this, WalletActivity.class);
                mIntent.putExtra(RequestActivity.CLASSNAME, "ImportActivity");
                startActivity(mIntent);
            }
        });

        mFlashOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFlashOffInActive){

                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_off);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_on);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_off);

                    mFlashOffInActive = true;
                }
                else{
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_on);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_off);
                    mFlashOffInActive = false;
                }
            }
        });

        mFlashOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFlashOnInActive){
                    mFlashOnInActive = true;
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_on);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_off);
                }
                else{

                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_off);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_on);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_off);
                    mFlashOnInActive = false;
                }
            }
        });

        mAutoFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFlashAutoActive){
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_off);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_off);
                    mFlashAutoActive = false;
                }
                else{
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_off);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_on);
                    mFlashAutoActive = true;
                }
            }
        });

        preview = (FrameLayout) findViewById(R.id.layout_camera_preview);
        cameraIndex = BACK_CAMERA_INDEX;

        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
                if (heightDiff > 100) {
                    mNavigationLayout.setVisibility(View.GONE);
                }
                else
                {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                }
            }
        });


        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

//        mScrollView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return mGestureDetector.onTouchEvent(motionEvent);
//            }
//        });

        try {
            mCamera = Camera.open(cameraIndex);
            mCamParam = mCamera.getParameters();
            Log.d("TAG", "Camera Does exist");
        } catch (Exception e) {
            Log.d("TAG", "Camera Does Not exist");
        }

        mPreview = new CameraSurfacePreview(ImportActivity.this, mCamera);
        preview.removeView(mPreview);
        preview.addView(mPreview);

        mCamera.setPreviewCallback(ImportActivity.this);

        orientationListener();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(ImportActivity.this, "Info", "Business directory info");
            }
        });
    }

    private void orientationListener() {
        Log.d("TAG", "orientationListener");
        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceOrientation = orientation;

                if (orientation == ORIENTATION_UNKNOWN){
                    return;
                }
                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(cameraIndex, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {
                    rotation = (info.orientation + orientation) % 360;
                }
                mCamParam.setRotation(rotation);
            }
        };

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }

        presentOrientation = 90 * (deviceOrientation / 360) % 360;
    }


    class FakeCapturePhoto extends AsyncTask<Void, Integer, Boolean> {
        public FakeCapturePhoto(){

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for(int i=0;i<=7;i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            try{
                mCamera.takePicture(null, null, new PhotoHandler(ImportActivity.this, "WalletPasswordActivity"));
            }
            catch (Exception e){

            }
        }
    }


    public void startCamera(int cameraIndex) {

        try {
            Log.d("TAG", "Opening Camera");
            mCamera = Camera.open(cameraIndex);
        } catch (Exception e) {
            Log.d("TAG", "Camera Does Not exist");
        }

        mPreview = new CameraSurfacePreview(ImportActivity.this, mCamera);
        SurfaceView msPreview = new SurfaceView(getApplicationContext());
        Log.d("TAG", "removeView");
        preview.removeView(mPreview);
        preview = (FrameLayout) findViewById(R.id.layout_camera_preview);
        Log.d("TAG", "addView");
        preview.addView(mPreview);
        Log.d("TAG", "setPreviewCallback");
        mCamera.setPreviewCallback(ImportActivity.this);
        Log.d("TAG", "end setPreviewCallback");

        new FakeCapturePhoto().execute();
    }

    public void stopCamera() {
        Log.d("TAG", "stopCamera");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            preview.removeView(mPreview);
            mCamera.release();
        }
        mCamera = null;
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
        if (mCamera != null) {
            stopCamera();
        }
        startCamera(BACK_CAMERA_INDEX);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCamera();
    }



    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
//        Log.d("TAG", "onPreviewFrame");
//
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        Camera.getCameraInfo(BACK_CAMERA_INDEX, cameraInfo);
//        int angle = cameraInfo.orientation;
//
//        Log.d("TAG", "Orientation Angle = " + angle);
//
//        presentOrientation = (90 * Math.round(deviceOrientation / 90)) % 360;
//        Log.d("TAG", "presentOrientation: " + presentOrientation);
//        int dRotation = display.getRotation();
//        Log.d("TAG", "dRotation: " + dRotation);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
        if(start != null & finish != null){

            float yDistance = Math.abs(finish.getY() - start.getY());

            if((finish.getRawX()>start.getRawX()) && (yDistance < 15)){
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if(xDistance > 50){
                    finish();
                    return true;
                }
            }
        }
        return false;
    }
}
