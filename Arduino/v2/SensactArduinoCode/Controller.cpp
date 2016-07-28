


#include "Arduino.h"
#include "Controller.h"


Controller::Controller(void (*callback)(byte r, byte d)){
  response_callback = callback;
  heldThreshold = 1000;
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
          if(oldValues[s] < sensors[s].getLevel(t) && currentValues[s] > sensors[s].getLevel(t) && !sensors[s].getTriggered() ){
            response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
          }
          break;

        case 1://Falling Edge
          //If will only recognize a rising edge as long as there was enough time from the last 'held above' trigger.
          if(oldValues[s] > sensors[s].getLevel(t) && currentValues[s] < sensors[s].getLevel(t) && !sensors[s].getTriggered() ){
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

            /*  If the timer is 0, then the held above can start.
             *  It records when the held above starts and when it is held for over the desired time it triggers.
             *  Once it triggers it will continue to reset the timer to the current time until it is reset.
             */
            if(sensors[s].getTimer() == 0){
              sensors[s].setTimer(millis());
            }else if(!sensors[s].getTriggered() && millis() - sensors[s].getTimer() >= heldThreshold){
              response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
              sensors[s].setTriggered(true);
            }else if(sensors[s].getTriggered()){
              sensors[s].setTimer(millis());
            }
                    
          }else{

            /* 
             *  If the held above was triggered, it stays triggered for a moment to allow it to skip any falling edge triggers.
             *  Once the sensor falls from the long trigger for the required time, the held above is reset and can be used again.
             *  If it never triggered then it will reset automatically
             */
            if(sensors[s].getTriggered() && millis() - sensors[s].getTimer() >= 100){
              sensors[s].setTriggered(false);
              sensors[s].setTimer(0);
            }else if(!sensors[s].getTriggered()){
              sensors[s].setTimer(0);
            }
          }
          break;
        
        case 5: //held below
          if(currentValues[s] < sensors[s].getLevel(t)){

            if(sensors[s].getTimer() == 0){
              sensors[s].setTimer(millis());
            }else if(!sensors[s].getTriggered() && millis() - sensors[s].getTimer() >= heldThreshold){
              response_callback(sensors[s].getResponse(t),sensors[s].getDetail(t));
              sensors[s].setTriggered(true);
            }else if(sensors[s].getTriggered()){
              sensors[s].setTimer(millis());
            }
           
          }else{
            if(sensors[s].getTriggered() && millis() - sensors[s].getTimer() >= 200){
              sensors[s].setTriggered(false);
              sensors[s].setTimer(0);
            }else if(!sensors[s].getTriggered()){
              sensors[s].setTimer(0);
            }
          }
          break;
      }

    }
  }
}

bool Controller::update_sensor_values(byte* newValues, byte num){
  //the number of sensors doesn't match
  if(num != SENSOR_NUM)
    return false;

//  Serial.print("updating values");

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

  heldThreshold = atoi(val);
  val = strtok(NULL,",");
  
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

void Controller::setHeldThreshold(int held){ heldThreshold = held;}
int Controller::getHeldThreshold(){ return heldThreshold;}

