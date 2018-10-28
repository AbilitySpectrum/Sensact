// -------------------------------------
// Triggers.h
// -------------------------------------
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * 
    This file is part of the Sensact Arduino software.

    Sensact Arduino software is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sensact Arduino software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this Sensact Arduino software.  
    If not, see <https://www.gnu.org/licenses/>.   
 * * * * * * * * * * * * * * * * * * * * * * * * * * * */ 
 
#ifndef Triggers_H
#define Triggers_H

#include "Sensors.h"
#include "Actions.h"
#include "IO.h"

// Condition values
#define TRIGGER_ON_LOW    1
#define TRIGGER_ON_HIGH   2
#define TRIGGER_ON_EQUAL  3

// Memory saving trick #3.  Pack data with values in the 0 - 15 range into one nibble.
// Action state and required state are packed into stateValues.
// condition and repeat are packed into conditions.
#define REQD_STATE(x)  ((x >> 4) & 0xf)
#define ACTION_STATE(x)   (x & 0xf)
#define CONDITION(x)  (x & 0xf)
#define ISREPEAT(x)     (x & 0x10)

// Flags holds the bits ACTION_TAKEN and DISCONNECTED
#define ACTION_TAKEN  1
#define DISCONNECTED  2  // Set if we suspect the sensor is disconnected.
                         // Prevents run-away repeats on disconnected joy-sticks.

// Trigger - holds values for one trigger
class Trigger {
  public:
    // Static elements
    char sensorID;
    char stateValues; // Required state in high bits.  Action state in low bits.
    int  triggerValue;
    char conditions;  // condition & repeat
    char actionID;
    long actionParameters;
    unsigned int  delayMs;
    
    // Dynamic elements
    char flags;
    unsigned int onTime;
  
    Trigger() {
      flags = DISCONNECTED;
      onTime = 0;
    }
    int readTrigger(InputStream *);
    void sendTrigger(OutputStream *);
};

// Triggers - A collection of all triggers
class Triggers {
  private:
    Trigger aTriggers[MAX_TRIGGERS];
    int nTriggers;
    int *paSensorStates;  // One state value per sensor, indexed by sensor ID 
    int maxSensorID; // The highest index into paSensorStates
    ActionData actions;
    
    void setupStates(const SensorData *);
    
  public:
    Triggers() {
      nTriggers = maxSensorID = 0;
    }
    
    void init(int maxSensorID);
    void reset();
    const ActionData* getActions(const SensorData *);
    int readTriggers(InputStream *);
    void sendTriggers(OutputStream *);
};

#endif
