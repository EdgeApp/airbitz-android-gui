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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.models.Wallet;
import com.airbitz.objects.FileSaveLocationDialog;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import google.com.android.cloudprint.PrintDialogActivity;

public class ExportSavingOptionFragment extends WalletBaseFragment
    implements FileSaveLocationDialog.FileSaveLocation {

    public static final String EXPORT_TYPE = "com.airbitz.fragments.exportsavingoption.export_type";
    private final String TAG = getClass().getSimpleName();

    View mView;
    private HighlightOnPressButton mFromButton;
    private HighlightOnPressButton mToButton;

    private RelativeLayout mDatesLayout;
    private LinearLayout mLastPeriodLayout;
    private LinearLayout mThisPeriodLayout;

    private TextView mFromTextView;
    private TextView mToTextView;

    private Button mThisWeekButton;
    private Button mLastWeekButton;
    private Button mThisMonthButton;
    private Button mLastMonthButton;
    private Button mTodayButton;
    private Button mYesterdayButton;

    private Button mPrintButton;
    private Button mSDCardButton;
    private Button mEmailButton;
    private Button mGoogleDriveButton;
    private Button mShareButton;
    private Button mViewButton;

    private EditText mPasswordEditText;

    private Bundle mBundle;

    private List<Button> mTimeButtons;
    private Calendar today;
    private Calendar mFromDate;
    private Calendar mToDate;

    private int mExportType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getArguments();
        mExportType = mBundle.getInt(EXPORT_TYPE);
        setHomeEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        mView = i.inflate(R.layout.fragment_export_saving_options, container, false);
        today = Calendar.getInstance();

        mFromButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_from_spinner);
        mToButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_to_spinner);

        mDatesLayout = (RelativeLayout) mView.findViewById(R.id.layout_export_data);
        mLastPeriodLayout = (LinearLayout) mView.findViewById(R.id.layout_export_last_period);
        mThisPeriodLayout = (LinearLayout) mView.findViewById(R.id.layout_export_this_period);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);

        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_export_saving_password_edittext);
        mPasswordEditText.setTypeface(NavigationActivity.latoRegularTypeFace);

        mPrintButton = (Button) mView.findViewById(R.id.fragment_exportsaving_button_print);
        mSDCardButton = (Button) mView.findViewById(R.id.fragment_exportsaving_button_sd_card);
        mEmailButton = (Button) mView.findViewById(R.id.fragment_exportsaving_button_email);
        mGoogleDriveButton = (Button) mView.findViewById(R.id.fragment_exportsaving_button_google_drive);
        mShareButton = (Button) mView.findViewById(R.id.fragment_exportsaving_button_share);
        mViewButton = (Button) mView.findViewById(R.id.fragment_exportsaving_button_view);

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

        showExportButtons();

        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Wallet wallet = mWallet;
                String data = null;
                if (mExportType == ExportTypes.PrivateSeed.ordinal()) {
                    if(mCoreApi.PasswordOK(AirbitzApplication.getUsername(), mPasswordEditText.getText().toString())) {
                        data = mCoreApi.getPrivateSeed(wallet);
                    } else {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                        return;
                    }
                } else {
                    data = mCoreApi.GetCSVExportData(wallet.getUUID(), mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
                    if(data == null || data.isEmpty()) {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(
                                getString(R.string.export_saving_option_no_transactions_message));
                        return;
                    }
                }

                if(data != null && !data.isEmpty()) {
                    File file = createLocalTempFile(data);
                    if(file != null) {
                        String printName = saveName();
                        Intent printIntent = new Intent(getActivity(), PrintDialogActivity.class);
                        printIntent.setDataAndType(Uri.fromFile(file), "text/plain");
                        printIntent.putExtra("title", printName);
                        startActivity(printIntent);
                    }
                }

            }
        });

        mSDCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Wallet wallet = mWallet;
                String data = null;
                if (mExportType == ExportTypes.PrivateSeed.ordinal()) {
                    if(mCoreApi.PasswordOK(AirbitzApplication.getUsername(), mPasswordEditText.getText().toString())) {
                        data = mCoreApi.getPrivateSeed(wallet);
                    } else {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                        return;
                    }
                }
                else {
                    data = mCoreApi.GetCSVExportData(wallet.getUUID(), mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);

                    if(data == null || data.isEmpty()) {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(
                                getString(R.string.export_saving_option_no_transactions_message));
                        return;
                    }
                }

                if(data != null && !data.isEmpty()) {
                    chooseDirectoryAndSave(data);
                }
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Wallet wallet = mWallet;
                String dataOrFile;
                if (mExportType == ExportTypes.PrivateSeed.ordinal()) {
                    if(mCoreApi.PasswordOK(AirbitzApplication.getUsername(), mPasswordEditText.getText().toString())) {
                        dataOrFile = mCoreApi.getPrivateSeed(wallet);
                    } else {
                        dataOrFile = null;
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                    }
                } else {
                    dataOrFile = getExportFilePath(wallet, mExportType);
                }
                exportWithEmail(wallet, dataOrFile);
            }
        });

        mGoogleDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Wallet wallet = mWallet;
                    String filePath = getExportFilePath(wallet, mExportType);
                if(filePath != null) {
                    Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sendIntent.setType("text/csv");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath));
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.fragment_directory_detail_share)));
                }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_standard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
        case android.R.id.home:
            ExportSavingOptionFragment.popFragment(mActivity);
            return true;
        case R.id.action_help:
            mActivity.pushFragment(
                new HelpFragment(HelpFragment.EXPORT_WALLET_OPTIONS), NavigationActivity.Tabs.WALLET.ordinal());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void writeToFile(File file, String data) {
        try {
            FileWriter out = new FileWriter(file);
            out.write(data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createLocalTempFile(String data) {
        File file = new File(getActivity().getFilesDir(), "print");
        FileOutputStream outputStream;

        try {
            outputStream = getActivity().openFileOutput("print", Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void showExportButtons() {
        int type = mBundle.getInt(EXPORT_TYPE);
        if (type == ExportTypes.CSV.ordinal()) {
            setAllButtonViews(View.GONE);
            mSDCardButton.setVisibility(View.VISIBLE);
            mShareButton.setVisibility(View.VISIBLE);
            mPrintButton.setVisibility(View.VISIBLE);
        }
        else if (type == ExportTypes.PrivateSeed.ordinal()) {
            setAllButtonViews(View.GONE);
            mPrintButton.setVisibility(View.VISIBLE);
            mSDCardButton.setVisibility(View.VISIBLE);
            mViewButton.setVisibility(View.VISIBLE);
            mPasswordEditText.setVisibility(View.VISIBLE);
        }
    }

    private void setAllButtonViews(int state) {
        mPrintButton.setVisibility(state);
        mSDCardButton.setVisibility(state);
        mEmailButton.setVisibility(state);
        mGoogleDriveButton.setVisibility(state);
        mShareButton.setVisibility(state);
        mViewButton.setVisibility(state);
        mPasswordEditText.setVisibility(state);
    }

    private void HighlightTimeButton(int pos) {
        for (int i = 0; i < mTimeButtons.size(); i++) {
            if (i == pos) {
                mTimeButtons.get(i).setBackground(getResources().getDrawable(R.drawable.bg_button_orange));
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
        if(!mCoreApi.PasswordExists()) {
            mPasswordEditText.setVisibility(View.GONE);
        }
        else if (type == ExportTypes.PrivateSeed.ordinal()) {
            mDatesLayout.setVisibility(View.GONE);
            mLastPeriodLayout.setVisibility(View.GONE);
            mThisPeriodLayout.setVisibility(View.GONE);
        }
    }

    private String getExportFilePath(Wallet wallet, int type) {
        String filepath = null;

        // for now just hard code
        if (type == ExportTypes.CSV.ordinal()) {
            String temp = mCoreApi.GetCSVExportData(wallet.getUUID(), mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
            if (temp != null) {
                if(temp.isEmpty()) {
                    ((NavigationActivity)getActivity()).ShowFadingDialog(getString(R.string.export_saving_option_no_transactions_message));
                    return null;
                }
                else {
                    filepath = "file://" + Common.createTempFileFromString("export.csv", temp);
                }
            }
        } else if (type == ExportTypes.PrivateSeed.ordinal()) {
            filepath = Common.createTempFileFromString("export.txt", mCoreApi.getPrivateSeed(wallet));
        } else if (type == ExportTypes.Quicken.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportQuicken" ofType:@"QIF"];
            return null;
        } else if (type == ExportTypes.Quickbooks.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportQuicken" ofType:@"QIF"];
            return null;
        } else if (type == ExportTypes.PDF.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportPDF" ofType:@"pdf"];
            return null;
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

    private void exportWithEmail(Wallet wallet, String filepath) {
        if (filepath == null) {
            ((NavigationActivity) getActivity()).ShowFadingDialog(
                    getString(R.string.export_saving_option_no_transactions_message));
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri file = Uri.parse(filepath);
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

    String mDataToSave;
    private void chooseDirectoryAndSave(String data) {
        mDataToSave = data;
        String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileSaveLocationDialog dialog = new FileSaveLocationDialog(getActivity(), file, this);
    }

    @Override
    public void onFileSaveLocation(File file) {
        if (file != null && mDataToSave != null) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(file.getAbsolutePath() + "/" + saveName()));
                Writer out = new OutputStreamWriter(fos, "UTF-8");
                out.write(mDataToSave);
                out.flush();
                out.close();
                ((NavigationActivity) getActivity()).ShowFadingDialog("File saved: " + saveName());
            } catch (Throwable t) {
                Log.d(TAG, "createFileFromString failed for " + file.getAbsolutePath());
            }
        }
    }

    private String saveName() {
        String filename = mFromButton.getText().toString() + "-" + mToButton.getText().toString() + ".csv";
        if (mExportType == ExportTypes.PrivateSeed.ordinal()) {
            filename = mWallet.getName()+".txt";
        }

        return filename.replace("/", "_");
    }

    public static void pushFragment(NavigationActivity mActivity, String uuid, int type) {
        Fragment fragment = new ExportSavingOptionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RequestFragment.FROM_UUID, uuid);
        bundle.putInt(ExportSavingOptionFragment.EXPORT_TYPE, type);
        fragment.setArguments(bundle);

        mActivity.pushFragment(fragment);
    }

    public static void popFragment(NavigationActivity mActivity) {
        mActivity.popFragment();
    }

    public enum ExportTypes {PrivateSeed, CSV, Quicken, Quickbooks, PDF}
}
