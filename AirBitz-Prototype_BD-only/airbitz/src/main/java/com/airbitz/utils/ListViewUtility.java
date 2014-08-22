
package com.airbitz.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.airbitz.R;

/**
 * Created on 2/10/14.
 */
public class ListViewUtility {

    public static void setListViewHeightBasedOnChildren(ListView listView, int position) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < position; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
        listView.setLayoutParams(params);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        // for (int i = 0; i < listAdapter.getCount(); i++) {
        // View listItem = listAdapter.getView(i, null, listView);
        // if (listItem instanceof ViewGroup) {
        // listItem.setLayoutParams(new
        // ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        // ViewGroup.LayoutParams.WRAP_CONTENT));
        // }
        // listItem.measure(0, 0);
        // totalHeight += listItem.getMeasuredHeight();
        // }

        final int childCount = listAdapter.getCount();
        if (childCount > 0) {
            final View listItem = listAdapter.getView(0, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            int height = listItem.getMeasuredHeight();
            height -=
                    totalHeight += (listItem.getMeasuredHeight() * childCount);
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView, Context mContext) {
        if(listView == null){
            return;
        }
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;

        final int childCount = listAdapter.getCount();
        if (childCount > 0) {
            int height = (int) mContext.getResources().getDimension(R.dimen.venue_list_small_height_175);
            totalHeight = (height * childCount) + (int) mContext.getResources().getDimension(R.dimen.offset_height);
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void setTransactionDetailListViewHeightBasedOnChildren(ListView listView, int size, Context context) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null || context == null) {
            return;
        }


        int totalHeight = 0;
        totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < size; i++) {
            int height = (int) context.getResources().getDimension(R.dimen.drop_down_height);
            totalHeight += height;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


    public static void setWalletListViewHeightBasedOnChildren(ListView listView, int size, Context context) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < size; i++) {
            int height = (int) context.getResources().getDimension(R.dimen.wallet_list_view_height);
            totalHeight += height;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void setTransactionListViewHeightBasedOnChildren(ListView listView, int size) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null || size<1) {
            return;
        }

        View listItem = listAdapter.getView(0, null, listView);
        if (listItem instanceof ViewGroup) {
            listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        listItem.measure(0, 0);

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom() + size * listItem.getMeasuredHeight();

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * size);
        listView.setLayoutParams(params);
    }
}
