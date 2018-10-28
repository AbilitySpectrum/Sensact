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

import com.fazecast.jSerialComm.SerialPort;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 *
 * @author Andrew
 */
public class PortSelectionDlg extends JDialog {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");

    private boolean wasCancelled = false;
    private SerialPort selection = null;
    private final JDialog thisDlg;
    
    public PortSelectionDlg(SerialPort[] ports) {
        super((JFrame) null, true);
        
        thisDlg = this;
        
        setLayout(new BorderLayout());
        
        add(titleLine(), BorderLayout.NORTH);        
        add(options(ports), BorderLayout.CENTER);       
        add(cancelBtn(), BorderLayout.SOUTH);
        
        pack();
        
        // Center on screen
        Dimension dim = getPreferredSize();
        Point center = ScreenInfo.getCenter();
        setLocation(center.x-dim.width/2, center.y-dim.height/2);
        
        setVisible(true);
    }
    
    public boolean wasCancelled() {
        return wasCancelled;
    }
    
    public SerialPort getPort() {
        return selection;
    }
    
    private JComponent titleLine() {
        JPanel p = new JPanel();
        JLabel title = new JLabel(RES.getString("PORT_SEL_TITLE"));
        title.setFont(new Font("Serif", Font.BOLD, 16));
        p.add(title);
        return p;
    }
    
    private JComponent options(SerialPort[] ports) {
        GridLayout grid = new GridLayout(0,1);
        JPanel panel = new JPanel(grid);
        for(SerialPort p: ports) {
            JButton btn = new JButton(p.getDescriptivePortName());
            panel.add( btn );
            final SerialPort thePort = p;
            btn.addActionListener(e -> {
                selection = thePort;
                thisDlg.dispose();
            });
        }
        return panel;
    }
    
    private JComponent cancelBtn() {
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 5, 10);
        JPanel p = new JPanel(layout);
        
        JButton b = new JButton(RES.getString("BTN_CANCEL"));
        b.addActionListener(e -> {
            wasCancelled = true;
            thisDlg.dispose();
        });
        p.add(b);
        return p;       
    }
}
