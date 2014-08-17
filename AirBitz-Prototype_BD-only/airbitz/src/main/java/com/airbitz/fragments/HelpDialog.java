package com.airbitz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.airbitz.R;

/**
 * Created by tom 8/16/2014
 */
public class HelpDialog extends Fragment {
    public static final String INFO = "file:///android_asset/html/info.html";
    public static final String EXPORT_WALLET = "file:///android_asset/html/infoExportWallet.html";
    public static final String EXPORT_WALLET_OPTIONS = "file:///android_asset/html/infoExportWalletOptions.html";
    public static final String IMPORT_WALLET = "file:///android_asset/html/infoImportWallet.html";
    public static final String RECIPIENT = "file:///android_asset/html/infoRecipient.html";
    public static final String REQUEST = "file:///android_asset/html/infoRequest.html";
    public static final String REQUEST_QR = "file:///android_asset/html/infoRequestQR.html";
    public static final String SEND = "file:///android_asset/html/infoSend.html";
    public static final String SETTINGS = "file:///android_asset/html/infoSettings.html";
    public static final String TRANSACTION_DETAILS = "file:///android_asset/html/infoTransactionDetails.html";
    public static final String TRANSACTIONS = "file:///android_asset/html/infoTransactions.html";
    public static final String WALLETS = "file:///android_asset/html/infoWallets.html";

    Spanned mHtml=null;
    String mFilePath=null;

    public HelpDialog() {}

    public HelpDialog(Spanned html) {
        mHtml = html;
    }

    public HelpDialog(String file) {
            mFilePath = file;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.requestFitSystemWindows();
        View v = inflater.inflate(R.layout.dialog_help_info, container, false);

        if(mHtml!=null) {
            TextView tv = (TextView) v.findViewById(R.id.dialog_help_textview);
            tv.setVisibility(View.VISIBLE);
            tv.setText(mHtml);
        } else {
            WebView tv = (WebView) v.findViewById(R.id.dialog_help_webview);
            tv.setVisibility(View.VISIBLE);
            tv.loadUrl(mFilePath);
        }

        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.dialog_help_close_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return v;
    }
}