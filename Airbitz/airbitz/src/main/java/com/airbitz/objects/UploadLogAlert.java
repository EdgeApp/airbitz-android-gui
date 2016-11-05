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

package com.airbitz.objects;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import co.airbitz.core.AirbitzCore;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.airbitz.AirbitzApplication;
import com.airbitz.BuildConfig;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.login.SignUpFragment;
import com.airbitz.utils.AccountDump;

public class UploadLogAlert {

    private NavigationActivity mActivity;
    private AirbitzCore mCoreAPI;
    private UploadLogsTask mUploadLogsTask;

    public UploadLogAlert(NavigationActivity activity) {
        this.mActivity = activity;
        this.mCoreAPI = AirbitzCore.getApi();
    }

    public void showUploadLogAlert() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.alert_upload_log, null);

        final EditText input = (EditText) view.findViewById(R.id.user_message);

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.upload_log_alert_title))
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(mActivity.getResources().getString(R.string.upload_log_alert_upload_logs),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (null != mUploadLogsTask) {
                                    mUploadLogsTask.cancel(true);
                                    mUploadLogsTask = null;
                                }
                                String pw = input.getText().toString();
                                mUploadLogsTask = new UploadLogsTask();
                                mUploadLogsTask.execute(pw);
                            }
                        })
                .setNeutralButton(mActivity.getResources().getString(R.string.string_bundle),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AccountDump.shareAccountData(mActivity);
                            }
                        })
                .setNegativeButton(mActivity.getResources().getString(R.string.string_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                showUploadLogsSkip();
                            }
                        });
        builder.create().show();
    }

    private void showUploadLogsSkip() {
        mActivity.ShowFadingDialog(mActivity.getResources().getString(R.string.upload_log_alert_skip), mActivity.getResources().getInteger(R.integer.alert_hold_time_default), true);
    }

    public void onResume() {
    }

    public void onPause() {
        if (null != mUploadLogsTask) {
            mUploadLogsTask.cancel(true);
            mUploadLogsTask = null;
        }
    }

    public class UploadLogsTask extends AsyncTask<String, Void, Boolean> {

        UploadLogsTask() { }

        @Override
        protected void onPreExecute() {
            mActivity.ShowFadingDialog(
                    mActivity.getResources().getString(R.string.upload_log_alert_uploading),
                    mActivity.getResources().getInteger(R.integer.alert_hold_time_forever), false);
        }

        @Override
        protected Boolean doInBackground(String... usermsg) {
            AirbitzCore.logi(usermsg[0]);
            int versionCode = com.airbitz.BuildConfig.VERSION_CODE;
            String versionName = com.airbitz.BuildConfig.VERSION_NAME;
            String appVersion = versionName + " (" + Integer.toString(versionCode) + ")";

            AirbitzCore.loge("App Version:" + appVersion);
            return mCoreAPI.uploadLogs();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mActivity != null) {
                if (success) {
                    mActivity.ShowFadingDialog(mActivity.getResources().getString(R.string.upload_log_alert_succeeded));
                } else {
                    mActivity.ShowFadingDialog(mActivity.getResources().getString(R.string.upload_log_alert_failed));
                }
            }
        }

        @Override
        protected void onCancelled() {
            mActivity.ShowFadingDialog(mActivity.getResources().getString(R.string.upload_log_alert_skip));
        }
    }
}
