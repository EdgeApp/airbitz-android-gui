package com.airbitz.models;

import java.util.List;

/**
 * Created on 3/13/14.
 */
public class Wallet {

    private String mName;
    private String mAmount;
    private List<AccountTransaction> mList;

    public Wallet(String name, String amount){
        mName = name;
        mAmount = amount;
    }

    public Wallet(String name, String amount, List<AccountTransaction> list){
        mName = name;
        mAmount = amount;
        mList = list;
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

    public void setList(List<AccountTransaction> list) {
        mList = list;
    }

    public List<AccountTransaction> getList() {
        return mList;
    }
}
