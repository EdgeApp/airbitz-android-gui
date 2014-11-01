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

package com.airbitz.shared.adapters;


import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;


public abstract class AbsBaseAdapter<T> extends BaseAdapter {

    // Member Variables //////////////////////////////////////////////////////////////////

    private List<T> mBackingList;

    // Constructors //////////////////////////////////////////////////////////////////////

    public AbsBaseAdapter() {
    }

    public AbsBaseAdapter(List<T> backingList) {
        mBackingList = backingList;
    }

    // Public Methods ////////////////////////////////////////////////////////////////////

    /**
     * Gets this adapter's backing List.
     */
    public List<T> getList() {
        return mBackingList;
    }

    /**
     * Sets the adapter to the specified List. Note, this method automatically triggers a
     * {@link #notifyDataSetChanged()} call.
     */
    public void setList(List<T> list) {
        mBackingList = list;
        notifyDataSetChanged();
    }

    /**
     * Adds the collection to the backing adapter. Note, this method automatically
     * triggers a {@link #notifyDataSetChanged()} call.
     */
    public void add(List<T> list) {

        if (mBackingList == null) {
            mBackingList = list;
        } else {
            mBackingList.addAll(list);
        }
        notifyDataSetChanged();
    }

    /**
     * Clears the data from the backing adapter. Note, this method automatically triggers
     * a {@link #notifyDataSetChanged()} call.
     */
    public void clear() {

        if (mBackingList != null) {
            mBackingList.clear();
            notifyDataSetChanged();
        }
    }

    // Protected Methods /////////////////////////////////////////////////////////////////

    protected abstract View newView(ViewGroup parent, int position);

    protected abstract void bindView(View view, int position);

    // BaseAdapter Methods ///////////////////////////////////////////////////////////////

    @Override
    public int getCount() {
        return (mBackingList == null ? 0 : mBackingList.size());
    }

    @Override
    public T getItem(int position) {
        if (mBackingList == null || position < 0 || position >= getCount()) {
            return null;
        } else {
            return mBackingList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View v = (convertView != null) ? convertView : newView(parent, position);
        bindView(v, position);

        return v;
    }


}
