#ifndef CONTROLLER_H
#define CONTROLLER_H

#include "Sensor.h"

#define SENSOR_NUM 5

class Controller{
  private:
    Sensor sensors[SENSOR_NUM];
    byte currentValues[SENSOR_NUM];
    byte oldValues[SENSOR_NUM];

    /* 
     * Makes the response happen 
     * 
     * @param r - the integer number of the response
     * @param d - the detail of the response
     */
    void process_response(byte r, byte d);

  public:
  
    /*
     * This function loops through all triggers for every sensor, checks if it triggered, and handles the responses
     * 
     * @param newVal the current readings of all sensor
     * @param oldVal the old readings of all sensor
     */
    void process_triggers();

    /*
     * Calls the Sensor class' function to write to EEPROM
     */
    void write_sensors_to_EEPROM();

    /*
     * Calls the Sensor class' function to read from EEPROM
     */
    void read_sensors_from_EEPROM();
};


#endif
