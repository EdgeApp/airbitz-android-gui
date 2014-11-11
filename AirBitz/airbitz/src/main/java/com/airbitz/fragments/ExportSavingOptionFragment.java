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

package com.airbitz.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created on 2/22/14.
 */
public class ExportSavingOptionFragment extends Fragment {
    public static final String EXPORT_TYPE = "com.airbitz.fragments.exportsavingoption.export_type";
    private final String TAG = getClass().getSimpleName();
    View mView;
    private HighlightOnPressSpinner mWalletSpinner;
    private HighlightOnPressButton mFromButton;
    private HighlightOnPressButton mToButton;

    private RelativeLayout mDatesLayout;
    private LinearLayout mLastPeriodLayout;
    private LinearLayout mThisPeriodLayout;

    private TextView mTitleTextView;
    private TextView mAccountTextView;
    private TextView mFromTextView;
    private TextView mToTextView;

    private Button mThisWeekButton;
    private Button mLastWeekButton;
    private Button mThisMonthButton;
    private Button mLastMonthButton;
    private Button mTodayButton;
    private Button mYesterdayButton;

    private HighlightOnPressButton mPrintButton;
    private ImageView mPrintImage;
    private HighlightOnPressButton mSDCardButton;
    private ImageView mSDCardImage;
    private HighlightOnPressButton mEmailButton;
    private ImageView mEmailImage;
    private HighlightOnPressButton mGoogleDriveButton;
    private ImageView mGoogleDriveImage;
    private HighlightOnPressButton mDropBoxButton;
    private ImageView mDropBoxImage;
    private HighlightOnPressButton mViewButton;
    private ImageView mViewImage;

    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;

    private EditText mPasswordEditText;

    private Bundle mBundle;

    private List<Button> mTimeButtons;
    private List<Wallet> mWalletList;
    private List<String> mWalletNameList;
    private Wallet mWallet;
    private CoreAPI mCoreApi;
    private Calendar today;
    private Calendar mFromDate;
    private Calendar mToDate;

    private int mExportType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getArguments();
        mExportType = mBundle.getInt(EXPORT_TYPE);

        mCoreApi = CoreAPI.getApi();
        mWalletList = mCoreApi.getCoreWallets(false);
        String uuid = getArguments().getString(RequestFragment.FROM_UUID);
        mWallet = mCoreApi.getWalletFromUUID(uuid);
        mWalletNameList = new ArrayList<String>();
        for (Wallet wallet : mWalletList) {
            mWalletNameList.add(wallet.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_export_saving_options, container, false);
        } else {

            return mView;
        }

        today = Calendar.getInstance();

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_exportsaving_account_spinner);
        mFromButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_from_spinner);
        mToButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_to_spinner);

        mAccountTextView = (TextView) mView.findViewById(R.id.textview_account);
        mDatesLayout = (RelativeLayout) mView.findViewById(R.id.fragment_export_date_entries);
        mLastPeriodLayout = (LinearLayout) mView.findViewById(R.id.layout_export_last_period);
        mThisPeriodLayout = (LinearLayout) mView.findViewById(R.id.layout_export_this_period);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);

        mAccountTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mToTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_export_saving_password_edittext);
        mPasswordEditText.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mPrintButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_print);
        mPrintImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_print);
        mSDCardButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_sd_card);
        mSDCardImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_sd_card);
        mEmailButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_email);
        mEmailImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_email);
        mGoogleDriveButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_google_drive);
        mGoogleDriveImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_google_drive);
        mDropBoxButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_dropbox);
        mDropBoxImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_dropbox);
        mViewButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_view);
        mViewImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_view);

        mThisMonthButton = (Button) mView.findViewById(R.id.button_this_month);
        mThisWeekButton = (Button) mView.findViewById(R.id.button_this_week);
        mTodayButton = (Button) mView.findViewById(R.id.button_today);
        mYesterdayButton = (Button) mView.findViewById(R.id.button_yesterday);
        mLastMonthButton = (Button) mView.findViewById(R.id.button_last_month);
        mLastWeekButton = (Button) mView.findViewById(R.id.button_last_week);

        mTimeButtons = new ArrayList<Button>();
        mTimeButtons.add(mYesterdayButton);
        mTimeButtons.add(mLastWeekButton);
        mTimeButtons.add(mLastMonthButton);
        mTimeButtons.add(mTodayButton);
        mTimeButtons.add(mThisWeekButton);
        mTimeButtons.add(mThisMonthButton);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(getString(R.string.export_title));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_request_wallet_spinner, mWalletNameList);
        dataAdapter.setDropDownViewResource(R.layout.item_request_wallet_spinner_dropdown);
        mWalletSpinner.setAdapter(dataAdapter);
        for (int i = 0; i < mWalletList.size(); i++) {
            if (mWallet.getUUID().equals(mWalletList.get(i).getUUID())) {
                mWalletSpinner.setSelection(i);
            }
        }

        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mWallet = mWalletList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        showExportButtons();

        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        mSDCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Wallet w = mWalletList.get(mWalletSpinner.getSelectedItemPosition());
                String dataOrFile;
                if (mExportType == ExportTypes.PrivateSeed.ordinal()) {
                    if(mCoreApi.PasswordOK(AirbitzApplication.getUsername(), mPasswordEditText.getText().toString())) {
                        dataOrFile = mCoreApi.getPrivateSeed(mWallet);
                    } else {
                        dataOrFile = null;
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                    }
                } else {
                    dataOrFile = getExportData(w, mExportType);
                }
                if(dataOrFile != null) {
                    exportWithEmail(w, dataOrFile);
                }
            }
        });

        mGoogleDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mDropBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBundle.getInt(EXPORT_TYPE) == ExportTypes.PrivateSeed.ordinal()) {
                    if(mCoreApi.PasswordOK(AirbitzApplication.getUsername(), mPasswordEditText.getText().toString())) {
                        ((NavigationActivity) getActivity()).ShowOkMessageDialog(mWallet.getName() + " " + getString(R.string.export_saving_option_private_seed), mCoreApi.getPrivateSeed(mWallet));
                    } else {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                    }
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
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.EXPORT_WALLET_OPTIONS), NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mThisWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today = Calendar.getInstance();
                HighlightTimeButton(4);
                String AMPM = "";
                if (today.get(Calendar.AM_PM) == 1) {
                    AMPM = "pm";
                } else {
                    AMPM = "am";
                }
                String tempMin = "";
                if (today.get(Calendar.MINUTE) < 10) {
                    tempMin = "0" + today.get(Calendar.MINUTE);
                } else {
                    tempMin = "" + today.get(Calendar.MINUTE);
                }
                Calendar beginning = Calendar.getInstance();
                beginning.set(Calendar.DAY_OF_WEEK, 1);

                mFromDate = beginning;
                mToDate = today;

                mFromButton.setText((beginning.get(Calendar.MONTH) + 1) + "/" + beginning.get(Calendar.DAY_OF_MONTH) + "/" + beginning.get(Calendar.YEAR) + " 12:00 am");
                mToButton.setText((today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.DAY_OF_MONTH) + "/" + today.get(Calendar.YEAR) + " " + today.get(Calendar.HOUR) + ":" + tempMin + " " + AMPM);
            }
        });
        mThisMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today = Calendar.getInstance();
                HighlightTimeButton(5);
                String AMPM = "";
                if (today.get(Calendar.AM_PM) == 1) {
                    AMPM = "pm";
                } else {
                    AMPM = "am";
                }
                String tempMin = "";
                if (today.get(Calendar.MINUTE) < 10) {
                    tempMin = "0" + today.get(Calendar.MINUTE);
                } else {
                    tempMin = "" + today.get(Calendar.MINUTE);
                }

                Calendar beginning = Calendar.getInstance();
                beginning.set(Calendar.DAY_OF_MONTH, 1);

                mFromDate = beginning;
                mToDate = today;

                mFromButton.setText((today.get(Calendar.MONTH) + 1) + "/1/" + today.get(Calendar.YEAR) + " 12:00 am");
                mToButton.setText((today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.DAY_OF_MONTH) + "/" + today.get(Calendar.YEAR) + " " + today.get(Calendar.HOUR) + ":" + tempMin + " " + AMPM);
            }
        });
        mTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today = Calendar.getInstance();
                HighlightTimeButton(3);

                String AMPM = "";
                if (today.get(Calendar.AM_PM) == 1) {
                    AMPM = "pm";
                } else {
                    AMPM = "am";
                }
                String tempMin = "";
                if (today.get(Calendar.MINUTE) < 10) {
                    tempMin = "0" + today.get(Calendar.MINUTE);
                } else {
                    tempMin = "" + today.get(Calendar.MINUTE);
                }

                Calendar beginning = Calendar.getInstance();
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                Calendar end = today;
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText((beginning.get(Calendar.MONTH) + 1) + "/" + beginning.get(Calendar.DAY_OF_MONTH) + "/" + beginning.get(Calendar.YEAR) + " 12:00 am");
                mToButton.setText((end.get(Calendar.MONTH) + 1) + "/" + end.get(Calendar.DAY_OF_MONTH) + "/" + end.get(Calendar.YEAR) + " " + today.get(Calendar.HOUR) + ":" + tempMin + " " + AMPM);
            }
        });
        mYesterdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today = Calendar.getInstance();
                HighlightTimeButton(0);
                String AMPM = "";
                if (today.get(Calendar.AM_PM) == 1) {
                    AMPM = "pm";
                } else {
                    AMPM = "am";
                }
                String tempMin = "";
                if (today.get(Calendar.MINUTE) < 10) {
                    tempMin = "0" + today.get(Calendar.MINUTE);
                } else {
                    tempMin = "" + today.get(Calendar.MINUTE);
                }

                Calendar beginning = Calendar.getInstance();
                beginning.add(Calendar.DATE, -1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                Calendar end = Calendar.getInstance();
                end.add(Calendar.DATE, -1);
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText((beginning.get(Calendar.MONTH) + 1) + "/" + beginning.get(Calendar.DAY_OF_MONTH) + "/" + beginning.get(Calendar.YEAR) + " 12:00 am");
                mToButton.setText((end.get(Calendar.MONTH) + 1) + "/" + end.get(Calendar.DAY_OF_MONTH) + "/" + end.get(Calendar.YEAR) + " 11:59 pm");
            }
        });
        mLastMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today = Calendar.getInstance();
                HighlightTimeButton(2);
                String AMPM = "";
                if (today.get(Calendar.AM_PM) == 1) {
                    AMPM = "pm";
                } else {
                    AMPM = "am";
                }
                String tempMin = "";
                if (today.get(Calendar.MINUTE) < 10) {
                    tempMin = "0" + today.get(Calendar.MINUTE);
                } else {
                    tempMin = "" + today.get(Calendar.MINUTE);
                }
                String tempYear = "";
                String tempMonth = "";
                if (today.get(Calendar.MONTH) == 0) {
                    tempYear = "" + (today.get(Calendar.YEAR) - 1);
                    tempMonth = "12";
                } else {
                    tempYear = "" + (today.get(Calendar.YEAR));
                    tempMonth = "" + (today.get(Calendar.MONTH));
                }
                int year = today.get(Calendar.YEAR);
                String tempDay = "";
                if (today.get(Calendar.MONTH) == 2) {
                    if (year % 4 != 0) {
                        tempDay = "28";
                    } else if (year % 100 != 0) {
                        tempDay = "29";
                    } else if (year % 400 != 0) {
                        tempDay = "28";
                    } else {
                        tempDay = "29";
                    }
                } else if (today.get(Calendar.MONTH) == 1 || today.get(Calendar.MONTH) == 3 || today.get(Calendar.MONTH) == 5 || today.get(Calendar.MONTH) == 7 || today.get(Calendar.MONTH) == 8 || today.get(Calendar.MONTH) == 10 || today.get(Calendar.MONTH) == 12) {
                    tempDay = "31";
                } else {
                    tempDay = "30";
                }

                Calendar beginning = Calendar.getInstance();
                beginning.add(Calendar.MONTH, -1);
                beginning.set(Calendar.DAY_OF_MONTH, 1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                Calendar end = Calendar.getInstance();
                end.add(Calendar.MONTH, -1);
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText(tempMonth + "/1/" + tempYear + " 12:00 am");
                mToButton.setText(tempMonth + "/" + tempDay + "/" + tempYear + " 11:59 pm");
            }
        });
        mLastWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today = Calendar.getInstance();
                HighlightTimeButton(1);
                String AMPM = "";
                if (today.get(Calendar.AM_PM) == 1) {
                    AMPM = "pm";
                } else {
                    AMPM = "am";
                }
                String tempMin = "";
                if (today.get(Calendar.MINUTE) < 10) {
                    tempMin = "0" + today.get(Calendar.MINUTE);
                } else {
                    tempMin = "" + today.get(Calendar.MINUTE);
                }

                Calendar beginning = Calendar.getInstance();
                beginning.add(Calendar.WEEK_OF_YEAR, -1);
                beginning.set(Calendar.DAY_OF_WEEK, 1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                Calendar end = Calendar.getInstance();
                end.add(Calendar.WEEK_OF_YEAR, -1);
                end.set(Calendar.DAY_OF_WEEK, 7);
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText((beginning.get(Calendar.MONTH) + 1) + "/" + beginning.get(Calendar.DAY_OF_MONTH) + "/" + beginning.get(Calendar.YEAR) + " 12:00 am");

                mToButton.setText((end.get(Calendar.MONTH) + 1) + "/" + end.get(Calendar.DAY_OF_MONTH) + "/" + end.get(Calendar.YEAR) + " 11:59 pm");
            }
        });

        mToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] dateTime = mToButton.getText().toString().split(" ");
                String[] date = dateTime[0].split("/");
                String[] time = dateTime[1].split(":");
                showSelectorDialog(mToButton, Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]), Integer.valueOf(time[0]), Integer.valueOf(time[1]), dateTime[2]);
            }
        });

        mFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] dateTime = mFromButton.getText().toString().split(" ");
                String[] date = dateTime[0].split("/");
                String[] time = dateTime[1].split(":");
                showSelectorDialog(mFromButton, Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]), Integer.valueOf(time[0]), Integer.valueOf(time[1]), dateTime[2]);
            }
        });

        setupUI(mExportType);
        mTodayButton.performClick();
        return mView;
    }

    private void showExportButtons() {
        int type = mBundle.getInt(EXPORT_TYPE);
        if (type == ExportTypes.CSV.ordinal()) {
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top_archive));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int) getResources().getDimension(R.dimen.nine_mm), 0, (int) getResources().getDimension(R.dimen.three_mm), 0);
            mDropBoxButton.setPadding((int) getResources().getDimension(R.dimen.nine_mm), 0, (int) getResources().getDimension(R.dimen.three_mm), 0);
        } else if (type == ExportTypes.Quicken.ordinal()) {
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top_archive));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int) getResources().getDimension(R.dimen.nine_mm), 0, (int) getResources().getDimension(R.dimen.three_mm), 0);
            mDropBoxButton.setPadding((int) getResources().getDimension(R.dimen.nine_mm), 0, (int) getResources().getDimension(R.dimen.three_mm), 0);
        } else if (type == ExportTypes.Quickbooks.ordinal()) {
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top_archive));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int) getResources().getDimension(R.dimen.nine_mm), 0, (int) getResources().getDimension(R.dimen.three_mm), 0);
            mDropBoxButton.setPadding((int) getResources().getDimension(R.dimen.nine_mm), 0, (int) getResources().getDimension(R.dimen.three_mm), 0);
        } else if (type == ExportTypes.PDF.ordinal()) {

        } else if (type == ExportTypes.PrivateSeed.ordinal()) {
            mGoogleDriveButton.setVisibility(View.GONE);
            mGoogleDriveImage.setVisibility(View.GONE);
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mDropBoxButton.setVisibility(View.GONE);
            mDropBoxImage.setVisibility(View.GONE);
            mSDCardButton.setVisibility(View.GONE);
            mSDCardImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.VISIBLE);
            mEmailButton.setVisibility(View.VISIBLE);

            mPasswordEditText.setVisibility(View.VISIBLE);
        }
    }

    private void HighlightTimeButton(int pos) {
        for (int i = 0; i < mTimeButtons.size(); i++) {
            if (i == pos) {
                mTimeButtons.get(i).setBackground(getResources().getDrawable(R.drawable.btn_cancel));
            } else {
                mTimeButtons.get(i).setBackground(getResources().getDrawable(R.drawable.emboss_down));
            }
        }
    }

    private void showSelectorDialog(final Button button, int indexMonth, int indexDay, int indexYear, int indexHour, int indexMinute, String AMPM) {

        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lLP);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        final TimePicker timePicker = new TimePicker(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustomLight));
        timePicker.setIs24HourView(false);
        if (AMPM.equals("pm") && indexHour != 12) {
            indexHour += 12;
        } else if (AMPM.equals("am") && indexHour == 12) {
            indexHour -= 12;
        }
        timePicker.setCurrentHour(indexHour);
        timePicker.setCurrentMinute(indexMinute);
        final DatePicker datePicker = new DatePicker(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustomLight));
        datePicker.setCalendarViewShown(false);
        datePicker.init(indexYear, indexMonth - 1, indexDay, null);

        linearLayout.addView(datePicker);
        linearLayout.addView(timePicker);


        AlertDialog frag = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom))
                .setTitle(getString(R.string.export_saving_option_pick_date))
                .setView(linearLayout)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                HighlightTimeButton(7);
                                int time = 0;
                                String tempAMPM = "";
                                if (timePicker.getCurrentHour() > 12) {
                                    time = timePicker.getCurrentHour() - 12;
                                    tempAMPM = "pm";
                                } else if (timePicker.getCurrentHour() == 0) {
                                    time = timePicker.getCurrentHour() + 12;
                                    tempAMPM = "am";
                                } else {
                                    time = timePicker.getCurrentHour();
                                    tempAMPM = "am";
                                }
                                if (timePicker.getCurrentHour() == 12) {
                                    tempAMPM = "pm";
                                }
                                String tempMin = "";
                                if (timePicker.getCurrentMinute() < 10) {
                                    tempMin = "0" + timePicker.getCurrentMinute();
                                } else {
                                    tempMin = "" + timePicker.getCurrentMinute();
                                }
                                button.setText((datePicker.getMonth() + 1) + "/" + datePicker.getDayOfMonth() + "/" + datePicker.getYear() + " " + time + ":" + tempMin + " " + tempAMPM);
                                if (button == mFromButton) {
                                    mFromDate.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                                    mFromDate.set(Calendar.MONTH, datePicker.getMonth());
                                    mFromDate.set(Calendar.YEAR, datePicker.getYear());
                                    mFromDate.set(Calendar.HOUR, timePicker.getCurrentHour());
                                    mFromDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                                } else {
                                    mToDate.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                                    mToDate.set(Calendar.MONTH, datePicker.getMonth());
                                    mToDate.set(Calendar.YEAR, datePicker.getYear());
                                    mToDate.set(Calendar.HOUR, timePicker.getCurrentHour());
                                    mToDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.string_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }
                )
                .create();
        frag.show();
    }

    private void setupUI(int type) {
        if (type == ExportTypes.PrivateSeed.ordinal()) {
            mDatesLayout.setVisibility(View.GONE);
            mLastPeriodLayout.setVisibility(View.GONE);
            mThisPeriodLayout.setVisibility(View.GONE);
        }
    }

    private String getExportData(Wallet wallet, int type) {
        String filepath = null;

        // for now just hard code
        if (type == ExportTypes.CSV.ordinal()) {
            String temp = mCoreApi.GetCSVExportData(wallet.getUUID(), mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
            if (temp != null) {
                filepath = Common.createTempFileFromString("export.csv", temp);
            }
        } else if (type == ExportTypes.PrivateSeed.ordinal()) {
            filepath = Common.createTempFileFromString("export.txt", mCoreApi.getPrivateSeed(wallet));
        } else if (type == ExportTypes.Quicken.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportQuicken" ofType:@"QIF"];
        } else if (type == ExportTypes.Quickbooks.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportQuicken" ofType:@"QIF"];
        } else if (type == ExportTypes.PDF.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportPDF" ofType:@"pdf"];
        }
        return filepath;
    }

    private String mimeTypeFor(int type) {
        String strMimeType;
        if (type == ExportTypes.CSV.ordinal()) {
            strMimeType = "text/plain";
        } else if (type == ExportTypes.Quicken.ordinal()) {
            strMimeType = "application/qif";
        } else if (type == ExportTypes.Quickbooks.ordinal()) {
            strMimeType = "application/qbooks";
        } else if (type == ExportTypes.PDF.ordinal()) {
            strMimeType = "application/pdf";
        } else if (type == ExportTypes.PrivateSeed.ordinal()) {
            strMimeType = "text/plain";
        } else {
            strMimeType = "???";
        }
        return strMimeType;
    }

    private void exportWithEmail(Wallet wallet, String data) {
        // Compose
        String filename = getExportData(wallet, mExportType);
        if (filename == null) {
            ((NavigationActivity) getActivity()).ShowOkMessageDialog(getString(R.string.export_saving_option_no_transactions_title),
                    getString(R.string.export_saving_option_no_transactions_message));
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri file = Uri.parse("file://" + filename);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_saving_option_email_subject));
        intent.putExtra(Intent.EXTRA_STREAM, file);
        intent.putExtra(Intent.EXTRA_TEXT, wallet.getName());

        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            ((NavigationActivity) getActivity()).ShowOkMessageDialog("", getString(R.string.export_saving_option_no_email_apps));
        }
    }

    public enum ExportTypes {PrivateSeed, CSV, Quicken, Quickbooks, PDF}

}
