package com.airbitz.objects;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;

/**
 * Created by tom on 8/5/14.
 * Network AsyncTask that checks for a connection first and includes a timeout for no response
 */
public class ConnectionAsyncTask  extends AsyncTask<Object, Object, Object> {
    private final int DIALOG_TIMEOUT_MILLIS = 60000;
    boolean connected = false;
    Handler mHandler;
    Context mContext;

    public ConnectionAsyncTask() {}

    public void ConnectionAsyncTask(Context context) {
        mContext = context;
    }

    private boolean connected(){
        ConnectivityManager check = (ConnectivityManager) AirbitzApplication.getContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (check != null && check.getActiveNetworkInfo()!=null) {
            connected = check.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
        }
        return connected;
    }

    protected void onPreExecute(){
        if(!connected()) {
            onCancelled();
        }
        showModalProgress(true);
    }

    @Override
    protected Object doInBackground(Object... objects) {
        return null;
    }

    @Override
    protected void onPostExecute(Object object) {
        showModalProgress(false);
    }

    @Override
    protected void onCancelled() {
        showModalProgress(false);
    }

    private ProgressDialog mProgressDialog;
    public void showModalProgress(final boolean show) {
        if(show) {
            mProgressDialog = ProgressDialog.show(mContext, null, null);
            mProgressDialog.setContentView(R.layout.layout_modal_indefinite_progress);
            mProgressDialog.setCancelable(false);
            if(mHandler==null)
                mHandler = new Handler();
            mHandler.postDelayed(mProgressDialogKiller, DIALOG_TIMEOUT_MILLIS);
        } else {
            if(mProgressDialog!=null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    Runnable mProgressDialogKiller = new Runnable() {
        @Override
        public void run() {
            if(mProgressDialog!=null) {
                mProgressDialog.dismiss();
                onCancelled();
            }
        }
    };
}