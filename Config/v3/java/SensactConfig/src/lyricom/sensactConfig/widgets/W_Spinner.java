package lyricom.sensactConfig.widgets;

import java.awt.Color;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.border.EmptyBorder;
import lyricom.sensactConfig.ui.Utils;

/**
 *
 * @author Andrew
 */
public class W_Spinner extends W_Base {
    
    protected final JSpinner spinner;
    protected final SpinnerListModel spinModel;

    public W_Spinner(String label, String[] values) {
        super();
                
        spinModel = new SpinnerListModel(values);
        spinner = new JSpinner(spinModel);
        spinner.setFont(Utils.STATE_FONT);
        spinner.setBackground(Color.BLUE);
        JFormattedTextField fld = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        fld.setEditable(false);
        fld.setColumns(2);
        fld.setBorder( new EmptyBorder(0,0,0,3));
        add(new JLabel(label));
        add(spinner);        
        spinner.addChangeListener(e -> widgetChanged());        
    }
}
