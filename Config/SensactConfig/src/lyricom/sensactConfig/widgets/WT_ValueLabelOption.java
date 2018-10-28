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

import lyricom.sensactConfig.model.Trigger;

/**
 * Displays the mouse option combo box.
 * Handles the synchronization of the mouse option combo selection
 * and the trigger.
 * 
 * @author Andrew
 */
public class WT_ValueLabelOption extends W_Combo {
    
    private final ValueLabelPair[] actions;
    private final Trigger theTrigger;
    private final boolean updateRepeat;
    private final int overlay;
    
    public WT_ValueLabelOption(String label, Trigger t, 
            ValueLabelPair[] actions, boolean updateRepeat) {
        super(label, actions);
        this.updateRepeat = updateRepeat;
        theTrigger = t;
        this.actions = actions;
        overlay = 0;
        update();
    }
    
    public WT_ValueLabelOption(String label, int overlay, Trigger t, 
            ValueLabelPair[] actions) {
        super(label, actions);
        this.updateRepeat = false;
        theTrigger = t;
        this.actions = actions;
        this.overlay = overlay;
        update();
    }

    public WT_ValueLabelOption(String label, Trigger t, ValueLabelPair[] actions) {
        this(label, t, actions, true);
    }

    @Override
    public void widgetChanged() {
        ValueLabelPair p = (ValueLabelPair) theBox.getSelectedItem();
        theTrigger.setActionParam(p.getValue() | overlay);
        if ( updateRepeat ) {     // Needed for IR
            theTrigger.setRepeat(p.getRepeat());
        }
    }
    
    @Override
    public void update() {
        int param = theTrigger.getActionParam() & 0xff;
        for(ValueLabelPair p: actions) {
            if (p.getValue() == param) {
                theBox.setSelectedItem(p);
            }
        }        
    }
}
