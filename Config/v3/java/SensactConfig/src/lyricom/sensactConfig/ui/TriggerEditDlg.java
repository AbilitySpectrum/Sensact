package lyricom.sensactConfig.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.widgets.*;

/**
 *
 * @author Andrew
 */
public class TriggerEditDlg extends JDialog {

    private final Trigger tmpTrig;
    private final Trigger theTrigger;
    private final TriggerEditDlg thisDlg;
    private final Dimension lblSize;
    private final boolean isContinuous;
    private boolean cancelled;
    
    TriggerEditDlg(Trigger t) {
        super(MainFrame.TheFrame, true);
        theTrigger = t;
        isContinuous = t.getSensor().isContinuous();
        
        tmpTrig = new Trigger(theTrigger.getSensor());
        tmpTrig.copyValue(theTrigger);
        
        thisDlg = this;
        
        // Work out the size for all the labels.
        // based on the longest string.
        JLabel text = new JLabel("   and go to state ");        
        lblSize = text.getPreferredSize();
 //       lblSize.width += 5;
        
        setLayout(new BorderLayout());
        
        add(title(), BorderLayout.NORTH);
        Box b = Box.createVerticalBox();
        b.add(initialStateLine());
        if (isContinuous) {
            b.add(sigLevelLine());
        } else {
            b.add(sensorValueLine());
        }
        b.add(delayLine());
        b.add(actionLine());
        b.add(actionStateLine());        
        
        add(b, BorderLayout.CENTER);

        add(buttons(), BorderLayout.SOUTH);
        
        addWindowListener(new WindowAdapter() {
            // This is called when the close button (at top right on windows)
            // is pressed.  Treat this as a cancel.
            @Override
            public void windowClosing(WindowEvent e) {
                cancelled = true;
            }
        });
        
        pack();
        // Center on screen
        Dimension dim = getPreferredSize();
        Point center = ScreenInfo.getCenter();
        setLocation(center.x-dim.width/2, center.y-dim.height/2);
               
        setVisible(true);
    }
    
    private JPanel title() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel l = new JLabel("Edit Trigger for " + theTrigger.getSensor().getName());
        l.setFont(Utils.TITLE_FONT);
        p.add(l);
        return p;
    }
        
    private JPanel initialStateLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("IF state is ", lblSize) );
        p.add(new WT_ReqdState("", tmpTrig));
        return p;
    }
    
    private static final String GREATER_THAN = "greater than";
    private static final String LESS_THAN = "less than";
    private static final String LEVEL_ONE = "level one";
    private static final String LEVEL_TWO = "level two";
    
    private JPanel sigLevelLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   and signal is ", lblSize) );
        
        final JComboBox cb = new JComboBox();
        cb.addItem(GREATER_THAN);
        cb.addItem(LESS_THAN);
        if (tmpTrig.getCondition() == Trigger.TRIGGER_ON_HIGH) {
            cb.setSelectedItem(GREATER_THAN);
        } else {
            cb.setSelectedItem(LESS_THAN);
        }
        
        cb.addActionListener(e -> {
            if (cb.getSelectedItem() == GREATER_THAN) {
                tmpTrig.setCondition(Trigger.TRIGGER_ON_HIGH);
            } else {
                tmpTrig.setCondition(Trigger.TRIGGER_ON_LOW);
            }
        });                
        p.add(cb);
        
        final JComboBox cb2 = new JComboBox();
        cb2.addItem(LEVEL_ONE);
        cb2.addItem(LEVEL_TWO);
        if (tmpTrig.getLevel() == Trigger.Level.LEVEL1) {
            cb2.setSelectedItem(LEVEL_ONE);
        } else {
            cb2.setSelectedItem(LEVEL_TWO);
        }
        
        cb2.addActionListener(e -> {
            if (cb2.getSelectedItem() == LEVEL_ONE) {
                tmpTrig.setLevel(Trigger.Level.LEVEL1);
                tmpTrig.setTriggerValue( theTrigger.getSensor().getLevel1() );
            } else {
                tmpTrig.setLevel(Trigger.Level.LEVEL2);
                tmpTrig.setTriggerValue( theTrigger.getSensor().getLevel2() );
            }
        });                
        p.add(cb2);
        
        return p;
    }
    
    private JPanel sensorValueLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   and signal is ", lblSize) );
        p.add(new WT_SensorValue("", tmpTrig));
        
        return p;
    }

    private JPanel delayLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   for more than ", lblSize) );
        p.add(new WT_Delay("", tmpTrig));
        p.add(new JLabel(" msec"));
        return p;
    }
        
    private JPanel actionLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("THEN do action ", lblSize) );
        p.add(new WT_Action("", tmpTrig));
        
        Dimension d = p.getPreferredSize();
        d.width = 520;
        p.setPreferredSize(d);
        p.setMaximumSize(d);
        
        return p;
    }
    
    private JPanel actionStateLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   and go to state ", lblSize) );        
        p.add(new WT_ActionState("", tmpTrig));
        return p;
    }
    
    private JPanel buttons() {
        JPanel p = new JPanel();
        
        JButton close = new JButton("Done");
        close.addActionListener(e -> {
            theTrigger.copyValue(tmpTrig);
            cancelled = false;
            thisDlg.dispose();
        });
        p.add(close);
        getRootPane().setDefaultButton(close);
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            cancelled = true;
            thisDlg.dispose();
        });
        p.add(cancel);
        return p;
    }

    public boolean wasCancelled() {
        return cancelled;
    }
}
