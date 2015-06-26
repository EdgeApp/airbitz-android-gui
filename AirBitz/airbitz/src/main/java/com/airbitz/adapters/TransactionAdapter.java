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

package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.directory.BusinessDirectoryFragment;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created on 2/13/14.
 */
public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private Wallet mWallet;
    private boolean mSearch;
    private boolean mIsBitcoin = true;
    private int mCurrencyNum;
    private CoreAPI mCoreAPI;
    private List<Transaction> mListTransaction;
    private LinkedHashMap<String, Uri> mContactList;
    private long[] mRunningSatoshi;
    private int mRound, mDimen;

    private final Picasso mPicasso;
    private SimpleDateFormat mFormatter;
    private Typeface mBitcoinTypeface;

    public TransactionAdapter(Context context, Wallet wallet, List<Transaction> listTransaction, LinkedHashMap<String, Uri> contactList) {
        super(context, R.layout.item_listview_transaction, listTransaction);
        mContext = context;
        mWallet = wallet;
        mCurrencyNum = mWallet.getCurrencyNum();
        mListTransaction = listTransaction;
        mContactList = contactList;
        createRunningSatoshi();
        mCoreAPI = CoreAPI.getApi();
        mPicasso = Picasso.with(context);
        mFormatter = new SimpleDateFormat("MMM dd h:mm aa", Locale.getDefault());
        mRound = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
        mDimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, mContext.getResources().getDisplayMetrics());

        mBitcoinTypeface = Typeface.createFromAsset(context.getAssets(), "font/Lato-Regular.ttf");
    }

    public void createRunningSatoshi() {
        mRunningSatoshi = new long[mListTransaction.size()];

        long total = 0;
        for (int i = mListTransaction.size() - 1; i > -1; i--) {
            total += mListTransaction.get(i).getAmountSatoshi();
            mRunningSatoshi[i] = total;
        }
    }

    public void setSearch(boolean isSearch) {
        mSearch = isSearch;
    }

    public void setIsBitcoin(boolean isBitcoin) {
        mIsBitcoin = isBitcoin;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if (convertView == null) {
            // well set up the ViewHolder
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_transaction, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.contactImageView = (ImageView) convertView.findViewById(R.id.imageview_contact_pic);
            viewHolder.contactImageViewFrame = (FrameLayout) convertView.findViewById(R.id.imageview_contact_pic_frame);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.textview_date);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.textview_name);
            viewHolder.runningTotalTextView = (TextView) convertView.findViewById(R.id.textview_amount_running_total);
            viewHolder.creditAmountTextView = (TextView) convertView.findViewById(R.id.textview_amount_kredit);
            viewHolder.confirmationsTextView = (TextView) convertView.findViewById(R.id.textview_confirmations);
            viewHolder.dateTextView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);
            viewHolder.nameTextView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);
            viewHolder.runningTotalTextView.setTypeface(mBitcoinTypeface);
            viewHolder.creditAmountTextView.setTypeface(mBitcoinTypeface);
            // store the holder with the view.
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }


        convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard));

        Transaction transaction = mListTransaction.get(position);

        String dateString = mFormatter.format(transaction.getDate() * 1000);
        viewHolder.dateTextView.setText(dateString);

        long transactionSatoshis = transaction.getAmountSatoshi();
        long transactionSatoshisAbs = Math.abs(transactionSatoshis);

        String name = transaction.getName();
        viewHolder.nameTextView.setText(name);
        Uri payeeImage = mContactList.get(name);
        if (mContactList != null && payeeImage != null && !name.isEmpty()) {
            viewHolder.contactImageViewFrame.setVisibility(View.VISIBLE);
            if (payeeImage.getScheme().contains("content")) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), payeeImage);
                    Bitmap bmap2 = ThumbnailUtils.extractThumbnail(bitmap, mDimen, mDimen);
                    RoundedTransformation rt = new RoundedTransformation(mRound, mRound);
                    bitmap = rt.transform(bmap2);
                    viewHolder.contactImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                mPicasso.load(payeeImage)
                        .noFade()
                        .transform(new RoundedTransformation(mRound, mRound))
                        .into(viewHolder.contactImageView);
                Log.d(TAG, "loading remote " + payeeImage.toString());
            }
            viewHolder.contactImageView.setBackgroundResource(0);
        } else {
            viewHolder.contactImageViewFrame.setVisibility(View.VISIBLE);
            if (transactionSatoshis > 0) {
                viewHolder.contactImageView.setImageResource(R.drawable.ic_request);
                viewHolder.contactImageView.setBackgroundResource(R.drawable.bg_icon_request);
            } else {
                viewHolder.contactImageView.setImageResource(R.drawable.ic_send);
                viewHolder.contactImageView.setBackgroundResource(R.drawable.bg_icon_send);
            }
        }

        String btcSymbol;
        String btcSymbolBalance = mCoreAPI.getUserBTCSymbol();
        Boolean bPositive;

        if (transactionSatoshis < 0) {
            btcSymbol = "-" + btcSymbolBalance;
            bPositive = false;
        } else {
            btcSymbol = btcSymbolBalance;
            bPositive = true;
        }

        if (mSearch) {
            String btcCurrency = mCoreAPI.formatSatoshi(transactionSatoshisAbs, true);
            viewHolder.creditAmountTextView.setText(btcCurrency);

            String fiatCurrency = mCoreAPI.FormatCurrency(transactionSatoshis, mCurrencyNum, false, true);
            viewHolder.runningTotalTextView.setText(fiatCurrency);

            if (bPositive) {
                viewHolder.runningTotalTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
            } else {
                viewHolder.runningTotalTextView.setTextColor(mContext.getResources().getColor(R.color.red));
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.red));
            }
        } else {
            viewHolder.runningTotalTextView.setTextColor(mContext.getResources().getColor(R.color.gray_text));
            if (bPositive) {
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
            } else {
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.red));
            }
            if (mIsBitcoin) {
                String walletCurrency = mCoreAPI.formatSatoshi(transactionSatoshisAbs, false);
                String totalCurrency = mCoreAPI.formatSatoshi(mRunningSatoshi[position], false);

                viewHolder.creditAmountTextView.setText(btcSymbol + " " + walletCurrency);
                viewHolder.runningTotalTextView.setText(btcSymbolBalance + " " + totalCurrency);
            } else {
                String walletCurrency = mCoreAPI.FormatCurrency(transactionSatoshis, mCurrencyNum, false, true);
                String totalCurrency = mCoreAPI.FormatCurrency(mRunningSatoshi[position], mCurrencyNum, false, true);

                viewHolder.creditAmountTextView.setText(walletCurrency);
                viewHolder.runningTotalTextView.setText(totalCurrency);
            }
        }
        if (mSearch) {
            viewHolder.confirmationsTextView.setText(transaction.getCategory());
            viewHolder.confirmationsTextView.setTextColor(mContext.getResources().getColor(R.color.gray_text));
        } else {
            viewHolder.confirmationsTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
            if (transaction.isSyncing()) {
                viewHolder.confirmationsTextView.setText(mContext.getString(R.string.synchronizing));
                viewHolder.confirmationsTextView.setTextColor(mContext.getResources().getColor(R.color.gray_text));
            } else if (transaction.getConfirmations() <= 0) {
                viewHolder.confirmationsTextView.setText(mContext.getString(R.string.fragment_wallet_unconfirmed));
                viewHolder.confirmationsTextView.setTextColor(mContext.getResources().getColor(R.color.red));
            } else if (transaction.getConfirmations() >= 6) {
                viewHolder.confirmationsTextView.setText(mContext.getString(R.string.fragment_wallet_confirmed));
            } else {
                viewHolder.confirmationsTextView.setText(transaction.getConfirmations() + mContext.getString(R.string.fragment_wallet_confirmations));
            }
        }
        return convertView;
    }

    public void selectItem(View convertView, int position) {
        if (0 == position && mListTransaction.size() == 1) {
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_solo_selected));
        } else if (0 == position) {
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_top_archive_selected));
        } else if (mListTransaction.size() - 1 == position) {
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_bottom_selected));
        } else {
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard_selected));
        }
    }

    static class ViewHolderItem {
        ImageView contactImageView;
        FrameLayout contactImageViewFrame;
        TextView dateTextView;
        TextView nameTextView;
        TextView runningTotalTextView;
        TextView creditAmountTextView;
        TextView confirmationsTextView;
    }
}
