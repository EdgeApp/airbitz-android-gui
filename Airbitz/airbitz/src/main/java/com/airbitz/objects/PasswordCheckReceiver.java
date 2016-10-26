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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.airbitz.R;
import com.airbitz.AirbitzApplication;
import com.airbitz.activities.NavigationActivity;
import co.airbitz.core.AirbitzCore;

import java.util.List;

public class PasswordCheckReceiver extends BroadcastReceiver {
    static final private String TAG = "PasswordCheckReceiver";

	public static final String USER_SWITCH = "com.airbitz.user_switch";
	public static final String USERNAME = "com.airbitz.username";
    public static final String LAST_PASSWORD_CHECK = "com.airbit.airbitzalert.password_check_time";

    public static final int ALARM_NOTIFICATION_CODE = 10;
    public static final int ALERT_NOTIFICATION_CODE = 20;
    public static final String TYPE = "com.airbitz.airbitalert.Type";
    private static final int REPEAT_NOTIFICATION_MILLIS = 1000 * 60 * 60 * 1; // 1 hour intervals
    final private static int MAX_REPEAT = 1000 * 60 * 60 * 24; // 1 day max

    public static void setup(Context context) {
        Intent intent = new Intent(context, PasswordCheckReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(context, ALARM_NOTIFICATION_CODE,
			intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (pi != null) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), REPEAT_NOTIFICATION_MILLIS, pi);
		}
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        long intervalLast = prefs.getLong(LAST_PASSWORD_CHECK, 0);
        long now = System.currentTimeMillis();

        if ((now - intervalLast) < MAX_REPEAT) {
            return;
        }

        AirbitzCore api = AirbitzCore.getApi();
        List<String> accounts = api.listLocalAccounts();
		if (null == accounts) {
			return;
		}

        for (String account : accounts) {
            Log.d(TAG, "Checking account " + account);
            if (!api.accountHasPassword(account)) {
				buildNotification(context, account);
			}
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putLong(LAST_PASSWORD_CHECK, now);
        editor.apply();
    }

	private void buildNotification(Context context, String account) {
		String title = String.format(context.getString(R.string.account_needs_password_title));
		String message = String.format(
			context.getString(R.string.account_needs_password_message), account);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ico_sending_3)
				.setContentTitle(title)
				.setContentText(message);
        Intent intent = new Intent(context, NavigationActivity.class);
		intent.setAction(USER_SWITCH);
		intent.putExtra(USERNAME, account);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
			ALERT_NOTIFICATION_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setAutoCancel(true);
		NotificationManager manager =
			(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(account, 0, mBuilder.build());
	}
}
