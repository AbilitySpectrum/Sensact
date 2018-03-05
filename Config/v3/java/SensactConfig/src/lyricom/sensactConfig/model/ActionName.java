package lyricom.sensactConfig.model;

/**
 * The ActionName is used to provide a string name to each action
 * and is also the key needed to retrieve an action by name.
 * @author Andrew
 */
public enum ActionName {
    NONE("Nothing"),
    RELAY_A("Relay A"),
    RELAY_B("Relay B"),
    BT_KEYBOARD("BT Keyboard"),
    BT_SPECIAL("BT Special"),
    BT_MOUSE("BT Mouse"),
    HID_KEYBOARD("HID Keyboard"),
    HID_SPECIAL("HID Special"),
    HID_MOUSE("HID Mouse"),
    BUZZER("Buzzer"),
    IR("IR"),
    SERIAL("Serial"),
    SET_STATE("Set State"),
    LIGHT_BOX("Light Box");
    
    private final String id;
    ActionName(String id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return id;
    }
}