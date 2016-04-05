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

package com.airbitz.objects;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.airbitz.R;
import com.airbitz.adapters.CategoryAdapter;

import java.util.Arrays;

public class CategoryWidget extends FrameLayout {

    static final int[] BACKGROUNDS = {
        R.drawable.bg_button_red,
        R.drawable.bg_button_green,
        R.drawable.bg_button_blue,
        R.drawable.bg_button_orange
    };

    public interface OnChangeListener {
        public void valueChange(CategoryWidget view);
    }

    private OnChangeListener mListener;
    private Context mContext;
    private Spinner mSpinner;
    private EditText mText;
    private CategoryAdapter mAdapter;

    public CategoryWidget(Context context) {
        super(context);
        init(context);
    }

    public CategoryWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CategoryWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_category_spinner, this, true);

        mSpinner = (Spinner) findViewById(R.id.prefix_spinner);
        mText = (EditText) findViewById(R.id.postfix_text);

        mAdapter = new CategoryAdapter(mContext, Arrays.asList(mContext.getResources().getStringArray(R.array.transaction_categories_list_no_colon)));
        mSpinner.setAdapter(mAdapter);
        mSpinner.setSelection(0);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCategoryBackground(position);
                if (null != mListener) {
                    mListener.valueChange(CategoryWidget.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (null != mListener) {
                    mListener.valueChange(CategoryWidget.this);
                }
            }
        });
    }

    public void setValue(String value) {
        updateDisplay(value);
    }

    public EditText getEditText() {
        return mText;
    }

    public void setOnChangeListener(OnChangeListener listener) {
        mListener = listener;
    }

    public Spinner getSpinner() {
        return mSpinner;
    }

    public String getValue() {
        return mSpinner.getSelectedItem().toString() + ":" + mText.getText().toString();
    }

    private void updateDisplay(String input) {
        int selected = 1;
        String currentType = "";
        if (input.isEmpty() || input.startsWith(mContext.getString(R.string.fragment_category_income))) {
            currentType = mContext.getString(R.string.fragment_category_income);
            selected = 1;
        } else if (input.startsWith(mContext.getString(R.string.fragment_category_expense))) {
            currentType = mContext.getString(R.string.fragment_category_expense);
            selected = 0;
        } else if (input.startsWith(mContext.getString(R.string.fragment_category_transfer))) {
            currentType = mContext.getString(R.string.fragment_category_transfer);
            selected = 2;
        } else if (input.startsWith(mContext.getString(R.string.fragment_category_exchange))) {
            currentType = mContext.getString(R.string.fragment_category_exchange);
            selected = 3;
        }
        mSpinner.setSelection(selected);
        updateCategoryBackground(selected);

        String strippedTerm = "";
        if (input.length() >= currentType.length()) {
            strippedTerm = input.substring(currentType.length());
        }
        mText.setText(strippedTerm);
    }

    private void updateCategoryBackground(int position) {
        mSpinner.setBackgroundResource(BACKGROUNDS[position]);
    }
}
