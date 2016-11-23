
#ifndef SensorData_H
#define SensorData_H
#include "Sensact.h"

// SensorDatum - Holds data from a single logical sensor
struct SensorDatum {  
  int sensorID;
  int sensorValue;
};

// SensorData - Used to accumulate and report data from all sensors.
class SensorData {
  private:
    struct SensorDatum *paData;
    int totalUnits;
    int dataIndex;
  public:
    SensorData() {}
    void init(int units) {
      paData = new SensorDatum[units];
      totalUnits = units;
    }
    
    void reset() {dataIndex = 0;}
    void addValue(int id, int val) {
      if(dataIndex < totalUnits) {
        paData[dataIndex].sensorID = id;
        paData[dataIndex].sensorValue = val;
        dataIndex++;
      }
    }
    
    int length() const { return dataIndex; }
    const SensorDatum* getValue(int i) const { return paData + i; }
  
};

// Sensor - Base class for all physical sensors.
/* A physical sensor will provide one or more logical sensors.
 * A physical sensor is something that must get all its values at once.
 * Each value is then reported to the rest of the program as an individual 
 * logical sensor.
 * e.g. The reading of a gyro (a physical sensor) will return x, y and z values.  
 * Each of these is returned as a separate logical sensor - with a unique ID
 * and an individual value.
 */
class Sensor {
  public:
    Sensor() {}
    virtual void init() = 0;

    // This call structure allows a single physical sensor 
    // to add multiple logical sensor values.
    virtual void getValues(SensorData *pData) = 0; 
    
    // Report the number of data units this sensor produces
    virtual int nDataUnits() = 0;
};

// AnalogSensor - A analog read from a single pin.
class AnalogSensor: public Sensor {
  protected:
    int id;
    int pinNumber;
  public:
    AnalogSensor(int i, int p) {
      id = i;
      pinNumber = p;
    }
    void init() {
      pinMode(pinNumber, INPUT);
    }
    void getValues(SensorData *pData) {
      int val = analogRead(pinNumber);
      pData->addValue(id, val);
    }
    int nDataUnits() { return 1; }
};

// GyroSensor - hypothetical gyro sensor.  Not fully implemented as yet.
// This is her just to illustrate how these classes may work.
class GyroSensor: public Sensor {
  private:
    int x_id;
    int y_id;
    int z_id;
  
  public:
    GyroSensor(int x, int y, int z) {
      x_id = x;
      y_id = y;
      z_id = z;
    }
    void init() { /* TBD */ }
    void getValues(SensorData *pData) {
      int xValue, yValue, zValue;
      // Get xValue, yValue, zValue ... somehow
      pData->addValue(x_id, xValue);
      pData->addValue(y_id, yValue);
      pData->addValue(z_id, zValue);
    }
    int nDataUnits() { return 3; }
};
  
// Sensors - a container for all sensors.
class Sensors {
  private:
    Sensor *paSensor[MAX_SENSORS];
    int nSensors;
    
    void addSensor(Sensor *s) {
      if (nSensors < MAX_SENSORS) {
        paSensor[nSensors++] = s;
      }
    }
  
  public:
    Sensors() { nSensors = 0; }
    void init();
    const SensorData* getData() const;
};

#endif


