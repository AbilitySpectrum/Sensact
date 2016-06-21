//#include <HID-Project.h>
//#include <HID-Settings.h>

#include <Mouse.h>
/*********************************************************
  2016-06-11 Sat : ylh

  I added reading from digital as well as analog lines and send the readings
  out to serial port (can be commented out in #define SERIAL_OUT

  digital signals are sent as -0, -1, etc
  analog signals are sent as "YPR:?:?:?"
  The touchpad signals are sent out as +0, +1, ..., +11

  These conventions are compatible with the p5 browser app:  ~/0/touchpad

  HID mouse control is controlled from the Joystick's A0, A1 signals,
  click = digital pin 3 (LEFT) pin 2 (RIGHT)

  --- touch pad code comes from
  This is a library for the MPR121 12-channel Capacitive touch sensor

  Designed specifically to work with the MPR121 Breakout in the Adafruit shop
  ----> https://www.adafruit.com/products/

  These sensors use I2C communicate, at least 2 pins are required
  to interface

  Adafruit invests time and resources providing this open source code,
  please support Adafruit and open-source hardware by purchasing
  products from Adafruit!

  Written by Limor Fried/Ladyada for Adafruit Industries.
  BSD license, all text above must be included in any redistribution
**********************************************************/
/*** HOW TO USE
    #define SERIAL_OUTPUT to output input signals. Do not use with mouse.
    #define SEE_ANALOG to output the analog pins
    #define SEE_DIGITAL to output the digital lines
    #define SEE_TOUCHPAD to output touchpad (I2C) data
    #define USE_MOUSE use mouse control - must turn off SERIAL_OUTPUT

*/
//#define SERIAL_OUTPUT
//#define SEE_ANALOG
//#define SEE_DIGITAL
//#define SEE_TOUCHPAD
#define USE_MOUSE

const uint32_t REFRACTORY = 20;
uint32_t l_checked;

#include <Wire.h>
#include "Adafruit_MPR121.h"


// MPR touchpad
// You can have up to 4 on one i2c bus but one is enough for testing!
Adafruit_MPR121 cap = Adafruit_MPR121();
// output "+?" where ? is the key number

// Keeps track of the last pins touched
// so we know when buttons are 'released'
uint16_t lasttouched = 0;
uint16_t currtouched = 0;

// analogPins
int nAnalogPins = 3;
int analogPins[3] = { A0, A1, A2 };

// digitalPins
// we added 3 push button pins on 11, 12 and 13
// when pushed, we output "-0", "-1", "-2", "-3"
int nDigitalPins = 4;
int digitalPins[4] = { 3, 4, 5, 6 };


void setup() {

#ifdef SERIAL_OUTPUT
  while (!Serial);        // needed to keep leonardo/micro from starting too fast!
  Serial.begin(9600);
#endif

#ifdef SEE_TOUCHPAD
  touchSetup();
#endif

#ifdef SEE_DIGITAL
  digitalPinsSetup();
#endif

#ifdef SEE_ANALOG
  analogPinsSetup();
#endif

#ifdef USE_MOUSE
  mouseSetup();
#endif
}



void loop() {

  if( l_checked < REFRACTORY ) l_checked = millis();

  if ( millis() - l_checked < REFRACTORY ) return;
  l_checked = millis();

#ifdef SEE_TOUCHPAD
  touchLoop();
#endif

#ifdef SEE_DIGITAL
  digitalPinsLoop();
#endif

#ifdef SEE_ANALOG
  analogPinsLoop();
#endif

#ifdef USE_MOUSE
  mouseLoop();
#endif

}

void analogPinsSetup() {
}

void analogPinsLoop() {

#ifdef SERIAL_OUTPUT
  Serial.print("YPR"); // use the YPR meter!

  for ( int j = 0; j < nAnalogPins; j++ ) {
    Serial.print(":");
    Serial.print(map(analogRead(analogPins[j]), 0, 1023, -60, 60 ));
  }
  Serial.print("\n");
#endif
}

void digitalPinsSetup() {
  for ( int j = 0; j < nDigitalPins; j++ ) {
    pinMode( digitalPins[j], INPUT_PULLUP);
  }
}

void digitalPinsLoop() {

  for ( int j = 0; j < nDigitalPins; j++ ) {
    if ( digitalRead(digitalPins[j]) == LOW ) {

#ifdef SERIAL_OUTPUT
      Serial.print(" - : ");
      Serial.println(j);
#endif
      if ( j == 3 ) {
        Mouse.press(MOUSE_LEFT);
      }
      if ( j == 2 ) {
        Mouse.press(MOUSE_RIGHT);
      }

    } else {
      if ( j == 3 ) {
        Mouse.release(MOUSE_LEFT);
      }
      if ( j == 2 ) {
        Mouse.release(MOUSE_RIGHT);
      }

    }
  }
}

void touchSetup() {

#ifdef SERIAL_OUTPUT
  Serial.println("Adafruit MPR121 Capacitive Touch sensor test");
#endif
  // Default address is 0x5A, if tied to 3.3V its 0x5B
  // If tied to SDA its 0x5C and if SCL then 0x5D
  if (!cap.begin(0x5A)) {

#ifdef SERIAL_OUTPUT
    Serial.println("MPR121 not found, check wiring ? ");
#endif
    while (1);
  }

#ifdef SERIAL_OUTPUT
  Serial.println("MPR121 found!");
#endif
}

void touchLoop() {
  // Get the currently touched pads
  currtouched = cap.touched();

  /* VERSION 1
    for (uint8_t i = 0; i < 12; i++) {
    // it if *is* touched and *wasnt* touched before, alert!
    if ((currtouched & _BV(i)) && !(lasttouched & _BV(i)) ) {
    Serial.print(" + : "); Serial.println(i); //Serial.println(" touched");
    }
    // if it *was* touched and now *isnt*, alert!
    if (!(currtouched & _BV(i)) && (lasttouched & _BV(i)) ) {
    Serial.print(" - : "); Serial.println(i); //Serial.println(" released");
    }
    }
    //*/

  //* VERSION to work with p5.js ~/0/touchpad
  for (uint8_t i = 0; i < 12; i++) {
    if ((currtouched & _BV(i)) ) {

#ifdef SERIAL_OUTPUT
      Serial.print(" + : ");
      Serial.println(i);
#endif
    }
  }

  //*/

  /* VERSION to output somewhat graphically
    for (uint8_t i = 0; i < 12; i++) {
    if ((currtouched & _BV(i)) ) {
    Serial.print(i);
    } else Serial.print(" ");
    }
    Serial.println();
    //*/

  // reset our state
  lasttouched = currtouched;

  // comment out this line for detailed data from the sensor!
  return;

  // debugging info, what
  Serial.print("\t\t\t\t\t\t\t\t\t\t\t\t\t 0x"); Serial.println(cap.touched(), HEX);
  Serial.print("Filt : ");
  for (uint8_t i = 0; i < 12; i++) {
    Serial.print(cap.filteredData(i)); Serial.print("\t");
  }
  Serial.println();
  Serial.print("Base : ");
  for (uint8_t i = 0; i < 12; i++) {
    Serial.print(cap.baselineData(i)); Serial.print("\t");
  }
  Serial.println();

  // put a delay so it isn't overwhelming
  delay(500);
}
