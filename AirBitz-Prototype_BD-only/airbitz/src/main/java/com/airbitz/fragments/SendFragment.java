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
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.CameraSurfacePreview;
import com.airbitz.objects.HighlightOnPressImageButton;
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
public class SendFragment extends Fragment implements Camera.PreviewCallback {
    private final String TAG = getClass().getSimpleName();
    
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

    private HighlightOnPressImageButton mHelpButton;

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

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_send_help_button);

        mFlashButton = (ImageButton) mView.findViewById(R.id.button_flash);
        mGalleryButton = (ImageButton) mView.findViewById(R.id.button_gallery);

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
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
        mFromTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.montserratRegularTypeFace);
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
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

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

                    String strTo = mToEdittext.getText().toString();
                    if(strTo==null || strTo.isEmpty()) {
                        hideKeyboard();
                        mListviewContainer.setVisibility(View.GONE);
                        return true;
                    }

                    boolean bIsUUID = false;
                    CoreAPI.BitcoinURIInfo results = mCoreAPI.CheckURIResults(strTo);
                    if(results.address!=null) {
                        GotoSendConfirmation(strTo, 0, "", bIsUUID);
                    } else {
                        hideKeyboard();
                        ((NavigationActivity) getActivity()).showOkMessageDialog(getString(R.string.fragment_send_confirmation_invalid_bitcoin_address));
                    }
                    return true;
                }
                return false;
            }
        });

        mToEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

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
                if(!uriString.isEmpty()) {
                    CoreAPI.BitcoinURIInfo info = mCoreAPI.CheckURIResults(uriString);
                    if(info!=null && info.getSzAddress()!=null) {
                        GotoSendConfirmation(info.address, info.amountSatoshi, info.label, false);
                    }
                }
            }
        }


        return mView;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    public void stopCamera() {
        Common.LogD(TAG, "stopCamera");
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
            Common.LogD(TAG, "Opening Camera");
            mCamera = Camera.open(cameraIndex);
        } catch (Exception e) {
            Common.LogD(TAG, "Camera Does Not exist");
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
        CoreAPI.BitcoinURIInfo info = AttemptDecodeBytes(bytes, camera);
        if(info!=null && info.getSzAddress()!=null) {
            stopCamera();
            GotoSendConfirmation(info.address, info.amountSatoshi, info.label, false);
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

            CoreAPI.BitcoinURIInfo info = AttemptDecodePicture(thumbnail);
            if(info!=null && info.getSzAddress()!=null) {
                GotoSendConfirmation(info.address, info.amountSatoshi, info.label, false);
            }
        }
    }

    // Select a picture from the Gallery
    private void PickAPicture() {
        mToEdittext.clearFocus();
        Intent in = new   Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, RESULT_LOAD_IMAGE);
    }

    private CoreAPI.BitcoinURIInfo AttemptDecodeBytes(byte[] bytes, Camera camera) {
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
            return mCoreAPI.CheckURIResults(rawResult.getText());
        } else {
            Common.LogD(TAG, "No QR code found");
        }
        return null;
    }

    private CoreAPI.BitcoinURIInfo AttemptDecodePicture(Bitmap thumbnail) {
        if(thumbnail==null) {
            Common.LogD(TAG, "No picture selected");
        } else {
            Common.LogD(TAG, "Picture selected");
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
                return mCoreAPI.CheckURIResults(rawResult.getText());
            } else {
                Common.LogD(TAG, "No QR code found");
            }
        }
        return null;
    }

    public void GotoSendConfirmation(String uuid, long amountSatoshi, String label, boolean isUUID) {
        Fragment fragment = new SendConfirmationFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_UUID, isUUID);
        bundle.putString(UUID, uuid);
        bundle.putLong(AMOUNT_SATOSHI, amountSatoshi);
        bundle.putString(LABEL, label);
        bundle.putString(FROM_WALLET_NAME, ((Wallet) walletSpinner.getSelectedItem()).getName());
        fragment.setArguments(bundle);
        if(mActivity!=null)
            mActivity.pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
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
}
