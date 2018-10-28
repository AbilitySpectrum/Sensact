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

/**
 * Timer for intervals between mouse speed changes.
 * @author Andrew
 */
public class WT_MouseTimer extends W_Number {

    public WT_MouseTimer(String label) {
        super(label, "Interval", 5, 0, 10000);
    }
    
    public void setNewValue(int val) {
        setValue(val);
    }

    public int getNewValue() {
        return getValue();
    }
}
