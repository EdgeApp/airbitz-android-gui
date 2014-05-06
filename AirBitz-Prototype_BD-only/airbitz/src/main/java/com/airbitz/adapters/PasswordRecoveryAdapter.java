package com.airbitz.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;

import com.airbitz.R;

import java.util.List;

/**
 * Created on 3/6/14.
 */
public class PasswordRecoveryAdapter extends ArrayAdapter<View> implements Filterable {

    private Context mContext;
    private List<String> mQuestions;
    private List<View> mItems;

    public PasswordRecoveryAdapter(Context context, List<View> items, List<String> questions){
        super(context, R.layout.item_password_recovery_spinner, items);
        mContext = context;
        mItems = items;
        mQuestions = questions;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public View getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        convertView = inflater.inflate(R.layout.item_password_recovery, null);
//        Spinner mySpinner = (Spinner)convertView.findViewById(R.id.item_password_recovery_spinner);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mQuestions);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mySpinner.setAdapter(adapter);
//
//        final EditText edittext = (EditText) convertView.findViewById(R.id.item_password_recovery_answer);
//        edittext.setOnKeyListener(new View.OnKeyListener() {
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                // If the event is a key-down event on the "enter" button
//                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    // Perform action on key press
//
//                    return true;
//                }
//                return false;
//            }
//        });

        return getItem(position);

//        return convertView;
    }
}