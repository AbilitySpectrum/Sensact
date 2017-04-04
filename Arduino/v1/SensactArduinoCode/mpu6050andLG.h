// LG code below

// 2016-10-28 Fri - LG code updated to check if reading has stopped
// if so, reset

// 2016-10-28 Fri - MPU6050 code has not been changed, will not work as is
// because it currently looks waiting for interrupt. This is based on
// Gyromouse_v2. Check out Gyromouse_v3 for improvements (in progress).

//* BEGIN MPU6050 code

#ifdef INCLUDE_MPU6050

// for MPU6050/GY521 ====================
// OpMode = 0 -> teapot
//            = 1 click function by movement distance
//            = 2 mouse function by accel
//            = 3 write out YPR values
// uncomment "OUTPUT_READABLE_QUATERNION" if you want to see the actual
// quaternion components in a [w, x, y, z] format (not best for parsing
// on a remote host such as Processing or something though)
//#define OUTPUT_READABLE_QUATERNION

// uncomment "OUTPUT_READABLE_EULER" if you want to see Euler angles
// (in degrees) calculated from the quaternions coming from the FIFO.
// Note that Euler angles suffer from gimbal lock (for more info, see
// http://en.wikipedia.org/wiki/Gimbal_lock)
//#define OUTPUT_READABLE_EULER

// uncomment "OUTPUT_READABLE_YAWPITCHROLL" if you want to see the yaw/
// pitch/roll angles (in degrees) calculated from the quaternions coming
// from the FIFO. Note this also requires gravity vector calculations.
// Also note that yaw/pitch/roll angles suffer from gimbal lock (for
// more info, see: http://en.wikipedia.org/wiki/Gimbal_lock)
//#define OUTPUT_READABLE_YAWPITCHROLL

// uncomment "OUTPUT_READABLE_REALACCEL" if you want to see acceleration
// components with gravity removed. This acceleration reference frame is
// not compensated for orientation, so +X is always +X according to the
// sensor, just without the effects of gravity. If you want acceleration
// compensated for orientation, us OUTPUT_READABLE_WORLDACCEL instead.
//#define OUTPUT_READABLE_REALACCEL

// uncomment "OUTPUT_READABLE_WORLDACCEL" if you want to see acceleration
// components with gravity removed and adjusted for the world frame of
// reference (yaw is relative to initial orientation, since no magnetometer
// is present in this case). Could be quite handy in some cases.
//#define OUTPUT_READABLE_WORLDACCEL

// uncomment "OUTPUT_TEAPOT" if you want output that matches the
// format used for the InvenSense teapot demo
#define OUTPUT_TEAPOT

int OpMode = 2;  //must not be in debug if OpMode==1


//*
//#include "Mouse.h"
#include "AACgyromouse.h"
#include "AACgyroclick.h"
#include "I2Cdev.h"
#include "MPU6050_6Axis_MotionApps20.h"
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
#include "Wire.h"
#endif
MPU6050 mpu;
// MPU control/status vars
bool dmpReady = false;  // set true if DMP init was successful
uint8_t mpuIntStatus;   // holds actual interrupt status byte from MPU
uint8_t devStatus;      // return status after each device operation (0 = success, !0 = error)
uint16_t packetSize;    // expected DMP packet size (default is 42 bytes)
uint16_t fifoCount;     // count of all bytes currently in FIFO
uint8_t fifoBuffer[64]; // FIFO storage buffer

// orientation/motion vars
Quaternion q;           // [w, x, y, z]         quaternion container
VectorInt16 aa;         // [x, y, z]            accel sensor measurements
VectorInt16 aaReal;     // [x, y, z]            gravity-free accel sensor measurements
VectorInt16 aaWorld;    // [x, y, z]            world-frame accel sensor measurements
VectorFloat gravity;    // [x, y, z]            gravity vector
float euler[3];         // [psi, theta, phi]    Euler angle container
float ypr[3];           // [yaw, pitch, roll]   yaw/pitch/roll container and gravity vector

// packet structure for InvenSense teapot demo
uint8_t teapotPacket[14] = {
  '$', 0x02, 0, 0, 0, 0, 0, 0, 0, 0, 0x00, 0x00, '\r', '\n'
};

// standalone 1
//GyroClick gClick;

// standalone 2
VectorInt16 gyro;
GyroMouse gMouse;
volatile bool mpuInterrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmpDataReady() {
  mpuInterrupt = true;
}
//*/


//========== MPU6050 ==============
void ylh_specialmousesetup() {
  gMouse.An0.setPin( SENSACT_IN2);
  gMouse.An1.setPin( SENSACT_IN3);
  gMouse.buzzer.setPin(SENSACT_BUZZER);

  gMouse.An0.setNormalHigh(1); //
  gMouse.An1.setNormalHigh(0);

}

void mpu_setup() {
  // join I2C bus (I2Cdev library doesn't do this automatically)
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
  Wire.begin();
  TWBR = 24; // 400kHz I2C clock (200kHz if CPU is 8MHz)
#elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
  Fastwire::setup(400, true);
#endif

  // initialize serial communication
  // (115200 chosen because it is required for Teapot Demo output, but it's
  // really up to you depending on your project)
  Serial.begin(57600); // 38400

  if ( OpMode == 0 || OpMode == 3 ) while (!Serial); // wait for Leonardo enumeration, others continue immediately

  // NOTE: 8MHz or slower host processors, like the Teensy @ 3.3v or Ardunio
  // Pro Mini running at 3.3v, cannot handle this baud rate reliably due to
  // the baud timing being too misaligned with processor ticks. You must use
  // 38400 or slower in these cases, or use some kind of external separate
  // crystal solution for the UART timer.

  // initialize device
  Serial.println(F("Initializing I2C devices..."));
  mpu.initialize();

  // verify connection
  Serial.println(F("Testing device connections..."));
  Serial.println(mpu.testConnection() ? F("MPU6050 connection successful") : F("MPU6050 connection failed"));


  // wait for ready
  if (OpMode == 0 && OpMode == 3) { //ylh: only when feeding data to Processing

    Serial.println(F("\nSend any character to begin DMP programming and demo: "));
    while (Serial.available() && Serial.read()); // empty buffer
    while (!Serial.available());                 // wait for data
    while (Serial.available() && Serial.read()); // empty buffer again
  }


  // load and configure the DMP
  Serial.println(F("Initializing DMP..."));
  devStatus = mpu.dmpInitialize();

  // supply your own gyro offsets here, scaled for min sensitivity
  //   mpu.setXAccelOffset(0);
  //   mpu.setYAccelOffset(0);
  //   mpu.setZAccelOffset(0); // 1688 factory default for my test chip
  //
  //   mpu.setXAccelOffset(-2484);
  //   mpu.setYAccelOffset(430);
  //   mpu.setZAccelOffset(1193);
  //   mpu.setXGyroOffset(27);
  //   mpu.setYGyroOffset(-44);
  //   mpu.setZGyroOffset(59);
  //-2703 238 1187  14  -16 84


  mpu.setXAccelOffset(-2739);
  mpu.setYAccelOffset(238);
  mpu.setZAccelOffset(1187);
  mpu.setXGyroOffset(14);
  mpu.setYGyroOffset(-16);
  mpu.setZGyroOffset(84);

  // make sure it worked (returns 0 if so)
  if (devStatus == 0) {
    // turn on the DMP, now that it's ready
    Serial.println(F("Enabling DMP..."));
    mpu.setDMPEnabled(true);

    // enable Arduino interrupt detection
    Serial.println(F("Enabling interrupt detection (Arduino external interrupt 0)..."));
    attachInterrupt(0, dmpDataReady, RISING);
    mpuIntStatus = mpu.getIntStatus();

    // set our DMP Ready flag so the main loop() function knows it's okay to use it
    Serial.println(F("DMP ready! Waiting for first interrupt..."));
    dmpReady = true;

    // get expected DMP packet size for later comparison
    packetSize = mpu.dmpGetFIFOPacketSize();
  }
  else {
    // ERROR!
    // 1 = initial memory load failed
    // 2 = DMP configuration updates failed
    // (if it's going to break, usually the code will be 1)
    Serial.print(F("DMP Initialization failed (code "));
    Serial.print(devStatus);
    Serial.println(F(")"));
  }
  if ( OpMode == 1 || OpMode == 2 ) {
    gMouse.mouseReset();
  }

  ylh_specialmousesetup();
}



void mpu_sensitivity() {
  gMouse.setSensitivity( (int) 50 ); /// ylh ******** fold into setprofile
  //   gClick.setSensitivity( (int) 50 );
}

void mpu_loop() {

  const float RADIANS_TO_DEGREES = 57.2958; //180/3.14159

  if ( OpMode == 1 || OpMode == 2 ) mpu_sensitivity();

  // if programming failed, don't try to do anything
  if (!dmpReady) return;

  // wait for MPU interrupt or extra packet(s) available
  while (!mpuInterrupt && fifoCount < packetSize) {
    // other program behavior stuff here
    // .
    // .
    // .
    // if you are really paranoid you can frequently test in between other
    // stuff to see if mpuInterrupt is true, and if so, "break;" from the
    // while() loop to immediately process the MPU data
    // .
    // .
    // .
  }

  // reset interrupt flag and get INT_STATUS byte
  mpuInterrupt = false;
  mpuIntStatus = mpu.getIntStatus();

  // get current FIFO count
  fifoCount = mpu.getFIFOCount();

  // check for overflow (this should never happen unless our code is too inefficient)
  if ((mpuIntStatus & 0x10) || fifoCount == 1024) {
    // reset so we can continue cleanly
    mpu.resetFIFO();

    // ylh
    //Serial.println(F("FIFO overflow!"));
    // otherwise, check for DMP data ready interrupt (this should happen frequently)
  }
  else if (mpuIntStatus & 0x02) {
    // wait for correct available data length, should be a VERY short wait
    while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount();

    // read a packet from FIFO
    mpu.getFIFOBytes(fifoBuffer, packetSize);

    // track FIFO count here in case there is > 1 packet available
    // (this lets us immediately read more without waiting for an interrupt)
    fifoCount -= packetSize;

#ifdef OUTPUT_READABLE_QUATERNION
    // display quaternion values in easy matrix form: w x y z
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    Serial.print("quat\t");
    Serial.print(q.w);
    Serial.print("\t");
    Serial.print(q.x);
    Serial.print("\t");
    Serial.print(q.y);
    Serial.print("\t");
    Serial.println(q.z);
#endif

#ifdef OUTPUT_READABLE_EULER
    // display Euler angles in degrees
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    mpu.dmpGetEuler(euler, &q);
    Serial.print("euler\t");
    Serial.print(euler[0] * 180 / M_PI);
    Serial.print("\t");
    Serial.print(euler[1] * 180 / M_PI);
    Serial.print("\t");
    Serial.println(euler[2] * 180 / M_PI);
#endif

#ifdef OUTPUT_READABLE_YAWPITCHROLL
    // display Euler angles in degrees
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    mpu.dmpGetGravity(&gravity, &q);
    mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
    Serial.print("ypr\t");
    Serial.print(ypr[0] * 180 / M_PI);
    Serial.print("\t");
    Serial.print(ypr[1] * 180 / M_PI);
    Serial.print("\t");
    Serial.println(ypr[2] * 180 / M_PI);
#endif

#ifdef OUTPUT_READABLE_REALACCEL
    // display real acceleration, adjusted to remove gravity
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    mpu.dmpGetAccel(&aa, fifoBuffer);
    mpu.dmpGetGravity(&gravity, &q);
    mpu.dmpGetLinearAccel(&aaReal, &aa, &gravity);
    Serial.print("areal\t");
    Serial.print(aaReal.x);
    Serial.print("\t");
    Serial.print(aaReal.y);
    Serial.print("\t");
    Serial.println(aaReal.z);
#endif

#ifdef OUTPUT_READABLE_WORLDACCEL
    // display initial world-frame acceleration, adjusted to remove gravity
    // and rotated based on known orientation from quaternion
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    mpu.dmpGetAccel(&aa, fifoBuffer);
    mpu.dmpGetGravity(&gravity, &q);
    mpu.dmpGetLinearAccel(&aaReal, &aa, &gravity);
    mpu.dmpGetLinearAccelInWorld(&aaWorld, &aaReal, &q);
    Serial.print("aworld\t");
    Serial.print(aaWorld.x);
    Serial.print("\t");
    Serial.print(aaWorld.y);
    Serial.print("\t");
    Serial.println(aaWorld.z);
#endif

#ifdef OUTPUT_TEAPOT
    // display quaternion values in InvenSense Teapot demo format:
    if ( OpMode == 0 ) {
      teapotPacket[2] = fifoBuffer[0];
      teapotPacket[3] = fifoBuffer[1];
      teapotPacket[4] = fifoBuffer[4];
      teapotPacket[5] = fifoBuffer[5];
      teapotPacket[6] = fifoBuffer[8];
      teapotPacket[7] = fifoBuffer[9];
      teapotPacket[8] = fifoBuffer[12];
      teapotPacket[9] = fifoBuffer[13];

      Serial.write(teapotPacket, 14);
    }
    else if ( OpMode == 1 ) {
      mpu.dmpGetQuaternion(&q, fifoBuffer);
      //         gClick.loop(q);
    }
    else if ( OpMode == 2 ) {
      // display real acceleration, adjusted to remove gravity

      //         mpu.dmpGetQuaternion(&q, fifoBuffer);
      //          mpu.dmpGetAccel(&aa, fifoBuffer);
      //          mpu.dmpGetGravity(&gravity, &q);
      //          mpu.dmpGetLinearAccel(&aaReal, &aa, &gravity);
      //          mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
      //          mpu.dmpGetEuler(euler, &q);


      mpu.dmpGetGyro((int16_t *) &gyro, (uint8_t *) fifoBuffer);

      gMouse.loop( gyro );
    }
    else if ( OpMode == 3 ) {
      mpu.dmpGetQuaternion(&q, fifoBuffer);
      mpu.dmpGetGravity(&gravity, &q);
      mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
      Serial.print("DMP:");
      Serial.print(ypr[0]*RADIANS_TO_DEGREES, 2);
      Serial.print(":");
      Serial.print(ypr[2]*RADIANS_TO_DEGREES, 2);
      Serial.print(":");
      Serial.println(-ypr[1]*RADIANS_TO_DEGREES, 2);
    }

    teapotPacket[11]++; // packetCount, loops at 0xFF on purpose
#endif

  }

}
// END MPU6050 */
#endif


#ifdef INCLUDE_LGGYRO
#include <L3G.h>
L3G my_gyro;
const int vccPIN = 0;  // ************** 

// ylh - WATCH the init()

void gyro_reset() {
//  digitalWrite(SENSACT_I2C_VCC, LOW);
//  delay(10);
//  digitalWrite(SENSACT_I2C_VCC, HIGH);
  Wire.begin();
  while (!my_gyro.init())
  {
    Serial.println("Failed to autodetect gyro type!");
    delay(100);
    //while (1);
  }
  my_gyro.setTimeout(100);
  my_gyro.enableDefault();
}

void gyro_setup() {

  if ( USE_GYRO == 0 ) return;

  Wire.begin();
//
//  pinMode(SENSACT_I2C_VCC, OUTPUT);
//  digitalWrite(SENSACT_I2C_VCC, HIGH);
  gyro_reset();
}

byte gyro_read () {

  if ( USE_GYRO == 0 ) return 0 ;

  my_gyro.read();
//  Serial.print(".");

  if (my_gyro.timeoutOccurred() ) {
    Serial.println("TIMEOUT!!");
    gyro_reset();
  }
  long x = my_gyro.g.x;
  long y = my_gyro.g.y;
  long z = my_gyro.g.z;
  return ( (byte) constrain( map(sqrt( x * x + y * y + z * z ), 0, 45000, 0, 100 ),
                             0, 100) );
}

#endif


