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
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.BitcoinDenomination;
import co.airbitz.core.Settings;
import co.airbitz.core.Transaction;
import co.airbitz.core.Utils;
import co.airbitz.core.Wallet;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CoreWrapper {
    public static double SATOSHI_PER_BTC = 1E8;
    public static double SATOSHI_PER_mBTC = 1E5;
    public static double SATOSHI_PER_uBTC = 1E2;

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void setupAccount(Context context, Account account) {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        account.callbacks(new Account.Callbacks() {
            public void remotePasswordChange() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.REMOTE_PASSWORD_CHANGE_ACTION));
                    }
                });
            }

            public void loggedOut() {
            }

            public void accountChanged() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.DATASYNC_UPDATE_ACTION));
                    }
                });
            }

            public void walletsLoading() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.WALLET_LOADING_START_ACTION));
                    }
                });
            }

            public void walletChanged(Wallet wallet) {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.WALLET_CHANGED_ACTION));
                    }
                });
            }

            public void walletsLoaded() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(
                            new Intent(Constants.WALLETS_ALL_LOADED_ACTION));
                    }
                });
            }

            public void walletsChanged() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.WALLETS_RELOADED_ACTION));
                    }
                });
            }

            public void otpSkew() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.OTP_SKEW_ACTION));
                    }
                });
            }

            public void otpRequired() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.OTP_ERROR_ACTION));
                    }
                });
            }

            public void otpResetPending() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.OTP_RESET_ACTION));
                    }
                });
            }

            public void exchangeRateChanged() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.EXCHANGE_RATE_UPDATED_ACTION));
                    }
                });
            }

            public void blockHeightChanged() {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.BLOCKHEIGHT_CHANGE_ACTION));
                    }
                });
            }

            public void balanceUpdate(final Wallet wallet, final Transaction tx) {
                handler.post(new Runnable() {
                    public void run() {
                        manager.sendBroadcast(new Intent(Constants.WALLETS_RELOADED_ACTION));
                    }
                });
            }

            public void incomingBitcoin(final Wallet wallet, final Transaction tx) {
                handler.post(new Runnable() {
                    public void run() {
                        if (null != tx) {
                            Intent intent = new Intent(Constants.INCOMING_BITCOIN_ACTION);
                            intent.putExtra(Constants.WALLET_UUID, wallet.id());
                            intent.putExtra(Constants.WALLET_TXID, tx.id());
                            manager.sendBroadcast(intent);
                        }
                    }
                });
            }

            public void sweep(final Wallet wallet, final Transaction tx, final long amountSwept) {
                handler.post(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(Constants.WALLET_SWEEP_ACTION);
                        intent.putExtra(Constants.WALLET_UUID, wallet.id());
                        if (tx != null) {
                            intent.putExtra(Constants.WALLET_TXID, tx.id());
                            intent.putExtra(Constants.AMOUNT_SWEPT, tx.amount());
                        } else {
                            intent.putExtra(Constants.WALLET_TXID, "");
                            intent.putExtra(Constants.AMOUNT_SWEPT, 0);
                        }
                        manager.sendBroadcast(intent);
                    }
                });
            }
        });
    }

    public static List<WalletWrapper> wrap(List<Wallet> wallets) {
        List<WalletWrapper> wrapped = new LinkedList<WalletWrapper>();
        if (wallets != null) {
            for (Wallet w : wallets) {
                wrapped.add(new WalletWrapper(w));
            }
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
        if (prefs.contains(Constants.DAILY_LIMIT_SETTING_PREF + account.username())) {
            return prefs.getBoolean(Constants.DAILY_LIMIT_SETTING_PREF + account.username(), true);
        } else {
            Settings settings = account.settings();
            if (settings != null) {
                return settings.dailySpendLimit();
            }
            return false;
        }
    }

    public static void setDailySpendLimitSetting(Context context, Account account, boolean set) {
        Settings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.dailySpendLimit(set);
        try {
            settings.save();

            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.DAILY_LIMIT_SETTING_PREF + account.username(), set);
            editor.apply();
        } catch (AirbitzException e) {
            AirbitzCore.logi("SetDailySpendLimitSetting error:");
        }
    }

   public static long getDailySpendLimit(Context context, Account account) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        if (prefs.contains(Constants.DAILY_LIMIT_PREF + account.username())) {
            return prefs.getLong(Constants.DAILY_LIMIT_PREF + account.username(), 0);
        } else {
            Settings settings = account.settings();
            if (settings != null) {
                return settings.dailySpendLimitSatoshis();
            }
            return 0;
        }
    }

    public static void setDailySpendSatoshis(Context context, Account account, long spendLimit) {
        Settings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.dailySpendLimitSatoshis(spendLimit);
        try {
            settings.save();

            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(Constants.DAILY_LIMIT_PREF + account.username(), spendLimit);
            editor.apply();
        } catch (AirbitzException e) {
            AirbitzCore.logi("setDailySpendSatoshis error:");
        }
    }

    public static boolean getPinSpendLimitSetting(Account account) {
        Settings settings = account.settings();
        if (settings != null) {
            return settings.spendRequirePin();
        }
        return true;
    }

    public static void setPinSpendLimitSetting(Context context, Account account, boolean set) {
        Settings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.spendRequirePin(set);
        try {
            settings.save();
        } catch (AirbitzException e) {
            AirbitzCore.logi("setPinSpendLimitSetting error:");
        }
    }

    public static long getPinSpendLimit(Account account) {
        Settings settings = account.settings();
        if (settings != null) {
            return settings.spendRequirePinSatoshis();
        }
        return 0;
    }

    public static void setPinSpendSatoshis(Context context, Account account, long spendLimit) {
        Settings settings = account.settings();
        if (settings == null) {
            return;
        }
        settings.spendRequirePinSatoshis(spendLimit);
        try {
            settings.save();
        } catch (AirbitzException e) {
            AirbitzCore.logi("SetPINSpendSatoshis error:");
        }
    }

    public static boolean tooMuchBitcoin(Account account, String bitcoin) {
        double val = 0.0;
        try {
            val = Double.parseDouble(bitcoin);
        } catch(NumberFormatException e) {
            // ignore any non-double
        }
        return Utils.btcStringToSatoshi(account, bitcoin) > Constants.MAX_SATOSHI;
    }

    public static boolean tooMuchFiat(Account account, String fiat, String currencyCode) {
        double maxFiat = AirbitzCore.getApi().exchangeCache().satoshiToCurrency((long) Constants.MAX_SATOSHI, currencyCode);
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
        Settings settings = account.settings();
        if (settings == null) {
            return;
        }
        int reminderCount = settings.recoveryReminderCount();
        reminderCount += val;
        settings.recoveryReminderCount(reminderCount);
        try {
            settings.save();
        } catch (AirbitzException e) {
            AirbitzCore.logi("incRecoveryReminder error:");
        }
    }

    public static boolean needsRecoveryReminder(Account account, Wallet wallet) {
        Settings settings = account.settings();
        if (settings != null) {
            int reminderCount = settings.recoveryReminderCount();
            if (reminderCount >= RECOVERY_REMINDER_COUNT) {
                // We reminded them enough
                return false;
            }

            if (wallet.balance() < 10000000) {
                // they do not have enough money to care
                return false;
            }

            if (AirbitzCore.getApi().accountHasRecovery(account.username())) {
                // Recovery questions already set
                clearRecoveryReminder(account);
                return false;
            }
        }
        return true;
    }

    public static long getTotalSentToday(Wallet wallet) {
        Calendar beginning = Calendar.getInstance();
        long end = beginning.getTimeInMillis();
        beginning.set(Calendar.HOUR_OF_DAY, 0);
        beginning.set(Calendar.MINUTE, 0);
        long start = beginning.getTimeInMillis();

        long sum = 0;
        List<Transaction> list = wallet.transactions();
        for (Transaction tx : list) {
            if (tx.amount() < 0
                    && tx.date().getTime() >= start
                    && tx.date().getTime() <= end) {
                sum += Math.abs(tx.amount());
            }
        }
        return sum;
    }

    public static String defaultBTCDenomination(Account account) {
        Settings settings = account.settings();
        if(settings == null) {
            return "";
        }
        BitcoinDenomination bitcoinDenomination =
            settings.bitcoinDenomination();
        if (bitcoinDenomination == null) {
            AirbitzCore.logi("Bad bitcoin denomination from core settings");
            return "";
        }
        return bitcoinDenomination.btcLabel();
    }

    public static boolean incrementPinCount(Account account) {
        Settings settings = account.settings();
        if (settings == null) {
            return false;
        }
        int pinLoginCount = settings.pinLoginCount();
        pinLoginCount++;
        settings.pinLoginCount(pinLoginCount);
        try {
            settings.save();
            if (pinLoginCount == 3
                    || pinLoginCount == 10
                    || pinLoginCount == 40
                    || pinLoginCount == 100) {
                return true;
            }
        } catch (AirbitzException e) {
            AirbitzCore.logi("incrementPinCount error:");
            return false;
        }
        return false;
    }

    public static String userBtcSymbol(Account account) {
        Settings settings = account.settings();
        if (settings == null) {
            return "";
        }
        BitcoinDenomination bitcoinDenomination =
            settings.bitcoinDenomination();
        if (bitcoinDenomination == null) {
            AirbitzCore.logi("Bad bitcoin denomination from core settings");
            return "";
        }
        return bitcoinDenomination.btcSymbol();
    }

    public static String btcToFiatConversion(Account account, String currency) {
        Settings settings = account.settings();
        if (settings != null) {
            BitcoinDenomination denomination =
                settings.bitcoinDenomination();
            long satoshi = 100;
            int fiatDecimals = 2;
            String amtBTCDenom = "1 ";
            if (denomination != null) {
                switch (denomination.type()) {
                    case BitcoinDenomination.BTC:
                        satoshi = (long) SATOSHI_PER_BTC;
                        fiatDecimals = 2;
                        amtBTCDenom = "1 ";
                        break;
                    case BitcoinDenomination.MBTC:
                        satoshi = (long) SATOSHI_PER_mBTC;
                        fiatDecimals = 3;
                        amtBTCDenom = "1 ";
                        break;
                    case BitcoinDenomination.UBTC:
                        satoshi = (long) SATOSHI_PER_uBTC;
                        fiatDecimals = 3;
                        amtBTCDenom = "1000 ";
                        break;
                    default:
                        break;
                }
            }
            double o = AirbitzCore.getApi().exchangeCache().satoshiToCurrency(satoshi, currency);
            if (denomination.type() == BitcoinDenomination.UBTC) {
                // unit of 'bits' is so small it's useless to show it's conversion rate
                // Instead show "1000 bits = $0.253 USD"
                o = o * 1000;
            }
            String amount = Utils.formatCurrency(o, currency, true, fiatDecimals);
            return amtBTCDenom + denomination.btcLabel() + " = " + amount + " " + currency;
        }
        return "";
    }

    public static String formatDefaultCurrency(Account account, double in) {
        Settings settings = account.settings();
        if (settings != null) {
            String pre = settings.bitcoinDenomination().btcSymbol();
            String out = String.format("%.3f", in);
            return pre+out;
        }
        return "";
    }

    public static long currencyToSatoshi(Account account, String amount, String currency) {
        try {
             Number cleanAmount =
                new DecimalFormat().parse(amount, new ParsePosition(0));
             if (null == cleanAmount) {
                 return 0;
             }
            double amountFiat = cleanAmount.doubleValue();
            long satoshi = AirbitzCore.getApi().exchangeCache().currencyToSatoshi(amountFiat, currency);

            // Round up to nearest 1 bits, .001 mBTC, .00001 BTC
            satoshi = 100 * (satoshi / 100);
            return satoshi;

        } catch (NumberFormatException e) {
            /* Sshhhhh */
        }
        return 0;
    }

    public static String formatCurrency(Account account, long satoshi, String currency, boolean withSymbol) {
        double o = AirbitzCore.getApi().exchangeCache().satoshiToCurrency(satoshi, currency);
        return Utils.formatCurrency(o, currency, withSymbol);
    }

    private static int CONFIRMED_CONFIRMATION_COUNT = 3;
    public boolean isConfirmed(Transaction t) {
        return t.height() >= CONFIRMED_CONFIRMATION_COUNT;
    }
}
