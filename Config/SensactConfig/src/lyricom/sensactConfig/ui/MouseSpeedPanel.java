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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.*;
import lyricom.sensactConfig.model.MouseSpeedTransfer;
import lyricom.sensactConfig.model.MouseSpeedTransferInterface;
import lyricom.sensactConfig.widgets.WT_MouseTimer;

/**
 * @author Andrew
 */
public class MouseSpeedPanel extends JPanel implements MouseSpeedTransferInterface {

    SpeedSlider speed1;
    SpeedSlider speed2;
    SpeedSlider speed3;
    WT_MouseTimer timer1;
    WT_MouseTimer timer2;
    
    public MouseSpeedPanel() {
        super(new FlowLayout(FlowLayout.LEFT));
        
        Box box = Box.createVerticalBox();
        
        JLabel title = new JLabel("Mouse Speed");
        title.setFont(Utils.TITLE_FONT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(title);
        
        speed1 = new SpeedSlider("Start speed:", 420);
        speed2 = new SpeedSlider("change to:", 480);
        speed3 = new SpeedSlider("change to:", 540);
               
        box.add( speed1 );
        box.add(timer1());
        box.add( speed2 );
        box.add(timer2());
        box.add( speed3 );
        add(box);
        
        MouseSpeedTransfer.getInstance().registerUIComponent(this);
    }
    
    private JPanel timer1() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel start = new JLabel("after");
        timer1 = new WT_MouseTimer("");
        timer1.setNewValue(500);
        JLabel after = new JLabel("milliseconds");
        
        p.add(start);
        p.add(timer1);
        p.add(after);
        return p;
    }
    
    private JPanel timer2() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel start = new JLabel("after on additional");
        timer2 = new WT_MouseTimer("");
        timer2.setNewValue(500);
        JLabel after = new JLabel("milliseconds");
        
        p.add(start);
        p.add(timer2);
        p.add(after);
        return p;
    }

    @Override
    public int[] getSpeeds() {
        int[] values = new int[5];
        values[0] = speed1.getValue();
        values[1] = speed2.getValue();
        values[2] = speed3.getValue();
        values[3] = timer1.getNewValue();
        values[4] = timer2.getNewValue();
        return values;
    }

    @Override
    public void setSpeeds(int[] values) {
        speed1.setValue(values[0]);
        speed2.setValue(values[1]);
        speed3.setValue(values[2]);
        timer1.setNewValue(values[3]);
        timer2.setNewValue(values[4]);
    }
  
    /*
     The scale used is logarithmic so that the "Very slow", "Slow"
     etc. sections are roughly the same size.
     To translate to real terms, divide the value by 100 and then:
      Log Value          Pixels per sec
        3.6                 33                 
        4.2                 66                
        4.8                133                 
        5.4                222                  
        6.0                416                 
        6.5                666     
     */
    private class SpeedSlider extends JPanel {
    
        private final JLabel speedLabel;
        private final JSlider slider;
        
        SpeedSlider(String label, int initialValue) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 20));
            
            setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel title = new JLabel(label);
            Dimension dim = title.getPreferredSize();
            dim.width = 80;
            title.setPreferredSize(dim);
            add( title );
            speedLabel = new JLabel("");
            
            slider = new JSlider(JSlider.HORIZONTAL, 350, 650, 400);
            Dimension d = slider.getPreferredSize();
            d.width = 300;
            slider.setPreferredSize(d);
            slider.setMaximumSize(d);
            slider.setValue(initialValue);
            
            add(slider);
            add(speedLabel);
            
            setSpeedLabel();
            
            slider.addChangeListener(e -> setSpeedLabel());            
        }
        
        private void setSpeedLabel() {           
            int val = slider.getValue();
            if (val < 400) {
                speedLabel.setText("Very slow");
            } else if (val < 470) {
                speedLabel.setText("Slow");
            } else if (val < 540) {
                speedLabel.setText("Medium");
            } else if (val < 610) {
                speedLabel.setText("Fast");
            } else {
                speedLabel.setText("Very fast");
            }
        }
        
        public int getValue() {
            return slider.getValue();
        }
        
        public void setValue(int val) {
            slider.setValue(val);
            setSpeedLabel();
        }
    }
}
