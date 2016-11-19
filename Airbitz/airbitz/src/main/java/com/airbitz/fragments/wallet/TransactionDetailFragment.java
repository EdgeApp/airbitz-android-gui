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

import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.Transaction;
import co.airbitz.core.TxOutput;
import co.airbitz.core.Utils;
import co.airbitz.core.Wallet;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.CategoryAdapter;
import com.airbitz.adapters.TransactionDetailCategoryAdapter;
import com.airbitz.adapters.TransactionDetailSearchAdapter;
import com.airbitz.api.Constants;
import com.airbitz.api.CoreWrapper;
import com.airbitz.api.DirectoryWrapper;
import com.airbitz.api.directory.Business;
import com.airbitz.api.directory.BusinessDetail;
import com.airbitz.api.directory.BusinessSearchResult;
import com.airbitz.api.directory.Category;
import com.airbitz.api.directory.DirectoryApi;
import com.airbitz.api.directory.ProfileImage;
import com.airbitz.api.directory.SearchResult;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.directory.DirectoryDetailFragment;
import com.airbitz.fragments.send.SendFragment;
import com.airbitz.fragments.send.SuccessFragment;
import com.airbitz.fragments.settings.SettingFragment;
import com.airbitz.objects.Calculator;
import com.airbitz.objects.CurrentLocationManager;
import com.airbitz.utils.Common;
import com.airbitz.utils.Common;
import com.airbitz.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionDetailFragment extends WalletBaseFragment
        implements CurrentLocationManager.OnCurrentLocationChange,
        NavigationActivity.OnBackPress,
        TransactionDetailCategoryAdapter.OnNewCategory,
        Calculator.OnCalculatorKey {
    private final String TAG = getClass().getSimpleName();
    private final int MIN_AUTOCOMPLETE = 5;

    private Button mDoneButton;
    private Button mAdvancedButton;
    private Spinner mCategorySpinner;
    private TextView mDateTextView;
    private RelativeLayout mPayeeNameLayout;
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
    private View mUpperLayout, mMiddleLayout;
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
    private Transaction mTransaction;
    private NearBusinessSearchAsyncTask mNearBusinessSearchAsyncTask = null;
    private OnlineBusinessSearchAsyncTask mOnlineBusinessSearchAsyncTask = null;
    private CheckReminderNotification mReminderTask = null;
    private SaveTransactionAsyncTask mSaveTask;

    private Category baseIncomeCat, baseExpenseCat, baseTransferCat, baseExchangeCat;
    private int[] mCategoryBackgrounds = {R.drawable.bg_button_red, R.drawable.bg_button_green,
            R.drawable.bg_button_blue, R.drawable.bg_button_orange};

    private Picasso mPicasso;

    private View mView;
    private NavigationActivity mActivity;
    private Dialog mMessageDialog;
    private String mWalletUUID;
    private String mTxId;

    public TransactionDetailFragment() {
        mAllowArchived = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (NavigationActivity) getActivity();
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setDropdownEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public String getSubtitle() {
        return mActivity.getString(R.string.transaction_details_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_transaction_detail, container, false);
        }

        mView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view ) {
                showUpperLayout(true);
                showMiddleLayout(true);
            }
        });

        mPicasso = Picasso.with(getActivity());
        mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        locationEnabled = CurrentLocationManager.locationEnabled(mActivity);
        Common.disabledNotification(mActivity, android.R.id.content);

        mCalculator = (Calculator) mActivity.findViewById(R.id.navigation_calculator_layout);
        mCalculator.setCalculatorKeyListener(this);
        mCalculator.setEditText(mFiatValueEdittext);

        mDoneButton = (Button) mView.findViewById(R.id.transaction_detail_button_done);
        mAdvancedButton = (Button) mView.findViewById(R.id.transaction_detail_button_advanced);

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

        mSearchListView = (ListView) mView.findViewById(R.id.listview_search);
        mBusinesses = new ArrayList<BusinessSearchResult>();
        mArrayNearBusinesses = new ArrayList<BusinessSearchResult>();
        mContactNames = new ArrayList<String>();
        mArrayAutoCompleteQueries = new ArrayList<String>();
        mArrayAutoComplete = new ArrayList<Object>();
        mArrayOnlineBusinesses = new ArrayList<BusinessSearchResult>();
        mArrayAddresses = new ConcurrentHashMap<String, String>();
        mCombinedPhotos = new LinkedHashMap<String, Uri>();
        mSearchAdapter = new TransactionDetailSearchAdapter(getActivity(), mBusinesses, mArrayAutoComplete);
        mSearchListView.setAdapter(mSearchAdapter);

        goSearch();

        mCategoryListView = (ListView) mView.findViewById(R.id.listview_category);
        mCategories = new ArrayList<Category>();
        mOriginalCategories = new ArrayList<Category>();

        mDateTextView.setTypeface(NavigationActivity.latoRegularTypeFace);

        mFiatValueEdittext.setTypeface(NavigationActivity.latoRegularTypeFace);
        mBitcoinValueTextview.setTypeface(NavigationActivity.latoRegularTypeFace, Typeface.NORMAL);

        mDoneButton.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.NORMAL);
        mAdvancedButton.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.NORMAL);

        mCategorySpinner = (Spinner) mView.findViewById(R.id.transaction_detail_button_category);
        CategoryAdapter spinnerAdapter = new CategoryAdapter(mActivity, Arrays.asList(getResources().getStringArray(R.array.transaction_categories_list)));
        mCategorySpinner.setAdapter(spinnerAdapter);
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
                    DirectoryDetailFragment.pushFragment(mActivity,
                        String.valueOf(mBizId), mPayeeEditText.getText().toString(), mBizDistance);
                }
            }
        });

        mPayeeEditText = (EditText) mView.findViewById(R.id.transaction_detail_edittext_name);
        mPayeeEditText.setTypeface(NavigationActivity.latoRegularTypeFace);
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
        mNoteEdittext.setTypeface(NavigationActivity.latoRegularTypeFace);
        mNoteEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showUpperLayout(!hasFocus);
                showMiddleLayout(!hasFocus);
                if (!hasFocus) {
                    mActivity.hideSoftKeyboard(mView);
                }
            }
        });
        mNoteEdittext.setHorizontallyScrolling(false);
        mNoteEdittext.setMaxLines(Integer.MAX_VALUE);

        mCategoryEdittext = (EditText) mView.findViewById(R.id.transaction_detail_edittext_category);
        mCategoryEdittext.setTypeface(NavigationActivity.latoRegularTypeFace);
        mCategoryEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mCategoryEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showCategoryPopup(hasFocus);
                if (hasFocus) {
                    if (!mCategoryEdittext.getText().toString().isEmpty()) {
                        mCategoryEdittext.setSelection(0, mCategoryEdittext.length());
                    }
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
                    showCategoryPopup(false);
                    showUpperLayout(true);
                    showMiddleLayout(true);
                    mActivity.hideSoftKeyboard(mView);
                    mCategoryEdittext.clearFocus();
                    return true;
                }
                return false;
            }
        });

        mCategoryEdittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mActivity.hideSoftKeyboard(mView);
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
                    AirbitzCore.logi("editable=" + editable.toString());
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
                    mActivity.hideSoftKeyboard(mView);
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
                mActivity.hideSoftKeyboard(mView);
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
                if (hasFocus) {
                    mFiatValue = mFiatValueEdittext.getText().toString(); // global save
                    mCalculator.setEditText(mFiatValueEdittext);
                    mFiatValueEdittext.selectAll();
                    showCalculator();
                } else {
                    hideCalculator();
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
                    mActivity.hideSoftKeyboard(mView);
                    return true;
                }
                return false;
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDone();
            }
        });
        mAdvancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAdvancedDetails(true);
            }
        });

        if (mFromSend || mFromRequest) {
            mPayeeEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_standard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_help:
            mActivity.pushFragment(new HelpFragment(HelpFragment.TRANSACTION_DETAILS));
            return true;
        case android.R.id.home:
            return onBackPress();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackPress() {
        saveTransaction();
        mActivity.popFragment();
        return true;
    }

    private void setupOriginalCategories() {
        // Initialize category items
        mCategories.clear();
        mOriginalCategories.clear();
        List<String> originalStrings = new ArrayList<>();
        List<String> catStrings = mAccount.categories().list();
        for (String cat : catStrings) {
            if(!originalStrings.contains(cat)) {
                originalStrings.add(cat);
            }
        }
        Collections.sort(originalStrings);
        for (String cat : originalStrings) {
            mOriginalCategories.add(new Category(cat, ""));
        }
        mCategories.addAll(mOriginalCategories);
        mCategoryAdapter = new TransactionDetailCategoryAdapter(getActivity(), mCategories);
        mCategoryListView.setAdapter(mCategoryAdapter);
    }

    @Override
    public void OnCalculatorKeyPressed(String tag) {
        if (tag.equals("done")) {
            hideCalculator();
        }
    }

    private void showCalculator() {
        mCalculator.showCalculator();
        showUpperLayout(false);
    }

    private void hideCalculator() {
        mCalculator.hideCalculator();
        showUpperLayout(true);

        try {
            String fiatString = mFiatValueEdittext.getText().toString();
            double fiatAmount = Double.parseDouble(fiatString);
            if (mTransaction.amount() < 0 && fiatAmount > 0) {
                mFiatValueEdittext.setText("-" + fiatString);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
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
            if (bundle.getString(Constants.WALLET_FROM) != null && bundle.getString(Constants.WALLET_FROM).equals(SuccessFragment.TYPE_SEND)) {
                AirbitzCore.logi("SEND");
                mFromSend = true;
                setCurrentType(Constants.EXPENSE);
            } else if (bundle.getString(Constants.WALLET_FROM) != null && bundle.getString(Constants.WALLET_FROM).equals(SuccessFragment.TYPE_REQUEST)) {
                mFromRequest = true;
                AirbitzCore.logi("REQUEST");
                setCurrentType(Constants.INCOME);
            }

            mWalletUUID = bundle.getString(Constants.WALLET_UUID);
            mTxId = bundle.getString(Constants.WALLET_TXID);
            if (mWalletUUID.isEmpty()) {
                AirbitzCore.logi("no detail info");
            } else if (mWallet == null || mTransaction == null) {
                mWallet = mAccount.wallet(mWalletUUID);
                mTransaction = mWallet.transaction(mTxId);

                if (mTransaction != null) {

                    if (mWallet.isArchived()) {
                        // Disable editing
                        mPayeeEditText.setEnabled(false);
                        mFiatValueEdittext.setEnabled(false);
                        mCategoryEdittext.setEnabled(false);
                        mNoteEdittext.setEnabled(false);
                        mCategorySpinner.setEnabled(false);
                    }

                    if ((mFromSend || mFromRequest) && TextUtils.isEmpty(mTransaction.meta().category())) {
                        mTransaction.meta().category(
                            Constants.CATEGORIES[mCategorySpinner.getSelectedItemPosition()]);
                    } else {
                        setCurrentType(mTransaction.meta().category());
                    }
                    // if there is a bizId, add it as the first one of the map
                    if (mTransaction.meta().bizid() != 0) {
                        mBizIds.put(mTransaction.meta().name(), mTransaction.meta().bizid());
                        mBizId = mTransaction.meta().bizid();
                    }
                    UpdateView(mTransaction);
                }
            }
        }

        if(mContactNames == null || mContactNames.isEmpty()) {
            AirbitzCore.logi("Getting Contact List");
            getContactsList();
        }

        if(mTransaction != null) {
            AirbitzCore.logi("Updating view");
            FindBizIdThumbnail(mTransaction.meta().name(), mTransaction.meta().bizid());
        }

        if(mOriginalCategories == null || mOriginalCategories.isEmpty() || mCategoryAdapter == null) {
            AirbitzCore.logi("Getting original categories");
            setupOriginalCategories();
        }

        mCategoryAdapter.setOnNewCategoryListener(this);
        AirbitzCore.logi("OnResume finished");
    }

    @Override
    protected void setDefaultWallet() {
        Bundle bundle = getArguments();
        String uuid = bundle.getString(Constants.WALLET_UUID);
        setDefaultWallet(uuid);
        if (mWallet == null && uuid != null) {
            mWallet = mAccount.wallet(uuid);
        }
        mLoading = false;
    }

    private void goDone() {
        saveTransaction();

        mReminderTask = new CheckReminderNotification(mWallet);
        mReminderTask.execute();

        String returnUrl = getArguments().getString(SendFragment.RETURN_URL);
        if(returnUrl != null && (returnUrl.startsWith("https://") || returnUrl.startsWith("http://"))) {
            final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(returnUrl));
            mActivity.startActivity(intent);
        }
        else {
            AirbitzCore.logi("Return URL does not begin with http or https");
        }
    }

    private void done() {
        String category = Common.formatCategory(
                            Constants.CATEGORIES[mCategorySpinner.getSelectedItemPosition()],
                            mCategoryEdittext.getText().toString());
        mAccount.categories().insert(category);
        mActivity.onBackPressed();
    }

    private void updateCategoryBackground(int position) {
        int newBackground = mCategoryBackgrounds[position];
        if (mCategorySpinner != null) {
            mCategorySpinner.setBackgroundResource(newBackground);
        }
        currentType = Constants.CATEGORIES[position];
        createNewCategoryChoices(mCategoryEdittext.getText().toString());
    }

    public void ShowReminderDialog(String title, String message) {
        if (mMessageDialog != null) {
            mMessageDialog.dismiss();
        }
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
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
        mMessageDialog = builder.show();
    }

    private void setCurrentType(String input) {
        int selected = Common.stringToPrefixCategoryIndex(input);
        currentType = Common.stringToPrefixCategory(input);
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
            int round = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mActivity.getResources().getDisplayMetrics());
            int dimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, mActivity.getResources().getDisplayMetrics());
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
                AirbitzCore.logi("loading remote " + payeeImage.toString());
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
            mToFromName.setVisibility(View.GONE);

            updateAutoCompleteArray(mPayeeEditText.getText().toString());
            updateBizId();
            updatePhoto();
            mSearchAdapter.notifyDataSetChanged();
        } else {
            mSearchListView.setVisibility(View.GONE);
            mToFromName.setVisibility(View.VISIBLE);
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
        AirbitzCore.logi("Biz ID: " + String.valueOf(mBizId));
    }


    private void showAdvancedDetails(boolean hasFocus) {
        Transaction tx = mWallet.transaction(mTxId);
        if (hasFocus && tx != null) {
            String inAddresses = "";
            String outAddresses = "";

            String finalBaseUrl;
            if (AirbitzCore.getApi().isTestNet()) {
                finalBaseUrl = "https://testnet.blockexplorer.com";
            } else { // LIVE
                finalBaseUrl = "https://blockexplorer.com";
            }

            long inSum = 0;

            String txid = String.format("<div class=\"wrapped\"><a href=\"%s/tx/%s\">%s</a></div>", finalBaseUrl, tx.id(), tx.id());

            if (null != tx.outputs()) {
                for (TxOutput input : tx.inputs()) {
                    inAddresses += String.format("<div class=\"wrapped\"><a href=\"%s/address/%s\">%s</a></div><div>%s</div>",
                            finalBaseUrl, input.address(), input.address(), Utils.formatSatoshi(mAccount, input.amount()));
                    inSum += input.amount();
                }
                for (TxOutput output : tx.outputs()) {
                    outAddresses += String.format("<div class=\"wrapped\"><a href=\"%s/address/%s\">%s</a></div><div>%s</div>",
                            finalBaseUrl, output.address(), output.address(), Utils.formatSatoshi(mAccount, output.amount()));
                }
            } else {
                inAddresses += getString(R.string.transaction_details_outputs_unavailable);
                inAddresses += ("<br>\n");
            }

            long feesSatoshi = tx.providerFees() + tx.minerFees();
            long netSum = inSum - feesSatoshi;
            long confirmations;

            if (tx.height() <= 0) {
                confirmations = 0;
            } else if (mWallet.blockHeight() == 0) {
                confirmations = 0;
            } else {
                confirmations = mWallet.blockHeight() - tx.height() + 1;
            }

            String conf = "" + confirmations;

            String content = Common.evaluateTextFile(getActivity(), R.raw.transaction_details);

            List<String> searchList = new ArrayList<String>();
            searchList.add("[[abtag TXID]]");
            searchList.add("[[abtag BTCTOTAL]]");
            searchList.add("[[abtag INPUT_ADDRESSES]]");
            searchList.add("[[abtag OUTPUT_ADDRESSES]]");
            searchList.add("[[abtag FEES]]");
            searchList.add("[[abtag CONFIRMATIONS]]");

            List<String> replaceList = new ArrayList<String>();
            replaceList.add(txid);
            replaceList.add(Utils.formatSatoshi(mAccount, netSum));
            replaceList.add(inAddresses);
            replaceList.add(outAddresses);
            replaceList.add(Utils.formatSatoshi(mAccount, feesSatoshi, true));
            replaceList.add(conf);

            for (int i = 0; i < searchList.size(); i++) {
                content = content.replace(searchList.get(i), replaceList.get(i));
            }

            mActivity.pushFragment(new HelpFragment(content), NavigationActivity.Tabs.WALLET.ordinal());

        } else {
            mActivity.hideSoftKeyboard(mView);
        }
    }

    private void setCategoryText(String textWithCategory) {
        setCurrentType(textWithCategory);
        String strippedTerm = Common.extractSuffixCategory(textWithCategory);
        doEdit = true;
        mCategoryEdittext.setText(strippedTerm);
        doEdit = false;
    }

    private void UpdateView(Transaction transaction) {
        doEdit = true;
        mDateTextView.setText(
            DateFormat.getDateFormat(mActivity).format(transaction.date()) + " " +
            DateFormat.getTimeFormat(mActivity).format(transaction.date()));

        String pretext = mFromSend ? mActivity.getResources().getString(R.string.transaction_details_from) :
                mActivity.getResources().getString(R.string.transaction_details_to);
        mToFromName.setText(pretext + mWallet.name());

        mPayeeEditText.setText(transaction.meta().name());
        updatePhoto();
        mNoteEdittext.setText(transaction.meta().notes());
        setCategoryText(transaction.meta().category());

        long coinValue = 0;
        String feeFormatted;
        if (transaction.amount() < 0) {
            coinValue = transaction.amount() + transaction.minerFees() + transaction.providerFees();
            feeFormatted = "+" + Utils.formatSatoshi(mAccount, transaction.minerFees() + transaction.providerFees(), false) + getString(R.string.transaction_details_advanced_fee);
        } else {
            coinValue = transaction.amount();
            feeFormatted = "";
        }

        mBitcoinValueTextview.setText(Utils.formatSatoshi(mAccount, coinValue, false));

        String currencyValue = null;
        // If no value set, then calculate it
        if (transaction.meta().fiat() == 0.0) {
            currencyValue = CoreWrapper.formatCurrency(mAccount, coinValue, mWallet.currency().code, false);
        } else {
            currencyValue = Utils.formatCurrency(transaction.meta().fiat(),
                    mWallet.currency().code, false);
        }
        mFiatValue = currencyValue;
        mFiatValueEdittext.setText(currencyValue);
        mFiatDenominationLabel.setText(mWallet.currency().code);

        mBitcoinSignTextview.setText(CoreWrapper.defaultBTCDenomination(mAccount));

        mBTCFeeTextView.setText(feeFormatted);
        mSearchListView.setVisibility(View.GONE);
        doEdit = false;
    }

    private void addMatchesForPrefix(String strPrefix, String strMatch) {
        List<String> cumulativeStrings = new ArrayList<String>();
        for (Category category : mOriginalCategories) {
            String s = category.getCategoryName();
            if (s.toLowerCase().startsWith(strPrefix.toLowerCase())
                    && s.toLowerCase().contains(strMatch.toLowerCase())) {
                if (!cumulativeStrings.contains(s)) {
                    cumulativeStrings.add(s);
                    mCategories.add(category);
                }
            }
        }
    }

    private void createNewCategoryChoices(String match) {
        mCategories.clear();
        addMatchesForPrefix(currentType, match);
        if (null != mCategoryAdapter) {
            mCategoryAdapter.notifyDataSetChanged();
        }
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
            mAccount.categories().insert(categoryName);
        }

        setCategoryText(categoryName);
        mActivity.hideSoftKeyboard(mView);
        showCategoryPopup(false);
    }

    class NearBusinessSearchAsyncTask extends AsyncTask<String, Integer, String> {
        private DirectoryApi api = DirectoryWrapper.getApi();

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
//                                AirbitzCore.logi("Adding " + business.getName() + " thumbnail");
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
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
            mSaveTask = null;
        }

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mCategoryAdapter.setOnNewCategoryListener(null);
        hideCalculator();
    }

    private boolean equals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        return s1 != null && s1.equals(s2);
    }

    private void saveTransaction() {
        if (mTransaction == null) {
            return;
        }
        String payee = mPayeeEditText.getText().toString();
        String category = Common.formatCategory(
                                Constants.CATEGORIES[mCategorySpinner.getSelectedItemPosition()],
                                mCategoryEdittext.getText().toString());
        String notes = mNoteEdittext.getText().toString();
        double fiat = 0.0;
        try {
            fiat = Double.valueOf(mFiatValueEdittext.getText().toString());
        } catch (Exception e) {
            fiat = 0.0;
        }
        if (!equals(mTransaction.meta().name(), payee)
                || !equals(mTransaction.meta().category(), category)
                || !equals(mTransaction.meta().notes(), notes)
                || Math.abs(mTransaction.meta().fiat() - fiat) > 0.000001) {
            mSaveTask = new SaveTransactionAsyncTask(
                    mTransaction, mBizId, payee, category, notes, fiat);
            mSaveTask.execute();
        }
    }

    private void startOnlineBusinessSearch(String term) {
        if(!mArrayAutoCompleteQueries.contains(term)) {
            mArrayAutoCompleteQueries.add(term);
            mOnlineBusinessSearchAsyncTask = new OnlineBusinessSearchAsyncTask();
            mOnlineBusinessSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, term);
        }
    }

    class SaveTransactionAsyncTask extends BaseAsyncTask<Void, Void, AirbitzException> {
        Transaction transaction;
        long Bizid;
        String Payee, Category, Note;
        double Fiat;

        public SaveTransactionAsyncTask(Transaction tx, long bizId, String payee,
             String category, String note, double fiat) {
            transaction = tx;
            Bizid = bizId;
            Payee = payee;
            Category = category;
            Note = note;
            Fiat = fiat;
        }

        @Override
        protected AirbitzException doInBackground(Void... voids) {
            transaction.meta().name(Payee);
            transaction.meta().category(Category);
            transaction.meta().notes(Note);
            transaction.meta().fiat(Fiat);
            transaction.meta().bizid(Bizid);
            try {
                mTransaction.save();
            } catch (AirbitzException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(AirbitzException result) {
            if (result != null) {
                mActivity.ShowFadingDialog(getString(R.string.transaction_details_transaction_save_failed));
            }
            onCancelled();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    class OnlineBusinessSearchAsyncTask extends AsyncTask<String, Integer, List<Business>> {
        private DirectoryApi api = DirectoryWrapper.getApi();

        public OnlineBusinessSearchAsyncTask() {
        }

        @Override
        protected List<Business> doInBackground(String... strings) {
            return api.getHttpAutoCompleteBusiness(strings[0], "", "", "");
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
            AirbitzCore.logi("Finding bizid thumbnail for "+name);
            GetBizIdThumbnailAsyncTask task = new GetBizIdThumbnailAsyncTask(name, id);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class GetBizIdThumbnailAsyncTask extends AsyncTask<Void, Void, BusinessDetail> {
        private DirectoryApi api = DirectoryWrapper.getApi();
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
                AirbitzCore.logi("Got " + uri);
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
            return CoreWrapper.needsRecoveryReminder(mAccount, mWallet);
        }

        @Override
        protected void onPostExecute(Boolean needsReminder) {
            if (mActivity == null) {
                return;
            }
            mReminderTask = null;
            if (mFromRequest && needsReminder && !mHasReminded) {
                mHasReminded = true;
                CoreWrapper.incRecoveryReminder(mAccount);
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
