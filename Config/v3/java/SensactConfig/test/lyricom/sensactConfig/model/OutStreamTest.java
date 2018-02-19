package lyricom.sensactConfig.model;

import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class OutStreamTest {
            
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
     * Test of putChar method, of class OutStream.
     */
    @Test
    public void testPutChar() {
        OutStream os = new OutStream();
        os.putChar((byte)'a');
        os.putChar((byte)'b');
        assertEquals("ab", BufToString(os.getBuffer()));
    }

    /**
     * Test of putNum method, of class OutStream.
     */
    @Test
    public void testPutNum() {
        OutStream os = new OutStream();
        os.putNum( 1, 2 );
        os.putNum( 2, 2 );
        os.putNum( -12345, 2 );
        os.putNum( 0x12345678, 4 );
        os.putNum( 0xffffffff, 4 );
        assertEquals("```a```blolgabcdefghoooooooo", BufToString(os.getBuffer()));
    }

    /**
     * Test of putID method, of class OutStream.
     */
    @Test
    public void testPutID() {
        OutStream os = new OutStream();
        os.putID( 1, 1 );
        os.putID( 2, 1 );
        os.putID( 15, 1 );
        os.putID( 17, 2 );
        assertEquals("ABOAA", BufToString(os.getBuffer()));
   }

    /**
     * Test of putCondition method, of class OutStream.
     */
    @Test
    public void testPutCondition() {
        OutStream os = new OutStream();
        os.putCondition( Trigger.TRIGGER_ON_LOW );
        os.putCondition( Trigger.TRIGGER_ON_HIGH );
        os.putCondition( Trigger.TRIGGER_ON_EQUAL );
        assertEquals("123", BufToString(os.getBuffer()));
    }

    /**
     * Test of putBoolean method, of class OutStream.
     */
    @Test
    public void testPutBoolean() {
        OutStream os = new OutStream();
        os.putBoolean( true );
        os.putBoolean( false );
        assertEquals("pq", BufToString(os.getBuffer()));
    }
    
}
