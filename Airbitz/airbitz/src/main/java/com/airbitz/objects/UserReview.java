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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.ContextThemeWrapper;

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Transaction;
import co.airbitz.core.Wallet;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.Date;
import java.util.List;

public class UserReview {
    private static int MAX_LOGINS = 7;
    private static int MAX_TRANSACTIONS = 7;
    private static long MAX_TIME_DIFFERENCE_MILLIS = 14 * 24 *60 * 60 * 1000;

    private static String LOGIN_COUNT = "com.airbitz.userreview.logincount";
    private static String FIRST_LOGIN_TIME = "com.airbitz.userreview.firstlogintime";
    private static String ALREADY_NOTIFIED = "com.airbitz.userreview.alreadynotified";

    private static boolean PASSWORD_REMINDER_DAYS_TO_MINS    = true;
    private static int DEFAULT_NUM_PASSWORD_USED             = 2;
    private static int DEFAULT_NUM_PASSWORD_USED_W_RECOVERY  = 4;
    private static int PASSWORD_DAYS_INCREMENT_POWER         = 2;
    private static int PASSWORD_COUNT_INCREMENT_POWER        = 2;
    private static int PASSWORD_DAYS_MAX_VALUE               = 64;
    private static int PASSWORD_COUNT_MAX_VALUE              = 128;
    private static int PASSWORD_WRONG_INCREMENT_DAYS         = 2;
    private static int PASSWORD_WRONG_INCREMENT_COUNT        = 4;

    private static final String  LAST_PASSWORD_LOGIN         = "LAST_PASSWORD_LOGIN";
    private static final String  PASSWORD_REMINDER_COUNT     = "PASSWORD_REMINDER_COUNT";
    private static final String  PASSWORD_REMINDER_DAYS      = "PASSWORD_REMINDER_DAYS";
    private static final String  NUM_NON_PASSWORD_LOGIN      = "NUM_NON_PASSWORD_LOGIN";
    private static final String  NUM_PASSWORD_USED           = "NUM_PASSWORD_USED";
    private static final String  PASSWORD_RECOVERY_ASK_COUNT = "PASSWORD_RECOVERY_ASK_COUNT";

    private static int     passwordReminderCount = 0;
    private static int     passwordReminderDays = 0;
    private static int     numNonPasswordLogin = 0;
    private static int     numPasswordUsed = 0;
    private static int     passwordRecoveryAskCount = 0;
    private static boolean passwordRecoveryAskedThisStartup = false;
    private static Date lastPasswordLogin;

    public static boolean   needsPasswordCheck = false;
    public static boolean   needsPasswordRecoveryPopup = false;

    static SharedPreferences mPrefs;
    static SharedPreferences.Editor mEditor;

    private static String userKey(String base) {
        Account account = AirbitzApplication.getAccount();
        return String.format("%s_%s", account.username(), base);
    }

    private static void loadSettings() {
        passwordReminderCount       = mPrefs.getInt(userKey(PASSWORD_REMINDER_COUNT), 0);
        passwordReminderDays        = mPrefs.getInt(userKey(PASSWORD_REMINDER_DAYS), 0);
        numNonPasswordLogin         = mPrefs.getInt(userKey(NUM_NON_PASSWORD_LOGIN), 0);
        numPasswordUsed             = mPrefs.getInt(userKey(NUM_PASSWORD_USED), 0);
        passwordRecoveryAskCount    = mPrefs.getInt(userKey(PASSWORD_RECOVERY_ASK_COUNT), 0);

        long lastPasswordLoginLong  = mPrefs.getLong(userKey(LAST_PASSWORD_LOGIN), 0L);
        lastPasswordLogin = new Date(lastPasswordLoginLong);

        // Check for invalid values and add defaults
        if (passwordReminderDays == 0 || passwordReminderCount == 0)
        {
            resetPasswordReminderToDefaults();
        }
    }

    public static void resetPasswordReminderToDefaults() {
        lastPasswordLogin = new Date();
        AirbitzCore mCoreAPI = AirbitzCore.getApi();
        Account account = AirbitzApplication.getAccount();

        String token = null;
        try {
            token = mCoreAPI.getRecovery2Token(account.username());
        } catch (AirbitzException e) {
            token = null;
        }
        if (token != null) {
            // Recovery is setup on this account. Set some more friendly defaults
            numPasswordUsed = DEFAULT_NUM_PASSWORD_USED_W_RECOVERY;
            passwordReminderDays = (int) Math.pow(PASSWORD_DAYS_INCREMENT_POWER, numPasswordUsed);
            passwordReminderCount = (int) Math.pow(PASSWORD_COUNT_INCREMENT_POWER, numPasswordUsed);
        } else {
            numPasswordUsed = DEFAULT_NUM_PASSWORD_USED;
            passwordReminderDays = (int) Math.pow(PASSWORD_DAYS_INCREMENT_POWER, numPasswordUsed);
            passwordReminderCount = (int) Math.pow(PASSWORD_COUNT_INCREMENT_POWER, numPasswordUsed);
        }
    }

    private static void saveSettings() {
        mEditor.putInt(userKey(PASSWORD_REMINDER_COUNT), passwordReminderCount);
        mEditor.putInt(userKey(PASSWORD_REMINDER_DAYS), passwordReminderDays);
        mEditor.putInt(userKey(NUM_NON_PASSWORD_LOGIN), numNonPasswordLogin);
        mEditor.putInt(userKey(NUM_PASSWORD_USED), numPasswordUsed);
        mEditor.putInt(userKey(PASSWORD_RECOVERY_ASK_COUNT), passwordRecoveryAskCount);

        mEditor.putLong(userKey(LAST_PASSWORD_LOGIN), lastPasswordLogin.getTime());
        mEditor.apply();
    }

    public static void setupPrefs() {
        mPrefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        loadSettings();
    }

    public static boolean offerUserReview() {
        setupPrefs();
        boolean notified = mPrefs.getBoolean(ALREADY_NOTIFIED, false);
        if(!notified && loginCountTriggered() && transactionCountTriggered() && timeUseTriggered()) {
            return true;
        }
        return false;
    }

    public static boolean loginCountTriggered() {
        boolean ret = true;
        int count = mPrefs.getInt(LOGIN_COUNT, 1);
        if(count != Integer.MAX_VALUE) {
            if(++count > MAX_LOGINS) {
                count = Integer.MAX_VALUE;
                ret = true;
            }
            else {
                ret = false;
            }
            mEditor.putInt(LOGIN_COUNT, count);
            mEditor.apply();
        }
        return ret;
    }

    public static boolean transactionCountTriggered() {
        if(AirbitzApplication.isLoggedIn()) {
            Account account = AirbitzApplication.getAccount();
            List<Wallet> walletList = account.wallets();
            if (null != walletList) {
                int transactionCount = 0;
                for (Wallet wallet : walletList) {
                    List<Transaction> transactions = wallet.transactions();
                    transactionCount += transactions.size();
                }
                return transactionCount > MAX_TRANSACTIONS;
            }
        }
        return false;
    }

    public static boolean timeUseTriggered() {
        if(mPrefs.contains(FIRST_LOGIN_TIME)) {
            long appFirstLaunch = mPrefs.getLong(FIRST_LOGIN_TIME, 0);
            if(appFirstLaunch != 0 && System.currentTimeMillis() > appFirstLaunch + MAX_TIME_DIFFERENCE_MILLIS) {
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

    public static void ShowUserReviewDialog(final NavigationActivity activity) {
        setupPrefs();
        mEditor.putBoolean(ALREADY_NOTIFIED, true);
        mEditor.apply();
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(activity);
            builder.setMessage(String.format(
                        activity.getString(R.string.user_review_message),
                        activity.getString(R.string.app_name)))
                    .setTitle(String.format(
                        activity.getString(R.string.user_review_title),
                        activity.getString(R.string.app_name)))
                    .setCancelable(false)
                    .setPositiveButton(activity.getResources().getString(R.string.user_review_great),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ShowUserLikesAirbitzDialog(activity);
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(activity.getResources().getString(R.string.user_review_not_good),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ShowUserDislikeDialog(activity);
                                    dialog.dismiss();
                                }
                            });
            builder.create().show();
    }

    public static void ShowUserDislikeDialog(final NavigationActivity activity) {
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(activity);
        builder.setMessage(activity.getString(R.string.user_review_ok_message))
                .setTitle(activity.getString(R.string.user_review_title))
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Context context = AirbitzApplication.getContext();
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:"));
                                intent.putExtra(Intent.EXTRA_EMAIL  , new String[] {
                                    AirbitzApplication.getContext().getString(R.string.app_support_email)
                                });
                                intent.putExtra(Intent.EXTRA_SUBJECT, String.format(
                                    context.getString(R.string.user_review_support_subject),
                                    context.getString(R.string.app_name)));
                                activity.startActivity(Intent.createChooser(intent, AirbitzApplication.getContext().getString(R.string.user_review_support_title)));
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(activity.getResources().getString(R.string.user_review_no_thanks),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        builder.create().show();
    }


    public static void ShowUserLikesAirbitzDialog(final NavigationActivity activity) {
        String title = String.format(activity.getString(R.string.user_review_title), activity.getString(R.string.app_name));
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(activity);
        builder.setMessage(activity.getString(R.string.user_review_play_store))
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(activity.getResources().getString(R.string.user_review_no_thanks),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        builder.create().show();
    }

    public static void passwordUsed()
    {
        numNonPasswordLogin = 0;
        numPasswordUsed++;
        lastPasswordLogin = new Date();
        passwordReminderDays  = (int) Math.pow(PASSWORD_DAYS_INCREMENT_POWER, numPasswordUsed);
        passwordReminderCount = (int) Math.pow(PASSWORD_COUNT_INCREMENT_POWER, numPasswordUsed);

        if (passwordReminderDays > PASSWORD_DAYS_MAX_VALUE)
            passwordReminderDays = PASSWORD_DAYS_MAX_VALUE;

        if (passwordReminderCount > PASSWORD_DAYS_MAX_VALUE)
            passwordReminderCount = PASSWORD_COUNT_MAX_VALUE;
        saveSettings();
    }

    private static int timeBetween(Date d1, Date d2, int unit){
        return (int)( (d2.getTime() - d1.getTime()) / (unit));
    }
    public static void passwordWrongAndSkipped()
    {
        int unit = 1000 * 60 * 60 * 24;

        if (PASSWORD_REMINDER_DAYS_TO_MINS)
            unit = 1000 * 60;

        int increment = timeBetween(lastPasswordLogin, new Date(), unit);
        increment += (PASSWORD_WRONG_INCREMENT_DAYS * unit);

        passwordReminderDays = increment / unit;
        passwordReminderCount = numNonPasswordLogin + PASSWORD_WRONG_INCREMENT_COUNT;

        saveSettings();
    }


    static void incPINorTouchIDLogin () {
        needsPasswordCheck = false;
        needsPasswordRecoveryPopup = false;

        numNonPasswordLogin++;

        if (numNonPasswordLogin >= passwordReminderCount) {
            needsPasswordCheck = true;
        }
        int unit = 1000 * 60 * 60 * 24;

        if (PASSWORD_REMINDER_DAYS_TO_MINS)
            unit = 1000 * 60;

        int days = timeBetween(lastPasswordLogin, new Date(), unit);

        if (days >= passwordReminderDays) {
            needsPasswordCheck = true;
        }

        if (!needsPasswordCheck && !passwordRecoveryAskedThisStartup) {
            String token = null;
            AirbitzCore mCoreAPI = AirbitzCore.getApi();
            Account account = AirbitzApplication.getAccount();

            try {
                token = mCoreAPI.getRecovery2Token(account.username());
            } catch (AirbitzException e) {
                token = null;
            }
            if (token == null) {
                // No recovery set. Lets ask user to set it up
                if (passwordRecoveryAskCount < 3)
                {
                    needsPasswordRecoveryPopup = true;
                }
            }
        }
        saveSettings();

    }

    public static void didAskPasswordRecovery() {
        passwordRecoveryAskCount++;
        passwordRecoveryAskedThisStartup = true;
        saveSettings();
    }

}
