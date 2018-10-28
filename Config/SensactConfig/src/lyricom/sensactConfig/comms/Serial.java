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

/**
 * Wrapper for the serial interface.
 * 
 * @author Andrew
 */

import com.fazecast.jSerialComm.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.ui.Utils;

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
                    SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 
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
    
    private List<Byte> readData() {
        List<Byte> blist = new ArrayList<>();
        byte[] buffer = new byte[1];
        boolean done = false;
        
        while (!done && !closing) {    
            int readBytes = thePort.readBytes(buffer, 1);
             switch (readBytes) {
                case 0:  // No data available
                    try {
                        Thread.sleep(100); // Wait for data
                    } catch (InterruptedException ex) {
                        // ignore
                    }   
                    break;
                    
                case 1:  // Got a byte!  Check for 'Z' final.
                    blist.add(buffer[0]);
                    if (buffer[0] == Model.END_OF_BLOCK) { // 'Z' terminates a data block.
                        done = true;
                    }   
                    break;
                    
                case -1:  // Error
//                    System.out.print("Read error. Closing - ");System.out.println(closing);
                    if (!closing) {
                        callback.connectionLost();
                    }
                    return null;
                    
                default:
                    break;
            }
            
        }
        return blist;
    }
       
    public int writeList(List<Byte> bytes) {
        return writeData(Utils.listToArray(bytes));
    }
    
    public int writeByte(Byte val) {
        byte[] buffer = new byte[1];
        buffer[0] = (byte) val;
        return writeData(buffer);
    }
    
    public int writeData(byte[] buffer) {
        int writeBytes;
        int totalBytes = 0;
        while (buffer.length > 300) {
            // On the mac we can only send <400 bytes at a time.
            byte[] part = Arrays.copyOfRange(buffer, 0, 300);
            buffer = Arrays.copyOfRange(buffer, 300, buffer.length);
            writeBytes = thePort.writeBytes(part,300);
            try {
                Thread.sleep(100);  // Give the Mac a chance - poor thing!
            } catch (InterruptedException ex) {
                // ignore
            }
            if (writeBytes == -1) {
                if (!closing) {
                    callback.connectionLost();
                }
                return -1;
            }
            totalBytes += writeBytes;
        }
        writeBytes = thePort.writeBytes(buffer, buffer.length);
        if (writeBytes == -1) {
//            System.out.println("Write error");
            if (!closing) {
                callback.connectionLost();
            }
            return -1;
        }
        totalBytes += writeBytes;
        return totalBytes;
    }
    
    public void startDispatchThread() {
        readThread = new Thread(this);
        readThread.start();
    }

    @Override
    public void run() {
        while( !closing) {
            List<Byte> bytes = readData();
            if (bytes != null && bytes.size() > 0) {
                callback.dispatchData(bytes);
            }
        } 
//        System.out.println("Read thread exit");
        readThread = null;
    }
}
