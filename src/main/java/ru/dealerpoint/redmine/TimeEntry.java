package ru.dealerpoint.redmine;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TimeEntry implements Serializable {
    private Long id;
    private Float hours;
    private String comments;
    private Issue issue;
    @SerializedName("activity_id") private Long activityId;

    public Float getHours() {
        return hours;
    }

    public void setHours(Float hours) {
        this.hours = hours;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }
}
