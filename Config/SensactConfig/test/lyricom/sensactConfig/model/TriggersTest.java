package lyricom.sensactConfig.model;

import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class TriggersTest {
    
    @BeforeClass
    public static void setupClass() throws Exception {
        Model.initModel(410);
        Triggers.getInstance().deleteAll();
    }
    
    private static final String testInput =
                "T``ab\n" +
                "t@CA`aoo2@EB```````c````pz\n" +
                "t@CB`aoo2@EB```````c````pz\n" +
                "t@CB`aoo1@@C`````````aodqz\n" +
                "t@CC`aoo2@ED```````d````pz\n" +
                "t@CD`aoo2@ED```````d````pz\n" +
                "t@CD`aoo1@@A`````````aodqz\n" +
                "t@AA`aoo2@EB```````a````pz\n" +
                "t@AB`aoo2@EB```````a````pz\n" +
                "t@AB`aoo1@@C`````````aodqz\n" +
                "t@AC`aoo2@ED```````b````pz\n" +
                "t@AD`aoo2@ED```````b````pz\n" +
                "t@AD`aoo1@@A`````````aodqz\n" +
                "t@BA`aoo2@@B````````````qz\n" +
                "t@BB`aoo2@GC`ai```fd`aodqz\n" +
                "t@BC`aoo2@GD`ai```fd`aodqz\n" +
                "t@BB`aoo1@EA```````e````qz\n" +
                "t@BC`aoo1@EA```````f````qz\n" +
                "t@BD`aoo1@EA```````h````qzZ\n";
            
    @Test
    public void simpleLoadTest() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        // A few checks to see if everything loaded correctly.
        Trigger t = trigs.get(0);
        assertEquals(t.getSensor(), Model.getSensorByID(3));
        assertEquals(t.getReqdState(), 1);
        assertEquals(t.getActionState(), 2);
        assertEquals(t.getDelay(), 0);
        
        t = trigs.get(1);
        assertEquals(t.getSensor(), Model.getSensorByID(3));
        assertEquals(t.getReqdState(), 2);
        assertEquals(t.getActionState(), 2);
        assertEquals(t.getDelay(), 0);
        assertEquals(t.getAction(), Model.getActionByType(ActionType.HID_MOUSE));
        assertEquals(t.getActionParam(), Model.MOUSE_LEFT);
        
        t = trigs.get(2);
        assertEquals(t.getSensor(), Model.getSensorByID(3));
        assertEquals(t.getReqdState(), 2);
        assertEquals(t.getActionState(), 3);
        assertEquals(t.getDelay(), 500);
        
        assertEquals(trigs.get(6).getSensor(), Model.getSensorByID(1));
    }
    
    @Test
    public void deleteTest() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t = trigs.get(1);
        trigs.deleteTrigger(t);
        t = trigs.get(1);
        // Trigger #1 should look like # 2 used to.
        assertEquals(t.getSensor(), Model.getSensorByID(3));
        assertEquals(t.getReqdState(), 2);
        assertEquals(t.getActionState(), 3);
        assertEquals(t.getDelay(), 500);
        
    }
        
    @Test
    public void deleteGroupTest() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        assertEquals(18, trigs.length());
        Sensor s = Model.getSensorByID(3);
        trigs.deleteTriggerSet(s);
        assertEquals(12, trigs.length());
        
        // The Sensor-1 block should be at the start now.
        Trigger t = trigs.get(0);
        assertEquals(t.getSensor(), Model.getSensorByID(1));
        assertEquals(t.getReqdState(), 1);
        assertEquals(t.getActionState(), 2);
        assertEquals(t.getDelay(), 0);
        
        t = trigs.get(1);
        assertEquals(t.getSensor(), Model.getSensorByID(1));
        assertEquals(t.getReqdState(), 2);
        assertEquals(t.getActionState(), 2);
        assertEquals(t.getDelay(), 0);
        assertEquals(t.getAction(), Model.getActionByType(ActionType.HID_MOUSE));
        assertEquals(t.getActionParam(), Model.MOUSE_UP);
        
        t = trigs.get(2);
        assertEquals(t.getSensor(), Model.getSensorByID(1));
        assertEquals(t.getReqdState(), 2);
        assertEquals(t.getActionState(), 3);
        assertEquals(t.getDelay(), 500);        
    }
    
    @Test
    public void insertBeforeFirst() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);

        Trigger tt = new Trigger();
        tt.copyValue(t3);
         
        // Move to the front
        trigs.insertTrigger(tt, t0, false);
        
         // Check that triggers have moved as expected.
        assertEquals(tt, trigs.get(0));
        assertEquals(t0, trigs.get(1));
        assertEquals(t1, trigs.get(2));
        assertEquals(t2, trigs.get(3));
        
        assertEquals(19, trigs.length());
    }

    @Test
    public void insertAfterFirst() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);
        
        Trigger tt = new Trigger();
        tt.copyValue(t3);
         
        // Move to the front
        trigs.insertTrigger(tt, t0, true);
        
         // Check that triggers have moved as expected.
        assertEquals(t0, trigs.get(0));
        assertEquals(tt, trigs.get(1));
        assertEquals(t1, trigs.get(2));
        assertEquals(t2, trigs.get(3));
        
        assertEquals(19, trigs.length());
    }
 
    @Test
    public void cutAndMoveDown() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);
         
        // Move down
        trigs.deleteTrigger(t0);
        trigs.insertTrigger(t0, t3, true);
        
         // Check that triggers have moved as expected.
        assertEquals(t1, trigs.get(0));
        assertEquals(t2, trigs.get(1));
        assertEquals(t3, trigs.get(2));
        assertEquals(t0, trigs.get(3));
        
        assertEquals(18, trigs.length());
    }
 
    @Test
    public void insertAfterEnd() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);
        Trigger t17 = trigs.get(17);
         
        Trigger tt = new Trigger();
        tt.copyValue(t3);
         
       // Move to the end
        trigs.insertTrigger(tt, t17, true);
        
         // Check that triggers have moved as expected.
        assertEquals(t0, trigs.get(0));
        assertEquals(t1, trigs.get(1));
        assertEquals(t2, trigs.get(2));
        assertEquals(t17, trigs.get(17));
        assertEquals(tt, trigs.get(18));
        
        assertEquals(19, trigs.length());
    }

     @Test
    public void insertBeforeEnd() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);
        Trigger t17 = trigs.get(17);
         
        Trigger tt = new Trigger();
        tt.copyValue(t3);
         
       // Move to the end
        trigs.insertTrigger(tt, t17, false);
        
         // Check that triggers have moved as expected.
        assertEquals(t0, trigs.get(0));
        assertEquals(t1, trigs.get(1));
        assertEquals(t2, trigs.get(2));
        assertEquals(tt, trigs.get(17));
        assertEquals(t17, trigs.get(18));
        
        assertEquals(19, trigs.length());
    }
    
    @Test
    public void replaceBeforeEnd() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);
        Trigger t17 = trigs.get(17);
         
        trigs.deleteTrigger(t2);
         
       // Move to the end
        trigs.insertTrigger(t2, t17, false);
        
         // Check that triggers have moved as expected.
        assertEquals(t0, trigs.get(0));
        assertEquals(t1, trigs.get(1));
        assertEquals(t3, trigs.get(2));
        assertEquals(t2, trigs.get(16));
        assertEquals(t17, trigs.get(17));
        
        assertEquals(18, trigs.length());
    }
    
    @Test
    public void moveFirstToEnd() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);
        Trigger t17 = trigs.get(17);
         
        trigs.deleteTrigger(t0);
         
       // Move to the end
        trigs.insertTrigger(t0, t17, true);
        
         // Check that triggers have moved as expected.
        assertEquals(t1, trigs.get(0));
        assertEquals(t2, trigs.get(1));
        assertEquals(t3, trigs.get(2));
        assertEquals(t17, trigs.get(16));
        assertEquals(t0, trigs.get(17));
        
        assertEquals(18, trigs.length());
    }    
    
    @Test
    public void moveLastToStart() {
        InStream in = new InStream(testInput.getBytes());
        Triggers trigs = Triggers.getInstance();
        trigs.deleteAll();
        try {
            trigs.loadTriggers(in);
        } catch (IOError e) {
            fail("Unexpected IOError");
            return;
        }
        
        Trigger t0 = trigs.get(0);
        Trigger t1 = trigs.get(1);
        Trigger t2 = trigs.get(2);
        Trigger t3 = trigs.get(3);
        Trigger t16 = trigs.get(16);
        Trigger t17 = trigs.get(17);
         
        trigs.deleteTrigger(t17);
         
       // Move to the end
        trigs.insertTrigger(t17, t0, false);
        
         // Check that triggers have moved as expected.
        assertEquals(t17, trigs.get(0));
        assertEquals(t0, trigs.get(1));
        assertEquals(t1, trigs.get(2));
        assertEquals(t2, trigs.get(3));
        assertEquals(t16, trigs.get(17));
        
        assertEquals(18, trigs.length());
    }
}
