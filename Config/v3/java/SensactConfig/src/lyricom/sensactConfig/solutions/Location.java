package lyricom.sensactConfig.solutions;

import lyricom.sensactConfig.model.Sensor;
import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class Location {
    Sensor sensor;
    int value;
    int condition;
    Trigger.Level level;
    
    Location getReverse() {
        Location l = new Location();
        l.sensor = sensor;
        l.value = value;
        l.level = level;
        if (condition == Trigger.TRIGGER_ON_HIGH) {
            l.condition = Trigger.TRIGGER_ON_LOW;
        } else {
            l.condition = Trigger.TRIGGER_ON_HIGH;
        }
        return l;
    }
}
