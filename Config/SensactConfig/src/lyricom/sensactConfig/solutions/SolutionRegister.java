/*
 * This file is part of the Sensact Configuration software.
 *
 * Sensact Configuration software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact Configuration software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom.sensactConfig.solutions;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lyricom.sensactConfig.model.GroupID;
import lyricom.sensactConfig.model.SensorGroup;

/**
 *
 * @author Andrew
 */
public class SolutionRegister {
    static private boolean initDone = false;
        
    public static void init() {
        if (initDone) return;
        SolutionRegister reg = getInstance();
        GroupID[] group1 = {GroupID.SENSOR1, GroupID.SENSOR2, GroupID.SENSOR3};
        GroupID[] group2 = {GroupID.SENSOR1, GroupID.SENSOR2, GroupID.SENSOR3, GroupID.ACCEL};
        reg.register( SolutionID.JOYSTICK_MOUSE, group1);
        reg.register( SolutionID.ONE_BTN_MOUSE, group2);
        reg.register( SolutionID.TOGGLE_MOUSE, group2);
        reg.register( SolutionID.MOUSE_CLICK_BUTTON, group2);
        reg.register( SolutionID.PRESS_HOLD_SELECT, group2);
        reg.register( SolutionID.PRESS_RELEASE_WAIT_SELECT, group2);
    
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
        SolutionID solution;
        GroupID[] applicableGroups;
        
        RegisterEntry(SolutionID s, GroupID[] a) {
            solution = s;
            applicableGroups = a;
        }
    }
    
    private Map<SolutionID, RegisterEntry> entryMap = new EnumMap<>(SolutionID.class);
    
    void register(SolutionID s, GroupID[] app) {
        RegisterEntry reg = new RegisterEntry(s, app);
        entryMap.put(s, reg);
    }
    
    public SolutionID[] getApplicableSolutions(GroupID groupID) {
        List<SolutionID> solutions = new ArrayList<>();
        for(RegisterEntry r: entryMap.values()) {
            for(GroupID gid: r.applicableGroups) {
                if (gid == groupID) {
                    solutions.add(r.solution);
                    break;
                }
            }
        }
        SolutionID[] values = new SolutionID[solutions.size()];
        return solutions.toArray(values);
    }
    
    SolutionBase startSolution(SolutionID solutionID, SolutionsUI sui, SensorGroup sg) {
        RegisterEntry r = entryMap.get(solutionID);
        if (r != null) {
            try {
                Class theClass = r.solution.getImplementation();
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
