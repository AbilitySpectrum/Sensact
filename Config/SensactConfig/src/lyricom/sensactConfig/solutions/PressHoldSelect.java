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
public class PressHoldSelect extends SolutionBase {

    public PressHoldSelect( SolutionsUI ui, SensorGroup sg ) {
        super(ui, sg);
    }

    @Override
    boolean doSolution() {
        boolean latching = false;
        
        Location btnLocHi = getButton();
        if (btnLocHi == null) return false;
        Location btnLocLo = btnLocHi.getReverse();
        if (cancelling) return false;
        
        int actionCount = theUI.getActionCount(SRes.getStr("PHS_ACTION_COUNT"));
        if (cancelling) return false;
        
        List<ActionRow> actions = theUI.getActions(actionCount, false);
        for(ActionRow ar: actions) {
            if (ar.latch != null && ar.latch.isSelected()) {
                latching = true;
            }
        }
        int delay = theUI.getDelay(SRes.getStr("PHS_PROMPT_DELAY"), 1000);
        if (cancelling) return false;
        
        int endDelay = 0;
        Trigger endAction = null;
        if (latching) {
            endDelay = theUI.getDelay(SRes.getStr("PHS_RESET_DELAY"), delay*2);
            if (cancelling) return false;
            
            endAction = theUI.getSingleAction(SRes.getStr("PHS_RESET_ACTION"));
            if (cancelling) return false;
        }

        SaAction none = Model.getActionByType(ActionType.NONE);
        Triggers.getInstance().deleteTriggerSet(btnLocHi.sensor);

        makeTrigger(1, btnLocHi,     0,  none,                   0, 2);
        Trigger a1 = actions.get(0).action;
        makeTrigger(2, btnLocLo,     0,  a1.getAction(), a1.getActionParam(), 1);
        int lastBaselineState = 2;
        int nextState = 3;
        for(int actionIndex = 1; actionIndex < actionCount; actionIndex++) {
            ActionRow ar = actions.get(actionIndex);
            Trigger pr = ar.prompt;
            Trigger ac = ar.action;
            boolean la = ar.latch.isSelected();
            
            makeTrigger(lastBaselineState, btnLocHi, delay, pr.getAction(), pr.getActionParam(), nextState);
            if (la) {
                makeTrigger(nextState, btnLocLo, 0, none, 0, nextState+1);
                makeTrigger(nextState+1, btnLocHi, 0, ac.getAction(), ac.getActionParam(), nextState+1);
                lastBaselineState = nextState;
                nextState += 2;
            } else {
                makeTrigger(nextState, btnLocLo, 0, ac.getAction(), ac.getActionParam(), 1);
                lastBaselineState = nextState;
                nextState++;
            }
        }
        if (latching) {
            makeTrigger(0, btnLocLo, endDelay, endAction.getAction(), endAction.getActionParam(), 1);
        }
        return true;
    }
    

}
