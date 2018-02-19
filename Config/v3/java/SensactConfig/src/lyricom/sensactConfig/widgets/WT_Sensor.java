package lyricom.sensactConfig.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.Sensor;
import lyricom.sensactConfig.model.Trigger;

/**
 * A combo box which displays all sensors.  
 * This is used to set the sensor value in State Change actions.
 * 
 * @author Andrew
 */
public class WT_Sensor extends W_Combo {

    private final Trigger theTrigger;
    public WT_Sensor(String label, Trigger t) {
        super(label, Model.sensorList);
        
        theTrigger = t;  
        update();
    }

    @Override
    public void widgetChanged() {
        Sensor s = (Sensor) theBox.getSelectedItem();
        
        int state = theTrigger.getActionParam() & 0xff;
        
        theTrigger.setActionParam( (s.getId() << 8) + state);
    }
    
    @Override
    public void update() {
        int sensorID = (theTrigger.getActionParam() >> 8) & 0xff;
        
        for(Sensor s: Model.sensorList) {
            if (s.getId() == sensorID) {
                theBox.setSelectedItem(s);
            }
        }
        
    }
}
