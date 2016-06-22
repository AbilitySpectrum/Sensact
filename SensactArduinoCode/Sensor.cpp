
#include "Sensor.h"
#include <Arduino.h>

Sensor::Sensor(){
  for(int i = 0; i < TRIG_NUM; i++){
    triggers[i].threshold = 0;
    triggers[i].event = 0;
    triggers[i].response = 0;
    triggers[i].detail = 0;
  }
}

void Sensor::update_sensor_params(byte* params){
  for(int i = 0; i < TRIG_NUM; i++){
    triggers[i].threshold = params[i*4];
    triggers[i].event = params[i*4+1];
    triggers[i].response = params[i*4+2];
    triggers[i].detail = params[i*4+3];
  }
}

int Sensor::get_sensor_params(char* buff){
  int count = 0;
  int num = 0;
  char t[4]={0};

  for(int i = 0; i < TRIG_NUM; i++){
    num = sprintf(t,"%d",triggers[i].threshold);
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
  buff[count++] = 0;
  return count;
}


