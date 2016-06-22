#ifndef SENSOR_H
#define SENSOR_H

#define TRIG_NUM 2

#include <Arduino.h>

struct Trigger_t{
    byte level;
    byte event;
    byte response;
    byte detail;
    byte counter; //rudimentary counter. keeps track of how long the signal is held above or held below.
    bool triggered; //tells if a response was just triggered or not. Used for held below and held above
};

class Sensor{
  private: 
    Trigger_t triggers[TRIG_NUM];


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
    void write_to_EEPROM(int startByte);

    /* Read the sensor params from EEPROM */
    void read_from_EEPROM(int startByte);

    byte getLevel(byte trig);
    byte getEvent(byte trig);
    byte getResponse(byte trig);
    byte getDetail(byte trig);
    byte getCounter(byte trig);
    byte getHeldFor(byte trig);
    bool getTriggered(byte trig);

    void setTriggered(byte trig, bool flag);
    void incrementCounter(byte trig);
    void resetCounter(byte trig);
};


#endif
