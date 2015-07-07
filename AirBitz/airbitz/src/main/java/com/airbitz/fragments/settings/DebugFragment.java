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

package com.airbitz.fragments.settings;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;

/*
 * Information and actions for User debugging assistance
 */
public class DebugFragment extends BaseFragment {

    View mView;
    private HighlightOnPressButton mClearWatchersButton;
    private HighlightOnPressButton mUploadLogButton;
    private HighlightOnPressImageButton mBackButton;
    private TextView mTitleTextView;
    private NavigationActivity mActivity;
    private CoreAPI mCoreAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (NavigationActivity) getActivity();
        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_debug, container, false);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mTitleTextView.setText(getString(R.string.fragment_debug_title));

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });


        mClearWatchersButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_debug_clear_watcher_button);
        mClearWatchersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ClearWatchersTask().execute();
            }
        });

        mUploadLogButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_debug_upload_log_button);
        mUploadLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadLogsTask().execute();
            }
        });

        try {
            String s = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            Integer iVersionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
            TextView appVersionText = (TextView) mView.findViewById(R.id.debug_app_version_text);
            s = s.concat(" (" + iVersionCode.toString() + ")");
            appVersionText.setText(s);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ((TextView) mView.findViewById(R.id.debug_network_version_text)).setText(CoreAPI.getApi().isTestNet() ? "Testnet" : "Mainnet");

        ((TextView) mView.findViewById(R.id.debug_core_version_text)).setText(CoreAPI.getApi().getCoreVersion());

        return mView;
    }

    /**
     * Upload core logs
     */
    public class UploadLogsTask extends AsyncTask<Void, Void, Void> {

        UploadLogsTask() { }

        @Override
        protected void onPreExecute() {
            if(isAdded()) {
                mUploadLogButton.setClickable(false);
                mUploadLogButton.setEnabled(false);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCoreAPI.uploadLogs();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if(isAdded()) {
                mUploadLogButton.setClickable(true);
                mUploadLogButton.setEnabled(true);
                mUploadLogButton.setBackgroundResource(R.drawable.btn_green);
            }
        }

        @Override
        protected void onCancelled() {
            if(isAdded()) {
                mUploadLogButton.setClickable(true);
                mUploadLogButton.setEnabled(true);
                mUploadLogButton.setBackgroundResource(R.drawable.btn_green);
            }
        }
    }

    /**
     * Clear watchers
     */
    public class ClearWatchersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mCoreAPI.stopWatchers();
            mCoreAPI.startWatchers();
            return null;
        }
    }
}
