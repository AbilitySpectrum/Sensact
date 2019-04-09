/*
 * This file is part of the Sensact Configuration software.
 *
 * Sensact Configuration software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact Configuration software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom.sensactConfig.comms;

import com.fazecast.jSerialComm.SerialPort;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lyricom.sensactConfig.model.IOError;
import lyricom.sensactConfig.model.InStream;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.Triggers;
import lyricom.sensactConfig.ui.MainFrame;
import lyricom.sensactConfig.ui.PortSelectionDlg;
import lyricom.sensactConfig.ui.SensorPanel;

/**
 * The Connection singleton class manages the creation of
 * the serial connection and any required reconnection.
 * 
 * If connection cannot be established this class will call
 * System.exit directly.
 * 
 * @author Andrew
 */
public class Connection implements SerialCallback {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");
    private static Connection instance = null;
    
    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }
    
    private final Serial serial;
    private Semaphore versionSemaphore; 
    private String versionString;
    private int versionID;
    private boolean connected = false;
    
    private Connection() {
        serial = Serial.getInstance();
    }
    
    public void establishConnection() {
        versionString = RES.getString("CM_DEFAULT_VERSION");
        serial.setCallback(this);
        doConnection(true);
        connected = true;
    }
    
    public String getVersionString() {
        return versionString;
    }
    
    public int getVersionID() {
        return versionID;
    }
    
    private void doConnection(boolean auto) {
        boolean connected;
        boolean connectionSuccess = false;
        while( ! connectionSuccess) {
            SerialPort port = doPortSelection(auto);
            
            if (serial.open_port(port)) {
                connected = true;
                serial.startDispatchThread();
            } else {
                connected = false;
                confirm(RES.getString("CMT_FAILURE"),
                    RES.getString("CM_FAIL_RETRY")
                );
            }
            
            if (connected) {
                try {
                    Thread.sleep(1000); // Wait for ATMega to reboot.
                    versionSemaphore = new Semaphore(1);
                    versionSemaphore.acquire();
                    serial.writeByte(Model.CMD_VERSION);
                    if (versionSemaphore.tryAcquire(1, 2000, TimeUnit.MILLISECONDS)) {
                        connectionSuccess = true;
                    } else {
                        confirm(RES.getString("CMT_ERROR"),
                            RES.getString("CM_NOT_SENSACT")
                        );                    
                        
                    }
                } catch (InterruptedException ex) {
                    confirm(RES.getString("CMT_ERROR"),
                            RES.getString("CM_NOT_SENSACT")
                    );                    
                }
                if (!connectionSuccess) {
                    serial.close();  // Did not get version #.  Close this port and try another.
                }
            }
        }
    }
    
    private SerialPort doPortSelection(boolean auto) {
        SerialPort[] ports = serial.get_list();
        
        while (ports.length == 0) {
            confirm(RES.getString("CMT_ERROR"), RES.getString("CM_NO_PORTS"));
            auto = false;
            ports = serial.get_list();
        }
        
        if (auto) {
            for(SerialPort p: ports) {
                if (p.getDescriptivePortName().contains("Leonardo")) {
                    return p;
                }
            }
        }
        
        PortSelectionDlg dlg = new PortSelectionDlg(ports);
        if (dlg.wasCancelled()) {
            System.exit(0);
        } 
        
        return dlg.getPort();
    }
    
    /*
     * Ask a yes/no question.  If the answer is no - exit.
     */
    private void confirm(String title, String message) {
        int val = JOptionPane.showConfirmDialog(
            null, message, title, JOptionPane.YES_NO_OPTION);
        if (val == JOptionPane.NO_OPTION) {
            System.exit(0);
        }    
        
    }

    /*
     * SerialCallback methods.
     *   dispatchData - get a data packet and figures out what to do with it.
     *   connectionLost - called when the connection is lost.
    */
    @Override
    public void dispatchData(List<Byte> bytes) {
        if (bytes.size() == 0) {
            return;
        }
        // Process Version Number
        if (bytes.get(0).equals(Model.CMD_VERSION)) {
            byte[] sub = new byte[bytes.size() - 2];
            for(int i=1; i < (bytes.size()-1); i++) {
                sub[i-1] = bytes.get(i);
            }
            versionString = new String(sub, Charset.defaultCharset());
            // A tedious conversion to version number.
            // 'A.B' becomes A * 100 + B.
            // There is nothing here to verify the version number format.
            int versionNum = 0;
            for (int i=0; i<sub.length; i++) {
                if (sub[i] >= (byte) '0' && sub[i] <= (byte) '9') {
                    versionNum = versionNum * 10 + (sub[i] - (byte) '0');
                } else if (sub[i] == (byte) '.') {
                    // Save major version number.
                    versionID = versionNum * 100;
                    versionNum = 0;
                }
                // Add on minor version number
                versionID += versionNum;
            }
            
            if (versionSemaphore != null) {
                versionSemaphore.release();
            }
                    
        // Process Sensor Reporting Data.
        } else if (bytes.get(0).equals(Model.START_OF_DATA)) {
            if (!connected) return;
            
            InStream in = new InStream(bytes);
            try {
                Model.updateSensorValues(in);
            } catch (IOError e) {
                System.out.println(RES.getString("CM_UNKNOWN") + ' ' + e.getMessage());
            }
            
        // Process Trigger Data
        } else if (bytes.get(0).equals(Model.START_OF_TRIGGERS)) {
            if (!connected) return;
            InStream input = new InStream(bytes);
            try {
                Triggers.getInstance().loadTriggers(input);
                SensorPanel.reloadTriggers();    
                Triggers.DATA_IN_SYNC = true;
            } catch(IOError e) {
                JOptionPane.showMessageDialog(MainFrame.TheFrame, 
                        RES.getString("CM_DATA_ERROR") + e.getMessage(),
                        RES.getString("CMT_DATA_ERROR"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void connectionLost() {
        System.out.println("Connection Lost");
        serial.close();
        connected = false;
        // Reconnect in a new thread??
        confirm(RES.getString("CMT_LOST"), RES.getString("CM_RECONNECT"));
        JOptionPane.showMessageDialog(null, 
                RES.getString("CM_INSTRUCT"), RES.getString("CMT_RECONNECT"),
                JOptionPane.INFORMATION_MESSAGE);
        
        SwingUtilities.invokeLater(() -> {
            establishConnection();
        });
    }

}
