package lyricom.sensactConfig.ui;

import javax.swing.JTabbedPane;

/**
 * Used to control the status indication (an icon in the title) of an 
 * individual tabbed panel. This allows a SensorGroupPanel to control 
 * its status without giving it full access to the tabbed pane object.
 * 
 * @author Andrew
 */
public class PaneStatusCntrl {
    private final JTabbedPane pane;
    private final int index;
    
    PaneStatusCntrl(JTabbedPane pane, int index) {
        this.pane = pane;
        this.index = index;
    }
    
    void panelContainsTriggers() {
        pane.setIconAt(index, Utils.getIcon(Utils.ICON_BLUETRI));
    }
    
    void panelIsEmpty() {
        pane.setIconAt(index, Utils.getIcon(Utils.ICON_EMPTY));        
    }
}
