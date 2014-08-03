package com.airbitz.objects;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

public class AirbitzService extends Service {
    private final String TAG = getClass().getSimpleName();
    public static final String SERVICE_USERNAME = "com.airbitz.objects.service.username";
    public static final String SERVICE_PASSWORD = "com.airbitz.objects.service.password";
    public static final String ASK_CREDENTIALS = "com.airbitz.objects.service.ask_credentials";
    public static final String SET_CREDENTIALS = "com.airbitz.objects.service.set_credentials";
    public static final String REPLY_CREDENTIALS = "com.airbitz.objects.service.reply_credentials";

    private String mUsername;
    private String mPassword;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerAppReceiver();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // no binding allowed
    }

    private void registerAppReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AirbitzService.ASK_CREDENTIALS);
        filter.addAction(AirbitzService.SET_CREDENTIALS);
        registerReceiver(AirbitzAppReceiver, filter);
    }

    // For receiving Service queries
    private BroadcastReceiver AirbitzAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Common.LogD(TAG, "Received a broadcast: "+intent.getAction());
            if(intent.getAction().equals(ASK_CREDENTIALS)) {
                Common.LogD(TAG, "Received get credentials");
                if(mUsername!=null && mPassword!=null) {
                    Intent appIntent = new Intent(REPLY_CREDENTIALS);
                    appIntent.putExtra(SERVICE_USERNAME, mUsername);
                    appIntent.putExtra(SERVICE_PASSWORD, mPassword);
                    sendBroadcast(appIntent);
                    Common.LogD(TAG, "Sending credentials = "+ mUsername +", "+ mPassword);
                }
            } else if(intent.getAction().equals(SET_CREDENTIALS)) {
                mUsername = intent.getStringExtra(SERVICE_USERNAME);
                mPassword = intent.getStringExtra(SERVICE_PASSWORD);
                Common.LogD(TAG, "Setting Username, mPassword = "+ mUsername +", "+ mPassword);
            }
        }
    };
}
