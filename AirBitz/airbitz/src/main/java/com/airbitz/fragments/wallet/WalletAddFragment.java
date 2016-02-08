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

package com.airbitz.fragments.wallet;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.CurrencyAdapter;
import co.airbitz.api.AccountSettings;
import co.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.utils.Common;

import java.util.List;

public class WalletAddFragment extends BaseFragment
    implements NavigationActivity.OnBackPress {

    public final String TAG = getClass().getSimpleName();

    private EditText mAddWalletNameEditText;
    private Button mAddWalletCancelButton;
    private Button mAddWalletDoneButton;
    private Spinner mAddWalletCurrencySpinner;
    private LinearLayout mAddWalletCurrencyLayout;

    private List<String> mCurrencyList;
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    private View mView;
    private AddWalletTask mAddWalletTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();

        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_wallets_add, container, false);
        }

        mAddWalletNameEditText = (EditText) mView.findViewById(R.id.fragment_wallets_addwallet_name_edittext);
        mAddWalletCancelButton = (Button) mView.findViewById(R.id.fragment_wallets_addwallet_cancel_button);
        mAddWalletDoneButton = (Button) mView.findViewById(R.id.fragment_wallets_addwallet_done_button);
        mAddWalletCurrencySpinner = (Spinner) mView.findViewById(R.id.fragment_wallets_addwallet_currency_spinner);
        mAddWalletCurrencyLayout = (LinearLayout) mView.findViewById(R.id.fragment_wallets_addwallet_currency_layout);

        mAddWalletNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    goDone();
                    return true;
                }
                return false;
            }
        });

        mCurrencyList = mCoreAPI.getCurrencyCodeAndDescriptionArray();
        CurrencyAdapter mCurrencyAdapter = new CurrencyAdapter(mActivity, R.layout.item_currency_small, mCurrencyList);
        mAddWalletCurrencySpinner.setAdapter(mCurrencyAdapter);
        AccountSettings settings = mCoreAPI.coreSettings();
        int num;
        if (settings != null)
            num = settings.getCurrencyNum();
        else
            num = mCoreAPI.defaultCurrencyNum();
        String defaultCode = mCoreAPI.getCurrencyCode(num);
        for (int i=0; i<mCurrencyList.size(); i++) {
            if (mCurrencyList.get(i).substring(0, 3).equals(defaultCode)) {
                mAddWalletCurrencySpinner.setSelection(i);
                break;
            }
        }

        mAddWalletDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDone();
            }
        });

        mAddWalletCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goCancel();
            }
        });

        mAddWalletNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mAddWalletNameEditText);
                }
            }
        });
        return mView;
    }

    @Override
    public String getTitle() {
        return mActivity.getString(R.string.fragment_wallets_add_wallet);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            WalletAddFragment.popFragment(mActivity);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackPress() {
        WalletAddFragment.popFragment(mActivity);
        return true;
    }

    private void goDone() {
        if (!Common.isBadWalletName(mAddWalletNameEditText.getText().toString())) {
            int[] nums = mCoreAPI.getCoreCurrencyNumbers();
            int currencyNum = nums[mAddWalletCurrencySpinner.getSelectedItemPosition()];

            mAddWalletTask = new AddWalletTask(mAddWalletNameEditText.getText().toString(), currencyNum);
            mAddWalletTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        } else {
            Common.alertBadWalletName(this.mActivity);
        }
    }

    private void goCancel() {
        mAddWalletCancelButton.setClickable(false);
        mAddWalletDoneButton.setClickable(false);

        WalletAddFragment.popFragment(mActivity);
    }

    public class AddWalletTask extends AsyncTask<Void, Void, Boolean> {

        private final String mWalletName;
        private final int mCurrencyNum;

        AddWalletTask(String walletName, int currencyNum) {
            mWalletName = walletName;
            mCurrencyNum = currencyNum;
        }

        @Override
        protected void onPreExecute() {
            if(isAdded()) {
                String msg = mActivity.getString(R.string.fragment_signup_creating_wallet);
                int timeout = mActivity.getResources().getInteger(R.integer.alert_hold_time_forever);
                mActivity.ShowFadingDialog(msg, null, timeout, false);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = mCoreAPI.createWallet(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                    mWalletName, mCurrencyNum);

            mCoreAPI.reloadWallets();
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddWalletTask = null;
            if(isAdded()) {
                if (!success) {
                    CoreAPI.debugLevel(1, "AddWalletTask failed");
                    mActivity.ShowFadingDialog(getString(R.string.fragment_wallets_created_wallet_failed));
                } else {
                    WalletAddFragment.popFragment(mActivity);
                    mActivity.ShowFadingDialog(String.format(getString(R.string.fragment_wallets_created_wallet), mWalletName));
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAddWalletTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mAddWalletNameEditText.requestFocus();
        mActivity.showSoftKeyboard(mAddWalletNameEditText);
    }

    public static void pushFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        Fragment fragment = new WalletAddFragment();
        mActivity.pushFragment(fragment, transaction);
    }

    public static void popFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        mActivity.popFragment(transaction);
    }
}
