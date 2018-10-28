package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Level extends W_Combo {
    private static final String LEVEL1 = "level 1";
    private static final String LEVEL2 = "level 2";
    private static final Object[] LONG_NAMES = {LEVEL1, LEVEL2};   
    
    private final Trigger theTrigger;
    public WT_Level(String label, Trigger t) {
        super(label, LONG_NAMES);
        theTrigger = t;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String p = (String) theBox.getSelectedItem();
        switch(p) {
            case LEVEL1:
                theTrigger.setLevel(Trigger.Level.LEVEL1);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel1() );
                break;
                
            case LEVEL2:
                theTrigger.setLevel(Trigger.Level.LEVEL2);
                theTrigger.setTriggerValue( theTrigger.getSensor().getLevel2() );
                break;
                
        }
    }
    
    @Override
    public void update() {
        switch(theTrigger.getLevel()) {
            case LEVEL1:
                theBox.setSelectedItem(LEVEL1);
                break;
            case LEVEL2:
                theBox.setSelectedItem(LEVEL2);
                break;
        }     
    }
}
