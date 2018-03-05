/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricom.sensactConfig.model;

import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class TriggerTest {
        
    @BeforeClass
    public static void setupClass() throws Exception {
        Model.initModel(410);
    }
    
    public String BufToString(List<Byte> bytes) {
        byte[] buffer;
        buffer = new byte[bytes.size()];
        int i = 0;
        for(Byte b: bytes) {
            buffer[i++] = b;
        }
        return new String(buffer);
    }
    
    /**
     * Test of toStream method, of class Trigger.
     */
    @Test
    public void testToStream() {
        Sensor s = Model.getSensorByID(2); // Input_1B
        Trigger t = new Trigger(s);
        SaAction a = Model.getActionByName(ActionName.BUZZER);
        t.setAction(a);
        t.setActionParam(a.getDefaultVal());
        t.setReqdState(1);
        t.setActionState(2);
        t.setDelay(500);
        t.setRepeat(false);
        t.setCondition(Trigger.TRIGGER_ON_HIGH);
        t.setTriggerValue(700);
        
        OutStream os = new OutStream();
        t.toStream(os);
        
        String result = BufToString(os.getBuffer());
        assertEquals("\nt@BA`bkl2@GB`ai```oj`aodqz", result);
    }

    /**
     * Test of fromStream method, of class Trigger.
     */
    @Test
    public void testFromStream() {
        Trigger t = new Trigger();       
        InStream is = new InStream("\nt@BA`bkl2@GB`abcdefg`aodqz".getBytes());
        try {
            t.fromStream(is);
            
            assertEquals("Input 1B", t.getSensor().getName());
            assertEquals("Buzzer", t.getAction().getName().toString());
            assertEquals(1, t.getReqdState());
            assertEquals(2, t.getActionState());
            assertEquals(0x1234567, t.getActionParam());
            assertEquals(500, t.getDelay());
            assertFalse(t.isRepeat());
            assertEquals(Trigger.TRIGGER_ON_HIGH, t.getCondition());
            assertEquals(700, t.getTriggerValue());
            
        } catch (IOError e) {
            fail("Unexpected IOError: " + e.getMessage());
        }
    }
    
}
