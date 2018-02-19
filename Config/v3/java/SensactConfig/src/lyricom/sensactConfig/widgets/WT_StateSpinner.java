package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_StateSpinner extends W_Spinner {

    private static final String STATE_KEYS[] =
        {"1", "2", "3", "4", "5", "6", "7", "8", "9", 
            "10", "11", "12", "13", "14", "15"};
    
    private final Trigger theTrigger;
    public WT_StateSpinner(String label, Trigger t) {
        super(label, STATE_KEYS);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        int sensorID = (theTrigger.getActionParam() >> 8) & 0xff;
        String s = (String) spinModel.getValue();
        Integer state = new Integer(s);
        theTrigger.setActionParam( (sensorID << 8) + state);
    }
    
    @Override
    public void update() {
        int state = theTrigger.getActionParam() & 0xff;
        spinner.setValue( Integer.toString(state) );
    }
}
