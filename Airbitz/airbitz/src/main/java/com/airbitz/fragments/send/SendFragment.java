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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import co.airbitz.core.Account;
import co.airbitz.core.Account.EdgeLoginInfo;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.ParsedUri;
import co.airbitz.core.PaymentRequest;
import co.airbitz.core.Transaction;
import co.airbitz.core.Wallet;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.Constants;
import com.airbitz.api.DirectoryWrapper;
import com.airbitz.api.directory.DirectoryApi;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.ScanFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class SendFragment extends ScanFragment {

    private final String TAG = getClass().getSimpleName();

    private final String FIRST_USAGE_COUNT = "com.airbitz.fragments.send.firstusagecount";
    private final String FIRST_BLE_USAGE_COUNT = "com.airbitz.fragments.send.firstusageblecount";

    private BitidLoginTask mBitidTask;
    private PaymentProtoFetch mPaymentTask;
    private EdgeLoginTask mEdgeLoginTask;
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

        LocalBroadcastManager.getInstance(getActivity())
            .registerReceiver(mSweepReceiver, new IntentFilter(Constants.WALLET_SWEEP_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSweepReceiver);
        if (mBitidTask != null) {
            mBitidTask.cancel(true);
            mBitidTask = null;
        }
        if (mPaymentTask != null) {
            mPaymentTask.cancel(true);
            mPaymentTask = null;
        }
        if (mEdgeLoginTask != null) {
            mEdgeLoginTask.cancel(true);
            mEdgeLoginTask = null;
        }
        mHandler.removeCallbacks(sweepNotFoundRunner);
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
        hideProcessing();
        try {
            if (null != text && text.length() == 8 && isBase32(text)) {
                showProcessing();
                mEdgeLoginTask = new EdgeLoginTask();
                mEdgeLoginTask.execute(text);
                return;
            }
            ParsedUri parsed = AirbitzCore.getApi().parseUri(text);
            if (parsed.type() != null) {
                switch (parsed.type()) {
                case BITID:
                    launchSingleSignonFragment(parsed, null);
                    return;
                case PAYMENT_PROTO:
                    showProcessing();
                    mPaymentTask = new PaymentProtoFetch(parsed);
                    mPaymentTask.execute();
                    return;
                case ADDRESS:
                    launchSendConfirmation(parsed, null, null);
                    return;
                case PRIVATE_KEY:
                    askImportOrSend(parsed);
                    return;
                default:
                    break;
                }
            }
        } catch (AirbitzException e) {
            AirbitzCore.loge(e.getMessage());
        }
        Uri uri = Uri.parse(text);
        Log.d(TAG, uri.toString());
        if (uri != null && ("airbitz-ret".equals(uri.getScheme())
                    || "bitcoin-ret".equals(uri.getScheme())
                    || "x-callback-url".equals(uri.getHost()))) {
            mActivity.handleRequestForPaymentUri(uri);
        } else {
            showMessageAndStartCameraDialog(
                R.string.fragment_send_failure_title,
                R.string.fragment_send_confirmation_invalid_bitcoin_address);
        }
    }

    private void askImportOrSend(final ParsedUri parsed) {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.content(parsed.address())
               .title(R.string.fragment_send_import_or_send_title)
               .theme(Theme.LIGHT)
               .positiveText(getResources().getString(R.string.fragment_send_import_or_send_import_funds))
               .neutralText(getResources().getString(R.string.string_cancel))
               .negativeText(getResources().getString(R.string.fragment_send_import_or_send_send_funds))
               .cancelable(false)
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        importKey(parsed);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        launchSendConfirmation(parsed, null, null);
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
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

    public class PaymentProtoFetch extends AsyncTask<Void, Void, Boolean> {
        ParsedUri mParsedUri;
        PaymentRequest mRequest;
        PaymentProtoFetch(ParsedUri uri) {
            mParsedUri = uri;
        }

        @Override
        public void onPreExecute() {
            showProcessing();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mRequest = mParsedUri.fetchPaymentRequest();
                return true;
            } catch (AirbitzException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                launchSendConfirmation(mParsedUri, mRequest, null);
            } else {
                if (mParsedUri.address() != null &&
                        mParsedUri.address().length() > 5) {
                    launchSendConfirmation(mParsedUri, null, null);
                } else {
                    showMessageAndStartCameraDialog(
                            R.string.fragment_send_failure_title,
                            R.string.fragment_send_confirmation_invalid_bitcoin_address);
                }
            }
            hideProcessing();
        }

        @Override
        protected void onCancelled() {
            hideProcessing();
        }
    }

    public class EdgeLoginTask extends AsyncTask<String, Void, Boolean> {
        EdgeLoginInfo mEdgeInfo;

        @Override
        public void onPreExecute() {
            showProcessing();
        }

        @Override
        protected Boolean doInBackground(String... text) {
            try {
                mEdgeInfo = mAccount.getEdgeLoginRequest(text[0]);
                return true;
            } catch (AirbitzException e) {
                AirbitzCore.loge(e.toString());
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                launchSingleSignonFragment(null, mEdgeInfo);
            } else {
                showMessageAndStartCameraDialog(
                        R.string.fragment_send_failure_title,
                        R.string.invalid_edge_login_request);
            }
            hideProcessing();
        }

        @Override
        protected void onCancelled() {
            hideProcessing();
        }
    }

    protected void transferWalletSelected(Wallet destWallet) {
        launchSendConfirmation(null, null, destWallet);
    }

    public void launchSendConfirmation(ParsedUri parsedUri, PaymentRequest request, Wallet destWallet) {
        hideProcessing();
        if (mWallet == null) {
            return;
        }
        SendConfirmationFragment fragment = new SendConfirmationFragment();
        fragment.setParsedUri(parsedUri);
        fragment.setPaymentRequest(request);
        fragment.setDestWallet(destWallet);

        Bundle bundle = new Bundle();
        bundle.putString(FROM_WALLET_UUID, mWallet.id());
        fragment.setArguments(bundle);
        if (mActivity != null) {
            mActivity.pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
        }
    }

    public void launchSingleSignonFragment(ParsedUri parsedUri, EdgeLoginInfo loginInfo) {
        hideProcessing();
        SingleSignOnFragment fragment = new SingleSignOnFragment();
        fragment.setEdgeLoginInfo(loginInfo);
        fragment.setParsedUri(parsedUri);
        fragment.setArguments(new Bundle());
        if (mActivity != null) {
            mActivity.pushFragment(fragment, NavigationActivity.Tabs.SEND.ordinal());
        }
    }

    protected void importKey(ParsedUri uri) {
        mSweptAddress = uri.address();
        showBusyLayout(mSweptAddress, true);
        try {
            mWallet.sweepKey(uri.privateKey());
            if (!TextUtils.isEmpty(mSweptAddress)) {
                mHandler.postDelayed(sweepNotFoundRunner, 30000);

                if (mSweptAddress != null) {
                    int hBitzIDLength = 4;
                    if (mSweptAddress.length() >= hBitzIDLength) {
                        String lastFourChars = mSweptAddress.substring(mSweptAddress.length() - hBitzIDLength, mSweptAddress.length());
                        HiddenBitsApiTask task = new HiddenBitsApiTask();
                        task.execute(lastFourChars);
                    } else {
                        AirbitzCore.logi("HiddenBits token error");
                    }
                }
            }
        } catch (AirbitzException e) {
            showBusyLayout(null, false);
            showMessageAndStartCameraDialog(R.string.import_title, R.string.import_wallet_private_key_invalid);
        }
    }

    public class HiddenBitsApiTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            AirbitzCore.logi("Getting HiddenBits API response");
        }

        @Override
        protected String doInBackground(String... params) {
            DirectoryApi api = DirectoryWrapper.getApi();
            return api.getHiddenBits(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                return;
            }
            AirbitzCore.logi("Got HiddenBits API response: " + result);

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                mTweet = jsonObject.getString("tweet");
                mToken = jsonObject.getString("token");
                mZeroMessage = jsonObject.getString("zero_message");
                mMessage = jsonObject.getString("message");

                // Check to see if both paths are done
                checkHiddenBitsAsyncData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            showBusyLayout(null, false);
        }
    }

    BroadcastReceiver mSweepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String txId = intent.getStringExtra(Constants.WALLET_TXID);
            long amount = intent.getLongExtra(Constants.AMOUNT_SWEPT, 0);

            AirbitzCore.logi("OnWalletSweep called with ID:" + txId + " and satoshis:" + amount);
            showBusyLayout(null, false);

            Transaction tx = null;
            if (!TextUtils.isEmpty(txId)) {
                tx = mWallet.transaction(txId);
            }
            mSweptAmount = amount;
            mSweptID = txId;

            // if a private address sweep
            mHandler.removeCallbacks(sweepNotFoundRunner);
            if (mTweet != null) {
                checkHiddenBitsAsyncData();
            } else {
                clearSweepAddress();
                mActivity.showPrivateKeySweepTransaction(mSweptID, mWallet.id(), mSweptAmount);
                mSweptAmount = -1;
            }
        }
    };

    private void clearSweepAddress() {
        // Clear out sweep info
        mSweptAddress = "";
    }

    // This is only called for HiddenBits
    private void checkHiddenBitsAsyncData() {
        // both async paths are finished if both of these are not empty
        if (mSweptAmount != -1 && mTweet != null) {
            AirbitzCore.logi("Both API and OnWalletSweep are finished");

            mHandler.removeCallbacks(sweepNotFoundRunner);
            showBusyLayout(null, false);

            mActivity.showHiddenBitsTransaction(mSweptID, mWallet.id(), mSweptAmount,
                    mMessage, mZeroMessage, mTweet);

            mSweptAmount = -1;
        }
    }

    private String mTweet, mToken, mMessage, mZeroMessage;
    String mSweptID;
    long mSweptAmount = -1;
    private String mSweptAddress;
    private String mSweepAddress;
    private MaterialDialog mDialog;

    Runnable sweepNotFoundRunner = new Runnable() {
        @Override
        public void run() {
            showBusyLayout(null, false);
            if (mQRCamera != null) {
                mQRCamera.startScanning();
            }
            if (isVisible()) {
                mSweptAmount = 0;
                if (mTweet != null) {
                    checkHiddenBitsAsyncData();
                } else {
                    mActivity.ShowOkMessageDialog(getString(R.string.import_wallet_swept_funds_title),
                                            getString(R.string.import_wallet_timeout_message));
                }
                mSweptAmount = -1;
            }
        }
    };

    private void showBusyLayout(String address, boolean on) {
        if(on) {
            MaterialDialog.Builder builder =
                new MaterialDialog.Builder(mActivity)
                        .content(String.format(getString(R.string.import_wallet_busy_text), address))
                        .cancelable(false)
                        .progress(true, 0)
                        .progressIndeterminateStyle(false);
            mDialog = builder.build();
            mDialog.show();
            if (mQRCamera != null) {
                mQRCamera.stopScanning();
            }
        } else {
            if (null != mDialog) {
                mDialog.dismiss();
            }
        }
    }

    private static boolean isBase32(String string) {
        Pattern p = Pattern.compile("^[A-Z2-7]+$");
        Matcher m = p.matcher(string);
        return m.matches();
    }
}
