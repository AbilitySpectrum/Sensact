package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_ReqdState extends W_Spinner {

    private static final String ANY = "Any";
    private static final String STATE_KEYS[] =
        {ANY, "1", "2", "3", "4", "5", "6", "7", "8", "9", 
            "10", "11", "12", "13", "14", "15"};
    
    private final Trigger theTrigger;
    public WT_ReqdState(String label, Trigger t) {
        super(label, STATE_KEYS);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        String s = (String) spinModel.getValue();
        int state;
        if (s == ANY) {
            state = 0;
        } else {
            state = new Integer(s);
        }
        theTrigger.setReqdState(state);
    }
    
    @Override
    public void update() {
        int state = theTrigger.getReqdState();
        String value;
        if (state == 0) {
            value = ANY;
        } else {
            value = Integer.toString(state);
        }
        spinner.setValue( value );
    }
}
