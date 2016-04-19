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

package com.airbitz.api.directory;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PluginDetailsResult {

    private static final String TAG = BusinessSearchResult.class.getSimpleName();
    private String mBizId;
    private boolean mEnabled;

    public PluginDetailsResult(String bizId, boolean enabled) {
        mBizId = bizId;
        mEnabled = enabled;
    }

    public static List<PluginDetailsResult> fromJsonArray(JSONArray array) {
        List<PluginDetailsResult> results = new ArrayList<PluginDetailsResult>();
        for (int counter = 0; counter < array.length(); counter++) {
            try {
                JSONObject object = array.getJSONObject(counter);
                PluginDetailsResult result = new PluginDetailsResult(
                        object.getString("business"), object.getBoolean("enabled"));
                results.add(result);
            } catch (JSONException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
        return results;
    }

    public String getId() {
        return mBizId;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
}
