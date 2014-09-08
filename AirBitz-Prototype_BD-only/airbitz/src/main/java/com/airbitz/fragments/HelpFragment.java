package com.airbitz.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by tom 8/16/2014
 */
public class HelpFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    public static final String INFO = "file:///android_asset/html/info.html";
    public static final String EXPORT_WALLET = "file:///android_asset/html/infoExportWallet.html";
    public static final String EXPORT_WALLET_OPTIONS = "file:///android_asset/html/infoExportWalletOptions.html";
    public static final String IMPORT_WALLET = "file:///android_asset/html/infoImportWallet.html";
    public static final String RECIPIENT = "file:///android_asset/html/infoRecipient.html";
    public static final String REQUEST = "file:///android_asset/html/infoRequest.html";
    public static final String REQUEST_QR = "file:///android_asset/html/infoRequestQR.html";
    public static final String SEND = "file:///android_asset/html/infoSend.html";
    public static final String SEND_CONFIRMATION = "file:///android_asset/html/infoSendConfirmation.html";
    public static final String SETTINGS = "file:///android_asset/html/infoSettings.html";
    public static final String TRANSACTION_DETAILS = "file:///android_asset/html/infoTransactionDetails.html";
    public static final String TRANSACTIONS = "file:///android_asset/html/infoTransactions.html";
    public static final String WALLETS = "file:///android_asset/html/infoWallets.html";

    Spanned mHtml=null;
    String mFilePath=null;

    public HelpFragment() {}

    public HelpFragment(Spanned html) {
        mHtml = html;
    }

    public HelpFragment(String file) {
            mFilePath = file;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_help_info, container, false);

        if(mHtml!=null) {
            TextView tv = (TextView) v.findViewById(R.id.dialog_help_textview);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setVisibility(View.VISIBLE);
            tv.setText(mHtml);
        } else {
            WebView webView = (WebView) v.findViewById(R.id.dialog_help_webview);
            webView.setVisibility(View.VISIBLE);
            if(!mFilePath.equals(INFO)) {
                webView.loadUrl(mFilePath);
            } else {
                //Get file contents and replace * with versionbuild
                String version = "version error";
                int build = 0;
                PackageManager manager = getActivity().getPackageManager();
                try {
                    PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
                    version = info.versionName;
                    build = info.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String original = loadAssetTextAsString(getActivity(), "html/info.html");
                String replaced = original.replace("*", version + " " + String.valueOf(build));

                webView.loadData(replaced, "text/html; charset=UTF-8", null);
            }
        }

        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.dialog_help_close_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        ((NavigationActivity)getActivity()).hideSoftKeyboard(v);

        return v;
    }

    private String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Common.LogD(TAG, "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Common.LogD(TAG, "Error closing asset " + name);
                }
            }
        }

        return null;
    }
}