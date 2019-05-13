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
package lyricom.comms;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.SwingUtilities;
import lyricom.ui.PortSelectionDlg;
import lyricom.ui.TheFrame;

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
    private static Connection instance = null;
    
    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }
    
    private final Serial serial;
    private boolean connected = false;
    
    private Connection() {
        serial = Serial.getInstance();
    }
    
    public boolean establishConnection(String port) {
        serial.setCallback(this);
        return doConnection(port);
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public int portCount() {
        SerialPort[] ports = serial.get_list();
        return ports.length;
    }
    
    public String portSelection() {
       SerialPort[] ports = serial.get_list();
       
       if (ports.length == 0) return null;
       
       PortSelectionDlg dlg = new PortSelectionDlg(ports);
       if (dlg.wasCancelled()) return null;
       
       SerialPort thePort = dlg.getPort();
       return thePort.getDescriptivePortName();         
    }
        
    private boolean doConnection(String portName) {
            SerialPort[] ports = serial.get_list();
            SerialPort thePort = null;
            
            // Find the port
            for(SerialPort p: ports) {
                if (p.getDescriptivePortName().contains(portName)) {
                    thePort = p;
                }
            }
            
            if (thePort == null) {
                return false;
            }
            if (thePort == null) {
                return false;
            }
            
            if (serial.open_port(thePort)) {
                serial.startDispatchThread();
                connected = true;
                return true;
            } else {
                return false;
            }     
    }
    
    /*
     * SerialCallback methods.
     *   dispatchData - get a data packet and figures out what to do with it.
     *   connectionLost - called when the connection is lost.
    */
    @Override
    public void dispatchData(String ch) {
        TheFrame.getInstance().newMappingChar(ch);
    }

    @Override
    public void connectionLost() {
        serial.close();
        connected = false;
    }
    
    public boolean connectionCheck() {
        return serial.connectionCheck();
    }

}
