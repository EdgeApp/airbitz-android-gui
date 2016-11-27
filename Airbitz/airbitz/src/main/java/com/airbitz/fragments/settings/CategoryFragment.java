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

package com.airbitz.fragments.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import co.airbitz.core.Account;
import co.airbitz.core.Categories;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.SettingsCategoryAdapter;
import com.airbitz.api.Constants;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.CategoryWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class CategoryFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    private Toolbar mToolbar;

    private Button mCancelButton;
    private Button mDoneButton;

    private boolean mChanged = false;

    private ListView mCategoryListView;
    private SettingsCategoryAdapter mCategoryAdapter;
    private List<String> mCategories;
    private List<String> mCurrentCategories;
    private SaveTask mSaveTask;
    private Account mAccount;
    private Categories mAccountCategories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccount = AirbitzApplication.getAccount();
        mAccountCategories = mAccount.categories();
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        View mView = i.inflate(R.layout.fragment_category, container, false);

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.settings_category_title);

        mCancelButton = (Button) mView.findViewById(R.id.button_cancel);
        mDoneButton = (Button) mView.findViewById(R.id.button_done);

        mCategoryListView = (ListView) mView.findViewById(R.id.category_list_view);
        mCategories = new ArrayList<String>();
        mCurrentCategories = new ArrayList<String>();

        mCategories = removeBlankSubcategories(mAccountCategories.list());
        Collections.sort(mCategories);
        mCurrentCategories.addAll(mCategories);

        mCategoryAdapter = new SettingsCategoryAdapter(
            getActivity(), mCurrentCategories, mCategories, mChangeListener);
        mCategoryListView.setAdapter(mCategoryAdapter);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSaveTask == null) {
                    mSaveTask = new SaveTask();
                    mSaveTask.execute();
                }
            }
        });

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_category_edit, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String text) {
                mCurrentCategories.clear();
                for (String s : mCategories) {
                    if (s.toLowerCase().contains(text.toLowerCase())) {
                        mCurrentCategories.add(s);
                    }
                }
                mCategoryAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void exit() {
        if (mChanged) {
            areYouSure();
        } else {
            mActivity.popFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                return true;
            case R.id.action_add:
                // custom dialog please
                showAddDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
            mSaveTask = null;
        }
    }

    private void showAddDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_category_add, null);

        final CategoryWidget category = (CategoryWidget) view.findViewById(R.id.category_widget);

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
        builder.setTitle("Add Category")
            .setCancelable(true)
            .setPositiveButton(R.string.settings_categories_add_new, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                addNewCategory(category.getValue());
            }
        });
        builder.setView(view);
        final Dialog alert = builder.create();

        category.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    addNewCategory(category.getValue());
                    alert.dismiss();
                    return true;
                }
                return false;
            }
        });

        alert.show();
    }

    private void areYouSure() {
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.fragment_category_cancel_message))
                .setTitle(getString(R.string.fragment_category_cancel_title))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.string_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mActivity.popFragment();
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
        Dialog alert = builder.create();
        alert.show();
    }

    private void addNewCategory(String category) {
        mCategories.add(category);
        mCurrentCategories.add(category);
        Collections.sort(mCategories);
        Collections.sort(mCurrentCategories);
        mCategoryAdapter.notifyDataSetChanged();

        mChanged = true;
    }

    public class SaveTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            saveAllChanges();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            mSaveTask = null;

            mActivity.showModalProgress(false);
            mActivity.popFragment();
        }

        @Override
        protected void onCancelled() {
            mSaveTask = null;
            mActivity.showModalProgress(false);
        }
    }

    private void saveAllChanges() {
        List<String> coreCategories = mAccountCategories.list();
        // Remove any categories first
        for (String category : coreCategories) {
            if (!mCategories.contains(category)) {
                mAccountCategories.remove(category);
            }
        }

        // Add any categories
        for (String category : mCategories) {
            mAccountCategories.insert(category);
        }
    }

    private List<String> removeBlankSubcategories(List<String> allcategories) {
        Set<String> cats = new HashSet<String>();
        for (int i = allcategories.size()-1; i > -1; i--) {
            String category = allcategories.get(i);
            String tmp = category.replace(":", "");
            if (tmp.equals(Constants.EXPENSE) ||
                    tmp.equals(Constants.INCOME) ||
                    tmp.equals(Constants.TRANSFER) ||
                    tmp.equals(Constants.EXCHANGE)) {
                allcategories.remove(category);
            } else if (cats.contains(category)) {
                allcategories.remove(category);
            } else {
                cats.add(category);
            }
        }
        return allcategories;
    }

    private CategoryWidget.OnChangeListener mChangeListener = new CategoryWidget.OnChangeListener() {
        public void valueChange(CategoryWidget view) {
            mChanged = true;
        }
    };
}
