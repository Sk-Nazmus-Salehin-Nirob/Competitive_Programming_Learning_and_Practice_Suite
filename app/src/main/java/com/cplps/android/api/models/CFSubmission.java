package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;

public class CFSubmission {
    @SerializedName("id")
    private long id;

    @SerializedName("problem")
    private CFProblem problem;

    @SerializedName("creationTimeSeconds")
    private long creationTimeSeconds;

    @SerializedName("verdict")
    private String verdict;

    public long getId() {
        return id;
    }

    public CFProblem getProblem() {
        return problem;
    }

    public long getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    public String getVerdict() {
        return verdict;
    }

    public boolean isAccepted() {
        return "OK".equals(verdict);
    }
}

class CFProblem {
    @SerializedName("contestId")
    private int contestId;

    @SerializedName("index")
    private String index;

    @SerializedName("name")
    private String name;

    @SerializedName("rating")
    private int rating;

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

    public String getProblemCode() {
        return contestId + index;
    }
}
