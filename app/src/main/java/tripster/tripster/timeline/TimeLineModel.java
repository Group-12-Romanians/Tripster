package tripster.tripster.timeline;

import java.io.Serializable;

/**
 * Created by HP-HP on 05-12-2015.
 */
public class TimeLineModel implements Serializable {
    //TODO: Don't need this. Use Event instead.
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
