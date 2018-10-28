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

import java.awt.FlowLayout;
import javax.swing.JPanel;

/**
 * A base class for all Widgets.
 * Establishes the layout for all widget panels.
 * Establishes the update method which will be overridden by subclasses.
 * 
 * @author Andrew
 */
public class W_Base extends JPanel {

    public W_Base() {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0));
    }
    
    /*
     * This method is called when a change to the widget has been
     * detected. It is responsible for modifying the underlying
     * trigger to match the widget.
    */
    public void widgetChanged() {
        
    }
    
    /*
     * This method is called when the underlying trigger has changed.
     * It is responsible for updating the widget from the state of the
     * trigger.
    */
    public void update() {
        
    }
}
