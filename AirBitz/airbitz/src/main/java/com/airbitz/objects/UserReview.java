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

import android.content.Context;
import android.content.SharedPreferences;

import com.airbitz.AirbitzApplication;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;

import java.util.List;

public class UserReview {
    private static int MAX_LOGINS = 7;
    private static int MAX_TRANSACTIONS = 7;
    private static long MAX_TIME_DIFFERENCE_MILLIS = 14 * 24 *60 * 60 * 1000;

    private static String LOGIN_COUNT = "com.airbitz.userreview.logincount";
    private static String FIRST_LOGIN_TIME = "com.airbitz.userreview.firstlogintime";
    private static String ALREADY_NOTIFIED = "com.airbitz.userreview.alreadynotified";

    static SharedPreferences mPrefs;
    static SharedPreferences.Editor mEditor;
    private static void setupPrefs() {
        mPrefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
    }

    public static boolean checkUserReview() {
        setupPrefs();
        boolean notified = mPrefs.getBoolean(ALREADY_NOTIFIED, false);
        if(! notified && loginCountTriggered() || transactionCountTriggered() || timeUseTriggered()) {
            return true;
        }
        return false;
    }

    public static boolean loginCountTriggered() {
        boolean ret = false;
        int count = mPrefs.getInt(LOGIN_COUNT, 1);
        if(count != Integer.MAX_VALUE) {
            if(count++ > MAX_LOGINS) {
                count = Integer.MAX_VALUE;
                ret = true;
            }
            mEditor.putInt(LOGIN_COUNT, count);
            mEditor.apply();
        }
        return ret;
    }

    public static boolean transactionCountTriggered() {
        if(AirbitzApplication.isLoggedIn()) {
            List<Wallet> walletList = CoreAPI.getApi().getCoreWallets(true);
            int transactionCount = 0;
            for(Wallet wallet : walletList) {
                if(!wallet.isArchiveHeader() && !wallet.isHeader()) {
                    List<Transaction> transactions = wallet.getTransactions();
                    transactionCount += transactions.size();
                }
            }
            return transactionCount > MAX_TRANSACTIONS;
        }
        else {
            return false;
        }
    }

    public static boolean timeUseTriggered() {
        if(mPrefs.contains(FIRST_LOGIN_TIME)) {
            long appFirstLaunch = mPrefs.getLong(FIRST_LOGIN_TIME, 0);
            if(appFirstLaunch != 0 && System.currentTimeMillis() > appFirstLaunch + MAX_TIME_DIFFERENCE_MILLIS) {
                mEditor.putLong(FIRST_LOGIN_TIME, 0);
                mEditor.apply();
                return true;
            }
            else {
                return false;
            }
        }
        else {
            mEditor.putLong(FIRST_LOGIN_TIME, System.currentTimeMillis());
            mEditor.apply();
            return false;
        }
    }
}