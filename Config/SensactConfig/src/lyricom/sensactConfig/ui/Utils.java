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
import java.awt.Font;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lyricom.sensactConfig.model.ActionType;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.widgets.WT_Action;

/**
 *
 * @author Andrew
 */
public class Utils {
    public static final Font STD_FONT = new Font("Dialog", Font.PLAIN, 12);
    public static final Font STD_BOLD_FONT = new Font("Dialog", Font.BOLD, 12);
    public static final Font STATE_FONT = new Font("Dialog", Font.PLAIN, 14);
    public static final Font TITLE_FONT = new Font("Dialog", Font.BOLD, 16);
    public static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 14);
    
    public static JLabel getLabel(String str, Dimension dim) {
        JLabel l = new JLabel(str);
        l.setPreferredSize(dim);
        return l;
    }
    
    public static JLabel getLabel(String str, int minWidth) {
        JLabel l = new JLabel(str);
        Dimension dim = l.getPreferredSize();
        if (dim.width < minWidth) {
            dim.width = minWidth;
        }
        l.setPreferredSize(dim);
        return l;
    }
    
    public static byte[] listToArray(List<Byte> bytes) {
        byte[] buffer;
        buffer = new byte[ bytes.size() ];
        int i = 0;
        for(Byte b: bytes) {
            buffer[i++] = b;
        }
        return buffer;
    }
    
    public static final int ICON_EMPTY = 1;
    public static final int ICON_BLUETRI = 2;
    private static ImageIcon emptyIcon = null;
    private static ImageIcon blueTriIcon;
    
    public static ImageIcon getIcon(int id) {
        if (emptyIcon == null) {
            // Load icons
            Utils u = new Utils();
            emptyIcon = u.createImageIcon("images/empty.png");
            blueTriIcon = u.createImageIcon("images/bluetri.png");
        }
        switch (id) {
            case ICON_EMPTY:
                return emptyIcon;
            case ICON_BLUETRI:
                return blueTriIcon;
            default:
                return null;
        }
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, "");
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    } 
}
