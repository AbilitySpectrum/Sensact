/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricom.sensactConfig.comms;

import java.util.Arrays;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class BufferTest {
 
    void printBuf(byte[] buffer) {
        for(byte b: buffer) {
            System.out.print((char)b);
        }
        System.out.println();
    }
    
    @Test
    public void byteArrayTest() {
        byte[] buffer = {'a','b','c','d','e','f','g','h','i'};
        
        printBuf(buffer);
        
        byte[] part = Arrays.copyOfRange(buffer, 0, 4);
        buffer = Arrays.copyOfRange(buffer, 4, buffer.length);
        printBuf(part);
        printBuf(buffer);
        
    }
}
