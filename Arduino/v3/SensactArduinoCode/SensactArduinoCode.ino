// ---------------------------------------
// Sensact code - Version 3.
// This is a complete re-think of the project.
// The class structure has been redefined.  Sensors, triggers and actions
// are in isolated blocks of code which share only the information they have to share.
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
//#include <IRLib2.h>
#include <Wire.h>

enum rMode{RUN, REPORT, IDLEX};  // IDLE seems to be a keyword - thus IDLEX.
rMode runMode;

extern Sensors sensors;
extern PCInputSensor *pcInput;  // Needed here so we can push commands to it.
Triggers triggers;
extern Actors actors;
SerialInputStream serialInput;
SerialOutputStream serialOutput;

long lastActionTime = 0;

void setup() {

  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
  
  
  Serial.begin(9600);

  sensors.init();
  actors.init();
  triggers.init();

  runMode = RUN;
  setLED();
}

void loop() {
  int cmd = checkForCommand();
  int val;
  
//  Serial.print(F("ram: ")); Serial.println(freeRam());
//  delay(1000);

  switch(cmd) {
    case START_OF_TRIGGER_BLOCK:
      serialInput.init();
      val = triggers.readTriggers(&serialInput);
      if (val == IO_ERROR) {
        flashLED(LED_RED);
        tone(SENSACT_BUZZER, 190, 500);
      } else {
        flashLED(LED_GREEN);
        tone(SENSACT_BUZZER, 800, 200);
        delay(500);
        tone(SENSACT_BUZZER, 800, 200);
        triggers.reset();
        actors.reset();
        sensors.reset();
      }
      break;
      
    case REQUEST_TRIGGERS:
      serialOutput.init();
      triggers.sendTriggers(&serialOutput);
      break;
      
    case GET_VERSION: // Get Version also sets IDLEX mode.
      sendVersionInfo();  
      runMode = IDLEX;
      setLED();
      break;
      
    case RUN_SENSACT:
      runMode = RUN;
      triggers.reset();
      actors.reset();
      sensors.reset();
      setLED();
      break;
      
    case REPORT_MODE:
      runMode = REPORT;
      setLED();
      break;
    
    case KEYBOARD_CMD:
      int cmd = Serial.read();
      pcInput->setNextCmd(cmd);
      break;
  }
  
  const SensorData *pSensorData;
  
  if (runMode == REPORT) {
    if ((lastActionTime + REPORTING_INTERVAL) < millis()) {
      pSensorData = sensors.getData();
      report(pSensorData);
      lastActionTime = millis();
    }
    
  } else if (runMode == RUN) {
    if ((lastActionTime + READING_INTERVAL) < millis()) {
      pSensorData = sensors.getData();
      const ActionData *pActionData = triggers.getActions(pSensorData);
      actors.doActions(pActionData);
      lastActionTime = millis();
    } 
  } // ELSE IDLEX mode - do nothing.
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
  } else if (runMode == REPORT) {  
     digitalWrite(LED_RED, HIGH);
  } else { // IDLEX mode
     digitalWrite(LED_BLUE, HIGH);
  }
} 
  
void ledsOff() {
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_BLUE, LOW);
  digitalWrite(LED_GREEN, LOW);
}  

void sendVersionInfo() {
  Serial.print(GET_VERSION);  // V
  Serial.print(VERSION);  // version #
  Serial.print(END_OF_BLOCK); // Z
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
  
