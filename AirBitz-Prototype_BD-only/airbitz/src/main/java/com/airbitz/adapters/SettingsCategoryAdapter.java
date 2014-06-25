package com.airbitz.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.airbitz.R;

import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class SettingsCategoryAdapter extends ArrayAdapter<String> {

    private List<String> mCategories;
    private Context mContext;

    public SettingsCategoryAdapter(Context context, List<String> categories) {
        super(context, R.layout.item_listview_settings_categories,categories);
        mCategories = categories;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_settings_categories, parent, false);

        EditText mCategoryName = (EditText) convertView.findViewById(R.id.category_field);
        mCategoryName.setText(mCategories.get(position));

        mCategoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

            }
        });

        mCategoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final int pos = position;
        Button mDeleteButton = (Button) convertView.findViewById(R.id.category_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCategories.remove(pos);
                //TODO Remove From Core
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
