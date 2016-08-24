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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import co.airbitz.core.Account;
import co.airbitz.core.Settings;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Transaction;
import co.airbitz.core.Wallet;

import java.util.List;
import java.util.LinkedList;

public class Constants {
    public static String PREFS = "co.airbitz.prefs";
    public static final String DAILY_LIMIT_PREF = "co.airbitz.spendinglimits.dailylimit";
    public static final String DAILY_LIMIT_SETTING_PREF = "co.airbitz.spendinglimits.dailylimitsetting";

    public static final String WALLET_LOADING_START_ACTION = "co.airbitz.notifications.wallets_data_loading_start";
    public static final String WALLET_CHANGED_ACTION = "co.airbitz.notifications.wallets_changed";
    public static final String WALLETS_ALL_LOADED_ACTION = "co.airbitz.notifications.all_wallets_data_loaded";
    public static final String WALLETS_RELOADED_ACTION = "co.airbitz.notifications.wallet_data_reloaded";

    public static final String BLOCKHEIGHT_CHANGE_ACTION = "co.airbitz.notifications.block_height_change";
    public static final String DATASYNC_UPDATE_ACTION = "com.airbitz.notifications.data_sync_update";
    public static final String EXCHANGE_RATE_UPDATED_ACTION = "co.airbitz.notifications.exchange_rate_update";
    public static final String INCOMING_BITCOIN_ACTION = "co.airbitz.notifications.incoming_bitcoin";
    public static final String REMOTE_PASSWORD_CHANGE_ACTION = "co.airbitz.notifications.remote_password_change";
    public static final String WALLET_SWEEP_ACTION = "co.airbitz.notifications.wallet_sweep_action";

    public static final String OTP_ERROR_ACTION = "com.airbitz.notifications.otp_error_action";
    public static final String OTP_RESET_ACTION = "com.airbitz.notifications.otp_reset_action";
    public static final String OTP_SKEW_ACTION = "com.airbitz.notifications.otp_skew_action";
    public static final String OTP_RESET_DATE = "com.airbitz.otp_reset_date";
    public static final String OTP_SECRET = "com.airbitz.otp_secret";

    public static final String AMOUNT_SWEPT = "co.airbitz.amount_swept";
    public static final String WALLETS_LOADED_TOTAL = "co.airbitz.wallets_loaded_total";
    public static final String WALLETS_TOTAL = "co.airbitz.wallets_total";
    public static final String WALLET_AMOUNT_SATOSHI = "com.airbitz.models.wallet.wallet_amount_satoshi";
    public static final String WALLET_NAME = "com.airbitz.models.wallet.wallet_name";
    public static final String WALLET_TXID = "co.airbitz.txid";
    public static final String WALLET_UUID = "co.airbitz.wallet_uuid";

    public static final String WALLET_FROM = "com.airbitz.WalletsFragment.FROM_SOURCE";
    public static final String WALLET_CREATE = "com.airbitz.WalletsFragment.CREATE";

    public static final String DIRECTORY_NAME_QUERY = "com.airbitz.DirectoryFragment.NAME_QUERY";
    public static final String DIRECTORY_CATEGORY_QUERY = "com.airbitz.DirectoryFragment.CATEGORY_QUERY";

    public static double MAX_SATOSHI = 9.223372036854775807E18; // = 0x7fffffffffffffff, but Java can't handle that.

    public static final long BIZ_ID_AIRBITZ = 3384;
    public static final long BIZ_ID_AMAZON = 11245;
    public static final long BIZ_ID_CLEVERCOIN = 10106;
    public static final long BIZ_ID_GLIDERA = 11063;
    public static final long BIZ_ID_HOME_DEPOT = 11141;
    public static final long BIZ_ID_PURSE = 5041;
    public static final long BIZ_ID_STARBUCKS = 11131;
    public static final long BIZ_ID_TARGET = 11132;
    public static final long BIZ_ID_WALMART = 11140;
    public static final long BIZ_ID_WHOLEFOODS = 11139;
    public static final long BIZ_ID_BITREFILL = 8498;

    public final static int EXPENSE_IDX = 0;
    public final static int INCOME_IDX = 1;
    public final static int TRANSFER_IDX = 2;
    public final static int EXCHANGE_IDX = 3;
    public final static String INCOME = "Income";
    public final static String EXPENSE = "Expense";
    public final static String TRANSFER = "Transfer";
    public final static String EXCHANGE = "Exchange";
    public final static String[] CATEGORIES = new String[] {
        EXPENSE, INCOME, TRANSFER, EXCHANGE
    };
}
