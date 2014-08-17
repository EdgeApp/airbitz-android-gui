package com.airbitz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.airbitz.R;

/**
 * A simple help dialog
 *
 */
public class HelpDialog extends Fragment {
    Spanned mHtml;

    public HelpDialog() {}

    public HelpDialog(Spanned html) {
        mHtml = html;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.requestFitSystemWindows();
        View v = inflater.inflate(R.layout.dialog_help_info, container, false);
        TextView tv = (TextView) v.findViewById(R.id.dialog_help_textview);
        tv.setText(mHtml);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.dialog_help_close_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return v;
    }
}