package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;

public class CFRatingChange {
    @SerializedName("contestId")
    private int contestId;

    @SerializedName("contestName")
    private String contestName;

    @SerializedName("ratingUpdateTimeSeconds")
    private long ratingUpdateTimeSeconds;

    @SerializedName("oldRating")
    private int oldRating;

    @SerializedName("newRating")
    private int newRating;

    @SerializedName("rank")
    private int rank;

    public int getContestId() {
        return contestId;
    }

    public String getContestName() {
        return contestName;
    }

    public long getRatingUpdateTimeSeconds() {
        return ratingUpdateTimeSeconds;
    }

    public int getOldRating() {
        return oldRating;
    }

    public int getNewRating() {
        return newRating;
    }

    public int getRank() {
        return rank;
    }

    public int getRatingChange() {
        return newRating - oldRating;
    }
}
