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

#include "Sensact.h"
#include "Sensors.h"
#include "Triggers.h"
#include "Actions.h"
#include "IO.h"
#include <EEPROM.h>
#include <SoftwareSerial.h>
//#include <IRLib2.h>
#include <Wire.h>

#ifdef MEMCHECK
#define MEMCHECK_SIZE 652
brkPoints BreakPoints;
char memcheck_done = 0;
char memcheck_started = 0;
#endif

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

#ifdef MEMCHECK
  BreakPoints.atStart = (int) __brkval;
#endif
  sensors.init();
  actors.init();
  triggers.init(sensors.getHighestID());
#ifdef MEMCHECK
  BreakPoints.triggersInit = (int) __brkval;
#endif

  runMode = RUN;
  setLED();

}

void loop() {
  int cmd = checkForCommand();
  int val;
  
//  Serial.print(F("ram: ")); Serial.println(freeRam());
//  delay(1000);

#ifdef MEMCHECK
  if (runMode == IDLEX) {
    if (!memcheck_started) {
      startMemCheck();
      memcheck_started = 1;
    }
    if (!memcheck_done) {
      doMemCheck();
      memcheck_done = 1;
    }
//    Serial.print(F("ram: ")); Serial.println(freeRam());
  } else {
    memcheck_done = 0;
  }
#endif

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

#ifdef MEMCHECK
char *memBuf;
void startMemCheck() {
  memBuf = new char[MEMCHECK_SIZE];
  for(int i=0; i<MEMCHECK_SIZE; i++) {
    memBuf[i] = 1;
  }
}

void doMemCheck() {
  extern int __heap_start; 
  int i;
  for(i=0; i<MEMCHECK_SIZE; i++) {
    if (memBuf[i] != 1) {
      break;
    }
  }
  Serial.println();
  Serial.println((int) &__heap_start);
  Serial.println(BreakPoints.atStart);
  Serial.println(BreakPoints.sensorsAlloc);
  Serial.println(BreakPoints.sensorsInit);
  Serial.println(BreakPoints.actorsAlloc);
  Serial.println(BreakPoints.actorsInit);
  Serial.println(BreakPoints.triggersInit);
  Serial.println((int) __brkval);

  Serial.println((int) &i);

  Serial.println((int)memBuf);
  Serial.print("MemCheck: "); Serial.println(i);
}
#endif
  
