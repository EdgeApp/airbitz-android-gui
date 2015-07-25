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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.utils.Common;
import com.airbitz.plugins.PluginFramework.Plugin;

import java.util.List;

public class BuySellFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    private View mView;
    private TextView mTitleTextView;
    private ListView mPluginsListView;
    private PluginsAdapter mAdapter;
    private List<Plugin> mPlugins;

    public BuySellFragment() {
        mPlugins = PluginFramework.getPlugins();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        mView = inflater.inflate(R.layout.fragment_buysell, container, false);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mTitleTextView.setText(getString(R.string.buysell_title));

        ImageButton mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).onBackPressed();
            }
        });

        mAdapter = new PluginsAdapter(getActivity(), mPlugins);
        mPluginsListView = (ListView) mView.findViewById(R.id.fragment_buysell_listview);
        mPluginsListView.setAdapter(mAdapter);

        mPluginsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PluginsAdapter adapter = (PluginsAdapter) adapterView.getAdapter();
                Plugin plugin = adapter.getItem(i);
                launchPlugin(plugin);
            }
        });

        return mView;
    }

    public boolean launchPluginByCountry(String country,  String provider) {
        Plugin plugin = null;
        for (Plugin p : mPlugins) {
            if (p.provider.equals(provider) && p.country.equals(country)) {
                plugin = p;
            }
        }
        if (null != plugin) {
            launchPlugin(plugin);
            return true;
        }
        return false;
    }

    private void launchPlugin(Plugin plugin) {
        PluginFragment fragment = new PluginFragment(plugin);
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
    }

    public class PluginsAdapter extends BaseAdapter {

        private Context mContext;
        private List<Plugin> mPlugins;

        public PluginsAdapter(Context context, List<Plugin> plugins) {
            mContext = context;
            mPlugins = plugins;
        }

        @Override
        public int getCount() {
            return mPlugins.size();
        }

        @Override
        public Plugin getItem(int position) {
            return mPlugins.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Plugin plugin = mPlugins.get(position);

            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.item_buysell_plugin, null, false);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.item_name);
                holder.name.setTypeface(NavigationActivity.latoRegularTypeFace);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(plugin.name);
            return convertView;
        }
    }

    static class ViewHolder {
        TextView name;
    }
}
