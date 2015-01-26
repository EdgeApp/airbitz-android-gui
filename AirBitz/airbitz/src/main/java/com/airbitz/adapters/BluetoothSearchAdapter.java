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
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.models.BleDevice;
import com.airbitz.models.Contact;

import java.util.List;

/**
 * Created by tom on 9/25/14.
 */
public class BluetoothSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<BleDevice> mDevices;

    private int[] mRSSIImages = { R.drawable.zero_bars, R.drawable.zero_bars, R.drawable.two_bars,
            R.drawable.three_bars, R.drawable.four_bars, R.drawable.five_bars };

    public BluetoothSearchAdapter(Context context, List<BleDevice> devices) {
        super(context, R.layout.item_listview_transaction_detail);
        mContext = context;
        mDevices = devices;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_bluetooth_le, parent, false);

        BleDevice device = mDevices.get(position);

        TextView nameView = (TextView) convertView.findViewById(R.id.bluetooth_item_name);
        nameView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        TextView addressView = (TextView) convertView.findViewById(R.id.bluetooth_item_address);
        addressView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);

        String partialAddress = null;
        String name = null;
        if(device.getDevice().getName().length() > 10) {
            partialAddress = device.getDevice().getName().substring(0, 10);
            name = device.getDevice().getName().substring(10);
        }
        Contact nameInContacts = null;
        if(name != null) {
            nameInContacts = findMatchingContact(name);
            if(nameInContacts != null && nameInContacts.getThumbnail() != null && !nameInContacts.getThumbnail().isEmpty()) {
                ImageView thumbnail = ((ImageView) convertView.findViewById(R.id.bluetooth_item_imageview));
                thumbnail.setImageURI(Uri.parse(nameInContacts.getThumbnail()));
            }
            nameView.setText(name);
            addressView.setText(partialAddress);
        }
        else {
            nameView.setText("Unknown");
            addressView.setText("Error in advertiser string");
        }

        ImageView rssiView = (ImageView) convertView.findViewById(R.id.bluetooth_item_rssi_image);
        int mappedRssi = mapRSSI(device.getRSSI());
        if(mappedRssi > 0 && mappedRssi < mRSSIImages.length) {
            rssiView.setBackgroundResource(mRSSIImages[mappedRssi]);
        } else {
            rssiView.setBackgroundResource(mRSSIImages[0]);
        }

        return convertView;
    }

    public void selectItem(View view) {
        view.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard_selected));
    }

        private int mapRSSI(int rssi) {
        if(rssi >= -41)      return 5;
        else if(rssi >= -53) return 4;
        else if(rssi >= -65) return 3;
        else if(rssi >= -77) return 2;
        else if(rssi >= -89) return 1;
        else                 return 0;
    }

    private Contact findMatchingContact(String displayName) {
        if(displayName.split(" ").length < 2) {
            return null;
        }
        String id = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(displayName.trim()));
        Cursor mapContact = mContext.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI}, null, null, null);
        while(mapContact != null && mapContact.moveToNext())
        {
            String name = mapContact.getString(mapContact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            id = mapContact.getString(mapContact.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            if(displayName.toLowerCase().contains(name.toLowerCase())) {
                return new Contact(displayName, null, null, id);
            }
        }
        return null;
    }
}
