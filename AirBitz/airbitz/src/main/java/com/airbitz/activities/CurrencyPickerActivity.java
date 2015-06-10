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

package com.airbitz.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.airbitz.R;
import com.airbitz.adapters.CurrencyAdapter;
import com.airbitz.api.CoreAPI;

import java.util.List;

public class CurrencyPickerActivity extends ActionBarActivity {
    private final String TAG = getClass().getSimpleName();
    public static  String CURRENCY = "currency";

    private CoreAPI mApi;
    private CurrencyAdapter mAdapter;
    private List<String> mOrigDescriptions;
    private List<String> mOrigCodes;
    private List<String> mDescriptions;
    private List<String> mCodes;
    private ListView mListView;
    private EditText mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_title);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mSearch = (EditText) findViewById(R.id.search);
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
                            mCodes.add(mOrigDescriptions.get(i));
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

        mAdapter = new CurrencyAdapter(this, mDescriptions);
        mListView = (ListView) findViewById(R.id.listview_currency);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String code = mCodes.get(i);
                Intent intent = new Intent();
                intent.putExtra(CURRENCY, code);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
