/* Sensact_v3 : 20151106 : 20151026 : ylh

This version works with v1 of Sensact board using the Pro Micro

 modified from test_Sensact_v2 to interact with 
 p5 version of configSensact (~/0/p5/configSensact)
 
 p5 -> Sensact
 "8," - config mode, 
 a) sends config info to p5, one time begin with "9999,...."
 b) receive config info from p5, any number of times
 "0,...."
 c) send input readings to p5, every x msec
 
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
   pinMode(SENSACT_IN1, INPUT);
   pinMode(SENSACT_IN2, INPUT);
   pinMode(SENSACT_IN3, INPUT);
   pinMode(SENSACT_IN4, INPUT);

   pinMode(SENSACT_RED, OUTPUT);
   pinMode(SENSACT_GREEN, OUTPUT);
   pinMode(SENSACT_BLUE, OUTPUT);
   pinMode(SENSACT_BUZZER, OUTPUT);

   pinMode(RELAY_A, OUTPUT);
   pinMode(RELAY_B, OUTPUT);

   pinMode(SENSACT_OUT1, OUTPUT);
   pinMode(SENSACT_OUT2, OUTPUT);
   pinMode(SENSACT_OUT3, OUTPUT);

   digitalWrite( ledPin, HIGH);

   for( int i=0; i<nInputs; i++ ) lastRead[i] = 0;
   for( int j=0; j<nOutputs; j++) {
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
      Serial.print(gyro_read());
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN2 ), 0, 1023, 0, 100));
      Serial.print(",");      
      Serial.print(map(analogRead( SENSACT_IN3 ), 0, 1023, 0, 100 ));
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN4 ), 0, 1023, 0, 100 ));
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
         case 0: 
            val = (byte) gyro_read();
            break;
         default:
            val = (byte) map(analogRead( map_in[i-1] ), 0, 1023, 0, 100);
            break;
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
               // the following cases have the same behaviour
            case RELAY_A:   
            case RELAY_B:  
            case LED: 
            case BUZZER:
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

void beep(int j) {
   if (j<1) return;
      for( int i=0; i<j; i++ ) {
      digitalWrite( SENSACT_BUZZER, HIGH );
      delay(10);
      digitalWrite( SENSACT_BUZZER, LOW );
      delay(150);
   }
}







































































