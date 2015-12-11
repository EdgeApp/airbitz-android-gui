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

package com.airbitz.fragments.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.DirectoryWrapper;
import com.airbitz.api.directory.DirectoryApi;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.ScanFragment;
import com.airbitz.models.Wallet;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.json.JSONException;
import org.json.JSONObject;

public class ImportFragment extends ScanFragment {

    public static String URI = "com.airbitz.importfragment.uri";

    private final String TAG = getClass().getSimpleName();

    private Handler mHandler = new Handler();
    private MaterialDialog mDialog;

    private String mTweet, mToken, mMessage, mZeroMessage;
    String mSweptID;
    long mSweptAmount = -1;
    private String mSweptAddress;

    Runnable sweepNotFoundRunner = new Runnable() {
        @Override
        public void run() {
            showBusyLayout(null, false);
            if (mQRCamera != null) {
                mQRCamera.startScanning();
            }
            if (isVisible()) {
                clearSweepAddress();
                mSweptAmount = -1;
                mActivity.ShowOkMessageDialog(getString(R.string.import_wallet_swept_funds_title),
                    getString(R.string.import_wallet_timeout_message));
            }
        }
    };

    public ImportFragment() {
        super(RunMode.IMPORT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getSubtitle() {
        return mActivity.getString(R.string.import_title);
    }

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

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity())
            .registerReceiver(mSweepReceiver, new IntentFilter(CoreAPI.WALLET_SWEEP_ACTION));

        Bundle args = getArguments();
        if (args != null && args.getString(URI) != null
                && getHiddenBitsToken(args.getString(URI)) != null) {
            mSweptAddress = args.getString(URI);
            showAddressDialog();
        }
        clearSweepAddress();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSweepReceiver);

        showBusyLayout(null, false);
        mHandler.removeCallbacks(sweepNotFoundRunner);
    }

    @Override
    public void onCameraScanResult(String result) {
        Log.d(TAG, "OnScanResult " + result);
        if (result != null) {
            Log.d(TAG, "HiddenBits found");
            processText(result);
        } else {
            showMessageAndStartCameraDialog(R.string.import_title, R.string.fragment_send_send_bitcoin_unscannable);
        }
    }

    @Override
    protected void processText(String uriString) {
        String token = getHiddenBitsToken(uriString);
        String entry = token != null ? token : uriString;

        showBusyLayout(entry, true);
        mSweptAddress = mCoreApi.SweepKey(mWallet.getUUID(), entry);

        if (mSweptAddress != null && !mSweptAddress.isEmpty()) {
            mHandler.postDelayed(sweepNotFoundRunner, 30000);

            if(token != null) { // also issue hidden bits
                int hBitzIDLength = 4;
                if(mSweptAddress.length() >= hBitzIDLength) {
                    String lastFourChars = mSweptAddress.substring(mSweptAddress.length() - hBitzIDLength, mSweptAddress.length());
                    HiddenBitsApiTask task = new HiddenBitsApiTask();
                    task.execute(lastFourChars);
                }
                else {
                    Log.d(TAG, "HiddenBits token error");
                }
            }
        } else {
            showBusyLayout(null, false);
            showMessageAndStartCameraDialog(R.string.import_title, R.string.import_wallet_private_key_invalid);
        }
    }

    // Returns null if not a HiddenBits token
    public static String getHiddenBitsToken(String uriIn) {
        final String HBITS_SCHEME = "hbits";
        if(uriIn == null) {
            return null;
        }

        Uri uri = Uri.parse(uriIn);
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equalsIgnoreCase(HBITS_SCHEME)) {
            Log.d("ImportFragment", "Good HiddenBits URI");
            return uri.toString().substring(scheme.length()+3);
        } else {
            Log.d("ImportFragment", "HiddenBits failed for: " + uriIn);
            return null;
        }
    }

    public class HiddenBitsApiTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Getting HiddenBits API response");
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
            Log.d(TAG, "Got HiddenBits API response: " + result);

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
            String txID = intent.getStringExtra(CoreAPI.WALLET_TXID);
            long amount = intent.getLongExtra(CoreAPI.AMOUNT_SWEPT, 0);
            Log.d(TAG, "OnWalletSweep called with ID:" + txID + " and satoshis:" + amount);

            showBusyLayout(null, false);

            mSweptID = txID;
            mSweptAmount = amount;

            String token = getHiddenBitsToken(mSweptAddress);
            if(token != null) { // hidden bitz
                // Check to see if both paths are done
                checkHiddenBitsAsyncData();
                return;
            }

            // if a private address sweep
            mHandler.removeCallbacks(sweepNotFoundRunner);

            clearSweepAddress();
            mActivity.showPrivateKeySweepTransaction(mSweptID, mWallet.getUUID(), mSweptAmount);
            mSweptAmount = -1;
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
            Log.d(TAG, "Both API and OnWalletSweep are finished");

            mHandler.removeCallbacks(sweepNotFoundRunner);
            showBusyLayout(null, false);

            mActivity.showHiddenBitsTransaction(mSweptID, mWallet.getUUID(), mSweptAmount,
                    mMessage, mZeroMessage, mTweet);

            mSweptAmount = -1;
        }
    }

    @Override
    protected String getAddressDialogTitle() {
        return getResources().getString(R.string.fragment_import_address_dialog_title);
    }

    public void showMessageAndStartCameraDialog(int title, int message) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.content(message)
               .title(title)
               .cancelable(false)
               .theme(Theme.LIGHT)
               .neutralText(getResources().getString(R.string.string_ok))
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        if (mQRCamera != null) {
                            mQRCamera.startScanning();
                        }
                        dialog.cancel();
                    }
               });
        builder.show();
    }

    @Override
    public void launchHelp() {
        mActivity.pushFragment(
            new HelpFragment(HelpFragment.IMPORT_WALLET),
                NavigationActivity.Tabs.IMPORT.ordinal());
    }

    @Override
    protected void transferWalletSelected(Wallet w) {
        // Not applicable in Import mode
    }
}
