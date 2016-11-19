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

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.Wallet;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.request.RequestFragment;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import google.com.android.cloudprint.PrintDialogActivity;

public class ExportSavingOptionFragment extends WalletBaseFragment
    implements FileSaveLocationDialog.FileSaveLocation {

    public static final String EXPORT_TYPE = "com.airbitz.fragments.exportsavingoption.export_type";
    private final String TAG = getClass().getSimpleName();

    View mView;
    private Button mFromButton;
    private Button mToButton;

    private RelativeLayout mDatesLayout;
    private LinearLayout mLastPeriodLayout;
    private LinearLayout mThisPeriodLayout;

    private TextView mFromTextView;
    private TextView mToTextView;

    private View mPrintRow;
    private View mSdRow;
    private View mEmailRow;
    private View mGoogleRow;
    private View mShareRow;
    private View mViewRow;

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
    private Calendar mFromDate;
    private Calendar mToDate;

    private int mExportType;

    private DateFormat mDateFormatter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getArguments();
        mExportType = mBundle.getInt(EXPORT_TYPE);
        setHomeEnabled(true);

        mDateFormatter = DateFormat.getDateTimeInstance();
    }

    @Override
    public String getSubtitle() {
        return mActivity.getString(R.string.export_subtitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        mView = i.inflate(R.layout.fragment_export_saving_options, container, false);

        mFromButton = (Button) mView.findViewById(R.id.fragment_exportsaving_from_spinner);
        mToButton = (Button) mView.findViewById(R.id.fragment_exportsaving_to_spinner);

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
                    if (mAccount.checkPassword(mPasswordEditText.getText().toString())) {
                        data = mWallet.seed();
                    } else {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                        return;
                    }
                } else if (mExportType == ExportTypes.XPub.ordinal()) {
                    data = mWallet.xpub();
                } else {
                    data = mWallet.csvExport(mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
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
                    if (mAccount.checkPassword(mPasswordEditText.getText().toString())) {
                        data = mWallet.seed();
                    } else {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                        return;
                    }
                } else if (mExportType == ExportTypes.XPub.ordinal()) {
                    data = mWallet.xpub();
                } else {
                    if (mExportType == ExportTypes.Quickbooks.ordinal()) {
                        data = mWallet.qboExport(mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
                    } else {
                        data = mWallet.csvExport(mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
                    }
                    if (data == null || data.isEmpty()) {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(
                                getString(R.string.export_saving_option_no_transactions_message));
                        return;
                    }
                }

                if (data != null && !data.isEmpty()) {
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
                    if (mAccount.checkPassword(mPasswordEditText.getText().toString())) {
                        dataOrFile = mWallet.seed();
                    } else {
                        dataOrFile = null;
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                    }
                } else if (mExportType == ExportTypes.XPub.ordinal()) {
                    dataOrFile = mWallet.xpub();
                } else {
                    dataOrFile = getExportFilePath(wallet, mExportType);
                }
                exportWithEmail(wallet, dataOrFile);
            }
        });

        mGoogleDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    if (mAccount.checkPassword(mPasswordEditText.getText().toString())) {
                        ((NavigationActivity) getActivity()).ShowOkMessageDialog(mWallet.name() + " " + getString(R.string.export_saving_option_private_seed),
                            mWallet.seed());
                    } else {
                        ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.server_error_bad_password));
                    }
                } else if (mBundle.getInt(EXPORT_TYPE) == ExportTypes.XPub.ordinal()) {
                    ((NavigationActivity) getActivity()).ShowOkMessageDialog(mWallet.name() + " " + getString(R.string.export_saving_option_xpub),
                        mWallet.xpub());
                }
            }
        });

        mThisWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(4);

                Calendar beginning = Calendar.getInstance();
                beginning.set(Calendar.DAY_OF_WEEK, 1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                beginning.set(Calendar.SECOND, 0);

                mFromDate = beginning;
                mToDate = Calendar.getInstance();

                mFromButton.setText(mDateFormatter.format(mFromDate.getTime()));
                mToButton.setText(mDateFormatter.format(mToDate.getTime()));
            }
        });
        mThisMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(5);

                Calendar beginning = Calendar.getInstance();
                beginning.set(Calendar.DAY_OF_MONTH, 1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                beginning.set(Calendar.SECOND, 0);

                mFromDate = beginning;
                mToDate = Calendar.getInstance();

                mFromButton.setText(mDateFormatter.format(mFromDate.getTime()));
                mToButton.setText(mDateFormatter.format(mToDate.getTime()));
            }
        });
        mTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(3);

                Calendar beginning = Calendar.getInstance();
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                beginning.set(Calendar.SECOND, 0);

                Calendar end = Calendar.getInstance();
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText(mDateFormatter.format(mFromDate.getTime()));
                mToButton.setText(mDateFormatter.format(mToDate.getTime()));
            }
        });
        mYesterdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(0);

                Calendar beginning = Calendar.getInstance();
                beginning.add(Calendar.DATE, -1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                beginning.set(Calendar.SECOND, 0);
                Calendar end = Calendar.getInstance();
                end.add(Calendar.DATE, -1);
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText(mDateFormatter.format(mFromDate.getTime()));
                mToButton.setText(mDateFormatter.format(mToDate.getTime()));
            }
        });
        mLastMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(2);

                Calendar beginning = Calendar.getInstance();
                beginning.add(Calendar.MONTH, -1);
                beginning.set(Calendar.DAY_OF_MONTH, 1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                beginning.set(Calendar.SECOND, 0);
                Calendar end = Calendar.getInstance();
                end.add(Calendar.MONTH, -1);
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText(mDateFormatter.format(mFromDate.getTime()));
                mToButton.setText(mDateFormatter.format(mToDate.getTime()));
            }
        });
        mLastWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(1);

                Calendar beginning = Calendar.getInstance();
                beginning.add(Calendar.WEEK_OF_YEAR, -1);
                beginning.set(Calendar.DAY_OF_WEEK, 1);
                beginning.set(Calendar.HOUR_OF_DAY, 0);
                beginning.set(Calendar.MINUTE, 0);
                beginning.set(Calendar.SECOND, 0);
                Calendar end = Calendar.getInstance();
                end.add(Calendar.WEEK_OF_YEAR, -1);
                end.set(Calendar.DAY_OF_WEEK, 7);
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                mFromDate = beginning;
                mToDate = end;

                mFromButton.setText(mDateFormatter.format(mFromDate.getTime()));
                mToButton.setText(mDateFormatter.format(mToDate.getTime()));
            }
        });

        mToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mFromButton,
                        mToDate.get(Calendar.MONTH) + 1, mToDate.get(Calendar.DAY_OF_MONTH), mToDate.get(Calendar.YEAR),
                        mToDate.get(Calendar.HOUR), mToDate.get(Calendar.MINUTE),
                        mToDate.get(Calendar.AM_PM) == Calendar.PM ? "pm" : "am");
            }
        });

        mFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mFromButton,
                        mFromDate.get(Calendar.MONTH) + 1, mFromDate.get(Calendar.DAY_OF_MONTH), mFromDate.get(Calendar.YEAR),
                        mFromDate.get(Calendar.HOUR), mFromDate.get(Calendar.MINUTE),
                        mFromDate.get(Calendar.AM_PM) == Calendar.PM ? "pm" : "am");
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
        if (type == ExportTypes.CSV.ordinal()
                || type == ExportTypes.Quickbooks.ordinal()) {
            setAllButtonViews(View.GONE);
            mSDCardButton.setVisibility(View.VISIBLE);
            mShareButton.setVisibility(View.VISIBLE);
            mPrintButton.setVisibility(View.VISIBLE);
            mEmailButton.setVisibility(View.VISIBLE);
        } else if (type == ExportTypes.PrivateSeed.ordinal()) {
            setAllButtonViews(View.GONE);
            mPrintButton.setVisibility(View.VISIBLE);
            mSDCardButton.setVisibility(View.VISIBLE);
            mViewButton.setVisibility(View.VISIBLE);
            mPasswordEditText.setVisibility(View.VISIBLE);
        } else if (type == ExportTypes.XPub.ordinal()) {
            setAllButtonViews(View.GONE);
            mPrintButton.setVisibility(View.VISIBLE);
            mSDCardButton.setVisibility(View.VISIBLE);
            mViewButton.setVisibility(View.VISIBLE);
        }
    }

    private void setAllButtonViews(int state) {
        mPasswordEditText.setVisibility(state);
        mPrintButton.setVisibility(state);
        mSDCardButton.setVisibility(state);
        mEmailButton.setVisibility(state);
        mGoogleDriveButton.setVisibility(state);
        mShareButton.setVisibility(state);
        mViewButton.setVisibility(state);
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


        Dialog frag = new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom))
                .setTitle(getString(R.string.export_saving_option_pick_date))
                .setView(linearLayout)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                HighlightTimeButton(7);
                                if (button == mFromButton) {
                                    mFromDate.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                                    mFromDate.set(Calendar.MONTH, datePicker.getMonth());
                                    mFromDate.set(Calendar.YEAR, datePicker.getYear());
                                    mFromDate.set(Calendar.HOUR, timePicker.getCurrentHour());
                                    mFromDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                                    mFromDate.set(Calendar.SECOND, 0);
                                    mFromButton.setText(mDateFormatter.format(mFromDate.getTime()));
                                } else {
                                    mToDate.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                                    mToDate.set(Calendar.MONTH, datePicker.getMonth());
                                    mToDate.set(Calendar.YEAR, datePicker.getYear());
                                    mToDate.set(Calendar.HOUR, timePicker.getCurrentHour());
                                    mToDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                                    mToDate.set(Calendar.SECOND, 0);
                                    mToButton.setText(mDateFormatter.format(mToDate.getTime()));
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
        if (!mAccount.passwordExists()) {
            mPasswordEditText.setVisibility(View.GONE);
        }
        if (type == ExportTypes.PrivateSeed.ordinal()
                || type == ExportTypes.XPub.ordinal()) {
            mDatesLayout.setVisibility(View.GONE);
            mLastPeriodLayout.setVisibility(View.GONE);
            mThisPeriodLayout.setVisibility(View.GONE);
        }
    }

    private String getExportFilePath(Wallet wallet, int type) {
        String filepath = null;

        // for now just hard code
        if (type == ExportTypes.CSV.ordinal()) {
            String data = mWallet.csvExport(mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
            return filePathForData("export.csv", data);
        } else if (type == ExportTypes.PrivateSeed.ordinal()) {
            filepath = Common.createTempFileFromString("export.txt", mWallet.seed());
        } else if (type == ExportTypes.XPub.ordinal()) {
            filepath = Common.createTempFileFromString("xpub.txt", mWallet.xpub());
        } else if (type == ExportTypes.Quicken.ordinal()) {
            return null;
        } else if (type == ExportTypes.Quickbooks.ordinal()) {
            String data = mWallet.qboExport(mFromDate.getTimeInMillis() / 1000, mToDate.getTimeInMillis() / 1000);
            return filePathForData("export.QBO", data);
        } else if (type == ExportTypes.PDF.ordinal()) {
            return null;
        }
        return filepath;
    }

    private String filePathForData(String filename, String data) {
        if (data == null) {
            return null;
        }
        if (data.isEmpty()) {
            ((NavigationActivity)getActivity()).ShowFadingDialog(getString(R.string.export_saving_option_no_transactions_message));
            return null;
        } else {
            return "file://" + Common.createTempFileFromString(filename, data);
        }
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
        } else if (type == ExportTypes.PrivateSeed.ordinal()
                    || type == ExportTypes.XPub.ordinal()) {
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
        intent.putExtra(Intent.EXTRA_TEXT, wallet.name());

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
                FileOutputStream fos = new FileOutputStream(new File(file.getAbsolutePath(), saveName()));
                Writer out = new OutputStreamWriter(fos, "UTF-8");
                out.write(mDataToSave);
                out.flush();
                out.close();
                ((NavigationActivity) getActivity()).ShowFadingDialog("File saved: " + saveName());
            } catch (Throwable t) {
                AirbitzCore.logi("createFileFromString failed for " + file.getAbsolutePath());
                android.util.Log.e(TAG, "", t);
            }
        }
    }

    private String saveName() {
        String filename = mFromButton.getText().toString() + "-" + mToButton.getText().toString() + ".csv";
        if (mExportType == ExportTypes.Quickbooks.ordinal()) {
            filename = mFromButton.getText().toString() + "-" + mToButton.getText().toString() + ".QBO";
        } else if (mExportType == ExportTypes.PrivateSeed.ordinal()) {
            filename = mWallet.name() + ".txt";
        } else if (mExportType == ExportTypes.XPub.ordinal()) {
            filename = mWallet.name() + "_xpub.txt";
        }
        return filename.replaceAll("/", "-").replaceAll(" ", "_").replaceAll(":", "");
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

    public enum ExportTypes {PrivateSeed, XPub, CSV, Quicken, Quickbooks, PDF}
}
