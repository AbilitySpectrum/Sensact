#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BNO055.h>
#include <utility/imumaths.h>

/* Set the delay between fresh samples */
#define BNO055_SAMPLERATE_DELAY_MS (100)

#define GYRO_INIT_THRESH 1.2
#define GYRO_CANCEL_THRESH 0.75

Adafruit_BNO055 bno = Adafruit_BNO055();

byte gyroReadings[3];

/**************************************************************************/
/*
    Arduino setup function (automatically called at startup)
*/
/**************************************************************************/
void setup(void)
{
  Serial.begin(115200);

  /* Initialise the sensor */
  if(!bno.begin())
  {
    /* There was a problem detecting the BNO055 ... check your connections */
//    Serial.print("Ooops, no BNO055 detected ... Check your wiring or I2C ADDR!");
    while(1);
  }

  delay(1000);
  bno.setExtCrystalUse(true);

}

/**************************************************************************/
/*
    Arduino loop function, called once 'setup' is complete (your own code
    should go here)
*/
/**************************************************************************/
void loop(void)
{
  // Possible vector values can be:
  // - VECTOR_ACCELEROMETER - m/s^2
  // - VECTOR_MAGNETOMETER  - uT
  // - VECTOR_GYROSCOPE     - rad/s
  // - VECTOR_EULER         - degrees
  // - VECTOR_LINEARACCEL   - m/s^2
  // - VECTOR_GRAVITY       - m/s^2
//  imu::Vector<3> euler = bno.getVector(Adafruit_BNO055::VECTOR_EULER);
//
//  /* Display the floating point data */
//  Serial.print((int)(euler.x()*100/360));
//  Serial.print(",");
//  Serial.print((int)((euler.y()+90)*100/180));
//  Serial.print(",");
//  Serial.println((int)((euler.z()+180)*100/360));

  imu::Vector<3> gyr = bno.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);

  if(gyroReadings[0] < 50 && gyr.x() > GYRO_CANCEL_THRESH){
    gyroReadings[0] = 50;
  }else if(gyroReadings[0] > 50 && gyr.x() < -GYRO_CANCEL_THRESH){
    gyroReadings[0] = 50;
  }else{
    if(gyr.x() < -GYRO_INIT_THRESH)
      gyroReadings[0] = 0;
    else if(gyr.x() > GYRO_INIT_THRESH)
      gyroReadings[0] = 100;
  }

  if(gyroReadings[1] < 50 && gyr.y() > GYRO_CANCEL_THRESH){
    gyroReadings[1] = 50;
  }else if(gyroReadings[1] > 50 && gyr.y() < -GYRO_CANCEL_THRESH){
    gyroReadings[1] = 50;
  }else{
    if(gyr.y() < -GYRO_INIT_THRESH)
      gyroReadings[1] = 0;
    else if(gyr.y() > GYRO_INIT_THRESH)
      gyroReadings[1] = 100;
  }

  if(gyroReadings[2] < 50 && gyr.z() > GYRO_CANCEL_THRESH){
    gyroReadings[2] = 50;
  }else if(gyroReadings[2] > 50 && gyr.z() < -GYRO_CANCEL_THRESH){
    gyroReadings[2] = 50;
  }else{
    if(gyr.z() < -GYRO_INIT_THRESH)
      gyroReadings[2] = 0;
    else if(gyr.z() > GYRO_INIT_THRESH)
      gyroReadings[2] = 100;
  }
  
  Serial.print(gyroReadings[0]);
  Serial.print(",");
  Serial.print(gyroReadings[1]);
  Serial.print(",");
  Serial.println(gyroReadings[2]);

  delay(BNO055_SAMPLERATE_DELAY_MS);
}
