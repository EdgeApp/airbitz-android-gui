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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.models.Wallet;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/22/14.
 */
public class ExportFragment extends BaseFragment {

    View mView;
    private HighlightOnPressButton mCSVButton;
    private HighlightOnPressButton mQuickenButton;
    private HighlightOnPressButton mQuickBooksButton;
    private HighlightOnPressButton mPdfbutton;
    private HighlightOnPressButton mWalletPrivateSeed;
    private HighlightOnPressSpinner mWalletSpinner;
    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressImageButton mBackButton;
    private TextView mTitleTextView;
    private Bundle bundle;
    private List<Wallet> mWalletList;
    private Wallet mWallet;
    private List<String> mWalletNameList;
    private CoreAPI mCoreApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        mCoreApi = CoreAPI.getApi();
        mWalletList = mCoreApi.getCoreWallets(false);
        String uuid = getArguments().getString(RequestFragment.FROM_UUID);
        mWallet = mCoreApi.getWalletFromUUID(uuid);
        mWalletNameList = new ArrayList<String>();
        for (Wallet wallet : mWalletList) {
            mWalletNameList.add(wallet.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_export, container, false);
        } else {

            return mView;
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mCSVButton = (HighlightOnPressButton) mView.findViewById(R.id.button_csv);
        mQuickenButton = (HighlightOnPressButton) mView.findViewById(R.id.button_quicken);
        mQuickBooksButton = (HighlightOnPressButton) mView.findViewById(R.id.button_quickbooks);
        mPdfbutton = (HighlightOnPressButton) mView.findViewById(R.id.button_pdf);
        mWalletPrivateSeed = (HighlightOnPressButton) mView.findViewById(R.id.button_wallet);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);
        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(getString(R.string.export_title));

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_export_account_spinner);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_request_wallet_spinner, mWalletNameList);
        dataAdapter.setDropDownViewResource(R.layout.item_request_wallet_spinner_dropdown);
        mWalletSpinner.setAdapter(dataAdapter);
        for (int i = 0; i < mWalletList.size(); i++) {
            if (mWallet.getUUID().equals(mWalletList.get(i).getUUID())) {
                mWalletSpinner.setSelection(i);
            }
        }
        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mWallet = mWalletList.get(i);
                bundle.putString(RequestFragment.FROM_UUID, mWallet.getUUID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.EXPORT_WALLET), NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

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

    private void gotoExportSavings(int type) {
        Fragment frag = new ExportSavingOptionFragment();
        bundle.putInt(ExportSavingOptionFragment.EXPORT_TYPE, type);
        frag.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(frag, NavigationActivity.Tabs.WALLET.ordinal());
    }
}
