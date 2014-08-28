package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Point;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.objects.HighlightOnPressButton;

import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class SettingsCategoryAdapter extends ArrayAdapter<String> {

    private List<String> mCurrentCategories;
    private List<String> mCategories;
    private List<TextView> mPopUpViews; //0-edittext, 1-Expense, 2-Income, 3-Transfer, 4-currentItemEdittext
    private List<Integer> mCurrentPosPopUp; //0- currentPos in currentCategories, 1- currentPos in Categories
    private RelativeLayout mPopUpContainer;
    private Context mContext;

    public SettingsCategoryAdapter(Context context, List<String> currentCategories, List<String> categories, List<TextView> popUpViews, RelativeLayout popUpContainer, List<Integer> currentPosPopUp) {
        super(context, R.layout.item_listview_settings_categories,currentCategories);
        mCurrentCategories = currentCategories;
        mCategories = categories;
        mContext = context;
        mPopUpViews = popUpViews;
        mPopUpContainer = popUpContainer;
        mCurrentPosPopUp = currentPosPopUp;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_settings_categories, parent, false);

        final EditText mCategoryName = (EditText) convertView.findViewById(R.id.category_field);
        mCategoryName.setText(mCurrentCategories.get(position));

        final int pos = position;

        mCategoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    System.out.println("Cat name has focus");
                    mPopUpViews.get(0).setText(mCurrentCategories.get(pos));
                    mPopUpViews.get(0).requestFocus();
                    mPopUpContainer.setVisibility(View.VISIBLE);
                    mCurrentPosPopUp.add(pos);
                    for(String s :mCategories){
                        if(s.compareTo(mCurrentCategories.get(pos))==0){
                            mCurrentPosPopUp.add(mCategories.indexOf(s));
                        }
                    }
                }
            }
        });

        HighlightOnPressButton mDeleteButton = (HighlightOnPressButton) convertView.findViewById(R.id.category_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentCategories.remove(pos);
                mCategories.remove(pos);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
