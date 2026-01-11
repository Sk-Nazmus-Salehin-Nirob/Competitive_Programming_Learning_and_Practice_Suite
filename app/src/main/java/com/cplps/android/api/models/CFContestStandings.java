package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CFContestStandings {
    @SerializedName("contest")
    private CFContest contest;

    @SerializedName("problems")
    private List<CFProblem> problems;

    public CFContest getContest() {
        return contest;
    }

    public List<CFProblem> getProblems() {
        return problems;
    }
}
