// ---------------------------------------
// Sensact code - Version 3.
// This is a complete re-think of the project.
// The class structure has been redefined.  Sensors, triggers and actions
// are not in isolated blocks of code which share only the information they have to share.
//
// Previously the system was centered on sensors which could have associated actions.
// This version is centered on triggers, which are associated with a sensor and an action.
// The addition of triggering states makes it possible to assemble the triggers into state
// machines which can be the basis for getting multiple functions from a single sensor.
// ----------------------------------------

#include "Sensact.h"
#include "Sensors.h"
#include "Triggers.h"
#include "Actions.h"
#include "IO.h"
#include <EEPROM.h>
#include <SoftwareSerial.h>
#include <IRLib.h>

enum rMode{RUN, REPORT};
rMode runMode;

extern Sensors sensors;
Triggers triggers;
extern Actors actors;
SerialInputStream serialInput;
SerialOutputStream serialOutput;

long lastActionTime = 0;

void setup() {
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
  
  sensors.init();
  actors.init();
  triggers.init();
  
  Serial.begin(9600);
  
  runMode = RUN;
  setLED();
}

void loop() {
  int cmd = checkForCommand();
  int val;
  
//  Serial.print(F("ram: ")); Serial.println(freeRam());
  switch(cmd) {
    case START_OF_TRIGGER_BLOCK:
      serialInput.init();
      val = triggers.readTriggers(&serialInput);
      if (val == IO_ERROR) {
        flashLED(LED_RED);
      } else {
        flashLED(LED_GREEN);
      }
      break;
      
    case REQUEST_TRIGGERS:
      serialOutput.init();
      triggers.sendTriggers(&serialOutput);
      break;
      
    case GET_VERSION:
      sendVersionInfo();  // TBD
      break;
      
    case RUN_SENSACT:
      runMode = RUN;
      setLED();
      break;
      
    case REPORT_MODE:
      runMode = REPORT;
      setLED();
      break;
  }
  
  const SensorData *pSensorData = sensors.getData();
  
  if (runMode == REPORT) {
    if ((lastActionTime + REPORTING_INTERVAL) < millis()) {
      report(pSensorData);
      lastActionTime = millis();
    }
    
  } else {
    if ((lastActionTime + READING_INTERVAL) < millis()) {
      const ActionData *pActionData = triggers.getActions(pSensorData);
      actors.doActions(pActionData);
      lastActionTime = millis();
    }
  }
}

int checkForCommand() {
  while(Serial.available()) {
    int val = Serial.read();
    // Is it one of the unique command characters (Q,R,S,T,U or V) ?
    if (val >= MIN_COMMAND && val <= MAX_COMMAND) {
      return val;
    }
  }
  return 0;
}

void flashLED(int led) {
  ledsOff();
  delay(250);
  digitalWrite(led, HIGH);
  delay(250);
  digitalWrite(led, LOW);
  delay(250);
  setLED();
}


void setLED() {
  ledsOff();
  if (runMode == RUN) {
     digitalWrite(LED_GREEN, HIGH);
  } else {  // REPORT mode
     digitalWrite(LED_RED, HIGH);
  }
} 
  
void ledsOff() {
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_BLUE, LOW);
  digitalWrite(LED_GREEN, LOW);
}  

void sendVersionInfo() {
  // TBD
}

// Report sensor values
void report(const SensorData *sdata) {
  serialOutput.init();
  serialOutput.putChar(START_OF_SENSOR_DATA);
  int len = sdata->length();
  serialOutput.putNum(len);
  for(int i=0; i<len; i++) {
    const SensorDatum *d = sdata->getValue(i);
    serialOutput.putID(d->sensorID);
    serialOutput.putNum(d->sensorValue);
  }
  serialOutput.putChar('\n');  // For debug readability
  serialOutput.putChar(END_OF_BLOCK);
}

int freeRam ()  {
   extern int __heap_start, *__brkval; 
   int v; 
   return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval); 
}
  
