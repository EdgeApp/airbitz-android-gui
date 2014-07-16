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
import com.airbitz.api.CoreAPI;
import com.airbitz.api.SWIGTYPE_p_int64_t;
import com.airbitz.api.SWIGTYPE_p_long;
import com.airbitz.api.SWIGTYPE_p_p_sABC_BitcoinURIInfo;
import com.airbitz.api.core;
import com.airbitz.api.tABC_BitcoinURIInfo;
import com.airbitz.api.tABC_Error;
import com.airbitz.models.Wallet;
import com.airbitz.objects.CameraSurfacePreview;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.List;


/**
 * Created on 2/22/14.
 */
public class SendFragment extends Fragment implements Camera.PreviewCallback, Camera.PictureCallback {
    public static final String AMOUNT_SATOSHI = "com.airbitz.Sendfragment_AMOUNT_SATOSHI";
    public static final String LABEL = "com.airbitz.Sendfragment_LABEL";
    public static final String UUID = "com.airbitz.Sendfragment_UUID";
    public static final String IS_UUID = "com.airbitz.Sendfragment_IS_UUID";
    public static final String FROM_WALLET_NAME = "com.airbitz.Sendfragment_FROM_WALLET_NAME";

    private Handler mHandler;
    private EditText mToEdittext;

    private View mView;

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
    private List<String> mWalletList;//NAMES
    private List<Wallet> mWallets;//Actual wallets
    private String mSpinnerWalletName;
    private List<String> mCurrentListing;

    private ArrayAdapter<String> listingAdapter;

    private int BACK_CAMERA_INDEX = 0;

//    private ScrollView mScrollView;

    private boolean mFlashOn = false;

    private CoreAPI mCoreAPI;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mWalletList = new ArrayList<String>();
        mWallets = new ArrayList<Wallet>();
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
                mSpinnerWalletName = mWalletList.get(i);
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

                    boolean bIsUUID = false;
                    String strTo = mToEdittext.getText().toString();

                    //disabled for now - user must select wallet from list, not type a name and hit return
//                    if(mCurrentListing.contains(strTo))
//                    {
//                        bIsUUID = true;
//                        strTo = mCoreAPI.getWalletFromName(strTo).getUUID();
//                    }
                    GotoSendConfirmation(strTo, 0, "", bIsUUID);
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
                    View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                    float heightPop = activityRootView.getHeight() - mListviewContainer.getX();
                    RelativeLayout.LayoutParams lLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)heightPop);
                    lLP.addRule(RelativeLayout.BELOW,R.id.layout_data);
                    lLP.setMargins(0,(int)getActivity().getResources().getDimension(R.dimen.negative_margin_popup),0,0);
                    mListviewContainer.setLayoutParams(lLP);
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
                Wallet w = mCoreAPI.getWallet(mCurrentListing.get(i));
                GotoSendConfirmation(w.getUUID(), 0, " ", true);
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
//            mCamera.takePicture(null, null, SendFragment.this);
//                GotoSendConfirmation("uuid", 0, "label", false);
            }
            catch (Exception e){
            }
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
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
//        Log.d("TAG", "removeView");
        mPreviewFrame.removeView(mPreview);
        mPreviewFrame = (FrameLayout) mView.findViewById(R.id.layout_camera_preview);
//        Log.d("TAG", "addView");
        mPreviewFrame.addView(mPreview);
//        Log.d("TAG", "setPreviewCallback");
        if(mCamera!=null)
            mCamera.setPreviewCallback(SendFragment.this);
//        Log.d("TAG", "end setPreviewCallback");
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);

//        new FakeCapturePhoto().execute();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Result rawResult = null;
        Reader reader = new QRCodeReader();
        int w = camera.getParameters().getPreviewSize().width;
        int h = camera.getParameters().getPreviewSize().height;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(bytes, w, h, 0, 0, w, h, false);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = reader.decode(bitmap);
            } catch (ReaderException re) {
                // nothing to do here
            } finally {
                reader.reset();
            }
        }
        if(rawResult!=null) {
            if(CheckQRResults(rawResult.getText())) {
                Log.d("SendFragment", "QR result is good");
            } else {
                Log.d("SendFragment", "QR result is bad");
            }
        }
    }

    private void GotoSendConfirmation(String uuid, long amountSatoshi, String label, boolean isUUID) {
        Fragment fragment = new SendConfirmationFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_UUID, isUUID);
        bundle.putString(UUID, uuid);
        bundle.putLong(AMOUNT_SATOSHI, amountSatoshi);
        bundle.putString(LABEL, label);
        bundle.putString(FROM_WALLET_NAME, mSpinnerWalletName);
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment);
    }

    private boolean CheckQRResults(String results)
    {
        boolean bSuccess = false;

        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_BitcoinURIInfo pUri = core.longPtr_to_ppBitcoinURIInfo(lp);

        core.ABC_ParseBitcoinURI(results, pUri, Error);

        BitcoinURIInfo uri = new BitcoinURIInfo(core.longp_value(lp));

        if (uri.getPtr(uri) != 0)
            {
                String uriAddress = uri.getSzAddress();
                SWIGTYPE_p_int64_t temp = uri.getAmountSatoshi();
                SWIGTYPE_p_long p = core.p64_t_to_long_ptr(temp);
                long amountSatoshi = core.longp_value(p);

                if (!uriAddress.isEmpty())
                {
                    Log.i("SendFragment", "    address: "+uriAddress);
                    Log.i("SendFragment", "    amount: "+amountSatoshi);

                    String label = uri.getSzLabel();
                    if (!label.isEmpty())
                    {
                        Log.i("SendFragment", "    label: "+label);
                    }
                    else
                    {
                        label = "Anonymous"; //TODO localize
                    }

                    String message = uri.getSzMessage();
                    if (!message.isEmpty())
                    {
                        Log.i("SendFragment", "    message: "+message);
                    }
                    bSuccess = true;

                    if(mHandler != null)
                        mHandler.removeCallbacks(cameraDelayRunner);
                    stopCamera();

                    GotoSendConfirmation(uriAddress, amountSatoshi, label, true);
                }
                else
                {
                    Log.i("SendFragment", "no address: ");
                    bSuccess = false;
                }
            }
            else
            {
                Log.i("SendFragment", "URI parse failed!");
                bSuccess = false;
            }

//            ABC_FreeURIInfo(uri);

        return bSuccess;
    }

    private class BitcoinURIInfo extends tABC_BitcoinURIInfo {
        public String address;
        public String label;
        public String message;
        public long amountSatoshi;
        public BitcoinURIInfo(long pv) {
            super(pv, false);
            if (pv != 0) {
                address = super.getSzAddress();
                label = super.getSzLabel();
                SWIGTYPE_p_int64_t temp = super.getAmountSatoshi();
                SWIGTYPE_p_long p = core.p64_t_to_long_ptr(temp);
                amountSatoshi = core.longp_value(p);
                message = super.getSzMessage();
            }
        }
        public long getPtr(tABC_BitcoinURIInfo p) {
            return getCPtr(p);
        }
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
        List<Wallet> tempWallets = mCoreAPI.loadWallets();
        for(Wallet wallet: tempWallets){
            if(!wallet.isHeader() && !wallet.isArchiveHeader())
                mWalletList.add(wallet.getName());
                mWallets.add(wallet);
        }
    }

    public void goAutoCompleteListing(){
        String text = mToEdittext.getText().toString();
        mCurrentListing.clear();
        if(text.isEmpty()){
            for(String name : mWalletList){
                if(name != mSpinnerWalletName){
                    mCurrentListing.add(name);
                }
            }
        }else {
            for (String name : mWalletList) {
                if (name != mSpinnerWalletName && name.toLowerCase().contains(text.toLowerCase())) {
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
