package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Condition_Short extends W_Combo {
    private static final String GREATER = ">";
    private static final String LESSER = "<";
    private static final Object[] SHORT_NAMES = {GREATER, LESSER};    
    
    private final Trigger theTrigger;
    public WT_Condition_Short(String label, Trigger t) {
        super(label, SHORT_NAMES);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String p = (String) theBox.getSelectedItem();
        switch(p) {
            case GREATER:
                theTrigger.setCondition(Trigger.TRIGGER_ON_HIGH);
                break;
                
            case LESSER:
                theTrigger.setCondition(Trigger.TRIGGER_ON_LOW);
                break;
                
        }
    }
    
    @Override
    public void update() {
        switch(theTrigger.getCondition()) {
            case Trigger.TRIGGER_ON_HIGH:
                theBox.setSelectedItem(GREATER);
                break;
            case Trigger.TRIGGER_ON_LOW:
                theBox.setSelectedItem(LESSER);
                break;
        }     
    }
}
