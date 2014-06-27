package com.airbitz.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.SettingsCategoryAdapter;
import com.airbitz.models.Category;
import com.airbitz.models.CategoryTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class CategoryFragment extends Fragment {

    private EditText mAddField;
    private EditText mSearchField;

    private View dummyFocus;

    private LinearLayout mAddPopUpContainer;
    private TextView mAddExpensePopUpTextView;
    private TextView mAddIncomePopUpTextView;
    private TextView mAddTransferPopUpTextView;

    private LinearLayout mDoneCancelContainer;

    private RelativeLayout mCategoryPage;

    private Button mAddButton;
    private Button mCancelButton;
    private Button mDoneButton;

    private boolean doEdit = false;

    private CategoryTypeEnum currentType = CategoryTypeEnum.Expense;

    private ListView mCategoryListView;
    private SettingsCategoryAdapter mCategoryAdapter;
    private List<String> mCategories;
    private List<String> mCurrentCategories;
    Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_category, container, false);

        activity = getActivity();

        //  Inflate Fields
        mAddField = (EditText) view.findViewById(R.id.add_field);
        mSearchField = (EditText) view.findViewById(R.id.search_field);

        mAddButton = (Button) view.findViewById(R.id.settings_category_button_add);
        mCancelButton = (Button) view.findViewById(R.id.button_cancel);
        mDoneButton = (Button) view.findViewById(R.id.button_done);

        mAddPopUpContainer = (LinearLayout) view.findViewById(R.id.add_popup_container);
        mAddExpensePopUpTextView = (TextView) view.findViewById(R.id.add_popup_expense);
        mAddIncomePopUpTextView = (TextView) view.findViewById(R.id.add_popup_income);
        mAddTransferPopUpTextView = (TextView) view.findViewById(R.id.add_popup_transfer);

        dummyFocus = view.findViewById(R.id.settings_category_dummy_focus);

        mCategoryListView = (ListView) view.findViewById(R.id.category_list_view);
        mCategories = new ArrayList<String>();
        mCurrentCategories = new ArrayList<String>();
        goAddCategories();
        mCategoryAdapter = new SettingsCategoryAdapter(getActivity(),mCurrentCategories, mCategories);
        mCategoryListView.setAdapter(mCategoryAdapter);

        mCategoryPage = (RelativeLayout) view.findViewById(R.id.category_page);
        mDoneCancelContainer = (LinearLayout) view.findViewById(R.id.done_cancel_container);

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

        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    dummyFocus.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    return true;
                }
                return false;
            }
        });

        mAddField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    mAddPopUpContainer.setVisibility(View.VISIBLE);
                    if(mAddField.getText().toString().isEmpty()){
                        mAddField.append("Expense:");
                        mAddField.setSelection(mAddField.getText().toString().length());
                    }else{

                    }
                }else {
                    mAddPopUpContainer.setVisibility(View.GONE);
                }
            }
        });

        mAddField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    goAddNewCategory();
                    return true;
                }
                return false;
            }
        });

        mAddField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(false == doEdit) {
                    if (CategoryTypeEnum.Income == currentType) {
                        if (editable.toString().length() < 7 || editable.toString().substring(0, 7).compareTo("Income:") != 0) {
                            if (editable.toString().length() > 7) {
                                String temp = editable.toString().substring(7);
                                doEdit = true;
                                editable.clear();
                                editable.append("Income:" + temp);
                                doEdit = false;
                            } else {
                                doEdit = true;
                                editable.clear();
                                editable.append("Income:");
                                doEdit = false;
                            }
                        } else if (editable.toString().length() >= 7) {
                            mAddExpensePopUpTextView.setText("Expense:" + editable.toString().substring(7));
                            mAddIncomePopUpTextView.setText("Income:" + editable.toString().substring(7));
                            mAddTransferPopUpTextView.setText("Transfer:" + editable.toString().substring(7));
                        }
                        if (mAddField.getSelectionStart() < 7) {
                            mAddField.setSelection(7, 7);
                        }
                    } else if (CategoryTypeEnum.Expense == currentType) {
                        if (editable.toString().length() < 8 || editable.toString().substring(0, 8).compareTo("Expense:") != 0) {
                            if (editable.toString().length() > 8) {
                                String temp = editable.toString().substring(8);
                                doEdit = true;
                                editable.clear();
                                editable.append("Expense:" + temp);
                                doEdit = false;
                            } else {
                                doEdit = true;
                                editable.clear();
                                editable.append("Expense:");
                                doEdit = false;
                            }
                        } else if (editable.toString().length() >= 8) {
                            mAddExpensePopUpTextView.setText("Expense:" + editable.toString().substring(8));
                            mAddIncomePopUpTextView.setText("Income:" + editable.toString().substring(8));
                            mAddTransferPopUpTextView.setText("Transfer:" + editable.toString().substring(8));
                        }
                        if (mAddField.getSelectionStart() < 8) {
                            mAddField.setSelection(8, 8);
                        }
                    } else if (CategoryTypeEnum.Transfer == currentType) {
                        if (editable.toString().length() < 9 || editable.toString().substring(0, 9).compareTo("Transfer:") != 0) {
                            if (editable.toString().length() > 9) {
                                String temp = editable.toString().substring(9);
                                doEdit = true;
                                editable.clear();
                                editable.append("Transfer:" + temp);
                                doEdit = false;
                            } else {
                                doEdit = true;
                                editable.clear();
                                editable.append("Transfer:");
                                doEdit = false;
                            }
                        } else if (editable.toString().length() >= 9) {
                            mAddExpensePopUpTextView.setText("Expense:" + editable.toString().substring(9));
                            mAddIncomePopUpTextView.setText("Income:" + editable.toString().substring(9));
                            mAddTransferPopUpTextView.setText("Transfer:" + editable.toString().substring(9));
                        }
                        if (mAddField.getSelectionStart() < 9) {
                            mAddField.setSelection(9, 9);
                        }
                    } else {
                        System.err.println("currentType was something other than Income, Expense or Transfer: " + currentType);
                    }
                }
            }
        });

        mAddExpensePopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddExpensePopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = CategoryTypeEnum.Expense;
                doEdit = false;
            }
        });

        mAddIncomePopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddIncomePopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = CategoryTypeEnum.Income;
                doEdit = false;
            }
        });

        mAddTransferPopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddTransferPopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = CategoryTypeEnum.Transfer;
                doEdit = false;
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
                //TODO Save to core (mCategories)
                getActivity().onBackPressed();
            }
        });

        dummyFocus.requestFocus();
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
        if(currentType == CategoryTypeEnum.Transfer){
            if(mAddField.getText().toString().length()>9 && mAddField.getText().toString().compareTo("Transfer:")!=0) {
                newCat = mAddField.getText().toString();
                mCategories.add(newCat);
                if(newCat.toLowerCase().contains(mSearchField.getText().toString().toLowerCase())){
                    mCurrentCategories.add(newCat);
                }
                mCategoryAdapter.notifyDataSetChanged();
                mAddField.getText().clear();
            }
        }else if(currentType == CategoryTypeEnum.Expense){
            if(mAddField.getText().toString().length()>8 && mAddField.getText().toString().compareTo("Transfer:")!=0) {
                newCat = mAddField.getText().toString();
                mCategories.add(newCat);
                if(newCat.toLowerCase().contains(mSearchField.getText().toString().toLowerCase())){
                    mCurrentCategories.add(newCat);
                }
                mCategoryAdapter.notifyDataSetChanged();
                mAddField.getText().clear();
            }
        }else if(currentType == CategoryTypeEnum.Income){
            if(mAddField.getText().toString().length()>7 && mAddField.getText().toString().compareTo("Income:")!=0) {
                newCat = mAddField.getText().toString();
                mCategories.add(newCat);
                if(newCat.toLowerCase().contains(mSearchField.getText().toString().toLowerCase())){
                    mCurrentCategories.add(newCat);
                }
                mCategoryAdapter.notifyDataSetChanged();
                mAddField.getText().clear();
            }
        }
        dummyFocus.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        mAddPopUpContainer.setVisibility(View.GONE);
    }

    public void showDoneCancel(){
        mDoneCancelContainer.setVisibility(View.VISIBLE);
    }

    public void hideDoneCancel(){
        mDoneCancelContainer.setVisibility(View.GONE);
    }
}
