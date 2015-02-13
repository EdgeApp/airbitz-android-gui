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

package com.airbitz.fragments;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

/**
 * Two Factor Authentication Menu
 * Created 2/5/15
 */
public class TwoFactorMenuFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    private Button mScanButton;
    private Button mResetButton;
    private HighlightOnPressImageButton mHelpButton;
    private TextView mTitleTextView;
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_twofactor_menu, container, false);

        ImageButton mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mScanButton = (Button) mView.findViewById(R.id.fragment_twofactor_menu_button_scan_token);
        mScanButton.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.pushFragment(new TwoFactorScanFragment(), NavigationActivity.Tabs.SETTING.ordinal());
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

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.fragment_twofactor_menu_title);

//        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
//        mHelpButton.setVisibility(View.VISIBLE);
//        mHelpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mActivity.pushFragment(new HelpFragment(HelpFragment.SPEND_LIMITS), NavigationActivity.Tabs.SETTING.ordinal());
//            }
//        });

        return mView;
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
            tABC_CC cc = core.ABC_OtpResetSet(AirbitzApplication.getUsername(), error);
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
