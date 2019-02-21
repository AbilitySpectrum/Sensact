/*
 * This file is part of the Sensact Configuration software.
 *
 * Sensact Configuration software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact Configuration software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom.sensactConfig.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.*;
import lyricom.sensactConfig.model.ActionType;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SaAction;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.widgets.*;

/**
 *
 * @author Andrew
 */
public class TriggerEditDlg extends JDialog {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");
    private static Dimension ACTION_SIZE = null;
    
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
        JLabel text = new JLabel(RES.getString("TDLG_LONGEST_STRING") + "    ");        
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
        JLabel l = new JLabel(
                RES.getString("TDLG_EDIT_TRIGGER_FOR") + " "
                    + theTrigger.getSensor().getName());
        l.setFont(Utils.TITLE_FONT);
        p.add(l);
        return p;
    }
        
    private JPanel initialStateLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel(RES.getString("TDLG_IF_STATE_IS"), lblSize) );
        p.add(new WT_ReqdState("", tmpTrig));
        return p;
    }
        
    private JPanel sigLevelLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   " + RES.getString("TDLG_AND_SIGNAL_IS"), lblSize) );
        
        W_Composite wc = new W_Composite();
        wc.add(new WT_Condition("", tmpTrig));
        wc.add(new WT_Level("", tmpTrig));
        
        p.add(wc);
        
        return p;
    }
    
    private JPanel sensorValueLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   " + RES.getString("TDLG_AND_SIGNAL_IS"), lblSize) );
        p.add(new WT_SensorValue("", tmpTrig));
        
        return p;
    }

    private JPanel delayLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   " + RES.getString("TDLG_FOR_MORE_THAN"), lblSize) );
        p.add(new WT_Delay("", tmpTrig));
        return p;
    }
        
    private JPanel actionLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel(RES.getString("TDLG_THEN_DO_ACTION"), lblSize) );
        
        if (ACTION_SIZE == null) {
            // Calibration
            SaAction savedAction = tmpTrig.getAction();
            tmpTrig.setAction(Model.getActionByType(ActionType.IR));
            int savedParam = tmpTrig.getActionParam();
            tmpTrig.setActionParam(2);
            WT_Action actionUI = new WT_Action("", tmpTrig);
            p.add(actionUI);
            ACTION_SIZE = p.getPreferredSize();
            ACTION_SIZE.width += 10;
//            System.out.println("Calibration - H: " + Integer.toString(ACTION_SIZE.height) + " W: " + Integer.toString(ACTION_SIZE.width));
            tmpTrig.setAction(savedAction);
            tmpTrig.setActionParam(savedParam);  
            actionUI.update();
        } else {       
            p.add(new WT_Action("", tmpTrig));
        }
        
        p.setPreferredSize(ACTION_SIZE);
        p.setMaximumSize(ACTION_SIZE);
        
        return p;
    }
    
    private JPanel actionStateLine() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add( Utils.getLabel("   " + RES.getString("TDLG_AND_GO_TO_STATE"), lblSize) );        
        p.add(new WT_ActionState("", tmpTrig));
        return p;
    }
    
    private JPanel buttons() {
        JPanel p = new JPanel();
        
        JButton close = new JButton(RES.getString("BTN_DONE"));
        close.addActionListener(e -> {
            theTrigger.copyValue(tmpTrig);
            cancelled = false;
            thisDlg.dispose();
        });
        p.add(close);
        getRootPane().setDefaultButton(close);
        
        JButton cancel = new JButton(RES.getString("BTN_CANCEL"));
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
