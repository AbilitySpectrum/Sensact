package lyricom.sensactConfig.solutions;

import java.util.List;
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
public class PressReleaseWaitSelect extends SolutionBase {

    public PressReleaseWaitSelect( SolutionsUI ui, SensorGroup sg ) {
        super(ui, sg);
    }

    @Override
    boolean doSolution() {
        Location btnLocHi = getButton();
        if (btnLocHi == null) return false;
        Location btnLocLo = btnLocHi.getReverse();
        if (cancelling) return false;
        
        int actionCount = theUI.getActionCount("How many actions?");
        if (cancelling) return false;
                
        List<ActionRow> actions = theUI.getActions(actionCount, true);

        int delay = theUI.getDelay("Enter the delay between prompts", 1000);
        if (cancelling) return false;
        
        int endDelay = theUI.getDelay("Enter the reset delay", delay*2);
        if (cancelling) return false;
        
        Trigger endAction = theUI.getSingleAction("What is the reset action?");
        if (cancelling) return false;
        
        SaAction none = Model.getActionByName(ActionName.NONE);
        Triggers.getInstance().deleteTriggerSet(btnLocHi.sensor);

        makeTrigger(1, btnLocHi, 0, none, 0, 2);
        
        int baseState = 2; 
        for(ActionRow ar: actions) {
            makeTrigger(baseState, btnLocLo, delay, ar.prompt.getAction(), ar.prompt.getActionParam(), baseState + 2);
            makeTrigger(baseState+2, btnLocHi, 0, ar.action.getAction(), ar.action.getActionParam(), baseState+3);
            if (ar.latch.isSelected()) {
                makeTrigger(baseState+3, btnLocHi, 0, ar.action.getAction(), ar.action.getActionParam(), baseState+3);
                makeTrigger(baseState+3, btnLocLo, endDelay, endAction.getAction(), endAction.getActionParam(), 1);
            } else {
                makeTrigger(baseState+3, btnLocLo, 0, endAction.getAction(), endAction.getActionParam(), 1);
            }
            baseState += 2;
        }
        
        return true;
    }
    

}
