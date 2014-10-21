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
import com.airbitz.models.BusinessSearchResult;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

/**
 * Created by matt on 6/10/14.
 */
public class TransactionDetailSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<BusinessSearchResult> mBusinesses;
    private List<String> mContactNames;
    private Map<String, Uri> mContactPhotos;
    private List<Object> mCombined;
    private final Picasso mPicasso;

    public TransactionDetailSearchAdapter(Context context, List<BusinessSearchResult> businesses, List<String> contactNames, List<Object> combined, Map<String, Uri> contactPhotos) {
        super(context, R.layout.item_listview_transaction_detail, combined);
        mContext = context;
        mBusinesses = businesses;
        mContactNames = contactNames;
        mContactPhotos = contactPhotos;
        mCombined = combined;
        mPicasso = Picasso.with(context);
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
        ViewHolderItem viewHolder;
        if (convertView == null) {
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
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        String nameForImage = "";
        if (mCombined.get(position) instanceof BusinessSearchResult) {
            final BusinessSearchResult business = (BusinessSearchResult) mCombined.get(position);
            nameForImage = business.getName();
            viewHolder.textView.setText(nameForImage);

            if (business.getSquareProfileImage()!=null) {
                mPicasso.load(business.getSquareProfileImage().getImageThumbnail()).into(viewHolder.imageView);
            }


            // create the address
            String strAddress = "";
            if (business.getAddress() != null) {
                strAddress += business.getAddress();
            }
            if (business.getCity() != null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getCity();
            }
            if (business.getState() != null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getState();
            }
            if (business.getPostalCode() != null) {
                strAddress += (strAddress.length() > 0 ? ", " : "") + business.getPostalCode();
            }

            viewHolder.addressView.setVisibility(View.VISIBLE);
            viewHolder.addressView.setText(strAddress);
            viewHolder.addressView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        } else if (mCombined.get(position) instanceof String) {
            nameForImage = (String) mCombined.get(position);
            viewHolder.textView.setText(nameForImage);
            viewHolder.addressView.setVisibility(View.GONE);
        }
        mPicasso.load(mContactPhotos.get(nameForImage)).into(viewHolder.imageView);
        return convertView;
    }

    static class ViewHolderItem {
        ImageView imageView;
        TextView textView;
        TextView addressView;
    }

}
