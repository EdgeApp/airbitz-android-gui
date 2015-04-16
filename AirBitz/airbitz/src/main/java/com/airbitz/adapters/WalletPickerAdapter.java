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
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;

import java.util.List;

/**
 * Created by matt on 7/22/14.
 */
public class WalletPickerAdapter extends ArrayAdapter {

    private Context mContext;
    private List<Wallet> mWalletList;
    private WalletPickerEnum mSource;
    private boolean mWithBalance;
    private Typeface mBitcoinTypeface;

    public WalletPickerAdapter(Context context, List<Wallet> walletList, WalletPickerEnum source) {
        this(context, walletList, source, true);
    }

    public WalletPickerAdapter(Context context, List<Wallet> walletList, WalletPickerEnum source, boolean withBalance) {
        super(context, R.layout.item_request_wallet_spinner, walletList);
        mContext = context;
        mWalletList = walletList;
        mSource = source;
        mWithBalance = withBalance;
        mBitcoinTypeface = Typeface.createFromAsset(context.getAssets(), "font/Lato-Regular.ttf");
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_request_wallet_spinner_dropdown, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.item_request_wallet_spinner_dropdown_textview);
        textView.setTypeface(mBitcoinTypeface);
        textView.setText(mWalletList.get(position).getName() + " (" + mWalletList.get(position).getBalanceFormatted() + ")");
        textView.setBackground(mContext.getResources().getDrawable(R.drawable.dropdown_item_selector));
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String title = mWalletList.get(position).getName();
        if(mWithBalance) {
            title += " (" + mWalletList.get(position).getBalanceFormatted() + ")";
        }
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mSource == WalletPickerEnum.SendTo) {
            convertView = inflater.inflate(R.layout.item_send_listing_spinner, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.item_send_listing_spinner_textview);
            textView.setTypeface(mBitcoinTypeface);
            textView.setText(title);
        } else {
            convertView = inflater.inflate(R.layout.item_request_wallet_spinner, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.item_request_wallet_spinner_textview);
            textView.setTypeface(mBitcoinTypeface);
            textView.setText(title);
        }
        return convertView;
    }
}
