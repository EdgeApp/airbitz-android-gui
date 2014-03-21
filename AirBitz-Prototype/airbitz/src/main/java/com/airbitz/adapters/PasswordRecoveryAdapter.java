package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.LandingActivity;

/**
 * Created on 3/6/14.
 */
public class PasswordRecoveryAdapter extends ArrayAdapter<String> implements Filterable {

    private Context mContext;
    private String[] mQuestionValue;

    public PasswordRecoveryAdapter(Context context, String[] questionValue){
        super(context, R.layout.item_password_recovery_spinner, questionValue);
        mContext = context;
        mQuestionValue = questionValue;
    }

    @Override
    public int getCount() {
        return mQuestionValue.length;
    }

    @Override
    public String getItem(int position) {
        return mQuestionValue[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_password_recovery_spinner, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.textview_question);
        textView.setTypeface(LandingActivity.montserratBoldTypeFace);
        textView.setText(mQuestionValue[position]);

        return convertView;
    }
}