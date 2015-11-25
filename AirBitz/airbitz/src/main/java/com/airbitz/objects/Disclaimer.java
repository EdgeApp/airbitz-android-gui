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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

public class Disclaimer {

    static final String DISCLAIMER_AGREED = "com.airbitz.prefs.agreed_disclaimer";

    private static Boolean mAgreed;
    private static Dialog mDisclaimerDialog;

    public static void showDisclaimer(final Context context) {
        if (null != mDisclaimerDialog) {
            mDisclaimerDialog.show();
            return;
        }

        mDisclaimerDialog = new Dialog(context);
        mDisclaimerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDisclaimerDialog.setContentView(R.layout.dialog_disclaimer);
        mDisclaimerDialog.setCancelable(false);

        WebView webView = (WebView) mDisclaimerDialog.findViewById(R.id.dialog_webview);
        webView.setVisibility(View.VISIBLE);
        webView.loadData(Common.evaluateTextFile(context, R.raw.info_disclaimer), "text/html; charset=UTF-8", null);

        Button button = (Button) mDisclaimerDialog.findViewById(R.id.button_agree);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                markDiscalimerSeen(context);
                mAgreed = true;
                mDisclaimerDialog.dismiss();
                mDisclaimerDialog = null;
            }
        });
        // Force dialog to be full screen
        mDisclaimerDialog.getWindow().setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT);
        mDisclaimerDialog.show();
    }

    public static boolean hasAgreedDisclaimer(Context context) {
        if (null == mAgreed) {
            SharedPreferences prefs = context.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
            mAgreed = prefs.getBoolean(DISCLAIMER_AGREED, false);
        }
        return mAgreed;
    }

    public static void markDiscalimerSeen(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DISCLAIMER_AGREED, true);
        editor.apply();
        mAgreed = true;
    }

}
