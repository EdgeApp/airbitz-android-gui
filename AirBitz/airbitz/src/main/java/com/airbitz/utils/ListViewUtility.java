/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

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
        if (listView == null) {
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
        if (listAdapter == null || listAdapter.getCount() < 1) {
            return;
        }
        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        View listItem = listAdapter.getView(0, null, listView);
        listItem.measure(0, 0);
        totalHeight += (listItem.getMeasuredHeight() + listView.getDividerHeight()) * listAdapter.getCount();

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }
}
