package com.airbitz.fragments;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

/**
 * Created by tom 8/16/2014
 */
public class HelpFragment extends Fragment {
    public static final int INFO = R.raw.info;
    public static final int EXPORT_WALLET = R.raw.info_export_wallet;
    public static final int EXPORT_WALLET_OPTIONS = R.raw.info_export_wallet_options;
    public static final int IMPORT_WALLET = R.raw.info_import_wallet;
    public static final int RECIPIENT = R.raw.info_recipient;
    public static final int REQUEST = R.raw.info_request;
    public static final int REQUEST_QR = R.raw.info_request_qr;
    public static final int SEND = R.raw.info_send;
    public static final int SEND_CONFIRMATION = R.raw.info_send_confirmation;
    public static final int SETTINGS = R.raw.info_settings;
    public static final int TRANSACTION_DETAILS = R.raw.info_transaction_details;
    public static final int TRANSACTIONS = R.raw.info_transactions;
    public static final int WALLETS = R.raw.info_wallets;
    private final String TAG = getClass().getSimpleName();
    Spanned mHtml = null;
    int mID = 0;

    public HelpFragment() {
    }

    public HelpFragment(Spanned html) {
        mHtml = html;
    }

    public HelpFragment(int resourceID) {
        mID = resourceID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_help_info, container, false);

        if (mHtml != null) {
            TextView tv = (TextView) v.findViewById(R.id.dialog_help_textview);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setVisibility(View.VISIBLE);
            tv.setText(mHtml);
        } else {
            WebView webView = (WebView) v.findViewById(R.id.dialog_help_webview);
            webView.setVisibility(View.VISIBLE);
            if (mID != INFO) {
                webView.loadData(Common.readRawTextFile(getActivity(), mID), "text/html; charset=UTF-8", null);
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
                String original = Common.readRawTextFile(getActivity(), R.raw.info);
                String replaced = original.replace("*", version + " " + String.valueOf(build));

                webView.loadData(replaced, "text/html; charset=UTF-8", null);
            }
        }

        // Watch for button clicks.
        Button button = (Button) v.findViewById(R.id.dialog_help_close_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        ((NavigationActivity) getActivity()).hideSoftKeyboard(v);

        return v;
    }
}