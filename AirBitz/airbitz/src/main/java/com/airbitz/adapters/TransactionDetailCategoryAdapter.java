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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.directory.BusinessDirectoryFragment;
import com.airbitz.models.Category;

import java.util.List;

/**
 * Created by matt on 6/12/14.
 */
public class TransactionDetailCategoryAdapter extends ArrayAdapter {
    private Context mContext;
    private List<Category> mCategories;

    private OnNewCategory mOnNewCategory;
    public interface OnNewCategory {
        public void onNewCategory(String categoryName);
    }
    public void setOnNewCategoryListener(OnNewCategory listener) {
        mOnNewCategory = listener;
    }

    public TransactionDetailCategoryAdapter(Context context, List<Category> categories) {
        super(context, R.layout.item_listview_transaction_detail, categories);
        mContext = context;
        mCategories = categories;
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public Category getItem(int position) {
        return mCategories.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Category category = mCategories.get(position);
        convertView = inflater.inflate(R.layout.item_listview_transaction_detail, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.transaction_detail_item_name);
        textView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);
        textView.setText(category.getCategoryName());
        if(category.getCategoryLevel().equals("base")) {
            ImageButton button = (ImageButton) convertView.findViewById(R.id.transaction_detail_item_new);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mOnNewCategory != null) {
                        mOnNewCategory.onNewCategory(category.getCategoryName());
                    }
                }
            });
        }
        return convertView;
    }
}
