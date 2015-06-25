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
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.directory.BusinessDirectoryFragment;
import com.airbitz.models.Business;

import java.util.List;

/**
 * Created on 2/11/14.
 */
public class BusinessSearchAdapter extends ArrayAdapter<Business> implements Filterable {

    private static int sGrayText;
    private static int sGreenText;
    private Context mContext;
    private List<Business> mLocationValue;

    public BusinessSearchAdapter(Context context, List<Business> locationValue) {
        super(context, R.layout.item_listview_location, locationValue);
        mContext = context;
        mLocationValue = locationValue;
        sGrayText = context.getResources().getColor(R.color.gray_text);
        sGreenText = context.getResources().getColor(R.color.green_text);
    }


    @Override
    public int getCount() {
        return mLocationValue.size();
    }

    @Override
    public Business getItem(int position) {
        return mLocationValue.get(position);
    }

    public String getItemValue(int position) {
        return mLocationValue.get(position).getName();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_location, parent, false);

        final Business business = mLocationValue.get(position);
        TextView textView = (TextView) convertView.findViewById(R.id.fragment_category_textview_title);

        if (business.getType().equalsIgnoreCase("business")) {
            textView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace, Typeface.BOLD);
        } else if (business.getType().equalsIgnoreCase("category")) {
            textView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace, Typeface.ITALIC);
        } else {
            // Assert. no other type
        }
        textView.setText(business.getName());
        textView.setTextColor(business.isCached() ? sGreenText : sGrayText);

        return convertView;
    }
}



