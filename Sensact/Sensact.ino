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
 "8," - config mode - 'get profile' on p5
 a) sends config info to p5, one time begin with "9999,...."
 b) receive config info from p5, any number of times - 'set profile'
 "0,...."
 c) return input readings to p5, every x msec
 
 "9" - Senseact goes into Run mode - 'run'
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

// depending on the Arduino IDE, comment out the HID.h, Keyboard.h and Mouse.h in new versions
//#include <HID.h>
#include <Keyboard.h>
#include <Mouse.h>
#include <Wire.h>

#include <SoftwareSerial.h>  
#include <EEPROM.h>
//#include "AACswitch.h"
#include "sensact.h"

// set pin numbers:
int ledPin = SENSACT_RED;      // which LED light to signal

// We are not flipping the LED on and off
//int ledState = LOW;          // ledState used to set the LED
//long previousMillis0 = 0;    // will store last time LED was updated
//long interval0 = 200;           // interval at which to blink (milliseconds)

long previousMillis = 0;        

// the follow variables is a long because the time, measured in miliseconds,
// will quickly become a bigger number than can be stored in an int.
//long interval = 400; 

long report_interval = 200; // interval between reporting signal levels
long pulseWidth = 50;  // output pulsewidth for the controls[] 
long read_interval = 50; // 400; // interval between processing the sensor signals
long REFRACTORY = 800;
long currentMillis = millis();

int whichSerial = 0; // 0 for Serial; 1 for bluetooth

int state = SENSACT_RUN;  
byte config[400];

long lastRead[nInputs];
// output signals: invert, relay1, relay2, BT, USB, click, joystick, buzzer
// 0 is a dummy pin!!
long whenOn[nOutputs], control[nOutputs] = { 
   0, SENSACT_OUT1, SENSACT_OUT2, 0,0, SENSACT_RED, 0, SENSACT_BUZZER }; // ylh WHY????
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

   pinMode(ledPin, OUTPUT);
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

   delay(200);

   global_reset(); // multiclick_setup(); //mc_setup(); // ylh test version, overrides config

#ifdef INCLUDE_BLUETOOTH
   bt_setup();
#endif

#ifdef INCLUDE_LGGYRO
   gyro_setup();
#endif

#ifdef INCLUDE_MPU6050
   mpu_setup();
#endif

   load_config();
   beep(2);
}

void loop()
{
#ifdef INCLUDE_MPU6050
   mpu_loop(); 
   return;
#endif

   //multiclick_loop(); //mc_loop(); //YLH TESt

   currentMillis = millis();

   serial_loop();  // YLH *******
   led_loop();

   //Serial.println("loop");


   for( int j=0; j<nOutputs; j++ ) {
      if( (currentMillis - whenOn[j]) > pulseWidth ) {
         if( control[j] == SENSACT_BUZZER )
            ;  //noTone(SENSACT_BUZZER); // NO NEED TO RESET BUZZER - we use tone()
         else     
            digitalWrite( control[j], LOW );
         onOff[j] = 0;
      }
   }

#ifdef INCLUDE_BLUETOOTH
   bt_loop();  // ylh ****
#endif

   switch( state ) {
   case SENSACT_CONFIG:
      report_signals();
      break;
   case SENSACT_RUN:
      process_signals();
#ifdef INCLUDE_REPORTING_WHEN_RUNNING
      report_signals();
#endif
      //mc_loop(); // ylh test version, overrides config
      break;
   default:
      break;
   }

   //   if( (currentMillis - previousMillis0) > interval0) {
   //      previousMillis0 = currentMillis;      
   //      if (ledState == LOW)
   //         ledState = HIGH;
   //      else
   //         ledState = LOW;
   //   }
}

//* BLUETOOTH CODE
#ifdef INCLUDE_BLUETOOTH
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
//*/
#endif

// led_state RED=output actuated; GREEN=run; BLUE=config
long ledInterval = 800;
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

//* SERIAL LOOP CODE HERE
// processing the interaction with web app
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

      //      else {
      //       whichSerial = 1;
      //       inChar = bluetooth.read();
      //       }

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

   //   this is tricky AND only for USB serial - if first character is
   //    '$' - send $$$ to BT to enter command mode
   //    '-' - send "---\n" to BT to exti command mode
   //    '@' - send the rest of the line plus '\n'
   //    '#' - send the rest of the line
   //    '%' - set bt to 9600
   //    '^' - set bt to 115200
   //    else - it's meant for Sensact to process

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

   // get the first token - if not 0,8,9 then abort the rest
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
         Serial.print("9999");
         for( int i=0; i<nInputs*(nOutputs + nValues); i++) {
            Serial.print(","); 
            config[i] = EEPROM.read(i);
            Serial.print(config[i]);   
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
      global_reset();
      break;
   default:
      state = SENSACT_UNKNOWN;
      break;
   }
}
//  END SERIAL LOOP CODE*/

void load_config() {
   //if(EEPROM.read(0) <=1) { // sanity check
   for( int i=0; i<(nInputs*(nOutputs+nValues)); i++) {
      config[i] = EEPROM.read(i);
   }
   //}
}

void report_signals() {
   if( (currentMillis - previousMillis) < report_interval) return;
   previousMillis = currentMillis;

   //if( whichSerial == 0 ) { //ylh kludge
   if( USE_GYRO == 0 ) {

#ifdef SENSACT_V1
      Serial.print( digitalRead(SENSACT_IN1) ); //Serial.print(gyro_read());
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN2 ), 0, 1023, 0, 100));
      Serial.print(",");      
      Serial.print(map(analogRead( SENSACT_IN3 ), 0, 1023, 0, 100 ));
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN4 ), 0, 1023, 0, 100 ));
#endif
#ifdef SENSACT_V2
      Serial.print( digitalRead(SENSACT_IN1A) ); //(map(analogRead( SENSACT_IN1A ), 0, 1023, 0, 100));
      Serial.print(",");
      Serial.print( digitalRead(SENSACT_IN1B) ); // (map(analogRead( SENSACT_IN1B ), 0, 1023, 0, 100));
      Serial.print(",");
      Serial.print(map(analogRead( SENSACT_IN2 ), 0, 1023, 0, 100));
      Serial.print(",");      
      Serial.print(map(analogRead( SENSACT_IN3 ), 0, 1023, 0, 100 ));
#endif
      Serial.print("\n");

   } 
   else { // use gyro
      //      Serial.print(gyro_read());
      //      Serial.print(",");
      //      Serial.print(map( my_gyro.g.x, -31000, 31000, 0, 100));
      //      Serial.print(",");      
      //      Serial.print(map( my_gyro.g.y, -31000, 31000, 0, 100 ));
      //      Serial.print(",");
      //      Serial.print(map( my_gyro.g.z , -31000, 31000, 0, 100 ));
      //      Serial.print("\n");

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

      delay(10); //ylh ******

      int offset = i*nOutputs;
      int offset2 = i*nValues + nInputs*nOutputs;

      if( USE_GYRO == 0 ) {
         switch(i) {
#ifdef SENSACT_V1
         case 0: 
            val = 0; //(byte) gyro_read();
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
         //         switch (i) {
         //         case 0: 
         //            val = (byte) gyro_read();
         //            break;
         //         case 1:
         //            val = (byte) map(my_gyro.g.x, -31000, 31000, 0, 100);
         //            break;
         //         case 2:
         //            val = (byte) map(my_gyro.g.y, -31000, 31000, 0, 100);
         //            break;
         //         case 3:
         //            val = (byte) map(my_gyro.g.z, -31000, 31000, 0, 100);
         //            break;
         //         }
      }

      if( config[ offset  + INVERT]==1 )
      { 
         val = 100 - val;
      }
      if( val > config[ offset2 + THRESHOLD_VAL] ) {
        lastRead[i] = currentMillis + REFRACTORY;
         for( int j=0; j<nOutputs; j++ ) {
            switch( j ) {
            case BLUETOOTH:   
               if( config[offset+BLUETOOTH] && onOff[j]==0 ) {
                  bluetooth.write( config[offset2 + BT_HID_VAL]);
                  startedOutput(j);
               }
               break;
            case USB_HID:   
               if( config[offset+USB_HID]  && onOff[j]==0 ) {
                  Keyboard.write( config[offset2 + USB_HID_VAL]);
                  startedOutput(j);
               }
               break;
            case BUZZER:
               if( config[offset+j] ) {
                  tone(control[j], NOTE_C5, 250); //digitalWrite(control[j], HIGH);
                  startedOutput(j);
               }
               break;
            case CLICK:              
               if( config[offset+j] && !Mouse.isPressed() ) {
                  Mouse.press();
                  // Short -long click Morse style handling
                  //mark();
               }

               break;
               // the following cases have the same behaviour
            case RELAY_A:   
            case RELAY_B:  

               if( config[offset+j] && onOff[j]==0 ) {
                  digitalWrite(control[j], HIGH);
                  startedOutput(j);
               }
               break;
            default: 
               break;
            }
         }
      } 
      else { // non trigger
         for( int j=0; j<nOutputs; j++ ) {
            switch( j ) {
            case CLICK: 
               if( config[offset+j] && Mouse.isPressed() ) {
                  Mouse.release();
               }
               /* Short -long click Morse style handling
                if( config[offset+j] ) {
                no_mark();
                }
                */
               break;
               // the following cases have the same behaviour
            case BLUETOOTH: 
            case USB_HID:   
            case BUZZER:
            case RELAY_A:   
            case RELAY_B:  
            default:
               break;
            }
         }
      }
      // special handling for joystick - or any two value action
      if( config[offset+JOYSTICK] && onOff[JOYSTICK]==0 ) {
         if( val<20 ) {
            joySelection(0, config[offset2 + JOY_VAL]);
            startedOutput(JOYSTICK);
            // don't add REFRACTORY
         }
         if( val>80 ) {
            joySelection(1, config[offset2 + JOY_VAL]);
            startedOutput(JOYSTICK);
            // don't add REFRACTORY
         }
      }
   }
}

void joySelection( int lowHigh, int selection ) {
   if( selection == KEY_L_R ) {
      if( lowHigh == 0 ) {
         Keyboard.press(KEY_LEFT_ARROW); 
      } 
      else {
         Keyboard.press(KEY_RIGHT_ARROW);
      }      
   }    
   else if( selection == KEY_U_D ) {
      if( lowHigh == 0 ) {
         Keyboard.press(KEY_UP_ARROW);
      }      
      else {
         Keyboard.press(KEY_DOWN_ARROW);
      }
   }  
   else if( selection == MOUSE_L_R ) {
      if( lowHigh == 0 ) {
         Mouse.move( -3, 0, 0);
      }      
      else  {
         Mouse.move( 3, 0, 0);
      }  
   }   
   else if( selection == MOUSE_U_D ) {
      if( lowHigh == 0 ) {
         Mouse.move( 0, -3, 0);
      }      
      else {
         Mouse.move( 0, 3, 0);
      }
   }
   Keyboard.releaseAll();
}

void startedOutput( int x ) {
   ledTime = whenOn[x] = currentMillis;
   onOff[x] ++;
}

void beep( int j) {
   if (j<1) return;
   for( int i=0; i<j; i++ ) {
      tone(SENSACT_BUZZER, NOTE_C7, 250);
      delay(50);
      //noTone(SENSACT_BUZZER);      
      delay(150);

      /*
      digitalWrite( SENSACT_BUZZER, HIGH );
       delay(10);
       digitalWrite( SENSACT_BUZZER, LOW );
       delay(150);
       */
   }
}

void global_reset() {
   click_setup();
}

