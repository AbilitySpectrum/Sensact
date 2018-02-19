package lyricom.sensactConfig.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class OutStream {
    private final List<Byte> buffer;
    
    public OutStream() {
        buffer = new ArrayList<>();
    }
    
    public List<Byte> getBuffer() {
        return buffer;
    }
    
    public void putChar(byte b) {
        buffer.add(b);
    }
    
    public void putNum(int val, int length) {
        if (length >= 4) {
            putChar( (byte) (((val >> 28) & 0xf) + Model.NUMBER_MASK));
            putChar( (byte) (((val >> 24) & 0xf) + Model.NUMBER_MASK));
        }
        if (length >= 3) {
            putChar( (byte) (((val >> 20) & 0xf) + Model.NUMBER_MASK));
            putChar( (byte) (((val >> 16) & 0xf) + Model.NUMBER_MASK));
        }
        if (length >= 2) {
            putChar( (byte) (((val >> 12) & 0xf) + Model.NUMBER_MASK));
            putChar( (byte) (((val >> 8) & 0xf) + Model.NUMBER_MASK));
        }
        putChar( (byte) (((val >> 4) & 0xf) + Model.NUMBER_MASK));
        putChar( (byte) (( val & 0xf ) + Model.NUMBER_MASK));
    }
    
    public void putID(int val, int nibbles) {
        if (nibbles >= 2) {
            putChar( (byte) (((val >> 4) & 0xf) + Model.ID_MASK) );
        }
        putChar( (byte) ((val & 0xf) + Model.ID_MASK) );
    }
    
    public void putCondition(int val) {
        putChar((byte) (val + Model.CONDITION_MASK) ); 
    }
    
    public void putBoolean(boolean val) {
        if (val) {
            putChar(Model.BOOL_TRUE);
        } else {
            putChar(Model.BOOL_FALSE);
        }
    }
}
