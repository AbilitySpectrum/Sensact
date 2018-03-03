package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_LightBox extends W_Number {

    private final Trigger theTrigger;
    public WT_LightBox(String label, Trigger t) {
        super(label, "Value", 3, 0, 255);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        int value = getValue();
        theTrigger.setActionParam( value );
    }
    
    @Override
    public void update() {
        int value = theTrigger.getActionParam() & 0xff;
        setValue(value);
    }
}
