package com.cplps.android.api.models;

import com.google.gson.annotations.SerializedName;

public class CFAuthor {
    @SerializedName("contestId")
    private int contestId;

    @SerializedName("participantType")
    private String participantType;

    public int getContestId() {
        return contestId;
    }

    public String getParticipantType() {
        return participantType;
    }
}
