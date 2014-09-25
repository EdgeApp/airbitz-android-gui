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
import com.airbitz.objects.Contact;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tom on 9/25/14.
 */
public class ContactSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<Contact> mContacts;
    private final Picasso picasso;

    public ContactSearchAdapter(Context context, List<Contact> contacts){
        super(context, R.layout.item_listview_transaction_detail);
        mContext = context;
        mContacts = contacts;
        picasso =  new Picasso.Builder(context).build();
    }

    @Override
    public int getCount() {
        return mContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return mContacts.get(position);
    }

    static class ViewHolderItem {
        ImageView imageView;
        TextView textView;
        TextView infoView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_transaction_detail_business, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.transaction_detail_item_name);
            viewHolder.textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.transaction_detail_item_imageview);
            viewHolder.infoView = (TextView) convertView.findViewById(R.id.transaction_detail_item_address);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        Contact contact = mContacts.get(position);
        viewHolder.textView.setText(contact.getName());
        if(contact.getEmail()!=null) {
            viewHolder.infoView.setText(contact.getEmail());
        } else if (contact.getPhone()!=null) {
            viewHolder.infoView.setText(contact.getPhone());
        }

        if(contact.getThumbnail()!=null)
            picasso.load(Uri.parse(mContacts.get(position).getThumbnail())).noFade().into(viewHolder.imageView);
        return convertView;
    }
}
