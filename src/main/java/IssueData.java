import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Dmitrii A. on 06.09.16.
 */
public class IssueData implements Serializable {
    private Long id;
    private String subject;
    private ItemData status;
    @SerializedName("assigned_to") private ItemData assignedTo;

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

    public ItemData getStatus() {
        return status;
    }

    public void setStatus(ItemData status) {
        this.status = status;
    }

    public ItemData getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(ItemData assignedTo) {
        this.assignedTo = assignedTo;
    }

    @Override
    public String toString()
    {
        return "Issue {" + this.id + " - " + this.subject + '}';
    }
}
