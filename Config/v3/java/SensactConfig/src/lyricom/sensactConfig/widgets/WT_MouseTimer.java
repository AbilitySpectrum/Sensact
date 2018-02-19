package lyricom.sensactConfig.widgets;

/**
 * Timer for intervals between mouse speed changes.
 * @author Andrew
 */
public class WT_MouseTimer extends W_Number {

    public WT_MouseTimer(String label) {
        super(label, "Interval", 5, 0, 10000);
    }
    
    public void setNewValue(int val) {
        setValue(val);
    }

    public int getNewValue() {
        return getValue();
    }
}
