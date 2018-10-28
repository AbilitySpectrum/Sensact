/*
 * Test Code for Sensact board V3
 * 
 * Update: For V4.1 Hardware.
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

#define OUTPUT_A 11  
#define OUTPUT_B 12  
#define IR_PIN 9

#define LED_RED   5
#define LED_GREEN 6
#define LED_BLUE  7

#define BUZZER  10

GyroSensor gyro;

void setup() {
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);

  pinMode(INPUT_1, INPUT);
  pinMode(INPUT_2, INPUT);
  pinMode(INPUT_3, INPUT);
  pinMode(INPUT_4, INPUT);
  pinMode(INPUT_5, INPUT);
  pinMode(INPUT_6, INPUT);

  pinMode(IR_PIN, OUTPUT);
  pinMode(OUTPUT_A, OUTPUT);
  pinMode(OUTPUT_B, OUTPUT);  

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
    cmd =  getNextToken();
    if (cmd == -1) { // Nothing but white space.
      return;
    } 
    readVal = 0;
    switch(cmd) {                
      case 'o':
        val = getNextToken();
        if (val == 'a') {
          doOutput(OUTPUT_A);
          Serial.println("Output A on" ); 
        } else if (val == 'b') {
          doOutput(OUTPUT_B);
          Serial.println("Output B on" ); 
        } else if (val == '0') {
          doOutput(0);
          Serial.println("Outputs off" ); 
        } else {
          Serial.println("Bad output option.");
        }
        break;

      case 'l':
        val = getNextToken();
        if (val == 'r') {
          setLED(LED_RED);
          Serial.println("LED Red" ); 
        } else if (val == 'g') {
          setLED(LED_GREEN);
          Serial.println("LED Green" ); 
        } else if (val == 'b') {
          setLED(LED_BLUE);
          Serial.println("LED Blue" ); 
        } else if (val == '0') {
          setLED(0);
          Serial.println("LED off");
        } else {
          Serial.println("Bad LED value.");
        }
        break;
        
      case 'b':
        Serial.println("BEEP!");
        doBeep();
        break;
        
      case 'r':
        val = getNextToken();
        val = val - '0';
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

int getNextToken() {
  int val;
  val = Serial.read();
  // Ignore white space
  while(val == ' ' || val == '\r' || val == '\n' || val == '\t') {
    val = Serial.read();
  }
  return val;
}

void doBeep() {
  tone( BUZZER, 400, 250);
  delay(250);
  tone( BUZZER, 500, 250);
  delay(250);
  tone( BUZZER, 600, 250);
  delay(250);
  tone( BUZZER, 800, 500);
}

void setLED(int color) {
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, LOW);
  digitalWrite(LED_BLUE, LOW);
  if (color != 0) {
    digitalWrite(color, HIGH);
  }
}

void doOutput(int val) {
  digitalWrite(OUTPUT_A, LOW);
  digitalWrite(OUTPUT_B, LOW);
  digitalWrite(val, HIGH);
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
  Serial.println(" 'o' + 'a' or 'b. Turns on an output port.");
  Serial.println(" 'o0' (o + zero) Turns all outputs off.");
  Serial.println(" 'l' + 'r', 'g' or 'b'.   Sets the color of the LED.");
  Serial.println(" 'l0'            Turns the LED off.");
  Serial.println(" 'b'             Sounds the buzzer.");
  Serial.println(" 'r'             Reads the value of all input pins.");
  Serial.println(" 'r' 1 to 6      Reads the value of a particular input port ");
  Serial.println("                 repeating until another command is entered.");
  Serial.println("                 (1 = I1A, 2 = I1B, 3 = I2A ... 6 = I3B)");
  Serial.println(" 'g'             Reads I2C Gyroscope.");
  Serial.println(" 't'             Runs the TV IR.  On/Off cycling every 1/4 second for two seconds.");
  Serial.println("                 Watch with a cell phone camera or with a multi-tester.");
}
