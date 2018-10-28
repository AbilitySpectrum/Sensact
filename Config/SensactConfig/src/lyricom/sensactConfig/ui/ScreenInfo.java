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
