package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.TransactionAdapter;
import com.airbitz.models.AccountTransaction;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.objects.ResizableImageView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class TransactionActivity extends Activity implements GestureDetector.OnGestureListener{

    private ClearableEditText mSearchField;

    private ImageButton mExportButton;
    private ImageButton mHelpButton;
    private ImageButton mBackButton;

    private ResizableImageView mRequestButton;
    private ResizableImageView mSendButton;

    private TextView mTitleTextView;

    private RelativeLayout mParentLayout;

    private ScrollView mScrollView;

    private Button mButtonBitcoinBalance;
    private Button mButtonDollarBalance;
    private Button mCheckingButton;

    private ListView mListTransaction;

    private TransactionAdapter mTransactionAdapter;

    private GestureDetector mGestureDetector;

    private Intent mIntent;

    private String[] mDate = {"DEC 10", "DEC 15", "NOV 1"};
    private String[] mName = {"Matt Kemp", "John Madden", "kelly@gmail.com"};
    private String[] mDebitAmount = {"B25.000", "B30.000", "B95.000"};
    private String[] mCreditAmount = {"-B5.000", "-B65.000", "-B95.000"};

    private List<AccountTransaction> mAccountTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_transaction);

        mAccountTransaction = new ArrayList<AccountTransaction>();
        mAccountTransaction.add(new AccountTransaction("Matt Kemp","DEC 10","B25.000", "-B5.000"));
        mAccountTransaction.add(new AccountTransaction("John Madden","DEC 15","B30.000", "-B65.000"));
        mAccountTransaction.add(new AccountTransaction("kelly@gmail.com","NOV 1","B95.000", "-B95.000"));

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        mTransactionAdapter = new TransactionAdapter(TransactionActivity.this, mAccountTransaction);

        mSearchField = (ClearableEditText) findViewById(R.id.edittext_search);

        mSendButton = (ResizableImageView) findViewById(R.id.button_send);
        mRequestButton = (ResizableImageView) findViewById(R.id.button_request);
        mCheckingButton = (Button) findViewById(R.id.button_balance);

        mExportButton = (ImageButton) findViewById(R.id.button_export);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mButtonBitcoinBalance = (Button) findViewById(R.id.button_bitcoinbalance);
        mButtonDollarBalance = (Button) findViewById(R.id.button_dollarbalance);
        mListTransaction = (ListView) findViewById(R.id.listview_transaction);
        mListTransaction.setAdapter(mTransactionAdapter);

        ListViewUtility.setTransactionListViewHeightBasedOnChildren(mListTransaction, mAccountTransaction.size(), this);


        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mCheckingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(TransactionActivity.this, WalletActivity.class);
                mIntent.putExtra(RequestActivity.CLASSNAME, "TransactionActivity");
                startActivity(mIntent);
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

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(TransactionActivity.this, ExportActivity.class);
                startActivity(mIntent);
            }
        });
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(TransactionActivity.this, RequestActivity.class);
                startActivity(mIntent);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(TransactionActivity.this, SendActivity.class);
                startActivity(mIntent);
            }
        });

        mListTransaction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mIntent = new Intent(TransactionActivity.this, TransactionDetailActivity.class);
                startActivity(mIntent);
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
                Common.showHelpInfo(TransactionActivity.this, "Info", "Business directory info");
            }
        });

    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
