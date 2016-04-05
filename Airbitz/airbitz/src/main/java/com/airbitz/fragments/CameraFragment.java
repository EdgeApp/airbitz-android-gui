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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.airbitz.R;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.objects.PictureCamera;

import java.io.ByteArrayOutputStream;

public class CameraFragment extends WalletBaseFragment {

    private final String TAG = getClass().getSimpleName();

    private Handler mHandler = new Handler();
    private Button mGalleryButton;
    private Button mTakePicture;
    private View mFlashButton;
    private RelativeLayout mCameraLayout;
    protected View mView;
    protected View mButtonBar;
    protected PictureCamera mCamera;
    protected View mResultFrame;
    protected ImageView mResultImage;
    protected Button mKeepPicture;
    protected Button mDiscardPicture;
    protected Bitmap mBitmap;

    public interface OnExitHandler {
        public void error();
        public void success(String encodedImage);
        public void back();
    }

    private OnExitHandler exitHandler;

    static final int MAX_WIDTH = 2000;
    static final int MAX_HEIGHT = 2000;

    protected Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, o);

            int scale = 1;
            if (o.outHeight > MAX_HEIGHT || o.outWidth > MAX_WIDTH) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(MAX_HEIGHT / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            o = new BitmapFactory.Options();
            o.inSampleSize = scale;

            final Bitmap picture  = BitmapFactory.decodeByteArray(data, 0, data.length, o);
            updateUi(picture);
        }
    };

    private void updateUi(final Bitmap bitmap) {
        mHandler.post(new Runnable() {
            public void run() {
                mCamera.stopCamera();
                mBitmap = bitmap;
                mResultImage.setImageBitmap(bitmap);
                mResultFrame.setVisibility(View.VISIBLE);
            }
        });
    }

    public CameraFragment() {
        setBackEnabled(true);
    }

    @Override
    protected String getSubtitle() {
        return mActivity.getString(R.string.fragment_take_picture);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_camera, container, false);
        mCameraLayout = (RelativeLayout) mView.findViewById(R.id.fragment_layout_camera);
        mCamera = new PictureCamera(this, mCameraLayout);

        mTakePicture = (Button) mView.findViewById(R.id.take_picture);
        mTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(mJpegCallback);
            }
        });

        mFlashButton = mView.findViewById(R.id.fragment_button_flash);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.setFlashOn(!mCamera.isFlashOn());
            }
        });

        mGalleryButton = (Button) mView.findViewById(R.id.fragment_button_photos);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in, PictureCamera.RESULT_LOAD_IMAGE);
            }
        });
        mButtonBar = mView.findViewById(R.id.fragment_buttons);

        mResultFrame = mView.findViewById(R.id.result_frame);
        mResultFrame.setVisibility(View.GONE);

        mResultImage = (ImageView) mView.findViewById(R.id.result_image);
        mKeepPicture = (Button) mView.findViewById(R.id.keep_picture);
        mKeepPicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendSuccess(mBitmap);
                mActivity.popFragment();
                mBitmap = null;
            }
        });

        mDiscardPicture = (Button) mView.findViewById(R.id.discard_picture);
        mDiscardPicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCamera.startCamera();
                mResultFrame.setVisibility(View.GONE);
                mBitmap = null;
            }
        });

        return mView;
    }

    private void sendSuccess(Bitmap bitmap) {
        if (null != exitHandler && null != bitmap) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, os);
            String imageEncoded = Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
            exitHandler.success(imageEncoded);
        }
    }

    public void setExitHandler(OnExitHandler handler) {
        this.exitHandler = handler;
    }

    @Override
    public boolean onBackPress() {
        if (null != exitHandler) {
            exitHandler.back();
        }
        mActivity.popFragment();
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PictureCamera.RESULT_LOAD_IMAGE
                && resultCode == Activity.RESULT_OK && null != data) {
            final Bitmap bitmap = mCamera.retrievePicture(data);
            mHandler.post(new Runnable() {
                public void run() {
                    mBitmap = bitmap;
                    sendSuccess(bitmap);
                    mActivity.popFragment();
                    mBitmap = null;
                }
            });
        }
    }

    @Override
    protected float getFabTop() {
        return mActivity.getFabTop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCamera.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stopCamera();
    }
}
