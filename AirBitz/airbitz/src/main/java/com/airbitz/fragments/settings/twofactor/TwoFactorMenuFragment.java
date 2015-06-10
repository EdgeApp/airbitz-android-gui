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

package com.airbitz.fragments.settings.twofactor;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Two Factor Authentication Menu
 * Created 2/5/15
 */
public class TwoFactorMenuFragment extends BaseFragment implements
    TwoFactorScanFragment.OnTwoFactorQRScanResult {
    private final String TAG = getClass().getSimpleName();

    public static String STORE_SECRET = "com.airbitz.twofactormenu.storesecret";
    public static String TEST_SECRET = "com.airbitz.twofactormenu.testsecret";
    public static String USERNAME = "com.airbitz.twofactormenu.username";

    String mUsername;
    boolean mStoreSecret = false;
    boolean mTestSecret = false;

    private Button mScanButton;
    private Button mResetButton;
    private TextView mResetDescription, mResetDate;
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;

    OnTwoFactorMenuResult mOnTwoFactorMenuResult;

    public interface OnTwoFactorMenuResult {
        public void onTwoFactorMenuResult(boolean success, String result);
    }
    public void setOnTwoFactorMenuResult(OnTwoFactorMenuResult listener) {
        mOnTwoFactorMenuResult = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_twofactor_menu, container, false);

        Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.fragment_twofactor_menu_title);
        getBaseActivity().setSupportActionBar(toolbar);
        getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);

        mScanButton = (Button) mView.findViewById(R.id.fragment_twofactor_menu_button_scan_token);
        mScanButton.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTwoFactorScan();
            }
        });

        mResetButton = (Button) mView.findViewById(R.id.fragment_twofactor_menu_button_request_reset);
        mResetButton.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResetTask = new ResetTask();
                mResetTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
            }
        });

        mResetDate = (TextView) mView.findViewById(R.id.fragment_twofactor_date);
        mResetDescription = (TextView) mView.findViewById(R.id.fragment_twofactor_description);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        mStoreSecret = bundle.getBoolean(STORE_SECRET, false);
        mTestSecret = bundle.getBoolean(TEST_SECRET, false);
        mUsername = bundle.getString(USERNAME);

        tABC_CC cc = mCoreAPI.GetTwoFactorDate();
        String date = mCoreAPI.mTwoFactorDate;
        if(cc == tABC_CC.ABC_CC_Ok) {
            if(date == null || date.isEmpty()) {
                mResetDate.setVisibility(View.GONE);
                mResetDescription.setVisibility(View.GONE);
                mResetButton.setVisibility(View.VISIBLE);
            }
            else {
                mResetDate.setVisibility(View.VISIBLE);
                mResetDescription.setVisibility(View.VISIBLE);
                mResetButton.setVisibility(View.GONE);
                mResetDate.setText(formatDate(date));
            }
        }
        else {
            mResetDate.setVisibility(View.GONE);
            mResetDescription.setVisibility(View.GONE);
            mResetButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String formatDate(String date) {
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Date result;
        try {
            result = df1.parse(date);
            return "Reset Date: " + outFormat.format(result);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Reset Date: null";
    }

    @Override
    public void onTwoFactorQRScanResult(boolean success, String result) {
        if(mOnTwoFactorMenuResult != null) {
            mOnTwoFactorMenuResult.onTwoFactorMenuResult(success, result);
        }
        mActivity.onBackPressed();
    }

    private void launchTwoFactorScan() {
        TwoFactorScanFragment fragment = new TwoFactorScanFragment();
        fragment.setOnTwoFactorQRScanResult(this);
        Bundle bundle = new Bundle();
        bundle.putBoolean(TwoFactorScanFragment.TEST_SECRET, mStoreSecret);
        bundle.putBoolean(TwoFactorScanFragment.STORE_SECRET, mTestSecret);
        bundle.putString(TwoFactorScanFragment.USERNAME, mUsername);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);
    }

    /**
     * Reset Two Factor Authentication
     */
    private ResetTask mResetTask;
    public class ResetTask extends AsyncTask<Void, Void, String> {

        ResetTask() { }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            tABC_Error error = new tABC_Error();
            tABC_CC cc = core.ABC_OtpResetSet(mUsername, error);
            String message = cc == tABC_CC.ABC_CC_Ok ?
                    getString(R.string.fragment_twofactor_menu_reset_requested) : Common.errorMap(mActivity, cc);

            return message;
        }

        @Override
        protected void onPostExecute(final String message) {
            onCancelled();
            mActivity.ShowFadingDialog(message);
        }

        @Override
        protected void onCancelled() {
            mResetTask = null;
            mActivity.showModalProgress(false);
        }
    }
}
