import java.io.Serializable;

/**
 * Created by Dmitrii A. on 07.09.16.
 */
public class ItemData implements Serializable {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Item {" + this.id + " - " + this.name + '}';
    }
}
