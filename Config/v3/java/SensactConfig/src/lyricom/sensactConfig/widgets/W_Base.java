package lyricom.sensactConfig.widgets;

import java.awt.FlowLayout;
import javax.swing.JPanel;

/**
 * A base class for all Widgets.
 * Establishes the layout for all widget panels.
 * Establishes the update method which will be overridden by subclasses.
 * 
 * @author Andrew
 */
public class W_Base extends JPanel {

    public W_Base() {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0));
    }
    
    /*
     * This method is called when a change to the widget has been
     * detected. It is responsible for modifying the underlying
     * trigger to match the widget.
    */
    public void widgetChanged() {
        
    }
    
    /*
     * This method is called when the underlying trigger has changed.
     * It is responsible for updating the widget from the state of the
     * trigger.
    */
    public void update() {
        
    }
}
