package com.airbitz.fragments;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.airbitz.activities.NavigationActivity;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.objects.CameraSurfacePreview;
import com.airbitz.objects.PhotoHandler;

/**
 * Created on 2/22/14.
 */
public class SendFragment extends Fragment implements Camera.PreviewCallback, Camera.PictureCallback {

    private Handler mHandler;
    private EditText mToEdittext;

    private View mView;
    private Button mFromButton;

    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;

    private ImageButton mFlashOnButton;
    private ImageButton mFlashOffButton;
    private ImageButton mAutoFlashButton;

    private Camera mCamera;
    private CameraSurfacePreview mPreview;

    private FrameLayout mPreviewFrame;

    private int BACK_CAMERA_INDEX = 0;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;

//    private ScrollView mScrollView;

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
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_root);
        mNavigationLayout = (RelativeLayout) view.findViewById(R.id.navigation_layout);

        mFlashOffButton = (ImageButton) view.findViewById(R.id.button_flash_off);
        mFlashOnButton = (ImageButton) view.findViewById(R.id.button_flash_on);
        mAutoFlashButton = (ImageButton) view.findViewById(R.id.button_flash_auto);

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
        mFromTextView = (TextView) view.findViewById(R.id.textview_from);
        mToTextView = (TextView) view.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) view.findViewById(R.id.textview_scan_qrcode);

        mFromButton = (Button) view.findViewById(R.id.button_from);
        mToEdittext = (EditText) view.findViewById(R.id.edittext_to);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mFromButton.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mQRCodeTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"),Color.parseColor("#addff1")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mQRCodeTextView.getPaint().setShader(textShader);

        mFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).switchFragmentThread(1);
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
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_on);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_off);
                    mFlashOnInActive = true;
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

                }
                else{
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_off);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mAutoFlashButton.setImageResource(R.drawable.ico_flash_auto_on);
                    mFlashAutoActive = true;
                }
            }
        });

        mPreviewFrame = (FrameLayout) view.findViewById(R.id.layout_camera_preview);

        mView = view;
        return view;
    }

    class FakeCapturePhoto extends AsyncTask<Void, Integer, Boolean>{
        public FakeCapturePhoto() { }

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
            mCamera.takePicture(null, null, SendFragment.this);
            }
            catch (Exception e){

            }
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Camera.CameraInfo info = new Camera.CameraInfo();

        new PhotoHandler(getActivity(), data, info);

        Fragment fragment = new SendConfirmationFragment();
        ((NavigationActivity) getActivity()).pushFragment(fragment);
    }



    public void stopCamera() {
        Log.d("TAG", "stopCamera");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mPreviewFrame.removeView(mPreview);
            mCamera.release();
        }
        mCamera = null;
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
        mPreviewFrame.removeView(mPreview);
        mPreviewFrame = (FrameLayout) mView.findViewById(R.id.layout_camera_preview);
        Log.d("TAG", "addView");
        mPreviewFrame.addView(mPreview);
        Log.d("TAG", "setPreviewCallback");
        if(mCamera!=null)
            mCamera.setPreviewCallback(SendFragment.this);
        Log.d("TAG", "end setPreviewCallback");

//        new FakeCapturePhoto().execute();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mHandler==null)
            mHandler = new Handler();
        mHandler.postDelayed(cameraDelayRunner, 500);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mHandler.postDelayed(cameraDelayRunner, 500);
        }
        else {
            if (mCamera != null) {
                stopCamera();
            }
        }
    }

    Runnable cameraDelayRunner = new Runnable() {
        @Override
        public void run() { startCamera(BACK_CAMERA_INDEX); }
    };

    @Override
    public void onPause() {
        super.onPause();
        if(mHandler != null)
            mHandler.removeCallbacks(cameraDelayRunner);
        stopCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCamera();
    }

}
