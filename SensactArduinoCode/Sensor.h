/*
 * The Sensor class owns all the parameters for the sensors.
 * It can read from and make individual config packets, read and write itself to and from EEPROM
 */


#ifndef SENSOR_H
#define SENSOR_H

#define TRIG_NUM 2

#include "EEPROM.h"
#include <Arduino.h>
#include <Keyboard.h>

struct Trigger_t{
    byte level;
    byte event;
    byte response;
    byte detail;
};

class Sensor{
  private: 
    Trigger_t triggers[TRIG_NUM];
    byte counter; //rudimentary counter. keeps track of how long the signal is held above or held below.


  public:
    Sensor();
    
    /* the length of params should be equal to 4*the number of triggers per sensor */
    void update_sensor_params(byte* params);

    /* This fills the buffer with the data from the sensor
     *
     * @return the number of bytes written
     */
    int get_sensor_params(char* buff);

    /* Writes the data in the sensor to the EEPROM */
    int write_to_EEPROM(int startByte);

    /* Read the sensor params from EEPROM */
    int read_from_EEPROM(int startByte);

    byte getLevel(byte trig);
    byte getEvent(byte trig);
    byte getResponse(byte trig);
    byte getDetail(byte trig);
    
    byte getCounter();
    void incrementCounter();
    void decrementCounter();
    void resetCounter();
};


#endif
