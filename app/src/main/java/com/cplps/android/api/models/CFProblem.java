package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;

public class CFProblem {
    @SerializedName("contestId")
    private int contestId;

    @SerializedName("index")
    private String index;

    @SerializedName("name")
    private String name;

    @SerializedName("rating")
    private int rating;

    @SerializedName("type")
    private String type;

    public int getContestId() {
        return contestId;
    }

    public String getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public String getType() {
        return type;
    }

    public String getProblemCode() {
        return contestId + index;
    }
}
