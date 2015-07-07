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

package com.airbitz.fragments.directory;

import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.MoreCategoryAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.models.Category;
import com.airbitz.models.Categories;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryFragment extends BaseFragment
    implements NavigationActivity.OnBackPress {

    String TAG = getClass().getSimpleName();

    private View mLoading;
    private MoreCategoryAdapter mMoreCategoryAdapter;
    private BusinessCategoryAsyncTask mBusinessCategoryAsynctask;
    private Categories mCategories;
    private TextView mTitleView;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);

        if (mMoreCategoryAdapter == null) {
            try {
                Categories categories = new Categories(null);
                categories.setBusinessCategoryArray(new ArrayList<Category>());
                mCategories = categories;
                mMoreCategoryAdapter = new MoreCategoryAdapter(getActivity(), mCategories);
            } catch (JSONException e) {
                // Should never happen
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_categories, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        getBaseActivity().setSupportActionBar(toolbar);

        mTitleView = (TextView) view.findViewById(R.id.title);
        mTitleView.setText(R.string.more_categories);

        mLoading = view.findViewById(R.id.empty);
        mListView = (ListView) view.findViewById(R.id.category_list);
        if (mMoreCategoryAdapter != null) {
            mListView.setAdapter(mMoreCategoryAdapter);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Category cat = mCategories.getBusinessCategoryArray().get(i);
                MapBusinessDirectoryFragment.pushFragment(mActivity, cat.getCategoryName(), "", "category");
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            return onBackPress();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackPress() {
        CategoryFragment.popFragment(mActivity);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCategories == null || mCategories.getCountValue() == 0) {
            try {
                mBusinessCategoryAsynctask = new BusinessCategoryAsyncTask();
                mBusinessCategoryAsynctask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "level");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setLoading(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBusinessCategoryAsynctask != null) {
            mBusinessCategoryAsynctask.cancel(true);
        }
    }

    private void setLoading(boolean loading) {
        mListView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        mLoading.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    class BusinessCategoryAsyncTask extends AsyncTask<String, Integer, Categories> {
        private AirbitzAPI api = AirbitzAPI.getApi();

        @Override
        protected void onPreExecute() {
            setLoading(true);
        }

        @Override
        protected Categories doInBackground(String... strings) {
            Categories jsonParsingResult = null;
            try {
                jsonParsingResult = api.getHttpCategories(strings[0]);
                String nextUrl = jsonParsingResult.getNextLink();
                mCategories = jsonParsingResult;
                getMoreBusinessCategory(mCategories, nextUrl);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
            return jsonParsingResult;
        }

        @Override
        protected void onPostExecute(Categories categories) {
            if (getActivity() == null) {
                return;
            }
            updateMoreSpinner(categories);
            setLoading(false);
        }
    }

    private Categories getMoreBusinessCategory(Categories initial, String link) {
        while (!link.equalsIgnoreCase("null")) {
            String jSOnString = AirbitzAPI.getApi().getRequest(link);
            Categories jsonParsingResult = null;
            try {
                jsonParsingResult = new Categories(new JSONObject(jSOnString));
                link = jsonParsingResult.getNextLink();
                initial.addCategories(jsonParsingResult);
            } catch (Exception e) {
                link = "null";
            }
        }
        return initial;
    }

    private void updateMoreSpinner(Categories categories) {
        if (categories != null) {
            ArrayList<Category> catArrayList = new ArrayList<Category>();

            for (Category cat : categories.getBusinessCategoryArray()) {
                if (!cat.getCategoryLevel().equalsIgnoreCase("1")
                        && !cat.getCategoryLevel().equalsIgnoreCase("2")
                        && !cat.getCategoryLevel().equalsIgnoreCase("3")
                        && !cat.getCategoryLevel().equalsIgnoreCase("null")) {
                    catArrayList.add(cat);
                }
            }
            categories.removeBusinessCategoryArray();
            categories.setBusinessCategoryArray(catArrayList);
            mCategories = categories;

            mMoreCategoryAdapter = new MoreCategoryAdapter(getActivity(), mCategories);
            mListView.setAdapter(mMoreCategoryAdapter);
        }
    }

    public static void pushFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        CategoryFragment fragment = new CategoryFragment();
        mActivity.pushFragment(fragment, transaction);
    }

    public static void popFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        mActivity.popFragment(transaction);
        mActivity.getFragmentManager().executePendingTransactions();
    }
}
