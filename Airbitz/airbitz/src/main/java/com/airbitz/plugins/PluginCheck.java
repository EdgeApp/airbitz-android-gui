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

package com.airbitz.plugins;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.api.DirectoryWrapper;
import com.airbitz.api.directory.PluginDetailsResult;
import com.airbitz.api.directory.DirectoryApi;
import com.airbitz.plugins.PluginFramework;

import org.json.JSONObject;

import java.util.List;
import java.util.LinkedList;

public class PluginCheck  {
    public static String TAG = PluginCheck.class.getSimpleName();

    public static void checkEnabledPlugins() {
        DetailTask task = new DetailTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class DetailTask extends AsyncTask<String, Void, String> {
        DirectoryApi mApi = DirectoryWrapper.getApi();

        @Override
        protected void onPreExecute() { }

        @Override
        protected String doInBackground(String... params) {
            try {
                return mApi.checkPluginDetails();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String results) {
            if (results != null) {
                try {
                    JSONObject j = new JSONObject(results);
                    List<PluginDetailsResult> details =
                        PluginDetailsResult.fromJsonArray(j.getJSONArray("results"));
                    for (PluginDetailsResult detail : details) {
                        PluginFramework.getPluginObjects().setPluginStatus(detail.getId(), detail.isEnabled());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
            PluginFramework.getPluginsGrouped();
        }
    }
}
