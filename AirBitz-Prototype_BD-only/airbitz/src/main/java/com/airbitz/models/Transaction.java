package com.airbitz.models;

/**
 * Created on 3/13/14.
 */
public class Transaction {

    public String mName;
    public String mAmount;

    public Transaction(String name, String amount){
        mName = name;
        mAmount = amount;
    }

    public void setName(String name){
        mName = name;
    }

    public String getName(){
        return mName;
    }

    public void setAmount(String amount){
        mAmount = amount;
    }

    public String getAmount(){
        return mAmount;
    }
}
