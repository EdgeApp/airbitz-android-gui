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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.directory.BusinessDirectoryFragment;
import com.airbitz.models.Wallet;

import java.util.HashMap;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletAdapter extends ArrayAdapter<Wallet> {

    public static final String DRAG_TAG = "DragTag";
    final int INVALID_ID = -1;
    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
    HashMap<String, Integer> mArchivedIdMap = new HashMap<String, Integer>();
    private Context mContext;
    private List<Wallet> mWalletList;
    private int selectedViewPos = -1;
    private boolean hoverFirstHeader = false;
    private boolean hoverSecondHeader = false;
    private int nextId = 0;
    private boolean mIsBitcoin = true;
    private CoreAPI mCoreAPI;
    private boolean closeAfterArchive = false;
    private int archivePos;
    private ImageView mArchiveButton;
    private boolean mArchiveOpen = false;
    private Typeface mBitcoinTypeface;

    private OnHeaderButtonPress mHeaderButtonListener;
    public interface OnHeaderButtonPress {
        public void OnHeaderButtonPressed();
    }

    public void setHeaderButtonListener(OnHeaderButtonPress listener) {
        mHeaderButtonListener = listener;
    }

    public WalletAdapter(Context context, List<Wallet> walletList) {
        super(context, R.layout.item_listview_wallets, walletList);
        mContext = context;
        mWalletList = walletList;
        for (Wallet wallet : mWalletList) {
            if (wallet.isArchiveHeader()) {
                archivePos = mWalletList.indexOf(wallet);
            }
            addWallet(wallet);
        }
        mCoreAPI = CoreAPI.getApi();

        mBitcoinTypeface = Typeface.createFromAsset(context.getAssets(), "font/Lato-Regular.ttf");
    }

    public void setFirstHeaderHover(boolean status) {
        hoverFirstHeader = status;
    }

    public void setSecondHeaderHover(boolean status) {
        hoverSecondHeader = status;
    }

    public void setSelectedViewPos(int position) {
        selectedViewPos = position;
    }

    public void setIsBitcoin(boolean isBitcoin) {
        mIsBitcoin = isBitcoin;
    }

    public void setArchiveButtonState(boolean open) {
        mArchiveOpen = open;
        if(mArchiveButton != null) {
            if(open) {
                mArchiveButton.animate()
                        .rotation(180)
                        .start();
            }
            else {
                mArchiveButton.animate()
                        .rotation(0)
                        .start();
            }
        }
    }

    public void addWallet(Wallet wallet) {
        if (mArchivedIdMap.containsKey(wallet.getUUID())) {
            mIdMap.put(wallet.getUUID(), mArchivedIdMap.get(wallet.getUUID()));
            mArchivedIdMap.remove(wallet.getUUID());
        } else {
            mIdMap.put(wallet.getUUID(), nextId);
            nextId++;
        }
    }

    public void swapWallets() {
        archivePos++;
        for (int i = 0; i < mWalletList.size(); ++i) {
            if (mWalletList.get(i).isArchiveHeader()) {
                archivePos = i;
            }
            if (!mIdMap.containsKey(mWalletList.get(i).getUUID())) {
                mIdMap.put(mWalletList.get(i).getUUID(), nextId);
                nextId++;
            }
        }
    }

    public void updateArchive() {
        for (int i = 0; i < mWalletList.size(); ++i) {
            if (mWalletList.get(i).isArchiveHeader()) {
                archivePos = i;
            }
        }
    }

    public void switchCloseAfterArchive(int pos) {
        archivePos = pos;
        closeAfterArchive = false; //!closeAfterArchive;
    }

    @Override
    public Wallet getItem(int position) {
        return super.getItem(position);
    }

    public List<Wallet> getList() {
        return mWalletList;
    }

    public int getArchivePos() {
        return archivePos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Wallet wallet = mWalletList.get(position);
        if (mWalletList.get(position).isHeader() || mWalletList.get(position).isArchiveHeader()) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_wallets_header, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.item_listview_wallets_header_text);
            final ImageView imageButton = (ImageView) convertView.findViewById(R.id.item_listview_wallets_header_image);
            if (mWalletList.get(position).isArchiveHeader()) {
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mHeaderButtonListener != null) {
                            mHeaderButtonListener.OnHeaderButtonPressed();
                        }
                    }
                });
                textView.setText(mContext.getString(R.string.fragment_wallets_list_archive_title));
                mArchiveButton = imageButton;
                imageButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.collapse_up));
                if (mArchiveOpen) {
                    mArchiveButton.setRotation(180);
                }
                archivePos = position;

                if (hoverSecondHeader) {
                    convertView.setVisibility(View.INVISIBLE);
                } else {
                    convertView.setVisibility(View.VISIBLE);
                }
            } else {
                textView.setText("");
                textView.setBackgroundResource(android.R.color.transparent);
                imageButton.setImageResource(android.R.color.transparent);
                if (hoverFirstHeader) {
                    convertView.setVisibility(View.INVISIBLE);
                } else {
                    convertView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_wallets, parent, false);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.fragment_category_textview_title);
            TextView amountTextView = (TextView) convertView.findViewById(R.id.textview_amount);
            View drag = convertView.findViewById(R.id.drag_container);
            if (archivePos == 2 && !wallet.isArchived()) {
                drag.setVisibility(View.GONE);
            } else {
                drag.setVisibility(View.VISIBLE);
            }

            titleTextView.setTypeface(mBitcoinTypeface);
            amountTextView.setTypeface(mBitcoinTypeface);
            if (wallet.isLoading()) {
                titleTextView.setText(R.string.loading);
            } else {
                titleTextView.setText(wallet.getName());
            }
            if (mIsBitcoin) {
                amountTextView.setText(mCoreAPI.formatSatoshi(wallet.getBalanceSatoshi(), true));
            } else {
                long satoshi = wallet.getBalanceSatoshi();
                String temp = mCoreAPI.FormatCurrency(satoshi, wallet.getCurrencyNum(), false, true);
                amountTextView.setText(temp);
            }

            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard));
        }
        if (archivePos < position && closeAfterArchive) {
            convertView.setVisibility(View.GONE);
        } else if (selectedViewPos == position) {
            convertView.setVisibility(View.INVISIBLE);
        } else {
            convertView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public void selectItem(View view, int position) {
        view.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard_selected));
    }

    public int getMapSize() {
        return mIdMap.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        for (Wallet w : mWalletList) {
            return !w.isLoading();
        }
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return !mWalletList.get(position).isLoading();
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size() || mWalletList.size() == 0 || position > mWalletList.size() - 1) {
            return INVALID_ID;
        }
        Wallet item = mWalletList.get(position);
        return mIdMap.get(item.getUUID());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
