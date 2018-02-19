package lyricom.sensactConfig.model;

import lyricom.sensactConfig.ui.ActionUI;

/**
 * POJO defining an action.  Immutable.
 * Named SaAction to avoid conflict with java.swing.action.
 * @author Andrew
 */
public class SaAction {
    private final int id;
    private final String name;
    private final int defaultVal;
    private final ActionUI optionUI;
    private final ParameterCheck pCheck;

    public SaAction(int id, String name, int defaultVal, ActionUI optionUI, ParameterCheck pCheck) {
        this.id = id;
        this.name = name;
        this.defaultVal = defaultVal;
        this.optionUI = optionUI;
        this.pCheck = pCheck;
    }
    
    public boolean doParameterCheck(int p) {
        if (pCheck == null) {
            return true;
        } else {
            return pCheck.doCheck(p);
        }
    }
    
    public ActionUI getOptionUI() {
        return optionUI;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDefaultVal() {
        return defaultVal;
    }
    
    public String toString() {
        return name;
    }
}
