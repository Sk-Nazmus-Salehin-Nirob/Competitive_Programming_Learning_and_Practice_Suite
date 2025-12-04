package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;

public class CFUser {
    @SerializedName("handle")
    private String handle;

    @SerializedName("rating")
    private int rating;

    @SerializedName("maxRating")
    private int maxRating;

    @SerializedName("rank")
    private String rank;

    @SerializedName("maxRank")
    private String maxRank;

    public String getHandle() {
        return handle;
    }

    public int getRating() {
        return rating;
    }

    public int getMaxRating() {
        return maxRating;
    }

    public String getRank() {
        return rank;
    }

    public String getMaxRank() {
        return maxRank;
    }
}
