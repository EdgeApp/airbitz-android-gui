package com.airbitz.fragments;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionDetailCategoryAdapter;
import com.airbitz.adapters.TransactionDetailSearchAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.Categories;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.SearchResult;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.models.defaultCategoryEnum;
import com.airbitz.utils.CalculatorBrain;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created on 2/20/14.
 */
public class TransactionDetailFragment extends Fragment implements View.OnClickListener {

    private Button mDoneButton;
    private Button mAdvanceDetailsButton;
    private TextView mAdvancedDetailTextView;
    private Button mXButton;

    private TextView mDateTextView;
    private TextView mTitleTextView;
    private TextView mToFromName;
    private EditText mPayeeEditText;
    private TextView mBitcoinValueTextview;
    private TextView mFeeTextview;

    private View mDummyFocus;

    private View popupTriangle;

    private CurrentLocationManager mLocationManager;
    private boolean locationEnabled;

    private String mCategoryOld = "";

    private int businessCount;

    private String currentType = "";
    private boolean doEdit = false;
    private boolean catSelected = false;
    private defaultCategoryEnum defaultCat = defaultCategoryEnum.Income;//TODO set this based on type of transaction

    private Bundle bundle;

    private int baseIncomePosition = 0;//TODO set these three from categories retrieved
    private int baseExpensePosition = 1;
    private int baseTransferPosition = 2;
    private int baseExchangePosition = 3;
    private int originalBaseIncomePosition = 0;//TODO set these three from categories retrieved
    private int originalBaseExpensePosition = 1;
    private int originalBaseTransferPosition = 2;
    private int originalBaseExchangePosition = 3;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private RelativeLayout mSentDetailLayout;
    private RelativeLayout mNoteDetailLayout;
    private RelativeLayout mNameDetailLayout;

    private EditText mFiatValueEdittext;
    private TextView mFiatDenominationLabel, mFiatDenominationAcronym;
    private EditText mNoteEdittext;
    private EditText mCategoryEdittext;

    private RelativeLayout mAdvancedDetailsPopup;

    private List<BusinessSearchResult> mBusinesses;
    private List<BusinessSearchResult> mOriginalBusinesses;
    private List<String> mContactNames;
    private List<Object> mCombined;
    private LinkedHashMap<String, Uri> mContactPhotos;

    private List<String> mCategories;
    private List<String> mOriginalCategories;

    private boolean mFromSend = false;
    private boolean mFromRequest = false;

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

    private CoreAPI mCoreAPI;
    private Wallet mWallet;
    private Transaction mTransaction;
    private int mCurrencyIndex;

    private BusinessSearchAsyncTask mBusinessSearchAsyncTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        if(bundle!=null) {
            if(bundle.getString(WalletsFragment.FROM_SOURCE)!=null && bundle.getString(WalletsFragment.FROM_SOURCE).equals("SEND")){
                System.out.println("SEND");
                mFromSend = true;
            } else if(bundle.getString(WalletsFragment.FROM_SOURCE)!=null && bundle.getString(WalletsFragment.FROM_SOURCE).equals("REQUEST")) {
                mFromRequest = true;
                System.out.println("REQUEST");
            }

            String walletUUID = bundle.getString(Wallet.WALLET_UUID);
            String txId = bundle.getString(Transaction.TXID);
            if (walletUUID.isEmpty()) {
                Log.d("TransactionDetailFragement", "no detail info");
            } else {
                mCoreAPI = CoreAPI.getApi();
                mWallet = mCoreAPI.getWallet(walletUUID);
                mTransaction = mCoreAPI.getTransaction(walletUUID, txId);
                mCurrencyIndex = mCoreAPI.SettingsCurrencyIndex();

                if(mTransaction.getCategory().isEmpty()) {
                    currentType = defaultCat.toString()+":";
                }else if(mTransaction.getCategory().startsWith("Income:")){
                    currentType = "Income:";
                    catSelected = true;
                }else if(mTransaction.getCategory().startsWith("Expense:")){
                    currentType = "Expense:";
                    catSelected = true;
                }else if(mTransaction.getCategory().startsWith("Transfer:")){
                    currentType = "Transfer:";
                    catSelected = true;
                }else if(mTransaction.getCategory().startsWith("Exchange:")){
                    currentType = "Exchange:";
                    catSelected = true;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        ((NavigationActivity)getActivity()).showNavBar();
        mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), "Enable location services for better results", Toast.LENGTH_SHORT).show();
        }else{
            locationEnabled = true;
        }

        mCalculatorBrain = new CalculatorBrain();
        mDF.setMinimumFractionDigits(0);
        mDF.setMaximumFractionDigits(6);
        mDF.setMinimumIntegerDigits(1);
        mDF.setMaximumIntegerDigits(8);
        setupCalculator(((NavigationActivity) getActivity()).getCalculatorView());

        popupTriangle = view.findViewById(R.id.fragment_transactiondetail_listview_triangle);

        mDoneButton = (Button) view.findViewById(R.id.transaction_detail_button_done);
        mAdvanceDetailsButton = (Button) view.findViewById(R.id.transaction_detail_button_advanced);
        mXButton = (Button) view.findViewById(R.id.x_button);

        mTitleTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_title);
        mPayeeEditText = (EditText) view.findViewById(R.id.transaction_detail_edittext_name);
        mToFromName = (TextView) view.findViewById(R.id.transaction_detail_textview_to_wallet);
        mBitcoinValueTextview = (TextView) view.findViewById(R.id.transaction_detail_textview_bitcoin_value);
        mFeeTextview = (TextView) view.findViewById(R.id.transaction_detail_textview_fee_value);
        mDateTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_date);

        mFiatValueEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_dollar_value);
        mFiatValueEdittext.setInputType(InputType.TYPE_NULL);
        mFiatDenominationLabel = (TextView) view.findViewById(R.id.transaction_detail_textview_currency_sign);
        mFiatDenominationAcronym = (TextView) view.findViewById(R.id.transaction_detail_textview_currency_text);

        mNoteEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_notes);
        mCategoryEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_category);

        mBackButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_help);

        mSentDetailLayout = (RelativeLayout) view.findViewById(R.id.layout_sent_detail);
        mNoteDetailLayout = (RelativeLayout) view.findViewById(R.id.transaction_detail_layout_note);
        mNameDetailLayout = (RelativeLayout) view.findViewById(R.id.transaction_detail_layout_name);

        mDummyFocus = view.findViewById(R.id.fragment_transactiondetail_dummy_focus);

        mAdvancedDetailsPopup = (RelativeLayout) view.findViewById(R.id.advanced_details_popup);
        mAdvancedDetailTextView = (TextView) view.findViewById(R.id.fragment_transactiondetail_textview);

        mSearchListView = (ListView) view.findViewById(R.id.listview_search);
        mBusinesses = new ArrayList<BusinessSearchResult>();
        mOriginalBusinesses = new ArrayList<BusinessSearchResult>();
        mContactNames = new ArrayList<String>();
        mCombined = new ArrayList<Object>();
        mContactPhotos = new LinkedHashMap<String, Uri>();
        mSearchAdapter = new TransactionDetailSearchAdapter(getActivity(),mBusinesses, mContactNames,mCombined,mContactPhotos);
        mSearchListView.setAdapter(mSearchAdapter);

        goSearch();

        mCategoryListView = (ListView) view.findViewById(R.id.listview_category);

        mCategories = mCoreAPI.loadCategories();
        mCategories.addAll(Arrays.asList(getActivity().getResources().getStringArray(R.array.transaction_categories_list)));
        for(int index=0; index<mCategories.size(); index++) {
            String cat = mCategories.get(index);
            if(cat.equals("Income:")) {
                baseIncomePosition = index;
                originalBaseIncomePosition = index;
            }
            if(cat.equals("Expense:")) {
                baseExpensePosition = index;
                originalBaseExpensePosition = index;
            }
            if(cat.equals("Transfer:")) {
                baseTransferPosition = index;
                originalBaseTransferPosition = index;
            }
            if(cat.equals("Exchange:")) {
                baseExchangePosition = index;
                originalBaseExchangePosition = index;
            }
        }
        mOriginalCategories = new ArrayList<String>();
        mOriginalCategories.addAll(mCategories);


        mCategoryAdapter = new TransactionDetailCategoryAdapter(getActivity(),mCategories);
        mCategoryListView.setAdapter(mCategoryAdapter);

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#eef2f7"),Color.parseColor("#a9bfd6")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mDateTextView.getPaint().setShader(textShader);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mDateTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.BOLD);
        mPayeeEditText.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mCategoryEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);

        mFiatValueEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitcoinValueTextview.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.BOLD);

        mNoteEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mDoneButton.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);

        mDummyFocus.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                    if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() > 30) {
                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(0, 0);
                    }
                }
            }
        });

        getContactsList();

        mAdvanceDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAdvancedDetails();
            }
        });

        mXButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDummyFocus.requestFocus();
                mAdvancedDetailsPopup.setVisibility(View.GONE);
            }
        });

        mPayeeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mAdvanceDetailsButton.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mNoteDetailLayout.setVisibility(View.GONE);
                    mSearchListView.setVisibility(View.VISIBLE);
                } else {
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                    mSentDetailLayout.setVisibility(View.VISIBLE);
                    mNoteDetailLayout.setVisibility(View.VISIBLE);
                    mSearchListView.setVisibility(View.GONE);
                }
            }
        });

        mCategoryEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mDateTextView.setVisibility(View.GONE);
                    mNameDetailLayout.setVisibility(View.GONE);
                    mAdvanceDetailsButton.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mDoneButton.setVisibility(View.GONE);
                    mCategoryListView.setVisibility(View.VISIBLE);
                    popupTriangle.setVisibility(View.VISIBLE);
                    if (!mCategoryEdittext.getText().toString().isEmpty()) {
                        mCategoryEdittext.setSelection(currentType.length(), mCategoryEdittext.getText().toString().length());
                    }
                    updateBlanks(mCategoryEdittext.getText().toString().substring(mCategoryEdittext.getText().toString().indexOf(':')+1));
                    goCreateCategoryList(mCategoryEdittext.getText().toString().substring(mCategoryEdittext.getText().toString().indexOf(':')+1));
                    mCategoryAdapter.notifyDataSetChanged();
                } else {
                    mDateTextView.setVisibility(View.VISIBLE);
                    mNameDetailLayout.setVisibility(View.VISIBLE);
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                    mSentDetailLayout.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.VISIBLE);
                    mCategoryListView.setVisibility(View.GONE);
                    popupTriangle.setVisibility(View.GONE);
                }
            }
        });

        mNoteEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mAdvanceDetailsButton.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mDoneButton.setVisibility(View.GONE);
                } else {
                    mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                    mSentDetailLayout.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.VISIBLE);
                }
            }
        });
        mNoteEdittext.setHorizontallyScrolling(false);
        mNoteEdittext.setMaxLines(Integer.MAX_VALUE);

        mPayeeEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mCategoryEdittext.requestFocus();
                    return true;
                }else if(actionId == EditorInfo.IME_ACTION_DONE){
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mCategoryEdittext.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mNoteEdittext.requestFocus();
                    return true;
                }else if(actionId == EditorInfo.IME_ACTION_DONE){
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mNoteEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mPayeeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mCombined.clear();
                if (editable.toString().isEmpty()) {
                    mCombined.addAll(mOriginalBusinesses);
                } else {
                    getMatchedContactsList(editable.toString());
                    getMatchedBusinessList(editable.toString());
                    combineMatchLists();
                }
                mSearchAdapter.notifyDataSetChanged();
                ListViewUtility.setTransactionDetailListViewHeightBasedOnChildren(mSearchListView, mCombined.size(), getActivity());
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
                if (!doEdit) {
                    if (!catSelected) {
                        String temp = editable.toString();
                        doEdit = true;
                        editable.clear();
                        editable.append(defaultCat.toString()).append(":").append(temp);
                        doEdit = false;
                        catSelected = true;
                    }
                    if ((currentType.equals("Income:") && !editable.toString().startsWith("Income:")) || (currentType.equals("Expense:") && !editable.toString().startsWith("Expense:")) || (currentType.equals("Transfer:") && !editable.toString().startsWith("Transfer:")) || (currentType.equals("Exchange:") && !editable.toString().startsWith("Exchange:"))) {
                        doEdit = true;
                        editable.clear();
                        editable.append(mCategoryOld);
                        doEdit = false;
                    }
                    updateBlanks(editable.toString().substring(editable.toString().indexOf(':') + 1));
                    goCreateCategoryList(editable.toString().substring(editable.toString().indexOf(':') + 1));
                    mCategoryAdapter.notifyDataSetChanged();
                    mCategoryOld = mCategoryEdittext.getText().toString();
                }
            }
        });

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mSearchAdapter.getItem(i) instanceof BusinessSearchResult){
                    mPayeeEditText.setText(((BusinessSearchResult) mSearchAdapter.getItem(i)).getName());
                }else{
                    mPayeeEditText.setText((String) mSearchAdapter.getItem(i));
                }
                mDateTextView.setVisibility(View.VISIBLE);
                mAdvanceDetailsButton.setVisibility(View.VISIBLE);
                mSentDetailLayout.setVisibility(View.VISIBLE);
                mNoteDetailLayout.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.GONE);
                if(mFromRequest || mFromSend) {
                    mCategoryEdittext.requestFocus();
                }else{
                    mDummyFocus.requestFocus();
                }
            }
        });

        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                catSelected = true;
                if(mCategories.get(i).startsWith("Income:")){
                    currentType = "Income:";
                }else if(mCategories.get(i).startsWith("Expense:")){
                    currentType = "Expense:";
                }else if(mCategories.get(i).startsWith("Transfer:")){
                    currentType = "Transfer:";
                }else if(mCategories.get(i).startsWith("Exchange:")){
                    currentType = "Exchange:";
                }
                //TODO move the strings around depending on negative/positive value
                doEdit = true;
                mCategoryEdittext.setText(mCategoryAdapter.getItem(i));
                doEdit = false;
                if(i==baseIncomePosition || i==baseExpensePosition || i == baseTransferPosition || i == baseExchangePosition){
                    mCategoryEdittext.setSelection(mCategoryEdittext.getText().length());
                }else {
                    if(mFromSend || mFromRequest) {
                        mNoteEdittext.requestFocus();
                    }else{
                        mDummyFocus.requestFocus();
                    }
                }
            }
        });

        mFiatValueEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                Common.showHelpInfo(getActivity(), "Transaction Detail", "Transaction Detail info");
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCoreAPI.addCategory(mCategoryEdittext.getText().toString(), mCoreAPI.loadCategories());
                getActivity().onBackPressed();
            }
        });

        if(mFromSend || mFromRequest){
            mCategoryEdittext.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mPayeeEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }

        UpdateView(mTransaction);
        return view;
    }

    private void updateBlanks(String term){
        if(baseIncomePosition < mCategories.size()) {
            mCategories.remove(baseIncomePosition);
            mCategories.add(baseIncomePosition, "Income:" + term);
        }
        if(baseExpensePosition < mCategories.size()) {
            mCategories.remove(baseExpensePosition);
            mCategories.add(baseExpensePosition, "Expense:" + term);
        }
        if(baseTransferPosition < mCategories.size()){
            mCategories.remove(baseTransferPosition);
            mCategories.add(baseTransferPosition, "Transfer:" + term);
        }
        if(baseExchangePosition < mCategories.size()) {
            mCategories.remove(baseExchangePosition);
            mCategories.add(baseExchangePosition, "Exchange:" + term);
        }

        mOriginalCategories.remove(originalBaseIncomePosition);
        mOriginalCategories.add(originalBaseIncomePosition,"Income:" + term);
        mOriginalCategories.remove(originalBaseExpensePosition);
        mOriginalCategories.add(originalBaseExpensePosition, "Expense:" + term);
        mOriginalCategories.remove(originalBaseTransferPosition);
        mOriginalCategories.add(originalBaseTransferPosition, "Transfer:" + term);
        mOriginalCategories.remove(originalBaseExchangePosition);
        mOriginalCategories.add(originalBaseExchangePosition, "Exchange:" + term);
    }

    private void ShowAdvancedDetails()
    {
        mAdvancedDetailsPopup.setVisibility(View.VISIBLE);

        SpannableStringBuilder inAddresses = new SpannableStringBuilder();
        SpannableStringBuilder outAddresses = new SpannableStringBuilder();
        String baseUrl = "";
        if (mCoreAPI.isTestNet()) { // TESTNET
            baseUrl += "https://blockexplorer.com/testnet/";
        } else { // LIVE
            baseUrl += "https://blockchain.info/";
        }

        int start = 0;
        int end = 0;
        for (CoreAPI.TxOutput t : mTransaction.getOutputs()) {
            String val = mCoreAPI.FormatDefaultCurrency(t.getmValue(), true, false);
            SpannableString html = new SpannableString(val+"\n");
            end = val.length();
            final String url = baseUrl + t.getSzAddress();
            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent i = new Intent( Intent.ACTION_VIEW );
                    i.setData(Uri.parse(url));
                    startActivity( i );
                }
            };
            html.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (t.getInput()) {
                inAddresses.append(html);
            } else {
                outAddresses.append(html);
            }
        }

        SpannableStringBuilder s = new SpannableStringBuilder();
        start = 0;
        end=0;
        s.append("Transaction ID").setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append("\n");

        start = s.length();
        s.append(mTransaction.getID());
        end = s.length();
        final String finalBaseUrl = baseUrl;
        ClickableSpan url = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent i = new Intent( Intent.ACTION_VIEW );
                i.setData( Uri.parse(finalBaseUrl + mTransaction.getID()) );
                startActivity( i );
            }
        };
        s.setSpan(url, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append("\n\n");

        //Total Sent - formatSatoshi
        start = s.length();
        s.append("Total Sent").setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append("\n");

        long feesSatoshi = mTransaction.getABFees()+mTransaction.getMinerFees();
        long total = mTransaction.getAmountSatoshi() + feesSatoshi;
        s.append(mCoreAPI.getUserBTCSymbol()+" "+mCoreAPI.FormatDefaultCurrency(total, true, false))
                .setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(Typeface.NORMAL), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append("\n\n");


        //Source - inAddresses
        start = s.length();
        s.append("Source").setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append("\n");
        s.append(inAddresses);
        s.append("\n\n");

        //Destination - outAddresses
        start = s.length();
        s.append("Destination").setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append("\n");
        s.append(outAddresses);
        s.append("\n\n");


        //Miner Fee - formatSatoshi
        start = s.length();
        s.append("Miner Fee").setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append("\n");

        start = s.length();
        s.append(mCoreAPI.getUserBTCSymbol()+" "+mCoreAPI.FormatDefaultCurrency(feesSatoshi, true, false))
                .setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(Typeface.NORMAL), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mAdvancedDetailTextView.setText(s);
        mAdvancedDetailTextView.setMovementMethod( LinkMovementMethod.getInstance() );
    }

    private void UpdateView(Transaction transaction) {
        String dateString = new SimpleDateFormat("MMM dd yyyy, kk:mm aa").format(transaction.getDate()*1000);
        mDateTextView.setText(dateString);

        String pretext = mFromSend ? getActivity().getResources().getString(R.string.transaction_details_from) :
                getActivity().getResources().getString(R.string.transaction_details_to);
        mToFromName.setText(pretext+transaction.getWalletName());

        mPayeeEditText.setText(transaction.getName());
        mNoteEdittext.setText(transaction.getNotes());
        doEdit = true;
        mCategoryEdittext.setText(transaction.getCategory());
        doEdit = false;

        long coinValue = transaction.getAmountSatoshi()+transaction.getMinerFees()+transaction.getABFees();
        mBitcoinValueTextview.setText(mCoreAPI.formatSatoshi(coinValue, false));

        String currencyValue = mCoreAPI.FormatCurrency(coinValue, mWallet.getCurrencyNum(), false, false);
        mFiatValueEdittext.setText(currencyValue.substring(0, currencyValue.indexOf('.') + Math.min(3, currencyValue.length() - currencyValue.indexOf('.'))));
        mFiatDenominationLabel.setText(mCoreAPI.FiatCurrencyAcronym());

        if(mFromSend) {
            String feeFormatted = "";
            feeFormatted = "+"+mCoreAPI.formatSatoshi(transaction.getMinerFees() + transaction.getABFees())+" fee";
            mFeeTextview.setText(feeFormatted);
            mFeeTextview.setVisibility(View.VISIBLE);
        } else {
            mFeeTextview.setVisibility(View.INVISIBLE);
        }
        mSearchListView.setVisibility(View.GONE);
    }

    public void goCreateCategoryList( String term ){
        mCategories.clear();
        for(int i = 0; i < mOriginalCategories.size();i++){
            String s = mOriginalCategories.get(i);
            if(s.toLowerCase().substring(s.indexOf(':')+1).contains(term.toLowerCase())){
                if(!mCategories.contains(s)) {
                    mCategories.add(s);
                    if(i == originalBaseIncomePosition){
                        baseIncomePosition = mCategories.indexOf(s);
                    }
                    if(i == originalBaseTransferPosition){
                        baseTransferPosition = mCategories.indexOf(s);
                    }
                    if(i == originalBaseExpensePosition){
                        baseExpensePosition = mCategories.indexOf(s);
                    }
                    if(i == originalBaseExchangePosition){
                        baseExchangePosition = mCategories.indexOf(s);
                    }
                }
            }
        }
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
            return api.getSearchByRadius("16093", "", strings[0], "", "1");
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
                if(mPayeeEditText.getText().toString().isEmpty()){
                    mCombined.addAll(mBusinesses);
                }else{
                    getMatchedContactsList(mPayeeEditText.getText().toString());
                    getMatchedBusinessList(mPayeeEditText.getText().toString());
                    combineMatchLists();
                }
            }catch (JSONException e) {
                e.printStackTrace();
                this.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }
            mSearchAdapter.notifyDataSetChanged();
            ListViewUtility.setTransactionDetailListViewHeightBasedOnChildren(mSearchListView,mCombined.size(),getActivity());
        }

        @Override
        protected void onCancelled(){
            mBusinessSearchAsyncTask = null;
            super.onCancelled();
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
        if(mBusinessSearchAsyncTask != null){
            mBusinessSearchAsyncTask.cancel(true);
        }
        mTransaction.setName(mPayeeEditText.getText().toString());
        mTransaction.setCategory(mCategoryEdittext.getText().toString());
        mTransaction.setNotes(mNoteEdittext.getText().toString());
        mTransaction.setAmountFiat(Double.valueOf(mFiatValueEdittext.getText().toString()));
        mCoreAPI.storeTransaction(mTransaction);
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
                String photoURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                if(photoURI!=null) {
                    Uri thumbUri = Uri.parse(photoURI);
                    mContactPhotos.put(name, thumbUri);
                }
            }
            for(String s: mContactPhotos.keySet()){
                mContactNames.add(s);
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

    public void goSearch(){
        mCombined.clear();
        mOriginalBusinesses.clear();
        mBusinesses.clear();
        if(locationEnabled) {
            mBusinessSearchAsyncTask = new BusinessSearchAsyncTask();
            mBusinessSearchAsyncTask.execute(mLocationManager.getLocation().getLatitude()+","+mLocationManager.getLocation().getLongitude());
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
