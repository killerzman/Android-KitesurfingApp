package com.vladt.kitesurfingapp.Models;

import java.io.Serializable;

public class KitesurfingSpot implements Serializable {
    private String mID;
    private String mName;
    private String mCountry;
    private String mWhenToGo;
    private Boolean mIsFavorite;
    private String mLongitude;
    private String mLatitude;
    private String mWindProbability;

    public String getID() {
        return mID;
    }

    public void setID(String mID) {
        this.mID = mID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String mCountry) {
        this.mCountry = mCountry;
    }

    public String getWhenToGo() {
        return mWhenToGo;
    }

    public void setWhenToGo(String mWhenToGo) {
        this.mWhenToGo = mWhenToGo;
    }

    public Boolean getIsFavorite() {
        return mIsFavorite;
    }

    public void setIsFavorite(Boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String mLongitude) {
        this.mLongitude = mLongitude;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getWindProbability() {
        return mWindProbability;
    }

    public void setWindProbability(String mWindProbability) {
        this.mWindProbability = mWindProbability;
    }


}
