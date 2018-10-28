package lyricom.sensactConfig.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class SensorGroup {
    private final String name;
    private final List<Sensor> members = new ArrayList<>();
    
    public SensorGroup(String name) {
        this.name = name;
    }
    
    public void add(Sensor s) {
        members.add(s);
    }
    
    public String getName() {
        return name;
    }
    
    public List<Sensor> getMembers() {
        return members;
    }
}
