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

package com.airbitz.fragments.send;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import co.airbitz.core.Account;
import co.airbitz.core.SpendTarget;
import co.airbitz.core.Wallet;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.ScanFragment;

public class SendFragment extends ScanFragment {

    private final String TAG = getClass().getSimpleName();

    private final String FIRST_USAGE_COUNT = "com.airbitz.fragments.send.firstusagecount";
    private final String FIRST_BLE_USAGE_COUNT = "com.airbitz.fragments.send.firstusageblecount";

    private BitidLoginTask mBitidTask;
    private NewSpendTask mSpendTask;
    Handler mHandler = new Handler();

    public SendFragment() {
        super(RunMode.SEND);
    }

    @Override
    protected String getSubtitle() {
        return mActivity.getString(R.string.fragment_send_subtitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkFirstUsage();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBitidTask != null) {
            mBitidTask.cancel(true);
            mBitidTask = null;
        }
        if (mSpendTask != null) {
            mSpendTask.cancel(true);
            mSpendTask = null;
        }
    }

    @Override
    protected void launchHelp() {
        mActivity.pushFragment(
            new HelpFragment(HelpFragment.SEND),
            NavigationActivity.Tabs.SEND.ordinal());
    }

    public void checkFirstUsage() {
        new Thread(new Runnable() {
            public void run() {
                SharedPreferences prefs = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
                int count = prefs.getInt(FIRST_USAGE_COUNT, 1);
                if (count <= 2) {
                    count++;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(FIRST_USAGE_COUNT, count);
                    editor.apply();
                    notifyFirstUsage(getString(R.string.fragment_send_first_usage));
                } else {
                    count = prefs.getInt(FIRST_BLE_USAGE_COUNT, 1);
                    if (count <= 2) {
                        count++;
                        notifyFirstUsage(getString(R.string.fragment_send_first_usage_ble));
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(FIRST_BLE_USAGE_COUNT, count);
                        editor.apply();
                    }
                }
            }
        }).start();
    }

    private void notifyFirstUsage(final String message) {
        mHandler.post(new Runnable() {
            public void run() {
                mActivity.ShowFadingDialog(message,
                    getResources().getInteger(R.integer.alert_hold_time_help_popups));
            }
        });
    }

    @Override
    public void onCameraScanResult(String result) {
        Log.d(TAG, result);
        if (result != null) {
            showProcessing();
            processText(result);
        } else {
            showMessageAndStartCameraDialog(R.string.send_title, R.string.fragment_send_send_bitcoin_unscannable);
        }
    }

    @Override
    protected String getAddressDialogTitle() {
        return getResources().getString(R.string.fragment_send_address_dialog_title);
    }

    @Override
    protected void processText(String text) {
        String parsedUri = mAccount.parseBitidUri(text);
        if (!TextUtils.isEmpty(parsedUri)) {
            askBitidLogin(parsedUri, text);
        } else {
            Uri uri = Uri.parse(text);
            Log.d(TAG, uri.toString());
            if (uri != null && ("airbitz-ret".equals(uri.getScheme())
                        || "bitcoin-ret".equals(uri.getScheme())
                        || "x-callback-url".equals(uri.getHost()))) {
				mActivity.handleRequestForPaymentUri(uri);
            } else {
                mSpendTask = new NewSpendTask();
                mSpendTask.execute(text);
            }
        }
    }

    private void askBitidLogin(final String uri, final String text) {
        hideProcessing();
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.content(getString(R.string.bitid_login_message, uri))
               .title(R.string.bitid_login_title)
               .theme(Theme.LIGHT)
               .positiveText(getResources().getString(R.string.string_continue))
               .negativeText(getResources().getString(R.string.string_cancel))
               .cancelable(false)
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        showProcessing();
                        mBitidTask = new BitidLoginTask();
                        mBitidTask.execute(text);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        mQRCamera.startScanning();
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public class BitidLoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... text) {
            return mAccount.bitidLogin(text[0]);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                mActivity.ShowFadingDialog(
                    getString(R.string.bitid_login_success),
                    getResources().getInteger(R.integer.alert_hold_time_help_popups));
            } else {
                mActivity.ShowFadingDialog(
                    getString(R.string.bitid_login_failure),
                    getResources().getInteger(R.integer.alert_hold_time_help_popups));
            }
            hideProcessing();
            mQRCamera.startScanning();
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class NewSpendTask extends AsyncTask<String, Void, Boolean> {
        SpendTarget target;

        NewSpendTask() {
            target = mWallet.newSpendTarget();
        }

        @Override
        protected Boolean doInBackground(String... text) {
            return target.newSpend(text[0]);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                launchSendConfirmation(target);
            } else {
                showMessageAndStartCameraDialog(
                    R.string.fragment_send_failure_title,
                    R.string.fragment_send_confirmation_invalid_bitcoin_address);
                hideProcessing();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    protected void transferWalletSelected(Wallet w) {
        SpendTarget target = mWallet.newSpendTarget();
        target.newTransfer(w.id());
        launchSendConfirmation(target);
    }

    public void launchSendConfirmation(SpendTarget target) {
        hideProcessing();
        if (mWallet == null) {
            return;
        }
        SendConfirmationFragment fragment = new SendConfirmationFragment();
        fragment.setSpendTarget(target);
        Bundle bundle = new Bundle();
        bundle.putString(FROM_WALLET_UUID, mWallet.id());
        fragment.setArguments(bundle);
        if (mActivity != null) {
            mActivity.pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
        }
    }
}
