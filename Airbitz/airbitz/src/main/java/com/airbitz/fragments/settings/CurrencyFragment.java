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

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import co.airbitz.core.AirbitzCore;
import co.airbitz.core.CoreCurrency;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.CurrencyAdapter;
import com.airbitz.fragments.BaseFragment;

import java.util.List;
import java.util.ArrayList;

public class CurrencyFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();
    public static String CURRENCY = "currency";

    public static interface OnCurrencySelectedListener {
        public void onCurrencySelected(String currencyCode);
    }

    private NavigationActivity mActivity;
    private OnCurrencySelectedListener mListener;
    private CurrencyAdapter mAdapter;
    private List<CoreCurrency> mOrigCurrencies;
    private List<CoreCurrency> mCurrencies;
    private ListView mListView;
    private EditText mSearch;
    private View mSearchClose;
    private String mSelected;

    public interface CurrencySelection {
        public void currencyChanged(int currencyNum);
    }
    private CurrencySelection mSelectionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
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
                mCurrencies.clear();
                if (TextUtils.isEmpty(query)) {
                    mCurrencies.addAll(mOrigCurrencies);
                } else {
                    for (int i = 0; i < mOrigCurrencies.size(); ++i) {
                        CoreCurrency d = mOrigCurrencies.get(i);
                        if (d.description.toLowerCase().contains(query.toLowerCase())
                            || d.code.toLowerCase().contains(query.toLowerCase())) {
                            mCurrencies.add(mOrigCurrencies.get(i));
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        mSearchClose = view.findViewById(R.id.search_close_btn);
        mSearchClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TextUtils.isEmpty(mSearch.getText())) {
                    mActivity.hideSoftKeyboard(mSearch);
                } else {
                    mSearch.setText("");
                }
            }
        });

        mOrigCurrencies = AirbitzCore.getApi().currencies();
        mCurrencies = new ArrayList<CoreCurrency>();
        mCurrencies.addAll(mOrigCurrencies);

        mAdapter = new CurrencyAdapter(getActivity(), mCurrencies);
        mListView = (ListView) view.findViewById(R.id.listview_currency);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mActivity.popFragment();
                mActivity.getFragmentManager().executePendingTransactions();
                if (mListener != null) {
                    mListener.onCurrencySelected(mCurrencies.get(i).code);
                }
            }
        });
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActivity.popFragment();
                mActivity.getFragmentManager().executePendingTransactions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(mSelected)) {
            for (int i = 0; i < mCurrencies.size(); ++i) {
                if (mSelected.equals(mCurrencies.get(i).code)) {
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
