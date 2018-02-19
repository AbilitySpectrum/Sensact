package lyricom.sensactConfig.widgets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Frequency extends W_Number {

    private final Trigger theTrigger;
    public WT_Frequency(String label, Trigger t) {
        super(label, "Frequency", 4, 50, 2000);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        int duration = theTrigger.getActionParam() & 0xffff;
        int freq = getValue(); 
        theTrigger.setActionParam( (freq << 16) + duration );
    }
    
    @Override
    public void update() {
        int freq = (theTrigger.getActionParam() >> 16) & 0xffff;
        setValue(freq);
    }
}
