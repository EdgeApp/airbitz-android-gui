/**
 * Copyright (c) 2016, Airbitz Inc
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

package com.airbitz.fragments.login;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Settings;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.Affiliates;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.utils.Common;

import org.w3c.dom.Text;

import static com.airbitz.fragments.login.SetupWriteItDownFragment.PIN;

/**
 * Created on 2/26/15.
 */
public class SetupContactsFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    private String mUsername;
    private String mPassword;
    private String mPin;

    private Button mNextButton;
    private TextView mMainText;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(false);
        mPositionNavBar = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup_contacts, container, false);
        mMainText = (TextView) mView.findViewById(R.id.fragment_setup_contacts_text);

        String string = mMainText.getText().toString();
        String newString = Common.evaluateTextString(mActivity, string);
        mMainText.setText(newString);

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mNextButton = (Button) mView.findViewById(R.id.fragment_setup_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNextButton.setClickable(false);
                requestContacts();
            }
        });

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.fragment_setup_titles);

        mNextButton.setBackgroundResource(R.drawable.setup_button_green);
        mNextButton.setClickable(true);

        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBackPress();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean mTryToStartContacts = true;

    public void requestContacts() {
        if (mTryToStartContacts) {
            mActivity.requestContactsFromFragment(false, new NavigationActivity.PermissionCallbacks() {

                @Override
                public void onDenied() {
                    mTryToStartContacts = false;
                }

                @Override
                public void onAllowed() {
                    launchSetupWriteItDown();
                }
            });

        } else {
            launchSetupWriteItDown();
        }

    }


    private void launchSetupWriteItDown() {

        SetupWriteItDownFragment fragment = new SetupWriteItDownFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SetupWriteItDownFragment.USERNAME, mUsername);
        bundle.putString(SetupWriteItDownFragment.PASSWORD, mPassword);
        bundle.putString(SetupWriteItDownFragment.PIN, mPin);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);
    }



//    private void goNext() {
//
//        requestCamera();
//
//        Account account = AirbitzApplication.getAccount();
//        account.startBackgroundTasks();
//
//        Settings settings = account.settings();
//        if (null != settings) {
//            settings.recoveryReminderCount(0);
//        }
//        try {
//            settings.save();
//        } catch (AirbitzException e) {
//            AirbitzCore.logi("SetupWriteItDownFragment.goNext 2 error:");
//        }
//        mActivity.UserJustLoggedIn(true, false);
//    }

    @Override
    public boolean onBackPress() {
        mActivity.hideSoftKeyboard(getView());
        // Do not go back
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
//        enableNextButton(true);
//        enableShow(mShow);
        Bundle bundle = getArguments();
        mUsername = bundle.getString(SetupWriteItDownFragment.USERNAME);
        mPassword = bundle.getString(SetupWriteItDownFragment.PASSWORD, "");
        mPin = bundle.getString(PIN);

//        Affiliates aff = new Affiliates(AirbitzApplication.getAccount());
//        aff.setupNewAccount();
    }
}
