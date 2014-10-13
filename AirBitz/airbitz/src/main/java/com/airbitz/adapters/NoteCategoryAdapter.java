package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.models.Categories;
import com.airbitz.models.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/3/14.
 */
public class NoteCategoryAdapter extends BaseAdapter{

    public static final String TAG = VenueAdapter.class.getSimpleName();
    private Context mContext;
    private Categories mCategories;
    private List<Category> mListCategory;
    private LayoutInflater mInflater;
    private int mCurrentPosition = 0;

    public NoteCategoryAdapter(Context context, Categories categories){
        mContext = context;
        mCategories = categories;
        if(mCategories!=null){
            mListCategory = categories.getBusinessCategoryArray();
        } else {
            mListCategory = new ArrayList<Category>();
        }
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mListCategory.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Category getListItemName(int position){
        return mListCategory.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mCurrentPosition = position;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_location, parent, false);
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.layout_item);
        relativeLayout.setBackgroundResource(android.R.color.transparent);
        TextView textView = (TextView) convertView.findViewById(R.id.fragment_category_textview_title);
        textView.setText(mListCategory.get(position).getCategoryName());
        Typeface latoBlackTypeFace=Typeface.createFromAsset(mContext.getAssets(), "font/Lato-Bla.ttf");
        textView.setTypeface(latoBlackTypeFace);


        return convertView;
    }
}
