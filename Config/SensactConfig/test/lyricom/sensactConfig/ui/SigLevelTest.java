/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricom.sensactConfig.ui;

import org.junit.*;
import static org.junit.Assert.*;
/**
 *
 * @author Andrew
 */
public class SigLevelTest  {
    
    @Test
    public void test1() {
        SigLevelBuffer buf = new SigLevelBuffer(5);
        
        buf.addValue(0);
        buf.addValue(2);
        buf.addValue(4);
        buf.addValue(3);
        buf.addValue(1);
        // Buf = 0 2 4 3 1
        assertEquals(0, buf.getMinVal());
        assertEquals(4, buf.getMaxVal());
        // Buf == 2 4 3 1 5
        buf.addValue(5);
        assertEquals(1, buf.getMinVal());
        assertEquals(5, buf.getMaxVal());
        
        buf.addValue(3);
        buf.addValue(3);
        buf.addValue(2);
        buf.addValue(3);
        // Buf == 5 3 3 2 3
        assertEquals(2, buf.getMinVal());
        assertEquals(5, buf.getMaxVal());
        buf.addValue(1);
        // Buff == 3 3 2 3 1
        assertEquals(1, buf.getMinVal());
        assertEquals(3, buf.getMaxVal());
   }
}
