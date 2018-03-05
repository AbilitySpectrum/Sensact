package lyricom.sensactConfig.widgets;

import javax.swing.JLabel;
import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Delay extends W_Number {

    private final Trigger theTrigger;
    public WT_Delay(String label, Trigger t) {
        super(label, "Hold time", 5, 0, 60000);
        add(new JLabel("msec"));
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        int delay = getValue(); 
        theTrigger.setDelay(delay);
    }
    
    @Override
    public void update() {
        int delay = theTrigger.getDelay();
        setValue(delay);
    }
}
