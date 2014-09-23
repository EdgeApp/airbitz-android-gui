package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.StringBusinessTypeEnum;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by matt on 6/10/14.
 */
public class TransactionDetailSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<BusinessSearchResult> mBusinesses;
    private List<String> mContactNames;
    private Map<String,Uri> mContactPhotos;
    private List<Object> mCombined;
    private final Picasso p;

    public TransactionDetailSearchAdapter(Context context, List<BusinessSearchResult> businesses, List<String> contactNames, List<Object> combined, Map<String, Uri> contactPhotos){
        super(context, R.layout.item_listview_transaction_detail, combined);
        mContext = context;
        mBusinesses = businesses;
        mContactNames = contactNames;
        mContactPhotos = contactPhotos;
        mCombined = combined;
        p =  new Picasso.Builder(context).build();
    }

    @Override
    public int getCount() {
        return mCombined.size();
    }

    @Override
    public Object getItem(int position) {
        return mCombined.get(position);
    }

    static class ViewHolderItem {
        ImageView imageView;
        TextView textView;
        TextView addressView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if(convertView==null){
            // well set up the ViewHolder
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_transaction_detail_business, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.transaction_detail_item_name);
            viewHolder.textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.transaction_detail_item_imageview);
            viewHolder.addressView = (TextView) convertView.findViewById(R.id.transaction_detail_item_address);
            // store the holder with the view.
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        if(mCombined.get(position) instanceof BusinessSearchResult) {
            final BusinessSearchResult business = (BusinessSearchResult) mCombined.get(position);
            viewHolder.textView.setText(business.getName());

            if(business.getSquareProfileImage()!=null)
                p.load(business.getSquareProfileImage().getImageThumbnail()).noFade().into(viewHolder.imageView);


            // create the address
            String strAddress = "";
            if (business.getAddress()!=null) {
                strAddress += business.getAddress();
            }
            if (business.getCity()!=null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getCity();
            }
            if (business.getState()!=null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getState();
            }
            if (business.getPostalCode()!=null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getPostalCode();
            }

            viewHolder.addressView.setVisibility(View.VISIBLE);
            viewHolder.addressView.setText(strAddress);
            viewHolder.addressView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        }else if(mCombined.get(position) instanceof String){
            final String contactName = (String) mCombined.get(position);
            viewHolder.textView.setText(contactName);
            viewHolder.addressView.setVisibility(View.GONE);
        }
        p.load(mContactPhotos.get(viewHolder.textView.getText())).noFade().into(viewHolder.imageView);
        return convertView;
    }

}
