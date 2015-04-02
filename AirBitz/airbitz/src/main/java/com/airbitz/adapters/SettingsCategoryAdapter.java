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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.objects.HighlightOnPressButton;

import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class SettingsCategoryAdapter extends ArrayAdapter<String> {

    private List<String> mCurrentCategories;
    private List<String> mCategories;
    private List<TextView> mPopUpViews; //0-edittext, 1-Expense, 2-Income, 3-Transfer, 4-currentItemEdittext
    private List<Integer> mCurrentPosPopUp; //0- currentPos in currentCategories, 1- currentPos in Categories
    private RelativeLayout mPopUpContainer;
    private NavigationActivity mContext;

    public SettingsCategoryAdapter(NavigationActivity context, List<String> currentCategories, List<String> categories, List<TextView> popUpViews, RelativeLayout popUpContainer, List<Integer> currentPosPopUp) {
        super(context, R.layout.item_listview_settings_categories, currentCategories);
        mCurrentCategories = currentCategories;
        mCategories = categories;
        mContext = context;
        mPopUpViews = popUpViews;
        mPopUpContainer = popUpContainer;
        mCurrentPosPopUp = currentPosPopUp;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_settings_categories, parent, false);

        final EditText mCategoryName = (EditText) convertView.findViewById(R.id.category_field);
        mCategoryName.setText(mCurrentCategories.get(position));

        final int pos = position;

        mCategoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    System.out.println("Cat name has focus");
                    mPopUpViews.get(0).setText(mCurrentCategories.get(pos));
                    mPopUpViews.get(0).requestFocus();
                    mPopUpContainer.setVisibility(View.VISIBLE);
                    mCurrentPosPopUp.add(pos);
                    for (String s : mCategories) {
                        if (s.compareTo(mCurrentCategories.get(pos)) == 0) {
                            mCurrentPosPopUp.add(mCategories.indexOf(s));
                        }
                    }
                }
            }
        });

        HighlightOnPressButton mDeleteButton = (HighlightOnPressButton) convertView.findViewById(R.id.category_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.ShowFadingDialog(String.format(mContext.getString(R.string.fragment_category_deleted), mCurrentCategories.get(position)));
                mCurrentCategories.remove(pos);
                mCategories.remove(pos);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
