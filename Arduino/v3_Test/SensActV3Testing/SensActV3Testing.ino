/*
 * Test Code for Sensact board V3
 * 
 * Commands can be sent via the serial link to drive Sensact functions.
 * 
 * 'i' + 0 to 15.  Sets power to the input jacks according to bit values.
 * 'o' + 0 to 3.   Turns on the output ports according to bit values.
 * 'l' + 0 to 7.   Sets the value of LED outputs.
 * 'b'             Sounds the buzzer.
 * 'r'             Reads the value of all input pins..
 * 'r' 1 to 6      Reads the value of a particular input pin.
 */

#include "Gyro.h"

#define INPUT_1   A0 
#define INPUT_2   A1 
#define INPUT_3   A2 
#define INPUT_4   A3 
#define INPUT_5   A4 
#define INPUT_6   A5 

#define RELAY_1 11
#define RELAY_2 12
#define IR_PIN 9

#define BUZZER  10
#define LATCH_PIN 4
#define COUNTER_PIN 6
#define COUNTER_RESET_PIN 7

#define DELAY_TIME  5 // Time in MS to wait for chips to reset, latch etc.  
                      // Probably can be much shorter.  Chips response times are given on ns on spec sheet.

GyroSensor gyro;

void setup() {
  pinMode(COUNTER_RESET_PIN, OUTPUT);
  pinMode(COUNTER_PIN, OUTPUT);
  pinMode(LATCH_PIN, OUTPUT);

  pinMode(INPUT_1, INPUT);
  pinMode(INPUT_2, INPUT);
  pinMode(INPUT_3, INPUT);
  pinMode(INPUT_4, INPUT);
  pinMode(INPUT_5, INPUT);
  pinMode(INPUT_6, INPUT);

  pinMode(IR_PIN, OUTPUT);
  pinMode(RELAY_1, OUTPUT);
  pinMode(RELAY_2, OUTPUT);  

  Serial.begin(9600);
  while(!Serial);
}

int readVal = 0;
long lastReadTime;
#define READ_INTERVAL_MS 1000

void loop() {
  int cmd;
  int val;

  if (readVal && ((millis() - lastReadTime) > READ_INTERVAL_MS) ) {
    doRead(readVal);
    lastReadTime = millis();
  }
  if (Serial.available()) {
    readVal = 0;
    cmd =  Serial.read();
    switch(cmd) {
      // Ignore white space
      case ' ':
      case '\n':
      case '\r':
      case '\t':
        break;

      case 'i':
        val = getInt();
        if (val >= 0 && val <= 15) {
          Serial.print("Input " ); Serial.println(val);
          setLatches(val);
        } else {
          Serial.println("Bad input value.");
        }
        break;
        
      case 'o':
        val = getInt();
        if (val >= 0 && val <= 3) {
          doOutput(val);
          Serial.print("Output " ); Serial.println(val);
        } else {
          Serial.println("Bad output value.");
        }
        break;

      case 'l':
        val = getInt();
        if (val >= 0 && val <= 7) {
          doLED(val);
          Serial.print("LED " ); Serial.println(val);
        } else {
          Serial.println("Bad LED value.");
        }
        break;
        
      case 'b':
        Serial.println("BEEP!");
        doBeep();
        break;
        
      case 'r':
        val = getInt();
        if (val == 0) {
          Serial.println("Read All.");
          doRead(0);
        } else if (val >= 1 && val <= 6) {
          Serial.print("Read " ); Serial.println(val);   
          doRead(val);   
          readVal = val;
          lastReadTime = millis();    
        } else {
          Serial.println("Bad read value.");
        }
        break;

      case 'g':
        setLatches(0);  // Turn off power to gyro
        delay(100);     // Wait for it to shut down completely.
        setLatches(8);  // Power on gyro
        delay(10);
        gyro.init();
        delay(10);     
        gyro.readValues();
        break;

      case 't':
      Serial.println("TV IR");
        for(int i=0; i<8; i++) {
          analogWrite(IR_PIN, 80);  // ~30% duty cycle
          delay(250);
          digitalWrite(IR_PIN, 0);
          delay(250);
        }
        break;
        
      case 'h':
        Serial.println("Help");
        doHelp();
        break;

      default:
        Serial.println("Huh?");
        break;
    }
  }

}

int getInt() {
  int val = 0;
  
  while(Serial.available()) {
    int ch = Serial.read();
    if (ch >= '0' && ch <= '9') {
      val = val * 10 + (ch - '0');
    } else {
      return val;
    }
  }
  return val;
}

void doBeep() {
  tone( BUZZER, 400, 500);
}

void setLatches(int latchBits) {
  doLED(latchBits);

  // Latch the counter output to the Latch_Qn outputs.
  digitalWrite(LATCH_PIN, HIGH);
  delay(DELAY_TIME);
  digitalWrite(LATCH_PIN, LOW); 

  // Turn off LEDs
  doLED(0);
}

void doLED(int val) {
  digitalWrite(COUNTER_PIN, LOW); // Ensure the right start point

  // Reset the counter
  digitalWrite(COUNTER_RESET_PIN, HIGH);
  delay(DELAY_TIME);      
  digitalWrite(COUNTER_RESET_PIN, LOW);

  // Set the counter - each low-to-high transition adds 1 to the counter
   for(int i=0; i<val; i++) {
    digitalWrite(COUNTER_PIN, HIGH);
    delay(DELAY_TIME);
    digitalWrite(COUNTER_PIN, LOW);
    delay(DELAY_TIME);
  }
}

void doOutput(int val) {
  if (val & 0x01) {
    digitalWrite(RELAY_1, HIGH);
  } else {
    digitalWrite(RELAY_1, LOW);
  }
  if (val & 0x02) {
    digitalWrite(RELAY_2, HIGH);
  } else {
    digitalWrite(RELAY_2, LOW);
  }
}

void doRead(int val) {
  if (val == 0) {
    Serial.print("Input 1 = "); Serial.println( analogRead(INPUT_1) );
    Serial.print("Input 2 = "); Serial.println( analogRead(INPUT_2) );
    Serial.print("Input 3 = "); Serial.println( analogRead(INPUT_3) );
    Serial.print("Input 4 = "); Serial.println( analogRead(INPUT_4) );
    Serial.print("Input 5 = "); Serial.println( analogRead(INPUT_5) );
    Serial.print("Input 6 = "); Serial.println( analogRead(INPUT_6) );
  } else {
    switch(val) {
      case 1:
        Serial.print("Input 1 = "); Serial.println( analogRead(INPUT_1) );
        break;
      case 2:
        Serial.print("Input 2 = "); Serial.println( analogRead(INPUT_2) );
        break;
      case 3:
        Serial.print("Input 3 = "); Serial.println( analogRead(INPUT_3) );
        break;
      case 4:
        Serial.print("Input 4 = "); Serial.println( analogRead(INPUT_4) );
        break;
      case 5:
        Serial.print("Input 5 = "); Serial.println( analogRead(INPUT_5) );
        break;
      case 6:
        Serial.print("Input 6 = "); Serial.println( analogRead(INPUT_6) );
        break;
    }
  }
}

void doHelp() {
  Serial.println(" 'i' + 0 to 15.  Sets power to the input jacks according to bit values.");
  Serial.println(" 'o' + 0 to 3.   Turns on the output ports according to bit values.");
  Serial.println(" 'l' + 0 to 7.   Sets the value of LED outputs.");
  Serial.println(" 'b'             Sounds the buzzer.");
  Serial.println(" 'r'             Reads the value of all input pins.");
  Serial.println(" 'r' 1 to 6      Reads the value of a particular input port ");
  Serial.println("                 repeating until another command is entered.");
  Serial.println("                 (1 = I1A, 2 = I1B, 3 = I2A ... 6 = I3B)");
  Serial.println(" 'g'             Reads I2C Gyroscope. (Leaves non-I2C inputs powered off)");
  Serial.println(" 't'             Runs the TV IR.  On/Off cycling every 1/4 second for two seconds.");
  Serial.println("                 Watch with a cell phone camera or with a multi-tester.");
}
