
#include "Arduino.h"
#define SENSACT_V2

const int bluetoothTx = 0;  // RX 0 of Arduino
const int bluetoothRx = 1;  // TX 1 of Arduino

#ifdef SENSACT_V1

const int SENSACT_RED = 21;
const int SENSACT_GREEN = 14;
const int SENSACT_BLUE = 15;
const int SENSACT_BUZZER = 10;

const int SENSACT_IN1 = 2;
const int SENSACT_SDA = 2;
const int SENSACT_SLK = 3;
const int SENSACT_IN2 = A2; //20; //A2;
const int SENSACT_IN3 = A1; //19; //A1;
const int SENSACT_IN4 = A0; //8; //A0;

const int SENSACT_OUT1 = 9;
const int SENSACT_OUT2 = 6;
const int SENSACT_OUT3 = 5;

const int nInputs = 4;
// config output indices and then the 3 values, threshold, bt char, hid char

const int map_in[] = { 
   SENSACT_IN2, SENSACT_IN3, SENSACT_IN4 };
#endif

#ifdef SENSACT_V2
const int SENSACT_RED = A3;
const int SENSACT_GREEN = 5;
const int SENSACT_BLUE = 4;
const int SENSACT_BUZZER = 10;

const int SENSACT_SDA = 2;
const int SENSACT_SLK = 3;
const int SENSACT_IN1A = 3;
const int SENSACT_IN1B = 2;

const int SENSACT_IN2 = A2;
const int SENSACT_IN3 = A1;  
//const int SENSACT_IN4 = A0; //8; //A0;

const int SENSACT_IR_IN = 12;
const int SENSACT_IR_OUT = 13;

const int SENSACT_OUT1 = 9;
const int SENSACT_OUT2 = 11;
const int nInputs = 4;

const int map_in[] = { 
   SENSACT_IN1A, SENSACT_IN1B, SENSACT_IN2, SENSACT_IN3 };
#endif

const int SENSACT_RUN = 9;
const int SENSACT_CONFIG = 8;
const int SENSACT_UNKNOWN = 99;

const int JOY_LEFT_RIGHT = 0, JOY_UP_DOWN = 1;

// config output indices and then the 3 values, threshold, bt char, hid char

const int 
I2C = 0,
//nInputs = 4;
INVERT = 0,
RELAY_A = 1,
RELAY_B = 2,
BLUETOOTH = 3,
USB_HID = 4,
CLICK = 5,  // was LED
JOYSTICK = 6,
BUZZER = 7,
nOutputs = 8,
THRESHOLD_VAL = 0,
BT_HID_VAL = 1,
USB_HID_VAL = 2,
JOY_VAL = 3,
nValues = 4
;

/* EEPROM data structure - also stored in config[] - in bytes
 the first bytes are output controls corresponding to each input
 [nInputs][nOutputs]
 
 this is followed by the values for [nInputs][nValues] - in bytes
 THRESHOLD - input trigger threshold
 BT_HID_VAL - char to output to BT
 USB_HID_VAL - char to output to USB HID
 */




