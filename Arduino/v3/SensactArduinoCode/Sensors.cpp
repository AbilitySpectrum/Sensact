// --------------------------------------
// Sensors.cpp
// --------------------------------------

#include "Sensact.h"
#include "Sensors.h"
#include "Wire.h"

SensorData data;
Sensors sensors;
PCInputSensor *pcInput;

void Sensors::init() {
  // This code defines the set of sensors.
  // It must match the sensor definitions in the JavaScript control code.
  // ID values must be unique but do not _have_ to be sequential
#ifdef VERSION_3_HW
  addSensor( new AnalogSensor(1, SENSACT_IN1A) );
  addSensor( new AnalogSensor(2, SENSACT_IN1B) );
  addSensor( new AnalogSensor(3, SENSACT_IN2A) );
  addSensor( new AnalogSensor(4, SENSACT_IN2B) );
  addSensor( new AnalogSensor(5, SENSACT_IN3A) );
  addSensor( new AnalogSensor(6, SENSACT_IN3B) );
  pcInput = new PCInputSensor(7);
  addSensor( pcInput );
  addSensor( new GyroSensor(8, 9, 10, 11, 12, 13) );

#elif VERSION_2_HW
  addSensor( new AnalogSensor(1, SENSACT_IN2) );
  addSensor( new AnalogSensor(2, SENSACT_IN3) );
  pcInput = new PCInputSensor(3);
  addSensor( pcInput );
  addSensor( new GyroSensor(4, 5, 6, 7, 8, 9) );
#endif

  int dataUnits = 0;
  for(int i=0; i<nSensors; i++) {
    paSensor[i]->init();
    dataUnits += paSensor[i]->nDataUnits();
  }
  
  data.init(dataUnits);
}

void Sensors::reset() {
  for(int i=0; i<nSensors; i++) {
    paSensor[i]->reset();
  }  
}

const SensorData* Sensors::getData() const {
  int i;
  data.reset();
  for(i=0; i<nSensors; i++) {
    paSensor[i]->getValues(&data);
  }
  return &data;
}  

const int MPU_addr=0x68;  // I2C address of the MPU-6050
void GyroSensor::init() {
  Wire.begin();
  Wire.beginTransmission(MPU_addr);
  Wire.write(0x6B);  // PWR_MGMT_1 register
  Wire.write(0);     // set to zero (wakes up the MPU-6050)
  Wire.endTransmission(true);
}

void GyroSensor::getValues(SensorData *pData) {

    Wire.beginTransmission(MPU_addr);
    Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
    Wire.endTransmission(false);
    Wire.requestFrom(MPU_addr,14,true);  // request a total of 14 registers
    int AcX=Wire.read()<<8|Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)     
    int AcY=Wire.read()<<8|Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
    int AcZ=Wire.read()<<8|Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
    int Tmp=Wire.read()<<8|Wire.read();  // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
    int GyX=Wire.read()<<8|Wire.read();  // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
    int GyY=Wire.read()<<8|Wire.read();  // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
    int GyZ=Wire.read()<<8|Wire.read();  // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)

  pData->addValue(acclX, AcX);
  pData->addValue(acclY, AcY);
  pData->addValue(acclZ, AcZ);
  
  pData->addValue(gyroX, GyX);
  pData->addValue(gyroY, GyY);
  pData->addValue(gyroZ, GyZ);
}
