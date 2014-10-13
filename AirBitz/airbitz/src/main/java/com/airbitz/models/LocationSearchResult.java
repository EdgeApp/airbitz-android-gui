package com.airbitz.models;

/**
 * Created by chris on 4/2/14.
 */
public class LocationSearchResult {

    private String mLocationName;
    private boolean mIsCached;

    public LocationSearchResult(String mLocationName, boolean isCached) {
        this.mLocationName = mLocationName;
        this.mIsCached = isCached;
    }

    @Override
    public String toString() {
        return mLocationName;
    }

    @Override
    public int hashCode() {
        return mLocationName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocationSearchResult) {
            final LocationSearchResult otherLocation = (LocationSearchResult) o;
            return this.mLocationName.equals(otherLocation.getLocationName());
        }
        return false;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public boolean isCached() {
        return mIsCached;
    }
}
