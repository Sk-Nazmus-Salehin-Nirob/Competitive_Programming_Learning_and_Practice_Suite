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

    @SerializedName("author")
    private CFAuthor author;

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

    // Helper methods to access problem details
    public String getProblemCode() {
        return problem != null ? problem.getProblemCode() : "";
    }

    public String getProblemName() {
        return problem != null ? problem.getName() : "";
    }

    public int getProblemRating() {
        return problem != null ? problem.getRating() : 0;
    }

    public String getContestName() {
        return ""; // Disabled - contest ID != round number
    }
}
