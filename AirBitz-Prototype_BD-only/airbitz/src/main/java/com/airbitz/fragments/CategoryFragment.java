package com.airbitz.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.airbitz.api.CoreAPI;
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
    private TextView mAddExchangePopUpTextView;
    private RelativeLayout mAddTriangleContainer;

    private LinearLayout mDoneCancelContainer;

    private RelativeLayout mItemPopUpContainer;
    private EditText mItemPopUpEdittext;
    private TextView mItemPopUpExpenseTextView;
    private TextView mItemPopUpIncomeTextView;
    private TextView mItemPopUpTransferTextView;
    private TextView mItemPopUpExchangeTextView;
    private Button mItemPopUpDelete;

    private List<TextView> popUpViews;

    private Button mAddButton;
    private Button mCancelButton;
    private Button mDoneButton;

    private boolean doEdit = false;
    private boolean popupDoEdit = false;


    private String currentType = "";
    private String mCategoryOld = "";
    private String mPopUpCurrentType = "";
    private String mPopUpCategoryOld = "";

    private ListView mCategoryListView;
    private SettingsCategoryAdapter mCategoryAdapter;
    private List<String> mCategories;
    private List<String> mCurrentCategories;
    private List<Integer> currentPosPopUp;//O - currentpos
    Activity activity;

    CoreAPI mCoreAPI;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_category, container, false);

        activity = getActivity();

        //  Inflate Fields
        mAddField = (EditText) mView.findViewById(R.id.add_field);
        mSearchField = (EditText) mView.findViewById(R.id.search_field);

        mAddButton = (Button) mView.findViewById(R.id.settings_category_button_add);
        mCancelButton = (Button) mView.findViewById(R.id.button_cancel);
        mDoneButton = (Button) mView.findViewById(R.id.button_done);

        mAddPopUpContainer = (LinearLayout) mView.findViewById(R.id.add_popup_container);
        mAddExpensePopUpTextView = (TextView) mView.findViewById(R.id.add_popup_expense);
        mAddIncomePopUpTextView = (TextView) mView.findViewById(R.id.add_popup_income);
        mAddTransferPopUpTextView = (TextView) mView.findViewById(R.id.add_popup_transfer);
        mAddExchangePopUpTextView = (TextView) mView.findViewById(R.id.add_popup_exchange);
        mAddTriangleContainer = (RelativeLayout) mView.findViewById(R.id.add_popup_triangle_container);

        mItemPopUpContainer = (RelativeLayout) mView.findViewById(R.id.popup_container);
        mItemPopUpEdittext = (EditText) mView.findViewById(R.id.item_popup_edittext);            //0
        mItemPopUpExpenseTextView = (TextView) mView.findViewById(R.id.item_popup_expense);      //1
        mItemPopUpIncomeTextView = (TextView) mView.findViewById(R.id.item_popup_income);        //2
        mItemPopUpTransferTextView = (TextView) mView.findViewById(R.id.item_popup_transfer);    //3
        mItemPopUpExchangeTextView = (TextView) mView.findViewById(R.id.item_popup_exchange);   //4
        mItemPopUpDelete = (Button) mView.findViewById(R.id.item_popup_delete);

        popUpViews = new ArrayList<TextView>();
        popUpViews.add(mItemPopUpEdittext);
        popUpViews.add(mItemPopUpExpenseTextView);
        popUpViews.add(mItemPopUpIncomeTextView);
        popUpViews.add(mItemPopUpTransferTextView);
        popUpViews.add(mItemPopUpExchangeTextView);
        currentPosPopUp = new ArrayList<Integer>();

        dummyFocus = mView.findViewById(R.id.settings_category_dummy_focus);

        mCategoryListView = (ListView) mView.findViewById(R.id.category_list_view);
        mCategories = new ArrayList<String>();
        mCurrentCategories = new ArrayList<String>();
        goAddCategories();
        mCategoryAdapter = new SettingsCategoryAdapter(getActivity(),mCurrentCategories, mCategories, popUpViews, mItemPopUpContainer, currentPosPopUp);
        mCategoryListView.setAdapter(mCategoryAdapter);

        mDoneCancelContainer = (LinearLayout) mView.findViewById(R.id.done_cancel_container);

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
                    mAddTriangleContainer.setVisibility(View.VISIBLE);
                    if(mAddField.getText().toString().isEmpty()){
                        mAddField.append("Expense:");
                        currentType = "Expense:";
                        mAddField.setSelection(mAddField.getText().toString().length());
                    }else{
                        mAddField.setSelection(mAddField.getText().toString().indexOf(':')+1,mAddField.getText().toString().length());
                    }
                }else {
                    mAddPopUpContainer.setVisibility(View.GONE);
                    mAddTriangleContainer.setVisibility(View.GONE);
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
                if(!doEdit) {
                    if ((currentType.equals("Income:") && !editable.toString().startsWith("Income:")) || (currentType.equals("Expense:") && !editable.toString().startsWith("Expense:")) || (currentType.equals("Transfer:") && !editable.toString().startsWith("Transfer:")) || (currentType.equals("Exchange:") && !editable.toString().startsWith("Exchange:"))) {
                        doEdit = true;
                        editable.clear();
                        editable.append(mCategoryOld);
                        doEdit = false;
                    }
                    updateAddBlanks(editable.toString().substring(editable.toString().indexOf(':') + 1));
                    mCategoryOld = mAddField.getText().toString();
                }
            }
        });

        mAddExpensePopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddExpensePopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = "Expense:";
                doEdit = false;
            }
        });

        mAddIncomePopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddIncomePopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = "Income:";
                doEdit = false;
            }
        });

        mAddTransferPopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddTransferPopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = "Transfer:";
                doEdit = false;
            }
        });

        mAddExchangePopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddExchangePopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = "Exchange:";
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
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                builder.setMessage("Are you sure you want to cancel any changes you've made?")
                        .setTitle("Cancel Changes")
                        .setCancelable(true)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        getActivity().onBackPressed();
                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }
                        );
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAllChanges();
                getActivity().onBackPressed();
            }
        });

        //Pop UP
        mItemPopUpEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    mPopUpCurrentType = mItemPopUpEdittext.getText().toString().substring(0,mItemPopUpEdittext.getText().toString().indexOf(':')+1);
                    mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().indexOf(':')+1,mItemPopUpEdittext.getText().toString().length());
                    updateItemBlanks(mItemPopUpEdittext.getText().toString().substring(mItemPopUpEdittext.getText().toString().indexOf(':')+1));
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        mItemPopUpEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    mCategories.set(currentPosPopUp.get(1),mItemPopUpEdittext.getText().toString());
                    mCurrentCategories.clear();
                    for(String s :mCategories){
                        if(s.toLowerCase().contains(mSearchField.getText().toString().toLowerCase())){
                            mCurrentCategories.add(s);
                        }
                    }
                    currentPosPopUp.clear();
                    mCategoryAdapter.notifyDataSetChanged();
                    mItemPopUpContainer.setVisibility(View.GONE);
                    dummyFocus.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    return true;
                }
                return false;
            }
        });

        mItemPopUpEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!popupDoEdit) {
                    if ((mPopUpCurrentType.equals("Income:") && !editable.toString().startsWith("Income:")) || (mPopUpCurrentType.equals("Expense:") && !editable.toString().startsWith("Expense:")) || (mPopUpCurrentType.equals("Transfer:") && !editable.toString().startsWith("Transfer:")) || (mPopUpCurrentType.equals("Exchange:") && !editable.toString().startsWith("Exchange:"))) {
                        popupDoEdit = true;
                        editable.clear();
                        editable.append(mPopUpCategoryOld);
                        popupDoEdit = false;
                    }
                    updateItemBlanks(editable.toString().substring(editable.toString().indexOf(':') + 1));
                    mPopUpCategoryOld = mAddField.getText().toString();
                }
            }
        });

        mItemPopUpExpenseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDoEdit = true;
                mItemPopUpEdittext.setText(mItemPopUpExpenseTextView.getText());
                mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().length());
                mPopUpCurrentType = "Expense:";
                popupDoEdit = false;
            }
        });

        mItemPopUpIncomeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDoEdit = true;
                mItemPopUpEdittext.setText(mItemPopUpIncomeTextView.getText());
                mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().length());
                mPopUpCurrentType = "Income:";
                popupDoEdit = false;
            }
        });

        mItemPopUpTransferTextView.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View view) {
                popupDoEdit = true;
                mItemPopUpEdittext.setText(mItemPopUpTransferTextView.getText());
                mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().length());
                mPopUpCurrentType = "Transfer:";
                popupDoEdit = false;
            }
        });

        mItemPopUpExchangeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDoEdit = true;
                mItemPopUpEdittext.setText(mItemPopUpExchangeTextView.getText());
                mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().length());
                mPopUpCurrentType = "Exchange:";
                popupDoEdit = false;
            }
        });

        mItemPopUpDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentCategories.remove(currentPosPopUp.get(0).intValue());
                mCategories.remove(currentPosPopUp.get(1).intValue());
                currentPosPopUp.clear();
                mCategoryAdapter.notifyDataSetChanged();
                mItemPopUpContainer.setVisibility(View.GONE);
                dummyFocus.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        dummyFocus.requestFocus();
        return mView;
    }

    private void goAddCategories(){
        mCategories = mCoreAPI.loadCategories();
        mCurrentCategories.addAll(mCategories);
    }

    private void updateAddBlanks(String term){
        mAddExpensePopUpTextView.setText("Expense:" + term);
        mAddIncomePopUpTextView.setText("Income:" + term);
        mAddTransferPopUpTextView.setText("Transfer:" + term);
        mAddExchangePopUpTextView.setText("Exchange:" + term);
    }

    private void updateItemBlanks(String term){
        mItemPopUpExpenseTextView.setText("Expense:" + term);
        mItemPopUpIncomeTextView.setText("Income:" + term);
        mItemPopUpTransferTextView.setText("Transfer:" + term);
        mItemPopUpExchangeTextView.setText("Exchange:" + term);
    }

    private void goAddNewCategory(){
        String newCat;
        if(!mAddField.getText().toString().substring(mAddField.getText().toString().indexOf(':')+1).trim().isEmpty()){
            newCat = mAddField.getText().toString();
            mCategories.add(newCat);
            if(newCat.toLowerCase().contains(mSearchField.getText().toString().toLowerCase())){
                mCurrentCategories.add(newCat);
            }
            mCategoryAdapter.notifyDataSetChanged();
            doEdit = true;
            mAddField.getText().clear();
            updateItemBlanks("");
            doEdit = false;
        }
        dummyFocus.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        mAddPopUpContainer.setVisibility(View.GONE);
        mAddTriangleContainer.setVisibility(View.GONE);
    }

    public void showDoneCancel(){
        mDoneCancelContainer.setVisibility(View.VISIBLE);
    }

    public void hideDoneCancel(){
        mDoneCancelContainer.setVisibility(View.GONE);
    }

    private void SaveAllChanges() {
        List<String> coreCategories = mCoreAPI.loadCategories();
        // Remove any categories first
        for(String category : coreCategories) {
            if(!mCategories.contains(category)) {
                mCoreAPI.removeCategory(category);
            }
        }

        // Add any categories
        for(String category : mCategories) {
            mCoreAPI.addCategory(category);
        }
    }
}
