package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Level_Short extends W_Combo {
    private static final String LEV1 = "lev 1";
    private static final String LEV2 = "lev 2";
    private static final Object[] SHORT_NAMES = {LEV1, LEV2};
    
    
    private final Trigger theTrigger;
    public WT_Level_Short(String label, Trigger t) {
        super(label, SHORT_NAMES);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String p = (String) theBox.getSelectedItem();
        switch(p) {
            case LEV1:
                theTrigger.setLevel(Trigger.Level.LEVEL1);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel(Trigger.Level.LEVEL1) );
                break;
                
            case LEV2:
                theTrigger.setLevel(Trigger.Level.LEVEL2);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel(Trigger.Level.LEVEL2) );
                break;
                
        }
    }
    
    @Override
    public void update() {
        switch(theTrigger.getLevel()) {
            case LEVEL1:
                theBox.setSelectedItem(LEV1);
                break;
            case LEVEL2:
                theBox.setSelectedItem(LEV2);
                break;
        }     
    }
}
