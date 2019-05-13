/*
 * This file is part of the Sensact software.
 *
 * Sensact software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom.ui;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * Defines the bounds of the usable window - subtracting the space used
 * by menu bars.
 * @author Andrew
 */
public class ScreenInfo {

    private static int top, bottom, left, right;
    
    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        top = bounds.y + insets.top;
        left = bounds.x + insets.left;
        bottom = bounds.y + bounds.height - insets.bottom;
        right = bounds.x + bounds.width - insets.right;
    }
    
    static int getTop() { return top; }
    static int getBottom() { return bottom; }
    static int getLeft() { return left; }
    static int getRight() { return right; }
}
