package ru.dealerpoint.redmine;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Issue implements Serializable {
    private Long id;
    private String subject;
    private Item status;
    private Item author;
    @SerializedName("estimated_hours") private Float estimatedHours;
    @SerializedName("spent_hours") private Float spentHours;
    @SerializedName("assigned_to") private Item assignedTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Item getStatus() {
        return status;
    }

    public void setStatus(Item status) {
        this.status = status;
    }

    public Item getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Item assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Float getEstimatedHours() {
        return estimatedHours == null ? 0 : estimatedHours;
    }

    public void setEstimatedHours(Float estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Float getSpentHours() {
        return spentHours == null ? 0 : spentHours;
    }

    public void setSpentHours(Float spentHours) {
        this.spentHours = spentHours;
    }

    public Item getAuthor() {
        return author;
    }

    public void setAuthor(Item author) {
        this.author = author;
    }

    @Override
    public String toString()
    {
        return "Issue {" + this.id + " - " + this.subject + '}';
    }
}
