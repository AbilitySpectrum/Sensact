/*
 * This file is part of the Sensact Configuration software.
 *
 * Sensact Configuration software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact Configuration software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom.sensactConfig.ui;

import java.util.ResourceBundle;
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
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");
    
    public static final ActionUI NONE           = new ActionUI();
    public static final ActionUI KEY_OPTION     = new KeyOptionUI();
    public static final ActionUI RELAY_OPTION   = new RelayOptionUI();
    public static final ActionUI MOUSE_OPTION   = new MouseOptionUI();
    public static final ActionUI HID_SPECIAL    = new HIDSpecialUI();
    public static final ActionUI HID_KEYPRESS   = new HIDKeyPress();
    public static final ActionUI HID_KEYRELEASE = new HIDKeyRelease();
    public static final ActionUI BT_SPECIAL     = new BTSpecialUI();
    public static final ActionUI BUZZER         = new BuzzerUI();
    public static final ActionUI IR_OPTION      = new IRActionUI();
    public static final ActionUI SET_STATE      = new SetStateUI();
    public static final ActionUI LIGHT_BOX      = new LightBoxUI();
    public static final ActionUI LCD_DISPLAY     = new LCDDisplayUI();
        
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
            return new WT_KeyOption(RES.getString("ACT_CHAR_LABEL"), t);
        }
    } 
    
    // ----------------------------------
    // RelayOptionUI - UI for the relay - starting with V4.3
    static final ValueLabelPair RelayActions[] = {
        new ValueLabelPair(Model.RELAY_PULSE, RES.getString("ACT_RELAY_DD_PULSE")),
        new ValueLabelPair(Model.RELAY_ON, RES.getString("ACT_RELAY_DD_ON")),
        new ValueLabelPair(Model.RELAY_OFF, RES.getString("ACT_RELAY_DD_OFF")),
    };
    
    public static class RelayOptionUI extends ActionUI {    
        @Override
         public W_Base createUI(Trigger t) {
             W_Base option = new WT_ValueLabelOption(
                     RES.getString("ACT_RELAY_LABEL"), t, RelayActions);
             return option;
        }         
    }     

    // ----------------------------------
    // MouseOptionUI - UI for components that take mouse motions.
    
    static final ValueLabelPair MouseActions[] = {
	new ValueLabelPair(Model.MOUSE_UP,      RES.getString("ACT_MOUSE_DD_UP"), true),
	new ValueLabelPair(Model.MOUSE_DOWN,    RES.getString("ACT_MOUSE_DD_DOWN"), true),
	new ValueLabelPair(Model.MOUSE_LEFT,    RES.getString("ACT_MOUSE_DD_LEFT"), true),
	new ValueLabelPair(Model.MOUSE_RIGHT,   RES.getString("ACT_MOUSE_DD_RIGHT"), true),
	new ValueLabelPair(Model.MOUSE_CLICK,   RES.getString("ACT_MOUSE_DD_CLICK")),
	new ValueLabelPair(Model.MOUSE_RIGHT_CLICK, RES.getString("ACT_MOUSE_DD_RCLICK")),
	new ValueLabelPair(Model.MOUSE_PRESS,   RES.getString("ACT_MOUSE_DD_PRESS")),
	new ValueLabelPair(Model.MOUSE_RELEASE, RES.getString("ACT_MOUSE_DD_RELEASE")),
	new ValueLabelPair(Model.NUDGE_UP,      RES.getString("ACT_MOUSE_DD_NUP")),
	new ValueLabelPair(Model.NUDGE_DOWN,    RES.getString("ACT_MOUSE_DD_NDOWN")),
	new ValueLabelPair(Model.NUDGE_LEFT,    RES.getString("ACT_MOUSE_DD_NLEFT")),
	new ValueLabelPair(Model.NUDGE_RIGHT,   RES.getString("ACT_MOUSE_DD_NRIGHT")),
	new ValueLabelPair(Model.NUDGE_STOP,    RES.getString("ACT_MOUSE_DD_NSTOP"))       
    };

    public static class MouseOptionUI extends ActionUI {    
        @Override
         public W_Base createUI(Trigger t) {
             W_Base option = new WT_ValueLabelOption(
                     RES.getString("ACT_MOUSE_LABEL"), t, MouseActions);
             return option;
        }         
    } 

    // ----------------------------------
    // HIDSpecialUI - UI for components that taking HID special keys.
    
    // HID special key values - defined by HID standard.
    static final ValueLabelPair HID_Keys[] = {
        new ValueLabelPair( 0xDA, RES.getString("ACT_KEY_UP_ARROW") ),
        new ValueLabelPair( 0xD9, RES.getString("ACT_KEY_DOWN_ARROW") ),
        new ValueLabelPair( 0xD8, RES.getString("ACT_KEY_LEFT_ARROW") ),
        new ValueLabelPair( 0xD7, RES.getString("ACT_KEY_RIGHT_ARROW") ),
        new ValueLabelPair( 0xB2, RES.getString("ACT_KEY_BACKSPACE") ),
        new ValueLabelPair( 0xB3, RES.getString("ACT_KEY_TAB") ),
        new ValueLabelPair( 0xB0, RES.getString("ACT_KEY_RETURN") ),
        new ValueLabelPair( 0xB1, RES.getString("ACT_KEY_ESCAPE") ),
        new ValueLabelPair( 0xD1, RES.getString("ACT_KEY_INSERT") ),
        new ValueLabelPair( 0xD4, RES.getString("ACT_KEY_DELETE") ),
        new ValueLabelPair( 0xD3, RES.getString("ACT_KEY_PAGE_UP") ),
        new ValueLabelPair( 0xD6, RES.getString("ACT_KEY_PAGE_DOWN") ),
        new ValueLabelPair( 0xD2, RES.getString("ACT_KEY_HOME") ),
        new ValueLabelPair( 0xD5, RES.getString("ACT_KEY_END") )
    };
   
    
    public static class HIDSpecialUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             W_Base option = new WT_ValueLabelOption(
                     RES.getString("ACT_KEY_LABEL"), t, HID_Keys);
             return option;
        }
    } 
    
    // Press-Release key codes
    static final ValueLabelPair PR_Keys[] = {
        new ValueLabelPair( 0x61, RES.getString("ACT_KEY_A")),
        new ValueLabelPair( 0x62, RES.getString("ACT_KEY_B")),
        new ValueLabelPair( 0x63, RES.getString("ACT_KEY_C")),
        new ValueLabelPair( 0x64, RES.getString("ACT_KEY_D")),
        new ValueLabelPair( 0x65, RES.getString("ACT_KEY_E")),
        new ValueLabelPair( 0x66, RES.getString("ACT_KEY_F")),
        new ValueLabelPair( 0x67, RES.getString("ACT_KEY_G")),
        new ValueLabelPair( 0x68, RES.getString("ACT_KEY_H")),
        new ValueLabelPair( 0x69, RES.getString("ACT_KEY_I")),
        new ValueLabelPair( 0x6A, RES.getString("ACT_KEY_J")),
        new ValueLabelPair( 0x6B, RES.getString("ACT_KEY_K")),
        new ValueLabelPair( 0x6C, RES.getString("ACT_KEY_L")),
        new ValueLabelPair( 0x6D, RES.getString("ACT_KEY_M")),
        new ValueLabelPair( 0x6E, RES.getString("ACT_KEY_N")),
        new ValueLabelPair( 0x6F, RES.getString("ACT_KEY_O")),
        new ValueLabelPair( 0x70, RES.getString("ACT_KEY_P")),
        new ValueLabelPair( 0x71, RES.getString("ACT_KEY_Q")),
        new ValueLabelPair( 0x72, RES.getString("ACT_KEY_R")),
        new ValueLabelPair( 0x73, RES.getString("ACT_KEY_S")),
        new ValueLabelPair( 0x74, RES.getString("ACT_KEY_T")),
        new ValueLabelPair( 0x75, RES.getString("ACT_KEY_U")),
        new ValueLabelPair( 0x76, RES.getString("ACT_KEY_V")),
        new ValueLabelPair( 0x77, RES.getString("ACT_KEY_W")),
        new ValueLabelPair( 0x78, RES.getString("ACT_KEY_X")),
        new ValueLabelPair( 0x79, RES.getString("ACT_KEY_Y")),
        new ValueLabelPair( 0x7A, RES.getString("ACT_KEY_Z")),
        new ValueLabelPair( 0x30, RES.getString("ACT_KEY_0")),
        new ValueLabelPair( 0x31, RES.getString("ACT_KEY_1")),
        new ValueLabelPair( 0x32, RES.getString("ACT_KEY_2")),
        new ValueLabelPair( 0x33, RES.getString("ACT_KEY_3")),
        new ValueLabelPair( 0x34, RES.getString("ACT_KEY_4")),
        new ValueLabelPair( 0x35, RES.getString("ACT_KEY_5")),
        new ValueLabelPair( 0x36, RES.getString("ACT_KEY_6")),
        new ValueLabelPair( 0x37, RES.getString("ACT_KEY_7")),
        new ValueLabelPair( 0x38, RES.getString("ACT_KEY_8")),
        new ValueLabelPair( 0x39, RES.getString("ACT_KEY_9")),
        new ValueLabelPair( 0x20, RES.getString("ACT_KEY_SPACE")),
        new ValueLabelPair( 0xDA, RES.getString("ACT_KEY_UP_ARROW") ),
        new ValueLabelPair( 0xD9, RES.getString("ACT_KEY_DOWN_ARROW") ),
        new ValueLabelPair( 0xD8, RES.getString("ACT_KEY_LEFT_ARROW") ),
        new ValueLabelPair( 0xD7, RES.getString("ACT_KEY_RIGHT_ARROW") ),
        new ValueLabelPair( 0xB2, RES.getString("ACT_KEY_BACKSPACE") ),
        new ValueLabelPair( 0xB3, RES.getString("ACT_KEY_TAB") ),
        new ValueLabelPair( 0xB0, RES.getString("ACT_KEY_RETURN") ),
        new ValueLabelPair( 0xB1, RES.getString("ACT_KEY_ESCAPE") ),
        new ValueLabelPair( 0xD1, RES.getString("ACT_KEY_INSERT") ),
        new ValueLabelPair( 0xD4, RES.getString("ACT_KEY_DELETE") ),
        new ValueLabelPair( 0xD3, RES.getString("ACT_KEY_PAGE_UP") ),
        new ValueLabelPair( 0xD6, RES.getString("ACT_KEY_PAGE_DOWN") ),
        new ValueLabelPair( 0xD2, RES.getString("ACT_KEY_HOME") ),
        new ValueLabelPair( 0xD5, RES.getString("ACT_KEY_END") )
    };
       
    public static class HIDKeyPress extends ActionUI {
        @Override
        public W_Base createUI(Trigger t) {
            return new WT_ValueLabelOption(
                    RES.getString("ACT_KEY_LABEL"), Model.KEY_PRESS, t, PR_Keys);            
        }
    }

    public static class HIDKeyRelease extends ActionUI {
        @Override
        public W_Base createUI(Trigger t) {
            return new WT_ValueLabelOption(
                    RES.getString("ACT_KEY_LABEL"), Model.KEY_RELEASE, t, PR_Keys);            
        }
    }

    // ----------------------------------
    // BTSpecialUI - UI for components that taking BT special keys
    
    // BT special key values - defined by BT standard.
    static final ValueLabelPair BT_Keys[] = {
        new ValueLabelPair( 14, RES.getString("ACT_KEY_UP_ARROW") ),
        new ValueLabelPair( 12, RES.getString("ACT_KEY_DOWN_ARROW") ),
        new ValueLabelPair( 11, RES.getString("ACT_KEY_LEFT_ARROW") ),
        new ValueLabelPair( 7, RES.getString("ACT_KEY_RIGHT_ARROW") ),
        new ValueLabelPair( 8, RES.getString("ACT_KEY_BACKSPACE") ),
        new ValueLabelPair( 9, RES.getString("ACT_KEY_TAB") ),
        new ValueLabelPair( 10, RES.getString("ACT_KEY_RETURN") ),
        new ValueLabelPair( 27, RES.getString("ACT_KEY_ESCAPE") ),
        new ValueLabelPair( 1, RES.getString("ACT_KEY_INSERT") ),
        new ValueLabelPair( 4, RES.getString("ACT_KEY_DELETE") ),
        new ValueLabelPair( 3, RES.getString("ACT_KEY_PAGE_UP") ),
        new ValueLabelPair( 6, RES.getString("ACT_KEY_PAGE_DOWN") ),
        new ValueLabelPair( 2, RES.getString("ACT_KEY_HOME") ),
        new ValueLabelPair( 5, RES.getString("ACT_KEY_END") )    
    };
    
    public static class BTSpecialUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             W_Base option = new WT_ValueLabelOption(
                     RES.getString("ACT_KEY_LABEL"), t, BT_Keys);
             return option;
        }
    } 
    
    ValueLabelPair OldIRActions[] = {
        new ValueLabelPair(Model.TV_ON_OFF,     "On/Off"),
        new ValueLabelPair(Model.VOLUME_UP,     "Volume Up"),
        new ValueLabelPair(Model.VOLUME_DOWN,   "Volume Down"),
        new ValueLabelPair(Model.CHANNEL_UP,    "Channel Up"),
        new ValueLabelPair(Model.CHANNEL_DOWN,  "Channel Down")
    };
    
    ValueLabelPair V2IRActions[] = {
        new ValueLabelPair(Model.IR_TV_ON_OFF,   RES.getString("ACT_TV_TV_ON_OFF")),
        new ValueLabelPair(Model.IR_VOLUME_UP,   RES.getString("ACT_TV_VOLUME_UP")),
        new ValueLabelPair(Model.IR_VOLUME_DOWN, RES.getString("ACT_TV_VOLUME_DOWN")),
        new ValueLabelPair(Model.IR_MUTE,        RES.getString("ACT_TV_MUTE")),
        new ValueLabelPair(Model.IR_BOX_ON_OFF,  RES.getString("ACT_TV_BOX_ON_OFF")),
        new ValueLabelPair(Model.IR_CHANNEL_UP,  RES.getString("ACT_TV_CHANNEL_UP")),
        new ValueLabelPair(Model.IR_CHANNEL_DOWN, RES.getString("ACT_TV_CHANNEL_DOWN")),
        new ValueLabelPair(Model.IR_DIGIT_0,  RES.getString("ACT_TV_DIGIT_0")),
        new ValueLabelPair(Model.IR_DIGIT_1,  RES.getString("ACT_TV_DIGIT_1")),
        new ValueLabelPair(Model.IR_DIGIT_2,  RES.getString("ACT_TV_DIGIT_2")),
        new ValueLabelPair(Model.IR_DIGIT_3,  RES.getString("ACT_TV_DIGIT_3")),
        new ValueLabelPair(Model.IR_DIGIT_4,  RES.getString("ACT_TV_DIGIT_4")),
        new ValueLabelPair(Model.IR_DIGIT_5,  RES.getString("ACT_TV_DIGIT_5")),
        new ValueLabelPair(Model.IR_DIGIT_6,  RES.getString("ACT_TV_DIGIT_6")),
        new ValueLabelPair(Model.IR_DIGIT_7,  RES.getString("ACT_TV_DIGIT_7")),
        new ValueLabelPair(Model.IR_DIGIT_8,  RES.getString("ACT_TV_DIGIT_8")),
        new ValueLabelPair(Model.IR_DIGIT_9,  RES.getString("ACT_TV_DIGIT_9"))
    };
    
    public static class IRActionUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             ValueLabelPair[] actionMap;

             if (Model.getVersionID() >= 406) {
                 actionMap = V2IRActions;
             } else {
                 actionMap = OldIRActions;
             }
             
             W_Composite comp = new W_Composite();
             W_Base irOption = 
                     new WT_ValueLabelOption(
                             RES.getString("ACT_IR_LABEL"), t, actionMap, false);
             W_Base repeat = new WT_Repeat(RES.getString("ACT_REPEAT"), t);
             
             comp.addPart(irOption);
             comp.addPart(repeat);
             return comp;
        }
    } 

    public static class BuzzerUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
            W_Composite comp = new W_Composite();
            comp.addPart( new WT_Frequency(RES.getString("ACT_BUZZ_FREQ_LABEL"), t));
            comp.addPart( new WT_Duration(RES.getString("ACT_BUZZ_DUR_LABEL"), t));
            return comp;
         }
    } 
    
    public static class SetStateUI extends ActionUI {        
        @Override
         public W_Base createUI(Trigger t) {
             W_Composite comp = new W_Composite();
             comp.addPart(new WT_Sensor(RES.getString("ACT_SENSOR_LABEL"), t));
             comp.addPart(new WT_StateSpinner(RES.getString("ACT_STATE_LABEL"), t));
             return comp;
         }
    }
    
    public static class LightBoxUI extends ActionUI {
        @Override
        public W_Base createUI(Trigger t) {
            return new WT_LightBox(RES.getString("ACT_VALUE_LABEL"), t);
        }
    }

    ValueLabelPair[] LD_Actions = {
        new ValueLabelPair(Model.LD_UP_ARROW,    RES.getString("LD_UP_ARROW")),
        new ValueLabelPair(Model.LD_DOWN_ARROW,  RES.getString("LD_DOWN_ARROW")),
        new ValueLabelPair(Model.LD_LEFT_ARROW,  RES.getString("LD_LEFT_ARROW")),
        new ValueLabelPair(Model.LD_RIGHT_ARROW, RES.getString("LD_RIGHT_ARROW")),
        new ValueLabelPair(Model.LD_TV_ON_OFF,   RES.getString("LD_TV_ON_OFF")),
        new ValueLabelPair(Model.LD_VOLUME_UP,   RES.getString("LD_VOLUME_UP")),
        new ValueLabelPair(Model.LD_VOLUME_DOWN, RES.getString("LD_VOLUME_DOWN")),
        new ValueLabelPair(Model.LD_CHANNEL_UP,  RES.getString("LD_CHANNEL_UP")),
        new ValueLabelPair(Model.LD_CHANNEL_DOWN, RES.getString("LD_CHANNEL_DOWN")),
        new ValueLabelPair(Model.LD_BLANK, RES.getString("LD_BLANK"))
    };
    
    public static class LCDDisplayUI extends ActionUI {
        @Override
        public W_Base createUI(Trigger t) {
            return new WT_ValueLabelOption(
                             RES.getString("ACT_RELAY_LABEL"), t, LD_Actions, false);
        }
    }
}
    

