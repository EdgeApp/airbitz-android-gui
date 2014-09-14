package com.airbitz.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionDetailCategoryAdapter;
import com.airbitz.adapters.TransactionDetailSearchAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.SearchResult;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.models.defaultCategoryEnum;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created on 2/20/14.
 */
public class TransactionDetailFragment extends Fragment implements CurrentLocationManager.OnLocationChange {
    private final String TAG = getClass().getSimpleName();

    private HighlightOnPressButton mDoneButton;
    private RelativeLayout         mAdvanceDetailsButtonLayout;
    private HighlightOnPressButton mAdvanceDetailsButton;

    private TextView mDateTextView;
    private RelativeLayout mPayeeNameLayout;
    private TextView mTitleTextView;
    private TextView mNotesTextView;
    private TextView mToFromName;
    private EditText mPayeeEditText;
    private ImageView mPayeeImageView;
    private FrameLayout mPayeeImageViewFrame;
    private TextView mBitcoinValueTextview;
    private TextView mBTCFeeTextView;
    private TextView mBitcoinSignTextview;

    private TextView mCategoryTextView;
    private LinearLayout mCategoryEdittextLayout;
    private LinearLayout mCategoryPopupLayout;


    private View mDummyFocus;

    private View popupTriangle;

    private CurrentLocationManager mLocationManager;
    private boolean locationEnabled;

    private String mCategoryOld = "";

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

    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;

    private LinearLayout mSentDetailLayout;
    private LinearLayout mNoteDetailLayout;
    private RelativeLayout mNameDetailLayout;

    private EditText mFiatValueEdittext;
    private String mFiatValue;
    private TextView mFiatDenominationLabel;
    private LinearLayout mEdittextNotesLayout;
    private EditText mNoteEdittext;
    private EditText mCategoryEdittext;


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

    private Calculator mCalculator;

    private Wallet mWallet;
    private Transaction mTransaction;

    private BusinessSearchAsyncTask mBusinessSearchAsyncTask = null;

    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(WalletsFragment.FROM_SOURCE) != null && bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_SEND)) {
                Common.LogD(TAG, "SEND");
                mFromSend = true;
            } else if (bundle.getString(WalletsFragment.FROM_SOURCE) != null && bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_REQUEST)) {
                mFromRequest = true;
                Common.LogD(TAG, "REQUEST");
            }

            String walletUUID = bundle.getString(Wallet.WALLET_UUID);
            String txId = bundle.getString(Transaction.TXID);
            if (walletUUID.isEmpty()) {
                Common.LogD(TAG, "no detail info");
            } else {
                mCoreAPI = CoreAPI.getApi();
                mWallet = mCoreAPI.getWalletFromUUID(walletUUID);
                mTransaction = mCoreAPI.getTransaction(walletUUID, txId);

                if (mTransaction.getCategory().isEmpty()) {
                    currentType = defaultCat.toString() + ":";
                } else if (mTransaction.getCategory().startsWith("Income:")) {
                    currentType = "Income:";
                    catSelected = true;
                } else if (mTransaction.getCategory().startsWith("Expense:")) {
                    currentType = "Expense:";
                    catSelected = true;
                } else if (mTransaction.getCategory().startsWith("Transfer:")) {
                    currentType = "Transfer:";
                    catSelected = true;
                } else if (mTransaction.getCategory().startsWith("Exchange:")) {
                    currentType = "Exchange:";
                    catSelected = true;
                }
            }
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_transaction_detail, container, false);
        } else {
            return mView;
        }

        mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), "Enable location services for better results", Toast.LENGTH_SHORT).show();
        } else {
            locationEnabled = true;
        }

        mCalculator = ((NavigationActivity) getActivity()).getCalculatorView();

        popupTriangle = mView.findViewById(R.id.fragment_transactiondetail_listview_triangle);

        mDoneButton = (HighlightOnPressButton) mView.findViewById(R.id.transaction_detail_button_done);
        mAdvanceDetailsButtonLayout = (RelativeLayout) mView.findViewById(R.id.transaction_detail_button_advanced_layout);
        mAdvanceDetailsButton = (HighlightOnPressButton) mView.findViewById(R.id.transaction_detail_button_advanced);

        mTitleTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_title);
        mNotesTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_notes);
        mPayeeNameLayout = (RelativeLayout) mView.findViewById(R.id.transaction_detail_layout_name);
        mPayeeEditText = (EditText) mView.findViewById(R.id.transaction_detail_edittext_name);
        mPayeeImageView = (ImageView) mView.findViewById(R.id.transaction_detail_contact_pic);
        mPayeeImageViewFrame = (FrameLayout) mView.findViewById(R.id.transaction_detail_contact_pic_frame);
        mToFromName = (TextView) mView.findViewById(R.id.transaction_detail_textview_to_wallet);
        mBitcoinValueTextview = (TextView) mView.findViewById(R.id.transaction_detail_textview_bitcoin_value);
        mBTCFeeTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_btc_fee_value);
        mDateTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_date);

        mFiatValueEdittext = (EditText) mView.findViewById(R.id.transaction_detail_edittext_dollar_value);
        mFiatDenominationLabel = (TextView) mView.findViewById(R.id.transaction_detail_textview_currency_sign);
        mBitcoinSignTextview = (TextView) mView.findViewById(R.id.transaction_detail_textview_bitcoin_sign);

        mEdittextNotesLayout = (LinearLayout) mView.findViewById(R.id.transaction_detail_layout_edittext_notes);
        mNoteEdittext = (EditText) mView.findViewById(R.id.transaction_detail_edittext_notes);
        mCategoryTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_category);
        mCategoryEdittextLayout = (LinearLayout) mView.findViewById(R.id.transaction_detail_edittext_category_layout);
        mCategoryEdittext = (EditText) mView.findViewById(R.id.transaction_detail_edittext_category);
        mCategoryPopupLayout = (LinearLayout) mView.findViewById(R.id.transaction_detail_category_popup_layout);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.transaction_detail_button_back);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.transaction_detail_button_help);

        mSentDetailLayout = (LinearLayout) mView.findViewById(R.id.layout_sent_detail);
        mNoteDetailLayout = (LinearLayout) mView.findViewById(R.id.transaction_detail_layout_note);
        mNameDetailLayout = (RelativeLayout) mView.findViewById(R.id.transaction_detail_layout_name);

        mDummyFocus = mView.findViewById(R.id.fragment_transactiondetail_dummy_focus);

        mSearchListView = (ListView) mView.findViewById(R.id.listview_search);
        mBusinesses = new ArrayList<BusinessSearchResult>();
        mOriginalBusinesses = new ArrayList<BusinessSearchResult>();
        mContactNames = new ArrayList<String>();
        mCombined = new ArrayList<Object>();
        mContactPhotos = new LinkedHashMap<String, Uri>();
        mSearchAdapter = new TransactionDetailSearchAdapter(getActivity(), mBusinesses, mContactNames, mCombined, mContactPhotos);
        mSearchListView.setAdapter(mSearchAdapter);

        goSearch();

        mCategoryListView = (ListView) mView.findViewById(R.id.listview_category);

        mCategories = mCoreAPI.loadCategories();
        mCategories.addAll(Arrays.asList(getActivity().getResources().getStringArray(R.array.transaction_categories_list)));
        for (int index = 0; index < mCategories.size(); index++) {
            String cat = mCategories.get(index);
            if (cat.equals("Income:")) {
                baseIncomePosition = index;
                originalBaseIncomePosition = index;
            }
            if (cat.equals("Expense:")) {
                baseExpensePosition = index;
                originalBaseExpensePosition = index;
            }
            if (cat.equals("Transfer:")) {
                baseTransferPosition = index;
                originalBaseTransferPosition = index;
            }
            if (cat.equals("Exchange:")) {
                baseExchangePosition = index;
                originalBaseExchangePosition = index;
            }
        }
        mOriginalCategories = new ArrayList<String>();
        mOriginalCategories.addAll(mCategories);


        mCategoryAdapter = new TransactionDetailCategoryAdapter(getActivity(), mCategories);
        mCategoryListView.setAdapter(mCategoryAdapter);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mDateTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPayeeEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mCategoryEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mNoteEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mFiatValueEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitcoinValueTextview.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.BOLD);

        mDoneButton.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);

        mDummyFocus.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                    if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() > 30) {
                        ((NavigationActivity) getActivity()).hideSoftKeyboard(activityRootView);
                    }
                }
            }
        });

        getContactsList();

        mAdvanceDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAdvancedDetails(true);
            }
        });

        mPayeeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showPayeeSearch(hasFocus);
                mPayeeEditText.selectAll();
            }
        });

        mCategoryEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showCategoryPopup(hasFocus);
                if (hasFocus) {
                    if (!mCategoryEdittext.getText().toString().isEmpty()) {
                        highlightEditableText(mCategoryEdittext);
                        mCategoryEdittext.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, KeyEvent.META_SHIFT_ON));
                    }
                    updateBlanks(mCategoryEdittext.getText().toString().substring(mCategoryEdittext.getText().toString().indexOf(':') + 1));
                    goCreateCategoryList(mCategoryEdittext.getText().toString().substring(mCategoryEdittext.getText().toString().indexOf(':') + 1));
                    mCategoryAdapter.notifyDataSetChanged();
                }
            }
        });

        mNoteEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mAdvanceDetailsButtonLayout.setVisibility(View.GONE);
                    mSentDetailLayout.setVisibility(View.GONE);
                    mDoneButton.setVisibility(View.GONE);
                } else {
                    mAdvanceDetailsButtonLayout.setVisibility(View.VISIBLE);
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
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    showPayeeSearch(false);
                    ((NavigationActivity) getActivity()).hideSoftKeyboard(mPayeeEditText);
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
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mCategoryEdittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mNoteEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
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
                    mContactPhotos.clear();
                    mContactPhotos.putAll(Common.GetMatchedContactsList(getActivity(), mPayeeEditText.getText().toString()));
                    mContactNames.clear();
                    for(String s: mContactPhotos.keySet()) {
                        mContactNames.add(s);
                    }

                    getMatchedBusinessList(editable.toString());
                    combineMatchLists();
                }
                mSearchAdapter.notifyDataSetChanged();
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
                if (mSearchAdapter.getItem(i) instanceof BusinessSearchResult) {
                    mPayeeEditText.setText(((BusinessSearchResult) mSearchAdapter.getItem(i)).getName());
                } else {
                    String name = (String) mSearchAdapter.getItem(i);
                    mPayeeEditText.setText(name);
                    setContactImageFromName(name);
                }
                mDateTextView.setVisibility(View.VISIBLE);
                mAdvanceDetailsButtonLayout.setVisibility(View.VISIBLE);
                mSentDetailLayout.setVisibility(View.VISIBLE);
                mNoteDetailLayout.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.GONE);
                if (mFromRequest || mFromSend) {
                    mCategoryEdittext.requestFocus();
                } else {
                    mDummyFocus.requestFocus();
                }
            }
        });

        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                catSelected = true;
                if (mCategories.get(i).startsWith("Income:")) {
                    currentType = "Income:";
                } else if (mCategories.get(i).startsWith("Expense:")) {
                    currentType = "Expense:";
                } else if (mCategories.get(i).startsWith("Transfer:")) {
                    currentType = "Transfer:";
                } else if (mCategories.get(i).startsWith("Exchange:")) {
                    currentType = "Exchange:";
                }
                //TODO move the strings around depending on negative/positive value
                doEdit = true;
                mCategoryEdittext.setText(mCategoryAdapter.getItem(i));
                doEdit = false;
                if (i == baseIncomePosition || i == baseExpensePosition || i == baseTransferPosition || i == baseExchangePosition) {
                    mCategoryEdittext.setSelection(mCategoryEdittext.getText().length());
                }
                mDummyFocus.requestFocus();
                showCategoryPopup(false);
            }
        });

        final TextWatcher mBTCTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mFiatValue = mFiatValueEdittext.getText().toString(); // global save
                mFiatValueEdittext.setSelection(mFiatValue.length());
            }
        };

        mFiatValueEdittext.addTextChangedListener(mBTCTextWatcher);
        mFiatValueEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mFiatValue = mFiatValueEdittext.getText().toString(); // global save
                    mCalculator.setEditText(mFiatValueEdittext);
                    mFiatValueEdittext.selectAll();
                    ((NavigationActivity) getActivity()).showCalculator();
                } else {
                    mFiatValueEdittext.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mFiatValueEdittext.setText(mFiatValue);
                        }
                    }, 1000);
                    ((NavigationActivity) getActivity()).hideCalculator();
                }
            }
        });

        mFiatValueEdittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).showCalculator();
            }
        });

        View.OnTouchListener preventOSKeyboard = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                edittext.selectAll();
                return true; // the listener has consumed the event, no keyboard popup
            }
        };

        mFiatValueEdittext.setOnTouchListener(preventOSKeyboard);
        mFiatValueEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
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
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.TRANSACTION_DETAILS), NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCoreAPI.addCategory(mCategoryEdittext.getText().toString());
                getActivity().onBackPressed();
            }
        });

        UpdateView(mTransaction);
        mPayeeEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mNoteEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mCategoryEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (mFromSend || mFromRequest) {
            mPayeeEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }

        return mView;
    }

    private void setContactImageFromName(String name) {
        Uri payeeImage = mContactPhotos.get(name);
        if(mContactPhotos!=null && payeeImage!=null) {
            mPayeeImageView.setVisibility(View.VISIBLE);
//            mPayeeImageView.setImageURI(null);
            mPayeeImageView.setImageURI(payeeImage);
        } else {
            mPayeeImageView.setVisibility(View.GONE);
        }
    }

    private void showPayeeSearch(boolean hasFocus) {
        if (hasFocus) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 95.0f);

            mAdvanceDetailsButton.setVisibility(View.GONE);
            mSentDetailLayout.setVisibility(View.GONE);
            mNoteDetailLayout.setVisibility(View.GONE);
            mNameDetailLayout.setLayoutParams(params);
            mSearchListView.setVisibility(View.VISIBLE);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 10.0f);

            mAdvanceDetailsButton.setVisibility(View.VISIBLE);
            mSentDetailLayout.setVisibility(View.VISIBLE);
            mNoteDetailLayout.setVisibility(View.VISIBLE);
            mNameDetailLayout.setLayoutParams(params);
            mSearchListView.setVisibility(View.GONE);
        }
    }

    private void showCategoryPopup(boolean hasFocus) {
        if (hasFocus) {
            mDateTextView.setVisibility(View.GONE);
            mPayeeNameLayout.setVisibility(View.GONE);
            mAdvanceDetailsButtonLayout.setVisibility(View.GONE);
            mSentDetailLayout.setVisibility(View.GONE);
            mDoneButton.setVisibility(View.GONE);

            if (true)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 75.0f);
                mNoteDetailLayout.setLayoutParams(params);
            }

            if (true)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 6.66f);
                mCategoryTextView.setLayoutParams(params);
            }

            mCategoryPopupLayout.setVisibility(View.VISIBLE);

            if (true)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 13.2f);
                mCategoryEdittextLayout.setLayoutParams(params);
            }

            mEdittextNotesLayout.setVisibility(View.GONE);
            mNotesTextView.setVisibility(View.GONE);
            mNoteEdittext.setVisibility(View.GONE);
        } else {

            mDateTextView.setVisibility(View.VISIBLE);
            mPayeeNameLayout.setVisibility(View.VISIBLE);
            mAdvanceDetailsButtonLayout.setVisibility(View.VISIBLE);
            mSentDetailLayout.setVisibility(View.VISIBLE);
            mDoneButton.setVisibility(View.VISIBLE);


            if (true)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 50.0f);
                mNoteDetailLayout.setLayoutParams(params);
            }

            if (true)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 10.0f);
                mCategoryTextView.setLayoutParams(params);
            }

            mCategoryPopupLayout.setVisibility(View.GONE);

            if (true)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 20.0f);
                mCategoryEdittextLayout.setLayoutParams(params);
            }

            mEdittextNotesLayout.setVisibility(View.VISIBLE);
            mNotesTextView.setVisibility(View.VISIBLE);
            mNoteEdittext.setVisibility(View.VISIBLE);
        }
    }

    private void highlightEditableText(EditText editText) {
        if (editText.getText().toString().startsWith("Income:")) {
            editText.setSelection(7, editText.length());
        } else if (editText.getText().toString().startsWith("Expense:")) {
            editText.setSelection(8, editText.length());
        } else if (editText.getText().toString().startsWith("Transfer:")) {
            editText.setSelection(9, editText.length());
        } else if (editText.getText().toString().startsWith("Exchange:")) {
            editText.setSelection(9, editText.length());
        }
    }

    private void updateBlanks(String term) {
        if (baseIncomePosition < mCategories.size()) {
            mCategories.remove(baseIncomePosition);
            mCategories.add(baseIncomePosition, "Income:" + term);
        }
        if (baseExpensePosition < mCategories.size()) {
            mCategories.remove(baseExpensePosition);
            mCategories.add(baseExpensePosition, "Expense:" + term);
        }
        if (baseTransferPosition < mCategories.size()) {
            mCategories.remove(baseTransferPosition);
            mCategories.add(baseTransferPosition, "Transfer:" + term);
        }
        if (baseExchangePosition < mCategories.size()) {
            mCategories.remove(baseExchangePosition);
            mCategories.add(baseExchangePosition, "Exchange:" + term);
        }

        mOriginalCategories.remove(originalBaseIncomePosition);
        mOriginalCategories.add(originalBaseIncomePosition, "Income:" + term);
        mOriginalCategories.remove(originalBaseExpensePosition);
        mOriginalCategories.add(originalBaseExpensePosition, "Expense:" + term);
        mOriginalCategories.remove(originalBaseTransferPosition);
        mOriginalCategories.add(originalBaseTransferPosition, "Transfer:" + term);
        mOriginalCategories.remove(originalBaseExchangePosition);
        mOriginalCategories.add(originalBaseExchangePosition, "Exchange:" + term);
    }

    private void ShowAdvancedDetails(boolean hasFocus) {
        if (hasFocus) {
            SpannableStringBuilder inAddresses = new SpannableStringBuilder();
            long inSum = 0;
            SpannableStringBuilder outAddresses = new SpannableStringBuilder();
            String baseUrl;
            if (mCoreAPI.isTestNet()) {
                baseUrl = "https://blockexplorer.com/testnet/";
            } else { // LIVE
                baseUrl = "https://blockchain.info/";
            }

            int start = 0;
            int end = 0;
            for (CoreAPI.TxOutput output : mTransaction.getOutputs()) {
                start = 0;
                end = 0;
                SpannableString val = new SpannableString(mCoreAPI.formatSatoshi(output.getmValue()));
                SpannableString address = new SpannableString(output.getAddress());
                end = address.length();
                final String url = baseUrl + "/address/" + output.getAddress();
                ClickableSpan span = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        mActivity.startActivity(i);
                    }
                };
                address.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                address.setSpan(new RelativeSizeSpan(0.95f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                SpannableStringBuilder full = new SpannableStringBuilder();
                full.append(address);
                full.append("\n");
                start = full.length();
                full.append(val).setSpan(new ForegroundColorSpan(Color.BLACK), start, start + val.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                full.append("\n");

                if (output.getmInput()) {
                    inAddresses.append(full);
                    inSum += output.getmValue();
                } else {
                    outAddresses.append(full);
                }
            }

            long feesSatoshi = mTransaction.getABFees() + mTransaction.getMinerFees();
            long netSum = inSum - feesSatoshi;

            SpannableStringBuilder s = new SpannableStringBuilder();
            start = 0;
            end = 0;
            s.append("Transaction ID").setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n");

            start = s.length();
            s.append(mTransaction.getmMalleableID());
            end = s.length();
            final String finalBaseUrl = baseUrl;
            ClickableSpan url = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(finalBaseUrl + "tx/" + mTransaction.getmMalleableID()));
                    mActivity.startActivity(i);
                }
            };
            s.setSpan(url, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n\n");

            //Total Sent - formatSatoshi
            start = s.length();
            s.append("Total Sent").setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n");

            s.append(mCoreAPI.formatSatoshi(netSum))
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
            s.append(mCoreAPI.getUserBTCSymbol() + " " + mCoreAPI.FormatDefaultCurrency(feesSatoshi, true, false))
                    .setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(Typeface.NORMAL), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(s), NavigationActivity.Tabs.WALLET.ordinal());
        } else {
            mDummyFocus.requestFocus();
        }
    }

    private void UpdateView(Transaction transaction) {
        String dateString = new SimpleDateFormat("MMM dd yyyy, kk:mm aa").format(transaction.getDate() * 1000);
        mDateTextView.setText(dateString);

        String pretext = mFromSend ? getActivity().getResources().getString(R.string.transaction_details_from) :
                getActivity().getResources().getString(R.string.transaction_details_to);
        mToFromName.setText(pretext + transaction.getWalletName());

        mPayeeEditText.setText(transaction.getName());
        setContactImageFromName(transaction.getName());
        mNoteEdittext.setText(transaction.getNotes());
        doEdit = true;
        mCategoryEdittext.setText(transaction.getCategory());
        doEdit = false;

        long coinValue = 0;
        String feeFormatted;
        if (transaction.getAmountSatoshi() < 0)
        {
            coinValue = transaction.getAmountSatoshi() + transaction.getMinerFees() + transaction.getABFees();
            feeFormatted = "+" + mCoreAPI.FormatDefaultCurrency(transaction.getMinerFees() + transaction.getABFees(), true, false) + " fee";
        }
        else
        {
            coinValue = transaction.getAmountSatoshi();
            feeFormatted = "";
        }

        mBitcoinValueTextview.setText(mCoreAPI.formatSatoshi(coinValue, false));

        String currencyValue = mCoreAPI.FormatCurrency(coinValue, mWallet.getCurrencyNum(), false, false);
        mFiatValue = currencyValue;
        mFiatValueEdittext.setText(currencyValue);
        mFiatDenominationLabel.setText(mCoreAPI.FiatCurrencyAcronym());
        mBitcoinSignTextview.setText(mCoreAPI.getDefaultBTCDenomination());

        mBTCFeeTextView.setText(feeFormatted);
        mSearchListView.setVisibility(View.GONE);
    }

    public void goCreateCategoryList(String term) {
        mCategories.clear();
        for (int i = 0; i < mOriginalCategories.size(); i++) {
            String s = mOriginalCategories.get(i);
            if (s.toLowerCase().substring(s.indexOf(':') + 1).contains(term.toLowerCase())) {
                if (!mCategories.contains(s)) {
                    mCategories.add(s);
                    if (i == originalBaseIncomePosition) {
                        baseIncomePosition = mCategories.indexOf(s);
                    }
                    if (i == originalBaseTransferPosition) {
                        baseTransferPosition = mCategories.indexOf(s);
                    }
                    if (i == originalBaseExpensePosition) {
                        baseExpensePosition = mCategories.indexOf(s);
                    }
                    if (i == originalBaseExchangePosition) {
                        baseExchangePosition = mCategories.indexOf(s);
                    }
                }
            }
        }
    }

    @Override
    public void OnCurrentLocationChange(Location location) {
        mLocationManager.removeLocationChangeListener(this);
        mBusinessSearchAsyncTask = new BusinessSearchAsyncTask();
        mBusinessSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLocationManager.getLocation().getLatitude() + "," + mLocationManager.getLocation().getLongitude());
    }

    class BusinessSearchAsyncTask extends AsyncTask<String, Integer, String> {
        private AirbitzAPI api = AirbitzAPI.getApi();

        public BusinessSearchAsyncTask() {
        }

        @Override
        protected String doInBackground(String... strings) {
            return api.getSearchByRadius("16093", "", strings[0], "", "1");
        }

        @Override
        protected void onPostExecute(String searchResult) {
            try {
                mBusinesses.clear();
                mCombined.clear();
                mOriginalBusinesses.clear();
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                mBusinesses.addAll(results.getBusinessSearchObjectArray());
                mOriginalBusinesses.addAll(mBusinesses);
                if (mPayeeEditText.getText().toString().isEmpty()) {
                    mCombined.addAll(mBusinesses);
                } else {
                    mContactPhotos.clear();
                    mContactPhotos.putAll(Common.GetMatchedContactsList(getActivity(), mPayeeEditText.getText().toString()));
                    mContactNames.clear();
                    for(String s: mContactPhotos.keySet()) {
                        mContactNames.add(s);
                    }
                    getMatchedBusinessList(mPayeeEditText.getText().toString());
                    combineMatchLists();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                this.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }
            mSearchAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            mBusinessSearchAsyncTask = null;
            super.onCancelled();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBusinessSearchAsyncTask != null) {
            mBusinessSearchAsyncTask.cancel(true);
        }
        mTransaction.setName(mPayeeEditText.getText().toString());
        mTransaction.setCategory(mCategoryEdittext.getText().toString());
        mTransaction.setNotes(mNoteEdittext.getText().toString());
        double amountFiat;
        try {
            amountFiat = Double.valueOf(mFiatValueEdittext.getText().toString());
        } catch (Exception e) {
            amountFiat = 0.0;
        }
        mTransaction.setAmountFiat(amountFiat);
        mCoreAPI.storeTransaction(mTransaction);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public void getContactsList() {
        ContentResolver cr = getActivity().getContentResolver();
        String columns[] = {ContactsContract.Contacts.DISPLAY_NAME};
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

//    public void getMatchedContactsList(String searchTerm) {
//        ContentResolver cr = getActivity().getContentResolver();
//        String columns[] = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
//                columns, ContactsContract.Contacts.DISPLAY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + searchTerm + "%"), null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
//        if (cur.getCount() > 0) {
//            mContactNames.clear();
//            mContactPhotos.clear();
//            while (cur.moveToNext()) {
//                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                String photoURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
//                if (photoURI != null) {
//                    Uri thumbUri = Uri.parse(photoURI);
//                    mContactPhotos.put(name, thumbUri);
//                }
//            }
//            for (String s : mContactPhotos.keySet()) {
//                mContactNames.add(s);
//            }
//        }
//        cur.close();
//    }

    public void getMatchedBusinessList(String searchTerm) {
        mBusinesses.clear();
        for (int i = 0; i < mOriginalBusinesses.size(); i++) {
            if (mOriginalBusinesses.get(i).getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                int j = 0;
                boolean flag = false;
                while (!flag && j != mBusinesses.size()) {
                    if (mBusinesses.get(j).getName().toLowerCase().compareTo(mOriginalBusinesses.get(i).getName().toLowerCase()) > 0) {
                        mBusinesses.add(j, mOriginalBusinesses.get(i));
                        flag = true;
                    }
                    j++;
                }
                if (j == mBusinesses.size() && !flag) {
                    mBusinesses.add(mOriginalBusinesses.get(i));
                }
            }
        }
    }

    public void combineMatchLists() {
        while (!mBusinesses.isEmpty() | !mContactNames.isEmpty()) {
            if (mBusinesses.isEmpty()) {
                mCombined.add(mContactNames.get(0));
                mContactNames.remove(0);
            } else if (mContactNames.isEmpty()) {
                mCombined.add(mBusinesses.get(0));
                mBusinesses.remove(0);
            } else if (mBusinesses.get(0).getName().toLowerCase().compareTo(mContactNames.get(0).toLowerCase()) < 0) {
                mCombined.add(mBusinesses.get(0));
                mBusinesses.remove(0);
            } else {
                mCombined.add(mContactNames.get(0));
                mContactNames.remove(0);
            }
        }
    }

    public void goSearch() {
        mCombined.clear();
        mOriginalBusinesses.clear();
        mBusinesses.clear();
        if (locationEnabled) {
            if (mLocationManager.getLocation() != null) {
                mBusinessSearchAsyncTask = new BusinessSearchAsyncTask();
                mBusinessSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLocationManager.getLocation().getLatitude() + "," + mLocationManager.getLocation().getLongitude());
            } else {
                mLocationManager.addLocationChangeListener(this);
            }
        }
    }
}
