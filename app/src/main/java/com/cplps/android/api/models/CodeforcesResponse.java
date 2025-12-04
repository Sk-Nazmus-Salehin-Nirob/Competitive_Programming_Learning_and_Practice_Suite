package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Codeforces API response wrapper
public class CodeforcesResponse<T> {
    @SerializedName("status")
    private String status;

    @SerializedName("result")
    private T result;

    @SerializedName("comment")
    private String comment;

    public String getStatus() {
        return status;
    }

    public T getResult() {
        return result;
    }

    public String getComment() {
        return comment;
    }

    public boolean isSuccess() {
        return "OK".equals(status);
    }
}
