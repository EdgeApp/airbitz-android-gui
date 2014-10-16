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
import com.airbitz.fragments.BusinessDirectoryFragment;
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
        textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
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
