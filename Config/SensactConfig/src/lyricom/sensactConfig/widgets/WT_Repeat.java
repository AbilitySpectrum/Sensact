package lyricom.sensactConfig.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_Repeat extends W_Base implements ActionListener {

    private final Trigger theTrigger;
    private final JCheckBox repeatBox;
    
    public WT_Repeat(String label, Trigger t) {
        super();
        theTrigger = t;
        repeatBox = new JCheckBox();
        add(new JLabel(label));
        add(repeatBox);        
        update();
        repeatBox.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        theTrigger.setRepeat(repeatBox.isSelected());
/*        if (theTrigger.isRepeat()) {
            System.out.println("Repeat is TRUE");
        } else {
            System.out.println("Repeat is FALSE");
        } */
    }
    
    @Override
    public void update() {
        repeatBox.setSelected(theTrigger.isRepeat());        
    }
}
