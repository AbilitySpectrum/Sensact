package lyricom.sensactConfig.solutions;

import javax.swing.JOptionPane;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SaAction;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.model.Triggers;

/**
 *
 * @author Andrew
 */
public class ToggleMouse extends SolutionBase {
    private static final String UP_DOWN = "Up-Down";
    private static final String LEFT_RIGHT = "Left-Right";
    private static final String[] ORIENTATION = {UP_DOWN, LEFT_RIGHT};
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String[] YES_NO = {YES, NO};
    
    public ToggleMouse( SolutionsUI ui, SensorGroup sg ) {
        super(ui, sg);
    }
    
    @Override
    boolean doSolution() {
        Location btnLocHi = getButton();
        if (btnLocHi == null) return false;
                
        SaAction mouse = mouseSelection();
        if (cancelling) return false;
        
        String orientation = theUI.getOption("Select the orientation.", ORIENTATION);
        if (cancelling) return false;

        int delay = theUI.getDelay("Enter delay between direction changes.", 500);
        if (cancelling) return false;
        
        String beep = theUI.getOption("Do you want a sound on direction change.", YES_NO);
        if (cancelling) return false;
        
        btnLocHi.level = Trigger.Level.LEVEL1;        
        Location btnLocLo = btnLocHi.getReverse();        
        Triggers.getInstance().deleteTriggerSet(btnLocHi.sensor);

        int param1, param2;
        if (orientation.equals(UP_DOWN)) {
            param1 = Model.MOUSE_UP;
            param2 = Model.MOUSE_DOWN;
        } else {
            param1 = Model.MOUSE_LEFT;
            param2 = Model.MOUSE_RIGHT;
        }

        SaAction none = Model.getActionByName("None");
        
        SaAction actionOnChange;
        int paramOnChange;
        if (beep.equals(YES)) {
            actionOnChange = Model.getActionByName("Buzzer");
            paramOnChange = (200 << 16) + 100;
        } else {
            actionOnChange = none;
            paramOnChange = 0;
        }
        
        makeTrigger(1, btnLocHi,     0,           none,            0, 2);
        makeTrigger(2, btnLocHi,     0,          mouse,       param1, 2);
        makeTrigger(2, btnLocLo, delay, actionOnChange,paramOnChange, 3);
        makeTrigger(3, btnLocHi,     0,           none,            0, 4);
        makeTrigger(4, btnLocHi,     0,          mouse,       param2, 4);
        makeTrigger(4, btnLocLo, delay, actionOnChange,paramOnChange, 1);

        return true;
    }

}
