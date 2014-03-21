package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.BusinessDirectoryActivity;
import com.airbitz.activities.LandingActivity;

/**
 * Created on 2/13/14.
 */
public class ExportAdapter extends BaseAdapter {


    public static final String TAG = ExportAdapter.class.getSimpleName();
    private final Context mContext;
    private final String[] mMenus;
    private LayoutInflater mInflater;


    public ExportAdapter(Context context, String[] menu) {
        mContext = context;
        mMenus = menu;
        mInflater = LayoutInflater.from(mContext);

    }

    @Override
    public int getCount() {
        return mMenus.length;
    }

    @Override
    public Object getItem(int position) {
        return mMenus[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_business, parent, false);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.textview_business_title);
        nameTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageview_icon);
        nameTextView.setText(mMenus[position]);

        if(position == 0){
            imageView.setImageResource(R.drawable.ico_csv);
        }
        else if(position == 1){
            imageView.setImageResource(R.drawable.ico_quicken);
        }
        else if(position == 2){
            imageView.setImageResource(R.drawable.ico_quickooks);
        }
        else if(position == 3){
            imageView.setImageResource(R.drawable.ico_view);
        }

        return convertView;
    }

}
