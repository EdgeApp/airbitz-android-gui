package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.LandingActivity;

import java.util.ArrayList;

/**
 * Created on 2/10/14.
 */
public class QuestionArrayAdapter extends ArrayAdapter<String>{

    private final Context mContext;
    private final ArrayList<String> mValues;
    private final LayoutInflater mInflater;

    public QuestionArrayAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.item_listview_forgot_password_questions, values);
        this.mContext = context;
        this.mValues = values;
        this.mInflater = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_forgot_password_questions, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.questionText);
        textView.setTypeface(LandingActivity.montserratBoldTypeFace);
        textView.setText(mValues.get(position));
        EditText editText = (EditText) convertView.findViewById(R.id.answer);
        editText.setTypeface(LandingActivity.montserratRegularTypeFace);
        return convertView;
    }
}
