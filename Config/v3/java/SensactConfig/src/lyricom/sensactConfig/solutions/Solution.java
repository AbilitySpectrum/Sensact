package lyricom.sensactConfig.solutions;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import lyricom.sensactConfig.model.SensorGroup;

/**
 * Base class for all solutions
 * @author Andrew
 */
public abstract class Solution implements Runnable {

    private final static List<SolutionAbility> ALL_SOLUTIONS;
    
    public static List<String> getApplicableSolutions(SensorGroup sg) {
        List<String> applicableSolutions = new ArrayList<>();
        for(SolutionAbility sa: ALL_SOLUTIONS) {
            for(String app: sa.applicableSensorGroups) {
                if (app.equals(sg.getName())) {
                    applicableSolutions.add(sa.solutionName);
                    break;
                }
            }               
        }
        return applicableSolutions;
    }
    
    private static class SolutionAbility {
        String solutionName;
        String[] applicableSensorGroups;
        
        SolutionAbility(String n, String[] list) {
            solutionName = n;
            applicableSensorGroups = list;
        }
    }
    
    public static Solution getSolutionByName(String name) {
        return null;
    }
    
    static {
        // Populate allSolutions.
        ALL_SOLUTIONS = new ArrayList<>();
        String[] obmList = {"Input 1", "Input 2", "Input 3"};
        ALL_SOLUTIONS.add( new SolutionAbility(
                "One Button Mouse", obmList ) );
    }
    
    String name;
    List<SensorGroup> applicableGroups = new ArrayList<>();
    
    String name() { 
        return name; 
    }
    
    List<SensorGroup> appliesTo() { 
        return applicableGroups; 
    }
}
