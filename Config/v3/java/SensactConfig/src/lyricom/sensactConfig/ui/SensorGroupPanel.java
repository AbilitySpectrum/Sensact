package lyricom.sensactConfig.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import lyricom.sensactConfig.model.Sensor;
import lyricom.sensactConfig.model.SensorGroup;

/**
 * A panel that holds information for a Sensor Group
 * and holds a list of the SensorPanel - one for each sensor in the group.
 * 
 * @author Andrew
 */
public class SensorGroupPanel extends JPanel {

    private final List<SensorPanel> triggerSetPanels = new ArrayList<>();
    public SensorGroupPanel(SensorGroup group) {
        super();
        
        setLayout(new BorderLayout());
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        Box b = Box.createVerticalBox();
        b.add(solutionsBtn());
        
        for(Sensor s: group.getMembers()) {
            SensorPanel tscp = new SensorPanel(s);
            triggerSetPanels.add(tscp);
            tscp.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.add(tscp);
        }
        
        p.add(b);
        
        JScrollPane scroll = new JScrollPane(p);
        add(scroll, BorderLayout.CENTER);
    }
    
    private JComponent solutionsBtn() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton solution = new JButton("Solutions");
        p.add(solution);
        return p;
    }  
}
