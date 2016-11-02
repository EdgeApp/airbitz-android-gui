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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;

public class GiftCardFragment extends BasePluginList {

    private final String FIRST_USAGE_COUNT = "com.airbitz.fragments.plugins.firstusagecount";
    Handler mHandler = new Handler();

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            public void run() {
                SharedPreferences prefs = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
                int count = prefs.getInt(FIRST_USAGE_COUNT, 1);
                if (count <= 2) {
                    count++;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(FIRST_USAGE_COUNT, count);
                    editor.apply();
                    notifyFirstUsage(getString(R.string.fragment_send_first_usage));
                }
            }
        }).start();

        mPlugins.clear();
        mPlugins.addAll(PluginFramework.getPluginsGrouped().get(PluginFramework.GENERAL_PLUGINS));
    }

    private void notifyFirstUsage(final String message) {
        mHandler.post(new Runnable() {

            public void run() {
                String popupText = String.format(getString(R.string.plugin_popup_notice),
                        getString(R.string.app_name));
                mActivity.ShowFadingDialog(popupText,
                        getResources().getInteger(R.integer.alert_hold_time_help_popups));
            }
        });
    }

    @Override
    protected String getTitle() {
        return mActivity.getString(R.string.drawer_spend_bitcoin);
    }
}