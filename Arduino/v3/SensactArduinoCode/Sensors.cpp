// --------------------------------------
// Sensors.cpp
// --------------------------------------

#include "Sensact.h"
#include "Sensors.h"

SensorData data;
Sensors sensors;
PCInputSensor *pcInput;

void Sensors::init() {
  // This code defines the set of sensors.
  // It must match the sensor definitions in the JavaScript control code.
  // ID values must be unique but do not _have_ to be sequential
  addSensor( new AnalogSensor(1, SENSACT_IN2) );
  addSensor( new AnalogSensor(2, SENSACT_IN3) );
  pcInput = new PCInputSensor(3);
  addSensor( pcInput );
  
  int dataUnits = 0;
  for(int i=0; i<nSensors; i++) {
    paSensor[i]->init();
    dataUnits += paSensor[i]->nDataUnits();
  }
  
  data.init(dataUnits);
}

const SensorData* Sensors::getData() const {
  int i;
  data.reset();
  for(i=0; i<nSensors; i++) {
    paSensor[i]->getValues(&data);
  }
  return &data;
}  
