package com.airbitz.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.LandingActivity;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.objects.CameraSurfacePreview;
import com.airbitz.objects.PhotoHandler;

/**
 * Created on 3/3/14.
 */
public class ImportFragment extends Fragment implements Camera.PreviewCallback, Camera.PictureCallback, GestureDetector.OnGestureListener{

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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_wallet, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_root);
        mNavigationLayout = (RelativeLayout) view.findViewById(R.id.navigation_layout);
//        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        mFromButton = (Button) view.findViewById(R.id.button_from);
        mToEdittext = (EditText) view.findViewById(R.id.edittext_to);

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
        mFromTextView = (TextView) view.findViewById(R.id.textview_from);
        mToTextView = (TextView) view.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) view.findViewById(R.id.textview_scan_qrcode);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);

        mFlashOffButton = (ImageButton) view.findViewById(R.id.button_flash_off);
        mFlashOnButton = (ImageButton) view.findViewById(R.id.button_flash_on);
        mAutoFlashButton = (ImageButton) view.findViewById(R.id.button_flash_auto);

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
//                mIntent = new Intent(ImportFragment.this, WalletActivity.class);
//                mIntent.putExtra(RequestActivity.CLASSNAME, "ImportActivity");
//                startActivity(mIntent);
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

        preview = (FrameLayout) view.findViewById(R.id.layout_camera_preview);
        cameraIndex = BACK_CAMERA_INDEX;

        try {
            mCamera = Camera.open(cameraIndex);
            mCamParam = mCamera.getParameters();
            Log.d("TAG", "Camera Does exist");
        } catch (Exception e) {
            Log.d("TAG", "Camera Does Not exist");
        }

        mPreview = new CameraSurfacePreview(getActivity(), mCamera);
        preview.removeView(mPreview);
        preview.addView(mPreview);

        mCamera.setPreviewCallback(ImportFragment.this);

        orientationListener();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Common.showHelpInfo(ImportFragment.this, "Info", "Business directory info");
            }
        });

        return view;
    }

    private void orientationListener() {
        Log.d("TAG", "orientationListener");
        orientationEventListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceOrientation = orientation;

                if (orientation == ORIENTATION_UNKNOWN){
                    return;
                }
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraIndex, info);
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
                mCamera.takePicture(null, null, ImportFragment.this);
            }
            catch (Exception e){

            }
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Camera.CameraInfo info = new Camera.CameraInfo();

        new PhotoHandler(getActivity(), data, info);

        Fragment fragment = new WalletPasswordFragment();
        ((NavigationActivity) getActivity()).pushFragment(fragment);
    }

    public void startCamera(int cameraIndex) {

        try {
            Log.d("TAG", "Opening Camera");
            mCamera = Camera.open(cameraIndex);
        } catch (Exception e) {
            Log.d("TAG", "Camera Does Not exist");
        }

        mPreview = new CameraSurfacePreview(getActivity(), mCamera);
        SurfaceView msPreview = new SurfaceView(getActivity().getApplicationContext());
        Log.d("TAG", "removeView");
        preview.removeView(mPreview);
        preview = (FrameLayout) getView().findViewById(R.id.layout_camera_preview);
        Log.d("TAG", "addView");
        preview.addView(mPreview);
        Log.d("TAG", "setPreviewCallback");
        mCamera.setPreviewCallback(ImportFragment.this);
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
//                    finish();
                    return true;
                }
            }
        }
        return false;
    }
}
