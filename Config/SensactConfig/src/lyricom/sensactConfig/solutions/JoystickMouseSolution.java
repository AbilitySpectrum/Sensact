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
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SaAction;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.model.Triggers;
import lyricom.sensactConfig.ui.SensorPanel;

/**
 *
 * @author Andrew
 */
//@RegisterInfo(
//        name = "Joystick Mouse",
//        applicaton = {"Input 1", "Input 2", "Input 3"}
//)

public class JoystickMouseSolution extends SolutionBase {    
    
    public JoystickMouseSolution( SolutionsUI ui, SensorGroup sg ) {
        super(ui, sg);
    }
    
    @Override
    boolean doSolution() {                
        Calibrator c = getCalibrator();
        c.startCalibration();
        c.getRestValues();
        
        Location upLocation, downLocation, leftLocation, rightLocation;
        upLocation = downLocation = leftLocation = rightLocation = null;
        
        boolean success = false;
        upLocation = c.getLocation(SRes.getStr("JW_UP"));
        if (upLocation != null) {
            downLocation = c.getLocation(SRes.getStr("JW_DOWN"));
            if (downLocation != null) {
                leftLocation = c.getLocation(SRes.getStr("JW_LEFT"));
                if (leftLocation != null) {
                    rightLocation = c.getLocation(SRes.getStr("JW_RIGHT"));
                    if (rightLocation != null) {
                        success = true;
                    }
                }
            }
        }
        c.endCalibration();
        if (cancelling) return false;
        
        if (!success) {
            JOptionPane.showMessageDialog(theUI,
                    SRes.getStr("JW_FAIL_MSG"),
                    SRes.getStr("SW_SOLUTION_FAIL_TITLE"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        SaAction action = mouseSelection();
        if (action == null) {
            return false;
        }
        
        upLocation.level = Trigger.Level.LEVEL1;
        downLocation.level = Trigger.Level.LEVEL2;
        leftLocation.level = Trigger.Level.LEVEL1;
        rightLocation.level = Trigger.Level.LEVEL2;
       
        Triggers trigs = Triggers.getInstance();
        trigs.deleteTriggerSet(upLocation.sensor);
        trigs.deleteTriggerSet(leftLocation.sensor);
        
        makeTrigger(1, upLocation,    0, action, Model.MOUSE_UP,    1);
        makeTrigger(1, downLocation,  0, action, Model.MOUSE_DOWN,  1);
        makeTrigger(1, leftLocation,  0, action, Model.MOUSE_LEFT,  1);
        makeTrigger(1, rightLocation, 0, action, Model.MOUSE_RIGHT, 1);

        return true;
    }
    

}
