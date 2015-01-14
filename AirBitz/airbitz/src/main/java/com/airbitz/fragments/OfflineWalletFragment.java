/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.fragments;

import android.app.Fragment;
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
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created on 2/12/14.
 */
public class OfflineWalletFragment extends BaseFragment implements GestureDetector.OnGestureListener {

    private static String[] menus = {"Copy Public Address", "External Storage/Print"};
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
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
        if (null != parentViewGroup) {
            parentViewGroup.removeView(mView);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView != null)
            return mView;
        mView = inflater.inflate(R.layout.fragment_offline_wallet, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mRootLayout = (RelativeLayout) mView.findViewById(R.id.layout_root);
        mAddressTextView = (TextView) mView.findViewById(R.id.textview_address);

        Shader textShader = new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"), Color.parseColor("#addff1")},
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
        mBackButton = (ImageButton) mView.findViewById(R.id.layout_airbitz_header_button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.layout_airbitz_header_button_help);

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
                ClipData clip = ClipData.newPlainText("private key", mAddressField.getText().toString());
                clipboard.setPrimaryClip(clip);
            }
        });


        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap qrCodeImage = BitmapFactory.decodeResource(getResources(), R.drawable.img_qr_code);

                String dir = "";
                if (Environment.isExternalStorageEmulated()) {
                    dir = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DCIM;
                } else {
                    dir = Environment.getDataDirectory().toString() + "/" + Environment.DIRECTORY_DCIM;
                }
                File fDir = new File(dir);
                if (!fDir.exists()) {
                    fDir.mkdirs();
                }
                String filename = dir + "_qrcode.jpg";
                Log.d("TAG FILE", "-> " + filename);
                try {
                    FileOutputStream out = new FileOutputStream(filename);
                    qrCodeImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();

                    Log.d("TAG FILE", "Saving File");
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
        if (start != null & finish != null) {

            float yDistance = Math.abs(finish.getY() - start.getY());

            if ((finish.getRawX() > start.getRawX()) && (yDistance < 15)) {
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if (xDistance > 50) {
                    getActivity().onBackPressed();
                    return true;
                }
            }

        }

        return false;
    }
}






