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
import android.widget.TextView;

import com.airbitz.R;

import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class SettingsCategoryAdapter extends ArrayAdapter<String> {

    private List<String> mCurrentCategories;
    private List<String> mCategories;
    private Context mContext;

    private boolean needFocus = false;
    private int needFocusPosition = -1;

    public SettingsCategoryAdapter(Context context, List<String> currentCategories, List<String> categories) {
        super(context, R.layout.item_listview_settings_categories,currentCategories);
        mCurrentCategories = currentCategories;
        mCategories = categories;
        mContext = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_settings_categories, parent, false);

        final EditText mCategoryName = (EditText) convertView.findViewById(R.id.category_field);
        mCategoryName.setText(mCurrentCategories.get(position));

        final int pos = position;

        if(pos == needFocusPosition && needFocus){
            mCategoryName.requestFocus();
        }

        mCategoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    needFocus = true;
                    needFocusPosition = pos;
                    System.out.println("Cat name has focus");
                }else{

                }
            }
        });
        mCategoryName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    needFocus = false;
                    needFocusPosition = -1;
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    return true;
                }
                return false;
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
        Button mDeleteButton = (Button) convertView.findViewById(R.id.category_delete);
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
