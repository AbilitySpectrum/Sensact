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
package lyricom.sensactConfig.widgets;

import java.awt.FlowLayout;
import javax.swing.JPanel;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SaAction;
import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Action extends W_Combo {
    
    private final Trigger theTrigger;
    W_Base actionUI = null;
    JPanel innerPanel;
    boolean updating;
    
    public WT_Action(String label, Trigger t) {
        super(label, Model.getActionList());
        theTrigger = t;
        innerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(innerPanel);
        update();
        updating = false;
    }

    @Override
    public void widgetChanged() {
        if (updating) return;
        SaAction action = (SaAction) theBox.getSelectedItem();
        if (action == theTrigger.getAction()) return; // No change of action.
        theTrigger.setAction(action);
        theTrigger.setActionParam(action.getDefaultVal());
        theTrigger.setRepeat(false);
        innerPanel.remove(actionUI);
        actionUI = action.getOptionUI().createUI(theTrigger);
        innerPanel.add(actionUI);
        revalidate();
    }
    
    @Override
    public void update() {
        updating = true; // Prevent widgetChanged being executed.
        if ((actionUI == null)
                || (theBox.getSelectedItem() != theTrigger.getAction()) ) {
            // Need to change UI
            theBox.setSelectedItem(theTrigger.getAction()); // This triggers widgetChanged.
            if (actionUI != null) {
                innerPanel.remove(actionUI);
            }
            actionUI = theTrigger.getAction().getOptionUI().createUI(theTrigger);
            innerPanel.add(actionUI); 
        }
        actionUI.update();
        updating = false;
        revalidate();
    }
}
