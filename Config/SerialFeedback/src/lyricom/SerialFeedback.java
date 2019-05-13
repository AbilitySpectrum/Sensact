/*
 * This file is part of the Sensact software.
 *
 * Sensact software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import lyricom.comms.Connection;
import lyricom.ui.MyProps;
import lyricom.ui.TheFrame;

/**
 *
 * @author Andrew
 */
public class SerialFeedback extends TimerTask {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            MyProps.getInstance().load();
        } catch(IOException ex) {
            System.out.println("Unable to open config.txt");
            System.exit(1);
        } catch (URISyntaxException ex) {
            System.out.println("File path problem");
            System.exit(1);            
        } catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);            
        }
         
        Timer t = new Timer();
        TimerTask task = new SerialFeedback();
        t.scheduleAtFixedRate(task, 1000, 5000);
        
        SwingUtilities.invokeLater(() -> {
            TheFrame.getInstance();
        });
    }
    
    int connectionAttempts = 0;
    int lastPortCount = 0;
    String tmp;
    
    @Override
    public void run() {
        Connection conn = Connection.getInstance();

        String portName = MyProps.getInstance().getPort();

        // If connections have been failing offer the user
        // a list of available ports to pick from.
        if (connectionAttempts >= 3) {
            if (conn.portCount() <= lastPortCount) {
                // No new ports since the last connection attempt
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        tmp = conn.portSelection();
                    });
                } catch(Exception ex) {
                    tmp = null;
                }

                if (tmp != null) {
                    portName = tmp;
                } else {
                    // This makes sure we do not ask again immediately.
                    connectionAttempts = 0;
                    lastPortCount = conn.portCount();
                }
            }
        }

        if (conn.isConnected()) {
            // Check to see if we are still connected
            if (!conn.connectionCheck()) {
                TheFrame.getInstance().newMessage("Not Connected");                                                            
            }
            return;

        } else {
            // Not connected.  Try to connect.
            if (conn.establishConnection(portName)) {
                TheFrame.getInstance().newMessage("Connected");  
                connectionAttempts = 0;
            } else {
                connectionAttempts++;
                lastPortCount = conn.portCount();
                TheFrame.getInstance().newMessage("Not Connected");                                        
            }
            return;
        }
    }
}
