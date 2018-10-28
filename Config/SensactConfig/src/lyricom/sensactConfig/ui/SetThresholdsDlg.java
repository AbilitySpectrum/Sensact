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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import lyricom.sensactConfig.comms.Serial;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.Sensor;
import lyricom.sensactConfig.model.SensorSignalLevelChangeListener;

/**
 *
 * @author Andrew
 */
public class SetThresholdsDlg extends JDialog implements SensorSignalLevelChangeListener {
    private final Sensor theSensor;
    private final SetThresholdsDlg thisDlg;
    
    private boolean cancelled;
    private int level1;
    private int level2;
    
    private mySlider currentSlider;
    private JSlider gtSlider;
    private JSlider ltSlider;
    
    private SigLevelBuffer sigBuf;
    
    public SetThresholdsDlg(Sensor s) {
        super(MainFrame.TheFrame, true);
        theSensor = s;
        thisDlg = this;
        cancelled = false;
        level1 = s.getLevel1();
        level2 = s.getLevel2();
        
        sigBuf = new SigLevelBuffer(20);
        
        setLayout(new BorderLayout());
        
        add(header(), BorderLayout.NORTH);
        
        add(sliders(), BorderLayout.CENTER);
        
        add(buttons(), BorderLayout.SOUTH);
        
        addWindowListener(new WindowAdapter() {
            // This is called when the close button (at top right on windows)
            // is pressed.  Treat this as a cancel.
            @Override
            public void windowClosing(WindowEvent e) {
                theSensor.removeListener();
                Serial.getInstance().writeByte(Model.CMD_VERSION);
                cancelled = true;
                thisDlg.dispose();
            }
        });
        
        pack();
        // Center on screen
        Dimension dim = getPreferredSize();
        Point center = ScreenInfo.getCenter();
        setLocation(center.x-dim.width/2, center.y-dim.height/2);
        
        // Do not do this before the sliders are set up to receive the values.
        theSensor.setListener(this);
        Serial.getInstance().writeByte(Model.CMD_DISPLAY);
        
        setVisible(true);
    }
    
    public boolean wasCancelled() {
        return cancelled;
    }
    
    public int getLevel1() {
        return level1;
    }
    
    public int getLevel2() {
        return level2;
    }
    
    private JComponent header() {
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 5, 10);
        JPanel p = new JPanel(layout);
        JLabel l = new JLabel("Set Thresholds for " + theSensor.getName());
        l.setFont(Utils.TITLE_FONT);
        p.add(l);
////        p.add(new JSeparator(JSeparator.HORIZONTAL));
        return p;
    }
    
    private JComponent buttons() {
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 5, 10);
        JPanel p = new JPanel(layout);
        JButton done = new JButton("Done");
        JButton cancel = new JButton("Cancel");
        done.addActionListener(e -> {
            theSensor.removeListener();
            Serial.getInstance().writeByte(Model.CMD_VERSION);
            level1 = gtSlider.getValue();
            level2 = ltSlider.getValue();
            thisDlg.dispose();
        });
        cancel.addActionListener(e -> {
            theSensor.removeListener();
            Serial.getInstance().writeByte(Model.CMD_VERSION);
            cancelled = true;
            thisDlg.dispose();
        });
//        p.add(new JSeparator(JSeparator.HORIZONTAL));
        getRootPane().setDefaultButton(done);
        p.add(done);
        p.add(cancel);
        return p;
    }
    
    private JComponent sliders() {
        Box b = Box.createVerticalBox();
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        p.add(newLabel("Current Signal Level"));
        currentSlider =  new mySlider(theSensor.getMinval(), theSensor.getMaxval(), 0);
//        currentSlider = newSlider(ltValue);
        p.add(currentSlider);
        b.add(p);

        p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        p.add(newLabel("Level 1 threshold"));
        gtSlider = newSlider(level1);
        p.add(gtSlider);
        b.add(p);
        
        p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        p.add(newLabel("Level 2 threshold"));
        ltSlider = newSlider(level2);
        p.add(ltSlider);
        b.add(p);
        
        p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        p.add(newLabel(""));
        p.add(new JLabel(" MIN"));
        JLabel max = new JLabel("MAX ");
        Dimension d = max.getPreferredSize();
        d.width = 450;
        max.setPreferredSize(d);
        max.setHorizontalAlignment(JLabel.RIGHT);
        p.add(max);
        b.add(p);
        b.add(explanitoryText());
        return b;
    }
    
    private JPanel explanitoryText() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextArea area = new JTextArea(7, 40);
        area.setEditable(false);
        area.setBackground(p.getBackground());
        
        area.setText("Use this interface to set the signal levels for a sensor.\n"
                + "The two sliders let you set the levels.\n"
                + "The top line shows the current signal level coming from the sensact.\n"
                + "The color of the top line indicates the signal level relative to "
                + "the sliders:\n"
                + "  RED - the signal is higher than both of the sliders.\n"
                + "  GREEN - the signal is lower than both of the sliders.\n"
                + "  BLUE - the signal level is between the two sliders.");
        p.add(Box.createHorizontalStrut(20));
        p.add(area);
        return p;
    }
    
    private JLabel newLabel(String s) {
        JLabel l = new JLabel(s);
        Dimension d = l.getPreferredSize();
        d.width = 150;
        l.setPreferredSize(d);
        l.setMaximumSize(d);
        return l;
    }
    
    private JSlider newSlider(int val) {
        
        JSlider s = new JSlider(JSlider.HORIZONTAL, theSensor.getMinval(), 
                theSensor.getMaxval(), val);
        s.setForeground(Color.red);
//        s.setMajorTickSpacing((theSensor.getMaxval() - theSensor.getMinval())/10);
//        s.setPaintTicks(true);
        Dimension d = s.getPreferredSize();
        d.width = 500;
        s.setPreferredSize(d);
        s.setMaximumSize(d);
        
        return s; 
    }

    @Override
    public void newSensorValue(int value) { 
        
        sigBuf.addValue(value);

        level1 = gtSlider.getValue();
        level2 = ltSlider.getValue();
        
        int gt, lt;
        if (level1 > level2) {
            gt = level1;
            lt = level2;
        } else {
            gt = level2;
            lt = level1;
        }
        if (value > gt) {
            currentSlider.setColor(Color.RED);
        } else if (value < lt) {
            currentSlider.setColor(Color.GREEN);
        } else {
            currentSlider.setColor(Color.BLUE);
        }
        currentSlider.setValue(value, sigBuf.getMaxVal(), sigBuf.getMinVal());
    }
    
   
    private class mySlider extends JPanel {
        private static final int PWIDTH = 500;
        int max;
        int min;
        int val;
        int bufmin;
        int bufmax;
        Color color;
        
        mySlider(int min, int max, int val) {
            super();
//            super(JSlider.HORIZONTAL, min, max, val);
            this.max = max;
            this.min = min;
            this.val = val;
            color = Color.RED;
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(PWIDTH, 20);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);  // Clears the area.
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.GRAY);
            g2d.drawRect(5,0,PWIDTH-12,10);
            int position = ((val - min) * (PWIDTH-14)) / (max - min);
            g2d.setColor(color);
            g2d.fillRect(6, 2, position, 7);
 /* Min/Max buffer level display - hold off for now.          
            g2d.setColor(Color.BLACK);
            int maxPosition = ((bufmax - min) * (PWIDTH-14)) / (max-min) + 6;
            g2d.fillRect(maxPosition, 1, 2, 9);
            int minPosition = ((bufmin - min) * (PWIDTH-14)) / (max-min) + 6;
            g2d.fillRect(minPosition, 1, 2, 9);
*/
        }
        
        public void setValue(int val, int bufmin, int bufmax) {
            this.val = val;
            this.bufmin = bufmin;
            this.bufmax = bufmax;
            // Accelerometer can deliver values outside the meter's range.
            // They get chopped.
            if (this.val > max) this.val = max;
            if (this.bufmin > max) this.bufmin = max;
            if (this.bufmax > max) this.bufmax = max;
            if (this.val < min) this.val = min;
            if (this.bufmin < min) this.bufmin = min;
            if (this.bufmax < min) this.bufmax = min;
            repaint();
        }
        
        public void setColor(Color c) {
            color = c;
        }

    }
}
