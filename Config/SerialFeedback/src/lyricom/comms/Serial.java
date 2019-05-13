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

/**
 * Wrapper for the serial interface.
 * 
 * @author Andrew
 */

import com.fazecast.jSerialComm.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Serial implements Runnable {
    static private Serial instance = null;
    static public Serial getInstance() {
        if (instance == null) {
            instance = new Serial();
        }
        return instance;
    }
    
    private SerialPort thePort;
    private volatile boolean closing;
    private SerialCallback callback;
    private Thread readThread = null;
    
    private Serial() {        
    }
    
    public SerialPort[] get_list() {
        return SerialPort.getCommPorts();
    }
    
    public boolean open_port(SerialPort port) {
        closing = false;
        if ( port.openPort() ) {
            port.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_BLOCKING, 
                    0, 
                    0);
            thePort = port;
            return true;
        } else {
            return false;
        }
    }
    
    public void setCallback(SerialCallback cb) {
        callback = cb;
    }
    
    public String getPortName() {
        if (thePort == null) {
            return "<none>";
        } else {
            return thePort.getDescriptivePortName();
        }
    }
    
    public boolean connectionCheck() {
        // Try to write a byte to Sensact
        // If the write fails it means we have been disconnected.
        // This seems to be the only way to detect disconnection.
        byte[] data = new byte[1];
        data[0] = ' ';
        int writeBytes = thePort.writeBytes(data,1);
        if (writeBytes != 1) {
            if (!closing) {
                callback.connectionLost();
            }
            return false;
        }
        return true;
    }
    
    public void close() {
        if (thePort != null) {
            closing = true;
            thePort.closePort();
            thePort = null;
            if (readThread != null) {
                readThread.interrupt();
            }
        }
    }
    
    private String readByte() {
        byte[] buffer = new byte[1];
        int readBytes = 0;
        
        while (readBytes == 0) {
            readBytes = thePort.readBytes(buffer, 1);
            if (readBytes == 0) {
                try {
                    Thread.sleep(100); // Wait for data
                } catch (InterruptedException ex) {
                    // ignore
                }  
            }
        }
        
        if (readBytes == 1) { // Got a byte
            String ch = new String(buffer, Charset.defaultCharset());
            return ch; 
            
        } else { // error - never seems to happen
            callback.connectionLost();
            return null;
        }
    }
          
    public void startDispatchThread() {
        readThread = new Thread(this);
        readThread.start();
    }

    @Override
    public void run() {
        String ch = "";
        while( !closing && ch != null) {
            ch = readByte();
            if (ch != null) {
                callback.dispatchData(ch);
            }
        } 
        readThread = null;
    }
}
