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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.login.SignUpFragment;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.List;

public class RememberPasswordCheck {

    private NavigationActivity mActivity;
    private AirbitzCore mCoreAPI;
    private Account mAccount;
    private PasswordOkTask mPasswordTask;

    public RememberPasswordCheck(NavigationActivity activity) {
        this.mActivity = activity;
        this.mCoreAPI = AirbitzCore.getApi();
        this.mAccount = AirbitzApplication.getAccount();
    }

    public void showPasswordCheckAlert() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.alert_remember_password, null);

        final EditText input = (EditText) view.findViewById(R.id.password);
        input.setTransformationMethod(new PasswordTransformationMethod());
        input.setImeOptions(EditorInfo.IME_ACTION_GO);

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.password_check_remember_title))
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(mActivity.getResources().getString(R.string.password_check_check_my_password),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (null != mPasswordTask) {
                                    mPasswordTask.cancel(true);
                                    mPasswordTask = null;
                                }
                                String pw = input.getText().toString();
                                mPasswordTask = new PasswordOkTask();
                                mPasswordTask.execute(pw);
                            }
                        })
                .setNegativeButton(mActivity.getResources().getString(R.string.password_check_check_later),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                showPasswordCheckSkip();
                            }
                        });
        builder.create().show();
    }

    private void showPasswordCheckSkip() {
        mActivity.ShowFadingDialog(mActivity.getResources().getString(R.string.password_check_skip), mActivity.getResources().getInteger(R.integer.alert_hold_time_default), true);
    }

    private void showPasswordCheckChange() {
        Fragment fragment = new SignUpFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SignUpFragment.MODE, SignUpFragment.CHANGE_PASSWORD_NO_VERIFY);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);
    }

    private void handlePasswordResults(boolean correct) {
        if (correct) {

            mActivity.ShowFadingDialog(mActivity.getResources().getString(R.string.password_check_great_job), mActivity.getResources().getInteger(R.integer.alert_hold_time_default), true);
        } else {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mActivity);
            builder.setMessage(mActivity.getString(R.string.password_check_incorrect_password_message))
                    .setTitle(mActivity.getString(R.string.password_check_incorrect_password_title))
                    .setCancelable(false)
                    .setPositiveButton(mActivity.getResources().getString(R.string.string_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    showPasswordCheckAlert();
                                }
                            })
                    .setNeutralButton(mActivity.getResources().getString(R.string.string_change),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    showPasswordCheckChange();
                                }
                            })
                    .setNegativeButton(mActivity.getResources().getString(R.string.string_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    showPasswordCheckSkip();
                                }
                            });
            builder.create().show();
        }
    }

    public void onResume() {
    }

    public void onPause() {
        if (null != mPasswordTask) {
            mPasswordTask.cancel(true);
            mPasswordTask = null;
        }
    }

    public class PasswordOkTask extends AsyncTask<String, Void, Boolean> {

        PasswordOkTask() { }

        @Override
        protected void onPreExecute() {
            String msg = mActivity.getResources().getString(R.string.password_check_checking);
            int timeout = mActivity.getResources().getInteger(R.integer.alert_hold_time_default);
            mActivity.ShowFadingDialog(msg, timeout, false);
        }

        @Override
        protected Boolean doInBackground(String... passwords) {
            return mAccount.checkPassword(passwords[0]);
        }

        @Override
        protected void onPostExecute(final Boolean correct) {
            mActivity.DismissFadingDialog();
            handlePasswordResults(correct);
            mPasswordTask = null;
        }

        @Override
        protected void onCancelled() {
            mActivity.DismissFadingDialog();
            mPasswordTask = null;
        }
    }
}
