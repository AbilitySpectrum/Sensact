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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

/**
 *
 * @author Andrew
 */
public class ScreenInfo {
    private static final Point center;
    private static final Dimension screenDim;

    static {
        screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenDim.width / 2;
        int y = screenDim.height / 2;
        center = new Point(x, y);
    }

    public static Point getCenter() {
        return center;
    }

    public static int getWidth() {
        return screenDim.width;
    }

    public static int getHeight() {
        return screenDim.height;
    }
}
