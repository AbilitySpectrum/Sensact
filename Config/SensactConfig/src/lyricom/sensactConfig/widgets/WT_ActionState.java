package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_ActionState extends W_Spinner {

    private static final String STATE_KEYS[] =
        {"1", "2", "3", "4", "5", "6", "7", "8", "9", 
            "10", "11", "12", "13", "14", "15"};
    
    private final Trigger theTrigger;
    public WT_ActionState(String label, Trigger t) {
        super(label, STATE_KEYS);
        theTrigger = t;
        update();
    }

    @Override
    public void widgetChanged() {
        String s = (String) spinModel.getValue();
        Integer state = new Integer(s);
        theTrigger.setActionState(state);
    }
    
    @Override
    public void update() {
        int state = theTrigger.getActionState();
        spinner.setValue( Integer.toString(state) );
    }

}
