
#include "Sensor.h"
#include <Arduino.h>

Sensor::Sensor(){
  for(int i = 0; i < TRIG_NUM; i++){
    triggers[i].level = 0;
    triggers[i].event = 0;
    triggers[i].response = 0;
    triggers[i].detail = 0;
  }
  counter = 0;
}

void Sensor::update_sensor_params(byte* params){
  for(int i = 0; i < TRIG_NUM; i++){
    triggers[i].level = params[i*4];
    triggers[i].event = params[i*4+1];
    triggers[i].response = params[i*4+2];
    triggers[i].detail = params[i*4+3];
  }
}

int Sensor::get_sensor_params(char* buff){
  int count = 0;
  int num = 0;
  char t[4]={0};

  //loop adding the trigger params for this sensor
  for(int i = 0; i < TRIG_NUM; i++){

    //sprintf changes the number to ascii characters
    num = sprintf(t,"%d",triggers[i].level);
    for(int j = 0;j<num; j++){
      buff[j+count] = t[j];
    }
    count += num;
    buff[count++] = ',';
    
    buff[count++] = '0' + triggers[i].event; //convert the int to a char
    buff[count++] = ',';
    buff[count++] = '0' + triggers[i].response; //convert int to char
    buff[count++] = ',';
    num = sprintf(t,"%d",triggers[i].detail);
    for(int j = 0;j<num; j++){
      buff[j+count] = t[j];
    }
    count += num;

    buff[count++] = ',';
  }
//  buff[count++] = 0; //add this to the end to show that the c_string is finished
  return count;
}

int Sensor::write_to_EEPROM(int startByte){
  int it = startByte;
  byte t = 0;
  for(int i = 0; i < TRIG_NUM; i++){
    EEPROM.write(it++,triggers[i].level);
    EEPROM.write(it++,triggers[i].event);
    EEPROM.write(it++,triggers[i].response);
//    t = triggers[i].event;
//    t = t | (triggers[i].response << 4);
//    EEPROM.write(it++,t);

    EEPROM.write(it++,triggers[i].detail);
  }
  return it-startByte;
}

int Sensor::read_from_EEPROM(int startByte){
  int it = startByte;
  byte t = 0;
  for(int i = 0; i < TRIG_NUM; i++){
    triggers[i].level = EEPROM.read(it++);
    triggers[i].event = EEPROM.read(it++);
    triggers[i].response = EEPROM.read(it++);
    triggers[i].detail = EEPROM.read(it++);
//    t = EEPROM.read(it++);
//    triggers[i].event = t & 0b1111;
//    triggers[i].response = triggers[i].response >> 4;
    
  }
  return it-startByte;
}


byte Sensor::getCounter(){return counter;}
void Sensor::incrementCounter(){ ++counter;}
void Sensor::decrementCounter(){ if(counter>0) {--counter; }}
void Sensor::resetCounter(){ counter = 0;}

byte Sensor::getLevel(byte trig){return triggers[trig].level;}
byte Sensor::getEvent(byte trig){return triggers[trig].event;}
byte Sensor::getResponse(byte trig){return triggers[trig].response;}
byte Sensor::getDetail(byte trig){return triggers[trig].detail;}

long Sensor::getTimer(){ return heldTimer;}
void Sensor::setTimer(long t){ heldTimer = t;}

bool Sensor::getTriggered(){ return heldTriggered;}
void Sensor::setTriggered(bool t){ heldTriggered = t;}



