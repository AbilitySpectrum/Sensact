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
public class WT_ReqdState extends W_Spinner {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");

    private static final String ANY = RES.getString("ANY_STATE");
    private static final String STATE_KEYS[] =
        {ANY, "1", "2", "3", "4", "5", "6", "7", "8", "9", 
            "10", "11", "12", "13", "14", "15"};
    
    private final Trigger theTrigger;
    public WT_ReqdState(String label, Trigger t) {
        super(label, STATE_KEYS);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        String s = (String) spinModel.getValue();
        int state;
        if (s == ANY) {
            state = 0;
        } else {
            state = new Integer(s);
        }
        theTrigger.setReqdState(state);
    }
    
    @Override
    public void update() {
        int state = theTrigger.getReqdState();
        String value;
        if (state == 0) {
            value = ANY;
        } else {
            value = Integer.toString(state);
        }
        spinner.setValue( value );
    }
}
