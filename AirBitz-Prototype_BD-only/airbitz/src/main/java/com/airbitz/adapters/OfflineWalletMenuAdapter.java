package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbitz.R;

/**
 * Created on 2/13/14.
 */
public class OfflineWalletMenuAdapter extends ArrayAdapter<String>{

    private Context mContext;
    private String[] mMenus;

    public OfflineWalletMenuAdapter(Context context, String[] menus){
        super(context, R.layout.item_listview_menu_wallet_address, menus);

        mContext = context;
        mMenus = menus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_menu_wallet_address, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.fragment_category_textview_title);
        textView.setText(mMenus[position]);
        return convertView;
    }
}
