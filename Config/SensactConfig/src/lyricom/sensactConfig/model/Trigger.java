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

/**
 *
 * @author Andrew
 */
public class Trigger {
    public static enum Level {
        LEVEL1, LEVEL2
    }
    
    public static final int TRIGGER_ON_LOW   = 1;
    public static final int TRIGGER_ON_HIGH  = 2;
    public static final int TRIGGER_ON_EQUAL = 3;
    
    public static final byte TRIGGER_START = (byte) 't';
    public static final byte TRIGGER_END = (byte) 'z';
    
    public static final int DEFAULT_STATE = 1;
    
    private int reqdState;
    private Sensor sensor;
    private int triggerValue;
    private int condition;
    private int delay;
    private boolean repeat = false;
    private SaAction action;
    private int actionParam;
    private int actionState;
    
    private Level level;
    
    // Package visibility only.  UI must create triggers via 
    // Triggers.newTrigger.
    Trigger() {
        sensor = null;
        triggerValue = 0;
        condition = TRIGGER_ON_HIGH;
        initValues();
    }
    
    public Trigger(Sensor s) {
        initValues();
        setSensor(s);
    }
    
    public void copyValue(Trigger other) {
        // Safety check?
        if (sensor != other.sensor) return;
        
        reqdState = other.reqdState;
        triggerValue = other.triggerValue;
        condition = other.condition;
        delay = other.delay;
        repeat = other.repeat;
        action = other.action;
        actionParam = other.actionParam;
        actionState = other.actionState;
        level = other.level;
    }
    
    public final void initValues() {
        reqdState = DEFAULT_STATE;
        actionState = DEFAULT_STATE;
        delay = 0;
        repeat = false;
        action = Model.getActionByID(0, 0);
        actionParam = 0;
        level = Level.LEVEL1;
    }
    
    public final void setSensor(Sensor s) {
        sensor = s;
        if (getSensor().isContinuous()) {
            if (level == Level.LEVEL1) {
                setTriggerValue( sensor.getLevel1() );
            } else {
                setTriggerValue( sensor.getLevel2() );
            }
            setCondition(TRIGGER_ON_HIGH);
        } else {
            setTriggerValue('a');
            setCondition(TRIGGER_ON_EQUAL);
        }
    }
    
    public void toStream(OutStream os) {
        os.putChar((byte)'\n');
        os.putChar(TRIGGER_START);
        os.putID(sensor.getId(), 2);
        os.putID(reqdState, 1);
        os.putNum(triggerValue, 2);
        os.putCondition(condition);
        os.putID(action.getId(), 2);
        os.putID(actionState, 1);
        os.putNum(actionParam, 4);
        os.putNum(delay, 2);
        os.putBoolean(repeat);
        os.putChar(TRIGGER_END);
    }
    
    public void fromStream(InStream is) throws IOError {
        if (is.getChar() != TRIGGER_START) {
            throw new IOError("Invalid start of trigger");
        }
        int sensorID = is.getID(2);
        Sensor tmp = Model.getSensorByID(sensorID);
        if (tmp == null) {
            throw new IOError("Invalid sensor ID");
        }
        setSensor(tmp);
        reqdState = is.getID(1);
        triggerValue = is.getNum(2);
        condition = is.getCondition();
        int actionID = is.getID(2);
        actionState = is.getID(1);
        actionParam = is.getNum(4);
        action = Model.getActionByID(actionID, actionParam);
        if (action == null) {
            throw new IOError("Invalid action ID");
        }
        delay = is.getNum(2);
        repeat = is.getBoolean();
        if (is.getChar() != TRIGGER_END) {
            throw new IOError("Invalid end of trigger");
        }
    }

     public int getReqdState() {
        return reqdState;
    }

     public void setReqdState(int reqdState) {
        this.reqdState = reqdState;
        Triggers.DATA_IN_SYNC = false;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public int getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(int triggerValue) {
        this.triggerValue = triggerValue;
        Triggers.DATA_IN_SYNC = false;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        if (sensor.isContinuous()) {
            this.condition = condition;
            Triggers.DATA_IN_SYNC = false;
        } else if (condition == TRIGGER_ON_EQUAL) {
            this.condition = condition;
            Triggers.DATA_IN_SYNC = false;
        }
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        Triggers.DATA_IN_SYNC = false;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        Triggers.DATA_IN_SYNC = false;
    }

    public SaAction getAction() {
        return action;
    }

    public void setAction(SaAction action) {
        this.action = action;
        Triggers.DATA_IN_SYNC = false;
    }

    public int getActionParam() {
        return actionParam;
    }

    public void setActionParam(int actionParam) {
        this.actionParam = actionParam;
        Triggers.DATA_IN_SYNC = false;
    }

    public int getActionState() {
        return actionState;
    }

    public void setActionState(int actionState) {
        this.actionState = actionState;
         Triggers.DATA_IN_SYNC = false;
   }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
         Triggers.DATA_IN_SYNC = false;
   }
}
