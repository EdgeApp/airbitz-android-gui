package com.airbitz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.SettingsCategoryAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class CategoryFragment extends Fragment {

    private EditText mAddField;
    private EditText mSearchField;

    private Button mAddButton;
    private Button mCancelButton;
    private Button mDoneButton;

    private ListView mCategoryListView;
    private SettingsCategoryAdapter mCategoryAdapter;
    private List<String> mCategories;
    private List<String> mCurrentCategories;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);


        //  Inflate Fields
        mAddField = (EditText) view.findViewById(R.id.add_field);
        mSearchField = (EditText) view.findViewById(R.id.search_field);

        mAddButton = (Button) view.findViewById(R.id.button_add);
        mCancelButton = (Button) view.findViewById(R.id.button_cancel);
        mDoneButton = (Button) view.findViewById(R.id.button_done);

        mCategoryListView = (ListView) view.findViewById(R.id.category_list_view);
        mCategories = new ArrayList<String>();
        mCurrentCategories = new ArrayList<String>();
        goAddCategories();
        mCategoryAdapter = new SettingsCategoryAdapter(getActivity(),mCurrentCategories);

        //  EditTexts
        mSearchField.addTextChangedListener(new TextWatcher() {
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

        mAddField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });


        //  Buttons
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //  ListView
        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        return view;
    }

    private void goAddCategories(){
        //TODO retrieve categories from core
        mCategories.add("Expense:Balloons");
        mCategories.add("Expense:Cotton Candy");
        mCategories.add("Income:Newspaper Route");
        mCurrentCategories.addAll(mCategories);
    }
}
