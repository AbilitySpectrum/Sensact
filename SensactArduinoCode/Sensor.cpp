
#include "Sensor.h"
#include <Arduino.h>

Sensor::Sensor(){
  for(int i = 0; i < TRIG_NUM; i++){
    triggers[i].level = 0;
    triggers[i].event = 0;
    triggers[i].response = 0;
    triggers[i].detail = 0;
    triggers[i].counter = 0;
    triggers[i].triggered = false;
  }
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
  buff[count++] = 0; //add this to the end to show that the c_string is finished
  return count;
}

void Sensor::incrementCounter(byte trig){ triggers[trig].counter++;}
void Sensor::resetCounter(byte trig){ triggers[trig].counter = 0;}

byte Sensor::getLevel(byte trig){return triggers[trig].level;}
byte Sensor::getEvent(byte trig){return triggers[trig].event;}
byte Sensor::getResponse(byte trig){return triggers[trig].response;}
byte Sensor::getDetail(byte trig){return triggers[trig].detail;}
byte Sensor::getCounter(byte trig){return triggers[trig].counter;}
bool Sensor::getTriggered(byte trig){return triggers[trig].triggered;}
void Sensor::setTriggered(byte trig, bool flag){triggers[trig].triggered = flag;}

