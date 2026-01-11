package com.cplps.android.api.models;

import java.util.List;

public class CFProblemSet {
    private List<CFProblem> problems;
    // We can ignore statistics for now

    public List<CFProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<CFProblem> problems) {
        this.problems = problems;
    }
}
