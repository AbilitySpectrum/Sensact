/*
 * The Controller class owns and modulates the Sensor objects. It is responsible for reading and writing the sensor parameters to EEPROM, 
 * for checking when a trigger should occur, and for reading and creating config packages to talk to the CONFIG software.
 */


#ifndef CONTROLLER_H
#define CONTROLLER_H

#include "Sensor.h"


#define SENSOR_NUM 5

class Controller{
  private:
    Sensor sensors[SENSOR_NUM];

    //Used in rising and falling triggers
    byte currentValues[SENSOR_NUM];
    byte oldValues[SENSOR_NUM];

    /* 
     * Makes the response happen. 
     * This is a function that is called when an event occurs. The response is handled by whatever made the controller 
     * 
     * @param r - the integer number of the response
     * @param d - the detail of the response
     */
    void (*response_callback)(byte r,byte d);

  public:

    Controller(void (*callback)(byte r, byte d));
  
    /*
     * This function loops through all triggers for every sensor, checks if it triggered, and handles the responses
     * 
     * @param newVal the current readings of all sensor
     * @param oldVal the old readings of all sensor
     */
    void process_triggers();

    /*
     * Moves the current values to old values, and new values to current values.
     * 
     * @param newValues - the current readings of the sensors
     */
    bool update_sensor_values(byte* newValues);

    bool set_sensor_param_package(char* inString); 

    int get_sensor_param_package(char* buff);

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
