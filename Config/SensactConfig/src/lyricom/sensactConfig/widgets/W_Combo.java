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

import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * JCombo box handler.
 * Creates a combo box with a label in front.
 * Does any styling that should be common to all combo boxes.
 * 
 * @author Andrew
 */
public class W_Combo extends W_Base {
    protected JComboBox theBox;
    
    public W_Combo(String label, Object[] items) {
        super();
        
        theBox = new JComboBox();
        int length = items.length;
        if (length > 20) length = 15;
        theBox.setMaximumRowCount(length);
        
        for(Object obj: items) {
            theBox.addItem(obj);
        }
        
        add(new JLabel(label));
        add(theBox);
        theBox.addActionListener(e -> widgetChanged());
    }
    
    // Alternate constructor for Lists of items.
    public W_Combo(String label, List<? extends Object> items) {
        this (label, items.toArray());
    }

}
