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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.models.Categories;
import com.airbitz.models.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/25/14.
 */
public class MoreCategoryAdapter extends BaseAdapter {

    public static final String TAG = VenueAdapter.class.getSimpleName();
    private Context mContext;
    private Categories mCategories;
    private List<Category> mListCategory;
    private LayoutInflater mInflater;

    public MoreCategoryAdapter(Context context, Categories categories) {
        mContext = context;
        mCategories = categories;
        if (mCategories != null) {
            mListCategory = categories.getBusinessCategoryArray();
        } else {
            mListCategory = new ArrayList<Category>();
        }
        mInflater = LayoutInflater.from(mContext);
    }

    public void setCategories(Categories categories) {
        mCategories = categories;
        if (mCategories != null) {
            mListCategory = categories.getBusinessCategoryArray();
        } else {
            mListCategory = new ArrayList<Category>();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mListCategory.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Category getListItemName(int position) {
        return mListCategory.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_category, parent, false);

        TextView textView = (TextView) convertView.findViewById(R.id.textview_category);
        textView.setText(mListCategory.get(position).getCategoryName());
        Typeface latoRegularTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/Lato-Regular.ttf");
        textView.setTypeface(latoRegularTypeFace);

        return convertView;
    }
}
