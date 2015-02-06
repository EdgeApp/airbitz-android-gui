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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.QRCamera;
import com.airbitz.utils.Common;

/**
 * Two Factor Authentication Scan
 * Created 2/6/15
 */
public class TwoFactorScanFragment extends BaseFragment implements
        QRCamera.OnScanResult {
    private final String TAG = getClass().getSimpleName();

    private HighlightOnPressImageButton mHelpButton, mBackButton;
    QRCamera mQRCamera;
    RelativeLayout mCameraLayout;
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_twofactor_scan, container, false);

//        ImageButton mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
//        mBackButton.setVisibility(View.VISIBLE);
//        mBackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getActivity().onBackPressed();
//            }
//        });

//        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
//        mHelpButton.setVisibility(View.VISIBLE);
//        mHelpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mActivity.pushFragment(new HelpFragment(HelpFragment.SPEND_LIMITS), NavigationActivity.Tabs.SETTING.ordinal());
//            }
//        });

        mCameraLayout = (RelativeLayout) mView.findViewById(R.id.fragment_twofactor_scan_camera_layout);
        return mView;
    }

    @Override
    public void onResume() {
        if(mQRCamera == null) {
            mQRCamera = new QRCamera(mActivity, mCameraLayout);
            mQRCamera.setOnScanResultListener(this);
            mQRCamera.startCamera();
        }
    }

    @Override
    public void onPause() {
        if(mQRCamera != null) {
            mQRCamera.stopCamera();
            mQRCamera.setOnScanResultListener(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mQRCamera != null && requestCode == QRCamera.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            mQRCamera.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onScanResult(String info) {

    }
}
