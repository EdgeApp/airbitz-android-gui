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

package com.airbitz.fragments.settings.twofactor;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.airbitz.core.AirbitzException;
import co.airbitz.core.AirbitzCore;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.QRCamera;

public class TwoFactorScanFragment extends BaseFragment implements
        QRCamera.OnScanResult {
    private final String TAG = getClass().getSimpleName();

    public static String STORE_SECRET = "com.airbitz.twofactorscan.storesecret";
    public static String TEST_SECRET = "com.airbitz.twofactorscan.testsecret";
    public static String USERNAME = "com.airbitz.twofactorscan.username";

    QRCamera mQRCamera;
    RelativeLayout mCameraLayout;
    private TextView mTitleTextView;

    boolean mSuccess = false;
    boolean mStoreSecret = false;
    boolean mTestSecret = false;
    String mUsername;
    String mSecret;

    //************** Callback for notification of two factor results
    OnTwoFactorQRScanResult mOnTwoFactorQRScanResult;
    public interface OnTwoFactorQRScanResult {
        public void onTwoFactorQRScanResult(boolean success, String result);
    }
    public void setOnTwoFactorQRScanResult(OnTwoFactorQRScanResult listener) {
        mOnTwoFactorQRScanResult = listener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public String getTitle() {
        return mActivity.getString(R.string.fragment_twofactor_scan_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        View mView = i.inflate(R.layout.fragment_twofactor_scan, container, false);

        mCameraLayout = (RelativeLayout) mView.findViewById(R.id.fragment_twofactor_scan_camera_layout);
        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle bundle = getArguments();
        mStoreSecret = bundle.getBoolean(STORE_SECRET, false);
        mTestSecret = bundle.getBoolean(TEST_SECRET, false);
        mUsername = bundle.getString(USERNAME);

        if(mQRCamera == null) {
            mQRCamera = new QRCamera(this, mCameraLayout);
            mQRCamera.setOnScanResultListener(this);
            mQRCamera.startCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void onScanResult(String result) {
        if(mOnTwoFactorQRScanResult != null) {
            mOnTwoFactorQRScanResult.onTwoFactorQRScanResult(processResult(result), result);
        }
    }

    void exit() {
        mActivity.onBackPressed();
    }

    boolean processResult(String secret)
    {
        if (secret != null && !secret.isEmpty()) {
            if (mStoreSecret) {
                mSuccess = storeSecret(secret);
            } else {
                mSuccess = true;
            }
            if (mTestSecret) {
                testSecret();
            } else {
                exit();
            }
            return true;
        } else {
            return false;
        }
    }

    boolean storeSecret(String secret) {
        mSecret = secret;
        return true;
    }

    void testSecret() {
        if (true) {
            exit();
        } else {
            ShowTryAgainDialog(getString(R.string.fragment_two_factor_scan_unable_import_title),
                    getString(R.string.fragment_two_factor_scan_unable_import_message));
        }
    }

    public void ShowTryAgainDialog(String title, String message) {
        if (!mActivity.isFinishing()) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(mActivity, R.style.AlertDialogCustom));
            builder.setMessage(message)
                    .setTitle(title)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.fragment_two_factor_scan_try_again),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(getString(R.string.fragment_two_factor_scan_no_thanks),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    exit();
                                }
                            });
            builder.create().show();
        }
    }
}
