package ru.dealerpoint.redmine;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Session implements Serializable {
    private String version;
    @SerializedName("tta_session") private String tta_session;
    @SerializedName("user_name") private String userName;

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
        return tta_session;
    }

    public void setTtaSisseon(String tta_session) {
        this.tta_session = tta_session;
    }
}
