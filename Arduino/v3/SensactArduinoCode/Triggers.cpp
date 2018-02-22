// -------------------------------------
// Triggers.cpp
// -------------------------------------

#include <Arduino.h>
#include "Triggers.h"

EEPROMInputStream EEIn;
EEPROMOutputStream EEOut;

void Triggers::init() {
  // See if there are triggers stored in EEPROM and read them.
  EEIn.init();
  char T = EEIn.getChar();
  if (T == START_OF_TRIGGER_BLOCK) { // There are triggers in EEProm
    readTriggers(&EEIn);
  }  
}

void Triggers::reset() {
  for(int i=0; i<=maxSensorID; i++) {
    paSensorStates[i] = 1;
  }  
  for(int j=0; j<nTriggers; j++) {
    Trigger *pTrigger = &aTriggers[j];
    pTrigger->flags = DISCONNECTED; //Assume disconnected until we get a signal
  }
}

// The first time the Triggers object gets sensor data 
// this code is run to set up the sensor state array.
void Triggers::setupStates(const SensorData *pData) {
  maxSensorID = 0;
  
  // Find the highest sensor ID
  int dataCount = pData->length();

  for(int i=0; i<dataCount; i++) {
    int ID = pData->getValue(i)->sensorID;
    if (ID > maxSensorID) {
      maxSensorID = ID;
    }
  }
    
  // Initialize sensor states.
  paSensorStates = new int[maxSensorID+1];
  reset();
}

const ActionData* Triggers::getActions(const SensorData *pData) {
      
  if (maxSensorID == 0) { // First time we setup sensor states
    setupStates(pData);
  }
  
  // Empty the actions container, preparing for new actions.
  actions.reset();    
  
  // Check each sensor against each trigger, looking for matches 
  // and associated actions.
  int nSensors = pData->length();
  for(int i=0; i<nSensors; i++) {
    const SensorDatum *pDatum = pData->getValue(i);
    int ID = pDatum->sensorID;
    int value = pDatum->sensorValue;
    for(int j=0; j<nTriggers; j++) {
      Trigger *pTrigger = &aTriggers[j];
      boolean matchCondition = true;  // Assume a match until we prove otherwise

      // Check sensor ID
      if (ID != (int) pTrigger->sensorID) {
        continue; // Wrong sensor ID - forget it.
      }

      // Check for disconnected sensor
      if (pTrigger->flags & DISCONNECTED) {
        if (value < 10 && value > -10) {
          // Still disconnected.  No match
          matchCondition = false;
        } else {
          // Larger value.  Sensor must have been re-connected.
          pTrigger->flags &= ~DISCONNECTED;
        }
      }

      // Check states
      if ((int) REQD_STATE(pTrigger->stateValues) == 0)  {  
        // Match ANY condition - match only if not already in target state.
        if (paSensorStates[ID] == (int) ACTION_STATE(pTrigger->stateValues)) { 
          matchCondition = false;
        }
      } else if (paSensorStates[ID] != (int) REQD_STATE(pTrigger->stateValues)) { 
        // Normal case - match only if states match.
        matchCondition = false; 
      }
      // Note: Even if matchCondition is false at this point
      // still need to fall through, to turn off any matched conditions.
      
      if (matchCondition) {
        // State matches - now check to see if value is correct.
        switch(CONDITION(pTrigger->conditions)) {
          case TRIGGER_ON_LOW:
            if (pTrigger->triggerValue < value) matchCondition = false;
            break;
          case TRIGGER_ON_HIGH:
            if (pTrigger->triggerValue > value) matchCondition = false;
            break;
          case TRIGGER_ON_EQUAL:
            if (pTrigger->triggerValue != value) matchCondition = false;
            break;
        }
      }
      
      if (matchCondition) {       
        // We have a trigger match!
        unsigned int now = millis() & 0xffff;  // See Memory Saving Trick #1
        if (pTrigger->onTime == 0) {  // Not triggered previously
          pTrigger->onTime = now; // Record time of initial trigger match
          
          if (pTrigger->delayMs == 0) { // No delay? Do action immediately.
            if (pTrigger->actionID == CHANGE_SENSOR_STATE) {
              int state = pTrigger->actionParameters & 0xff;
              int sensorID = (pTrigger->actionParameters >> 8) & 0xff;
              paSensorStates[sensorID] = state;
            } else {
              actions.addAction(pTrigger->actionID, pTrigger->actionParameters, false);
            }
            paSensorStates[ID] = ACTION_STATE(pTrigger->stateValues);  
            pTrigger->flags |= ACTION_TAKEN;
          }
          
        } else if (!(pTrigger->flags & ACTION_TAKEN)) { // Waiting for delay
          if (timeDiff(now, pTrigger->onTime) > pTrigger->delayMs) {
            if (pTrigger->actionID == CHANGE_SENSOR_STATE) {
              int state = pTrigger->actionParameters & 0xff;
              int sensorID = (pTrigger->actionParameters >> 8) & 0xff;
              paSensorStates[sensorID] = state;
            } else {
              actions.addAction(pTrigger->actionID, pTrigger->actionParameters, false);
            }
            paSensorStates[ID] = ACTION_STATE(pTrigger->stateValues);  
            pTrigger->flags |= ACTION_TAKEN;
         }
          
        } else if (ISREPEAT(pTrigger->conditions)) {
          if ( (value < 10 && value > -10) && (timeDiff(now, pTrigger->onTime) > 15000))  {
            // 15 seconds repeating on a near-0 signal probably means the sensor is disconnected.
            // So stop doing the repeats.
            // Note: A cable connected to a sensor jack with nothing on the other end
            // can result in a small signal - thus 'val < 10' rather than just 'val == 0'.
            pTrigger->flags |= DISCONNECTED; 
          } else {
            // Requests for repeated actions are sent to action processing.
            // There the decision of whether actually to do the action or not 
            // is made (in assessAction) - based on the time since the last action.
            actions.addAction(pTrigger->actionID, pTrigger->actionParameters, true);
          }
        }          
        
      } else { 
          pTrigger->onTime = 0;
          pTrigger->flags &= ~ACTION_TAKEN;
      }
    }
  }
  
  return &actions;
}

int Triggers::readTriggers(InputStream *is) {
  long tCount = is->getNum();
  if (tCount == IO_NUMERROR) return IO_ERROR;
  if (tCount > MAX_TRIGGERS) return IO_ERROR;
  
  for(int i=0; i<tCount; i++) {
    if (aTriggers[i].readTrigger(is) == IO_ERROR) {
      return IO_ERROR;
    }
  }
  nTriggers = tCount;
  
  int Z = is->getChar();
  if (Z == MOUSE_SPEED) {
    if (readMouseSpeed(is) == IO_ERROR) {
      return IO_ERROR;
    }
    Z = is->getChar();
  }
  if (Z != END_OF_BLOCK) { 
    return IO_ERROR;
  }
  
  // If triggers are read successfully - save them.
  EEOut.init();
  sendTriggers(&EEOut);
  
  return 0;
}

void Triggers::sendTriggers(OutputStream *os) {
  os->putChar(START_OF_TRIGGER_BLOCK);
  os->putNum(nTriggers);
  for(int i=0; i<nTriggers; i++) {
    aTriggers[i].sendTrigger(os);
  }
  os->putChar(MOUSE_SPEED);
  sendMouseSpeed(os);
  os->putChar(END_OF_BLOCK);  // End of transmission block
}

int Trigger::readTrigger(InputStream *is) {
  int actionState;
  int reqdState;
  int tmpint;
  long tmplong;
  
  if (is->getChar() != TRIGGER_START) return IO_ERROR;
  if ((tmpint       = is->getID())        == IO_ERROR) return IO_ERROR;
  sensorID = tmpint;
  if ((reqdState  = is->getState())     == IO_ERROR) return IO_ERROR;
  if ((tmplong = is->getNum())    == IO_NUMERROR) return IO_ERROR;
  triggerValue = tmplong;
  if ((tmpint  = is->getCondition()) == IO_ERROR) return IO_ERROR;
  conditions = tmpint;
  if ((tmpint  = is->getID())        == IO_ERROR) return IO_ERROR;
  actionID = tmpint;
  if ((actionState = is->getState())      == IO_ERROR) return IO_ERROR;
  stateValues = (reqdState << 4) | actionState; // Pack both states into one char.
  if ((tmplong = is->getLong())   == IO_NUMERROR) return IO_ERROR;
  actionParameters = tmplong;
  if ((tmplong = is->getNum())    == IO_NUMERROR) return IO_ERROR;
  delayMs = tmplong;
  if ((tmpint  = is->getBool())      == IO_ERROR) return IO_ERROR;
  if (tmpint) {
    conditions += 0x10;  // Repeat
  }
  if (is->getChar() != TRIGGER_END) return IO_ERROR;
  return 0;
}

void Trigger::sendTrigger(OutputStream *os) {
  os->putChar(TRIGGER_START);
  os->putID(sensorID);
  os->putState( REQD_STATE(stateValues) ); 
  os->putNum(triggerValue);
  os->putCondition(CONDITION(conditions));
  os->putID(actionID);
  os->putState( ACTION_STATE(stateValues) );  
  os->putLong(actionParameters);
  os->putNum(delayMs);
  os->putBool(ISREPEAT(conditions));
  os->putChar(TRIGGER_END);
}



