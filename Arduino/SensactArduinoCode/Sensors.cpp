// --------------------------------------
// Sensors.cpp
// --------------------------------------
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * 
    This file is part of the Sensact Arduino software.

    Sensact Arduino software is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sensact Arduino software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this Sensact Arduino software.  
    If not, see <https://www.gnu.org/licenses/>.   
 * * * * * * * * * * * * * * * * * * * * * * * * * * * */ 
 
#include "Sensact.h"
#include "Sensors.h"
#include "Wire.h"

SensorData data;
Sensors sensors;
PCInputSensor *pcInput;

void Sensors::init() {
  // This code defines the set of sensors.
  // It must match the sensor definitions in the Java control code.
  // ID values must be unique but do not _have_ to be sequential
  addSensor( new AnalogSensor(1, SENSACT_IN1A) );
  addSensor( new AnalogSensor(2, SENSACT_IN1B) );
  addSensor( new AnalogSensor(3, SENSACT_IN2A) );
  addSensor( new AnalogSensor(4, SENSACT_IN2B) );
  addSensor( new AnalogSensor(5, SENSACT_IN3A) );
  addSensor( new AnalogSensor(6, SENSACT_IN3B) );
  pcInput = new PCInputSensor(7);
  addSensor( pcInput );
  addSensor( new GyroSensor(8, 9, 10, 11, 12, 13, 14) );
  addSensor( new ADS_1015(0x48, 15, 16, 17, 18) );
  addSensor( new ADS_1015(0x49, 19, 20, 21, 22) );
#ifdef MEMCHECK
  BreakPoints.sensorsAlloc = (int) __brkval;
#endif  
  int dataUnits = 0;
  for(int i=0; i<nSensors; i++) {
    paSensor[i]->init();
    dataUnits += paSensor[i]->nDataUnits();
  }
  
  data.init(dataUnits);
#ifdef MEMCHECK
  BreakPoints.sensorsInit = (int) __brkval;
#endif
}

void Sensors::reset() {
  for(int i=0; i<nSensors; i++) {
    paSensor[i]->reset();
  }  
}

int Sensors::getHighestID() const {
  int maxSensorID = 0;
  
  const SensorData *pData = getData();
  int dataCount = pData->length();

  for(int i=0; i<dataCount; i++) {
    int ID = pData->getValue(i)->sensorID;
    if (ID > maxSensorID) {
      maxSensorID = ID;
    }
  }
  return maxSensorID;
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
  if (Wire.endTransmission(true) == 0) {
    initNeeded = false;
  } else {
    // init failed - probably device is not connected.
    timeOfLastInitAttempt = millis() & 0xffff;
    initNeeded = true;
  }
}

void GyroSensor::getValues(SensorData *pData) {

  boolean skipRead = false;
  int AcX, AcY, AcZ, GyX, GyY, GyZ, Tmp;
  float anyMotion;
  
  AcX = AcY = AcZ = GyX = GyY = GyZ = 0; // Default values if read is skipped.
    
  if (initNeeded) {
    // Retry init every 2 seconds
    int now = millis() & 0xffff;
    if (timeDiff(now, timeOfLastInitAttempt) < 2000) {
      skipRead = true;  // Skip read - initNeeded but not yet time for retry.
    } else {
      init();
    }
    if (initNeeded) { 
      skipRead = true;  // Skip read.  Init failed.
    }
   }

  if (!skipRead) {
    Wire.beginTransmission(MPU_addr);
    Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
    if (Wire.endTransmission(false) != 0) {
      // Device may have been disconnected
      initNeeded = true;
      timeOfLastInitAttempt = millis() & 0xffff;
      skipRead = true;  // Init needed again.
    }
  }

  if (!skipRead) {
    Wire.requestFrom(MPU_addr,14,true);  // request a total of 14 registers
    AcX=Wire.read()<<8|Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)     
    AcY=Wire.read()<<8|Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
    AcZ=Wire.read()<<8|Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
    Tmp=Wire.read()<<8|Wire.read();  // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
    GyX=Wire.read()<<8|Wire.read();  // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
    GyY=Wire.read()<<8|Wire.read();  // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
    GyZ=Wire.read()<<8|Wire.read();  // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)
  }

  pData->addValue(acclX, AcX);
  pData->addValue(acclY, AcY);
  pData->addValue(acclZ, AcZ);

  // Gyro values:
  //  Small motions generate values of +/- ~1,500
  //  Larger motions can generate +/- ~15,000
  //  Max values are -32768 to +32767
  pData->addValue(gyroX, GyX);
  pData->addValue(gyroY, GyY);
  pData->addValue(gyroZ, GyZ);

  anyMotion = (float) GyX * (float) GyX;
  anyMotion += (float) GyY * (float) GyY;
  anyMotion += (float) GyZ * (float) GyZ;
  anyMotion = sqrt(anyMotion) / 2; 
  // Max value for anyMotion observed at this point is 28,377.92
  // which is about sqrt(32676 ^ 2 * 3) / 2
  pData->addValue(gyroAny, (int)anyMotion);
}

void ADS_1015::init() {
  device = new Adafruit_ADS1015(address);
}

void ADS_1015::getValues(SensorData *pData) {
  pData->addValue(id1, device->readADC_SingleEnded(0));
  pData->addValue(id2, device->readADC_SingleEnded(1));
  pData->addValue(id3, device->readADC_SingleEnded(2));
  pData->addValue(id4, device->readADC_SingleEnded(3));
}


