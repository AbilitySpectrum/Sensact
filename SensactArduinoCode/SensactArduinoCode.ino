/*
 * This code is very similar to the original Sensact code, except it is split into classes for a modular architecture.
 * 
 * Most of the work goes on in the Controller class.
 * 
 * NL
 */



#include "Sensor.h"
#include "Controller.h"
//
#include <Mouse.h>
#include <Keyboard.h>
//#include <SoftwareSerial.h>

#define BT_TX_PIN 3
#define BT_RX_PIN 2

#define RESET_FROM_EEPROM
//#define INCLUDE_BTHID
//#define INCLUDE_BTXBEE

/* Define the pins for INPUTs and relays here */
#define IN_PIN1 0
#define IN_PIN2 1
#define IN_PIN3 2
#define IN_PIN4 3
#define IN_PIN5 4

#define RELAY_PINA 9
#define RELAY_PINB 10

enum state{
  RUN,
  CONFIG
};
//SoftwareSerial bluetooth(BT_TX_PIN,BT_RX_PIN);

void response_callback(byte r, byte d);
Controller controller(&response_callback);

byte readings[SENSOR_NUM];
char inString[300];
state currentState;

float EA[3];
float oldX;

void setup() {

  pinMode(IN_PIN1,INPUT);
  pinMode(IN_PIN2,INPUT);
  pinMode(IN_PIN3,INPUT);
  pinMode(IN_PIN4,INPUT);
  pinMode(IN_PIN5,INPUT);

  pinMode(RELAY_PINA,OUTPUT);
  pinMode(RELAY_PINB,OUTPUT);

  currentState = CONFIG;
  
  Serial.begin(9600);
  
  Mouse.begin();
  Keyboard.begin();
  
#ifdef INCLUDE_BTHID
//    bt_init();
#endif

#ifdef INCLUDE_BTXBEE
//    Serial1.begin(115200);
#endif 

#ifdef RESET_FROM_EEPROM
  reset_from_EEPROM();
#endif
}

void reset_from_EEPROM(){
  controller.read_sensors_from_EEPROM();
}

void loop() {
  if(Serial.available()){
    serial_loop();
  }
//
//  if(Serial1.available()){
//    readBT();
////    Serial.print(incoming);
//  }

  read_sensors();
  
  if(currentState == RUN)
    controller.process_triggers();
  else
    printData();
    
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

void process_serial(){
  char* val = strtok(inString,",");
  byte sCount = 0, tCount = 0;
  byte sens[8];
    switch(atoi(val)){
      case 0: //config package
        currentState = CONFIG;
//        Serial.println("config");
        controller.set_sensor_param_package(&inString[2]);
        controller.write_sensors_to_EEPROM();
        break;
      case 8: //report current config setup
//        Serial.println("request");
        {
          currentState = CONFIG;
          Serial.print("9999,");
          char buff[300] = {0};
          controller.get_sensor_param_package(buff);
          Serial.println(buff);
        }
        break;
      case 9: //run Sensact
        currentState = RUN;
        break;
      default:
        break;
    }
}

void read_sensors(){

  readings[0] = analogRead(IN_PIN1) * 100.0/1024;
  readings[1] = analogRead(IN_PIN2) * 100/1024;
  readings[2] = analogRead(IN_PIN3) * 100/1024;
  readings[3] = analogRead(IN_PIN4) * 100/1024;
  readings[4] = analogRead(IN_PIN5) * 100/1024;

  controller.update_sensor_values(readings);
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
  //reset the relays here
  
  switch(r){
    case 1: //relay A
      //pulse relay A
      break; 
    case 2: //relay B
      //pulse relay B
      break;
    case 3: // Bluetooth HID
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
      break;
    case 7: // IR
      break;
    default:
      break;
  }
}

/*
 * Outputs the current sensor readings
 */
void printData(){
  Serial.print(readings[0]);
  for(int i = 1; i < 5; i++){
    Serial.print(",");
    Serial.print(readings[i]);
  }
  Serial.println();
}


//
//void readBT(){
//  char incoming[8];
//  byte i = 0;
//  byte j = 0;
//  oldX = EA[0];
//  while(Serial1.available()){
//    incoming[i] = Serial1.read();
////    Serial.print(incoming[i]);
//   if(incoming[i] == ',' || incoming[i] == '\n'){
//    EA[j++] = atof(incoming);
//    i = 0;
//   }else{
//    i++;
//   }
//  }
////  Serial.print(oldX);
////  Serial.print(",");
////  Serial.println(EA[0]);
//  printData();
//}

