package com.airbitz.fragments;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionDetailCategoryAdapter;
import com.airbitz.adapters.TransactionDetailSearchAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.AccountTransaction;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.Categories;
import com.airbitz.models.SearchResult;
import com.airbitz.models.Wallet;
import com.airbitz.models.defaultCategoryEnum;
import com.airbitz.utils.CalculatorBrain;
import com.airbitz.utils.ListViewUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2/20/14.
 */
public class TransactionDetailFragment extends Fragment implements View.OnClickListener {

    private Button mDoneButton;
    private Button mAdvanceDetailsButton;
    private Button mXButton;

    private TextView mDateTextView;
    private TextView mTitleTextView;
    private EditText mNameEditText;
    private TextView mBitcoinValueTextview;
    private TextView mNoteTextView;

    private LinearLayout mDummyFocus;

    private int businessCount;

    private String currentType = "";
    private boolean doEdit = false;
    private boolean catSelected = false;
    private defaultCategoryEnum defaultCat = defaultCategoryEnum.Income;//TODO set this based on type of transaction

    private Bundle bundle;

    private int baseIncomePosition = 0;//TODO set these three from categories retrieved
    private int baseExpensePosition = 1;
    private int baseTransferPosition = 2;


    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private RelativeLayout mSentDetailLayout;
    private RelativeLayout mNoteDetailLayout;
    private RelativeLayout mNameDetailLayout;

    private EditText mDollarValueEdittext;
    private EditText mNoteEdittext;
    private EditText mCategoryEdittext;

    private RelativeLayout mAdvancedDetailsPopup;

    private List<BusinessSearchResult> mBusinesses;
    private List<BusinessSearchResult> mOriginalBusinesses;
    private List<String> mContactNames;
    private List<Object> mCombined;
    private Map<String, Uri> mContactPhotos;

    private List<String> mCategories;

    private boolean fromSendRequest = false;

    private ListView mSearchListView;
    private ListView mCategoryListView;
    private TransactionDetailSearchAdapter mSearchAdapter;
    private TransactionDetailCategoryAdapter mCategoryAdapter;
    private AirbitzAPI api = AirbitzAPI.getApi();

    DecimalFormat mDF = new DecimalFormat("@###########");
    private Boolean userIsInTheMiddleOfTypingANumber = false;
    private CalculatorBrain mCalculatorBrain;
    private static final String DIGITS = "0123456789.";
    private ClipboardManager clipboard;
    private float mBTCtoUSDConversion = 450.0f;

    private AccountTransaction mTransaction;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        if(bundle!=null && bundle.getString(WalletsFragment.FROM_SOURCE).equals("SEND")) {
            fromSendRequest = true;
        } else {
            if(bundle.getString(WalletsFragment.FROM_SOURCE)!=null) {
                    String walletUUID = bundle.getString(Wallet.WALLET_UUID);
                    String txId = bundle.getString(WalletsFragment.TXID);
                    if(walletUUID.isEmpty()) {
                        Log.d("TransactionDetailFragement", "no detail info");
                    } else {
                        CoreAPI mCoreAPI = CoreAPI.getApi();
                        mTransaction = mCoreAPI.getTransaction(walletUUID, txId);
                    }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        mCalculatorBrain = new CalculatorBrain();
        mDF.setMinimumFractionDigits(0);
        mDF.setMaximumFractionDigits(6);
        mDF.setMinimumIntegerDigits(1);
        mDF.setMaximumIntegerDigits(8);
        setupCalculator(((NavigationActivity) getActivity()).getCalculatorView());


        mDoneButton = (Button) view.findViewById(R.id.transaction_detail_button_done);
        mAdvanceDetailsButton = (Button) view.findViewById(R.id.transaction_detail_button_advanced);
        mXButton = (Button) view.findViewById(R.id.x_button);

        mTitleTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_title);
        mNameEditText = (EditText) view.findViewById(R.id.transaction_detail_edittext_name);
        mNoteTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_notes);
        mBitcoinValueTextview = (TextView) view.findViewById(R.id.transaction_detail_textview_bitcoin_value);
        mDateTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_date);

        mDollarValueEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_dollar_value);
        mDollarValueEdittext.setInputType(InputType.TYPE_NULL);

        mNoteEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_notes);
        mCategoryEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_category);

        mBackButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_help);

        mSentDetailLayout = (RelativeLayout) view.findViewById(R.id.layout_sent_detail);
        mNoteDetailLayout = (RelativeLayout) view.findViewById(R.id.transaction_detail_layout_note);
        mNameDetailLayout = (RelativeLayout) view.findViewById(R.id.transaction_detail_layout_name);

        mDummyFocus = (LinearLayout) view.findViewById(R.id.dummy_focus);

        mAdvancedDetailsPopup = (RelativeLayout) view.findViewById(R.id.advanced_details_popup);

        mSearchListView = (ListView) view.findViewById(R.id.listview_search);
        mBusinesses = new ArrayList<BusinessSearchResult>();
        mOriginalBusinesses = new ArrayList<BusinessSearchResult>();
        mContactNames = new ArrayList<String>();
        mCombined = new ArrayList<Object>();
        mContactPhotos = new HashMap<String, Uri>();
        mSearchAdapter = new TransactionDetailSearchAdapter(getActivity(),mBusinesses, mContactNames,mCombined,mContactPhotos);
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

        mAdvanceDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdvancedDetailsPopup.setVisibility(View.VISIBLE);
            }
        });

        mXButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdvancedDetailsPopup.setVisibility(View.GONE);
            }
        });

        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
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
                            new BusinessSearchAsyncTask().execute(latLong);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }else{
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
                    mNameDetailLayout.setVisibility(View.GONE);
                    mAdvanceDetailsButton.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mDoneButton.setVisibility(View.GONE);
                    mCategoryListView.setVisibility(View.VISIBLE);
                    if(!mCategoryEdittext.getText().toString().isEmpty()) {
                        mCategoryEdittext.setSelection(currentType.length(), mCategoryEdittext.getText().toString().length());
                    }
                }else{
                    mDateTextView.setVisibility(View.VISIBLE);
                    mNameDetailLayout.setVisibility(View.VISIBLE);
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                    mSentDetailLayout.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.VISIBLE);
                    mCategoryListView.setVisibility(View.GONE);
                    mNoteEdittext.requestFocus();
                }
            }
        });

        mNoteEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    mAdvanceDetailsButton.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mDoneButton.setVisibility(View.GONE);
                }else{
                    System.out.println("Note loses focus");
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE); 
                    mSentDetailLayout.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    mDummyFocus.requestFocus();
                }
            }
        });
        mNoteEdittext.setHorizontallyScrolling(false);
        mNoteEdittext.setMaxLines(Integer.MAX_VALUE);

        mNameEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    //mNameEditText.clearFocus();
                    mCategoryEdittext.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mCategoryEdittext.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    //mCategoryEdittext.clearFocus();
                    mNoteEdittext.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mNoteEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    System.out.println("Action Done");
                    mDummyFocus.requestFocus();
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
                if(false == doEdit) {
                    if(false == catSelected){
                        String temp = editable.toString();
                        doEdit = true;
                        editable.clear();
                        editable.append(defaultCat.toString()+":"+temp);
                        doEdit = false;
                        catSelected = true;
                    }
                    if (currentType.charAt(0) == 'I') {
                        if (editable.toString().length() < 7 || editable.toString().substring(0, 7).compareTo("Income:")!=0) {
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
                        }else if(editable.toString().length() >= 7){
                            mCategories.remove(baseIncomePosition);
                            mCategories.add(baseIncomePosition, "Income:" + editable.toString().substring(7));
                            mCategories.remove(baseExpensePosition);
                            mCategories.add(baseExpensePosition, "Expense:" + editable.toString().substring(7));
                            mCategories.remove(baseTransferPosition);
                            mCategories.add(baseTransferPosition, "Transfer:" + editable.toString().substring(7));
                            mCategoryAdapter.notifyDataSetChanged();
                        }
                        if (mCategoryEdittext.getSelectionStart() < 7) {
                            mCategoryEdittext.setSelection(7, 7);
                        }
                    } else if (currentType.charAt(0) == 'E') {
                        if ( editable.toString().length() < 8 || editable.toString().substring(0, 8).compareTo("Expense:")!=0) {
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
                        }else if(editable.toString().length() >= 8){
                            mCategories.remove(baseIncomePosition);
                            mCategories.add(baseIncomePosition, "Income:" + editable.toString().substring(8));
                            mCategories.remove(baseExpensePosition);
                            mCategories.add(baseExpensePosition, "Expense:" + editable.toString().substring(8));
                            mCategories.remove(baseTransferPosition);
                            mCategories.add(baseTransferPosition, "Transfer:" + editable.toString().substring(8));
                            mCategoryAdapter.notifyDataSetChanged();
                        }
                        if (mCategoryEdittext.getSelectionStart() < 8) {
                            mCategoryEdittext.setSelection(8, 8);
                        }
                    } else if (currentType.charAt(0) == 'T') {
                        if (editable.toString().length() < 9 || editable.toString().substring(0, 9).compareTo("Transfer:")!=0) {
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
                        }else if(editable.toString().length() >= 9){
                            mCategories.remove(baseIncomePosition);
                            mCategories.add(baseIncomePosition,"Income:"+editable.toString().substring(9));
                            mCategories.remove(baseExpensePosition);
                            mCategories.add(baseExpensePosition, "Expense:" + editable.toString().substring(9));
                            mCategories.remove(baseTransferPosition);
                            mCategories.add(baseTransferPosition, "Transfer:" + editable.toString().substring(9));
                            mCategoryAdapter.notifyDataSetChanged();
                        }
                        if (mCategoryEdittext.getSelectionStart() < 9) {
                            mCategoryEdittext.setSelection(9, 9);
                        }
                    } else {
                        System.err.println("currentType was something other than Income, Expense or Transfer: "+currentType);
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
                catSelected = true;
                if(mCategories.get(i).charAt(0) == 'I'){
                    currentType = "Income:";
                }else if(mCategories.get(i).charAt(0) == 'E'){
                    currentType = "Expense:";
                }else if(mCategories.get(i).charAt(0) == 'T'){
                    currentType = "Transfer:";
                }
                //TODO move the strings around depending on negative/positive value
                doEdit = true;
                mCategoryEdittext.setText(mCategoryAdapter.getItem(i));
                doEdit = false;
                if(i==baseIncomePosition){
                    mCategoryEdittext.setSelection(mCategoryEdittext.getText().length());
                }else if(i==baseExpensePosition){
                    mCategoryEdittext.setSelection(mCategoryEdittext.getText().length());
                }else if(i==baseTransferPosition){
                    mCategoryEdittext.setSelection(mCategoryEdittext.getText().length());
                }else {
                    mNoteEdittext.requestFocus();
                }
            }
        });

        mDollarValueEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showCustomKeyboard(view);
                } else {
                    hideCustomKeyboard();
                }
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
                mCategories.add(0,mCategoryEdittext.getText().toString());
                getActivity().onBackPressed();
            }
        });

        currentType = defaultCat.toString()+":";

        if(fromSendRequest){
            mNameEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        return view;
    }

    @Override
    public void onClick(View v) {

        View focusCurrent = getActivity().getWindow().getCurrentFocus();
        if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;
        EditText display = (EditText) focusCurrent;
        Editable editable = display.getText();
        int start = display.getSelectionStart();
        // delete the selection, if chars are selected:
        int end = display.getSelectionEnd();
        if (end > start) {
            editable.delete(start, end);
        }
        String buttonTag = v.getTag().toString();

        if(buttonTag.equals("done")) {
            hideCustomKeyboard();
            mDummyFocus.requestFocus();
        } else if(buttonTag.equals("back")) {
            String s = display.getText().toString();
            if(s.length() == 1) { // 1 character, just set to 0
                mCalculatorBrain.performOperation(CalculatorBrain.CLEAR);
                display.setText("0");
            } else if (s.length() > 1) {
                display.setText(s.substring(0, s.length()-1));
            }

        } else if (DIGITS.contains(buttonTag)) {

            // digit was pressed
            if (userIsInTheMiddleOfTypingANumber) {
                if (buttonTag.equals(".") && display.getText().toString().contains(".")) {
                    // ERROR PREVENTION
                    // Eliminate entering multiple decimals
                } else {
                    display.append(buttonTag);
                }
            } else {
                if (buttonTag.equals(".")) {
                    // ERROR PREVENTION
                    // This will avoid error if only the decimal is hit before an operator, by placing a leading zero
                    // before the decimal
                    display.setText(0 + buttonTag);
                } else {
                    display.setText(buttonTag);
                }
                userIsInTheMiddleOfTypingANumber = true;
            }

        } else {
            // operation was pressed
            if (userIsInTheMiddleOfTypingANumber) {

                mCalculatorBrain.setOperand(Double.parseDouble(display.getText().toString()));
                userIsInTheMiddleOfTypingANumber = false;
            }

            mCalculatorBrain.performOperation(buttonTag);
            display.setText(mDF.format(mCalculatorBrain.getResult()));
        }

    }

    class BusinessSearchAsyncTask extends AsyncTask<String, Integer, String>{
        private AirbitzAPI api = AirbitzAPI.getApi();

        public BusinessSearchAsyncTask() {
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
        String columns[] ={ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                columns, ContactsContract.Contacts.DISPLAY_NAME+" LIKE "+DatabaseUtils.sqlEscapeString("%"+searchTerm+"%"), null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        if (cur.getCount() > 0) {
            mContactNames.clear();
            mContactPhotos.clear();
            while (cur.moveToNext()) {
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                mContactNames.add(name);
                String photoURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                Uri thumbUri = Uri.parse(photoURI);
                mContactPhotos.put(name, thumbUri);
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
                    if(mBusinesses.get(j).getName().toLowerCase().compareTo(mOriginalBusinesses.get(i).getName().toLowerCase())>0){
                        mBusinesses.add(j, mOriginalBusinesses.get(i));
                        flag = true;
                    }
                    j++;
                }
                if(j == mBusinesses.size() && !flag){
                    mBusinesses.add(mOriginalBusinesses.get(i));
                }
            }
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

    public void hideCustomKeyboard() {
        ((NavigationActivity) getActivity()).hideCalculator();
    }

    public void showCustomKeyboard(View v) {
        ((NavigationActivity) getActivity()).showCalculator();
    }

    private void setupCalculator(View l) {
        l.findViewById(R.id.button_calc_0).setOnClickListener(this);
        l.findViewById(R.id.button_calc_1).setOnClickListener(this);
        l.findViewById(R.id.button_calc_2).setOnClickListener(this);
        l.findViewById(R.id.button_calc_3).setOnClickListener(this);
        l.findViewById(R.id.button_calc_4).setOnClickListener(this);
        l.findViewById(R.id.button_calc_5).setOnClickListener(this);
        l.findViewById(R.id.button_calc_6).setOnClickListener(this);
        l.findViewById(R.id.button_calc_7).setOnClickListener(this);
        l.findViewById(R.id.button_calc_8).setOnClickListener(this);
        l.findViewById(R.id.button_calc_9).setOnClickListener(this);

        l.findViewById(R.id.button_calc_plus).setOnClickListener(this);
        l.findViewById(R.id.button_calc_minus).setOnClickListener(this);
        l.findViewById(R.id.button_calc_multiply).setOnClickListener(this);
        l.findViewById(R.id.button_calc_division).setOnClickListener(this);
        l.findViewById(R.id.button_calc_percent).setOnClickListener(this);
        l.findViewById(R.id.button_calc_equal).setOnClickListener(this);
        l.findViewById(R.id.button_calc_c).setOnClickListener(this);
        l.findViewById(R.id.button_calc_dot).setOnClickListener(this);
        l.findViewById(R.id.button_calc_done).setOnClickListener(this);
        l.findViewById(R.id.button_calc_back).setOnClickListener(this);
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
