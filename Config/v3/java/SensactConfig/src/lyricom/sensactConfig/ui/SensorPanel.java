package lyricom.sensactConfig.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import lyricom.sensactConfig.model.Sensor;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.model.Triggers;

/**
 * And panel containing controlling information for a particular sensor.
 * Contains a dynamic list of TriggerPanel - one for each trigger created
 * for the sensor.
 * 
 * Creates SetThresholdDlg to set levels 1 and 2.
 * Creates TriggerEditDlg to create a trigger.
 * 
 * @author Andrew
 */
public class SensorPanel extends JPanel {
    protected static final List<SensorPanel> theSensorPanels = new ArrayList<>();
    
    public static void reloadTriggers() {
        for(SensorPanel sp: theSensorPanels) {
            sp.deleteAllUI();
        }
        for(Trigger t: Triggers.getInstance().getAll()) {
            for(SensorPanel sp: theSensorPanels) {
                if (sp.getSensor() == t.getSensor()) {
                    sp.addTriggerUI(t);
                }
            }
        }
    }
    
    private final Sensor theSensor;
   
    private final JPanel triggersPanel;
    private final List<TriggerPanel> triggerPanelList = new ArrayList<>();

    SensorPanel(Sensor s) {
        super();
        theSensor = s;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
               
        triggersPanel = getTriggersPanel();
        add(Box.createVerticalStrut(5));
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(triggerHeader());
//        add(new JSeparator(JSeparator.HORIZONTAL));
        add(triggersPanel);
        SensorPanel.theSensorPanels.add(this);
    }
    
    public Sensor getSensor() {
        return theSensor;
    }
    
    private JComponent triggerHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel(theSensor.getName());
        Dimension d = name.getPreferredSize();
        d.width = 60;
        name.setPreferredSize(d);
        name.setMaximumSize(d);
        p.add(name);
        
        JButton newTrigger = new JButton("New Trigger");
        p.add(newTrigger);
        newTrigger.addActionListener(e -> {
            addTrigger();
            revalidate();
        });
        
        JButton deleteAll = new JButton("Delete All");
        p.add(deleteAll);
        deleteAll.addActionListener(e -> {
            deleteAll();
            revalidate();
        });
        
        if (theSensor.isContinuous()) {
            JButton setThresholdsBtn = new JButton("Set Thresholds");
            p.add(setThresholdsBtn);
            setThresholdsBtn.addActionListener(e -> setThresholds());
        }
        
        return p;
    }
    
    private JPanel getTriggersPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }
    
    private void addTrigger() {
        Trigger t = Triggers.getInstance().newTrigger(theSensor);
        TriggerEditDlg dlg = new TriggerEditDlg(t);
        if (dlg.wasCancelled()) {
            Triggers.getInstance().deleteTrigger(t);
        } else {
            TriggerPanel tp = new TriggerPanel(t, this);
            triggerPanelList.add(tp);
            triggersPanel.add(tp);   
        }
    }
    
    // Called by TriggerPanel when delete has been requested.
    void removeTriggerUI(TriggerPanel tp) {
        triggersPanel.remove(tp);
        triggerPanelList.remove(tp);
        revalidate();
    }
    
    // This adds the trigger UI for an existing trigger.
    public void addTriggerUI(Trigger t) {
        TriggerPanel tp = new TriggerPanel(t, this);
        triggerPanelList.add(tp);
        triggersPanel.add(tp);  
        revalidate();
    }
    
    // Delete all trigger UI, but not the underlying triggers.
    // This is called when triggers are received, and by now
    // the underlying triggers have already been removed.
    public void deleteAllUI() {
        triggersPanel.removeAll();
        triggerPanelList.clear();
        revalidate();
    }
    
    private void deleteAll() {
        triggersPanel.removeAll();
        Triggers.getInstance().deleteTriggerSet(theSensor);
        triggerPanelList.clear();       
    }
    
    private void setThresholds() {
        SetThresholdsDlg dlg = new SetThresholdsDlg(theSensor);
        if (!dlg.wasCancelled()) {
            int l1 = dlg.getLevel1();
            int l2 = dlg.getLevel2();
            theSensor.setLevels(l1, l2);
            for (TriggerPanel tpl : triggerPanelList) {
                tpl.levelsChanged();
            }            
        };        
    }
    
    // Move up-down support
    boolean isTop(TriggerPanel tp) {
        return (tp == triggerPanelList.get(0));
    }
    
    boolean isBottom(TriggerPanel tp) {
        TriggerPanel last = triggerPanelList.get(triggerPanelList.size()-1);
        return (tp == last);
    }
    
    private int getIndexOf(TriggerPanel tp) {
        int i;
        for(i=0; i<triggerPanelList.size(); i++) {
            if (tp == triggerPanelList.get(i)) {
                return i;
            }
        }        
        return -1;
    }
    
    void moveUp(TriggerPanel tp) {
        int index = getIndexOf(tp);
        if (index > 0) {
           triggerPanelList.remove(tp);
           triggerPanelList.add(index-1, tp);
           
           triggersPanel.remove(tp);
           triggersPanel.add(tp, index-1);
           triggersPanel.revalidate();
           
           Trigger thisTrigger = tp.getTrigger();
           Trigger beforeTrigger;
           if (index == 1) {
               beforeTrigger = null;
           } else {
               beforeTrigger = triggerPanelList.get(index-2).getTrigger();
           }
           Triggers.getInstance().placeAfter(thisTrigger, beforeTrigger);
       }
    }
    
    void moveDown(TriggerPanel tp) {
       int index = getIndexOf(tp);
        if (index < (triggerPanelList.size() - 1)) {
           triggerPanelList.remove(tp);
           triggerPanelList.add(index+1, tp);
           
           triggersPanel.remove(tp);
           triggersPanel.add(tp, index+1);
           triggersPanel.revalidate();
           
           Trigger thisTrigger = tp.getTrigger();
           Trigger beforeTrigger;
           beforeTrigger = triggerPanelList.get(index).getTrigger();           
           Triggers.getInstance().placeAfter(thisTrigger, beforeTrigger);
       }
    }
}
