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
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.models.Wallet;

import java.util.ArrayList;
import java.util.List;

public class ExportFragment extends WalletBaseFragment {

    View mView;
    private Button mCSVButton;
    private Button mQuickenButton;
    private Button mQuickBooksButton;
    private Button mPdfbutton;
    private Button mWalletPrivateSeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHomeEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);

        mView = i.inflate(R.layout.fragment_export, container, false);
        mCSVButton = (Button) mView.findViewById(R.id.button_csv);
        mQuickenButton = (Button) mView.findViewById(R.id.button_quicken);
        mQuickBooksButton = (Button) mView.findViewById(R.id.button_quickbooks);
        mPdfbutton = (Button) mView.findViewById(R.id.button_pdf);
        mWalletPrivateSeed = (Button) mView.findViewById(R.id.button_wallet);

        mCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoExportSavings(ExportSavingOptionFragment.ExportTypes.CSV.ordinal());
            }
        });

        mQuickenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoExportSavings(ExportSavingOptionFragment.ExportTypes.Quicken.ordinal());
            }
        });

        mQuickBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoExportSavings(ExportSavingOptionFragment.ExportTypes.Quickbooks.ordinal());
            }
        });

        mPdfbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoExportSavings(ExportSavingOptionFragment.ExportTypes.PDF.ordinal());
            }
        });

        mWalletPrivateSeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoExportSavings(ExportSavingOptionFragment.ExportTypes.PrivateSeed.ordinal());
            }
        });
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_standard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
        case android.R.id.home:
            ExportFragment.popFragment(mActivity);
            return true;
        case R.id.action_help:
            mActivity.pushFragment(
                new HelpFragment(HelpFragment.EXPORT_WALLET), NavigationActivity.Tabs.WALLET.ordinal());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void gotoExportSavings(int type) {
        ExportSavingOptionFragment.pushFragment(mActivity, mWallet.getUUID(), type);
    }

    public static void pushFragment(NavigationActivity mActivity) {
        Fragment fragment = new ExportFragment();
        mActivity.pushFragment(fragment);
    }

    public static void popFragment(NavigationActivity mActivity) {
        mActivity.popFragment();
    }
}
