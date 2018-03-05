package lyricom.sensactConfig.solutions;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lyricom.sensactConfig.model.SensorGroup;

/**
 *
 * @author Andrew
 */
public class SolutionRegister {
    static private boolean initDone = false;
    static private final Map<String, String> toolTips = new HashMap<>();
    
    public static void init() {
        if (initDone) return;
        SolutionRegister reg = getInstance();
        String[] groups1 = {"Input 1", "Input 2", "Input 3"};
        reg.register( JoystickMouseSolution.class, "Joystick Mouse", groups1);
        reg.register( OneBtnMouse.class, "One Button Mouse", groups1);
        reg.register( ToggleMouse.class, "Toggle Mouse", groups1);
        reg.register( MouseClickButton.class, "Mouse Click Button", groups1);
        
        toolTips.put("Joystick Mouse", "<html>Use a joystick to control mouse motion.</html>");
        
        toolTips.put("Mouse Click Button", "<html>One button controls mouse clicks.<br/>"
                + "  Press and release == left mouse click<br/>"
                + "  Press, wait for one beep and release == right mouse click<br/>"
                + "  Press, wait for two beeps and release == left press and hold<br/>"
                + "    which allows drag and drop.</html>");
        
        toolTips.put("One Button Mouse", "<html>One click == left mouse click<br/>"
                + "Press hold for one beep and release puts you in mouse-up mode<br/>"
                + "  after two beeps you are in mouse down mode<br/>"
                + "  three and four beeps get you to mouse left and right mode.<br/>"
                + "After a period of inactivity the device resets and you may choose another mode.</html>");
        
        toolTips.put("Toggle Mouse", "<html>One button toggles between left and right mouse motion,<br/>"
                + "or up and down mouse motion.<br/>"
                + "You can choose to have the change in motion happen immediately<br/>"
                + "or after a short delay.</html>");
        initDone = true;
    }

    static private SolutionRegister instance = null;
    
    public static SolutionRegister getInstance() {
        if (instance == null) {
            instance = new SolutionRegister();
        }
        return instance;
    }
    
    private SolutionRegister() {
        
    }
    
    private class RegisterEntry {
        Class clazz;
        String solutionName;
        String[] applicableGroups;
        
        RegisterEntry(Class c, String s, String[] a) {
            clazz = c;
            solutionName = s;
            applicableGroups = a;
        }
    }
    
    private Map<String, RegisterEntry> entryMap = new TreeMap<>();
    
    void register(Class clazz, String solutionName, String[] app) {
        RegisterEntry reg = new RegisterEntry(clazz, solutionName, app);
        entryMap.put(solutionName, reg);
    }
    
    public String[] getApplicableSolutions(String groupName) {
        List<String> names = new ArrayList<>();
        for(RegisterEntry r: entryMap.values()) {
            for(String s: r.applicableGroups) {
                if (s.equals(groupName)) {
                    names.add(r.solutionName);
                    break;
                }
            }
        }
        String[] values = new String[names.size()];
        return names.toArray(values);
    }
    
    public String getToolTip(String name) {
        return toolTips.get(name);
    }
    
    SolutionBase startSolution(String solutionName, SolutionsUI sui, SensorGroup sg) {
        RegisterEntry r = entryMap.get(solutionName);
        if (r != null) {
            try {
                Class theClass = r.clazz;
                Constructor constructor 
                        = theClass.getConstructor(SolutionsUI.class, SensorGroup.class);
                SolutionBase solution = (SolutionBase) constructor.newInstance(sui, sg);
                
                Thread t = new Thread(solution);
                t.start();
                return solution;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
