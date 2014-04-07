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
import com.airbitz.activities.MapBusinessDirectoryActivity;
import com.airbitz.utils.ImageHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.greenhalolabs.halohalo.ResHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created on 2/14/14.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private MapBusinessDirectoryActivity mActivity;

    private static final String TAG = MapInfoWindowAdapter.class.getSimpleName();

    public MapInfoWindowAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mActivity = (MapBusinessDirectoryActivity) context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = mInflater.inflate(R.layout.map_info_window_custom, null);

        TextView titleView = (TextView) view.findViewById(R.id.titleInfo);
        TextView addressView = (TextView) view.findViewById(R.id.addressInfo);
        LinearLayout infoLayout = (LinearLayout) view.findViewById(R.id.infoLayout);
        LinearLayout shadowLayout = (LinearLayout) view.findViewById(R.id.shadow_layout);

        titleView.setText(marker.getTitle());
        if (marker.getTitle().equalsIgnoreCase(ResHelper.getStringByResId(R.string.your_location))) {
            addressView.setText(" ");
            LinearLayout balloonLayout = (LinearLayout) view.findViewById(R.id.balloon_layout);
            balloonLayout.setPadding(0, 0, 0, 0);

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
            Bitmap roundedBackground = ImageHelper.scaleCenterCrop(backgroundImage, 90, 170);
            roundedBackground = ImageHelper.getRoundedCornerBitmap(roundedBackground, 5);
            ImageView backgroundImageView = (ImageView) view.findViewById(R.id.background_image);
            backgroundImageView.setImageBitmap(roundedBackground);

            Bitmap image = null;
            try {
                String imageUrl = mActivity.getMarkerImageLink().get(marker);
                InputStream in = new URL(imageUrl).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            if(image==null){
                image = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.bg_navbar);
            }
            roundedBackground = ImageHelper.scaleCenterCrop(image, 90, 170);
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
