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
