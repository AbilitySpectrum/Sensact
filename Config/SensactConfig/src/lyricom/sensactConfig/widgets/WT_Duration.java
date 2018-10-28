package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Duration extends W_Number {

    private final Trigger theTrigger;
    public WT_Duration(String label, Trigger t) {
        super(label, "Duration", 4, 20, 1000);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        int freq = (theTrigger.getActionParam() >> 16) & 0xffff;
        int duration = getValue(); 
        theTrigger.setActionParam( (freq << 16) + duration );
    }
    
    @Override
    public void update() {
        int duration = theTrigger.getActionParam() & 0xffff;
        setValue(duration);
    }
}
