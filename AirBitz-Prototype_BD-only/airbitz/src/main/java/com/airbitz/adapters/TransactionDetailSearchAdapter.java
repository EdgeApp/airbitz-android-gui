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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(mCombined.get(position) instanceof BusinessSearchResult) {
            final BusinessSearchResult business = (BusinessSearchResult) mCombined.get(position);
            convertView = inflater.inflate(R.layout.item_listview_transaction_detail_business, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.transaction_detail_item_name);
            textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
            textView.setText(business.getName());
            ImageView imageView = (ImageView) convertView.findViewById(R.id.transaction_detail_item_imageview);
            p.load(business.getSquareProfileImage().getImageThumbnail()).noFade().into(imageView);
            TextView addressView = (TextView) convertView.findViewById(R.id.transaction_detail_item_address);

            String s = business.getCountry();
            if(!s.isEmpty() && !business.getState().isEmpty()){
                s = business.getState()+ ", " + s;
            }
            if(!s.isEmpty() && !business.getCity().isEmpty()){
                s = business.getCity()+ ", " + s;
            }
            if(!s.isEmpty() && !business.getAddress().isEmpty()){
                s = business.getAddress()+ ", " + s;
            }
            addressView.setText(s);
            addressView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        }else if(mCombined.get(position) instanceof String){
            final String contactName = (String) mCombined.get(position);
            convertView = inflater.inflate(R.layout.item_listview_transaction_detail_contact, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.transaction_detail_item_name);
            textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
            textView.setText(contactName);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.transaction_detail_item_imageview);
            p.load(mContactPhotos.get(contactName)).noFade().into(imageView);
        }
        return convertView;
    }

}
