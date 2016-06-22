#include "Arduino.h"
#include "Controller.h"

void Controller::process_triggers(){
  for(byte s = 0; s < SENSOR_NUM; s++){
    for(byte t = 0; t< TRIG_NUM; t++){
      if(sensors[s].getResponse(t) == 0) //don't bother processing events if there is no response
        continue;

      switch(sensors[s].getEvent(t)){
        case 0: //Rising edge
          if(oldValues[s] < sensors[s].getLevel(t) && currentValues[s] > sensors[s].getLevel(t) && !sensors[s].getTriggered(t)){
            process_response(sensors[s].getResponse(t),sensors[s].getDetail(t));
            sensors[s].setTriggered(t,false);
          }
          break;
        
        case 1://Falling Edge
          if(oldValues[s] > sensors[s].getLevel(t) && currentValues[s] < sensors[s].getLevel(t) && !sensors[s].getTriggered(t)){
            process_response(sensors[s].getResponse(t),sensors[s].getDetail(t));
            sensors[s].setTriggered(t,false);
          }
          break;
        
        case 2: //Above Level
          if(currentValues[s] > sensors[s].getLevel(t))
            process_response(sensors[s].getResponse(t),sensors[s].getDetail(t));
          break;
        
        case 3: //Below Level
          if(currentValues[s] < sensors[s].getLevel(t))
            process_response(sensors[s].getResponse(t),sensors[s].getDetail(t));
          break;
        
        case 4: //held above
          if(currentValues[s] > sensors[s].getLevel(t)){
            sensors[s].incrementCounter(t);
            
            //loops here and increments the counter every time. if the sensor is held above the trigger level for long enough, the response will occur
            if(sensors[s].getCounter(t) >= 250){ 
              process_response(sensors[s].getResponse(t),sensors[s].getDetail(t));
              sensors[s].resetCounter(t);
              sensors[s].setTriggered(t,true);
            }
          }
          break;
        
        case 5: //held below
          if(currentValues[s] < sensors[s].getLevel(t)){
            sensors[s].incrementCounter(t);

            if(sensors[s].getCounter(t) >= 250){ 
              process_response(sensors[s].getResponse(t),sensors[s].getDetail(t));
              sensors[s].resetCounter(t);
              sensors[s].setTriggered(t,true);
            }
          }
          break;
      }

    }
  }
}

void Controller::process_response(byte r, byte d){
  
}

