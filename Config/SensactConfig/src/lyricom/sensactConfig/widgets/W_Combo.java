package lyricom.sensactConfig.widgets;

import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * JCombo box handler.
 * Creates a combo box with a label in front.
 * Does any styling that should be common to all combo boxes.
 * 
 * @author Andrew
 */
public class W_Combo extends W_Base {
    protected JComboBox theBox;
    
    public W_Combo(String label, Object[] items) {
        super();
        
        theBox = new JComboBox();
        int length = items.length;
        if (length > 20) length = 15;
        theBox.setMaximumRowCount(length);
        
        for(Object obj: items) {
            theBox.addItem(obj);
        }
        
        add(new JLabel(label));
        add(theBox);
        theBox.addActionListener(e -> widgetChanged());
    }
    
    // Alternate constructor for Lists of items.
    public W_Combo(String label, List<? extends Object> items) {
        this (label, items.toArray());
    }

}
