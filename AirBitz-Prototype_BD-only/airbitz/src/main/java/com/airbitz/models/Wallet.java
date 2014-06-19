package com.airbitz.models;

import java.util.List;

/**
 * Created on 3/13/14.
 */
public class Wallet {

    public static final String WALLET_NAME = "name";
    public static final String WALLET_AMOUNT = "amount";

    private String mName;
    private String mUsername;
    private String mUUID;
    private int mCurrencyNum;
    private int mAttributes;
    private double mBalance;
    private List<AccountTransaction> mTransactions;

    private String mAmount;

    public Wallet(String name, double bal){
        mName = name;
        mBalance = bal;
    }

    public Wallet(String name, double balance, List<AccountTransaction> list){
        mName = name;
        mBalance = balance;
        mTransactions = list;
    }

    public void setName(String name){
        mName = name;
    }
    public String getName(){
        return mName;
    }

    public void setUsermame(String name){
        mUsername = name;
    }
    public String getUsermame(){
        return mUsername;
    }

    public void setUUID(String uuid){
        mUUID = uuid;
    }
    public String getUUID(){
        return mUUID;
    }

    public void setmCurrencyNum(int num) { mCurrencyNum = num; }
    public int getmCurrencyNum() {return mCurrencyNum; }

    public void setAttributes(int attr) { mAttributes = attr; }
    public int getAttributes() {return mAttributes; }

    public void setBalance(double bal) { mBalance = bal; }
    public double getBalance() {return mBalance; }

    public void setAmount(String amount){
        mAmount = amount;
    }
    public String getAmount(){
        return mAmount;
    }

    public void setList(List<AccountTransaction> list) {
        mTransactions = list;
    }
    public List<AccountTransaction> getList() {
        return mTransactions;
    }
}
