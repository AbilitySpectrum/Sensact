package lyricom.sensactConfig.solutions;

import javax.swing.JDialog;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.ui.MainFrame;

/**
 *
 * @author Andrew
 */
public class SolutionsUI extends JDialog {
    
    private final SensorGroup theGroup;
    
    public SolutionsUI(SensorGroup sg) {
        super(MainFrame.TheFrame, true);
        theGroup = sg;
        
        pack();
        
        setVisible(true);
    }
}
