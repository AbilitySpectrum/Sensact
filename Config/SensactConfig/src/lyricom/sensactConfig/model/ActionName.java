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