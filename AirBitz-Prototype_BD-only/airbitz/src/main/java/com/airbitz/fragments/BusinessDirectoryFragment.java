package com.airbitz.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.DisplayActivity;
import com.airbitz.activities.LandingActivity;
import com.airbitz.activities.SecurityActivity;
import com.airbitz.utils.Common;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class BusinessDirectoryFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_send, container, false);

        return view;
    }

}
