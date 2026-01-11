package com.cplps.android.models;

public class BookmarkCategory {
    private int categoryId;
    private int userId;
    private String categoryName;
    private long createdAt;
    private int problemCount; // convenience field for UI

    public BookmarkCategory() {
    }

    public BookmarkCategory(int categoryId, int userId, String categoryName, long createdAt) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getProblemCount() {
        return problemCount;
    }

    public void setProblemCount(int problemCount) {
        this.problemCount = problemCount;
    }
}
