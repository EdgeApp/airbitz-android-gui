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
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.directory.BusinessDirectoryFragment;
import com.airbitz.models.BusinessSearchResult;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

/**
 * Created by matt on 6/10/14.
 */
public class TransactionDetailSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<BusinessSearchResult> mBusinesses;
    private List<String> mContactNames;
    private Map<String, Uri> mContactPhotos;
    private List<Object> mCombined;
    private final Picasso mPicasso;

    public TransactionDetailSearchAdapter(Context context, List<BusinessSearchResult> businesses, List<String> contactNames,
                                          List<Object> combined, Map<String, Uri> contactPhotos) {
        super(context, R.layout.item_listview_icon_with_text, combined);
        mContext = context;
        mBusinesses = businesses;
        mContactNames = contactNames;
        mContactPhotos = contactPhotos;
        mCombined = combined;
        mPicasso = Picasso.with(context);
    }

    @Override
    public int getCount() {
        return mCombined.size();
    }

    @Override
    public Object getItem(int position) {
        return mCombined.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if (convertView == null) {
            // well set up the ViewHolder
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_icon_with_text, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.imageWrapper = convertView.findViewById(R.id.icon_wrapper);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.item_name);
            viewHolder.textView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.imageAbbrev = (TextView) convertView.findViewById(R.id.icon_abbrev);
            viewHolder.addressView = (TextView) convertView.findViewById(R.id.item_description);
            // store the holder with the view.
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        String nameForImage = "";
        viewHolder.imageWrapper.setBackgroundResource(nextColor(position));
        if (mCombined.get(position) instanceof BusinessSearchResult) {
            final BusinessSearchResult business = (BusinessSearchResult) mCombined.get(position);
            nameForImage = business.getName();
            viewHolder.textView.setText(nameForImage);

            if (business.getSquareProfileImage()!=null) {
                viewHolder.imageView.setVisibility(View.VISIBLE);
                mPicasso.load(business.getSquareProfileImage().getImageThumbnail()).into(viewHolder.imageView);
            } else {
                viewHolder.imageView.setVisibility(View.GONE);
                viewHolder.imageAbbrev.setVisibility(View.VISIBLE);
                viewHolder.imageAbbrev.setText(formatAbbrev(nameForImage));
            }

            // create the address
            String strAddress = "";
            if (business.getAddress() != null) {
                strAddress += business.getAddress();
            }
            if (business.getCity() != null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getCity();
            }
            if (business.getState() != null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getState();
            }
            if (business.getPostalCode() != null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getPostalCode();
            }

            viewHolder.addressView.setVisibility(View.VISIBLE);
            viewHolder.addressView.setText(strAddress);
            viewHolder.addressView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);
        } else if (mCombined.get(position) instanceof String) {
            nameForImage = (String) mCombined.get(position);
            viewHolder.textView.setText(nameForImage);
            viewHolder.addressView.setVisibility(View.GONE);

            if (mContactPhotos.get(nameForImage) != null) {
                viewHolder.imageView.setVisibility(View.VISIBLE);
                mPicasso.load(mContactPhotos.get(nameForImage)).into(viewHolder.imageView);
            } else {
                viewHolder.imageView.setVisibility(View.GONE);
                viewHolder.imageAbbrev.setVisibility(View.VISIBLE);
                viewHolder.imageAbbrev.setText(formatAbbrev(nameForImage));
            }
        }
        return convertView;
    }

    private String formatAbbrev(String name) {
        return name.substring(0, 1).toUpperCase();
    }

    private int nextColor(int position) {
        switch (position % 8) {
        case 0: return R.color.contact_color_1;
        case 1: return R.color.contact_color_2;
        case 2: return R.color.contact_color_3;
        case 3: return R.color.contact_color_4;
        case 4: return R.color.contact_color_5;
        case 5: return R.color.contact_color_6;
        case 6: return R.color.contact_color_7;
        default: return R.color.contact_color_8;
        }
    }

    static class ViewHolderItem {
        View imageWrapper;
        ImageView imageView;
        TextView imageAbbrev;
        TextView textView;
        TextView addressView;
    }

}
