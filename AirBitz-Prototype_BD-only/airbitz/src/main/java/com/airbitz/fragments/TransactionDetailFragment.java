package com.airbitz.fragments;

import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.MoreCategoryAdapter;
import com.airbitz.adapters.NoteCategoryAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Categories;

import org.json.JSONObject;

/**
 * Created on 2/20/14.
 */
public class TransactionDetailFragment extends Fragment implements GestureDetector.OnGestureListener{
//    private Button mCheckingButton;
    private Button mDoneButton;
//    private Button mSerialButton;

    private TextView mDateTextView;
    private TextView mTitleTextView;
    private TextView mNameTextView;
    private TextView mSentFromTextView;
    private TextView mBitcoinValueTextview;
//    private TextView mCategoryTextView;
    private TextView mNoteTextView;

//    private Spinner mCategorySpinner;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

//    private RelativeLayout mParentLayout;
//    private LinearLayout mPaddingLayout;

//    private ScrollView mScrollView;

    private EditText mDollarValueEdittext;
    private EditText mNoteEdittext;
    private EditText mCategoryEdittext;

    private String mNextUrl = "";

    private Categories mCategories = null;

    private MoreCategoryAdapter mMoreCategoryAdapter;

    private AirbitzAPI api = AirbitzAPI.getApi();

    private Boolean firstLoad = true;

    private ClipboardManager clipboard;

    private NoteCategoryAdapter mNoteCategoryAdapter;

    private GestureDetector mGestureDetector;

    private Intent mIntent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);


        mGestureDetector = new GestureDetector(this);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mDoneButton = (Button) view.findViewById(R.id.transaction_detail_button_done);
//        mSerialButton = (Button) view.findViewById(R.id.button_serial);

        mTitleTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_title);
        mNameTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_name);
        mSentFromTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_to);
//        mCategoryTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_category);
        mNoteTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_notes);
        mBitcoinValueTextview = (TextView) view.findViewById(R.id.transaction_detail_textview_bitcoin_value);
        mDateTextView = (TextView) view.findViewById(R.id.transaction_detail_textview_date);

        mDollarValueEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_dollar_value);
        mNoteEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_notes);
        mCategoryEdittext = (EditText) view.findViewById(R.id.transaction_detail_edittext_category);

        mBackButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.transaction_detail_button_help);

//        mCategorySpinner = (Spinner) view.findViewById(R.id.spinner_categories);

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#eef2f7"),Color.parseColor("#a9bfd6")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mDateTextView.getPaint().setShader(textShader);

        mCategoryEdittext.setKeyListener(null);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mDateTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.BOLD);
        mNameTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
//        mSerialButton.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mSentFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
//        mCheckingButton.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mCategoryEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);

        mDollarValueEdittext.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitcoinValueTextview.setTypeface(NavigationActivity.helveticaNeueTypeFace, Typeface.BOLD);

//        mCategoryTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mNoteTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mNoteEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mDoneButton.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);

//        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
//                if (heightDiff > 100) {
////                    mNavigationLayout.setVisibility(View.GONE);
//                    mPaddingLayout.setVisibility(View.GONE);
//                }
//                else
//                {
////                    mNavigationLayout.setVisibility(View.VISIBLE);
//                    mPaddingLayout.setVisibility(View.VISIBLE);
//                }
//            }
//        });

//        mCheckingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mIntent = new Intent(getActivity(), WalletActivity.class);
//                mIntent.putExtra(RequestActivity.CLASSNAME, "RequestActivity");
//                startActivity(mIntent);
//            }
//        });

//        mSerialButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("address key",((Button) view).getText().toString());
//                clipboard.setPrimaryClip(clip);
//            }
//        });

//        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
//                if (heightDiff > 100) {
//                    mNavigationLayout.setVisibility(View.GONE);
//                }
//                else
//                {
//                    mNavigationLayout.setVisibility(View.VISIBLE);
//                }
//            }
//        });


//        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return mGestureDetector.onTouchEvent(motionEvent);
//            }
//        });
//
//        mScrollView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return mGestureDetector.onTouchEvent(motionEvent);
//            }
//        });

//        mCategoryEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if(hasFocus){
//                    mCategorySpinner.performClick();
//                }
//            }
//        });


//        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                if(firstLoad){
//                    firstLoad = false;
//                }
//                else{
//                    String name = ((MoreCategoryAdapter) adapterView.getAdapter()).getListItemName(position).getCategoryName();
//                    mCategoryEdittext.setText(name);
//                    mNoteEdittext.requestFocus();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });


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

//
//    class BusinessCategoryAsyncTask extends AsyncTask<String, Integer, Categories>{
//
//        private AirbitzAPI api = AirbitzAPI.getApi();
//        private ProgressDialog progressDialog;
//
//        @Override
//        protected Categories doInBackground(String... strings) {
//            Categories jsonParsingResult = null;
//            try{
//                jsonParsingResult = api.getHttpCategories(strings[0]);
//                mNextUrl = jsonParsingResult.getNextLink();
//                mCategories = jsonParsingResult;
//                getMoreBusinessCategory(mCategories,mNextUrl);
//            } catch (Exception e){
//
//            }
//
//            return jsonParsingResult;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setIndeterminate(true);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(Categories categories) {
//
//            progressDialog.dismiss();
//            mCategorySpinner.setVisibility(View.INVISIBLE);
//
//            ArrayList<Category> catArrayList= new ArrayList<Category>();
//
//            if(categories != null){
//
//            for(Category cat: categories.getBusinessCategoryArray()){
//                if(!cat.getCategoryLevel().equalsIgnoreCase("1") && !cat.getCategoryLevel().equalsIgnoreCase("2") && !cat.getCategoryLevel().equalsIgnoreCase("3") && !cat.getCategoryLevel().equalsIgnoreCase("null")){
//                    catArrayList.add(cat);
//                }
//            }
//
//            categories.removeBusinessCategoryArray();
//            categories.setBusinessCategoryArray(catArrayList);
//
//            mMoreCategoryAdapter = new MoreCategoryAdapter(getActivity(), mCategories);
//            mCategorySpinner.setAdapter(mMoreCategoryAdapter);
//            }
//        }
//    }

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


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
        if(start != null & finish != null){

            float yDistance = Math.abs(finish.getY() - start.getY());

            if((finish.getRawX()>start.getRawX()) && (yDistance < 15)){
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if(xDistance > 50){
                    getActivity().onBackPressed();
                    return true;
                }
            }

        }

        return false;
    }

}
