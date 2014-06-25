package com.airbitz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.SettingsCategoryAdapter;
import com.airbitz.models.CategoryTypeEnum;

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

    private CategoryTypeEnum currentType = CategoryTypeEnum.Expense;

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
        mCategoryListView.setAdapter(mCategoryAdapter);

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
                mCurrentCategories.clear();
                for(String s :mCategories){
                    if(s.toLowerCase().contains(editable.toString().toLowerCase())){
                        mCurrentCategories.add(s);
                    }
                }
                mCategoryAdapter.notifyDataSetChanged();
            }
        });

        mAddField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    goAddNewCategory();
                }
                return false;
            }
        });


        //  Buttons
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goAddNewCategory();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Popup
                getActivity().onBackPressed();
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Save to core
                getActivity().onBackPressed();
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

    private void goAddNewCategory(){
        String newCat;
        if(currentType == CategoryTypeEnum.Income){
            newCat = mAddField.getText().toString().substring(7);
        }else if(currentType == CategoryTypeEnum.Expense){

        }else if(currentType == CategoryTypeEnum.Transfer){

        }
    }
}
