package lyricom.sensactConfig.model;

import java.util.ResourceBundle;

/**
 * Access to the model-related resource bundle.
 * 
 * @author Andrew
 */
public class MRes {
    private static final ResourceBundle RES = ResourceBundle.getBundle("model");    

    static String getStr(String key) {
        return RES.getString(key);
    }
}
