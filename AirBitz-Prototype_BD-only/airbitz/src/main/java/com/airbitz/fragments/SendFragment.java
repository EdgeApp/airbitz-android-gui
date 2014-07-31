package com.airbitz.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.SWIGTYPE_p_int64_t;
import com.airbitz.api.SWIGTYPE_p_long;
import com.airbitz.api.SWIGTYPE_p_p_sABC_BitcoinURIInfo;
import com.airbitz.api.core;
import com.airbitz.api.tABC_BitcoinURIInfo;
import com.airbitz.api.tABC_Error;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.CameraSurfacePreview;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
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

    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;

    private ImageButton mHelpButton;

    private ImageButton mFlashButton;
    private ImageButton mGalleryButton;

    private ListView mListingListView;
    private RelativeLayout mListviewContainer;

    private Camera mCamera;
    private CameraSurfacePreview mPreview;

    private FrameLayout mPreviewFrame;

    private View dummyFocus;

    private HighlightOnPressSpinner walletSpinner;
    private List<String> mWalletList;//NAMES
    private List<Wallet> mWallets;//Actual wallets
    private Wallet mFromWallet;
    private String mSpinnerWalletName;
    private List<Wallet> mCurrentListing;

    private WalletPickerAdapter listingAdapter;

    private int BACK_CAMERA_INDEX = 0;

    private boolean mFlashOn = false;

    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(mCoreAPI==null)
            mCoreAPI = CoreAPI.getApi();

        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_send, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        addWalletNamesToList();

        mHelpButton = (ImageButton) mView.findViewById(R.id.button_help);

        mFlashButton = (ImageButton) mView.findViewById(R.id.button_flash);
        mGalleryButton = (ImageButton) mView.findViewById(R.id.button_gallery);

        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) mView.findViewById(R.id.textview_scan_qrcode);

        mToEdittext = (EditText) mView.findViewById(R.id.edittext_to);

        mListviewContainer = (RelativeLayout) mView.findViewById(R.id.listview_container);
        mListingListView = (ListView) mView.findViewById(R.id.listing_listview);

        mCurrentListing = new ArrayList<Wallet>();
        listingAdapter = new WalletPickerAdapter(getActivity(), mCurrentListing, WalletPickerEnum.SendTo);
        mListingListView.setAdapter(listingAdapter);

        dummyFocus = mView.findViewById(R.id.dummy_focus);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mQRCodeTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        walletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.from_wallet_spinner);
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.SendFrom);
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

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickAPicture();
            }
        });

        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mFlashOn){
                    mFlashButton.setImageResource(R.drawable.btn_flash_on);
                    mFlashOn = true;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                }
                else{
                    mFlashButton.setImageResource(R.drawable.btn_flash_off);
                    mFlashOn = false;
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
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
                    if(CheckURIResults(strTo)) {
                        GotoSendConfirmation(strTo, 0, "", bIsUUID);
                    }
                    else {
                        if(strTo.length()>0)
                            showMessageAlert("", getString(R.string.fragment_send_confirmation_invalid_bitcoin_address));
                        else
                            mListingListView.setVisibility(View.GONE);
                        hideKeyboard();
                    }
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
                    /*View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                    float heightPop = activityRootView.getHeight() - mListviewContainer.getX();
                    RelativeLayout.LayoutParams lLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)heightPop);
                    lLP.addRule(RelativeLayout.BELOW,R.id.layout_data);
                    lLP.setMargins(0,(int)getActivity().getResources().getDimension(R.dimen.negative_margin_popup),0,0);
                    mListviewContainer.setLayoutParams(lLP);*/
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
                mListviewContainer.setVisibility(View.GONE);
                hideKeyboard();
                Wallet w = mCurrentListing.get(i);

                GotoSendConfirmation(w.getUUID(), 0, " ", true);
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Send", "Send info");
            }
        });


        mPreviewFrame = (FrameLayout) mView.findViewById(R.id.layout_camera_preview);

        Bundle bundle = getArguments();
        if(bundle!=null) {
            String uuid = bundle.getString(UUID); // From a wallet with this UUID
            if(uuid!=null) {
                mFromWallet = mCoreAPI.getWallet(uuid);
                if(mFromWallet!=null) {
                    for(int i=0; i<mWallets.size(); i++) {
                        if(mFromWallet.getUUID().equals(mWallets.get(i).getUUID()) && !mWallets.get(i).isArchived()) {
                            final int finalI = i;
                            walletSpinner.post(new Runnable() {
                                @Override
                                public void run() {
                                    walletSpinner.setSelection(finalI);
                                }
                            });
                            break;
                        }
                    }
                }
            } else if (bundle.getString(WalletsFragment.FROM_SOURCE).equals("URI")) {
                String uriString = bundle.getString(NavigationActivity.URI);
                bundle.putString(NavigationActivity.URI, ""); //to clear the URI after reading once
                if(!uriString.isEmpty())
                    CheckURIResults(uriString);
            }
        }


        return mView;
    }

    private void hideKeyboard() {
        final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
        if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() > 100) {
            final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0, 0);
        }
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
        mPreviewFrame.removeView(mPreview);
        mPreviewFrame.addView(mPreview);
        if(mCamera!=null)
            mCamera.setPreviewCallback(SendFragment.this);
        Camera.Parameters params = mCamera.getParameters();
        List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
        if(supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        if(supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);

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
            if(CheckURIResults(rawResult.getText())) {
                Log.d("SendFragment", "QR result is good");
            } else {
                Log.d("SendFragment", "QR result is bad");
            }
        }
    }

    private static int RESULT_LOAD_IMAGE = 678;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

            AttemptDecodePicture(thumbnail);
        }
    }

    // Select a picture from the Gallery
    private void PickAPicture() {
        mToEdittext.clearFocus();
        Intent in = new   Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, RESULT_LOAD_IMAGE);
    }

    private void AttemptDecodePicture(Bitmap thumbnail) {
        if(thumbnail==null) {
            Log.d("SendFragment", "No picture selected");
        } else {
            Log.d("SendFragment", "Picture selected");
            Result rawResult = null;
            Reader reader = new QRCodeReader();
            int w = thumbnail.getWidth();
            int h = thumbnail.getHeight();
            int[] pixels = new int[w*h];
            thumbnail.getPixels(pixels, 0, w, 0, 0, w, h);
            RGBLuminanceSource source = new RGBLuminanceSource(w, h, pixels);
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
                if(CheckURIResults(rawResult.getText())) {
                    Log.d("SendFragment", "QR result is good");
                } else {
                    Log.d("SendFragment", "QR result is bad");
                }
            } else {
                Log.d("Send Fragment", "No QR code found");
            }
        }
    }

    private void GotoSendConfirmation(String uuid, long amountSatoshi, String label, boolean isUUID) {
        if(mToEdittext!=null)
            mToEdittext.setText(uuid);

        Fragment fragment = new SendConfirmationFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_UUID, isUUID);
        bundle.putString(UUID, uuid);
        bundle.putLong(AMOUNT_SATOSHI, amountSatoshi);
        bundle.putString(LABEL, label);
        bundle.putString(FROM_WALLET_NAME, ((Wallet) walletSpinner.getSelectedItem()).getName());
        fragment.setArguments(bundle);
        if(mActivity!=null)
            mActivity.pushFragment(fragment);
    }

    public boolean CheckURIResults(String results)
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

                if (uriAddress!=null) {
                    Log.i("SendFragment", "Send address: "+uriAddress);
                    Log.i("SendFragment", "Send amount: "+amountSatoshi);

                    String label = uri.getSzLabel();
                    String message = uri.getSzMessage();
                    if (message!=null) {
                        Log.i("SendFragment", "    message: "+message);
                    }
                    bSuccess = true;

                    GotoSendConfirmation(uriAddress, amountSatoshi, label, false);
                }
                else {
                    Log.i("SendFragment", "no address: ");
                    bSuccess = false;
                }
            }
            else {
                Log.i("SendFragment", "URI parse failed!");
                bSuccess = false;
            }

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
        dummyFocus.requestFocus();
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
        List<Wallet> temp = mCoreAPI.getCoreWallets();
        mWallets = new ArrayList<Wallet>();
        mWalletList = new ArrayList<String>();
        for(Wallet wallet: temp){
            if(!wallet.isArchived()){
                mWallets.add(wallet);
                mWalletList.add(wallet.getName());
            }
        }
    }

    public void goAutoCompleteListing(){
        String text = mToEdittext.getText().toString();
        mCurrentListing.clear();
        if(text.isEmpty()){
            for(Wallet w : mWallets){
                if(!w.getName().equals(mSpinnerWalletName) && !w.isArchived()){
                    mCurrentListing.add(w);
                }
            }
        }else {
            for (Wallet w : mWallets) {
                if (!w.getName().equals(mSpinnerWalletName) && !w.isArchived() && w.getName().toLowerCase().contains(text.toLowerCase())) {
                    mCurrentListing.add(w);
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

    private void showMessageAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
