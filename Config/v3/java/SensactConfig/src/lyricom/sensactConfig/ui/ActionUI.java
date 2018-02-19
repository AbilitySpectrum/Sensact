package lyricom.sensactConfig.ui;

import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.widgets.*;


/**
 * User interfaces for unique parameters associated with 
 * particular actions.
 * 
 * @author Andrew
 */
public class ActionUI {
    
    public static final ActionUI NONE           = new ActionUI();
    public static final ActionUI KEY_OPTION     = new KeyOptionUI();
    public static final ActionUI MOUSE_OPTION   = new MouseOptionUI();
    public static final ActionUI HID_SPECIAL    = new HIDSpecialUI();
    public static final ActionUI BT_SPECIAL     = new BTSpecialUI();
    public static final ActionUI BUZZER         = new BuzzerUI();
    public static final ActionUI IR_OPTION      = new IRActionUI();
    public static final ActionUI SET_STATE      = new SetStateUI();
        
    // ----------------------------------------
    // Class and sub-class definition.
    private ActionUI() {
    }
    
    public W_Base createUI(Trigger t) {
        return new W_Base(); // Default. 
    }
        
    // ----------------------------------
    // KeyOptionUI - UI for components that take an ascii value.
    public static class KeyOptionUI extends ActionUI {        
        @Override
        public W_Base createUI(Trigger t) {
            return new WT_KeyOption("Character:", t);
        }
    } 

    // ----------------------------------
    // MouseOptionUI - UI for components that take mouse motions.
    
    static final ValueLabelPair MouseActions[] = {
	new ValueLabelPair(Model.MOUSE_UP,      "Mouse Up", true),
	new ValueLabelPair(Model.MOUSE_DOWN,    "Mouse Down", true),
	new ValueLabelPair(Model.MOUSE_LEFT,    "Mouse Left", true),
	new ValueLabelPair(Model.MOUSE_RIGHT,   "Mouse Right", true),
	new ValueLabelPair(Model.MOUSE_CLICK,   "Mouse Click"),
	new ValueLabelPair(Model.MOUSE_RIGHT_CLICK, "Mouse Right Click"),
	new ValueLabelPair(Model.MOUSE_PRESS,   "Mouse Press"),
	new ValueLabelPair(Model.MOUSE_RELEASE, "Mouse Release"),
	new ValueLabelPair(Model.NUDGE_UP,      "Nudge Up"),
	new ValueLabelPair(Model.NUDGE_DOWN,    "Nudge Down"),
	new ValueLabelPair(Model.NUDGE_LEFT,    "Nudge Left"),
	new ValueLabelPair(Model.NUDGE_RIGHT,   "Nudge Right"),
	new ValueLabelPair(Model.NUDGE_STOP,    "Nudge Stop")       
    };

    public static class MouseOptionUI extends ActionUI {    
        @Override
         public W_Base createUI(Trigger t) {
             W_Base option = new WT_ValueLabelOption("Mouse Action:", t, MouseActions);
             return option;
        }         
    } 

    // ----------------------------------
    // HIDSpecialUI - UI for components that taking HID special keys.
    
    // HID special key values - defined by HID standard.
    static final ValueLabelPair HID_Keys[] = {
        new ValueLabelPair( 0xDA, "UP ARROW" ),
        new ValueLabelPair( 0xD9, "DOWN ARROW" ),
        new ValueLabelPair( 0xD8, "LEFT ARROW" ),
        new ValueLabelPair( 0xD7, "RIGHT ARROW" ),
        new ValueLabelPair( 0xB2, "BACKSPACE" ),
        new ValueLabelPair( 0xB3, "TAB" ),
        new ValueLabelPair( 0xB0, "RETURN" ),
        new ValueLabelPair( 0xB1, "ESC" ),
        new ValueLabelPair( 0xD1, "INSERT" ),
        new ValueLabelPair( 0xD4, "DELETE" ),
        new ValueLabelPair( 0xD3, "PAGE UP" ),
        new ValueLabelPair( 0xD6, "PAGE DOWN" ),
        new ValueLabelPair( 0xD2, "HOME" ),
        new ValueLabelPair( 0xD5, "END" )
    };
   
    
    public static class HIDSpecialUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             W_Base option = new WT_ValueLabelOption("Key:", t, HID_Keys);
             return option;
        }
    } 

    // ----------------------------------
    // BTSpecialUI - UI for components that taking BT special keys
    
    // BT special key values - defined by BT standard.
    static final ValueLabelPair BT_Keys[] = {
        new ValueLabelPair( 14, "UP ARROW" ),
        new ValueLabelPair( 12, "DOWN ARROW" ),
        new ValueLabelPair( 11, "LEFT ARROW" ),
        new ValueLabelPair( 7, "RIGHT ARROW" ),
        new ValueLabelPair( 8, "BACKSPACE" ),
        new ValueLabelPair( 9, "TAB" ),
        new ValueLabelPair( 10, "RETURN" ),
        new ValueLabelPair( 27, "ESC" ),
        new ValueLabelPair( 1, "INSERT" ),
        new ValueLabelPair( 4, "DELETE" ),
        new ValueLabelPair( 3, "PAGE UP" ),
        new ValueLabelPair( 6, "PAGE DOWN" ),
        new ValueLabelPair( 2, "HOME" ),
        new ValueLabelPair( 5, "END" )
    };
    
    public static class BTSpecialUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             W_Base option = new WT_ValueLabelOption("Key:", t, BT_Keys);
             return option;
        }
    } 
    
    ValueLabelPair IRActions[] = {
        new ValueLabelPair(Model.TV_ON_OFF,     "On/Off"),
        new ValueLabelPair(Model.VOLUME_UP,     "Volume Up"),
        new ValueLabelPair(Model.VOLUME_DOWN,   "Volume Down"),
        new ValueLabelPair(Model.CHANNEL_UP,    "Channel Up"),
        new ValueLabelPair(Model.CHANNEL_DOWN,  "Channel Down")
    };
    
    public static class IRActionUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             W_Composite comp = new W_Composite();
             W_Base irOption = 
                     new WT_ValueLabelOption("IR Action:", t, IRActions, false);
             W_Base repeat = new WT_Repeat("Repeat:", t);
             
             comp.addPart(irOption);
             comp.addPart(repeat);
             return comp;
        }
    } 

    public static class BuzzerUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
            W_Composite comp = new W_Composite();
            comp.addPart( new WT_Frequency("Frequency:", t));
            comp.addPart( new WT_Duration("Duration:", t));
            return comp;
         }
    } 
    
    public static class SetStateUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             W_Composite comp = new W_Composite();
             comp.addPart(new WT_Sensor("Sensor:", t));
             comp.addPart(new WT_StateSpinner("State:", t));
             return comp;
         }
    }
}
    

