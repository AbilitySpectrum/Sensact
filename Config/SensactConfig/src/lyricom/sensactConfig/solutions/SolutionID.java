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
package lyricom.sensactConfig.solutions;

/**
 * Provides a unique ID to each solution, along with a localized name.
 * The class which handles the solution is part of the ID.
 * 
 * @author Andrew
 */
public enum SolutionID {   
    JOYSTICK_MOUSE("Joystick Mouse", JoystickMouseSolution.class),
    ONE_BTN_MOUSE("One Button Mouse", OneBtnMouse.class),
    TOGGLE_MOUSE("Toggle Mouse", ToggleMouse.class),
    MOUSE_CLICK_BUTTON("Mouse Click Button", MouseClickButton.class),
    PRESS_HOLD_SELECT("Press-Hold-Select", PressHoldSelect.class),
    PRESS_RELEASE_WAIT_SELECT("Press-Release-Wait-Select", PressReleaseWaitSelect.class);
    
    private final String name;
    private final Class implementation;
    
    SolutionID(String name, Class implementation) {
        this.name = name;
        this.implementation = implementation;
    }
    
    public String getName() {
        return name;
    }
    
    public String toString() {
        return name;
    }
    
    public Class getImplementation() {
        return implementation;
    }
}
