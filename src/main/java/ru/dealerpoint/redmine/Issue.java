package ru.dealerpoint.redmine;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Issue implements Serializable {
    private Long id;
    private String subject;
    private Item status;
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

    @Override
    public String toString()
    {
        return "Issue {" + this.id + " - " + this.subject + '}';
    }
}
