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
public class WT_Level extends W_Combo {
    private static final String LEVEL1 = "level 1";
    private static final String LEVEL2 = "level 2";
    private static final Object[] LONG_NAMES = {LEVEL1, LEVEL2};   
    
    private final Trigger theTrigger;
    public WT_Level(String label, Trigger t) {
        super(label, LONG_NAMES);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String p = (String) theBox.getSelectedItem();
        switch(p) {
            case LEVEL1:
                theTrigger.setLevel(Trigger.Level.LEVEL1);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel1() );
                break;
                
            case LEVEL2:
                theTrigger.setLevel(Trigger.Level.LEVEL2);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel2() );
                break;
                
        }
    }
    
    @Override
    public void update() {
        switch(theTrigger.getLevel()) {
            case LEVEL1:
                theBox.setSelectedItem(LEVEL1);
                break;
            case LEVEL2:
                theBox.setSelectedItem(LEVEL2);
                break;
        }     
    }
}
