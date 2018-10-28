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
 *
 * @author Andrew
 */
public class WT_Condition_Short extends W_Combo {
    private static final String GREATER = ">";
    private static final String LESSER = "<";
    private static final Object[] SHORT_NAMES = {GREATER, LESSER};    
    
    private final Trigger theTrigger;
    public WT_Condition_Short(String label, Trigger t) {
        super(label, SHORT_NAMES);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String p = (String) theBox.getSelectedItem();
        switch(p) {
            case GREATER:
                theTrigger.setCondition(Trigger.TRIGGER_ON_HIGH);
                break;
                
            case LESSER:
                theTrigger.setCondition(Trigger.TRIGGER_ON_LOW);
                break;
                
        }
    }
    
    @Override
    public void update() {
        switch(theTrigger.getCondition()) {
            case Trigger.TRIGGER_ON_HIGH:
                theBox.setSelectedItem(GREATER);
                break;
            case Trigger.TRIGGER_ON_LOW:
                theBox.setSelectedItem(LESSER);
                break;
        }     
    }
}
