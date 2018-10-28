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
public class WT_StateSpinner extends W_Spinner {

    private static final String STATE_KEYS[] =
        {"1", "2", "3", "4", "5", "6", "7", "8", "9", 
            "10", "11", "12", "13", "14", "15"};
    
    private final Trigger theTrigger;
    public WT_StateSpinner(String label, Trigger t) {
        super(label, STATE_KEYS);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        int sensorID = (theTrigger.getActionParam() >> 8) & 0xff;
        String s = (String) spinModel.getValue();
        Integer state = new Integer(s);
        theTrigger.setActionParam( (sensorID << 8) + state);
    }
    
    @Override
    public void update() {
        int state = theTrigger.getActionParam() & 0xff;
        spinner.setValue( Integer.toString(state) );
    }
}
