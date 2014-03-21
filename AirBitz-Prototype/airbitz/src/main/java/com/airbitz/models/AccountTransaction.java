package com.airbitz.models;

/**
 * Created on 3/14/14.
 */
public class AccountTransaction {

    private String mDate = "";
    private String mName = "";
    private String mDebitAmount ="";
    private String mCreditAmount = "";

    public AccountTransaction(String name, String date, String debitAmount, String creditAmount){
        mDate = date;
        mName = name;
        mDebitAmount = debitAmount;
        mCreditAmount = creditAmount;
    }

    public void setName(String name){
        mName = name;
    }

    public String getName(){
        return mName;
    }

    public void setDate(String date){
        mDate = date;
    }

    public String getDate(){
        return mDate;
    }

    public void setDebitAmount(String amount){
        mDebitAmount = amount;
    }

    public String getDebitAmount(){
        return mDebitAmount;
    }

    public void setCreditAmount(String amount){
        mCreditAmount = amount;
    }

    public String getCreditAmount(){
        return mCreditAmount;
    }
}
