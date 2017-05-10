// -------------------------------------
// Triggers.cpp
// -------------------------------------

#include <Arduino.h>
#include "Triggers.h"

EEPROMInputStream EEIn;
EEPROMOutputStream EEOut;

/*
 *  Memory Saving Trick #1
 *  Save times in unsigned int values.  This will hold times up to 65 seconds.
 *  Use timeDiff to calculate the time interval between 'now' and a previous time 'prev'.
 *  'now' should always be greater than 'prev'.  If it isn't it is because our counter
 *  rolled over - and the difference calculation is adjusted to take this into account.
 *  
 *  Since we never care about time intervals bigger than a couple of seconds this works just fine.
 *  Using unsigned int rather than a long saves 4 bytes per trigger structure.
 *  With MAX_TRIGGERS set to 20 this is a savings of 80 bytes.
 */
unsigned int timeDiff(unsigned int now, unsigned int prev) {
  if (now > prev) return (now - prev);
  // else
  return (0xffff - prev + now + 1);  // Counter rolled over.
}

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
  
  // Create a temporary place to hold state changes.
  // This keeps state changes from having a changed effect 
  // in the middle of processing the sensor data.
  int aTmpStates[maxSensorID+1];
  for(int i=0; i<=maxSensorID; i++) {
    aTmpStates[i] = paSensorStates[i];
  }  
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

      // Check states
      if ((int)pTrigger->reqdState == 0)  {
        // Match ANY condition - match only if not already in target state.
        if (paSensorStates[ID] == (int) pTrigger->actionState) {
          matchCondition = false;
        }
      } else if (paSensorStates[ID] != (int) pTrigger->reqdState) {
        // Normal case - match only if states match.
        matchCondition = false; 
      }
      // Note: Even if matchCondition is false at this point
      // still need to fall through, to turn off any matched conditions.
      
      if (matchCondition) {
        // State matches - now check to see if value is correct.
        switch(pTrigger->condition) {
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
          pTrigger->repeatInterval = REPEAT_INTERVAL;  // Repeat at slow default.
          pTrigger->repeatCount = 0;
          
          if (pTrigger->delayMs == 0) { // No delay? Do action immediately.
            aTmpStates[ID] = pTrigger->actionState;
            actions.addAction(pTrigger->actionID, pTrigger->actionParameters);
            pTrigger->actionTaken = true;
            pTrigger->lastActionTime = now;
          }
          
        } else if (pTrigger->actionTaken == false) { // Waiting for delay
          if (timeDiff(now, pTrigger->onTime) > pTrigger->delayMs) {
            aTmpStates[ID] = pTrigger->actionState;
            actions.addAction(pTrigger->actionID, pTrigger->actionParameters);
            pTrigger->actionTaken = true;
            pTrigger->lastActionTime = now;
          }
          
        } else if (pTrigger->repeat) {
          if ( timeDiff(now, pTrigger->lastActionTime) > pTrigger->repeatInterval) {
            actions.addAction(pTrigger->actionID, pTrigger->actionParameters);
            pTrigger->lastActionTime = now;
            pTrigger->repeatCount++;
            if (pTrigger->repeatCount > 40) {
              pTrigger->repeatInterval = REPEAT_INTERVAL / 4;
            } else if (pTrigger->repeatCount > 10) { 
              pTrigger->repeatInterval = REPEAT_INTERVAL / 2; 
            } 
            
          } 
        }          
        
      } else { 
          pTrigger->onTime = 0;
          pTrigger->actionTaken = false;
          pTrigger->lastActionTime = 0;          
      }
    }
  }
  
  // Copy the temporary sensor values to the permanent ones.
  for(int i=0; i<=maxSensorID; i++) {
    paSensorStates[i] = aTmpStates[i];
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
  os->putChar(END_OF_BLOCK);  // End of transmission block
}

int Trigger::readTrigger(InputStream *is) {
  int tmpint;
  long tmplong;
  
  if (is->getChar() != TRIGGER_START) return IO_ERROR;
  if ((tmpint       = is->getID())        == IO_ERROR) return IO_ERROR;
  sensorID = tmpint;
  if ((tmpint  = is->getState())     == IO_ERROR) return IO_ERROR;
  reqdState = tmpint;
  if ((tmplong = is->getNum())    == IO_NUMERROR) return IO_ERROR;
  triggerValue = tmplong;
  if ((tmpint  = is->getCondition()) == IO_ERROR) return IO_ERROR;
  condition = tmpint;
  if ((tmpint  = is->getID())        == IO_ERROR) return IO_ERROR;
  actionID = tmpint;
  if ((tmpint = is->getState())      == IO_ERROR) return IO_ERROR;
  actionState = tmpint;
  if ((tmplong = is->getLong())   == IO_NUMERROR) return IO_ERROR;
  actionParameters = tmplong;
  if ((tmplong = is->getNum())    == IO_NUMERROR) return IO_ERROR;
  delayMs = tmplong;
  if ((tmpint  = is->getBool())      == IO_ERROR) return IO_ERROR;
  repeat = tmpint;
  if (is->getChar() != TRIGGER_END) return IO_ERROR;
  return 0;
}

void Trigger::sendTrigger(OutputStream *os) {
  os->putChar(TRIGGER_START);
  os->putID(sensorID);
  os->putState(reqdState);
  os->putNum(triggerValue);
  os->putCondition(condition);
  os->putID(actionID);
  os->putState(actionState);
  os->putLong(actionParameters);
  os->putNum(delayMs);
  os->putBool(repeat);
  os->putChar(TRIGGER_END);
}



