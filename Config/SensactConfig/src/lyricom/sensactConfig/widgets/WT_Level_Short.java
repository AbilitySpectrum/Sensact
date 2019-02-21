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

import java.util.ResourceBundle;
import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Level_Short extends W_Combo {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");
    private static final String LEV1 = RES.getString("T_LEVEL_1_SHORT");
    private static final String LEV2 = RES.getString("T_LEVEL_1_SHORT");
    private static final Object[] SHORT_NAMES = {LEV1, LEV2};
    
    
    private final Trigger theTrigger;
    public WT_Level_Short(String label, Trigger t) {
        super(label, SHORT_NAMES);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String p = (String) theBox.getSelectedItem();
        if (p == LEV1) {
                theTrigger.setLevel(Trigger.Level.LEVEL1);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel(Trigger.Level.LEVEL1) );
        
        } else if (p == LEV2) {
                theTrigger.setLevel(Trigger.Level.LEVEL2);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel(Trigger.Level.LEVEL2) );
        }
    }
    
    @Override
    public void update() {
        switch(theTrigger.getLevel()) {
            case LEVEL1:
                theBox.setSelectedItem(LEV1);
                break;
            case LEVEL2:
                theBox.setSelectedItem(LEV2);
                break;
        }     
    }
}
