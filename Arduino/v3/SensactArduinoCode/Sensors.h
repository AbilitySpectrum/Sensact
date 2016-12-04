
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

class PCInputSensor: public Sensor {
  protected:
    int id;
    int nextCmd;
  public:
    PCInputSensor(int i) {
      id = i;
      nextCmd = 0;
    }
    
    void init() {}
    
    void getValues(SensorData *pData) {
        pData->addValue(id, nextCmd);
        nextCmd = 0;
    }
    
    void setNextCmd(int val) {
      nextCmd = val;
    }
    
    int nDataUnits() { return 1; }
}; 
   

// GyroSensor - first cut.  Very raw measurements.
class GyroSensor: public Sensor {
  private:
    int acclX;
    int acclY;
    int acclZ;
    int gyroX;
    int gyroY;
    int gyroZ;
  
  public:
    GyroSensor(int x, int y, int z, int gx, int gy, int gz) {
      acclX = x;
      acclY = y;
      acclZ = z;
      gyroX = gx;
      gyroY = gy;
      gyroZ = gz;
    }
    void init();
    void getValues(SensorData *pData);
    int nDataUnits() { return 6; }
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


