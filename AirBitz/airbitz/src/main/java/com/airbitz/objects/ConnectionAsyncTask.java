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
public class ConnectionAsyncTask extends AsyncTask<Object, Object, Object> {
    private final int DIALOG_TIMEOUT_MILLIS = 60000;
    boolean connected = false;
    Handler mHandler;
    Context mContext;
    private ProgressDialog mProgressDialog;
    Runnable mProgressDialogKiller = new Runnable() {
        @Override
        public void run() {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                onCancelled();
            }
        }
    };

    public ConnectionAsyncTask() {
    }

    public void ConnectionAsyncTask(Context context) {
        mContext = context;
    }

    private boolean connected() {
        ConnectivityManager check = (ConnectivityManager) AirbitzApplication.getContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (check != null && check.getActiveNetworkInfo() != null) {
            connected = check.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
        }
        return connected;
    }

    protected void onPreExecute() {
        if (!connected()) {
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

    public void showModalProgress(final boolean show) {
        if (show) {
            mProgressDialog = ProgressDialog.show(mContext, null, null);
            mProgressDialog.setContentView(R.layout.layout_modal_indefinite_progress);
            mProgressDialog.setCancelable(false);
            if (mHandler == null)
                mHandler = new Handler();
            mHandler.postDelayed(mProgressDialogKiller, DIALOG_TIMEOUT_MILLIS);
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }
}