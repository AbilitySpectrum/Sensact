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
package lyricom.sensactConfig.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import lyricom.sensactConfig.model.Sensor;
import lyricom.sensactConfig.model.Trigger;

/**
 * Displays the required signal level graphically.
 * There is no widgetChanged method here because the
 * widget cannot be changed.
 * 
 * @author Andrew
 */
public class WT_SignalLevel extends W_Base {
    private static final int PANEL_HEIGHT = 16;
    private static final int PANEL_WIDTH = 102;

    private final Trigger theTrigger;
    public WT_SignalLevel(Trigger t) {
        theTrigger = t;
        update();
    }
    
    @Override
    public void update() {
        repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Trigger Value - if Condition != equals.
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 101, 15);
        
        Sensor sen = theTrigger.getSensor();
        int splitPoint = ((theTrigger.getTriggerValue() - sen.getMinval()) * 100)
                / (sen.getMaxval() - sen.getMinval());
        g2d.setColor(Color.GREEN);
        if (theTrigger.getCondition() == Trigger.TRIGGER_ON_HIGH) {
            g2d.fillRect(splitPoint + 1, 1, 100-splitPoint, 14);
        } else if (theTrigger.getCondition() == Trigger.TRIGGER_ON_LOW) {
            g2d.fillRect(1, 1, splitPoint, 14);            
        }
    }
}
