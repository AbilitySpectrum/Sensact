package lyricom.sensactConfig.model;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * @author Andrew
 */
public class InStreamTest {
    
    /**
     * Test of getNum method, of class InStream.
     */
    @Test
    public void testGetNum() throws Exception {
        InStream is = new InStream( "```a```blolgabcdefghooooooooA".getBytes() );
        assertTrue(is.getNum(2) == 1);
        assertTrue(is.getNum(2) == 2);
        assertTrue(is.getNum(2) == -12345);  // Test 2-byte negatives.
        assertTrue(is.getNum(4) == 0x12345678); // Test 4-byte values
        assertTrue(is.getNum(4) == 0xffffffff);
        try {   // Check for invalid number.
            is.getNum(2);
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals(ex.getMessage(), "Invalid Number");
       }
        try {   // Check for end of stream.
            is.getNum(2);
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals(ex.getMessage(), "End of stream");
       }
    }

    /**
     * Test of getID method, of class InStream.
     */
    @Test
    public void testGetID() throws Exception {
        InStream is = new InStream( "AOAAa".getBytes() );
        assertTrue(is.getID(1) == 1);
        assertTrue(is.getID(1) == 15);
        assertTrue(is.getID(2) == 0x11);
        try {   // Check for end of stream.
            is.getID(1);
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals(ex.getMessage(), "Invalid ID");
       }
        try {   // Check for end of stream.
            is.getID(1);
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals(ex.getMessage(), "End of stream");
       }
    }

    /**
     * Test of getCondition method, of class InStream.
     */
    @Test
    public void testGetCondition() throws Exception {
        InStream is = new InStream( "123a".getBytes() );
        assertTrue(is.getCondition() == Trigger.TRIGGER_ON_LOW);
        assertTrue(is.getCondition() == Trigger.TRIGGER_ON_HIGH);
        assertTrue(is.getCondition() == Trigger.TRIGGER_ON_EQUAL);
        try {   // Check for end of stream.
            is.getCondition();
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals("Invalid Condition", ex.getMessage());
       }
        try {   // Check for end of stream.
            is.getCondition();
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals(ex.getMessage(), "End of stream");
       }
    }

    /**
     * Test of getBoolean method, of class InStream.
     */
    @Test
    public void testGetBoolean() throws Exception {
        InStream is = new InStream( "pqa".getBytes() );
        assertTrue(is.getBoolean());
        assertFalse(is.getBoolean());
        try {   // Check for end of stream.
            is.getBoolean();
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals("Invalid Boolean", ex.getMessage());
       }
        try {   // Check for end of stream.
            is.getBoolean();
            fail("IOError expected");
        } catch (IOError ex) {
            assertEquals(ex.getMessage(), "End of stream");
       }
    }
    
}
