package lyricom.sensactConfig.model;

/**
 * The ActionName is used to provide a string name to each action
 * and is also the key needed to retrieve an action by name.
 * @author Andrew
 */
public enum ActionName {
    NONE,
    RELAY_A,
    RELAY_B,
    BT_KEYBOARD,
    BT_SPECIAL,
    BT_MOUSE,
    HID_KEYBOARD,
    HID_SPECIAL,
    HID_KEYPRESS,
    HID_KEYRELEASE,
    HID_MOUSE,
    BUZZER,
    IR,
    SERIAL,
    SET_STATE,
    LIGHT_BOX;

    
    private final String localizedName;
    ActionName() {
        localizedName = MRes.getStr(this.name());
    }
    
    @Override
    public String toString() {
        return localizedName;
    }
 
}