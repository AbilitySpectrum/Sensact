package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_SensorValue extends W_TextField {

    private final Trigger theTrigger;
    public WT_SensorValue(String label, Trigger t) {
        super(label, 1);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String text = field.getText();
        theTrigger.setTriggerValue(text.charAt(0));
    }

    @Override
    public void update() {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (theTrigger.getTriggerValue() & 0xff);
        field.setText(new String(bytes));       
    }
}
