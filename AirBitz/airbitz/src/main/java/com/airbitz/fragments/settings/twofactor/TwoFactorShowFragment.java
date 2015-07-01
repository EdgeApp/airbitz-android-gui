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

import android.app.Fragment;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

/**
 * Two Factor Authentication Show
 * Created 2/6/15
 */
public class TwoFactorShowFragment extends BaseFragment
{
    private final String TAG = getClass().getSimpleName();

    Button mImportButton, mApproveButton, mCancelButton;
    ImageView mQRView;
    RelativeLayout mQRViewLayout;
    EditText mPassword;
    Switch mEnabledSwitch;
    private TextView mTitleTextView;
    LinearLayout mRequestView;
    boolean _isOn;
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();

        setHasOptionsMenu(true);
        setDrawerEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        View mView = i.inflate(R.layout.fragment_twofactor_show, container, false);

        Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.fragment_twofactor_show_title);
        getBaseActivity().setSupportActionBar(toolbar);
        getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPassword = (EditText) mView.findViewById(R.id.fragment_twofactor_show_password_edittext);

        mRequestView = (LinearLayout) mView.findViewById(R.id.fragment_twofactor_request_view);
        mApproveButton = (Button) mView.findViewById(R.id.fragment_twofactor_show_button_approve);
        mApproveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmRequest();
            }
        });

        mCancelButton = (Button) mView.findViewById(R.id.fragment_twofactor_show_button_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequest();
            }
        });

        mImportButton = (Button) mView.findViewById(R.id.fragment_twofactor_button_import);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTwoFactorMenu();
            }
        });

        mQRViewLayout = (RelativeLayout) mView.findViewById(R.id.fragment_twofactor_show_qr_layout);
        mQRView = (ImageView) mView.findViewById(R.id.fragment_twofactor_show_qr_image);

        mEnabledSwitch = (Switch) mView.findViewById(R.id.fragment_twofactor_show_toggle_enabled);
        mEnabledSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlipped(mEnabledSwitch.isChecked());
            }
        });
        return mView;
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

    @Override
    public void onResume() {
        super.onResume();

        initUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    void initUI()
    {
        mPassword.setText("");
        mPassword.setVisibility(mCoreAPI.PasswordExists() ? View.VISIBLE : View.GONE);

        _isOn = false;
        updateTwoFactorUI(false);

        // Check for any pending reset requests
        checkStatus(false);
    }

    void updateTwoFactorUI(boolean enabled)
    {
        mRequestView.setVisibility(View.GONE);
        mImportButton.setVisibility(mCoreAPI.hasOTPError() ? View.VISIBLE : View.GONE);
        mEnabledSwitch.setChecked(enabled);
        mEnabledSwitch.setText(getString(enabled ? R.string.fragment_twofactor_show_enabled : R.string.fragment_twofactor_show_disabled));
        mQRViewLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void launchTwoFactorMenu() {
        Fragment fragment = new TwoFactorMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(TwoFactorMenuFragment.STORE_SECRET, true);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);
    }

    void checkStatus(boolean bMsg)
    {
        mCheckStatusTask = new CheckStatusTask(bMsg);
        mCheckStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    /**
     * Check Two Factor Status
     */
    private CheckStatusTask mCheckStatusTask;
    public class CheckStatusTask extends AsyncTask<Void, Void, tABC_CC> {
        boolean mMsg;

        CheckStatusTask(boolean bMsg) {
            mMsg = bMsg;
            mActivity.showModalProgress(true);
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Void... params) {
            tABC_CC cc = mCoreAPI.OtpAuthGet();
            return cc;
        }

        @Override
        protected void onPostExecute(final tABC_CC cc) {
            onCancelled();
            updateTwoFactorUI(mCoreAPI.isTwoFactorOn());
            if(cc == tABC_CC.ABC_CC_Ok) {
                checkSecret(mMsg);
            }
            else {
                mActivity.ShowFadingDialog(getString(R.string.fragment_twofactor_show_unable_status));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    void checkSecret(boolean bMsg)
    {
        if (mCoreAPI.isTwoFactorOn()) {
            if (mCoreAPI.GetTwoFactorSecret() == null) {
                mQRViewLayout.setVisibility(View.GONE);
            }
            else {
                mQRViewLayout.setVisibility(View.VISIBLE);
            }
        }
        showQrCode(mCoreAPI.isTwoFactorOn());

        if (mCoreAPI.GetTwoFactorSecret() != null) {
            mActivity.showModalProgress(true);
            checkRequest();
            if (bMsg) {
                mActivity.ShowFadingDialog(getString(R.string.fragment_twofactor_show_now_enabled));
            }
        } else {
            if (bMsg) {
                mActivity.ShowFadingDialog(getString(R.string.fragment_twofactor_show_now_disabled));
            }
        }
    }

    void showQrCode(boolean show)
    {
        if (show) {
            Bitmap bitmap = mCoreAPI.getTwoFactorQRCodeBitmap();
            if(bitmap != null) {
                bitmap = Common.AddWhiteBorder(bitmap);
                mQRView.setImageBitmap(bitmap);
                animateQrCode(true);
            }
            else {
                mQRViewLayout.setVisibility(View.INVISIBLE);
            }
        } else {
            animateQrCode(false);
        }
    }

    void animateQrCode(boolean show)
    {
        if (show) {
            if (mQRViewLayout.getVisibility() != View.VISIBLE) {
                mQRViewLayout.setAlpha(0f);
                mQRViewLayout.setVisibility(View.VISIBLE);
                mQRViewLayout.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .setListener(null);
            }
        } else {
            if (mQRViewLayout.getVisibility() == View.VISIBLE) {
                mQRViewLayout.setAlpha(1f);
                mQRViewLayout.setVisibility(View.VISIBLE);
                mQRViewLayout.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .setListener(null);
            }
        }
    }

    void checkRequest()
    {
        tABC_Error error = new tABC_Error();
        boolean pending = mCoreAPI.isTwoFactorResetPending(AirbitzApplication.getUsername());
        boolean okay = error.getCode() == tABC_CC.ABC_CC_Ok;
        if (okay && pending) {
            mRequestView.setVisibility(View.VISIBLE);
        } else {
            mRequestView.setVisibility(View.GONE);
            if(!okay) {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, error.getCode()));
            }
        }
        mActivity.showModalProgress(false);
    }

    void switchFlipped(boolean isChecked) {
        mSwitchFlippedTask = new SwitchFlippedTask(isChecked);
        mSwitchFlippedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    void switchTwoFactor(boolean on)
    {
        tABC_CC cc = mCoreAPI.enableTwoFactor(on);
        if(cc==tABC_CC.ABC_CC_Ok) {
            updateTwoFactorUI(on);
            checkSecret(true);
        }
    }

    /**
     * Flip Enable switch task
     */
    private SwitchFlippedTask mSwitchFlippedTask;
    public class SwitchFlippedTask extends AsyncTask<Void, Void, Boolean> {
        boolean mIsChecked;
        SwitchFlippedTask(boolean isChecked) {
            mIsChecked = isChecked;
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mCoreAPI.PasswordOK(AirbitzApplication.getUsername(), mPassword.getText().toString());
        }

        @Override
        protected void onPostExecute(final Boolean authenticated) {
            onCancelled();

            if(authenticated) {
                switchTwoFactor(mIsChecked);
            }
            else {
                mPassword.requestFocus();
                mEnabledSwitch.setChecked(!mIsChecked);
                mActivity.ShowOkMessageDialog(getString(R.string.activity_signup_incorrect_password),
                        getString(R.string.activity_signup_incorrect_password));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    private void confirmRequest()
    {
        if(mCoreAPI.PasswordOK(AirbitzApplication.getUsername(), mPassword.getText().toString())) {
            mConfirmRequestTask = new ConfirmRequestTask();
            mConfirmRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
        else {
            mActivity.ShowFadingDialog(getString(R.string.activity_signup_incorrect_password));
            mActivity.showModalProgress(false);
        }
    }

    /**
     * Confirm Request
     */
    private ConfirmRequestTask mConfirmRequestTask;
    public class ConfirmRequestTask extends AsyncTask<Void, Void, tABC_CC> {

        ConfirmRequestTask() { }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Void... params) {
            return mCoreAPI.enableTwoFactor(false);
        }

        @Override
        protected void onPostExecute(final tABC_CC cc) {
            onCancelled();
            mActivity.showModalProgress(false);

            if (cc == tABC_CC.ABC_CC_Ok) {
                mActivity.ShowFadingDialog("Request confirmed, Two Factor off.");
                updateTwoFactorUI(false);
            }
            else {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, cc));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    private void cancelRequest()
    {
        if(mCoreAPI.PasswordOK(AirbitzApplication.getUsername(), mPassword.getText().toString())) {
            mCancelRequestTask = new CancelRequestTask();
            mCancelRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
        else {
            mActivity.ShowFadingDialog(getString(R.string.activity_signup_incorrect_password));
            mActivity.showModalProgress(false);
        }
    }

    /**
     * Cancel Request
     */
    private CancelRequestTask mCancelRequestTask;
    public class CancelRequestTask extends AsyncTask<Void, Void, tABC_CC> {

        CancelRequestTask() { }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Void... params) {
            return mCoreAPI.cancelTwoFactorRequest();
        }

        @Override
        protected void onPostExecute(final tABC_CC cc) {
            onCancelled();
            mActivity.showModalProgress(false);

            if (cc == tABC_CC.ABC_CC_Ok) {
                mActivity.ShowFadingDialog("Reset Cancelled.");
                mRequestView.setVisibility(View.GONE);
            }
            else {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, cc));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }
}
