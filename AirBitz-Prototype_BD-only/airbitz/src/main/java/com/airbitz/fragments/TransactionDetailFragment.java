package com.airbitz.fragments;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.MoreCategoryAdapter;
import com.airbitz.adapters.NoteCategoryAdapter;
import com.airbitz.adapters.TransactionDetailCategoryAdapter;
import com.airbitz.adapters.TransactionDetailSearchAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.Categories;
import com.airbitz.models.SearchResult;
import com.airbitz.utils.CacheUtil;
import com.airbitz.utils.ListViewUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created on 2/20/14.
 */
public class TransactionDetailFragment extends Fragment{

    private Button mDoneButton;
    private Button mAdvanceDetailsButton;

    private TextView mDateTextView;
    private TextView mTitleTextView;
    private EditText mNameEditText;
    private TextView mBitcoinValueTextview;
    private TextView mNoteTextView;

    private int businessCount;

    private String currentType = "";
    private boolean doEdit = false;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private RelativeLayout mSentDetailLayout;
    private RelativeLayout mNoteDetailLayout;

    private EditText mDollarValueEdittext;
    private EditText mNoteEdittext;
    private EditText mCategoryEdittext;

    private List<BusinessSearchResult> mBusinesses;
    private List<BusinessSearchResult> mOriginalBusinesses;
    private List<String> mContactNames;
    private List<Object> mCombined;

    private List<String> mCategories;

    private ListView mSearchListView;
    private ListView mCategoryListView;
    private TransactionDetailSearchAdapter mSearchAdapter;
    private TransactionDetailCategoryAdapter mCategoryAdapter;
    private AirbitzAPI api = AirbitzAPI.getApi();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mDoneButton = (Button) view.findViewById(R.id.transaction_detail_button_done);
        mAdvanceDetailsButton = (Button) view.findViewById(R.id.transaction_detail_button_advanced);

        mTitleTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_title);
        mNameEditText = (EditText) view.findViewById(R.id.transaction_detail_edittext_name);
        mNoteTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_notes);
        mBitcoinValueTextview = (TextView) view.findViewById(R.id.transaction_detail_textview_bitcoin_value);
        mDateTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_date);

        mDollarValueEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_dollar_value);
        mNoteEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_notes);
        mCategoryEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_category);

        mBackButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_help);

        mSentDetailLayout = (RelativeLayout) view.findViewById(R.id.layout_sent_detail);
        mNoteDetailLayout = (RelativeLayout) view.findViewById(R.id.transaction_detail_layout_note);

        mSearchListView = (ListView) view.findViewById(R.id.listview_search);
        mBusinesses = new ArrayList<BusinessSearchResult>();
        mOriginalBusinesses = new ArrayList<BusinessSearchResult>();
        mContactNames = new ArrayList<String>();
        mCombined = new ArrayList<Object>();
        mSearchAdapter = new TransactionDetailSearchAdapter(getActivity(),mBusinesses, mContactNames,mCombined);
        mSearchListView.setAdapter(mSearchAdapter);

        mCategoryListView = (ListView) view.findViewById(R.id.listview_category);
        mCategories = new ArrayList<String>();//TODO put different string order depending upon type of transaction
        for(String cat :getActivity().getResources().getStringArray(R.array.transaction_categories_list)){
            mCategories.add(cat);
        }
        mCategoryAdapter = new TransactionDetailCategoryAdapter(getActivity(),mCategories);
        mCategoryListView.setAdapter(mCategoryAdapter);

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#eef2f7"),Color.parseColor("#a9bfd6")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mDateTextView.getPaint().setShader(textShader);

        //mCategoryEdittext.setKeyListener(null);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mDateTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.BOLD);
        mNameEditText.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mCategoryEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);

        mDollarValueEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitcoinValueTextview.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.BOLD);

        mNoteTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mNoteEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mDoneButton.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);

        getContactsList();
        System.out.println(mContactNames);

        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    mDateTextView.setVisibility(View.GONE);
                    mAdvanceDetailsButton.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mNoteDetailLayout.setVisibility(View.GONE);
                    mSearchListView.setVisibility(View.VISIBLE);

                    if(mNameEditText.getText().toString().isEmpty()) {
                        try {
                            Activity activity = getActivity();
                            SharedPreferences pref = activity.getSharedPreferences("PREF_NAME", Activity.MODE_PRIVATE);
                            String latLong = String.valueOf(pref.getFloat("LAT_KEY", -1));
                            latLong += "," + String.valueOf(pref.getFloat("LON_KEY", -1));
                            mCombined.clear();
                            mOriginalBusinesses.clear();
                            mBusinesses.clear();
                            new BusinessSearchAysncTask().execute(latLong);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }else{
                    mDateTextView.setVisibility(View.VISIBLE);
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                    mSentDetailLayout.setVisibility(View.VISIBLE);
                    mNoteDetailLayout.setVisibility(View.VISIBLE);
                    mSearchListView.setVisibility(View.GONE);
                    mCategoryEdittext.requestFocus();
                }
            }
        });

        mCategoryEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    mDateTextView.setVisibility(View.GONE);
                    mNameEditText.setVisibility(View.GONE);
                    mAdvanceDetailsButton.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mDoneButton.setVisibility(View.GONE);
                    mCategoryListView.setVisibility(View.VISIBLE);
                }else{
                    mDateTextView.setVisibility(View.VISIBLE);
                    mNameEditText.setVisibility(View.VISIBLE);
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                    mSentDetailLayout.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.VISIBLE);
                    mCategoryListView.setVisibility(View.GONE);
                    mNoteEdittext.requestFocus();
                }
            }
        });

        mNameEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    mNameEditText.clearFocus();
                    mCategoryEdittext.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    return true;
                }
                return false;
            }
        });

        mCategoryEdittext.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    mCategoryEdittext.clearFocus();
                    mNoteEdittext.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    return true;
                }
                return false;
            }
        });

        mNameEditText.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mCombined.clear();
                if(editable.toString().isEmpty()){
                    mCombined.addAll(mOriginalBusinesses);
                }else{
                    getMatchedContactsList(editable.toString());
                    getMatchedBusinessList(editable.toString());
                    combineMatchLists();
                }
                mSearchAdapter.notifyDataSetChanged();
                ListViewUtility.setTransactionDetailListViewHeightBasedOnChildren(mSearchListView,mCombined.size(),getActivity());
            }
        });

        mCategoryEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(doEdit == false) {
                    System.out.println("Gonna Change Some Text");
                    if (currentType.charAt(0) == 'I') {
                        if (editable.toString().length() < 7 || editable.toString().substring(0, 7) != "Income:") {
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
                        }
                        if (mCategoryEdittext.getSelectionStart() < 7) {
                            mCategoryEdittext.setSelection(7, 7);
                        }
                    } else if (currentType.charAt(0) == 'E') {
                        if ( editable.toString().length() < 8 || editable.toString().substring(0, 8) != "Expense:") {
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
                        }
                        if (mCategoryEdittext.getSelectionStart() < 8) {
                            mCategoryEdittext.setSelection(8, 8);
                        }
                    } else if (currentType.charAt(0) == 'T') {
                        if (editable.toString().length() < 9 || editable.toString().substring(0, 9) != "Transfer:") {
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
                        }
                        if (mCategoryEdittext.getSelectionStart() < 9) {
                            mCategoryEdittext.setSelection(9, 9);
                        }
                    } else {
                        System.out.println("OODODODODOOODDODODODODODODOD I FAILED");
                    }
                }
            }
        });

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mSearchAdapter.getItem(i) instanceof BusinessSearchResult){
                    mNameEditText.setText(((BusinessSearchResult)mSearchAdapter.getItem(i)).getName());
                }else{
                    mNameEditText.setText((String)mSearchAdapter.getItem(i));
                }
                mDateTextView.setVisibility(View.VISIBLE);
                mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                mSentDetailLayout.setVisibility(View.VISIBLE);
                mNoteDetailLayout.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.GONE);
                mCategoryEdittext.requestFocus();
            }
        });

        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mCategoryAdapter.getItem(i).charAt(0) == 'I'){
                    currentType = "Income:";
                }else if(mCategoryAdapter.getItem(i).charAt(0) == 'E'){
                    currentType = "Expense:";
                }else if(mCategoryAdapter.getItem(i).charAt(0) == 'T'){
                    currentType = "Transfer:";
                }
                doEdit = true;
                mCategoryEdittext.setText(mCategoryAdapter.getItem(i));
                doEdit = false;
                mCategoryEdittext.setSelection(mCategoryEdittext.getText().length());
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();

            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Common.showHelpInfo(TransactionDetailFragment.this, "Info", "Business directory info");
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    class BusinessSearchAysncTask extends AsyncTask<String, Integer, String>{
        private AirbitzAPI api = AirbitzAPI.getApi();

        public BusinessSearchAysncTask() {
        }

        @Override protected String doInBackground(String... strings) {
            String jsonParsingResult = api.getSearchByRadius("16093", "", strings[0], "", "1");
            return jsonParsingResult;
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                mBusinesses.clear();
                mCombined.clear();
                mOriginalBusinesses.clear();
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                businessCount = results.getCountValue();
                mBusinesses.addAll(results.getBusinessSearchObjectArray());
                mOriginalBusinesses.addAll(mBusinesses);
                mCombined.addAll(mBusinesses);
            }catch (JSONException e) {
                e.printStackTrace();
                this.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }
            mSearchAdapter.notifyDataSetChanged();
            System.out.println("list size: "+mCombined.size());
            ListViewUtility.setTransactionDetailListViewHeightBasedOnChildren(mSearchListView,mCombined.size(),getActivity());
            mSearchListView.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onResume() {
        super.onResume();
//        new BusinessCategoryAsyncTask().execute("name");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void getContactsList(){
        ContentResolver cr = getActivity().getContentResolver();
        String columns[] ={ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                columns, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                mContactNames.add(name);
            }
        }
        cur.close();
    }

    public void getMatchedContactsList(String searchTerm){
        ContentResolver cr = getActivity().getContentResolver();
        String columns[] ={ContactsContract.Contacts.DISPLAY_NAME};
        System.out.println("Query: "+ContactsContract.Contacts.DISPLAY_NAME+" LIKE "+DatabaseUtils.sqlEscapeString("%"+searchTerm+"%"));
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                columns, ContactsContract.Contacts.DISPLAY_NAME+" LIKE "+DatabaseUtils.sqlEscapeString("%"+searchTerm+"%"), null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        if (cur.getCount() > 0) {
            mContactNames.clear();
            while (cur.moveToNext()) {
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                mContactNames.add(name);
            }
        }
        cur.close();
    }

    public void getMatchedBusinessList(String searchTerm){
        mBusinesses.clear();
        for(int i=0; i < mOriginalBusinesses.size();i++) {
            if(mOriginalBusinesses.get(i).getName().toLowerCase().contains(searchTerm.toLowerCase())){
                int j = 0;
                boolean flag = false;
                while(!flag && j !=mBusinesses.size()){
                    System.out.println("Before: mBusinesses: "+mBusinesses.get(j).getName()+" mOriginalBusinesses: "+mOriginalBusinesses.get(i).getName());
                    if(mBusinesses.get(j).getName().toLowerCase().compareTo(mOriginalBusinesses.get(i).getName().toLowerCase())>0){
                        mBusinesses.add(j,mOriginalBusinesses.get(i));
                        System.out.println("After: mBusinesses: "+mBusinesses.get(j).getName()+" mOriginalBusinesses: "+mOriginalBusinesses.get(i).getName());
                        flag = true;
                    }
                    j++;
                }
                if(j == mBusinesses.size() && !flag){
                    mBusinesses.add(mOriginalBusinesses.get(i));
                }
            }
        }
        for(BusinessSearchResult bSR:mBusinesses){
            System.out.println(bSR.getName());
        }
    }

    public void combineMatchLists(){
        while(!mBusinesses.isEmpty() | !mContactNames.isEmpty()){
            if(mBusinesses.isEmpty()){
                mCombined.add(mContactNames.get(0));
                mContactNames.remove(0);
            }else if(mContactNames.isEmpty()){
                mCombined.add(mBusinesses.get(0));
                mBusinesses.remove(0);
            }else if(mBusinesses.get(0).getName().toLowerCase().compareTo(mContactNames.get(0).toLowerCase())<0){
                mCombined.add(mBusinesses.get(0));
                mBusinesses.remove(0);
            }else{
                mCombined.add(mContactNames.get(0));
                mContactNames.remove(0);
            }
        }
    }


    public Categories getMoreBusinessCategory(Categories initial, String link){
        while(!link.equalsIgnoreCase("null")){

            String jSOnString = api.getRequest(link);
            Categories jsonParsingResult = null;
            try{
                jsonParsingResult = new Categories(new JSONObject(jSOnString));
                link = jsonParsingResult.getNextLink();
                initial.addCategories(jsonParsingResult);
            } catch(Exception e) {
                link = "null";
            }

        }

        return initial;
    }
}
