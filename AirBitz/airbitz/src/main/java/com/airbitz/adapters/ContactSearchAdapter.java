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
import com.airbitz.models.Contact;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<Contact> mContacts;
    private final Picasso mPicasso;

    public ContactSearchAdapter(Context context, List<Contact> contacts) {
        super(context, R.layout.item_listview_icon_with_text);
        mContext = context;
        mContacts = contacts;
        mPicasso = Picasso.with(mContext);
    }

    @Override
    public int getCount() {
        return mContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return mContacts.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderItem viewHolder;
        if (convertView == null || null == convertView.getTag()) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_icon_with_text, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.contactName = (TextView) convertView.findViewById(R.id.item_name);
            viewHolder.contactName.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

            viewHolder.imageWrapper = convertView.findViewById(R.id.icon_wrapper);
            viewHolder.contactIcon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.contactAbbrev = (TextView) convertView.findViewById(R.id.icon_abbrev);

            viewHolder.contactAddress = (TextView) convertView.findViewById(R.id.item_description);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        Contact contact = mContacts.get(position);
        viewHolder.contactName.setText(contact.getName());
        if (contact.getEmail() != null) {
            viewHolder.contactAddress.setText(contact.getEmail());
        } else if (contact.getPhone() != null) {
            viewHolder.contactAddress.setText(contact.getPhone());
        }

        viewHolder.imageWrapper.setBackgroundResource(nextColor(position));
        if (contact.getName() != null && contact.getThumbnail() != null) {
            viewHolder.contactIcon.setVisibility(View.VISIBLE);
            mPicasso.load(Uri.parse(contact.getThumbnail())).noFade().into(viewHolder.contactIcon);
            viewHolder.contactAbbrev.setVisibility(View.GONE);
        } else {
            viewHolder.contactIcon.setVisibility(View.GONE);
            viewHolder.contactAbbrev.setVisibility(View.VISIBLE);
            viewHolder.contactAbbrev.setText(formatAbbrev(contact.getName()));
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
        TextView contactName;
        View imageWrapper;
        ImageView contactIcon;
        TextView contactAbbrev;
        TextView contactAddress;
    }
}
