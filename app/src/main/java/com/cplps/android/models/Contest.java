package com.cplps.android.models;

public class Contest {
    private int contestId;
    private int platformId;
    private String contestName;
    private long contestDate;
    private int ratingChange;
    private int newRating;
    private int rank;
    private String platformName; // For display purposes

    public Contest() {
    }

    public Contest(String contestName, long contestDate, int ratingChange, int newRating, int rank) {
        this.contestName = contestName;
        this.contestDate = contestDate;
        this.ratingChange = ratingChange;
        this.newRating = newRating;
        this.rank = rank;
    }

    // Getters and setters
    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public long getContestDate() {
        return contestDate;
    }

    public void setContestDate(long contestDate) {
        this.contestDate = contestDate;
    }

    public int getRatingChange() {
        return ratingChange;
    }

    public void setRatingChange(int ratingChange) {
        this.ratingChange = ratingChange;
    }

    public int getNewRating() {
        return newRating;
    }

    public void setNewRating(int newRating) {
        this.newRating = newRating;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }
}
