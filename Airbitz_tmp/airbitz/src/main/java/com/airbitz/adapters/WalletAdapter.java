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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import co.airbitz.core.Account;
import co.airbitz.core.Wallet;
import com.airbitz.api.WalletWrapper;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.fragments.directory.BusinessDirectoryFragment;

import java.util.HashMap;
import java.util.List;

public class WalletAdapter extends ArrayAdapter<WalletWrapper> {

    public static final String DRAG_TAG = "DragTag";
    final int INVALID_ID = -1;
    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
    HashMap<String, Integer> mArchivedIdMap = new HashMap<String, Integer>();
    private Context mContext;
    private List<WalletWrapper> mWalletList;
    private int selectedViewPos = -1;
    private int mSelectedWalletPos = -1;
    private boolean hoverFirstHeader = false;
    private boolean hoverSecondHeader = false;
    private int nextId = 0;
    private boolean mIsBitcoin = true;
    private Account mAccount;
    private boolean closeAfterArchive = false;
    private int archivePos;
    private ImageView mArchiveButton;
    private boolean mArchiveOpen = false;
    private Typeface mBitcoinTypeface;
    private int mCurrencyNum;

    private WalletMenuListener mWalletMenuListener;
    public interface WalletMenuListener {
        public void renameWallet(Wallet wallet);
        public void deleteWallet(Wallet wallet);
    }

    public void setWalletMenuListener(WalletMenuListener listener) {
        mWalletMenuListener = listener;
    }

    private OnHeaderButtonPress mHeaderButtonListener;
    public interface OnHeaderButtonPress {
        public void OnHeaderButtonPressed();
    }

    public void setHeaderButtonListener(OnHeaderButtonPress listener) {
        mHeaderButtonListener = listener;
    }

    public WalletAdapter(Context context, List<WalletWrapper> walletList) {
        super(context, R.layout.item_listview_wallets, walletList);
        mContext = context;
        mWalletList = walletList;
        for (WalletWrapper wallet : mWalletList) {
            if (wallet.isArchiveHeader()) {
                archivePos = mWalletList.indexOf(wallet);
            }
            addWallet(wallet);
        }
        mAccount = AirbitzApplication.getAccount();
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

    public void setSelectedWallet(int position) {
        mSelectedWalletPos = position;
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

    public void addWallet(WalletWrapper wallet) {
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
            WalletWrapper wallet = mWalletList.get(i);
            if (wallet.isArchiveHeader()) {
                archivePos = i;
            }
            if (!mIdMap.containsKey(wallet.getUUID())) {
                mIdMap.put(wallet.getUUID(), nextId);
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
    public WalletWrapper getItem(int position) {
        return super.getItem(position);
    }

    public List<WalletWrapper> getList() {
        return mWalletList;
    }

    public int getArchivePos() {
        return archivePos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WalletWrapper wallet = mWalletList.get(position);
        if (wallet.isHeader()) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_wallets_header, parent, false);
        } else if (wallet.isArchiveHeader()) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_wallets_archive_header, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.item_listview_wallets_archive_header_text);
            final ImageView imageButton = (ImageView) convertView.findViewById(R.id.item_listview_wallets_archive_header_image);
            if (wallet.isArchiveHeader()) {
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
            View drag = convertView.findViewById(R.id.menu_container);
            if (archivePos == 2 && !wallet.wallet().isArchived()) {
                drag.setVisibility(View.INVISIBLE);
            } else {
                drag.setVisibility(View.VISIBLE);
            }

            titleTextView.setTypeface(mBitcoinTypeface);
            amountTextView.setTypeface(mBitcoinTypeface);
            if (wallet.wallet().isLoading()) {
                titleTextView.setText(R.string.loading);
            } else {
                titleTextView.setText(wallet.wallet().getName());
            }
            if (mIsBitcoin) {
                amountTextView.setText(mAccount.formatSatoshi(wallet.wallet().getBalanceSatoshi(), true));
            } else {
                long satoshi = wallet.wallet().getBalanceSatoshi();
                String temp = mAccount.FormatCurrency(satoshi, mCurrencyNum, false, true);
                amountTextView.setText(temp);
            }

            convertView.setBackgroundResource(R.drawable.wallet_list_standard);
            if (mSelectedWalletPos == position) {
                convertView.setSelected(true);
            } else {
                convertView.setSelected(false);
            }

            final Wallet menuWallet = wallet.wallet();
            final PopupMenu popupMenu = new PopupMenu(mContext, drag);
            popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, mContext.getString(R.string.string_rename));
            popupMenu.getMenu().add(Menu.NONE, 2, Menu.NONE, mContext.getString(R.string.string_delete));
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (mWalletMenuListener == null) {
                        return false;
                    }
                    if (item.getItemId() == 1) {
                        mWalletMenuListener.renameWallet(menuWallet);
                    } else {
                        mWalletMenuListener.deleteWallet(menuWallet);
                    }
                    return true;
                }
            });
            drag.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    popupMenu.show();
                }
            });
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
        for (WalletWrapper w : mWalletList) {
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
        WalletWrapper item = mWalletList.get(position);
        return mIdMap.get(item.getUUID());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void setCurrencyNum(int currencyNum) {
        mCurrencyNum = currencyNum;
    }
}
