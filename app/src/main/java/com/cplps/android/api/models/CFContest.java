package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;

public class CFContest {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("phase")
    private String phase;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPhase() {
        return phase;
    }
}
