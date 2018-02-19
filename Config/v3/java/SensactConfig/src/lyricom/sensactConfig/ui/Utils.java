package lyricom.sensactConfig.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.JLabel;

/**
 *
 * @author Andrew
 */
public class Utils {
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
}
