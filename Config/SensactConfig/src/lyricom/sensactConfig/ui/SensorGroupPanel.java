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
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import lyricom.sensactConfig.model.Sensor;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.solutions.SolutionRegister;
import lyricom.sensactConfig.solutions.SolutionsUI;

/**
 * A panel that holds information for a Sensor Group
 * and holds a list of the SensorPanel - one for each sensor in the group.
 * 
 * @author Andrew
 */
public class SensorGroupPanel extends JPanel {

    private final List<SensorPanel> sensorPanels = new ArrayList<>();
    private final SensorGroup thisGroup;
    private final PaneStatusCntrl statusControl;
    private boolean hasTriggers;
    
    public SensorGroupPanel(SensorGroup group, PaneStatusCntrl psc) {
        super();
        
        setLayout(new BorderLayout());
        
        thisGroup = group;
        statusControl = psc;
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        Box b = Box.createVerticalBox();
        if (SolutionRegister.getInstance()
                .getApplicableSolutions(group.getName()).length > 0) {        
            b.add(solutionsBtn());
        } else {
            b.add(new JLabel(" "));
        }
        
        for(Sensor s: group.getMembers()) {
            SensorPanel tscp = new SensorPanel(s, this);
            sensorPanels.add(tscp);
            tscp.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.add(tscp);
        }
        
        p.add(b);
        
        JScrollPane scroll = new JScrollPane(p);
        add(scroll, BorderLayout.CENTER);
    }
    
    void checkPanelStatus() {
        boolean hasT = false;   // assumed to start
        for(SensorPanel sp: sensorPanels) {
            if (sp.getTriggerCount() > 0) {
                hasT = true;
            } 
        }
        
        if (hasTriggers != hasT) {
            // Status has changed
            hasTriggers = hasT;
            if (hasTriggers) {
                statusControl.panelContainsTriggers();
            } else {
                statusControl.panelIsEmpty();
            }
        }
    }
    
    private JComponent solutionsBtn() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton solution = new JButton("Solutions");
        solution.addActionListener(e -> {
            new SolutionsUI(thisGroup);
        });
        p.add(solution);
        return p;
    }  
}
