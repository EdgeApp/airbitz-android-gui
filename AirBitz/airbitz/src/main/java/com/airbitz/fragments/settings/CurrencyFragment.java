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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.CurrencyAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.fragments.BaseFragment;

import java.util.List;

public class CurrencyFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();
    public static String CURRENCY = "currency";

    public static interface OnCurrencySelectedListener {
        public void onCurrencySelected(String code);
    }

    private NavigationActivity mActivity;
    private OnCurrencySelectedListener mListener;
    private CoreAPI mApi;
    private CurrencyAdapter mAdapter;
    private List<String> mOrigDescriptions;
    private List<String> mOrigCodes;
    private List<String> mDescriptions;
    private List<String> mCodes;
    private ListView mListView;
    private EditText mSearch;
    private String mSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currency_list, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_category_title);

        mActivity = getBaseActivity();
        mActivity.setSupportActionBar(toolbar);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        mSearch = (EditText) view.findViewById(R.id.search);
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String query = editable.toString();
                mDescriptions.clear(); mCodes.clear();
                if (TextUtils.isEmpty(query)) {
                    mDescriptions.addAll(mOrigDescriptions);
                    mCodes.addAll(mOrigCodes);
                } else {
                    for (int i = 0; i < mOrigDescriptions.size(); ++i) {
                        String d = mOrigDescriptions.get(i);
                        if (d.toLowerCase().contains(query.toLowerCase())) {
                            mDescriptions.add(mOrigDescriptions.get(i));
                            mCodes.add(mOrigCodes.get(i));
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        mApi = CoreAPI.getApi();
        mOrigDescriptions = mApi.getCurrencyCodeAndDescriptionArray();
        mOrigCodes = mApi.getCurrencyCodeArray();
        mDescriptions = mApi.getCurrencyCodeAndDescriptionArray();
        mCodes = mApi.getCurrencyCodeArray();

        mAdapter = new CurrencyAdapter(getActivity(), mDescriptions);
        mListView = (ListView) view.findViewById(R.id.listview_currency);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String code = mCodes.get(i);
                int num = mApi.getCurrencyNumberFromCode(code);
                if (num > 0) {
                    tABC_AccountSettings settings = mApi.newCoreSettings();
                    if (settings == null) {
                        return;
                    }
                    settings.setCurrencyNum(num);
                    mApi.saveAccountSettings(settings);
                }

                mActivity.popFragment();
            }
        });
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActivity.popFragment();
                if (null != mListener) {
                    mListener.onCurrencySelected(null);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(mSelected)) {
            for (int i = 0; i < mCodes.size(); ++i) {
                if (mSelected.equals(mCodes.get(i))) {
                    mListView.setSelection(i);
                }
            }
        }
    }

    public void setOnCurrencySelectedListener(OnCurrencySelectedListener listener) {
        mListener = listener;
    }

    public void setSelected(String code) {
        mSelected = code;
    }
}
