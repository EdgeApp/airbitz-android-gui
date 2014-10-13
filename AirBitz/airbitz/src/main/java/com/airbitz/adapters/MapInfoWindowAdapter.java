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
    private MapBusinessDirectoryFragment mFragment;

    public MapInfoWindowAdapter(Context context, MapBusinessDirectoryFragment frag) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mFragment = frag;
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
                String imageUrl = mFragment.getMarkerImageLink().get(marker);
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
