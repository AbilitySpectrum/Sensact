package lyricom.sensactConfig.solutions;

import javax.swing.JCheckBox;
import lyricom.sensactConfig.model.Trigger;

/**
 * This class is used to pass prompt and action information from
 * the SolutionsUI to the PressHoldSelect and PressReleaseWaitSelect
 * solutions.
 * 
 * @author Andrew
 */
public class ActionRow {
    Trigger prompt;
    Trigger action;
    JCheckBox latch;
        
    ActionRow(Trigger p, Trigger a, JCheckBox b) {
        prompt = p;
        action = a;
        latch = b;
    }    
}
