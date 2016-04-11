/* Sensact_v3 : 20151106 : 20151026 : ylh
 
 20160407 : now being modified for Sensact board v2, check defines
 
 version in ~/0/1/Sensact
 - p5 code in configSensact
 - Arduino code in Sensact
 
 ==
 20151026 version works with v1 of Sensact board using the Pro Micro
 
 modified from test_Sensact_v2 to interact with 
 p5 version of configSensact (~/0/p5/configSensact)
 
 p5 -> Sensact
 "8," - config mode, 
 a) sends config info to p5, one time begin with "9999,...."
 b) receive config info from p5, any number of times
 "0,...."
 c) return input readings to p5, every x msec
 
 "9" - Senseact goes into Run mode
 a) no output to p5
 b) act according to config info
 
 "$", "-", "#', '@' - special mode: see code below
 
 NB - first 3 inputs are analog signals (use map_in[] to map), 
 4th input is I2C which requires a different scaling (currently specific to L3G20 
 gyroscope
 
 */

const int RESET_EEPROM = 0;
const int USE_GYRO = 0;
const int USE_BT = 1;

#include <Keyboard.h>
#include <Wire.h>

// for MPU6050/GY521 ====================
// OpMode = 0 -> teapot
//            = 1 click function by movement distance
//            = 2 mouse function by accel
//            = 3 write out YPR values
int OpMode = 2;  //must not be in debug if OpMode==1

#include "pitches.h"
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
   '$', 0x02, 0,0, 0,0, 0,0, 0,0, 0x00, 0x00, '\r', '\n' };

// standalone 1
GyroClick gClick;

// standalone 2
VectorInt16 gyro;
GyroMouse gMouse;
volatile bool mpuInterrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmpDataReady() {
   mpuInterrupt = true;
}

//========== MPU6050 ==============

#include <L3G.h>
L3G my_gyro;


#include <SoftwareSerial.h>  
#include <EEPROM.h>
//#include "AACswitch.h"
#include "sensact.h"

// constants won't change. Used here to 
// set pin numbers:
int ledPin = SENSACT_RED;      // the number of the LED pin

// Variables will change:
int ledState = LOW;             // ledState used to set the LED
long previousMillis = 0;        // will store last time LED was updated
long previousMillis0 = 0;

// the follow variables is a long because the time, measured in miliseconds,
// will quickly become a bigger number than can be stored in an int.
//long interval = 400; 
long interval0 = 200;           // interval at which to blink (milliseconds)

long report_interval = 200;
long beep_interval = 20;
long read_interval = 400; // refractory for signals
long currentMillis = millis();


int whichSerial = 0; // 0 for Serial; 1 for bluetooth

int state = SENSACT_RUN;  
byte config[300];

long lastRead[nInputs];
long whenOn[nOutputs], control[nOutputs] = { 
   0, RELAY_A, RELAY_B, 0,0, SENSACT_RED, SENSACT_BUZZER };
long onOff[nOutputs];

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup() {

   beep(1);
   Serial.begin(9600);  // Begin the serial monitor at 9600bps

   if ( RESET_EEPROM) {
      for( int i=0; i<nInputs*(nOutputs + nValues); i++) {
         EEPROM.write( i, 0 );    
         delay(10);          
      }
   }
   // set the digital pin as output:
   //pinMode(ledPin, OUTPUT);

   pinMode(SENSACT_IN2, INPUT);
   pinMode(SENSACT_IN3, INPUT);
#ifdef SENSACT_V1
   pinMode(SENSACT_IN1, INPUT);
   pinMode(SENSACT_IN4, INPUT);
#endif
#ifdef SENSACT_V2
   pinMode(SENSACT_IN1A, INPUT);
   pinMode(SENSACT_IN1B, INPUT);
#endif

   pinMode(SENSACT_RED, OUTPUT);
   pinMode(SENSACT_GREEN, OUTPUT);
   pinMode(SENSACT_BLUE, OUTPUT);
   pinMode(SENSACT_BUZZER, OUTPUT);

   pinMode(RELAY_A, OUTPUT);
   pinMode(RELAY_B, OUTPUT);

   pinMode(SENSACT_OUT1, OUTPUT);
   pinMode(SENSACT_OUT2, OUTPUT);
#ifdef SENSACT_V1
   pinMode(SENSACT_OUT3, OUTPUT);
#endif

   digitalWrite( ledPin, HIGH);

   for( int i=0; i<nInputs; i++ ) lastRead[i] = 0;
   for( int j=0; j<nOutputs; j++) {
      if( control[j] == SENSACT_BUZZER) 
         noTone(SENSACT_BUZZER);
      else
         digitalWrite( control[j], LOW );
      whenOn[j] = 0;
      onOff[j] = 0;
   }

   delay(1000);

   //Serial.println("1");

   bt_setup();
   //Serial.println("2");
   gyro_setup();
   //Serial.println("3");
   mpu_setup();

   load_config();

   beep(2);
}

void loop()
{
   currentMillis = millis();

   serial_loop();
   led_loop();

   //Serial.println("loop");

   for( int j=0; j<nOutputs; j++ ) {
      if( (currentMillis - whenOn[j]) > beep_interval ) {
         if( control[j] == SENSACT_BUZZER )
            ;  //noTone(SENSACT_BUZZER);
         else     
            digitalWrite( control[j], LOW );
         onOff[j] = 0;
      }
   }

   bt_loop();

   switch( state ) {
   case SENSACT_CONFIG:
      report_signals();
      break;
   case SENSACT_RUN:
      process_signals();
      break;
   default:
      break;
   }

   if( (currentMillis - previousMillis0) > interval0) {
      // save the last time you blinked the LED 
      previousMillis0 = currentMillis;      
      // if the LED is off turn it on and vice-versa:
      if (ledState == LOW)
         ledState = HIGH;
      else
         ledState = LOW;
      /* hard-coded test
       if( analogRead( SENSACT_IN4 ) > 500 ) {
       previousMillis0 += interval0;
       bluetooth.write( 'j' );
       }
       */
   }
}

void bt_setup()
{
   if( USE_BT == 0 ) return;

   delay(1000);

   bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps

   bluetooth.print("$");  // Print three times individually
   bluetooth.print("$");
   bluetooth.print("$");  // Enter command mode
   delay(100);  // Short delay, wait for the Mate to send back CMD
   bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
   // 115200 can be too fast at times for NewSoftSerial to relay the data reliably

   bluetooth.begin(9600);  // Start bluetooth serial at 9600
   //*/
}

void bt_loop()
{
   if( USE_BT == 0 ) return;
   if(bluetooth.available())  // If the bluetooth sent any characters
   {
      // Send any characters the bluetooth prints to the serial monitor
      Serial.print((char)bluetooth.read());  
   }
}

// led_state RED=config; GREEN=run; BLUE=unknown
int ledInterval = 800;
long ledTime = 0;

void led_loop() {
   digitalWrite(ledPin, LOW);

   int sum=0;
   for(int i=0; i<nOutputs; i++) sum += onOff[i];

   if( sum > 0 || (currentMillis - ledTime ) < ledInterval ) { // over write if there is output going on
      ledPin = SENSACT_RED;
   } 
   else {
      switch( state ) {
      case SENSACT_CONFIG: 
         ledPin = SENSACT_BLUE; 
         break;
      case SENSACT_RUN: 
         ledPin = SENSACT_GREEN; 
         break;
      case SENSACT_UNKNOWN:
      default:
         ledPin = SENSACT_RED; 
         break;
      }
   }
   digitalWrite(ledPin, HIGH);

}

char inString [300];
int charpos = 0;
void serial_loop() {
   int inChar;
   // Read serial input:
   while (Serial.available() > 0 ) { // || bluetooth.available() > 0) {
      if( Serial.available() > 0 ) {
         whichSerial = 0;
         inChar = Serial.read();       
      }
      /*
      else {
       whichSerial = 1;
       inChar = bluetooth.read();
       }
       */
      if (inChar == '\n') {
         inString[charpos] = 0;
         process_serial();  //process a whole line
         // clear the string for new input:
         charpos = 0;
         break;
      }
      inString[charpos++] = (char)inChar;
   }
}

void process_serial() {
   const char s[2] = ",";
   char *token;

   int j=0;

   /* this is tricky AND only for USB serial - if first character is
    '$' - send $$$ to BT to enter command mode
    '-' - send "---\n" to BT to exti command mode
    '@' - send the rest of the line plus '\n'
    '#' - send the rest of the line
    else - it's meant for Sensact to process
    */
   if( whichSerial == 0 ) {
      switch (inString[0]) {
      case '$':
         bluetooth.print("$");  // Print three times individually
         bluetooth.print("$");
         bluetooth.print("$");  // Enter command mode
         return;
         break;
      case '-':
         bluetooth.print("---\n");
         return;
         break;
      case '@': 
         bluetooth.println(inString+1);
         return;
         break;
      case '#': 
         bluetooth.print(inString+1);
         return;
         break;
      case '%': 
         bluetooth.begin(9600);
         return;
      case '^': 
         bluetooth.begin(115200);
         return;
         break;
      default:
         break;
      }
   }

   /* get the first token - if not 0,8,9 then abort the rest*/
   token = strtok(inString, s);

   switch(atoi(token)) {
   case 0:  // store in EPROM
      token = strtok(NULL, s);
      while( token != NULL ) 
      {
         config[j++]= atoi(token);
         token = strtok(NULL, s);
      }
      for( int i=0; i<j; i++) {
         EEPROM.write( i, config[i] );    
         delay(10);          
      }

      state = SENSACT_CONFIG;
      break;
   case 8:  // report sensact config from EEPROM
      if(whichSerial == 0 ) { /// ylh kludge
         Serial.print("9999,");
         for( int i=0; i<nInputs*(nOutputs + nValues); i++) {
            config[i] = EEPROM.read(i);
            Serial.print(config[i]);   
            Serial.print(",");      
         }
         Serial.println();
      } 
      else {
         bluetooth.print("9999,");
         for( int i=0; i<nInputs*(nOutputs + nValues); i++) {
            config[i] = EEPROM.read(i);
            bluetooth.print(config[i]);   
            bluetooth.print(",");      
         }
         bluetooth.println();
      }
      state = SENSACT_CONFIG;
      break;
   case 9:
      state = SENSACT_RUN;
      break;
   default:
      state = SENSACT_UNKNOWN;
      break;
   }
}
void load_config() {
   //if(EEPROM.read(0) <=1) { // sanity check
   for( int i=0; i<(nInputs*(nOutputs+nValues)); i++) {
      config[i] = EEPROM.read(i);
   }
   //}
}

void report_signals() {
   if( (currentMillis - previousMillis) < report_interval) return;
   // save the last time you blinked the LED  
   previousMillis = currentMillis;

   //if( whichSerial == 0 ) { //ylh kludge
   if( USE_GYRO == 0 ) {

#ifdef SENSACT_V1
      Serial.print( SENSACT_IN1 ); //Serial.print(gyro_read());
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN2 ), 0, 1023, 0, 100));
      Serial.print(",");      
      Serial.print(map(analogRead( SENSACT_IN3 ), 0, 1023, 0, 100 ));
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN4 ), 0, 1023, 0, 100 ));
#endif
#ifdef SENSACT_V2
      Serial.print(map(analogRead( SENSACT_IN1A ), 0, 1023, 0, 100));
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN1B ), 0, 1023, 0, 100));
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN2 ), 0, 1023, 0, 100));
      Serial.print(",");      
      Serial.print(map(analogRead( SENSACT_IN3 ), 0, 1023, 0, 100 ));
#endif
      Serial.print("\n");

   } 
   else { // use gyro
      Serial.print(gyro_read());
      Serial.print(",");
      Serial.print(map( my_gyro.g.x, -31000, 31000, 0, 100));
      Serial.print(",");      
      Serial.print(map( my_gyro.g.y, -31000, 31000, 0, 100 ));
      Serial.print(",");
      Serial.print(map( my_gyro.g.z , -31000, 31000, 0, 100 ));
      Serial.print("\n");

   }
   /*} 
    else {      
    bluetooth.print(map(analogRead( SENSACT_IN2 ), 0, 1023, 0, 100));
    bluetooth.print(",");      
    bluetooth.print(map(analogRead( SENSACT_IN3 ), 0, 1023, 0, 100 ));
    bluetooth.print(",");
    bluetooth.println(map(analogRead( SENSACT_IN4 ), 0, 1023, 0, 100 ));
    }  
    */
}

void process_signals() {
   byte val;

   for(int i=0; i<nInputs; i++ ) {
      if( (currentMillis - lastRead[i]) < read_interval ) continue;
      lastRead[i] = currentMillis;

      int offset = i*nOutputs;
      int offset2 = i*nValues + nInputs*nOutputs;

      if( USE_GYRO == 0 ) {
         switch(i) {
#ifdef SENSACT_V1
         case 0: 
            val = (byte) gyro_read();
            break;
         default:
            val = (byte) map(analogRead( map_in[i-1] ), 0, 1023, 0, 100);
            break;
#endif
#ifdef SENSACT_V2
         default:
            val = (byte) map(analogRead( map_in[i] ), 0, 1023, 0, 100);
            break;
#endif
         }
      } 
      else {
         switch (i) {
         case 0: 
            val = (byte) gyro_read();
            break;
         case 1:
            val = (byte) map(my_gyro.g.x, -31000, 31000, 0, 100);
            break;
         case 2:
            val = (byte) map(my_gyro.g.y, -31000, 31000, 0, 100);
            break;
         case 3:
            val = (byte) map(my_gyro.g.z, -31000, 31000, 0, 100);
            break;
         }
      }

      if( config[ offset  + INVERT]==1 )
      { 
         val = 100 - val;
      }
      if( val > config[ offset2 + THRESHOLD_VAL] ) {
         for( int j=0; j<nOutputs; j++ ) {
            switch( j ) {
            case BLUETOOTH:   
               if( config[offset+BLUETOOTH] && onOff[j]==0 ) {
                  bluetooth.write( config[offset2 + BT_HID_VAL]);
                  ledTime = whenOn[j] = currentMillis;
                  onOff[j] ++;
               }
               break;
            case USB_HID:   
               if( config[offset+USB_HID]  && onOff[j]==0 ) {
                  Keyboard.write( config[offset2 + USB_HID_VAL]);
                  ledTime = whenOn[j] = currentMillis;
                  onOff[j] ++;
               }
               break;
            case BUZZER:
               if( config[offset+j] ) {
                  tone(control[j], NOTE_C7, 250); //digitalWrite(control[j], HIGH);
                  ledTime = whenOn[j] = currentMillis;
                  onOff[j] ++;
               }
               break;
               // the following cases have the same behaviour
            case RELAY_A:   
            case RELAY_B:  
            case LED: 
               if( config[offset+j] ) {
                  digitalWrite(control[j], HIGH);
                  ledTime = whenOn[j] = currentMillis;
                  onOff[j] ++;
               }
               break;
            }
         }
      }
   }
}

/* 4th input 
 
 ylh - WATCH the init()
 
 */

void gyro_setup() {

   if( USE_GYRO == 0 ) return;


   Serial.print("g1");
   Wire.begin();

   Serial.print("g2");
   if (!my_gyro.init())
   {
      Serial.println("Failed to autodetect gyro type!");
      while (1);
   }
   Serial.print("g3");
   my_gyro.enableDefault();
   Serial.print("g4");
}

byte gyro_read () {

   if( USE_GYRO == 0 ) return 0 ;

   my_gyro.read();

   long x = my_gyro.g.x;
   long y = my_gyro.g.y;
   long z = my_gyro.g.z;
   return ( (byte) constrain( map(sqrt( x*x + y*y + z*z ), 0, 45000, 0, 100 ),
   0, 100) );
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

      if( OpMode==0 || OpMode==3 ) while (!Serial); // wait for Leonardo enumeration, others continue immediately

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
   if (OpMode==0 && OpMode==3) { //ylh: only when feeding data to Processing

      Serial.println(F("\nSend any character to begin DMP programming and demo: "));
      while (Serial.available() && Serial.read()); // empty buffer
      while (!Serial.available());                 // wait for data
      while (Serial.available() && Serial.read()); // empty buffer again
   } 


   // load and configure the DMP
   Serial.println(F("Initializing DMP..."));
   devStatus = mpu.dmpInitialize();

   // supply your own gyro offsets here, scaled for min sensitivity
   mpu.setXAccelOffset(0);
   mpu.setYAccelOffset(0);
   mpu.setZAccelOffset(0); // 1688 factory default for my test chip

   mpu.setXAccelOffset(-2484);
   mpu.setYAccelOffset(430);
   mpu.setZAccelOffset(1193);
   mpu.setXGyroOffset(27);
   mpu.setYGyroOffset(-44);
   mpu.setZGyroOffset(59);


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
   if( OpMode ==1 || OpMode==2 ) {
      gMouse.mouseReset();
   }
}

void mpu_sensitivity() { 
   gMouse.setSensitivity( (int) 50 ); /// ylh ******** fold into setprofile
   gClick.setSensitivity( (int) 50 );
}    

void mpu_loop() {

   const float RADIANS_TO_DEGREES = 57.2958; //180/3.14159

   if( OpMode ==1 || OpMode==2 ) mpu_sensitivity();

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
      Serial.print(euler[0] * 180/M_PI);
      Serial.print("\t");
      Serial.print(euler[1] * 180/M_PI);
      Serial.print("\t");
      Serial.println(euler[2] * 180/M_PI);
#endif

#ifdef OUTPUT_READABLE_YAWPITCHROLL
      // display Euler angles in degrees
      mpu.dmpGetQuaternion(&q, fifoBuffer);
      mpu.dmpGetGravity(&gravity, &q);
      mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
      Serial.print("ypr\t");
      Serial.print(ypr[0] * 180/M_PI);
      Serial.print("\t");
      Serial.print(ypr[1] * 180/M_PI);
      Serial.print("\t");
      Serial.println(ypr[2] * 180/M_PI);
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
      if( OpMode == 0 ){
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
      else if( OpMode == 1 ) {
         mpu.dmpGetQuaternion(&q, fifoBuffer);
         gClick.loop(q);
      } 
      else if( OpMode == 2 ){
         // display real acceleration, adjusted to remove gravity
         /*
         mpu.dmpGetQuaternion(&q, fifoBuffer);
          mpu.dmpGetAccel(&aa, fifoBuffer);
          mpu.dmpGetGravity(&gravity, &q);
          mpu.dmpGetLinearAccel(&aaReal, &aa, &gravity);
          mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
          mpu.dmpGetEuler(euler, &q);
          */

         mpu.dmpGetGyro((int16_t *) &gyro, (uint8_t *) fifoBuffer);

         gMouse.loop( gyro );
      }      
      else if( OpMode == 3 ){
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


void beep(int j) {
   if (j<1) return;
   for( int i=0; i<j; i++ ) {
      tone(SENSACT_BUZZER, NOTE_C7, 250);
      delay(50);
      noTone(SENSACT_BUZZER);      
      delay(150);

      /*
      digitalWrite( SENSACT_BUZZER, HIGH );
       delay(10);
       digitalWrite( SENSACT_BUZZER, LOW );
       delay(150);
       */
   }
}

























































































