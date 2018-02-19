package lyricom.sensactConfig.comms;

/**
 * Wrapper for the serial interface.
 * 
 * @author Andrew
 */

import com.fazecast.jSerialComm.*;
import java.util.ArrayList;
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
                    System.out.print("Read error. Closing - ");System.out.println(closing);
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
        int writeBytes = thePort.writeBytes(buffer, buffer.length);
        if (writeBytes == -1) {
            System.out.println("Write error");
            if (!closing) {
                callback.connectionLost();
            }
        }
        return writeBytes;
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
        System.out.println("Read thread exit");
        readThread = null;
    }
}
