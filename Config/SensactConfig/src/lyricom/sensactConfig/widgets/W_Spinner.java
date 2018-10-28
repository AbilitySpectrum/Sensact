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

import java.awt.Color;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.border.EmptyBorder;
import lyricom.sensactConfig.ui.Utils;

/**
 *
 * @author Andrew
 */
public class W_Spinner extends W_Base {
    
    protected final JSpinner spinner;
    protected final SpinnerListModel spinModel;

    public W_Spinner(String label, String[] values) {
        super();
                
        spinModel = new SpinnerListModel(values);
        spinner = new JSpinner(spinModel);
        spinner.setFont(Utils.STATE_FONT);
        spinner.setBackground(Color.BLUE);
        JFormattedTextField fld = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        fld.setEditable(false);
        fld.setColumns(2);
        fld.setBorder( new EmptyBorder(0,0,0,3));
        add(new JLabel(label));
        add(spinner);        
        spinner.addChangeListener(e -> widgetChanged());        
    }
    
    public String getValue() {
        return (String) spinModel.getValue();
    }
}
