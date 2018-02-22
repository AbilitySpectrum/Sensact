package lyricom.sensactConfig.solutions;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lyricom.sensactConfig.model.SensorGroup;

/**
 *
 * @author Andrew
 */
public class SolutionRegister {
    static boolean initDone = false;
    public static void init() {
        if (initDone) return;
        SolutionRegister reg = getInstance();
        String[] groups1 = {"Input 1", "Input 2", "Input 3"};
        reg.register( "JoystickMouseSolution", "Joystick Mouse", groups1);
        reg.register( "OneBtnMouse", "One Button Mouse", groups1);
        reg.register( "ToggleMouse", "Toggle Mouse", groups1);
        reg.register( "MouseClickButton", "Mouse Click Button", groups1);
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
        String className;
        String solutionName;
        String[] applicableGroups;
        
        RegisterEntry(String c, String s, String[] a) {
            className = c;
            solutionName = s;
            applicableGroups = a;
        }
    }
    
    private Map<String, RegisterEntry> entryMap = new TreeMap<>();
    
    void register(String className, String solutionName, String[] app) {
        RegisterEntry reg = new RegisterEntry(className, solutionName, app);
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
    
    SolutionBase startSolution(String solutionName, SolutionsUI sui, SensorGroup sg) {
        RegisterEntry r = entryMap.get(solutionName);
        if (r != null) {
            try {
                String className = "lyricom.sensactConfig.solutions." + r.className;
                Class theClass = Class.forName(className);
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
