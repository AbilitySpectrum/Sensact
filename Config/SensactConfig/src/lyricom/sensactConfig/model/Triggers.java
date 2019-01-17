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
package lyricom.sensactConfig.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Holds the list of triggers.
 * 
 * @author Andrew
 */
public class Triggers {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");

    public static boolean DATA_IN_SYNC;
    
    // singleton pattern
    private static Triggers instance = null;
    
    public static Triggers getInstance() {
        if (instance == null) {
            instance = new Triggers();
        }
        return instance;
    }
    
    private List<Trigger> triggers = new ArrayList<>();
    private List<TriggerCallback> callbacks = new ArrayList<>();
    
    private Triggers() {        
    }
    
    public int length() {
        return triggers.size();
    }
    
    private void sizeChanged() {
        for(TriggerCallback tc: callbacks) {
            tc.newTriggerCount(length());
        }
    }
    
    public void addCallback(TriggerCallback tc) {
        callbacks.add(tc);
        tc.newTriggerCount(length());
    }
    
    public void removeCallback(TriggerCallback tc) {
        callbacks.remove(tc);
    }
    
    public Trigger get(int index) {
        return triggers.get(index);
    }
    
    public List<Trigger> getAll() {
        return triggers;
    }
    
    public void replace(List<Trigger> newList) {
        triggers = newList;
        sizeChanged();
    }
    
    public Trigger newTrigger(Sensor s) {
        DATA_IN_SYNC = false;
        Trigger t = new Trigger(s);
        triggers.add(t);
        sizeChanged();
        return t;
    }
    
    public void deleteAll() {
        DATA_IN_SYNC = false;
        triggers = new ArrayList<>();
        sizeChanged();
    }
    
    public void deleteTriggerSet(Sensor s) {
        DATA_IN_SYNC = false;
        List<Trigger> list = new ArrayList<>();
        for(Trigger t: triggers) {
            if (t.getSensor() != s) {
                list.add(t);
            }
        }
        triggers = list;
        sizeChanged();
    }
    
    public void deleteTrigger(Trigger t) {
        DATA_IN_SYNC = false;
        triggers.remove(t);
        sizeChanged();
    }
    
    // Move the trigger so that it is immediately following the
    // reference trigger.  If the ref trigger is null move the trigger
    // to the front of the list.
    public void placeAfter(Trigger t, Trigger ref) {
        int refIndex = 0;
        int tIndex;
        
        if (t == ref) return;
        
        DATA_IN_SYNC = false;
        if (ref != null) {
            for(refIndex = 0; refIndex < triggers.size(); refIndex++) {  
                if (triggers.get(refIndex) == ref) {
                    break;
                }
            }  
            if (refIndex == triggers.size()) {
                return;  // not found
            }
        }
        for(tIndex = 0; tIndex < triggers.size(); tIndex++) {  
            if (triggers.get(tIndex) == t) {
                break;
            }
        }  
        if (tIndex == triggers.size()) {
            return;  // not found
        }
        
        if (ref == null) {
            triggers.remove(t);
            triggers.add(0, t);
        } else if (refIndex < tIndex) { // move up
            triggers.remove(t);
            triggers.add(refIndex+1, t);
        } else if (refIndex == tIndex) { // Should not happen, but ...
            return; // do nothing
        } else {
            triggers.remove(t); // Will move refIndex up 1.
            triggers.add(refIndex, t);
        }
    }
    
    public void loadTriggers(InStream in) throws IOError {
        List<Trigger> tmpList = new ArrayList<>();
        readTriggers(tmpList, in);
        groupLevels(tmpList);
        replace(tmpList);
        DATA_IN_SYNC = true;
    }
    
    private void readTriggers(List<Trigger> tmp, InStream in) throws IOError {
        if (!Objects.equals(in.getChar(), Model.START_OF_TRIGGERS)) {
            throw new IOError(RES.getString("CDE_INVALID_START"));
        }
        int triggerCount = in.getNum(2);
        for(int i=0; i<triggerCount; i++) {
            Trigger t = new Trigger();
            t.fromStream(in);
            tmp.add(t);
        }
        
        int ch = in.getChar();
        if (ch == Model.MOUSE_SPEED) {
            MouseSpeedTransfer.getInstance().fromStream(in);
            ch = in.getChar();
        }
        if (ch != Model.END_OF_BLOCK) {
            throw new IOError(RES.getString("CDE_INVALID_END"));
        }
    }
    
    public OutStream getTriggerData() {
        OutStream os = new OutStream();
        os.putChar(Model.START_OF_TRIGGERS);
        os.putNum(Triggers.getInstance().length(), 2);
        for(Trigger t: Triggers.getInstance().getAll()) {
            t.toStream(os);
        }
        MouseSpeedTransfer.getInstance().toStream(os);
        os.putChar(Model.END_OF_BLOCK);
        return os;
    }
    
    class Cluster {
        private long sum;
        private int count;
        private int avg;
        private int width;
        
        public void reset(int w) {
            width = w;
            sum = 0;
            avg = count = 0;
        }
        
        public boolean empty() {
            return (count == 0);
        }
        
        public void add(int val) {
            sum += val;
            count++;
            avg = (int) sum/count;
        }
        
        public boolean inRange(int val) {
            return (proximity(val) < width);
        }
        
        public int proximity(int val) {
            return (int) (Math.abs(val - avg));
        }
        
        public int avg() {
            return (int) avg;
        }
    }
    
    // Now the hard bit.  Deduce and set levels.
    // All data within 15% of the average of a group
    // is clusters in that group.
    // Two groups are collected.
    // Data belonging to neither group is put into the nearest group.
    private void groupLevels(List<Trigger> tmp) {
        Cluster group1 = new Cluster();
        Cluster group2 = new Cluster();
        
        // For each sensor ...
        for(Sensor s: Model.sensorList) {
            int clusterWidth = ((s.getMaxval() - s.getMinval()) * 15) / 100;
            group1.reset(clusterWidth);
            group2.reset(clusterWidth);
            
            // .. go through all the triggers and cluster for that sensor.
            for(Trigger t: tmp) {
                if (t.getSensor() == s) {
                    int tval = t.getTriggerValue();
                    if (group1.empty()) {
                        group1.add(tval);
                    } else {
                        // Group 1 already started
                        if (group1.inRange(tval)) {
                            // This one can be added to group 1.
                            group1.add(tval);
                        } else {
                            if (group2.empty()) {
                                group2.add(tval);
                            } else {
                                // Add to the nearest group.
                                if (group1.proximity(tval) < group2.proximity(tval)) {
                                    group1.add(tval);
                                } else {
                                    group2.add(tval);
                                }
                            }
                        }                        
                    }
                }
            }
            if (!group1.empty()) {
                // ... then set sensor levels and adjust trigger values
                // to reflect the results of clustering.
                if (group2.empty()) {
                    s.setLevels(group1.avg(), s.getLevel2());
                } else {
                    s.setLevels(group1.avg(), group2.avg());
                }
                for(Trigger t: tmp) {
                    if (t.getSensor() == s) {
                        if (group2.empty()) {
                            // All are in group 1
                            t.setTriggerValue(group1.avg());
                            t.setLevel(Trigger.Level.LEVEL1);
                        } else {                        
                            int tval = t.getTriggerValue();
                            if (group1.proximity(tval) < group2.proximity(tval)) {
                                t.setTriggerValue(group1.avg());
                                t.setLevel(Trigger.Level.LEVEL1);
                            } else {
                                t.setTriggerValue(group2.avg());
                                t.setLevel(Trigger.Level.LEVEL2);                        
                            }
                        }
                    }
                }
            }
        }
    }
}
