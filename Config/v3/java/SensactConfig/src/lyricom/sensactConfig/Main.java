/*
 * Main
 */

package lyricom.sensactConfig;

import lyricom.sensactConfig.ui.MainFrame;
import javax.swing.SwingUtilities;
import lyricom.sensactConfig.comms.Connection;
import lyricom.sensactConfig.model.Model;

/**
 *
 * @author Andrew
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         
        Connection conn = Connection.getInstance();
        conn.establishConnection();
        
        Model.initModel(conn.getVersionID());
        
        SwingUtilities.invokeLater(() -> {
            new MainFrame(conn.getVersionString());
        });
    }

}
