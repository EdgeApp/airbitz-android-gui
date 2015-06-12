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

package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.objects.CategoryWidget;

import java.util.List;
import java.util.Arrays;

public class SettingsCategoryAdapter extends ArrayAdapter<String> {

    private List<String> mCurrentCategories;
    private List<String> mCategories;
    private Context mContext;
    private CategoryWidget.OnChangeListener mListener;

    public SettingsCategoryAdapter(Context context, List<String> currentCategories, List<String> categories, CategoryWidget.OnChangeListener listener) {
        super(context, R.layout.item_listview_settings_categories, currentCategories);
        mCurrentCategories = currentCategories;
        mCategories = categories;
        mContext = context;
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_settings_categories, parent, false);

        final String text = mCurrentCategories.get(position);
        final CategoryWidget category = (CategoryWidget)  convertView.findViewById(R.id.category_widget);
        category.setValue(text);
        category.setTag((Object) position);
        category.setOnChangeListener(new CategoryWidget.OnChangeListener() {
            public void valueChange(CategoryWidget view) {
                int position = (int) view.getTag();
                String newItem = view.getValue();
                String oldItem = mCurrentCategories.get(position);

                int idx = mCategories.indexOf(oldItem);
                mCategories.set(idx, newItem);
                mCurrentCategories.set(position, newItem);

                if (null != mListener) {
                    mListener.valueChange(view);
                }
            }
        });

        Button deleteButton = (Button) convertView.findViewById(R.id.category_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String item = mCurrentCategories.get(position);
                mCurrentCategories.remove(item);
                mCategories.remove(item);
                notifyDataSetChanged();

                if (null != mListener) {
                    mListener.valueChange(null);
                }
            }
        });
        return convertView;
    }
}
