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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import com.airbitz.api.directory.DirectoryApi;
import co.airbitz.core.Account;

public class Affiliates {

    private static String TAG = DirectoryApi.class.getSimpleName();

    private static final String SERVER_ROOT = "https://api.airbitz.co/";

    private static final String AFFILIATES_REGISTER = SERVER_ROOT + "affiliates/register";
    private static final String AFFILIATES_QUERY = SERVER_ROOT + "affiliates/query";
    private static final String AFFILIATES_TOUCH = SERVER_ROOT + "affiliates/";

    private DirectoryApi mDirectory;
    private Account mAccount;

    public Affiliates(Account account) {
        mDirectory = DirectoryWrapper.getApi();
        mAccount = account;
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
    public String affiliateBitidRegister(String address, String signature, String uri, String paymentAddress) {
        try {
            JSONObject body = new JSONObject();
            body.put("bitid_address", address);
            body.put("bitid_signature", signature);
            body.put("bitid_url", uri);
            body.put("payment_address", paymentAddress);
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

    public String affiliateCampaignUrl() {
        String bitidUri = affiliateBitidUri();
        if (null != bitidUri) {
            // affiliateBitidRegister
            Account.BitidSignature bitid = mAccount.bitidSignature(bitidUri, bitidUri);
            return affiliateBitidRegister(bitid.address, bitid.signature, bitidUri, bitid.address);
        }
        return null;
    }
}
