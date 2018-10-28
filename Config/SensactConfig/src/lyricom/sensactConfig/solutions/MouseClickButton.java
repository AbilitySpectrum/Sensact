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

import lyricom.sensactConfig.model.ActionName;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SaAction;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.model.Triggers;

/**
 *
 * @author Andrew
 */
public class MouseClickButton extends SolutionBase {
    
    public MouseClickButton( SolutionsUI ui, SensorGroup sg ) {
        super(ui, sg);
    }
    
    @Override
    boolean doSolution() {
        Location btnLocHi = getButton();
        if (btnLocHi == null) return false;
                                
        // Gather the required actions ...
        SaAction mouse = mouseSelection();       
        if (cancelling) return false;

        SaAction buzz = Model.getActionByName(ActionName.BUZZER);
        SaAction none = Model.getActionByName(ActionName.NONE);
        // ... and the required button positions.        
        btnLocHi.level = Trigger.Level.LEVEL1;        
        Location btnLocLo = btnLocHi.getReverse();
        
        Triggers.getInstance().deleteTriggerSet(btnLocHi.sensor);
        
        makeTrigger(1, btnLocHi,     0,  none,                  0, 2);
        makeTrigger(2, btnLocLo,     0,  mouse, Model.MOUSE_CLICK, 1);
        makeTrigger(2, btnLocHi,   500,  buzz,   (200 << 16) + 50, 3);
        makeTrigger(3, btnLocLo,     0,  mouse, Model.MOUSE_RIGHT_CLICK, 1);
        makeTrigger(3, btnLocHi,   500,  buzz,   (200 << 16) + 50, 4);
        makeTrigger(4, btnLocLo,     0,  mouse, Model.MOUSE_PRESS, 1);

        return true;
    }

}
