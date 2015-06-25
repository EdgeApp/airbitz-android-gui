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

/**
 * Created by tom on 9/25/14.
 */
public class ContactSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<Contact> mContacts;
    private final Picasso mPicasso;

    public ContactSearchAdapter(Context context, List<Contact> contacts) {
        super(context, R.layout.item_listview_transaction_detail);
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
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_transaction_detail_business, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.transaction_detail_item_name);
        textView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.transaction_detail_item_imageview);
        TextView infoView = (TextView) convertView.findViewById(R.id.transaction_detail_item_address);

        Contact contact = mContacts.get(position);
        textView.setText(contact.getName());
        if (contact.getEmail() != null) {
            infoView.setText(contact.getEmail());
        } else if (contact.getPhone() != null) {
            infoView.setText(contact.getPhone());
        }

        if (contact.getName() != null && contact.getThumbnail() != null) {
            mPicasso.load(Uri.parse(contact.getThumbnail())).noFade().into(imageView);
        }
        return convertView;
    }

    static class ViewHolderItem {
        ImageView imageView;
        TextView textView;
        TextView infoView;
    }
}
