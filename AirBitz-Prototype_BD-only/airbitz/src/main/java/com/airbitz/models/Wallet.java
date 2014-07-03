package com.airbitz.models;

import java.util.List;

/**
 * Created on 3/13/14.
 */
public class Wallet {

    // Strings for argument passing in bundles
    public static final String WALLET_NAME = "com.airbitz.models.wallet.wallet_name";
    public static final String WALLET_AMOUNT_SATOSHI = "com.airbitz.models.wallet.wallet_amount_satoshi";
    public static final String WALLET_UUID = "com.airbitz.WalletsFragment.UUID";

    private String mName;
    private String mUUID;
    private int mCurrencyNum;
    private long mAttributes;
    private long mBalance;
    private List<Transaction> mTransactions;

    private String mAmount = "";

    public Wallet(String name, String amount) {
        mName = name;
    }

    public Wallet(String name, long balanceSatoshi){
        mName = name;
        mBalance = balanceSatoshi;
    }

    public Wallet(String name, long balance, List<Transaction> list){
        mName = name;
        mBalance = balance;
        mTransactions = list;
    }

    public boolean isHeader() {
        return getName().equals("xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL");
    }

    public boolean isArchiveHeader() {
        return getName().equals("SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd");
    }

    public boolean isArchived() {
        return (getAttributes() & 0x1) == 1;
    }

    public void setName(String name){
        mName = name;
    }
    public String getName(){
        return mName;
    }

    public void setUUID(String uuid){
        mUUID = uuid;
    }
    public String getUUID(){
        return mUUID;
    }

    public void setCurrencyNum(int num) { mCurrencyNum = num; }
    public int getCurrencyNum() {return mCurrencyNum; }

    public void setAttributes(long attr) { mAttributes = attr; }
    public long getAttributes() {return mAttributes; }

    public void setBalance(long bal) { mBalance = bal; }
    public long getBalance() {return mBalance; }

    public void setAmount(String amount){
        mAmount = amount;
    }
    public String getAmount(){
        return mAmount;
    }

    public void setTransactions(List<Transaction> list) {
        mTransactions = list;
    }
    public List<Transaction> getTransactions() {
        return mTransactions;
    }
}
