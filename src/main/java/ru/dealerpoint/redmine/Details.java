package ru.dealerpoint.redmine;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Details implements Serializable {
    private String version;
    private Float today;
    @SerializedName("user_name") private String userName;
    @SerializedName("this_week") private Float thisWeek;
    @SerializedName("this_month") private Float thisMonth;

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

    public Float getToday() {
        return today;
    }

    public void setToday(Float today) {
        this.today = today;
    }

    public Float getThisWeek() {
        return thisWeek;
    }

    public void setThisWeek(Float thisWeek) {
        this.thisWeek = thisWeek;
    }

    public Float getThisMonth() {
        return thisMonth;
    }

    public void setThisMonth(Float thisMonth) {
        this.thisMonth = thisMonth;
    }
}
