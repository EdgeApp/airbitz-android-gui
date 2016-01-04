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

package com.airbitz.plugins;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.utils.Common;
import com.airbitz.plugins.PluginFramework.Plugin;

import java.util.List;
import java.util.Map;

public class BuySellFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    private View mView;
    private ExpandableListView mPluginsListView;
    private PluginsAdapter mAdapter;

    public BuySellFragment() {
        setBackEnabled(true);
    }

    @Override
    protected String getTitle() {
        return mActivity.getString(R.string.buysell_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        mView = inflater.inflate(R.layout.fragment_buysell, container, false);

        mAdapter = new PluginsAdapter(getActivity());
        mPluginsListView = (ExpandableListView) mView.findViewById(R.id.fragment_buysell_listview);
        mPluginsListView.setAdapter(mAdapter);
        mPluginsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Plugin plugin = mAdapter.getChild(groupPosition, childPosition);
                launchPlugin(plugin, null);
                return true;
            }
        });
        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((NavigationActivity) getActivity()).onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean launchPluginByCountry(String country, String provider, Uri uri) {
        Plugin plugin = null;
        List<Plugin> mPlugins = PluginFramework.getPlugins();
        for (Plugin p : mPlugins) {
            if (p.provider.equals(provider) && p.country.equals(country)) {
                plugin = p;
            }
        }
        if (null != plugin) {
            launchPlugin(plugin, uri);
            return true;
        }
        return false;
    }

    private void launchPlugin(Plugin plugin, Uri uri) {
        PluginFragment.pushFragment(mActivity, plugin, uri);
    }

    public class PluginsAdapter extends BaseExpandableListAdapter {

        private Context mContext;
        private Map<String, List<Plugin>> mPlugins;
        private String[] mTags;

        public PluginsAdapter(Context context) {
            mContext = context;
            mPlugins = PluginFramework.getPluginsGrouped();
            mTags = PluginFramework.getTags();
        }

        @Override
        public int getGroupCount() {
            return mTags.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            String key = mTags[groupPosition];
            return mPlugins.get(key).size();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int position) {
            String key = mTags[groupPosition];
            return mPlugins.get(key).size();
        }

        @Override
        public String getGroup(int groupPosition) {
            return mTags[groupPosition];
        }

        @Override
        public Plugin getChild(int groupPosition, int position) {
            String key = mTags[groupPosition];
            return mPlugins.get(key).get(position);
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.item_buysell_header, null, false);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.item_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(mTags[groupPosition]);
            // Keep the group expanded
            ExpandableListView list = (ExpandableListView) parent;
            list.expandGroup(groupPosition);
            return convertView;
        }

        @Override
        public View getChildView(final int groupId, final int position, boolean isLastChild, View convertView, ViewGroup parent) {
            String key = mTags[groupId];
            Plugin plugin = mPlugins.get(key).get(position);

            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.item_buysell_plugin, null, false);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.item_name);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.name.setTypeface(NavigationActivity.latoRegularTypeFace);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(plugin.name);
            holder.image.setImageResource(plugin.imageResId);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    static class ViewHolder {
        TextView name;
        ImageView image;
    }
}
