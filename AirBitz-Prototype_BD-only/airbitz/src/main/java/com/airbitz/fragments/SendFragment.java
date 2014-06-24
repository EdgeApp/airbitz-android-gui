package com.airbitz.fragments;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.objects.CameraSurfacePreview;
import com.airbitz.objects.PhotoHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/22/14.
 */
public class SendFragment extends Fragment implements Camera.PreviewCallback, Camera.PictureCallback {

    private Handler mHandler;
    private EditText mToEdittext;

    private View mView;
    //private Button mFromButton;

    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;

    private ImageButton mFlashOnButton;
    private ImageButton mFlashOffButton;
    private ImageButton mGalleryButton;

    private ListView mListingListView;
    private LinearLayout mListviewContainer;

    private Camera mCamera;
    private CameraSurfacePreview mPreview;

    private FrameLayout mPreviewFrame;

    private View dummyFocus;

    private Spinner walletSpinner;
    private List<String> mWalletList;
    private String currentWallet;
    private List<String> mCurrentListing;

    private ArrayAdapter<String> listingAdapter;

    private int BACK_CAMERA_INDEX = 0;

//    private ScrollView mScrollView;

    private boolean mFlashOn = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mWalletList = new ArrayList<String>();
        addWalletNamesToList();

        mFlashOffButton = (ImageButton) view.findViewById(R.id.button_flash_off);
        mFlashOnButton = (ImageButton) view.findViewById(R.id.button_flash_on);
        mGalleryButton = (ImageButton) view.findViewById(R.id.button_gallery);

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
        mFromTextView = (TextView) view.findViewById(R.id.textview_from);
        mToTextView = (TextView) view.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) view.findViewById(R.id.textview_scan_qrcode);

        mToEdittext = (EditText) view.findViewById(R.id.edittext_to);

        mListviewContainer = (LinearLayout) view.findViewById(R.id.listview_container);
        mListingListView = (ListView) view.findViewById(R.id.listing_listview);

        mCurrentListing = new ArrayList<String>();
        listingAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mCurrentListing);
        mListingListView.setAdapter(listingAdapter);

        dummyFocus = view.findViewById(R.id.dummy_focus);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mQRCodeTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        walletSpinner = (Spinner) view.findViewById(R.id.from_wallet_spinner);
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mWalletList);
        walletSpinner.setAdapter(dataAdapter);

        walletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentWallet = mWalletList.get(i);
                goAutoCompleteListing();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"),Color.parseColor("#addff1")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mQRCodeTextView.getPaint().setShader(textShader);

        mFlashOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mFlashOn){
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_off);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_on);
                    mFlashOn = true;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                }
                else{
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_on);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mFlashOn = false;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                }
            }
        });

        mFlashOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFlashOn){
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_on);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_off);
                    mFlashOn = false;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                }
                else{
                    mFlashOffButton.setImageResource(R.drawable.ico_flash_off_off);
                    mFlashOnButton.setImageResource(R.drawable.ico_flash_on_on);
                    mFlashOn = true;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                }
            }
        });

        mToEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    dummyFocus.requestFocus();
                    Fragment frag = new SendConfirmationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("wallet_name", (String)walletSpinner.getSelectedItem());
                    bundle.putString("to_name",mToEdittext.getText().toString());
                    frag.setArguments(bundle);
                    ((NavigationActivity)getActivity()).pushFragment(frag);
                    return true;
                }
                return false;
            }
        });

        mToEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                goAutoCompleteListing();
            }
        });

        mToEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    goAutoCompleteListing();
                }else{
                    mListviewContainer.setVisibility(View.GONE);
                }
            }
        });

        mListingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dummyFocus.requestFocus();
                Fragment frag = new SendConfirmationFragment();
                Bundle bundle = new Bundle();
                bundle.putString("wallet_name", (String)walletSpinner.getSelectedItem());
                bundle.putString("to_name",mCurrentListing.get(i));
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag);
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

    public void addWalletNamesToList(){
        CoreAPI api = CoreAPI.getApi();
        List<Wallet> tempWallets = api.loadWallets();
        for(Wallet wallet: tempWallets){
            if(wallet.getName()!="xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL" && wallet.getName()!="SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd")
                mWalletList.add(wallet.getName());
        }
    }

    public void goAutoCompleteListing(){
        String text = mToEdittext.getText().toString();
        mCurrentListing.clear();
        if(text.isEmpty()){
            for(String name : mWalletList){
                if(name != currentWallet){
                    mCurrentListing.add(name);
                }
            }
        }else {
            for (String name : mWalletList) {
                if (name != currentWallet && name.toLowerCase().contains(text.toLowerCase())) {
                    mCurrentListing.add(name);
                }
            }
        }
        if(mCurrentListing.isEmpty() || !mToEdittext.hasFocus()){
            mListviewContainer.setVisibility(View.GONE);
        }else{
            mListviewContainer.setVisibility(View.VISIBLE);
        }
        listingAdapter.notifyDataSetChanged();
    }

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
