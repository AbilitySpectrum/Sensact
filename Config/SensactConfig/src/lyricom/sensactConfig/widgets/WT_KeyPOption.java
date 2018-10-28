package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 * This is the UI for Key-Press and Key-Release commands.
 * 'press_release' is the overlay which codes the press or release command.
 * On the arduino this is treated within the HID keyboard action.
 * @author Andrew
 */
public class WT_KeyPOption extends W_TextField {

    private final Trigger theTrigger;
    private final int press_release;
    public WT_KeyPOption(String label, int pr, Trigger t) {
        super(label, 3);
        theTrigger = t;
        press_release = pr;
        update();
    }
    
    @Override
    public void widgetChanged() {
        String text = field.getText();
        int pval = 0;
        for(int i=0; i < text.length(); i++) {
            pval <<= 8;
            pval += (int)(text.charAt(i));
        } 
        pval |= press_release;
        theTrigger.setActionParam(pval);
    }

    @Override
    public void update() {
        StringBuilder sbld = new StringBuilder();
        int ap = theTrigger.getActionParam();
        // Skip first char.  It is FF or FE press/release marker.
        for(int i = 1; i<4; i++) {
            int ch = (ap >> (8 * (3-i))) & 0xff;

            if (ch != 0) {
                sbld.append((char) ch);
            }
        }

        field.setText(sbld.toString());       
    }
}
