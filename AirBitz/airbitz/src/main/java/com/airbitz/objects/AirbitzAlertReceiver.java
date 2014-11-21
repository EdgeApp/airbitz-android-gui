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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.AirbitzAPI;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Airbitz alerts are notifications of critical bug fixes
 * They are intended to be checked daily with a quick request to a server
 * If server not responsive in 2 seconds, the alarm is skipped
 */
public class AirbitzAlertReceiver extends BroadcastReceiver {
    final private String TAG = getClass().getSimpleName();

    public static final int ALERT_NOTIFICATION_CODE = 45631;
    public static final String ALERT_NOTIFICATION_TYPE = "com.airbitz.navigation.NotificationType";

    //FIXME REFACTOR TO ONE DAY INTERVALS BEFORE RELEASE
    final private static int REPEAT_ALERT_MILLIS = 60 * 1000 * 1; //1 minute     60 * 24; // 1 Day intervals

    NotificationTask mNotificationTask;
    PowerManager.WakeLock mWakeLock;
    Handler mHandler = new Handler();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        //Acquire the lock
        mWakeLock.acquire();

        mHandler.postDelayed(murderNotificationTask, 2000); // 2 second TTL

        mNotificationTask = new NotificationTask(context);
        mNotificationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    final Runnable murderNotificationTask = new Runnable() {
        @Override
        public void run() {
            if(mNotificationTask != null && !mNotificationTask.isCancelled()) {
                mNotificationTask.cancel(true);
            }
            //Release the lock
            if(mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    };


    public static void SetRepeatingAlertAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AirbitzAlertReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), REPEAT_ALERT_MILLIS, pi);
    }

    public static void CancelNextAlertAlarm(Context context)
    {
        Intent intent = new Intent(context, AirbitzAlertReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void issueOSNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Airbitz notification")
                .setContentText("Please touch to read critical messages")
                .setSmallIcon(R.drawable.ic_launcher);

        Intent resultIntent = new Intent(context, NavigationActivity.class);
        resultIntent.setType(ALERT_NOTIFICATION_TYPE);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                        ALERT_NOTIFICATION_CODE, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(ALERT_NOTIFICATION_CODE, builder.build());
    }

    public class NotificationTask extends AsyncTask<Void, Void, String> {
        String mMessageId;
        String mBuildNumber;
        Context mContext;

        NotificationTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            AirbitzAPI api = AirbitzAPI.getApi();
            PackageInfo pInfo;
            try {
                pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            mMessageId = String.valueOf(getMessageIDPref(mContext));

//            mBuildNumber = "2014112001"; // TESTING
            mBuildNumber = String.valueOf(pInfo.versionCode);

            return api.getMessages(mMessageId, mBuildNumber);
        }

        @Override
        protected void onPostExecute(final String response) {
            Log.d(TAG, "Notification response of " + mMessageId + "," + mBuildNumber + ": " + response);
            if(response != null && response.length() != 0) {
                if(hasAlerts(response)) {
                    issueOSNotification(mContext);
                }
            }
            mHandler.removeCallbacks(murderNotificationTask);
            mHandler.post(murderNotificationTask);
        }
     }

    private int getMessageIDPref(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(NavigationActivity.LAST_MESSAGE_ID, 0); // default to Automatic
    }

    private boolean hasAlerts(String input) {
        try {
            JSONObject json = new JSONObject(input);
            int count = json.getInt("count");
            if(count > 0) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}