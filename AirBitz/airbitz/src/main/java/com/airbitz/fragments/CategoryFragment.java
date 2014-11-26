/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.SettingsCategoryAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.HighlightOnPressButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class CategoryFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    Activity mParentActivity;
    CoreAPI mCoreAPI;
    private EditText mAddField;
    private EditText mSearchField;
    private TextView mTitleTextView;
    private View dummyFocus;
    private LinearLayout mAddPopUpContainer;
    private TextView mAddExpensePopUpTextView;
    private TextView mAddIncomePopUpTextView;
    private TextView mAddTransferPopUpTextView;
    private TextView mAddExchangePopUpTextView;
    private LinearLayout mDoneCancelContainer;
    private RelativeLayout mItemPopUpContainer;
    private EditText mItemPopUpEdittext;
    private TextView mItemPopUpExpenseTextView;
    private TextView mItemPopUpIncomeTextView;
    private TextView mItemPopUpTransferTextView;
    private TextView mItemPopUpExchangeTextView;
    private HighlightOnPressButton mItemPopUpDelete;
    private View mRootView;
    private List<TextView> popUpViews;
    private HighlightOnPressButton mAddButton;
    private HighlightOnPressButton mCancelButton;
    private HighlightOnPressButton mDoneButton;
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
    private List<Integer> currentPosPopUp;
    private boolean mKeyboardUp = false;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_category, container, false);

        mRootView = mView.findViewById(R.id.category_page);

        mParentActivity = getActivity();

        mParentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mAddField = (EditText) mView.findViewById(R.id.add_field);
        mSearchField = (EditText) mView.findViewById(R.id.search_field);

        mAddButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_category_button_add);
        mCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.button_cancel);
        mDoneButton = (HighlightOnPressButton) mView.findViewById(R.id.button_done);

        mAddPopUpContainer = (LinearLayout) mView.findViewById(R.id.add_popup_triangle_container);
        mAddExpensePopUpTextView = (TextView) mView.findViewById(R.id.add_popup_expense);
        mAddIncomePopUpTextView = (TextView) mView.findViewById(R.id.add_popup_income);
        mAddTransferPopUpTextView = (TextView) mView.findViewById(R.id.add_popup_transfer);
        mAddExchangePopUpTextView = (TextView) mView.findViewById(R.id.add_popup_exchange);

        mItemPopUpContainer = (RelativeLayout) mView.findViewById(R.id.popup_container);
        mItemPopUpEdittext = (EditText) mView.findViewById(R.id.item_popup_edittext);            //0
        mItemPopUpExpenseTextView = (TextView) mView.findViewById(R.id.item_popup_expense);      //1
        mItemPopUpIncomeTextView = (TextView) mView.findViewById(R.id.item_popup_income);        //2
        mItemPopUpTransferTextView = (TextView) mView.findViewById(R.id.item_popup_transfer);    //3
        mItemPopUpExchangeTextView = (TextView) mView.findViewById(R.id.item_popup_exchange);   //4
        mItemPopUpDelete = (HighlightOnPressButton) mView.findViewById(R.id.item_popup_delete);

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
        mCategoryAdapter = new SettingsCategoryAdapter(getActivity(), mCurrentCategories, mCategories, popUpViews, mItemPopUpContainer, currentPosPopUp);
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
                for (String s : mCategories) {
                    if (s.toLowerCase().contains(editable.toString().toLowerCase())) {
                        mCurrentCategories.add(s);
                    }
                }
                mCategoryAdapter.notifyDataSetChanged();
            }
        });

        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
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
                if (hasFocus) {
                    mAddPopUpContainer.setVisibility(View.VISIBLE);
                    if (mAddField.getText().toString().isEmpty()) {
                        mAddField.append(getString(R.string.fragment_category_expense));
                        currentType = getString(R.string.fragment_category_expense);
                        mAddField.setSelection(mAddField.getText().toString().length());
                    } else {
                        mAddField.setSelection(mAddField.getText().toString().indexOf(':') + 1, mAddField.getText().toString().length());
                    }
                } else {
                    mAddPopUpContainer.setVisibility(View.GONE);
                }
            }
        });

        mAddField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
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
                if (!doEdit) {
                    if ((currentType.equals(getString(R.string.fragment_category_income)) && !editable.toString().startsWith(getString(R.string.fragment_category_income))) || (currentType.equals(getString(R.string.fragment_category_expense)) && !editable.toString().startsWith(getString(R.string.fragment_category_expense))) || (currentType.equals(getString(R.string.fragment_category_transfer)) && !editable.toString().startsWith(getString(R.string.fragment_category_transfer))) || (currentType.equals(getString(R.string.fragment_category_exchange)) && !editable.toString().startsWith(getString(R.string.fragment_category_exchange)))) {
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
                currentType = getString(R.string.fragment_category_expense);
                doEdit = false;
            }
        });

        mAddIncomePopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddIncomePopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = getString(R.string.fragment_category_income);
                doEdit = false;
            }
        });

        mAddTransferPopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddTransferPopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = getString(R.string.fragment_category_transfer);
                doEdit = false;
            }
        });

        mAddExchangePopUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEdit = true;
                mAddField.setText(mAddExchangePopUpTextView.getText());
                mAddField.setSelection(mAddField.getText().toString().length());
                currentType = getString(R.string.fragment_category_exchange);
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
                builder.setMessage(getString(R.string.fragment_category_cancel_message))
                        .setTitle(getString(R.string.fragment_category_cancel_title))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.string_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        getActivity().onBackPressed();
                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.string_no),
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
                if (hasFocus) {
                    mPopUpCurrentType = mItemPopUpEdittext.getText().toString().substring(0, mItemPopUpEdittext.getText().toString().indexOf(':') + 1);
                    mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().indexOf(':') + 1, mItemPopUpEdittext.getText().toString().length());
                    updateItemBlanks(mItemPopUpEdittext.getText().toString().substring(mItemPopUpEdittext.getText().toString().indexOf(':') + 1));
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        mItemPopUpEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mCategories.set(currentPosPopUp.get(1), mItemPopUpEdittext.getText().toString());
                    mCurrentCategories.clear();
                    for (String s : mCategories) {
                        if (s.toLowerCase().contains(mSearchField.getText().toString().toLowerCase())) {
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
                if (!popupDoEdit) {
                    if ((mPopUpCurrentType.equals(getString(R.string.fragment_category_income)) && !editable.toString().startsWith(getString(R.string.fragment_category_income))) || (mPopUpCurrentType.equals(getString(R.string.fragment_category_expense)) && !editable.toString().startsWith(getString(R.string.fragment_category_expense))) || (mPopUpCurrentType.equals(getString(R.string.fragment_category_transfer)) && !editable.toString().startsWith(getString(R.string.fragment_category_transfer))) || (mPopUpCurrentType.equals(getString(R.string.fragment_category_exchange)) && !editable.toString().startsWith(getString(R.string.fragment_category_exchange)))) {
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
                mPopUpCurrentType = getString(R.string.fragment_category_expense);
                popupDoEdit = false;
            }
        });

        mItemPopUpIncomeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDoEdit = true;
                mItemPopUpEdittext.setText(mItemPopUpIncomeTextView.getText());
                mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().length());
                mPopUpCurrentType = getString(R.string.fragment_category_income);
                popupDoEdit = false;
            }
        });

        mItemPopUpTransferTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDoEdit = true;
                mItemPopUpEdittext.setText(mItemPopUpTransferTextView.getText());
                mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().length());
                mPopUpCurrentType = getString(R.string.fragment_category_transfer);
                popupDoEdit = false;
            }
        });

        mItemPopUpExchangeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDoEdit = true;
                mItemPopUpEdittext.setText(mItemPopUpExchangeTextView.getText());
                mItemPopUpEdittext.setSelection(mItemPopUpEdittext.getText().toString().length());
                mPopUpCurrentType = getString(R.string.fragment_category_exchange);
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

        // for keyboard hide and show

        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean keyboardStatus = mKeyboardUp;
                int heightDiff = mRootView.getRootView().getHeight() - mRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably the keyboard...
                    mKeyboardUp = true;
                } else {
                    mKeyboardUp = false;
                }
                if (keyboardStatus && !mKeyboardUp) { // keyboard just hid
                    Log.d(TAG, "Keyboard hid");
                    hideItemEditContainters();
                }
            }
        });

        return mView;
    }

    private void goAddCategories() {
        mCategories = removeBlankSubcategories(mCoreAPI.loadCategories());
        mCurrentCategories.addAll(mCategories);
    }

    private void updateAddBlanks(String term) {
        mAddExpensePopUpTextView.setText(getString(R.string.fragment_category_expense) + term);
        mAddIncomePopUpTextView.setText(getString(R.string.fragment_category_income) + term);
        mAddTransferPopUpTextView.setText(getString(R.string.fragment_category_transfer) + term);
        mAddExchangePopUpTextView.setText(getString(R.string.fragment_category_exchange) + term);
    }

    private void updateItemBlanks(String term) {
        mItemPopUpExpenseTextView.setText(getString(R.string.fragment_category_expense) + term);
        mItemPopUpIncomeTextView.setText(getString(R.string.fragment_category_income) + term);
        mItemPopUpTransferTextView.setText(getString(R.string.fragment_category_transfer) + term);
        mItemPopUpExchangeTextView.setText(getString(R.string.fragment_category_exchange) + term);
    }

    private void goAddNewCategory() {
        String newCat;
        if (!mAddField.getText().toString().substring(mAddField.getText().toString().indexOf(':') + 1).trim().isEmpty()) {
            newCat = mAddField.getText().toString();
            mCategories.add(newCat);
            if (newCat.toLowerCase().contains(mSearchField.getText().toString().toLowerCase())) {
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
    }

    private void hideItemEditContainters() {
        mItemPopUpContainer.setVisibility(View.GONE);
        mAddPopUpContainer.setVisibility(View.GONE);
    }

    public void showDoneCancel() {
        mShowDoneVisibility = View.VISIBLE;
        mHandler.postDelayed(delayedShowDoneCancel, 100);
    }

    public void hideDoneCancel() {
        mShowDoneVisibility = View.GONE;
        mHandler.postDelayed(delayedShowDoneCancel, 100);
    }

    private int mShowDoneVisibility = View.INVISIBLE;
    Runnable delayedShowDoneCancel = new Runnable() {
        @Override
        public void run() {
            mDoneCancelContainer.setVisibility(mShowDoneVisibility);
        }
    };

    private void SaveAllChanges() {
        List<String> coreCategories = mCoreAPI.loadCategories();
        // Remove any categories first
        for (String category : coreCategories) {
            if (!mCategories.contains(category)) {
                mCoreAPI.removeCategory(category);
            }
        }

        // Add any categories
        for (String category : mCategories) {
            mCoreAPI.addCategory(category);
        }
    }

    private List<String> removeBlankSubcategories(List<String> allcategories) {
        for (int i=allcategories.size()-1; i> -1; i--) {
            String category = allcategories.get(i);
            if (category.equals(getString(R.string.fragment_category_expense)) ||
                    category.equals(getString(R.string.fragment_category_income)) ||
                    category.equals(getString(R.string.fragment_category_transfer)) ||
                    category.equals(getString(R.string.fragment_category_exchange))) {
                allcategories.remove(category);
            }
        }
        return allcategories;
    }
}
