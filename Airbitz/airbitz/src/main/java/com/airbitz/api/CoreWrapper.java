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
import co.airbitz.core.AccountSettings;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Transaction;
import co.airbitz.core.Wallet;

import java.util.List;
import java.util.LinkedList;

public class CoreWrapper {
    public static void setupAccount(Context context, Account account) {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        account.setCallbacks(new Account.Callbacks() {
            public void userRemotePasswordChange() {
                manager.sendBroadcast(new Intent(Constants.REMOTE_PASSWORD_CHANGE_ACTION));
            }
            public void userLoggedOut() {
            }
            public void userAccountChanged() {
                manager.sendBroadcast(new Intent(Constants.DATASYNC_UPDATE_ACTION));
            }
            public void userWalletsLoading() {
                manager.sendBroadcast(new Intent(Constants.WALLET_LOADING_START_ACTION));
            }
            public void userWalletStatusChange(int loaded, int total) {
                Intent intent = new Intent(Constants.WALLET_LOADING_STATUS_ACTION);
                intent.putExtra(Constants.WALLETS_LOADED_TOTAL, loaded);
                intent.putExtra(Constants.WALLETS_TOTAL, total);
                manager.sendBroadcast(intent);
            }
            public void userWalletsLoaded() {
                manager.sendBroadcast(
                    new Intent(Constants.WALLETS_ALL_LOADED_ACTION));
            }
            public void userWalletsChanged() {
                manager.sendBroadcast(new Intent(Constants.WALLETS_RELOADED_ACTION));
            }
            public void userOTPRequired(String secret) {
                Intent intent = new Intent(Constants.OTP_ERROR_ACTION);
                intent.putExtra(Constants.OTP_SECRET, secret);
                manager.sendBroadcast(intent);
            }
            public void userOtpResetPending() {
                manager.sendBroadcast(new Intent(Constants.OTP_RESET_ACTION));
            }
            public void userExchangeRateChanged() {
                manager.sendBroadcast(new Intent(Constants.EXCHANGE_RATE_UPDATED_ACTION));
            }
            public void userBlockHeightChanged() {
                manager.sendBroadcast(new Intent(Constants.BLOCKHEIGHT_CHANGE_ACTION));
            }
            public void userBalanceUpdate() {
            }
            public void userIncomingBitcoin(Wallet wallet, Transaction transaction) {
                Intent intent = new Intent(Constants.INCOMING_BITCOIN_ACTION);
                intent.putExtra(Constants.WALLET_UUID, wallet.getUUID());
                intent.putExtra(Constants.WALLET_TXID, transaction.getID());
            }
            public void userSweep(Wallet wallet, Transaction transaction) {
                Intent intent = new Intent(Constants.WALLET_SWEEP_ACTION);
                intent.putExtra(Constants.WALLET_UUID, wallet.getUUID());
                intent.putExtra(Constants.WALLET_TXID, transaction.getID());
                intent.putExtra(Constants.AMOUNT_SWEPT, transaction.getBalance());
                manager.sendBroadcast(intent);
            }

            public void userBitcoinLoading() {
                manager.sendBroadcast(new Intent(Constants.WALLETS_LOADING_BITCOIN_ACTION));
            }

            public void userBitcoinLoaded() {
                manager.sendBroadcast(new Intent(Constants.WALLETS_LOADED_BITCOIN_ACTION));
            }
        });
    }

    public static List<WalletWrapper> wrap(List<Wallet> wallets) {
        List<WalletWrapper> wrapped = new LinkedList<WalletWrapper>();
        for (Wallet w : wallets) {
            wrapped.add(new WalletWrapper(w));
        }
        return wrapped;
    }

    public static List<Wallet> unwrap(List<WalletWrapper> wallets) {
        boolean archived = false;
        List<Wallet> unwrapper = new LinkedList<Wallet>();
        for (WalletWrapper w : wallets) {
            if (w.isArchiveHeader()) {
                archived = true;
            } else if (w.isHeader()) {
                archived = false;
            } else {
                Wallet wallet = w.wallet();
                wallet.walletArchived(archived);
                unwrapper.add(wallet);
            }
        }
        return unwrapper;
    }

    public static boolean getDailySpendLimitSetting(Context context, Account account) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        if (prefs.contains(Constants.DAILY_LIMIT_SETTING_PREF + account.getUsername())) {
            return prefs.getBoolean(Constants.DAILY_LIMIT_SETTING_PREF + account.getUsername(), true);
        } else {
            AccountSettings settings = account.settings();
            if (settings != null) {
                return settings.getBDailySpendLimit();
            }
            return false;
        }
    }

    public static void setDailySpendLimitSetting(Context context, Account account, boolean set) {
        AccountSettings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.setBDailySpendLimit(set);
        try {
            settings.save();

            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.DAILY_LIMIT_SETTING_PREF + account.getUsername(), set);
            editor.apply();
        } catch (AirbitzException e) {
            AirbitzCore.debugLevel(1, "SetDailySpendLimitSetting error:");
        }
    }

   public static long getDailySpendLimit(Context context, Account account) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        if (prefs.contains(Constants.DAILY_LIMIT_PREF + account.getUsername())) {
            return prefs.getLong(Constants.DAILY_LIMIT_PREF + account.getUsername(), 0);
        } else {
            AccountSettings settings = account.settings();
            if (settings != null) {
                return settings.getDailySpendLimitSatoshis();
            }
            return 0;
        }
    }

    public static void setDailySpendSatoshis(Context context, Account account, long spendLimit) {
        AccountSettings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.setDailySpendLimitSatoshis(spendLimit);
        try {
            settings.save();

            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(Constants.DAILY_LIMIT_PREF + account.getUsername(), spendLimit);
            editor.apply();
        } catch (AirbitzException e) {
            AirbitzCore.debugLevel(1, "setDailySpendSatoshis error:");
        }
    }

    public static boolean getPinSpendLimitSetting(Account account) {
        AccountSettings settings = account.settings();
        if (settings != null) {
            return settings.getSpendRequirePin();
        }
        return true;
    }

    public static void setPinSpendLimitSetting(Context context, Account account, boolean set) {
        AccountSettings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.setSpendRequirePin(set);
        try {
            settings.save();
        } catch (AirbitzException e) {
            AirbitzCore.debugLevel(1, "setPinSpendLimitSetting error:");
        }
    }

    public static long getPinSpendLimit(Account account) {
        AccountSettings settings = account.settings();
        if (settings != null) {
            return settings.getSpendRequirePinSatoshis();
        }
        return 0;
    }

    public static void setPinSpendSatoshis(Context context, Account account, long spendLimit) {
        AccountSettings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.setSpendRequirePinSatoshis(spendLimit);
        try {
            settings.save();
        } catch (AirbitzException e) {
            AirbitzCore.debugLevel(1, "SetPINSpendSatoshis error:");
        }
    }

    public static boolean tooMuchBitcoin(Account account, String bitcoin) {
        double val = 0.0;
        try {
            val = Double.parseDouble(bitcoin);
        } catch(NumberFormatException e) {
            // ignore any non-double
        }
        return account.denominationToSatoshi(bitcoin) > Constants.MAX_SATOSHI;
    }

    public static boolean tooMuchFiat(Account account, String fiat, int currencyNum) {
        double maxFiat = account.SatoshiToCurrency((long) Constants.MAX_SATOSHI, currencyNum);
        double val = 0.0;
        try {
            val = Double.parseDouble(fiat);
        } catch(NumberFormatException e) {
            // ignore any non-double
        }
        return val > maxFiat;
    }

    private static final int RECOVERY_REMINDER_COUNT = 2;

    public static void incRecoveryReminder(Account account) {
        incRecoveryReminder(account, 1);
    }

    public static void clearRecoveryReminder(Account account) {
        incRecoveryReminder(account, RECOVERY_REMINDER_COUNT);
    }

    private static void incRecoveryReminder(Account account, int val) {
        AccountSettings settings = account.settings();
        if (settings == null) {
            return;
        }
        int reminderCount = settings.settings().getRecoveryReminderCount();
        reminderCount += val;
        settings.settings().setRecoveryReminderCount(reminderCount);
        try {
            settings.save();
        } catch (AirbitzException e) {
            AirbitzCore.debugLevel(1, "incRecoveryReminder error:");
        }
    }

    public static boolean needsRecoveryReminder(Account account, Wallet wallet) {
        AccountSettings settings = account.settings();
        if (settings != null) {
            int reminderCount = settings.settings().getRecoveryReminderCount();
            if (reminderCount >= RECOVERY_REMINDER_COUNT) {
                // We reminded them enough
                return false;
            }

            if (wallet.getBalanceSatoshi() < 10000000) {
                // they do not have enough money to care
                return false;
            }

            if (account.hasRecoveryQuestionsSet()) {
                // Recovery questions already set
                clearRecoveryReminder(account);
                return false;
            }
        }
        return true;
    }
}
