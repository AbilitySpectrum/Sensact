package lyricom.sensactConfig.solutions;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SaAction;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.model.Triggers;
import lyricom.sensactConfig.ui.SensorPanel;

/**
 * Base class for all solutions
 * @author Andrew
 */
public abstract class SolutionBase implements Runnable {
    
    protected volatile boolean cancelling = false;
    protected SolutionsUI theUI;
    protected SensorGroup theGroup;
    private Thread solutionThread;
    private Calibrator calibrator;
    
    public SolutionBase(SolutionsUI ui, SensorGroup sg) {
        theUI = ui;
        theGroup = sg;
        calibrator = null;
    }
    
    // called from SolutionsUI on the Swing thread.
    void cancel() {
        cancelling = true;
        if (calibrator != null) {
            calibrator.cancel();
        }
        solutionThread.interrupt();
    }
    
    protected Calibrator getCalibrator() {
        calibrator = new Calibrator(theUI, theGroup);
        return calibrator;
    }
    
    @Override
    public void run() {
        solutionThread = Thread.currentThread();
        if (doSolution() == true) {
            // Success
            SensorPanel.reloadTriggers();
            theUI.solutionComplete();
        }
        if (!cancelling) {
            theUI.solutionComplete();
        }
        theUI = null;
    }
    
    // To be provided by sub-classes.
    // Returns true when solution is complete and new triggers have been made.
    // false under all error and cancel conditions.
    abstract boolean doSolution();
    
    // -------------------------------------------------------
    // Utility routines for common solution actions.
    //
    
    // For single-button targetted solutions, this gets the button.
    protected Location getButton() {
        Calibrator c = getCalibrator();
        c.startCalibration();
        c.getRestValues();
        
        Location btnLocHi = c.getLocation("Please press and hold the button.");
        c.endCalibration();
        if (cancelling) return null;
        
        if (btnLocHi == null) {
            JOptionPane.showMessageDialog(theUI,
                    "Sorry. Button press was not detected.",
                    "Solution Failure",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        } else {
            return btnLocHi;
        }       
    }
    
    private static final String HID_MOUSE = "HID Mouse";
    private static final String BT_MOUSE = "Bluetooth Mouse";
    private static final String[] MOUSE_OPTS = {HID_MOUSE, BT_MOUSE};
    
    // Get the type of mouse
    protected SaAction mouseSelection() {
        if (cancelling) return null;
        String option = theUI.getOption("What kind of mouse?", MOUSE_OPTS);
        if (option == null || cancelling) return null;
        if (option.equals(HID_MOUSE)) {
            return Model.getActionByName("HID Mouse");
        } else {
            return Model.getActionByName("BT Mouse");
        }
    }

    void makeTrigger(int startState, Location loc, int delay, 
            SaAction action, int actionParam, int finalState) {
                
        loc.sensor.setLevel( loc.level, loc.value );
        Trigger t = Triggers.getInstance().newTrigger(loc.sensor);
        t.setLevel(loc.level);
        t.setReqdState(startState);
        t.setTriggerValue( loc.value );
        t.setCondition( loc.condition );
        t.setDelay(delay);
        t.setAction(action);
        t.setActionParam(actionParam);
        t.setActionState(finalState);
    }
}
