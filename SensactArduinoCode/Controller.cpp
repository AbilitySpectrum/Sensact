#include "Arduino.h"
#include "Controller.h"


Controller::Controller(void (*callback)(byte r, byte d)){
  response_callback = callback;
}


void Controller::process_triggers(){
  for(byte s = 0; s < SENSOR_NUM; s++){
    for(byte t = 0; t< TRIG_NUM; t++){
      if(sensors[s].getResponse(t) == 0) //don't bother processing events if there is no response
        continue;

      //This switch checks what type of triggering event the trigger uses and processes it accordlingly
      switch(sensors[s].getEvent(t)){
        case 0: //Rising edge
          //If will only recognize a rising edge as long as there was enough time from the last 'held below' trigger.
          // The number 20 is arbitrary and can be set to any number
          if(oldValues[s] < sensors[s].getLevel(t) && currentValues[s] > sensors[s].getLevel(t) && sensors[s].getCounter() < 20 ){
            response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
          }
          break;
        
        case 1://Falling Edge
          //If will only recognize a rising edge as long as there was enough time from the last 'held above' trigger.
          // The number 20 is arbitrary and can be set to any number
          if(oldValues[s] > sensors[s].getLevel(t) && currentValues[s] < sensors[s].getLevel(t) && sensors[s].getCounter() < 20 ){
            response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
          }
          break;
        
        case 2: //Above Level
          if(currentValues[s] > sensors[s].getLevel(t))
            response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
          break;
        
        case 3: //Below Level
          if(currentValues[s] < sensors[s].getLevel(t))
            response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
          break;
        
        case 4: //held above
          if(currentValues[s] > sensors[s].getLevel(t)){
                    
            //loops here and increments the counter every time. if the sensor is held above the trigger level for long enough, the response will occur
            // the number 50 is arbitrary, and can be set to whatever number is appropriate to give the desired effect.
            if(sensors[s].getCounter() >= 50){ 
              response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
              sensors[s].resetCounter();
            }else{
              sensors[s].incrementCounter();
            }
          }else{
            sensors[s].decrementCounter();
          }
          break;
        
        case 5: //held below
          if(currentValues[s] < sensors[s].getLevel(t)){
                    
            //loops here and increments the counter every time. if the sensor is held above the trigger level for long enough, the response will occur
            if(sensors[s].getCounter() >= 100){ 
              response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
              sensors[s].resetCounter();
            }else{
              sensors[s].incrementCounter();
            }
          }else{
            sensors[s].decrementCounter();
          }
          break;
      }

    }
  }
}

//TODO: for some reason the size is 2...
bool Controller::update_sensor_values(byte* newValues){
  //the number of sensors doesn't match
//  if(sizeof(newValues)/sizeof(byte) != SENSOR_NUM)
//    return false;

//  Serial.print("updating sensor values");

  for(int i = 0; i < SENSOR_NUM; i++){
    oldValues[i] = currentValues[i];
    currentValues[i] = newValues[i];
  }

  return true;
}

bool Controller::set_sensor_param_package(char* inString){
//  if(sizeof(inString)/sizeof(char) != SENSOR_NUM*TRIG_NUM)
//    return false;


  char* val = strtok(inString,",");
  byte sCount = 0, tCount = 0;
  byte sens[8];

  while (val != NULL){
    sens[tCount++] = atoi(val);
    if(tCount == 8){
      tCount = 0;
      sensors[sCount++].update_sensor_params(sens);
    }
    val = strtok(NULL,",");
  }
}

int Controller::get_sensor_param_package(char* buff){
  char t[TRIG_NUM*12];
  int count = 0;
  for(int i = 0;i < SENSOR_NUM; i++){
    int num = sensors[i].get_sensor_params(t);
    strncpy(&buff[count], t, num);
    count += num;
  }
  
  //set the last character to null so the cstring is finished.
  //--count because the last character is a ',', we remove it and replace it with a 0
  buff[--count] = 0; 
  

  return count;
}

void Controller::write_sensors_to_EEPROM(){
  int currentAddress = 0;
  for(int i = 0; i < SENSOR_NUM; i++){
    currentAddress += sensors[i].write_to_EEPROM(currentAddress);
  }
}

void Controller::read_sensors_from_EEPROM(){
  int currentAddress = 0;
  for(int i = 0; i < SENSOR_NUM; i++){
    currentAddress += sensors[i].read_from_EEPROM(currentAddress);
  }
}

