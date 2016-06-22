#include "Sensor.h"

#include <Mouse.h>
//#include <Keyboard.h>
//#include <EEPROM.h>
//#include <SoftwareSerial.h>

#define BT_TX_PIN 3
#define BT_RX_PIN 2
#define INCLUDE_BTHID
#define INCLUDE_BTXBEE

//SoftwareSerial bluetooth(BT_TX_PIN,BT_RX_PIN);
float EA[3];

float oldX;


char inString[300];


Sensor sensors[8];

void setup() {
  Serial.begin(9600);
  
#ifdef INCLUDE_BTHID
//    bt_init();
#endif

#ifdef INCLUDE_BTXBEE
    Serial1.begin(115200);
#endif
  oldX = 0;
}

void loop() {
  if(Serial.available()){
    serial_loop();
  }

  if(Serial1.available()){
    readBT();
//    Serial.print(incoming);
  }

  
//  Serial.println("triggered");
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
        do{
          val = strtok(NULL,",");
          if(val != NULL){
//            Serial.println(val);
            sens[tCount++] = atoi(val);
            if(tCount == 8){
              tCount = 0;
              sensors[sCount++].update_sensor_params(sens);
            }
          }
        }while (val != NULL);
        
        break;
      case 8: //report current config setup
//        Serial.println("request");
        {
          Serial.print("9999 ");
          char buff[60] = {0};
          for(int i = 0; i < 8; i++){
            sensors[i].get_sensor_params(buff);
            Serial.print(buff);
          }
          Serial.println();
        }
        break;
      case 9: //run Sensact
        
        break;
      default:
        break;
    }
}

void printData(){
//  if(EA[0] > 300 && oldX < 300){
    
//  }
}

void readBT(){
  char incoming[8];
  byte i = 0;
  byte j = 0;
  oldX = EA[0];
  while(Serial1.available()){
    incoming[i] = Serial1.read();
//    Serial.print(incoming[i]);
   if(incoming[i] == ',' || incoming[i] == '\n'){
    EA[j++] = atof(incoming);
    i = 0;
   }else{
    i++;
   }
  }
//  Serial.print(oldX);
//  Serial.print(",");
//  Serial.println(EA[0]);
  printData();
}

