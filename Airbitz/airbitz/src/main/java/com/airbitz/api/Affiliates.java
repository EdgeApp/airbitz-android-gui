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

package com.airbitz.api;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.Affiliates;
import com.airbitz.api.directory.DirectoryApi;

import co.airbitz.core.Account;
import co.airbitz.core.DataStore;
import co.airbitz.core.ReceiveAddress;
import co.airbitz.core.Wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

public class Affiliates {

    private static String TAG = DirectoryApi.class.getSimpleName();

    private static final String SERVER_ROOT = "https://api.airbitz.co/";

    private static final String AFFILIATES_REGISTER = SERVER_ROOT + "affiliates/register";
    private static final String AFFILIATES_QUERY = SERVER_ROOT + "affiliates/query";
    private static final String AFFILIATES_TOUCH = SERVER_ROOT + "affiliates/";

    private static final String AFFILIATE_DATA_STORE = "affiliate_program";
    private static final String AFFILIATE_LINK_DATA = "affiliate_link";
    private static final String AFFILIATE_INFO_DATA = "affiliate_info";

    private static final String AFFILIATE_LOCAL_CHECKED = "co.airbitz.notifications.affiliate_checked";
    private static final String AFFILIATE_LOCAL_DATA = "co.airbitz.notifications.affiliate_data";

    private DirectoryApi mDirectory;
    private Account mAccount;
    private String mAffiliateUrl;
    private String mAffiliateInfo;

    public Affiliates() {
        mDirectory = DirectoryWrapper.getApi();
    }

    public Affiliates(Account account) {
        this();
        mAccount = account;

        DataStore store = account.data(AFFILIATE_DATA_STORE);
        mAffiliateUrl = store.get(AFFILIATE_LINK_DATA);
        mAffiliateInfo = store.get(AFFILIATE_INFO_DATA);
    }

    public void setupNewAccount() {
        DataStore store = mAccount.data(AFFILIATE_DATA_STORE);
        SharedPreferences prefs =
            AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        String data = prefs.getString(AFFILIATE_LOCAL_DATA, null);
        if (data != null && store.get(AFFILIATE_INFO_DATA) == null) {
            store.set(AFFILIATE_INFO_DATA, data);
        }
    }

    public String getAffiliateInfo() {
        return mAffiliateInfo;
    }


    /**
     * Returns a bitid URI to sign and post back
     */
    public String affiliateBitidUri() {
        String response = mDirectory.getRequest(AFFILIATES_REGISTER);
        Log.d(TAG, "" + response);
        try {
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("bitid_uri");
        } catch (JSONException e){
            Log.e(TAG, ""+e.getMessage());
        }
        return null;
    }

    /**
     * Returns an affiliate URL to share with friends
     */
    public String affiliateBitidRegister(Context context, String address, String signature, String uri, String paymentAddress) {
        try {
            Wallet currentWallet = mAccount.wallet(AirbitzApplication.getCurrentWallet());
            if (currentWallet == null) {
                return null;
            }
            ReceiveAddress request = currentWallet.newReceiveRequest();
            request.meta().name(context.getString(R.string.app_name))
                          .category(context.getString(R.string.affiliate_category))
                          .notes(context.getString(R.string.affiliate_notes));
            request.finalizeRequest();

            JSONObject body = new JSONObject();
            body.put("bitid_address", address);
            body.put("bitid_signature", signature);
            body.put("bitid_url", uri);
            body.put("payment_address", request.address());
            String response = mDirectory.postRequest(AFFILIATES_REGISTER, body.toString());
            Log.d(TAG, "" + response);

            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("affiliate_link");
        } catch (JSONException e){
            Log.e(TAG, "" + e.getMessage());
        }
        return null;
    }

    public String query() {
        return mDirectory.getRequest(AFFILIATES_QUERY);
    }

    public String affiliateCampaignUrl(Context context) {
        String bitidUri = affiliateBitidUri();
        if (null != bitidUri) {
            // affiliateBitidRegister
            Account.BitidSignature bitid = mAccount.bitidSign(bitidUri, bitidUri);
            return affiliateBitidRegister(context,bitid.address, bitid.signature, bitidUri, bitid.address);
        }
        return null;
    }

    public static class AffiliateQueryTask extends AsyncTask<Void, Void, String> {
        Affiliates affiliate;
        NavigationActivity activity;

        public AffiliateQueryTask(NavigationActivity activity) {
            this.affiliate = new Affiliates();
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... params) {
            SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
            if (!prefs.getBoolean(AFFILIATE_LOCAL_CHECKED, false)) {
                return affiliate.query();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String response) {
            SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(AFFILIATE_LOCAL_CHECKED, true);
            if (response != null) {
                editor.putString(AFFILIATE_LOCAL_DATA, response);
            }
            editor.apply();
        }
    }

    public static class AffiliateTask extends AsyncTask<Void, Void, String> {
        Affiliates affiliate;
        NavigationActivity activity;
        Account account;

        public AffiliateTask(NavigationActivity activity, Account account, Affiliates affiliate) {
            this.account = account;
            this.affiliate = affiliate;
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            activity.showModalProgress(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (affiliate.mAffiliateUrl == null) {
                String url = affiliate.affiliateCampaignUrl(activity);
                if (url != null) {
                    account.data(AFFILIATE_DATA_STORE).set(AFFILIATE_LINK_DATA, url);
                }
                return url;
            } else {
                return affiliate.mAffiliateUrl;
            }
        }

        @Override
        protected void onCancelled() {
            activity.showModalProgress(false);
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(final String url) {
            super.onPostExecute(url);
            activity.showModalProgress(false);

            if (url != null) {
                affiliate.mAffiliateUrl = url;

                final MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
                builder.content(String.format(
                        activity.getString(R.string.affiliate_refer_friends_body),
                            url,
                            activity.getString(R.string.app_name)))
                    .title(R.string.affiliate_refer_friends_title)
                    .theme(Theme.LIGHT)
                    .positiveText(activity.getString(R.string.string_share))
                    .neutralText(activity.getString(R.string.string_copy))
                    .negativeText(activity.getString(R.string.string_cancel))
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            share(activity, url);
                        }
                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            copy(activity, url);
                        }
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.cancel();
                        }
                    });
                builder.show();
            } else {
                Toast toast = Toast.makeText(activity, activity.getString(R.string.affiliate_link_error), Toast.LENGTH_LONG);
                toast.show();
            }
        }

        private void share(NavigationActivity activity, String url) {
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");

            String shareText = activity.getString(R.string.affiliate_link_share_text) + "\n\n" + url;

            share.putExtra(Intent.EXTRA_TEXT, shareText);
            activity.startActivity(Intent.createChooser(share,
                activity.getString(R.string.string_share)));
        }

        private void copy(NavigationActivity activity, String url) {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(activity.getString(R.string.affiliate_link), url);
            clipboard.setPrimaryClip(clip);
            activity.ShowFadingDialog(activity.getString(R.string.affiliate_link_copied));
        }
    }
}
