package lyricom.sensactConfig.widgets;

/**
 * A class to hold labels and their associated values.
 * 
 * @author Andrew
 */
public class ValueLabelPair {
    private String optName;
    private int optValue;
    private boolean doRepeat;

    public ValueLabelPair(int v, String n) {
        optName = n;
        optValue = v;
        doRepeat = false;
    }

    public ValueLabelPair(int v, String n, boolean r) {
        optName = n;
        optValue = v;
        doRepeat = r;
    }
    
    public int getValue() {
        return optValue;
    }
    
    public boolean getRepeat() {
        return doRepeat;
    }

    @Override
    public String toString() {
        return optName;
    }
}
