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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.utils.Common;
import com.airbitz.plugins.PluginFramework.Plugin;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public abstract class BasePluginList extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    private View mView;
    private ListView mPluginsListView;
    private PluginsAdapter mAdapter;
    protected List<Plugin> mPlugins;

    public BasePluginList() {
        setBackEnabled(true);
        mPlugins = new ArrayList<Plugin>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        mView = inflater.inflate(R.layout.fragment_pluginlist, container, false);

        mAdapter = new PluginsAdapter(getActivity(), mPlugins);
        mPluginsListView = (ListView) mView.findViewById(R.id.fragment_buysell_listview);
        mPluginsListView.setAdapter(mAdapter);

        mPluginsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PluginsAdapter adapter = (PluginsAdapter) adapterView.getAdapter();
                Plugin plugin = adapter.getItem(i);
                launchPlugin(plugin, null);
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
        PluginFragment.pushFragment(mActivity, plugin, uri, getTitle());
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
                convertView = inflater.inflate(R.layout.item_listview_plugins, null, false);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.textview_top_line);
                holder.provider = (TextView) convertView.findViewById(R.id.textview_bottom_line);
                holder.provider.setTypeface(NavigationActivity.latoRegularTypeFace);
                holder.name.setTextColor(mContext.getResources().getColor(R.color.semi_black_text));
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.name.setTypeface(NavigationActivity.latoRegularTypeFace);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name = plugin.name;
            if (!TextUtils.isEmpty(plugin.subtitle)) {
                name += " " + plugin.subtitle;
            }
            holder.name.setText(name);
            String provider = plugin.provider;
            holder.provider.setText(provider);
            new DownloadImageTask(holder.image)
                    .execute(plugin.imageUrl);
            return convertView;
        }
    }

    static class ViewHolder {
        TextView name;
        TextView provider;
        ImageView image;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
