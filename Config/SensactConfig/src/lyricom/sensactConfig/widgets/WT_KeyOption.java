package lyricom.sensactConfig.widgets;

import lyricom.sensactConfig.model.Trigger;

/**
 *
 * @author Andrew
 */
public class WT_KeyOption extends W_TextField {

    private final Trigger theTrigger;
    public WT_KeyOption(String label, Trigger t) {
        super(label, 4);
        theTrigger = t;
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
        theTrigger.setActionParam(pval);
    }

    @Override
    public void update() {
        StringBuilder sbld = new StringBuilder();
        int ap = theTrigger.getActionParam();
        for(int i = 0; i<4; i++) {
            int ch = (ap >> (8 * (3-i))) & 0xff;

            if (ch != 0) {
                sbld.append((char) ch);
            }
        }

        field.setText(sbld.toString());       
    }
}
