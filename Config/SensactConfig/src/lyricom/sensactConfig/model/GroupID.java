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
 * The GrouID is used to provide a localizable string name to each sensor
 * group and also provides a key for to retrieval and identification of 
 * a group (in Solutions).
 * 
 * @author Andrew
 */
public enum GroupID {
    SENSOR1,
    SENSOR2,
    SENSOR3,
    ACCEL,
    GYRO,
    USB_PORT;
    
    String localizedName;
    
    GroupID() {
        localizedName = MRes.getStr(this.name());
    }
    
    public String toString() {
        return localizedName;
    }

}
