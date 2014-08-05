package com.airbitz.objects;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

/**
 * Created by tom on 8/5/14.
 * Network AsyncTask that checks for a connection first and includes a timeout for no response
 */
public class ConnectionAsyncTask  extends AsyncTask {

    private Context context;
    boolean connected = false;

    public ConnectionAsyncTask(Context context) {
        this.context = context;
    }

    private boolean connected(){
        ConnectivityManager check = (ConnectivityManager) this.context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (check != null) {
            connected = check.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
        }
        return connected;
    }

    protected void onPreExecute(){
        if(!connected()) {
            //TODO throw a dialog and abort
            this.cancel(true);
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return null;
    }
}