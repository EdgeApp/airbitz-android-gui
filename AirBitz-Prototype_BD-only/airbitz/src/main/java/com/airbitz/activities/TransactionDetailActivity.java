package com.airbitz.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.MoreCategoryAdapter;
import com.airbitz.adapters.NoteCategoryAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Categories;
import com.airbitz.models.Category;
import com.airbitz.objects.RoundedImageView;
import com.airbitz.utils.Common;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created on 2/20/14.
 */
public class TransactionDetailActivity extends Activity implements GestureDetector.OnGestureListener{
    private Button mCheckingButton;
    private Button mDoneButton;
    private Button mSerialButton;

    private TextView mDateTextView;
    private TextView mTitleTextView;
    private TextView mNameTextView;
    private TextView mSentFromTextView;
    private TextView mBitcoinValueTextview;
    private TextView mCategoryTextView;
    private TextView mNoteTextView;

    private Spinner mCategorySpinner;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;
    private LinearLayout mPaddingLayout;

    private ScrollView mScrollView;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);

        mPaddingLayout = (LinearLayout) findViewById(R.id.bottom_padding);

        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        mCheckingButton = (Button) findViewById(R.id.button_checking);
        mDoneButton = (Button) findViewById(R.id.button_done);
        mSerialButton = (Button) findViewById(R.id.button_serial);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mNameTextView = (TextView) findViewById(R.id.textview_name);
        mSentFromTextView = (TextView) findViewById(R.id.textview_sent_from);
        mCategoryTextView = (TextView) findViewById(R.id.textview_category);
        mNoteTextView = (TextView) findViewById(R.id.textview_notes);
        mBitcoinValueTextview = (TextView) findViewById(R.id.textview_bitcoin_value);
        mDateTextView = (TextView) findViewById(R.id.textview_date);

        mDollarValueEdittext = (EditText) findViewById(R.id.edittext_dollar_value);
        mNoteEdittext = (EditText) findViewById(R.id.edittext_notes);
        mCategoryEdittext = (EditText) findViewById(R.id.edittext_category);

        RoundedImageView iconImage = (RoundedImageView )findViewById(R.id.imageview_photo);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mCategorySpinner = (Spinner) findViewById(R.id.spinner_categories);

        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#eef2f7"),Color.parseColor("#a9bfd6")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mDateTextView.getPaint().setShader(textShader);

        iconImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.people));

        mCategoryEdittext.setKeyListener(null);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mDateTextView.setTypeface(LandingActivity.helveticaNeueTypeFace, Typeface.BOLD);
        mNameTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);
        mSerialButton.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);
        mSentFromTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);
        mCheckingButton.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);
        mCategoryEdittext.setTypeface(LandingActivity.latoBlackTypeFace);

        mDollarValueEdittext.setTypeface(LandingActivity.helveticaNeueTypeFace);
        mBitcoinValueTextview.setTypeface(LandingActivity.helveticaNeueTypeFace, Typeface.BOLD);

        mCategoryTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mNoteTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mNoteEdittext.setTypeface(LandingActivity.latoBlackTypeFace);
        mDoneButton.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);

        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
                if (heightDiff > 100) {
                    mNavigationLayout.setVisibility(View.GONE);
                    mPaddingLayout.setVisibility(View.GONE);
                }
                else
                {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                    mPaddingLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mCheckingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(TransactionDetailActivity.this, WalletActivity.class);
                mIntent.putExtra(RequestActivity.CLASSNAME, "RequestActivity");
                startActivity(mIntent);
            }
        });

        mSerialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = android.content.ClipData.newPlainText("address key",((Button) view).getText().toString());
                clipboard.setPrimaryClip(clip);
            }
        });

        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
                if (heightDiff > 100) {
                    mNavigationLayout.setVisibility(View.GONE);
                }
                else
                {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                }
            }
        });


        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mCategoryEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    mCategorySpinner.performClick();
                }
            }
        });


        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(firstLoad){
                    firstLoad = false;
                }
                else{
                    String name = ((MoreCategoryAdapter) adapterView.getAdapter()).getListItemName(position).getCategoryName();
                    mCategoryEdittext.setText(name);
                    mNoteEdittext.requestFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(TransactionDetailActivity.this, "Info", "Business directory info");
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    class BusinessCategoryAsyncTask extends AsyncTask<String, Integer, Categories>{

        private AirbitzAPI api = AirbitzAPI.getApi();
        private ProgressDialog progressDialog;

        @Override
        protected Categories doInBackground(String... strings) {
            Categories jsonParsingResult = null;
            try{
                jsonParsingResult = api.getHttpCategories(strings[0]);
                mNextUrl = jsonParsingResult.getNextLink();
                mCategories = jsonParsingResult;
                getMoreBusinessCategory(mCategories,mNextUrl);
            } catch (Exception e){

            }

            return jsonParsingResult;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(TransactionDetailActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Categories categories) {

            progressDialog.dismiss();
            mCategorySpinner.setVisibility(View.INVISIBLE);

            ArrayList<Category> catArrayList= new ArrayList<Category>();

            if(categories != null){

            for(Category cat: categories.getBusinessCategoryArray()){
                if(!cat.getCategoryLevel().equalsIgnoreCase("1") && !cat.getCategoryLevel().equalsIgnoreCase("2") && !cat.getCategoryLevel().equalsIgnoreCase("3") && !cat.getCategoryLevel().equalsIgnoreCase("null")){
                    catArrayList.add(cat);
                }
            }

            categories.removeBusinessCategoryArray();
            categories.setBusinessCategoryArray(catArrayList);

            mMoreCategoryAdapter = new MoreCategoryAdapter(TransactionDetailActivity.this, mCategories);
            mCategorySpinner.setAdapter(mMoreCategoryAdapter);
            }
        }
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
        new BusinessCategoryAsyncTask().execute("name");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
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
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
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
                    finish();
                    return true;
                }
            }

        }

        return false;
    }

}
