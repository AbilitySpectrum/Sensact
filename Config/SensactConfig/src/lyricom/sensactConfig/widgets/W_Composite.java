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

import java.util.ArrayList;
import java.util.List;

/**
 * A container for several widgets.
 * 
 * @author Andrew
 */
public class W_Composite extends W_Base {
    
    private final List<W_Base> subParts = new ArrayList<>();
    
    public W_Composite() {
        super();
    }
    
    public void addPart(W_Base part) {
        subParts.add(part);
        add(part);  // Add to JPanel
    }
    
    @Override
    public void update() {
        subParts.forEach((p) -> {
            p.update();
        });
    }
    
}
