package com.cplps.android.models;

public class SolvedProblem {
    private int problemId;
    private int platformId;
    private String problemCode;
    private String problemName;
    private int problemRating;
    private long solvedAt;
    private String platformName; // For display purposes

    public SolvedProblem() {
    }

    public SolvedProblem(String problemCode, String problemName, int rating, long solvedAt) {
        this.problemCode = problemCode;
        this.problemName = problemName;
        this.problemRating = rating;
        this.solvedAt = solvedAt;
    }

    // Getters and setters
    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public String getProblemCode() {
        return problemCode;
    }

    public void setProblemCode(String problemCode) {
        this.problemCode = problemCode;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public int getProblemRating() {
        return problemRating;
    }

    public void setProblemRating(int problemRating) {
        this.problemRating = problemRating;
    }

    public long getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(long solvedAt) {
        this.solvedAt = solvedAt;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }
}
