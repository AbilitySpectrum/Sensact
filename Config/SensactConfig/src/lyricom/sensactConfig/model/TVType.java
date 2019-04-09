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

package lyricom.sensactConfig.model;

/**
 * A POJO
 * Holds the description, ID and IR Code map for a single TV controller.
 * NOTE: The ID here is used as an index into TV protocol information
 * on the Leonardo.  This is NOT the IRLib protocol number.
 * 
 * @author Andrew
 */
public class TVType {    
    
    private final String description;
    private final int TVid;
    private final TVCodeMap map;
    private boolean canControlVolume;
    
    TVType(String d, int c, TVCodeMap m, boolean v) {
        description = d;
        TVid = c;
        map = m;
        canControlVolume = v;
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    public int getTVId() {
        return TVid;
    }
    
    public TVCodeMap getMap() {
        return map;
    }
    
    public boolean canControlVolume() {
        return canControlVolume;
    }
}
