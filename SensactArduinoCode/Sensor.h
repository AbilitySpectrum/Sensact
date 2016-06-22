#ifndef SENSOR_H
#define SENSOR_H

#define TRIG_NUM 2

#include <Arduino.h>

struct Trigger_t{
    byte threshold;
    byte event;
    byte response;
    byte detail;
};

class Sensor{
  private: 
    Trigger_t triggers[TRIG_NUM];


  public:
    Sensor();
    
    /* the length of params should be equal to 4*the number of triggers per sensor */
    void update_sensor_params(byte* params);
    
    int get_sensor_params(char* buff);
};


#endif
