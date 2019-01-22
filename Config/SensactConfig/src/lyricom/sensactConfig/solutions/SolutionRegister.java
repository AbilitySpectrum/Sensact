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
    static private final Map<SolutionID, String> toolTips = new EnumMap<>(SolutionID.class);
        
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
        
        toolTips.put(SolutionID.JOYSTICK_MOUSE, "<html><b>Joystick Mouse</b><br/>"
                + "<i>Use a joystick to control mouse motion.</i><br/>"
                + "You can use a joystick to control mouse movement.<br/>"
                + "The wizard will determine the joystick orientation. </html>");
        
        toolTips.put(SolutionID.MOUSE_CLICK_BUTTON, "<html><b>Mouse Click Button</b><br/>"
                + "<i>One button generates all mouse clicks.</i><br/>"
                + "<ul>"
                + "<li> Press and release generates a left mouse click.<br/>"
                + "Pressing twice generates a double click.</li>"
                + "<li> Press, wait for one beep and release generates <br/>"
                + "a right mouse click.</li>"
                + "<li> Press, wait for two beeps and release generates<br/>"
                + "a left mouse press and hold.  You can then move the<br/>"
                + "mouse (using a joystick or other buttons) to drag<br/>"
                + "and drop.  To release the hold, press and release."
                + "</li>"
                + "</ul></html>");
        
        toolTips.put(SolutionID.ONE_BTN_MOUSE, "<html><b>One Button Mouse</b><br/>"
                + "<i>Control a computer with a single button</i><br/>"
                + "<ul>"
                + "<li> Press and release generates a left mouse click.<br/>"
                + "Clicking twice generates a double click.</li>"
                + "<li> Press, wait for one beep and then release<br/>"
                + "You will now be in <i>mouse up</i> mode. Pressing the<br/>"
                + "button will move the mouse up.  Release the button until<br/>"
                + "you hear a low beep.  This means the device has reset<br/>"
                + "and you can select another mode.</li>"
                + "<li> Press, wait for two beeps and then release<br/>"
                + "This puts you in <i>mouse down</i> mode.</li>"
                + "<li> Wait for three or four beeps to get into<br/>"
                + "<i>mouse left</i> and <i>mouse right</i> modes.</li>"
                + "</ul>"
                + "Add an on-screen keyboard to support typing and most<br/>"
                + "computer functions can be performed.</html>");
       
        toolTips.put(SolutionID.TOGGLE_MOUSE, "<html><b>Toggle Mouse</b><br/>"
                + "<i>One button controls one axis of mouse motion</i><br/>"
                + "One button controls either the up/down or the <br/>"
                + "left/right motion of the mouse.  You would typically<br/>"
                + "use two buttons - one for each axis of motion.<br/>"
                + "Press the button, the mouse moves in one direction.<br/>"
                + "Release and press again, the mouse moves in the other direction.<br/>"
                + "You can choose to have the change in motion happen immediately<br/>"
                + "or after a short delay.  The delay is useful.  It allows you<br/>"
                + "to nudge the mouse in one direction with repeated quick taps.</html>");
        
        toolTips.put(SolutionID.PRESS_HOLD_SELECT, "<html><b>Press - Hold - Select</b><br/>"
                + "<i>Create a multi-function button</i><br/>"
                + "This wizard guides you in the creation of a multi-function<br/>"
                + "button, where functions are selected by holding the button<br/>"
                + "for different lengths of time.<br/>"
                + "For each function you will define the prompt (often a buzzer<br/>"
                + "sound) and the action.<br/>"
                + "You have the option to <i>latch</i> an action. This will make<br/>"
                + "the button continue to do a particular action until the button<br/>"
                + "is released for a while.  This is useful for mouse motions<br/>"
                + "or TV volume control. When an action is latched you will need<br/>"
                + "define a <i>reset prompt</i> which tells the user when the system has reset.<br/>"
                + "</html>");
        
        toolTips.put(SolutionID.PRESS_RELEASE_WAIT_SELECT, "<html><b>Press - Release - Wait - Select</b><br/>"
                + "<i>Create a multi-function button</i><br/>"
                + "What if you have a client who can touch a button but cannot<br/>"
                + "hold it?  This wizards guides you in the creation of a<br/>"
                + "multi-function button which does not require that the user <br/>"
                + "hold the button.<br/>"
                + "Press and release starts the selection process.  Then, in response<br/>"
                + "to the appropriate prompt another press and release selects the action.<br/>"
                + "For each function you will define the prompt (often a buzzer<br/>"
                + "sound) and the action.<br/>"
                + "You have the option to <i>latch</i> an action. This will make<br/>"
                + "the button continue to do a particular action until the button<br/>"
                + "is released for a while.  This is useful for mouse motions<br/>"
                + "or TV volume control. When an action is latched you will need<br/>"
                + "define a <i>reset prompt</i> which tells the user when the system has reset.<br/>"
                + "</html>");
        
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
    
    public String getToolTip(SolutionID id) {
        return toolTips.get(id);
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
