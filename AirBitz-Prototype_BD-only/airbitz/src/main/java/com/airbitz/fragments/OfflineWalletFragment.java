package com.airbitz.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created on 2/12/14.
 */
public class OfflineWalletFragment extends Fragment implements GestureDetector.OnGestureListener{

    private EditText mAddressField;
    private EditText mPrivateKeyField;

    private TextView mAddressTextView;

    private ImageView mQRCodeImage;

    private Button mDoneButton;
    private Button mCopyButton;
    private Button mPrintButton;

    private RelativeLayout mRootLayout;

    private ScrollView mScrollView;

    private ImageButton mHelpButton;
    private ImageButton mBackButton;

    private ClipboardManager clipboard;

    private GestureDetector mGestureDetector;

    private static String[] menus = {"Copy Public Address", "External Storage/Print"};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private View mView;
    @Override public void onDestroyView() {
        super.onDestroyView();
        ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
        if( null != parentViewGroup ) {
            parentViewGroup.removeView( mView );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        mView = inflater.inflate(R.layout.fragment_offline_wallet, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mRootLayout = (RelativeLayout) mView.findViewById(R.id.layout_root);
        mAddressTextView = (TextView) mView.findViewById(R.id.textview_address);

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"),Color.parseColor("#addff1")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mAddressTextView.getPaint().setShader(textShader);

        mAddressTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_scroll);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mGestureDetector = new GestureDetector(this);

        mAddressField = (EditText) mView.findViewById(R.id.edittext_address);
        mPrivateKeyField = (EditText) mView.findViewById(R.id.edittext_privatekey);
        mPrivateKeyField.setKeyListener(null);
        mAddressField.setKeyListener(null);

        mDoneButton = (Button) mView.findViewById(R.id.fragment_offline_wallet_button_done);
        mCopyButton = (Button) mView.findViewById(R.id.button_copy_address);
        mPrintButton = (Button) mView.findViewById(R.id.button_external_storage);

        mCopyButton.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.BOLD);
        mPrintButton.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.BOLD);

        mQRCodeImage = (ImageView) mView.findViewById(R.id.imageview_qrcode);
        mBackButton = (ImageButton) mView.findViewById(R.id.fragment_category_button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.fragment_category_button_help);

        TextView titleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);

        titleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mAddressField.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.ITALIC);
        mPrivateKeyField.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.ITALIC);
        mRootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("private key",mAddressField.getText().toString());
                clipboard.setPrimaryClip(clip);
            }
        });


        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap qrCodeImage = BitmapFactory.decodeResource(getResources(), R.drawable.img_qr_code);

                String dir="";
                if(Environment.isExternalStorageEmulated()){
                    dir=Environment.getExternalStorageDirectory().toString()+"/"+Environment.DIRECTORY_DCIM;
                } else {
                    dir=Environment.getDataDirectory().toString()+"/"+Environment.DIRECTORY_DCIM;
                }
                File fDir = new File(dir);
                if(!fDir.exists()){
                    fDir.mkdirs();
                }
                String filename = dir+"_qrcode.jpg";
                Log.d("TAG FILE", "-> " + filename);
                try {
                    FileOutputStream out = new FileOutputStream(filename);
                    qrCodeImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();

                    Log.d("TAG FILE","Saving File");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("TAG FILE", "Error");
                }


            }
        });

        return mView;
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
                    getActivity().onBackPressed();
                    return true;
                }
            }

        }

        return false;
    }
}






