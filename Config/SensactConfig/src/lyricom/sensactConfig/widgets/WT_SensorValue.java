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
public class WT_SensorValue extends W_TextField {

    private final Trigger theTrigger;
    public WT_SensorValue(String label, Trigger t) {
        super(label, 1);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String text = field.getText();
        theTrigger.setTriggerValue(text.charAt(0));
    }

    @Override
    public void update() {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (theTrigger.getTriggerValue() & 0xff);
        field.setText(new String(bytes));       
    }
}
