package ru.dealerpoint.redmine;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Session implements Serializable {
    private String version;
    @SerializedName("tta_session") private String ttaSession;
    @SerializedName("user_name") private String userName;
    @SerializedName("day_starting_at") private int dayStartingAt;
    @SerializedName("day_ending_in") private int dayEndingIn;
    @SerializedName("lunch_starting_at") private int lunchStartingAt;
    @SerializedName("lunch_ending_in") private int lunchEndingIn;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTtaSession() {
        return ttaSession;
    }

    public int getDayEndingIn() {
        return dayEndingIn;
    }

    public void setDayEndingIn(int dayEndingIn) {
        this.dayEndingIn = dayEndingIn;
    }

    public int getDayStartingAt() {
        return dayStartingAt;
    }

    public void setDayStartingAt(int dayStartingAt) {
        this.dayStartingAt = dayStartingAt;
    }

    public int getLunchEndingIn() {
        return lunchEndingIn;
    }

    public void setLunchEndingIn(int lunchEndingIn) {
        this.lunchEndingIn = lunchEndingIn;
    }

    public int getLunchStartingAt() {
        return lunchStartingAt;
    }

    public void setLunchStartingAt(int lunchStartingAt) {
        this.lunchStartingAt = lunchStartingAt;
    }

    public void setTtaSession(String ttaSession) {
        this.ttaSession = ttaSession;
    }
}
