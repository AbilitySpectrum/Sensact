package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 * Displays the mouse option combo box.
 * Handles the synchronization of the mouse option combo selection
 * and the trigger.
 * 
 * @author Andrew
 */
public class WT_ValueLabelOption extends W_Combo {
    
    private final ValueLabelPair[] actions;
    private final Trigger theTrigger;
    private final boolean updateRepeat;
    
    public WT_ValueLabelOption(String label, Trigger t, 
            ValueLabelPair[] actions, boolean updateRepeat) {
        super(label, actions);
        this.updateRepeat = updateRepeat;
        theTrigger = t;
        this.actions = actions;
        update();
    }

    public WT_ValueLabelOption(String label, Trigger t, ValueLabelPair[] actions) {
        this(label, t, actions, true);
    }

    @Override
    public void widgetChanged() {
        ValueLabelPair p = (ValueLabelPair) theBox.getSelectedItem();
        theTrigger.setActionParam(p.getValue());
        if ( updateRepeat ) {     // Needed for IR
            theTrigger.setRepeat(p.getRepeat());
        }
    }
    
    @Override
    public void update() {
        for(ValueLabelPair p: actions) {
            if (p.getValue() == theTrigger.getActionParam()) {
                theBox.setSelectedItem(p);
            }
        }        
    }
}
