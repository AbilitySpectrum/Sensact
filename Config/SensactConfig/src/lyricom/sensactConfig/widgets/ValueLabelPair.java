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
 * A class to hold labels and their associated values.
 * 
 * @author Andrew
 */
public class ValueLabelPair {
    private String optName;
    private int optValue;
    private boolean doRepeat;

    public ValueLabelPair(int v, String n) {
        optName = n;
        optValue = v;
        doRepeat = false;
    }

    public ValueLabelPair(int v, String n, boolean r) {
        optName = n;
        optValue = v;
        doRepeat = r;
    }
    
    public int getValue() {
        return optValue;
    }
    
    public boolean getRepeat() {
        return doRepeat;
    }

    @Override
    public String toString() {
        return optName;
    }
}
