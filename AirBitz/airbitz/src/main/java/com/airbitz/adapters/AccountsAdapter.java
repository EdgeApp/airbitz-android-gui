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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;

import java.util.List;

/**
 * Created 2/28/15.
 */
public class AccountsAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mUsernames;
    private boolean mDrawer;

    private OnButtonTouched mOnButtonTouchedListener;
    public interface OnButtonTouched {
        public void onButtonTouched(String account);
    }
    public void setButtonTouchedListener(OnButtonTouched listener) {
        mOnButtonTouchedListener = listener;
    }

    public AccountsAdapter(Context context, List<String> usernames) {
        mContext = context;
        mUsernames = usernames;
    }

    public AccountsAdapter(Context context, List<String> usernames, boolean drawer) {
        this(context, usernames);
        mDrawer = drawer;
    }

    @Override
    public int getCount() {
        return mUsernames.size();
    }

    @Override
    public String getItem(int position) {
        return mUsernames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if(mDrawer) {
            convertView = inflater.inflate(R.layout.item_accounts_drawer, parent, false);
        }
        else {
            convertView = inflater.inflate(R.layout.item_accounts_login, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.item_usernames_text);
        textView.setText(mUsernames.get(position));
        textView.setTypeface(NavigationActivity.latoRegularTypeFace);

        // Show delete button if there is a listener
        if(mOnButtonTouchedListener != null) {
            ImageView imageView = (ImageView) convertView.findViewById(R.id.item_usernames_button);
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnButtonTouchedListener != null) {
                        mOnButtonTouchedListener.onButtonTouched(mUsernames.get(position));
                    }
                }
            });
        }
        return convertView;
    }
}
