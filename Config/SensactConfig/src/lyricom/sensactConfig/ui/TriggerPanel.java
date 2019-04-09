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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.LineBorder;
import lyricom.sensactConfig.model.ActionType;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.model.Triggers;
import lyricom.sensactConfig.widgets.*;

/**
 * A panel which holds the UI for a single trigger.
 * @author Andrew
 */
public class TriggerPanel extends JPanel {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");
    private static int ACTION_WIDTH = 0;
    private static int ACTION_HEIGHT = 0;
    
    private JPopupMenu popup;
    
    private final Trigger theTrigger;
    private final WT_ReqdState reqdState;
    private final W_Base signalLevel;
    private final WT_Delay delay;
    private final WT_Action actionUI;
    private final WT_ActionState actionState;
    private final SensorPanel parent;
    private final boolean isContinuous;
    private final TriggerPanel thisPanel;
    
    TriggerPanel(Trigger t, SensorPanel p) {
        super();
        theTrigger = t;
        isContinuous = t.getSensor().isContinuous();
        thisPanel = this;
        
        setToolTipText(RES.getString("TPANEL_TOOL_TIP"));
        
        parent = p;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        
        setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        createMenus();
        
        reqdState = new WT_ReqdState(RES.getString("TPANEL_IF"), t);
        add( reqdState );
//        add( new ThickBar(10));
        if (isContinuous) {
//            W_Composite wc = new W_Composite();
//            wc.addPart( new WT_Condition_Short("sig", t));
//            wc.addPart( new WT_Level_Short("", t));
//            signalLevel = wc;
            signalLevel = new WT_SignalLevel(t);
        } else {
            signalLevel = new WT_SensorValue("", t);
        }
        add( signalLevel );
//        add( new ThickBar(10));
        delay = new WT_Delay(RES.getString("TPANEL_FOR"), t);
        add(delay);
        
//        add(new Arrow(35));
        
        if (ACTION_WIDTH == 0) {
            // Calibrate the size of actionUI.
            Trigger tmp = new Trigger(t.getSensor());
            tmp.setAction(Model.getActionByType(ActionType.IR));
            tmp.setActionParam( 2 );
            WT_Action tmpUI = new WT_Action(RES.getString("TPANEL_DO"), tmp);
            Dimension dim = tmpUI.getPreferredSize();
//            System.out.println("Calibration - H: " + Integer.toString(dim.height) + " W: " + Integer.toString(dim.width));
            ACTION_WIDTH = dim.width + 5;
            ACTION_HEIGHT = dim.height;
        }
        
        actionUI = new WT_Action(RES.getString("TPANEL_DO"), t);
        actionUI.setPreferredSize(new Dimension(ACTION_WIDTH, ACTION_HEIGHT)); // Enough for PI
        add( actionUI );
        actionState = new WT_ActionState(RES.getString("TPANEL_GO_TO"), t);
        add( actionState );
        
        MouseListener pListener = new PopupListener();
        this.addMouseListener(pListener);
    }
    
    Trigger getTrigger() {
        return theTrigger;
    }
    
    private JMenuItem editMI;
    private JMenuItem deleteMI;
    private JMenuItem moveUpMI;
    private JMenuItem moveDownMI;
    
    private void createMenus() {
        popup = new JPopupMenu();
        editMI = new JMenuItem(RES.getString("TPANEL_DD_EDIT"));
        deleteMI = new JMenuItem(RES.getString("TPANEL_DD_DELETE"));
        moveUpMI = new JMenuItem(RES.getString("TPANEL_DD_MOVE_UP"));
        moveDownMI = new JMenuItem(RES.getString("TPANEL_DD_MOVE_DOWN"));
        popup.add(editMI);
        popup.add(deleteMI);
        popup.add(moveUpMI);
        popup.add(moveDownMI);
        
        editMI.addActionListener(e -> {
            new TriggerEditDlg(theTrigger);
            reqdState.update();
            signalLevel.update();
            delay.update();
            actionUI.update();
            actionState.update();
        });
        
        deleteMI.addActionListener(e -> {
            Triggers.getInstance().deleteTrigger(theTrigger);
            parent.removeTriggerUI(this);
        });
        
        moveUpMI.addActionListener(e -> {
            parent.moveUp(this);
        });
        
        moveDownMI.addActionListener(e -> {
            parent.moveDown(this);
        });
    }
    
    public void levelsChanged() {
        if (theTrigger.getLevel() == Trigger.Level.LEVEL1) {
            theTrigger.setTriggerValue( theTrigger.getSensor().getLevel1() );
        } else {
            theTrigger.setTriggerValue( theTrigger.getSensor().getLevel2() );
        }
        signalLevel.update();
    }
    
    class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                moveUpMI.setEnabled(true);
                moveDownMI.setEnabled(true);
                if (parent.isTop(thisPanel)) {
                    moveUpMI.setEnabled(false);
                }
                if (parent.isBottom(thisPanel)) {
                    moveDownMI.setEnabled(false);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    private static class Arrow extends JPanel {
        private final int width;
        
        Arrow(int w) {
            width = w;
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width,16);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(0, 7, width-11, 7);
            
            GeneralPath arrowHead = new GeneralPath();
            arrowHead.moveTo(width-11, 2);
            arrowHead.lineTo(width-1, 7);
            arrowHead.lineTo(width-11, 13);
            arrowHead.closePath();
            g2d.fill(arrowHead);
        }
    }
    
    private static class ThickBar extends JPanel {
        private final int width;
        ThickBar(int w) {
            width = w;
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width,16);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(0, 7, width-1, 7);
        }
    }}
