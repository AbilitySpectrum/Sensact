// -------------------------------------
// Triggers.h
// -------------------------------------

#ifndef Triggers_H
#define Triggers_H

#include "Sensors.h"
#include "Actions.h"
#include "IO.h"

// Condition values
#define TRIGGER_ON_LOW    1
#define TRIGGER_ON_HIGH   2
#define TRIGGER_ON_EQUAL  3

// Trigger - holds values for one trigger
class Trigger {
  public:
    // Static elements
    char sensorID;
    char reqdState;
    int  triggerValue;
    char condition;
    char actionID;
    char actionState;
    long actionParameters;
    int  delayMs;
    char repeat;
    
    // Dynamic elements
    boolean actionTaken;
    long onTime;
    long lastActionTime;
  
    Trigger() {
      actionTaken = false;
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
    
    void init();
    const ActionData* getActions(const SensorData *);
    int readTriggers(InputStream *);
    void sendTriggers(OutputStream *);
};

#endif
