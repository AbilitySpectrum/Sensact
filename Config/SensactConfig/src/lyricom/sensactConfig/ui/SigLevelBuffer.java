package lyricom.sensactConfig.ui;

/**
 * This class buffers a set of signal levels and then can efficiently return
 * the maximum and minimum values in the buffer (and thus in the last bufsize
 * signal level reports).
 * 
 * @author Andrew
 */
public class SigLevelBuffer {
    private final int[] buffer;
    private final int bufsize;
    private int bufPtr;
    boolean first;
    boolean wrapped; // True once the buffer loading has wrapped around
    int max;
    int min;
    
    public SigLevelBuffer(int bufsize) {
        this.bufsize = bufsize;
        buffer = new int[bufsize];
        first = Boolean.TRUE;
        wrapped = Boolean.FALSE;
    }
    
    public int getMinVal() {
        return min;
    }
    
    public int getMaxVal() {
        return max;
    }
    
    public void addValue(int val) {
        boolean regenMax = Boolean.FALSE;
        boolean regenMin = Boolean.TRUE;
        
        if (first) {
            // Special action on the first time.
            min = max = val;
            bufPtr = 0;
            buffer[bufPtr++] = val;
            first = Boolean.FALSE;
            return;
        }
        // Check for new max and min values.
        if (val > max) {
            max = val;
        }
        if (val < min) {
            min = val;
        }
        
        // See if a max or min will be removed.
        // This will require a scan of the buffer to find a new max/min
        if (wrapped) {
            int removed = buffer[bufPtr];
            if (removed >= max) { // Should never be > but whatever...
                regenMax = Boolean.TRUE;
            } else if (removed <= min) {
                regenMin = Boolean.TRUE;
            }
        }
        
        // Add to the buffer
        buffer[bufPtr++] = val;
        
        // Check for wrap-around
        if (bufPtr >= bufsize) {
            wrapped = Boolean.TRUE;
            bufPtr = 0;
        }
        
        if (regenMax) {
            int newMax = buffer[0];
            for(int i=0; i<bufsize; i++) {
                if (buffer[i] == max) { //Duplicate of old max - we quit.
                    newMax = max;       // This guards against a full buffer 
                                        // scan in situations where
                    break;              // the buffer is filled with the same
                                        // value (e.g. all 0.
                } else if (buffer[i] > newMax) {
                    newMax = buffer[i];
                }
            }
            max = newMax;
        }
        
        if (regenMin) {
            int newMin = buffer[0];
            for(int i=0; i<bufsize; i++) {
                if (buffer[i] == min) { 
                    newMin = min;       
                    break;              
                } else if (buffer[i] < newMin) {
                    newMin = buffer[i];
                }
            }
            min = newMin;
        }       
    }
}
