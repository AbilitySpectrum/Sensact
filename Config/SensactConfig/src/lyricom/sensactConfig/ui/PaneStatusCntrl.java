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
package lyricom.sensactConfig.ui;

import javax.swing.JTabbedPane;

/**
 * Used to control the status indication (an icon in the title) of an 
 * individual tabbed panel. This allows a SensorGroupPanel to control 
 * its status without giving it full access to the tabbed pane object.
 * 
 * @author Andrew
 */
public class PaneStatusCntrl {
    private final JTabbedPane pane;
    private final int index;
    
    PaneStatusCntrl(JTabbedPane pane, int index) {
        this.pane = pane;
        this.index = index;
    }
    
    void panelContainsTriggers() {
        pane.setIconAt(index, Utils.getIcon(Utils.ICON_BLUETRI));
    }
    
    void panelIsEmpty() {
        pane.setIconAt(index, Utils.getIcon(Utils.ICON_EMPTY));        
    }
}
