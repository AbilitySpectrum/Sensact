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
package lyricom.sensactConfig.model;

/**
 * The ActionType is used to provide a string name to each action
 * and is also the key needed to retrieve an action by name.
 * 
 * Each action has an associated sensactActionID.  This is the ID for the 
 * action taken by the Sensact and this must be in sync with the Sensact code.
 * In some cases several actions listed here have the same sensactActionID.
 * This is because different UIs are needed for different facets of the
 * action.  e.g. The UI to enter a letter (a text box) is different from the
 * UI to select a special key (a combo box) but both result in a keyboard
 * action with an appropriate key code on the Sensact.
 * 
 * @author Andrew
 */
public enum ActionType {
    NONE(0),
    RELAY_A(1),
    RELAY_B(2),
    BT_KEYBOARD(3),
    BT_SPECIAL(3),
    BT_MOUSE(9),
    HID_KEYBOARD(4),
    HID_SPECIAL(4),
    HID_KEYPRESS(4),
    HID_KEYRELEASE(4),
    HID_MOUSE(5),
    BUZZER(7),
    IR(8),
    SERIAL(6),
    SET_STATE(10),
    LIGHT_BOX(11);

    
    private final String localizedName;
    private final int sensactActionID;
    
    ActionType(int id) {
        localizedName = MRes.getStr(this.name());
        sensactActionID = id;
    }
    
    @Override
    public String toString() {
        return localizedName;
    }
    
    public int getActionID() {
        return sensactActionID;
    }
 
}