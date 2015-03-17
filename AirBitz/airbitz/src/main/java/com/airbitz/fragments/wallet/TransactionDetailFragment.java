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

package com.airbitz.fragments.wallet;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
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
import com.airbitz.adapters.CategoryAdapter;
import com.airbitz.adapters.TransactionDetailCategoryAdapter;
import com.airbitz.adapters.TransactionDetailSearchAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_CC;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.directory.DirectoryDetailFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.settings.SettingFragment;
import com.airbitz.fragments.send.SuccessFragment;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.Category;
import com.airbitz.objects.CurrentLocationManager;
import com.airbitz.models.ProfileImage;
import com.airbitz.models.SearchResult;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;
import com.airbitz.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2/20/14.
 */
public class TransactionDetailFragment extends BaseFragment
        implements CurrentLocationManager.OnCurrentLocationChange,
        TransactionDetailCategoryAdapter.OnNewCategory {
    private final String TAG = getClass().getSimpleName();
    private final int MIN_AUTOCOMPLETE = 5;

    private HighlightOnPressButton mDoneButton;
    private HighlightOnPressButton mAdvanceDetailsButton;
    private HighlightOnPressSpinner mCategorySpinner;
    private TextView mDateTextView;
    private RelativeLayout mPayeeNameLayout;
    private TextView mTitleTextView;
    private TextView mNotesTextView;
    private TextView mToFromName;
    private EditText mPayeeEditText;
    private ImageView mPayeeImageView;
    private RelativeLayout mPayeeImageViewFrame;
    private TextView mBitcoinValueTextview;
    private TextView mBTCFeeTextView;
    private TextView mBitcoinSignTextview;
    private TextView mCategoryTextView;
    private LinearLayout mCategoryEdittextLayout;
    private LinearLayout mCategoryPopupLayout;
    private View mUpperLayout, mMiddleLayout, mLowerLayout;
    private View mDummyFocus;
    private CurrentLocationManager mLocationManager;
    private boolean locationEnabled;
    private String currentType = "";
    private boolean doEdit = false;
    private boolean mHasReminded = false;
    private Bundle bundle;
    private int baseIncomePosition = 0;
    private int baseExpensePosition = 1;
    private int baseTransferPosition = 2;
    private int baseExchangePosition = 3;
    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;
    private EditText mFiatValueEdittext;
    private String mFiatValue;
    private TextView mFiatDenominationLabel;
    private EditText mNoteEdittext;
    private EditText mCategoryEdittext;
    private List<BusinessSearchResult> mBusinesses;
    private List<BusinessSearchResult> mArrayNearBusinesses, mArrayOnlineBusinesses;
    private List<String> mContactNames;
    private List<String> mArrayAutoCompleteQueries;
    private ConcurrentHashMap<String, String> mArrayAddresses;
    private List<Object> mArrayAutoComplete;
    private HashMap<String, Uri> mCombinedPhotos;
    private HashMap<String, Long> mBizIds = new LinkedHashMap<String, Long>();
    private long mBizId;
    private String mBizDistance;
    private List<Category> mOriginalCategories;
    private List<Category> mCategories;
    private boolean mFromSend = false;
    private boolean mFromRequest = false;
    private ListView mSearchListView;
    private ListView mCategoryListView;
    private TransactionDetailSearchAdapter mSearchAdapter;
    private TransactionDetailCategoryAdapter mCategoryAdapter;
    private Calculator mCalculator;
    private Wallet mWallet;
    private Transaction mTransaction;
    private NearBusinessSearchAsyncTask mNearBusinessSearchAsyncTask = null;
    private OnlineBusinessSearchAsyncTask mOnlineBusinessSearchAsyncTask = null;
    private CheckReminderNotification mReminderTask = null;

    private Category baseIncomeCat, baseExpenseCat, baseTransferCat, baseExchangeCat;
    private int[] mCategoryBackgrounds = {R.drawable.bg_button_red, R.drawable.bg_btn_green,
            R.drawable.bg_btn_blue_stretch, R.drawable.bg_button_orange};

    private Picasso mPicasso;

    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
    private AlertDialog mMessageDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (NavigationActivity) getActivity();
        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_transaction_detail, container, false);
        }

        mPicasso = Picasso.with(getActivity());
        mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), getString(R.string.fragment_business_enable_location_services), Toast.LENGTH_SHORT).show();
        } else {
            locationEnabled = true;
        }

        mCalculator = ((NavigationActivity) getActivity()).getCalculatorView();

        mDoneButton = (HighlightOnPressButton) mView.findViewById(R.id.transaction_detail_button_done);
        mAdvanceDetailsButton = (HighlightOnPressButton) mView.findViewById(R.id.transaction_detail_button_advanced);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mTitleTextView.setText(R.string.transaction_details_title);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);

        mNotesTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_notes);
        mPayeeNameLayout = (RelativeLayout) mView.findViewById(R.id.transaction_detail_layout_name);
        mPayeeImageView = (ImageView) mView.findViewById(R.id.transaction_detail_contact_pic);
        mPayeeImageViewFrame = (RelativeLayout) mView.findViewById(R.id.transaction_detail_contact_pic_frame);
        mToFromName = (TextView) mView.findViewById(R.id.transaction_detail_textview_to_wallet);
        mBitcoinValueTextview = (TextView) mView.findViewById(R.id.transaction_detail_textview_bitcoin_value);
        mBTCFeeTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_btc_fee_value);
        mDateTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_date);

        mFiatValueEdittext = (EditText) mView.findViewById(R.id.transaction_detail_edittext_dollar_value);
        mFiatDenominationLabel = (TextView) mView.findViewById(R.id.transaction_detail_textview_currency_sign);
        mBitcoinSignTextview = (TextView) mView.findViewById(R.id.transaction_detail_textview_bitcoin_sign);

        mCategoryTextView = (TextView) mView.findViewById(R.id.transaction_detail_textview_category);
        mCategoryEdittextLayout = (LinearLayout) mView.findViewById(R.id.transaction_detail_edittext_category_layout);
        mCategoryPopupLayout = (LinearLayout) mView.findViewById(R.id.transaction_detail_category_popup_layout);

        mUpperLayout = mView.findViewById(R.id.transactiondetail_upper_layout);
        mMiddleLayout = mView.findViewById(R.id.transactiondetail_middle_layout);
        mLowerLayout = mView.findViewById(R.id.transactiondetail_lower_layout);

        mDummyFocus = mView.findViewById(R.id.fragment_transactiondetail_dummy_focus);

        mSearchListView = (ListView) mView.findViewById(R.id.listview_search);
        mBusinesses = new ArrayList<BusinessSearchResult>();
        mArrayNearBusinesses = new ArrayList<BusinessSearchResult>();
        mContactNames = new ArrayList<String>();
        mArrayAutoCompleteQueries = new ArrayList<String>();
        mArrayAutoComplete = new ArrayList<Object>();
        mArrayOnlineBusinesses = new ArrayList<BusinessSearchResult>();
        mArrayAddresses = new ConcurrentHashMap<String, String>();
        mCombinedPhotos = new LinkedHashMap<String, Uri>();
        mSearchAdapter = new TransactionDetailSearchAdapter(getActivity(), mBusinesses, mContactNames, mArrayAutoComplete, mCombinedPhotos);
        mSearchListView.setAdapter(mSearchAdapter);

        goSearch();

        mCategoryListView = (ListView) mView.findViewById(R.id.listview_category);
        mCategories = new ArrayList<Category>();

        mDateTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mFiatValueEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitcoinValueTextview.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.NORMAL);

        mDoneButton.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.NORMAL);

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

        mAdvanceDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAdvancedDetails(true);
            }
        });

        mCategorySpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.transaction_detail_button_category);
        CategoryAdapter mCategoryAdapter = new CategoryAdapter(mActivity, Arrays.asList(getResources().getStringArray(R.array.transaction_categories_list_no_colon)));
        mCategorySpinner.setAdapter(mCategoryAdapter);
        mCategorySpinner.setSelection(0);
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCategoryBackground(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        mPayeeImageViewFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBizId != 0) {
                    Bundle bundle = new Bundle();
                    bundle.putString(DirectoryDetailFragment.BIZID, String.valueOf(mBizId));
                    bundle.putString("", mPayeeEditText.getText().toString());
                    bundle.putString("", mBizDistance);
                    Fragment fragment = new DirectoryDetailFragment();
                    fragment.setArguments(bundle);
                    ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
                }
            }
        });

        mPayeeEditText = (EditText) mView.findViewById(R.id.transaction_detail_edittext_name);
        mPayeeEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPayeeEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mPayeeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showPayeeSearch(hasFocus);
                mPayeeEditText.selectAll();
            }
        });

        mPayeeEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mCategoryEdittext.requestFocus();
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    showPayeeSearch(false);
                    ((NavigationActivity) getActivity()).hideSoftKeyboard(mPayeeEditText);
                    updatePhoto();
                    updateBizId();
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
                if(!doEdit && isResumed()) {
                    updateAutoCompleteArray(mPayeeEditText.getText().toString());
                    updateBizId();
                    updatePhoto();
                    mSearchAdapter.notifyDataSetChanged();
                }
            }
        });

        mNoteEdittext = (EditText) mView.findViewById(R.id.transaction_detail_edittext_notes);
        mNoteEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mNoteEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mNoteEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showUpperLayout(!hasFocus);
                showMiddleLayout(!hasFocus);
            }
        });
        mNoteEdittext.setHorizontallyScrolling(false);
        mNoteEdittext.setMaxLines(Integer.MAX_VALUE);

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
        if(mActivity.isLargeDpi()) {
            ViewGroup vg = (ViewGroup) mView.findViewById(R.id.transaction_detail_layout_edittext_notes);
            ViewGroup.LayoutParams lp = vg.getLayoutParams();
            lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
            vg.setLayoutParams(lp);
        }

        mCategoryEdittext = (EditText) mView.findViewById(R.id.transaction_detail_edittext_category);
        mCategoryEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mCategoryEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mCategoryEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showCategoryPopup(hasFocus);
                if (hasFocus) {
                    if (!mCategoryEdittext.getText().toString().isEmpty()) {
                        highlightEditableText(mCategoryEdittext);
                    }
                    updateBlanks(mCategoryEdittext.getText().toString());
                    createNewCategoryChoices(mCategoryEdittext.getText().toString());
                }
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

        mCategoryEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!doEdit && isResumed()) {
                    Log.d(TAG, "editable=" + editable.toString());
                    updateBlanks(editable.toString());
                    createNewCategoryChoices(editable.toString());
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
                }
                updateBizId();
                updatePhoto();
                showUpperLayout(true);
                showMiddleLayout(true);
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
                setCurrentType(mCategories.get(i).getCategoryName());
                setCategoryText(TransactionDetailFragment.this.mCategoryAdapter.getItem(i).getCategoryName());
                if (i == baseIncomePosition || i == baseExpensePosition || i == baseTransferPosition || i == baseExchangePosition) {
                    mCategoryEdittext.setSelection(mCategoryEdittext.getText().length());
                }
                mDummyFocus.requestFocus();
                showCategoryPopup(false);
            }
        });

        final TextWatcher mFiatTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(isResumed()) {
                    mFiatValue = mFiatValueEdittext.getText().toString(); // global save
                    mFiatValueEdittext.setSelection(mFiatValue.length());
                }
            }
        };

        mFiatValueEdittext.addTextChangedListener(mFiatTextWatcher);
        mFiatValueEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showUpperLayout(!hasFocus);
                if (hasFocus) {
                    mFiatValue = mFiatValueEdittext.getText().toString(); // global save
                    mCalculator.setEditText(mFiatValueEdittext);
                    mFiatValueEdittext.selectAll();
                    ((NavigationActivity) getActivity()).showCalculator();
                } else {
                    ((NavigationActivity) getActivity()).hideCalculator();
                }
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
                    mFiatValue = mFiatValueEdittext.getText().toString(); // global save
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDone();
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
                goDone();
            }
        });

        if (mFromSend || mFromRequest) {
            mPayeeEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }

        return mView;
    }

    private void setupOriginalCategories() {
        // Initialize category items
        mCategories.clear();
        mOriginalCategories = new ArrayList<Category>();
        List<String> originalStrings = new ArrayList<>();
        List<String> catStrings = mCoreAPI.loadCategories();
        for(String cat : catStrings) {
            if(!originalStrings.contains(cat)) {
                originalStrings.add(cat);
            }
        }
        Collections.sort(originalStrings);
        for(String cat : originalStrings) {
            mOriginalCategories.add(new Category(cat, ""));
        }

        mOriginalCategories.add(baseIncomeCat = new Category(getString(R.string.fragment_category_income), "base"));
        mOriginalCategories.add(baseExpenseCat = new Category(getString(R.string.fragment_category_expense), "base"));
        mOriginalCategories.add(baseTransferCat = new Category(getString(R.string.fragment_category_transfer), "base"));
        mOriginalCategories.add(baseExchangeCat = new Category(getString(R.string.fragment_category_exchange), "base"));

        mCategories.addAll(mOriginalCategories);

        mCategoryAdapter = new TransactionDetailCategoryAdapter(getActivity(), mCategories);
        mCategoryListView.setAdapter(mCategoryAdapter);
    }

    private void showUpperLayout(boolean visible) {
        if (visible) {
            mUpperLayout.setVisibility(View.VISIBLE);
        } else {
            mUpperLayout.setVisibility(View.GONE);
        }
    }

    private void showMiddleLayout(boolean visible) {
        if (visible) {
            mMiddleLayout.setVisibility(View.VISIBLE);
        } else {
            mMiddleLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(WalletsFragment.FROM_SOURCE) != null && bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_SEND)) {
                Log.d(TAG, "SEND");
                mFromSend = true;
                setCurrentType(getString(R.string.fragment_category_expense));
            } else if (bundle.getString(WalletsFragment.FROM_SOURCE) != null && bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_REQUEST)) {
                mFromRequest = true;
                Log.d(TAG, "REQUEST");
                setCurrentType(getString(R.string.fragment_category_income));
            }

            String walletUUID = bundle.getString(Wallet.WALLET_UUID);
            String txId = bundle.getString(Transaction.TXID);
            if (walletUUID.isEmpty()) {
                Log.d(TAG, "no detail info");
            } else {
                mWallet = mCoreAPI.getWalletFromUUID(walletUUID);
                mTransaction = mCoreAPI.getTransaction(walletUUID, txId);

                if (mTransaction != null) {
                    if((mFromSend || mFromRequest) && !mTransaction.getCategory().contains(getString(R.string.fragment_category_transfer))) {
                        mTransaction.setCategory(currentType);
                    }
                    else {
                        setCurrentType(mTransaction.getCategory());
                    }
                    // if there is a bizId, add it as the first one of the map
                    if (mTransaction.getmBizId() != 0) {
                        mBizIds.put(mTransaction.getName(), mTransaction.getmBizId());
                        mBizId = mTransaction.getmBizId();
                    }
                }
            }
        }

        if(mContactNames == null || mContactNames.isEmpty()) {
            Log.d(TAG, "Getting Contact List");
            getContactsList();
        }

        if(mTransaction != null) {
            Log.d(TAG, "Updating view");
            FindBizIdThumbnail(mTransaction.getName(), mTransaction.getmBizId());
            UpdateView(mTransaction);
        }

        if(mOriginalCategories == null || mOriginalCategories.isEmpty()) {
            Log.d(TAG, "Getting original categories");
            setupOriginalCategories();
        }

        mCategoryAdapter.setOnNewCategoryListener(this);
        Log.d(TAG, "OnResume finished");
    }


    private void goDone() {
        mReminderTask = new CheckReminderNotification(mWallet);
        mReminderTask.execute();
    }

    private void done() {
        String category = mCategorySpinner.getSelectedItem().toString() + ":" + mCategoryEdittext.getText().toString();
        mCoreAPI.addCategory(category);
        mActivity.onBackPressed();
    }

    private void updateCategoryBackground(int position) {
        int newBackground = mCategoryBackgrounds[position];
        if(mCategorySpinner != null) {
            mCategorySpinner.setBackgroundResource(newBackground);
        }
        currentType = getResources().getStringArray(R.array.transaction_categories_list)[position];
        createNewCategoryChoices(mCategoryEdittext.getText().toString());
    }

    public void ShowReminderDialog(String title, String message) {
        if (mMessageDialog != null) {
            mMessageDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                done();
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(SettingFragment.START_RECOVERY_PASSWORD, true);
                                mActivity.switchFragmentThread(NavigationActivity.Tabs.MORE.ordinal(), bundle);
                            }
                        }
                ).setNegativeButton(getResources().getString(R.string.string_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        done();
                    }
                }
        );
        mMessageDialog = builder.create();
        mMessageDialog.show();
    }

    private void setCurrentType(String input) {
        int selected = 1;
        if (input.isEmpty() || input.startsWith(getString(R.string.fragment_category_income))) {
            currentType = getString(R.string.fragment_category_income);
            selected = 1;
        } else if (input.startsWith(getString(R.string.fragment_category_expense))) {
            currentType = getString(R.string.fragment_category_expense);
            selected = 0;
        } else if (input.startsWith(getString(R.string.fragment_category_transfer))) {
            currentType = getString(R.string.fragment_category_transfer);
            selected = 2;
        } else if (input.startsWith(getString(R.string.fragment_category_exchange))) {
            currentType = getString(R.string.fragment_category_exchange);
            selected = 3;
        }
        mCategorySpinner.setSelection(selected);
    }

    private void updateAutoCompleteArray(String strTerm) {
        // if there is anything in the payee field

        mArrayAutoComplete.clear();
        if (!strTerm.isEmpty()) {
            // go through all the near businesses
            mArrayAutoComplete.addAll(getMatchedNearBusinessList(strTerm));

            // go through all the contacts
            Map<String, Uri> list = Common.GetMatchedContactsList(mActivity, strTerm);
            for (String s : list.keySet()) {
                mArrayAutoComplete.add(s);
                mCombinedPhotos.put(s, list.get(s));
            }

            // check if we have less than the minimum
            if (mArrayAutoComplete.size() < MIN_AUTOCOMPLETE) {
                // add the matches from other businesses
                for (BusinessSearchResult business : mArrayOnlineBusinesses) {
                    // if it matches what the user has currently typed
                    if (business.getName().toLowerCase().contains(strTerm.toLowerCase())) {
                        // if it isn't already in the near array
                        if (!mArrayNearBusinesses.contains(business.getName().toLowerCase())) {
                            // add this business to the auto complete array
                            mArrayAutoComplete.add(business);
                        }
                    }
                }

                // issue an auto-complete request for it
                startOnlineBusinessSearch(strTerm);
            }

        } else {
            if (mFromRequest) {
                // this is a receive so use the address book
                // show all the contacts
                Map<String, Uri> list = Common.GetMatchedContactsList(mActivity, null);
                for (String s : list.keySet()) {
                    mArrayAutoComplete.add(s);
                    mCombinedPhotos.put(s, list.get(s));
                }

            } else {
                // this is a sent so we must be looking for businesses
                // since nothing in payee yet, just populate with businesses (already sorted by distance)
                mArrayAutoComplete.addAll(mArrayNearBusinesses);
            }
        }

        // force the table to reload itself
        mSearchAdapter.notifyDataSetChanged();
    }

    private void updatePhoto() {
        Uri payeeImage = mCombinedPhotos.get(mPayeeEditText.getText().toString());
        if (mCombinedPhotos != null && payeeImage != null) {
            mPayeeImageViewFrame.setVisibility(View.VISIBLE);

            mPayeeImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            int round = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            int dimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
            if (payeeImage.getScheme().contains("content")) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), payeeImage);
                    Bitmap bmap2 = ThumbnailUtils.extractThumbnail(bitmap, dimen, dimen);
                    RoundedTransformation rt = new RoundedTransformation(round, round);
                    bitmap = rt.transform(bmap2);
                    mPayeeImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "loading remote " + payeeImage.toString());
                mPicasso.with(getActivity())
                        .load(payeeImage)
                        .transform(new RoundedTransformation(round, round))
                        .into(mPayeeImageView);
            }
        } else {
            mPayeeImageViewFrame.setVisibility(View.INVISIBLE);
        }
    }

    private void showPayeeSearch(boolean hasFocus) {
        if (hasFocus) {

            mSearchListView.setVisibility(View.VISIBLE);

            updateAutoCompleteArray(mPayeeEditText.getText().toString());
            updateBizId();
            updatePhoto();
            mSearchAdapter.notifyDataSetChanged();
        } else {
            mSearchListView.setVisibility(View.GONE);
        }
    }

    private void showCategoryPopup(boolean hasFocus) {
        showUpperLayout(!hasFocus);
        showMiddleLayout(!hasFocus);
        if (hasFocus) {
            mCategoryPopupLayout.setVisibility(View.VISIBLE);
        } else {
            mCategoryPopupLayout.setVisibility(View.GONE);
        }
    }

    private void updateBizId() {
        mBizId = 0;
        if (mBizIds.containsKey(mPayeeEditText.getText().toString())) {
            mBizId = mBizIds.get(mPayeeEditText.getText().toString());
        }
        Log.d(TAG, "Biz ID: " + String.valueOf(mBizId));
    }


    private void highlightEditableText(EditText editText) {
        if (editText.getText().toString().startsWith(getString(R.string.fragment_category_income))) {
            editText.setSelection(7, editText.length());
        } else if (editText.getText().toString().startsWith(getString(R.string.fragment_category_expense))) {
            editText.setSelection(8, editText.length());
        } else if (editText.getText().toString().startsWith(getString(R.string.fragment_category_transfer))) {
            editText.setSelection(9, editText.length());
        } else if (editText.getText().toString().startsWith(getString(R.string.fragment_category_exchange))) {
            editText.setSelection(9, editText.length());
        }
    }

    private void updateBlanks(String term) {
        baseIncomeCat.setCategoryName(getString(R.string.fragment_category_income) + term);
        baseExpenseCat.setCategoryName(getString(R.string.fragment_category_expense) + term);
        baseTransferCat.setCategoryName(getString(R.string.fragment_category_transfer) + term);
        baseExchangeCat.setCategoryName(getString(R.string.fragment_category_exchange) + term);
    }

    private void ShowAdvancedDetails(boolean hasFocus) {
        if (hasFocus) {
            SpannableStringBuilder inAddresses = new SpannableStringBuilder();
            long inSum = 0;
            SpannableStringBuilder outAddresses = new SpannableStringBuilder();
            String baseUrl;
            if (mCoreAPI.isTestNet()) {
                baseUrl = "https://www.biteasy.com/testnet/";
            } else { // LIVE
                baseUrl = "https://www.biteasy.com/blockchain/";
            }
            final String finalBaseUrl = baseUrl;

            int start;
            int end;
            for (CoreAPI.TxOutput output : mTransaction.getOutputs()) {
                start = 0;
                SpannableString val = new SpannableString(mCoreAPI.formatSatoshi(output.getmValue()));
                SpannableString address = new SpannableString(output.getAddress());
                end = address.length();
                final String url = finalBaseUrl + "addresses/" + output.getAddress();
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
            s.append(getString(R.string.transaction_details_advanced_txid)).setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n");

            start = s.length();
            s.append(mTransaction.getmMalleableID());
            end = s.length();
            ClickableSpan url = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(finalBaseUrl + "transactions/" + mTransaction.getmMalleableID()));
                    mActivity.startActivity(i);
                }
            };
            s.setSpan(url, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n\n");

            //Total Sent - formatSatoshi
            start = s.length();
            s.append(getString(R.string.transaction_details_advanced_total_sent)).setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n");

            s.append(mCoreAPI.formatSatoshi(netSum))
                    .setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(Typeface.NORMAL), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n\n");


            //Source - inAddresses
            start = s.length();
            s.append(getString(R.string.transaction_details_advanced_source)).setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n");
            s.append(inAddresses);
            s.append("\n\n");

            //Destination - outAddresses
            start = s.length();
            s.append(getString(R.string.transaction_details_advanced_destination)).setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n");
            s.append(outAddresses);
            s.append("\n\n");


            //Miner Fee - formatSatoshi
            start = s.length();
            s.append(getString(R.string.transaction_details_advanced_miner_fee)).setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.append("\n");

            start = s.length();
            s.append(mCoreAPI.formatSatoshi(feesSatoshi, true))
                    .setSpan(new ForegroundColorSpan(Color.BLACK), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(Typeface.NORMAL), start, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mActivity.pushFragment(new HelpFragment(s), NavigationActivity.Tabs.WALLET.ordinal());
        } else {
            mDummyFocus.requestFocus();
        }
    }

    private void setCategoryText(String textWithCategory) {
        setCurrentType(textWithCategory);
        String strippedTerm = "";
        if(textWithCategory.length() >= currentType.length()) {
            strippedTerm = textWithCategory.substring(currentType.length());
        }
        doEdit = true;
        mCategoryEdittext.setText(strippedTerm);
        doEdit = false;
    }

    private void UpdateView(Transaction transaction) {
        doEdit = true;
        String dateString = new SimpleDateFormat("MMM dd yyyy, kk:mm aa").format(transaction.getDate() * 1000);
        mDateTextView.setText(dateString);

        String pretext = mFromSend ? mActivity.getResources().getString(R.string.transaction_details_from) :
                mActivity.getResources().getString(R.string.transaction_details_to);
        mToFromName.setText(pretext + transaction.getWalletName());

        mPayeeEditText.setText(transaction.getName());
        updatePhoto();
        mNoteEdittext.setText(transaction.getNotes());
        setCategoryText(transaction.getCategory());

        long coinValue = 0;
        String feeFormatted;
        if (transaction.getAmountSatoshi() < 0) {
            coinValue = transaction.getAmountSatoshi() + transaction.getMinerFees() + transaction.getABFees();
            feeFormatted = "+" + mCoreAPI.formatSatoshi(transaction.getMinerFees() + transaction.getABFees(), false) + getString(R.string.transaction_details_advanced_fee);
        } else {
            coinValue = transaction.getAmountSatoshi();
            feeFormatted = "";
        }

        mBitcoinValueTextview.setText(mCoreAPI.formatSatoshi(coinValue, false));

        String currencyValue = null;
        // If no value set, then calculate it
        if (transaction.getAmountFiat() == 0.0) {
            currencyValue = mCoreAPI.FormatCurrency(coinValue, mWallet.getCurrencyNum(),
                    false, false);
        } else {
            currencyValue = mCoreAPI.formatCurrency(transaction.getAmountFiat(),
                    mWallet.getCurrencyNum(), false);
        }
        mFiatValue = currencyValue;
        mFiatValueEdittext.setText(currencyValue);
        mFiatDenominationLabel.setText((mCoreAPI.getCurrencyAcronyms())[mCoreAPI.CurrencyIndex(mWallet.getCurrencyNum())]);

        mBitcoinSignTextview.setText(mCoreAPI.getDefaultBTCDenomination());

        mBTCFeeTextView.setText(feeFormatted);
        mSearchListView.setVisibility(View.GONE);
        doEdit = false;
    }

    private void addMatchesForPrefix(String strPrefix, String strMatch)
    {
        List<String> cumulativeStrings = new ArrayList<String>();

        for (Category category : mOriginalCategories) {
            String s = category.getCategoryName();

            if (s.toLowerCase().contains(strPrefix.toLowerCase()) &&
                    s.substring(strPrefix.length()).toLowerCase().contains(strMatch.toLowerCase())) {
                if (!cumulativeStrings.contains(s)) {
//                    Log.d(TAG, "Adding "+s+" for prefix, match = "+strPrefix+","+strMatch);
                    cumulativeStrings.add(s);
                    mCategories.add(category);
                }
            }
        }
    }

    private void createNewCategoryChoices(String match)
    {
        mCategories.clear();

        List<String> orderedCategories = new ArrayList<>();

        // Order the categories list, currentType first
        orderedCategories.add(currentType);
        for(String type : getResources().getStringArray(R.array.transaction_categories_list)) {
            if(!type.contains(currentType)) {
                orderedCategories.add(type);
            }
        }

        for (String type : orderedCategories) {
//            Log.d(TAG, "Searching for "+type);
            addMatchesForPrefix(type, match);
        }
        mCategoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnCurrentLocationChange(Location location) {
        mLocationManager.removeLocationChangeListener(this);
        mNearBusinessSearchAsyncTask = new NearBusinessSearchAsyncTask();
        mNearBusinessSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLocationManager.getLocation().getLatitude() + "," + mLocationManager.getLocation().getLongitude());
    }

    @Override
    public void onNewCategory(String categoryName) {
        if (!categoryName.substring(categoryName.indexOf(':') + 1).trim().isEmpty()) {
            mCategories.add(new Category(categoryName, ""));
            mCoreAPI.addCategory(categoryName);
        }

        setCategoryText(categoryName);
        mDummyFocus.requestFocus();
        showCategoryPopup(false);
    }

    class NearBusinessSearchAsyncTask extends AsyncTask<String, Integer, String> {
        private AirbitzAPI api = AirbitzAPI.getApi();

        public NearBusinessSearchAsyncTask() {
        }

        @Override
        protected String doInBackground(String... strings) {
            return api.getSearchByRadius("16093", "", strings[0], "", "1");
        }

        @Override
        protected void onPostExecute(String searchResult) {
            if (mActivity == null) {
                return;
            }
            try {
                mArrayNearBusinesses.clear();
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                for (BusinessSearchResult business : results.getBusinessSearchObjectArray()) {
                    if (!business.getName().isEmpty()) {
                        mArrayNearBusinesses.add(business);

                        // create the address
                        // create the address
                        String strAddress = "";
                        if (business.getAddress() != null) {
                            strAddress += business.getAddress();
                        }
                        if (business.getCity() != null) {
                            strAddress += (strAddress.length() > 0 ? ", " : "") + business.getCity();
                        }
                        if (business.getState() != null) {
                            strAddress += (strAddress.length() > 0 ? ", " : "") + business.getState();
                        }
                        if (business.getPostalCode() != null) {
                            strAddress += (strAddress.length() > 0 ? ", " : "") + business.getPostalCode();
                        }
                        if (strAddress.length() > 0) {
                            mArrayAddresses.put(business.getName(), strAddress);
                        }

                        // set the biz id if available
                        long numBizId = Long.valueOf(business.getId());
                        if (numBizId != 0) {
                            mBizIds.put(business.getName(), numBizId);
                        }

                        // check if we can get a thumbnail
                        ProfileImage pImage = business.getSquareProfileImage();
                        if (pImage != null) {
                            String thumbnail = pImage.getImageThumbnail();
                            if (thumbnail != null) {
                                Uri uri = Uri.parse(thumbnail);
                                mCombinedPhotos.put(business.getName(), uri);
//                                Log.d(TAG, "Adding " + business.getName() + " thumbnail");
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                this.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }
            combineMatchLists();
            updateAutoCompleteArray(mPayeeEditText.getText().toString());
            updateBizId();
            updatePhoto();
            mSearchAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            mNearBusinessSearchAsyncTask = null;
            super.onCancelled();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNearBusinessSearchAsyncTask != null) {
            mNearBusinessSearchAsyncTask.cancel(true);
        }

        String category = mCategorySpinner.getSelectedItem().toString() + ":" + mCategoryEdittext.getText().toString();
        SaveTransactionAsyncTask task = new SaveTransactionAsyncTask(mTransaction, mBizId, mPayeeEditText.getText().toString(), category, mNoteEdittext.getText().toString(),
                mFiatValueEdittext.getText().toString());
        task.execute();

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mCategoryAdapter.setOnNewCategoryListener(null);
    }

    private void startOnlineBusinessSearch(String term) {
        if(!mArrayAutoCompleteQueries.contains(term)) {
            mArrayAutoCompleteQueries.add(term);
            mOnlineBusinessSearchAsyncTask = new OnlineBusinessSearchAsyncTask();
            mOnlineBusinessSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, term);
        }
    }

    class SaveTransactionAsyncTask extends AsyncTask<Void, Void, tABC_CC> {
        Transaction transaction;
        long Bizid;
        String Payee, Category, Note, Fiat;

        public SaveTransactionAsyncTask(Transaction tx, long bizId, String payee,
             String category, String note, String fiat) {
            transaction = tx;
            Bizid = bizId;
            Payee = payee;
            Category = category;
            Note = note;
            Fiat = fiat;
        }

        @Override
        protected tABC_CC doInBackground(Void... voids) {
            transaction.setName(Payee);
            transaction.setCategory(Category);
            transaction.setNotes(Note);
            double amountFiat;
            try {
                amountFiat = Double.valueOf(Fiat);
            } catch (Exception e) {
                amountFiat = 0.0;
            }
            transaction.setAmountFiat(amountFiat);
            transaction.setmBizId(Bizid);
            return mCoreAPI.storeTransaction(mTransaction);
        }

        @Override
        protected void onPostExecute(tABC_CC result) {
            if (result!=tABC_CC.ABC_CC_Ok)
            {
                mActivity.ShowFadingDialog(getString(R.string.transaction_details_transaction_save_failed));
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    class OnlineBusinessSearchAsyncTask extends AsyncTask<String, Integer, List<Business>> {
        private AirbitzAPI api = AirbitzAPI.getApi();

        public OnlineBusinessSearchAsyncTask() {
        }

        @Override
        protected List<Business> doInBackground(String... strings) {
            return api.getHttpAutoCompleteBusiness(strings[0], "", "");
        }

        @Override
        protected void onPostExecute(List<Business> businesses) {
            if (mActivity == null || businesses == null) {
                return;
            }
            for (Business business : businesses) {
                BusinessSearchResult bsresult = new BusinessSearchResult(business.getId(), business.getName());
                if (!mArrayOnlineBusinesses.contains(bsresult)) {
                    mArrayOnlineBusinesses.add(bsresult);
                }
                if (!mBizIds.containsKey(bsresult.getName()) && !bsresult.getId().isEmpty()) {
                    mBizIds.put(bsresult.getName(), Long.valueOf(bsresult.getId()));
                }

                if (business.getSquareImageLink() != null) {
                    Uri uri = Uri.parse(business.getSquareImageLink());
                    mCombinedPhotos.put(business.getName(), uri);
                }
            }
            combineMatchLists();

            updateAutoCompleteArray(mPayeeEditText.getText().toString());
            updateBizId();
            updatePhoto();
            mSearchAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            mOnlineBusinessSearchAsyncTask = null;
            super.onCancelled();
        }
    }

    public void getContactsList() {
        mContactNames.clear();
        ContentResolver cr = mActivity.getContentResolver();
        String columns[] = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ;
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, columns, null, null, null);
        if (cursor!=null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                mContactNames.add(name);
            }
            cursor.close();
        }
    }

    public List<BusinessSearchResult> getMatchedNearBusinessList(String searchTerm) {
        mBusinesses.clear();
        for (int i = 0; i < mArrayNearBusinesses.size(); i++) {
            if (mArrayNearBusinesses.get(i).getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                int j = 0;
                boolean flag = false;
                while (!flag && j != mBusinesses.size()) {
                    if (mBusinesses.get(j).getName().toLowerCase().compareTo(mArrayNearBusinesses.get(i).getName().toLowerCase()) > 0) {
                        mBusinesses.add(j, mArrayNearBusinesses.get(i));
                        flag = true;
                    }
                    j++;
                }
                if (j == mBusinesses.size() && !flag) {
                    mBusinesses.add(mArrayNearBusinesses.get(i));
                }
            }
        }
        return mBusinesses;
    }

    public void combineMatchLists() {
        while (!mBusinesses.isEmpty() | !mContactNames.isEmpty()) {
            if (mBusinesses.isEmpty()) {
                mArrayAutoComplete.add(mContactNames.get(0));
                mContactNames.remove(0);
            } else if (mContactNames.isEmpty()) {
                mArrayAutoComplete.add(mBusinesses.get(0));
                mBusinesses.remove(0);
            } else if (mBusinesses.get(0).getName().toLowerCase().compareTo(mContactNames.get(0).toLowerCase()) < 0) {
                mArrayAutoComplete.add(mBusinesses.get(0));
                mBusinesses.remove(0);
            } else {
                mArrayAutoComplete.add(mContactNames.get(0));
                mContactNames.remove(0);
            }
        }
    }

    public void goSearch() {
        mArrayAutoComplete.clear();
        mArrayNearBusinesses.clear();
        mBusinesses.clear();
        if (locationEnabled) {
            if (mLocationManager.getLocation() != null) {
                mNearBusinessSearchAsyncTask = new NearBusinessSearchAsyncTask();
                mNearBusinessSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLocationManager.getLocation().getLatitude() + "," + mLocationManager.getLocation().getLongitude());
            } else {
                mLocationManager.addLocationChangeListener(this);
            }
        }
    }

    private void FindBizIdThumbnail(String name, long id) {
        if (id != 0) {
            Log.d(TAG, "Finding bizid thumbnail for "+name);
            GetBizIdThumbnailAsyncTask task = new GetBizIdThumbnailAsyncTask(name, id);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class GetBizIdThumbnailAsyncTask extends AsyncTask<Void, Void, BusinessDetail> {
        private AirbitzAPI api = AirbitzAPI.getApi();
        private String mName;
        private long mBizId;

        GetBizIdThumbnailAsyncTask(String name, long id) {
            mName = name;
            mBizId = id;
        }

        @Override
        protected BusinessDetail doInBackground(Void... voids) {
            return api.getHttpBusiness((int) mBizId);
        }

        @Override
        protected void onPostExecute(BusinessDetail business) {
            if (mActivity == null) {
                return;
            }
            if (business != null && business.getSquareImageLink() != null) {
                Uri uri = Uri.parse(business.getSquareImageLink());
                Log.d(TAG, "Got " + uri);
                mCombinedPhotos.put(mName, uri);
                updatePhoto();
                updateBizId();
                mSearchAdapter.notifyDataSetChanged();
                mBizDistance = business.getDistance();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    class CheckReminderNotification extends AsyncTask<Void, Void, Boolean> {
        private Wallet mWallet;

        CheckReminderNotification(Wallet wallet) {
            mWallet = wallet;
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return mCoreAPI.needsRecoveryReminder(mWallet);
        }

        @Override
        protected void onPostExecute(Boolean needsReminder) {
            if (mActivity == null) {
                return;
            }
            mReminderTask = null;
            if (mFromRequest && needsReminder && !mHasReminded) {
                mHasReminded = true;
                mCoreAPI.incRecoveryReminder();
                ShowReminderDialog(getString(R.string.transaction_details_recovery_reminder_title),
                        getString(R.string.transaction_details_recovery_reminder_message));
            } else {
                done();
            }
            finish();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            done();
            finish();
        }

        private void finish() {
            mActivity.showModalProgress(false);
            mReminderTask = null;
        }
    }
}
