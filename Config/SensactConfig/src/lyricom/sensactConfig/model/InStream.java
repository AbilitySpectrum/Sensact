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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author Andrew
 */
public class InStream {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");
    
    private final List<Byte> bytes;
    private int index;
    
    public InStream(List<Byte> blist) {
        bytes = blist;
        index = 0;
    }
    
    // Used in testing.
    public InStream(byte[] bs) {
        bytes = new ArrayList<>();
        for(byte b: bs) {
            bytes.add(b);
        }
    }
    
    private Byte nextByte() throws IOError {
        if (index < bytes.size()) {
            Byte b =  bytes.get(index);
            index++;
            return b;
        } else {
            throw new IOError(RES.getString("CDE_END_OF_STREAM"));
        }
    }
    
    public Byte getChar() throws IOError {
        Byte b = nextByte();
        while((b == '\n') || (b == '\r') || (b == ' ')) {
            b = nextByte();
        }
        return b;        
    }
    
    /*
     * Get a number of 'length' bytes.
     * One character per nibble
     * 2 byte values can be negative (e.g. trigger values for gyro).
     * 4 byte values are parameters, bit are interpreted by action.
     */
    public int getNum(int length) throws IOError {
        boolean negative = false;
	int value = 0;
        
	for (int i=0; i < length*2; i++) {
            int tmp = ((int) getChar() & 0xff) - Model.NUMBER_MASK;
            if (tmp < 0 || tmp > 15) {
		throw new IOError(RES.getString("CDE_INVALID_NUMBER"));
            }
            value = (value << 4) + tmp;
            if ((i == 0) && (tmp & 0x8) == 0x8) {
		negative = true;
            }
        }
        if (negative) {
            if (length == 2) {
                value = value - 0x10000;
            }
        }
        
	return value;        
    }
    
    public int getID(int nibbles) throws IOError {
        int value = 0;
        for(int i=0; i<nibbles; i++) {
            int tmp = ((int) getChar() & 0xff) - Model.ID_MASK;
             if (tmp < 0 || tmp > 15) {
		throw new IOError(RES.getString("CDE_INVALID_ID"));
            }
            value = (value << 4) + tmp;
        }     
        return value;
    }
    
    public int getCondition() throws IOError {
        int tmp = ((int) getChar() & 0xff) - Model.CONDITION_MASK;
        if (tmp < 1 || tmp > 3) {
            throw new IOError(RES.getString("CDE_INVALID_CONDITION"));
        }
        return tmp;
    }

    public boolean getBoolean() throws IOError {
        switch (getChar()) {
            case Model.BOOL_TRUE:
                return true;
            case Model.BOOL_FALSE:
                return false;
            default:
                throw new IOError(RES.getString("CDE_INVALID_BOOLEAN"));
        }
    }
}

