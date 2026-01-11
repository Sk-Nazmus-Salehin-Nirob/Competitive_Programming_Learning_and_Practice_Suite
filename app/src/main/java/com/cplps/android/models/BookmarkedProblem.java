package com.cplps.android.models;

public class BookmarkedProblem {
    private int bookmarkId;
    private int userId;
    private String problemUrl;
    private String problemCode;
    private String problemName;
    private int problemRating;
    private String category; // "to_solve" or "interesting"
    private long addedAt;

    public BookmarkedProblem() {
    }

    public BookmarkedProblem(int bookmarkId, int userId, String problemUrl, String problemCode,
            String problemName, int problemRating, String category, long addedAt) {
        this.bookmarkId = bookmarkId;
        this.userId = userId;
        this.problemUrl = problemUrl;
        this.problemCode = problemCode;
        this.problemName = problemName;
        this.problemRating = problemRating;
        this.category = category;
        this.addedAt = addedAt;
    }

    public int getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(int bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getProblemUrl() {
        return problemUrl;
    }

    public void setProblemUrl(String problemUrl) {
        this.problemUrl = problemUrl;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
}
