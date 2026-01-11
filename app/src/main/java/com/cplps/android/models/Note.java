package com.cplps.android.models;

public class Note {
    private int noteId;
    private int userId;
    private String content;
    private long createdAt;

    public Note() {
    }

    public Note(int noteId, int userId, String content, long createdAt) {
        this.noteId = noteId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
