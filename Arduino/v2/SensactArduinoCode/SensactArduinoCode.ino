/*
 * This code is very similar to the original Sensact code, except it is split into classes for a modular architecture.
 * 
 * Most of the work goes on in the Controller class.
 * 
 * NL
 */


#include "Sensor.h"
#include "Controller.h"
#include "pitches.h"
#include <Mouse.h>
#include <Keyboard.h>
#include <SoftwareSerial.h>
#include <EEPROM.h>

/******************************************************************************
 * Define the options for the Sensact Here
 ******************************************************************************/

#define RESET_FROM_EEPROM
#define INCLUDE_BTHID
//#define INCLUDE_BTXBEE

#define XBEE_NUM_SENSORS 3

/*******************************************************************************
 * Define the pins for INPUTs and OUTPUTs
 * 
 * This should be updated as the Sensact PINouts change between versions
 *******************************************************************************/
#define IN_PIN1 0
#define IN_PIN2 1
#define IN_PIN3 2
#define IN_PIN4 3
#define IN_PIN5 4


/* The LED_RED pin should be put on a digital line for the next version so that we can use more analog inputs */
#define LED_RED A3
#define LED_GREEN 5
#define LED_BLUE 4

#define BUZZER_PIN 10
#define RELAY_PINA 9
#define RELAY_PINB 11


/***********************************************************************************
 * The code for the Sensact starts here
 ***********************************************************************************/



enum state{
  RUN,
  CONFIG,
  DEBUG
};
state currentState;


void response_callback(byte r, byte d);
Controller controller(&response_callback);

//used to store the most recent readings of the sensors
byte readings[SENSOR_NUM];
float xbeeSensors[XBEE_NUM_SENSORS];
//used to store the incoming config sequences
char inString[300]; 


#ifdef INCLUDE_BTHID
  #define BT_TX_PIN 0
  #define BT_RX_PIN 1
  SoftwareSerial blueHID(BT_TX_PIN,BT_RX_PIN);
#endif

#ifdef INCLUDE_BTHID
void bt_setup()
{
//   if( USE_BT == 0 ) return;

   delay(1000);

   blueHID.begin(115200);  // The Bluetooth Mate defaults to 115200bps

   blueHID.print("$");  // Print three times individually
   blueHID.print("$");
   blueHID.print("$");  // Enter command mode
   delay(100);  // Short delay, wait for the Mate to send back CMD
   blueHID.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
   // 115200 can be too fast at times for NewSoftSerial to relay the data reliably

   blueHID.begin(9600);  // Start bluetooth serial at 9600
}
#endif

void reset_from_EEPROM(){
  controller.read_sensors_from_EEPROM();
}

void setup() {

  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(RELAY_PINA,OUTPUT);
  pinMode(RELAY_PINB,OUTPUT);

  // The Sensact starts in RUN Mode
  currentState = RUN;
  
  Serial.begin(9600);
  
  Mouse.begin();
  Keyboard.begin();
  
#ifdef INCLUDE_BTHID
    bt_setup();
#endif

#ifdef INCLUDE_BTXBEE
    Serial1.begin(115200);
#endif 

#ifdef RESET_FROM_EEPROM
  reset_from_EEPROM();
#endif
}


void loop() {
  if(Serial.available()){
    serial_loop();
  }
  
#ifdef INCLUDE_BTXBEE
    readBT();
#endif 

  read_sensors();
  
  
  //Precess Responses when in RUN or DEBUG mode, print sensor data when in DEBUG or CONFIG mode
  if(currentState != CONFIG)
    controller.process_triggers();
  if (currentState != RUN)
    printData();



  //update the LED to reflect the current status
  led_loop();
 
  //reset the relays
  digitalWrite(RELAY_PINA, LOW);
  digitalWrite(RELAY_PINB, LOW);
  
  delay(10);
}

//Taken from Sensact Code
void serial_loop() {
//  Serial.println("Serial loop");
  int charpos = 0;
   int inChar;
   // Read serial input:
   while (Serial.available() > 0 ) { 
      if( Serial.available() > 0 ) {
         inChar = Serial.read();       
      }

      if (inChar == '\n') {
         inString[charpos] = 0;
         process_serial();  //process a whole line
         // clear the string for new input:
         charpos = 0;
         break;
      }
      inString[charpos++] = (char)inChar;
   }
}

void led_loop(){
  switch(currentState){
    case CONFIG:
      digitalWrite(LED_RED, HIGH);
      digitalWrite(LED_GREEN, LOW);
      digitalWrite(LED_BLUE, LOW);
      break;
    case RUN:
      digitalWrite(LED_GREEN, HIGH);
      digitalWrite(LED_RED, LOW);
      digitalWrite(LED_BLUE, LOW);
      break;    
    case DEBUG:
      digitalWrite(LED_GREEN, LOW);
      digitalWrite(LED_RED, LOW);
      digitalWrite(LED_BLUE, HIGH);
      break;    
  }
}

void process_serial(){
  char* val = strtok(inString,",");
  byte sCount = 0, tCount = 0;
//  byte sens[8];
    switch(atoi(val)){
      case 0: //config package
        currentState = CONFIG;
        controller.set_sensor_param_package(&inString[2]);
        controller.write_sensors_to_EEPROM();
        break;
      case 7: //debug mode
        currentState = DEBUG;
        break;
      case 8: //report current config setup
        {
          currentState = CONFIG;
          Serial.print("9999,");
          Serial.print(controller.getHeldThreshold());
          Serial.print(",");
          char buff[300] = {0};
          controller.get_sensor_param_package(buff);
          Serial.print(buff);
          Serial.println("");
          Serial.flush();
        }
        break;
      case 9: //run Sensact
        currentState = RUN;
        break;
      default:
        break;
    }
}

/*
 * Reads the analog pins and updates the controller.
 */
void read_sensors(){

  readings[0] = analogRead(IN_PIN1) * 100.0/1024;
  readings[1] = analogRead(IN_PIN2) * 100/1024;
  readings[2] = analogRead(IN_PIN3) * 100/1024;
  readings[3] = analogRead(IN_PIN4) * 100/1024;
  readings[4] = analogRead(IN_PIN5) * 100/1024;

  //this is the bluetooth sensor data
  readings[5] = xbeeSensors[0];
  readings[6] = xbeeSensors[1];
  readings[7] = xbeeSensors[2];

  controller.update_sensor_values(readings, sizeof(readings));
}


/* 
 * This is in the form of a callback. When the controller knows that a trigger should happen, it will call this function
 * I made it a callback so that the controller wouldn't have to deal with any communications.
 * It could be changed quite easily
 * 
 * NL
 */
void response_callback(byte r, byte d){
//  Serial.print("got callback:");
//  Serial.print(r);
//  Serial.print(",");
//  Serial.println(d);

  
  switch(r){
    case 1: //relay A
      digitalWrite(RELAY_PINA, HIGH);
      break; 
    case 2: //relay B
      digitalWrite(RELAY_PINB, HIGH);
      break;
    case 3: // Bluetooth HID
      blueHID.write((char)d);
      break;
    case 4: //Keyboard
      
      Keyboard.write(d);
      break;
    case 5: //Mouse
      switch(d){
        case 0: //move left
          Mouse.move(-5,0);
          break;
        case 1: //move right
          Mouse.move(5,0);
          break;
        case 2: //move up
          Mouse.move(0,-5);
          break;
        case 3: //move down
          Mouse.move(0,5);
          break;
        case 4: //click
          Mouse.click();
          break;
        default:
          break;
      }
    
      break;
    case 6: //Buzzer
      beep(1);
      break;
    case 7: // IR
      break;
    default:
      break;
  }
}

/* Taken from Sensact Code */
void beep( int j) {
   if (j<1) return;
   for( int i=0; i<j; i++ ) {
      tone(BUZZER_PIN, NOTE_C7, 250);
      delay(50);
      //noTone(SENSACT_BUZZER);      
      delay(150);

      /*
      digitalWrite( SENSACT_BUZZER, HIGH );
       delay(10);
       digitalWrite( SENSACT_BUZZER, LOW );
       delay(150);
       */
   }
}

/*
 * Outputs the current sensor readings
 */
void printData(){
  Serial.print(readings[0]);
  for(int i = 1; i < SENSOR_NUM; i++){
    Serial.print(",");
    Serial.print(readings[i]);
  }
  Serial.println();
}

#ifdef INCLUDE_BTXBEE

void readBT(){
  char incoming[20];
  byte i = 0;
  byte j = 0;
  while(Serial1.available()){
    incoming[i] = Serial1.read();
   if(incoming[i] == ',' || incoming[i] == '\n'){
    xbeeSensors[j++] = atof(incoming);
    i = 0;
   }else{
    i++;
   }
  }
}
#endif
