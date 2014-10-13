package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created on 3/6/14.
 */
public class PasswordRecoveryAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private List<String> mQuestions;

    public PasswordRecoveryAdapter(Context context, List<String> questions){
        super(context, R.layout.item_password_recovery_spinner, questions);
        mContext = context;
        mQuestions = questions;
    }

    @Override
    public int getCount() {
        return mQuestions.size()-1;
    }

    @Override
    public String getItem(int position) {
        return mQuestions.get(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.item_password_recovery_spinner_dropdown, parent, false);

        TextView textView = (TextView) convertView.findViewById(R.id.textview_dropdown_question);
        textView.setText(mQuestions.get(position));
        textView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        final TextView finalTextView = textView;
        textView.post(new Runnable() {
            @Override
            public void run() {
                finalTextView.setSingleLine(false);
            }
        });
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.item_password_recovery_spinner, parent, false);

        TextView textView = (TextView) convertView.findViewById(R.id.textview_question);
        textView.setText(mQuestions.get(position));
        textView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        return convertView;
    }
}