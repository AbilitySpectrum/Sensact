package lyricom.sensactConfig.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A singleton class which holds, organizes and controls TV control
 * information.
 * This is where all the action happens!
 * 
 * HOW TV IR CODES WORK.
 * The init() function of this class creates the definitions for all the
 * supported TVs and their codes.  Only the bottom 24 bits of the codes are
 * defined here for reasons that are clarified below.
 * 
 * In the Java code there are IR action codes defined in Model.java.  These
 * are used in ActionUI.java to define the combo boxes which allow the user
 * to make a selection.  When a selection is made the IR action code is
 * stored in the trigger's action parameter.
 * 
 * The UI also contains a tabbed panel which allows the user to select the
 * TV and set-top box which are to be supported.  The TV definitions can be 
 * changed without effecting the IR-related trigger definitions and visa-versa.
 * The current TV selections are store in this TVInfo class.
 * 
 * On the Leonardo side there is a table, indexed by the TV ID, which 
 * contains the IRLib protocol ID, the number of bits, the khz value and 
 * the MSB of all the codes for that TV.
 * 
 * For most triggers, when the user sends out the trigger (to Sensact or
 * via Export) the trigger data is written.  For IR the process is a little
 * more difficult.  For IR triggers a call is made to TVInfo.ID2Code().  This 
 * takes the IR action code (from the trigger) and the TV ID (set in the
 * "TV Selection" panel and stored in TVInfo) and uses this to find the actual 
 * code needed for that TV to perform the required action.
 * 
 * The TV ID is placed in the MSB of the trigger's action parameter and the 
 * lower 24 bits of the needed IR code are placed in the rest.  
 * 
 * When the trigger is executed on the Sensact, the TV ID and IR code are 
 * extracted from the action parameter.  Then the TV ID is used to get the 
 * required protocol, bits and khz values for the TV.  Also the value needed 
 * for the MSB of the code is accessed and the full 32-bit code is 
 * generated.
 * 
 * This works because the high-order bits of the IR codes tend to be a
 * manufacture's ID.  This means that the MSB is usually the same for all
 * the codes for a particular device.  This may prove to be untrue for some
 * TVs ... we will deal with that when we get there.
 * 
 * When triggers are read (via Get from Sensact or via Import) a reverse
 * process sets the action ID for IR-related triggers and sets the TV type.
 * 
 * This solution has many benefits:
 *   . There is no additional space consumed in the EEPROM
 *   . There is no extension to the existing data transfer protocol
 *   . There is minimal extra code space consumed in Sensact.  
 *     There is a small data table in flash memory.
 *     A bit of space is consumed by turning on some IRLib .h files in order
 *     to support some new tvs.
 *   . The solution supports volume control via one device with channel 
 *     control via a different device.
 *   . The solution supports independent manipulation of TV type and 
 *     IR-related triggers, by delaying the actual generation of the codes 
 *     until transmission time.
 * 
 * @author Andrew
 */
public class TVInfo {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");

    // TV ID codes.  These are used to map into an array of TV information
    // on the Leonardo.  They are NOT the IRLib TV protocol codes.
    // *** These values must match index locations used in the arduino code.
    public static final int RCA_TV     = 0;
    public static final int SAMSUNG_TV = 1;
    public static final int LG_TV      = 2;
    public static final int ROGERS_BOX = 3;
    public static final int BELL_BOX   = 4;
            
    // Singleton Pattern
    private static TVInfo instance = null;
    public static TVInfo getInstance() {
        if (instance == null) {
            instance = new TVInfo();
        }
        return instance;
    }
    
    private List<TVType> tvTypes = new ArrayList<>();
    private TVType currentVolumeControl;
    private TVType currentChannelControl;
    private TVTypeUI ui;    // Link to the user interface controls.
    
    private TVInfo() {}
    
    // Initialize TV information.
    // All definitions of TV codes and data are held here.
    public void init() {
        // RCA TV Codes - prefix is 00
        TVCodeMap map = new TVCodeMap();
        map.addMapping(Model.IR_TV_ON_OFF, 0xf2a0d5);
        map.addMapping(Model.IR_BOX_ON_OFF, 0xf2a0d5);
        map.addMapping(Model.IR_VOLUME_UP, 0xf2f0d0);
        map.addMapping(Model.IR_VOLUME_DOWN, 0xf2e0d1);
        map.addMapping(Model.IR_MUTE, 0xf3f0c0);
        map.addMapping(Model.IR_CHANNEL_UP, 0xf2d0d2);
        map.addMapping(Model.IR_CHANNEL_DOWN, 0xf2c0d3);
        map.addMapping(Model.IR_DIGIT_0, 0xf300cf);
        map.addMapping(Model.IR_DIGIT_1, 0xf310ce);
        map.addMapping(Model.IR_DIGIT_2, 0xf320cd);
        map.addMapping(Model.IR_DIGIT_3, 0xf330cc);
        map.addMapping(Model.IR_DIGIT_4, 0xf340cb);
        map.addMapping(Model.IR_DIGIT_5, 0xf350ca);
        map.addMapping(Model.IR_DIGIT_6, 0xf360c9);
        map.addMapping(Model.IR_DIGIT_7, 0xf370c8);
        map.addMapping(Model.IR_DIGIT_8, 0xf380c7);
        map.addMapping(Model.IR_DIGIT_9, 0xf390c6);
        tvTypes.add(new TVType(RES.getString("TV_RCA"), RCA_TV, map, true));
        
        // Samsung TV Codes - prefix is xE0
        map = new TVCodeMap();
        map.addMapping(Model.IR_TV_ON_OFF, 0xE040BF);
        map.addMapping(Model.IR_BOX_ON_OFF, 0xE040BF);
        map.addMapping(Model.IR_VOLUME_UP, 0xE0E01F);
        map.addMapping(Model.IR_VOLUME_DOWN, 0xE0D02F);
        map.addMapping(Model.IR_MUTE, 0xE0F00F);
        map.addMapping(Model.IR_CHANNEL_UP, 0xE048B7);
        map.addMapping(Model.IR_CHANNEL_DOWN, 0xE008F7);
        map.addMapping(Model.IR_DIGIT_0, 0xE08877);
        map.addMapping(Model.IR_DIGIT_1, 0xE020DF);
        map.addMapping(Model.IR_DIGIT_2, 0xE0A05F);
        map.addMapping(Model.IR_DIGIT_3, 0xE0609F);
        map.addMapping(Model.IR_DIGIT_4, 0xE010EF);
        map.addMapping(Model.IR_DIGIT_5, 0xE0906F);
        map.addMapping(Model.IR_DIGIT_6, 0xE050AF);
        map.addMapping(Model.IR_DIGIT_7, 0xE030CF);
        map.addMapping(Model.IR_DIGIT_8, 0xE0B04F);
        map.addMapping(Model.IR_DIGIT_9, 0xE0708F);
        tvTypes.add(new TVType(RES.getString("TV_SAMSUNG"), SAMSUNG_TV, map, true));
        
        // LG TV Codes - prefix is x20
        map = new TVCodeMap();
        map.addMapping(Model.IR_TV_ON_OFF, 0xDF10EF);
        map.addMapping(Model.IR_BOX_ON_OFF, 0xDF10EF);
        map.addMapping(Model.IR_VOLUME_UP, 0xDF40BF);
        map.addMapping(Model.IR_VOLUME_DOWN, 0xDFC03F);
        map.addMapping(Model.IR_MUTE, 0xDF906F);
        map.addMapping(Model.IR_CHANNEL_UP, 0xDF00FF);
        map.addMapping(Model.IR_CHANNEL_DOWN, 0xDF807F);
        map.addMapping(Model.IR_DIGIT_0, 0xDF08F7);
        map.addMapping(Model.IR_DIGIT_1, 0xDF8877);
        map.addMapping(Model.IR_DIGIT_2, 0xDF48B7);
        map.addMapping(Model.IR_DIGIT_3, 0xDFC837);
        map.addMapping(Model.IR_DIGIT_4, 0xDF28D7);
        map.addMapping(Model.IR_DIGIT_5, 0xDFA857);
        map.addMapping(Model.IR_DIGIT_6, 0xDF6897);
        map.addMapping(Model.IR_DIGIT_7, 0xDFE817);
        map.addMapping(Model.IR_DIGIT_8, 0xDF18E7);
        map.addMapping(Model.IR_DIGIT_9, 0xDF9867);
        tvTypes.add(new TVType(RES.getString("TV_LG"), LG_TV, map, true));
        
        // Bell Fibe - prefix is x25
        map = new TVCodeMap();
        map.addMapping(Model.IR_TV_ON_OFF, 0x00260C);
        map.addMapping(Model.IR_BOX_ON_OFF, 0x00260C);
        map.addMapping(Model.IR_VOLUME_UP, 0x002610);
        map.addMapping(Model.IR_VOLUME_DOWN, 0x002611);
        map.addMapping(Model.IR_MUTE, 0x00260D);
        map.addMapping(Model.IR_CHANNEL_UP, 0x002620);
        map.addMapping(Model.IR_CHANNEL_DOWN, 0x002621);
        map.addMapping(Model.IR_DIGIT_0, 0x002600);
        map.addMapping(Model.IR_DIGIT_1, 0x002601);
        map.addMapping(Model.IR_DIGIT_2, 0x002602);
        map.addMapping(Model.IR_DIGIT_3, 0x002603);
        map.addMapping(Model.IR_DIGIT_4, 0x002604);
        map.addMapping(Model.IR_DIGIT_5, 0x002605);
        map.addMapping(Model.IR_DIGIT_6, 0x002606);
        map.addMapping(Model.IR_DIGIT_7, 0x002607);
        map.addMapping(Model.IR_DIGIT_8, 0x002608);
        map.addMapping(Model.IR_DIGIT_9, 0x002609);
        tvTypes.add(new TVType(RES.getString("TV_BELL_BOX"), BELL_BOX, map, true));

        // Rogers
        map = new TVCodeMap();
        map.addMapping(Model.IR_BOX_ON_OFF, 0x37C107);
        map.addMapping(Model.IR_CHANNEL_UP, 0x377111);
        map.addMapping(Model.IR_CHANNEL_DOWN, 0x36F121);
        map.addMapping(Model.IR_DIGIT_0, 0x373119);
        map.addMapping(Model.IR_DIGIT_1, 0x36113D);
        map.addMapping(Model.IR_DIGIT_2, 0x37111D);
        map.addMapping(Model.IR_DIGIT_3, 0x36912D);
        map.addMapping(Model.IR_DIGIT_4, 0x37910D);
        map.addMapping(Model.IR_DIGIT_5, 0x365135);
        map.addMapping(Model.IR_DIGIT_6, 0x375115);
        map.addMapping(Model.IR_DIGIT_7, 0x36D125);
        map.addMapping(Model.IR_DIGIT_8, 0x37D105);
        map.addMapping(Model.IR_DIGIT_9, 0x363139);
        tvTypes.add(new TVType(RES.getString("TV_ROGERS_BOX"), ROGERS_BOX, map, false));
    }
    
    public List<TVType> getTVTypes() {
        return tvTypes;
    }
    
    public TVType getTVTypeByID(int id) {
        for(TVType t: tvTypes) {
            if (t.getTVId() == id) return t;
        }
        return null;
    }
    
    // set*Control functions are called from the UI when the user
    // changed the selected TV type.
    public void setVolumeControl(TVType type, TVTypeUI ui) {
        currentVolumeControl = type;
        this.ui = ui;
    }
    
    public void setChannelControl(TVType type, TVTypeUI ui) {
        currentChannelControl = type;
        this.ui = ui;
    }
    
    // Called when a trigger is being sent out.  Uses the TV type and
    // the action ID to generate the appropriate IR codes.
    public int ID2Code(int actionID) {
        int tvid;
        int irCode;
        if (actionID < 100) { // Volume Control
            tvid = currentVolumeControl.getTVId();
            irCode = currentVolumeControl.getMap().getIRCode(actionID);
        } else {  // Channel Control
            tvid = currentChannelControl.getTVId();
            irCode = currentChannelControl.getMap().getIRCode(actionID);
            
        }
        return (tvid << 24) | irCode;
    }
    
    // Called when receiving a trigger.  Turns the IR code in the trigger
    // into an tv type and an action id.
    // This code informs the UI of the TV code selection found in the
    // trigger data.
    public int Code2ID(int code) {
        int tvid = (code & 0xFF000000) >> 24;
        int irCode = code & 0xFFFFFF;
        
        TVType type = getTVTypeByID(tvid);
        if (type == null) {
            // Invalid code
            return 0;   
        }   
       
        int actionID = type.getMap().getActionID(irCode);
        if (actionID == 0) {
            // Invalid code
            return 0;
        } else if (actionID < 100) {   // Volume action
            ui.changeVolumeControl(type);
        } else {                // Channel control
            ui.changeChannelControl(type);
        }
        
        return actionID;
    }
}
