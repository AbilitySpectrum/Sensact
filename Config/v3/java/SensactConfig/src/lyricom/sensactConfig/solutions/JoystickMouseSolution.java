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
@RegisterInfo(
        name = "Joystick Mouse",
        applicaton = {"Input 1", "Input 2", "Input 3"}
)

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
        upLocation = c.getLocation("Move joystick to the UP position.");
        if (upLocation != null) {
            downLocation = c.getLocation("Move joystick to the DOWN position.");
            if (downLocation != null) {
                leftLocation = c.getLocation("Move joystick to the LEFT position.");
                if (leftLocation != null) {
                    rightLocation = c.getLocation("Move joystick to the RIGHT position.");
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
                    "Sorry. Joystick motion was not detected.",
                    "Solution Failure",
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
