package lyricom.sensactConfig.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lyricom.sensactConfig.ui.ActionUI;

/**
 * Static constant values.
 * Static method for accessing the model.
 * Values here match values used in the Sensact.
 * @author Andrew
 */
public class Model {
    // Commands sent to sensact
    public static final Byte CMD_VERSION        = (byte) 'V';
    public static final Byte CMD_RUN            = (byte) 'R';
    public static final Byte CMD_DISPLAY        = (byte) 'Q';
    public static final Byte CMD_GET_TRIGGERS   = (byte) 'U';
    public static final Byte START_OF_DATA      = (byte) 'S';
    public static final Byte START_OF_TRIGGERS  = (byte) 'T';
    public static final Byte MOUSE_SPEED        = (byte) 'Y';
    public static final Byte END_OF_BLOCK       = (byte) 'Z';
    
    // Communication protocol constants
    static final byte NUMBER_MASK = 0x60;
    static final byte ID_MASK = 0x40;
    static final byte CONDITION_MASK = '0';
    static final byte BOOL_TRUE = 'p';
    static final byte BOOL_FALSE = 'q';
    static final byte TRIGGER_MASK = '0';

    // Mouse Action values
    static public final int MOUSE_UP = 1;
    static public final int MOUSE_DOWN = 2;
    static public final int MOUSE_LEFT = 3;
    static public final int MOUSE_RIGHT = 4;
    static public final int MOUSE_CLICK = 5;
    static public final int MOUSE_PRESS = 6;
    static public final int MOUSE_RELEASE = 7;
    static public final int MOUSE_RIGHT_CLICK = 8;
    static public final int NUDGE_UP = 10;
    static public final int NUDGE_DOWN = 11;
    static public final int NUDGE_LEFT = 12;
    static public final int NUDGE_RIGHT = 13;
    static public final int NUDGE_STOP = 14;
    
    // IR TV Control values	
    static public final int TV_ON_OFF = 1;
    static public final int VOLUME_UP = 2;
    static public final int VOLUME_DOWN = 3;
    static public final int CHANNEL_UP = 4;
    static public final int CHANNEL_DOWN = 5;

    // Lists of Sensors and Actions.
    // Created once by initModel.
    public static List<Sensor> sensorList;
    public static List<SensorGroup> sensorGroups;
    public static List<SaAction> actionList;
    public static Map<String,SaAction> actionMap;
    
    public static void initModel(int versionID) {
        initSensorList(versionID);
        initActionList(versionID);
    }
    
    // Define sensors and sensor groupings.
    private static void initSensorList(int versionID) {
        sensorList = new ArrayList<>();
        sensorGroups = new ArrayList<>();
        
        SensorGroup grp = new SensorGroup("Input 1");
        grp.add( new Sensor(1, "Input 1A", 0, 1023, true) );
        grp.add( new Sensor(2, "Input 1B", 0, 1023, true) );
        sensorGroups.add(grp);

        grp = new SensorGroup("Input 2");
        grp.add( new Sensor(3, "Input 2A", 0, 1023, true) );
        grp.add( new Sensor(4, "Input 2B", 0, 1023, true) );
        sensorGroups.add(grp);
        
        grp = new SensorGroup("Input 3");
        grp.add( new Sensor(5, "Input 3A", 0, 1023, true) );
        grp.add( new Sensor(6, "Input 3B", 0, 1023, true) );
        sensorGroups.add(grp);
        
        grp = new SensorGroup("Accel");
        grp.add( new Sensor(8,  "Accel-X", -16000, 16000, true) );
        grp.add( new Sensor(9,  "Accel-Y", -16000, 16000, true) );
        grp.add( new Sensor(10, "Accel-Z", -16000, 16000, true) );
        sensorGroups.add(grp);

	/*
	* Gyro values go from -32768 to + 32767, but the high values
	* are for very violent motions.  
	* Here we chop off the highest values to improve sensitivity 
	* for the smaller motions.
	* Simlarly the "Any Motion sensor will deliver values from
	* 0 to 28,377 but we chop off the most violent motions.
        */
        grp = new SensorGroup("Gyro");
        grp.add( new Sensor(11, "Gyro-X", -15000, 15000, true) );
        grp.add( new Sensor(12, "Gyro-Y", -15000, 15000, true) );
        grp.add( new Sensor(13, "Gyro-Z", -15000, 15000, true) );
        if (versionID >= 401) {
            grp.add( new Sensor(14, "Any Motion", 0, 13000, true) );            
        }
        sensorGroups.add(grp);

        grp = new SensorGroup("USB Port");
        grp.add( new Sensor(7, "USB Input", 0, 255, false) );
        sensorGroups.add(grp);
        
        // Create a single list of all sensors
        for (SensorGroup g: sensorGroups) {
            for (Sensor s: g.getMembers() ) {
                sensorList.add(s);
            }
        }
    }
    
    // Define possible Actions.
    // Order is important.  This will be the order in the combo box.
    private static void initActionList(int versionID) {
        actionList = new ArrayList<>();
        actionMap = new HashMap<>();
        
        actionList.add(new SaAction(0, "None",    0, ActionUI.NONE, null));
        actionList.add(new SaAction(1, "Relay A", 0, ActionUI.NONE, null));
        actionList.add(new SaAction(2, "Relay B", 0, ActionUI.NONE, null));
        
        actionList.add(new SaAction(3, "BT Keyboard", 65, ActionUI.KEY_OPTION, (p) -> p >= 32));
        actionList.add(new SaAction(3, "BT Special",  10, ActionUI.BT_SPECIAL, (p) -> p < 32));
        
        actionList.add(new SaAction(9, "BT Mouse", MOUSE_UP, ActionUI.MOUSE_OPTION, null));
        actionList.add(new SaAction(4, "HID Keyboard",  65,  ActionUI.KEY_OPTION, 
                (p) -> !((0x100 > p) && (p > 0x7f))));
        actionList.add(new SaAction(4, "HID Special", 0xB0,  ActionUI.HID_SPECIAL, 
                (p) ->  ((0x100 > p) && (p > 0x7f))));
        actionList.add(new SaAction(5, "HID Mouse", MOUSE_UP,       ActionUI.MOUSE_OPTION, null));
        actionList.add(new SaAction(7, "Buzzer", (400 << 16) + 250, ActionUI.BUZZER,       null));
        actionList.add(new SaAction(8, "IR", TV_ON_OFF,             ActionUI.IR_OPTION,    null));
        if (versionID >= 400) {
            actionList.add(new SaAction(6, "Serial", 65, ActionUI.KEY_OPTION, null));
        }
        actionList.add(new SaAction(10, "Set State", 0x101, ActionUI.SET_STATE, null));
        
        if (versionID >= 402) {
            actionList.add(new SaAction(11, "Light Box", 0, ActionUI.LIGHT_BOX, null));
        }
        
        // Create a map of actions for lookup-by-name
        for (SaAction a: actionList) {
            actionMap.put(a.getName(), a);
        }
    }
    
    // ------------------------------------
    // Methods for accessing Sensors and Actions.
    //
    
    public static List<SensorGroup> getSensorGroups() {
        return sensorGroups;
    }
    
    public static Sensor getSensorByID(int id) {
        for (Sensor s: sensorList) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }
    
    /*
     * getActionByID uses the parameter passed to determine whether
     * the BT or HID action is 'Keyboard' or 'Special'.
    */
    public static SaAction getActionByID(int id, int param) {
        for (SaAction a: actionList) {
            if (a.getId() == id) {
                if (a.doParameterCheck(param)) {
                    return a;
                }
            }
        }
        return null;
    }
    
    public static SaAction getActionByName(String name) {
        return actionMap.get(name);
    }
    
    public static List<SaAction> getActionList() {
        return actionList;
    }
    
    // This is called when sensor data is received.
    public static void updateSensorValues(InStream in) throws IOError {
        if (in.getChar() != START_OF_DATA) {
            throw new IOError("Invalid start of sensor data");
        }
        
        int sensorCount = in.getNum(2);
        for(int i=0; i<sensorCount; i++) {
            int id = in.getID(2);
            int value = in.getNum(2);
            Sensor s = getSensorByID(id);
            if (s != null) {
                s.setCurrentValue(value);
            } else {
                System.out.print("No sensor with id ");
                System.out.println(id);
            }
        }       
    }
}
