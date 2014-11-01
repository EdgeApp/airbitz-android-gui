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

package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.MapBusinessDirectoryFragment;
import com.airbitz.utils.ImageHelper;
import com.airbitz.fragments.maps.GoogleMapLayer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created on 2/14/14.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = MapInfoWindowAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private Context mContext;
    private GoogleMapLayer mLayer;

    public MapInfoWindowAdapter(Context context, GoogleMapLayer layer) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mLayer = layer;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = mInflater.inflate(R.layout.map_info_window_custom, null);

        TextView titleView = (TextView) view.findViewById(R.id.titleInfo);
        TextView addressView = (TextView) view.findViewById(R.id.addressInfo);
        LinearLayout infoLayout = (LinearLayout) view.findViewById(R.id.infoLayout);
        LinearLayout shadowLayout = (LinearLayout) view.findViewById(R.id.shadow_layout);

        if (marker.getTitle().length() > 19) {
            titleView.setText(marker.getTitle().substring(0, 15) + "...");
        } else {
            titleView.setText(marker.getTitle());
        }
        if (marker.getTitle().equalsIgnoreCase(mContext.getString(R.string.your_location))) {
            addressView.setText(" ");
            LinearLayout balloonLayout = (LinearLayout) view.findViewById(R.id.balloon_layout);
            LinearLayout balloonLayoutInner = (LinearLayout) view.findViewById(R.id.balloon_layout_inner);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            balloonLayout.setLayoutParams(llp);
            balloonLayout.setPadding((int) mContext.getResources().getDimension(R.dimen.padding_map_info), (int) mContext.getResources().getDimension(R.dimen.padding_map_info), (int) mContext.getResources().getDimension(R.dimen.padding_map_info), (int) mContext.getResources().getDimension(R.dimen.padding_map_info));
            balloonLayout.setBackgroundResource(R.drawable.bg_balloon_info);
            balloonLayoutInner.setLayoutParams(llp);
            balloonLayoutInner.setPadding((int) mContext.getResources().getDimension(R.dimen.padding_map_info), (int) mContext.getResources().getDimension(R.dimen.padding_map_info), (int) mContext.getResources().getDimension(R.dimen.padding_map_info), (int) mContext.getResources().getDimension(R.dimen.padding_map_info));

            shadowLayout.setVisibility(View.GONE);
            ImageView backgroundImageView = (ImageView) view.findViewById(R.id.background_image);
            backgroundImageView.setVisibility(View.GONE);

            titleView.setTextColor(0xFF000000);
            titleView.setShadowLayer(0, 0, 0, 0x00ffffff);
            shadowLayout.setBackgroundColor(0x00ffffff);
            TextView yourLocationTextView = (TextView) view.findViewById(R.id.titleYourLocation);
            yourLocationTextView.setVisibility(View.VISIBLE);
        } else {
            addressView.setText(marker.getSnippet());

            Bitmap backgroundImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bg_selected_tab);
            Bitmap roundedBackground = ImageHelper.scaleCenterCrop(backgroundImage, 70, 164);
            roundedBackground = ImageHelper.getRoundedCornerBitmap(roundedBackground, 25);
            ImageView backgroundImageView = (ImageView) view.findViewById(R.id.background_image);
            backgroundImageView.setImageBitmap(roundedBackground);

            Bitmap image = null;
            try {
                String imageUrl = mLayer.getMarkerImageLink().get(marker);
                InputStream in = new URL(imageUrl).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (image == null) {
                image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bg_navbar);
            }
            roundedBackground = ImageHelper.scaleCenterCrop(image, 70, 164);
            roundedBackground = ImageHelper.getRoundedCornerBitmap(roundedBackground, 5);
            backgroundImageView.setImageBitmap(roundedBackground);
        }

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
