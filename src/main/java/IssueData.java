import java.io.Serializable;

/**
 * Created by Dmitrii A. on 06.09.16.
 */
public class IssueData implements Serializable {
    private Long id;
    private String subject;

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

    @Override
    public String toString()
    {
        return "Issue {" + this.id + " - " + this.subject + '}';
    }
}
