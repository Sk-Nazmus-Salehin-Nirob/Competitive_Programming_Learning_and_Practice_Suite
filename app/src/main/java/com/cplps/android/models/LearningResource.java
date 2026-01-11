package com.cplps.android.models;

public class LearningResource {
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_FILE = "file";

    private int id;
    private int topicId;
    private String type;
    private String content; // text or file path
    private String name; // display name for files
    private long createdAt;

    public LearningResource() {
    }

    public LearningResource(int id, int topicId, String type, String content, String name, long createdAt) {
        this.id = id;
        this.topicId = topicId;
        this.type = type;
        this.content = content;
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
