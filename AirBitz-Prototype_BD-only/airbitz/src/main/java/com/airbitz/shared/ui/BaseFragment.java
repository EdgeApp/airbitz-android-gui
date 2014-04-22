package com.airbitz.shared.ui;

import android.app.Fragment;

/**
 * Created by dannyroa on 3/30/14.
 */
public class BaseFragment extends Fragment {


    public void setActionBarTitle(String title) {

        getActivity().getActionBar().setTitle(title);
    }

    public void showActionBarHome(boolean value) {

        getActivity().getActionBar().setDisplayShowHomeEnabled(value);

    }
}
