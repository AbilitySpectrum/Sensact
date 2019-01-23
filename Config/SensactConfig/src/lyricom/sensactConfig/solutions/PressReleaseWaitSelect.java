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

import java.util.List;
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
        
        int actionCount = theUI.getActionCount(SRes.getStr("PHS_ACTION_COUNT"));
        if (cancelling) return false;
                
        List<ActionRow> actions = theUI.getActions(actionCount, true);

        int delay = theUI.getDelay(SRes.getStr("PHS_PROMPT_DELAY"), 1000);
        if (cancelling) return false;
        
        int endDelay = theUI.getDelay(SRes.getStr("PHS_RESET_DELAY"), delay*2);
        if (cancelling) return false;
        
        Trigger endAction = theUI.getSingleAction(SRes.getStr("PHS_RESET_ACTION"));
        if (cancelling) return false;
        
        SaAction none = Model.getActionByType(ActionType.NONE);
        Triggers.getInstance().deleteTriggerSet(btnLocHi.sensor);

        makeTrigger(1, btnLocHi, 0, none, 0, 2);
        
        int baseState = 2; 
        for(ActionRow ar: actions) {
            makeTrigger(baseState, btnLocLo, delay, ar.prompt.getAction(), ar.prompt.getActionParam(), baseState + 2);
            if (baseState == 10 && ar.latch.isSelected()) {
                // Special case.  Saves one trigger on last latching action
                makeTrigger(baseState+2, btnLocHi, 0, ar.action.getAction(), ar.action.getActionParam(), baseState+2);
                makeTrigger(baseState+2, btnLocLo, endDelay, endAction.getAction(), endAction.getActionParam(), 1);
            } else {
                makeTrigger(baseState+2, btnLocHi, 0, ar.action.getAction(), ar.action.getActionParam(), baseState+3);
                if (ar.latch.isSelected()) {
                        makeTrigger(baseState+3, btnLocHi, 0, ar.action.getAction(), ar.action.getActionParam(), baseState+3);
                        makeTrigger(baseState+3, btnLocLo, endDelay, endAction.getAction(), endAction.getActionParam(), 1);
                } else {
                    makeTrigger(baseState+3, btnLocLo, 0, endAction.getAction(), endAction.getActionParam(), 1);
                }
            }
            baseState += 2;
        }
        
        return true;
    }
    

}
