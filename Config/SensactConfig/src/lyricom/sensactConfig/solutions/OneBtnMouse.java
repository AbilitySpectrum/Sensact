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

import javax.swing.JOptionPane;
import lyricom.sensactConfig.model.ActionType;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SaAction;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.model.Triggers;

/**
 *
 * @author Andrew
 */
public class OneBtnMouse extends SolutionBase {
    
    public OneBtnMouse( SolutionsUI ui, SensorGroup sg ) {
        super(ui, sg);
    }
    
    @Override
    boolean doSolution() {
        Location btnLocHi = getButton();
        if (btnLocHi == null) return false;
                        
        int delay = theUI.getDelay("Enter delay between beeps.", 1000);
        
        if (cancelling) return false;
        
        // Gather the required actions ...
        SaAction mouse = mouseSelection();       
        if (cancelling) return false;

        SaAction buzz = Model.getActionByType(ActionType.BUZZER);
        SaAction none = Model.getActionByType(ActionType.NONE);
        // ... and the required button positions.        
        btnLocHi.level = Trigger.Level.LEVEL1;        
        Location btnLocLo = btnLocHi.getReverse();
        
        Triggers.getInstance().deleteTriggerSet(btnLocHi.sensor);
        
        makeTrigger(1, btnLocHi,     0,  none,                   0, 2);
        makeTrigger(2, btnLocLo,     0, mouse,   Model.MOUSE_CLICK, 1);
        makeTrigger(2, btnLocHi, delay,  buzz, ((800 << 16) + 250), 3);
        makeTrigger(3, btnLocLo,     0,  none,                   0, 4);
        makeTrigger(4, btnLocHi,     0, mouse,   Model.MOUSE_UP,    4);
        makeTrigger(3, btnLocHi, delay,  buzz, ((400 << 16) + 250), 5);
        makeTrigger(5, btnLocLo,     0,  none,                   0, 6);
        makeTrigger(6, btnLocHi,     0, mouse,   Model.MOUSE_DOWN,  6);
        makeTrigger(5, btnLocHi, delay,  buzz, ((600 << 16) + 250), 7);
        makeTrigger(7, btnLocLo,     0,  none,                   0, 8);
        makeTrigger(8, btnLocHi,     0, mouse,   Model.MOUSE_LEFT,  8);
        makeTrigger(7, btnLocHi, delay,  buzz, ((500 << 16) + 250), 9);
        makeTrigger(9, btnLocLo,     0,  none,                   0, 10);
        makeTrigger(10, btnLocHi,     0, mouse,  Model.MOUSE_RIGHT,  10);
        makeTrigger(0, btnLocLo, delay * 2,  buzz, ((200 << 16) + 150), 1);
        
        return true;
    }
}
