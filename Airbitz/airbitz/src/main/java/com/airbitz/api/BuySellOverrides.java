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
import android.os.AsyncTask;

import com.airbitz.api.directory.DirectoryApi;

import java.util.Map;

public class BuySellOverrides {

    private static BuySellOverrides mSingleton;

    private Map<String, String> mOverrides;

    public static void sync() {
        if (mSingleton == null) {
            mSingleton = new BuySellOverrides();
        }
        mSingleton.run();
    }

    private void run() {
        new BuySellOverrideTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static class BuySellOverrideTask extends AsyncTask<Void, Void, Map<String, String>> {

        private DirectoryApi mDirectory;
        private BuySellOverrides mBuySell;

        public BuySellOverrideTask(BuySellOverrides buySell) {
            mDirectory = DirectoryWrapper.getApi();
            mBuySell = buySell;
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            return mDirectory.getCurrencyUrlOverrides();
        }

        @Override
        protected void onPostExecute(final Map<String, String> response) {
            mBuySell.mOverrides = response;
        }
    }

    public static String getCurrencyUrlOverrides(String currencyCode) {
        if (mSingleton != null && mSingleton.mOverrides != null) {
            return mSingleton.mOverrides.get(currencyCode);
        }
        return null;
    }
}
