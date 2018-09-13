// -------------------------------------
// Actions.cpp
// -------------------------------------

/*
 * Possible symbols for #ifdefs
 * 
 *   __AVR_ATmega32U4__    // Leonardo
 *   __AVR_ATmega2560__    // AT Mega
 */
 
#ifdef __AVR_ATmega32U4__  // Leonardo
#include <Keyboard.h>
#include <Mouse.h>
#endif
#include "Wire.h"

#include <IRLibSendBase.h>
#include <IRLib_P01_NEC.h>   // Lowest numbers must be first
//#include <IRLib_P02_Sony.h>
//#include <IRLib_P03_RC5.h>
//#include <IRLib_P04_RC6.h>
//#include <IRLib_P05_Panasonic_Old.h>
//#include <IRLib_P06_JVC.h>
#include <IRLib_P07_NECx.h>
//#include <IRLib_P08_Samsung36.h>
//#include <IRLib_P09_GICable.h>
//#include <IRLib_P10_DirecTV.h>
#include <IRLibCombo.h>     // Combine them into "IRsend"
#include <IRLibProtocols.h>

#include "Actions.h"

Actors actors;

/*
 *  Memory Saving Trick #1
 *  Save times in unsigned int values.  This will hold times up to 65 seconds.
 *  Use timeDiff to calculate the time interval between 'now' and a previous time 'prev'.
 *  'now' should always be greater than 'prev'.  If it isn't it is because our counter
 *  rolled over - and the difference calculation is adjusted to take this into account.
 *  
 *  Since we never care about time intervals bigger than a couple of seconds this works just fine.
 *  Using unsigned int rather than a long saves 4 bytes per trigger structure.
 *  With MAX_TRIGGERS set to 20 this is a savings of 80 bytes.
 */
unsigned int timeDiff(unsigned int now, unsigned int prev) {
  if (now > prev) return (now - prev);
  // else
  return (0xffff - prev + now + 1);  // Counter rolled over.
}

void Actors::init() {
  // Add actors
  addActor( new Relay(1, SENSACT_RELAY_1) );
  addActor( new Relay(2, SENSACT_RELAY_2) );
  addActor( new BTKeyboard(3) );
#ifdef __AVR_ATmega32U4__  // Leonardo
  addActor( new HIDKeyboard(4) );
#endif
  addActor( new BTMouse(9) );
#ifdef __AVR_ATmega32U4__  // Leonardo
  addActor( new HIDMouse(5) );
#endif
  addActor( new Buzzer(7, SENSACT_BUZZER) );
  addActor( new IRTV(8) );
  addActor( new SerialSend(6) );
  addActor( new LightBox(11) );

  for(int i=0; i<nActors; i++) {
    apActors[i]->init();
  }
}

void Actors::reset() {
  for(int i=0; i<nActors; i++) {
    apActors[i]->reset();
  }  
}

void Actors::doActions(const ActionData *pData) {
  int nActions = pData->length();
  for(int i=0; i<nActions; i++) {
    int id = pData->getAction(i)->actionID & ACTION_ID_MASK;
    int repeat = pData->getAction(i)->actionID & REPEAT_BIT;
    long param = pData->getAction(i)->actionParameter;

    // Find the required actor
    for(int j=0; j<nActors; j++) {
      if (apActors[j]->id == id) {
        apActors[j]->assessAction(param, repeat);
        break;
      }
    }
  }
  // Check for actions that may need to be terminated
  for(int i=0; i<nActors; i++) {
    apActors[i]->checkAction();
  }
}

// Default Repeat Logic - repeat every 1/2 second //
void Actor::assessAction(long param, int repeat) {
  unsigned int now = millis() & 0xffff;
  if (repeat) {
    if ( timeDiff(now, lastActionTime) < DEFAULT_REPEAT_INTERVAL ) {
      return; // Not long enough since the last action.  
    }
  }
  lastActionTime = now;
  doAction(param);
}

// === RELAY === //
void Relay::init() {
  pinMode(pin, OUTPUT);
}

void Relay::doAction(long param) {
  switch(param) {
    case RELAY_PULSE:
      actionStartTime = millis();
      digitalWrite(pin, HIGH);
      break;
    case RELAY_ON:
      digitalWrite(pin, HIGH);
      break;
    case RELAY_OFF:
      digitalWrite(pin, LOW);
      break;
  }
}

void Relay::checkAction() {
  if (actionStartTime) {
    if ( (millis() - actionStartTime) > PULSE_WIDTH) {
      digitalWrite(pin, LOW);
      actionStartTime = 0;
    }
  }
}

// === BUZZER === //
void Buzzer::init() {
  pinMode(pin, OUTPUT);
}

void Buzzer::doAction(long param) {
  int pitch = (param >> 16) & 0xffff;
  int duration = param & 0xffff;
  
  tone(pin, pitch, duration);
}

// === Mouse Control === //
#define SA_MOUSE_UP    1
#define SA_MOUSE_DOWN  2
#define SA_MOUSE_LEFT  3
#define SA_MOUSE_RIGHT 4
#define SA_MOUSE_CLICK 5
#define SA_MOUSE_PRESS 6
#define SA_MOUSE_RELEASE 7
#define SA_MOUSE_RIGHT_CLICK 8

// Mouse nudge commands - for Gyro Accel motions
#define NUDGE_UP      10
#define NUDGE_DOWN    11
#define NUDGE_LEFT    12
#define NUDGE_RIGHT   13
#define NUDGE_STOP    14

unsigned char delay_1 = 35;
unsigned char jump_1 = 2;

unsigned int transitionTime_1 = 500;
unsigned char delay_2 = 59;
unsigned char jump_2 = 6;

unsigned int transitionTime_2 = 1000;
unsigned char delay_3 = 23;
unsigned char jump_3 = 6;

int readMouseSpeed(InputStream *is) {
  unsigned char d1, d2, d3;
  unsigned char j1, j2, j3;
  unsigned int t1, t2;
  int tmpInt;
  long tmpLong;

  int len = is->getNum();
  if (len != 20) {
    // Discard unknown data.
    for(int i=0; i<len; i++) {
      is->getChar();
    }
    return 0;
  }

  if ((tmpInt = is->getID()) == IO_ERROR) return IO_ERROR;
  d1 = tmpInt;
  if ((tmpInt = is->getID()) == IO_ERROR) return IO_ERROR;
  j1 = tmpInt;
  if ((tmpInt = is->getID()) == IO_ERROR) return IO_ERROR;
  d2 = tmpInt;
  if ((tmpInt = is->getID()) == IO_ERROR) return IO_ERROR;
  j2 = tmpInt;
  if ((tmpInt = is->getID()) == IO_ERROR) return IO_ERROR;
  d3 = tmpInt;
  if ((tmpInt = is->getID()) == IO_ERROR) return IO_ERROR;
  j3 = tmpInt;
  if ((tmpLong = is->getNum()) == IO_ERROR) return IO_ERROR;
  t1 = tmpLong;
  if ((tmpLong = is->getNum()) == IO_ERROR) return IO_ERROR;
  t2 = tmpLong;

  delay_1 = d1;
  jump_1 = j1;
  transitionTime_1 = t1;
  delay_2 = d2;
  jump_2 = j2;
  transitionTime_2 = t1 + t2;
  delay_3 = d3;
  jump_3 = j3;

  return 0;
}

void sendMouseSpeed(OutputStream *os) {
  os->putNum(20); //data length
  os->putID(delay_1);
  os->putID(jump_1);
  os->putID(delay_2);
  os->putID(jump_2);
  os->putID(delay_3);
  os->putID(jump_3);
  os->putNum(transitionTime_1);
  os->putNum(transitionTime_2 - transitionTime_1);
}

// MouseControl repeat logic is somewhat complex.
// Horizontal and vertical motion can be simultaneous, 
// so separate time counters are required.
// Mouse press, release, click and nudge actions do not repeat.
void MouseControl::assessAction(long param, int repeat) {
  unsigned int now = millis() & 0xffff;
  int option = param & 0xffff;
  
  if (repeat) {
    // Accelerating mouse logic.
    unsigned int repeatInterval; // Time between mouse moves.
    if (maxSpeedReached) {
      repeatInterval = delay_3;
      jumpSize = jump_3;      
    } else if (timeDiff(now, mouseStartTime) < transitionTime_1) {
      repeatInterval = delay_1;
      jumpSize = jump_1;
    } else if (timeDiff(now, mouseStartTime) < transitionTime_2) {
      repeatInterval = delay_2;
      jumpSize = jump_2;
    } else {
      repeatInterval = delay_3;
      jumpSize = jump_3;
      maxSpeedReached = 1;
    }

    if (option == SA_MOUSE_UP || option == SA_MOUSE_DOWN) {
      if ( timeDiff(now, lastMouseVerticalMove) < repeatInterval) {
        return;   // It's not yet time to repeat.
      }
    } else if (option == SA_MOUSE_LEFT || option == SA_MOUSE_RIGHT) {
      if ( timeDiff(now, lastMouseHorizontalMove) < repeatInterval) {
        return;   
      }      
    } else {
      // Repeat for all other mouse actions is not allowed
      return;
    }
  } else {
    // Initial action - not a repeat
    jumpSize = jump_1;
    mouseStartTime = now;
    maxSpeedReached = 0;
  }
  if (option == SA_MOUSE_UP || option == SA_MOUSE_DOWN) {
    lastMouseVerticalMove = now;
  } else if (option == SA_MOUSE_LEFT || option == SA_MOUSE_RIGHT) {
    lastMouseHorizontalMove = now;
  }
  doAction(param);   
}

void MouseControl::doAction(long param) {
  int option = param & 0xffff;
  switch(option) {
    case SA_MOUSE_UP:
      mc_move(0, -jumpSize);
      break;
    case SA_MOUSE_DOWN:
      mc_move(0, jumpSize);
      break;
    case SA_MOUSE_LEFT:
      mc_move(-jumpSize, 0);
      break;
    case SA_MOUSE_RIGHT:
      mc_move(jumpSize, 0);
      break;
    case SA_MOUSE_CLICK:
      mc_button(MC_LEFT_CLICK);
      break;
    case SA_MOUSE_RIGHT_CLICK:
      mc_button(MC_RIGHT_CLICK);
      break;
    case SA_MOUSE_PRESS:
      mc_button(MC_PRESS);
      break;
    case SA_MOUSE_RELEASE:
      mc_button(MC_RELEASE);
      break;
    case NUDGE_UP:
      if (verticalMouseState == MOUSE_MOVING_DOWN) {
        verticalMouseState = MOUSE_STILL;
      } else if (verticalMouseState == MOUSE_STILL) {
        verticalMouseState = MOUSE_MOVING_UP;
        assessAction(SA_MOUSE_UP, 0);
      }
      break;
    case NUDGE_DOWN:
      if (verticalMouseState == MOUSE_MOVING_UP) {
        verticalMouseState = MOUSE_STILL;
      } else if (verticalMouseState == MOUSE_STILL) {
        verticalMouseState = MOUSE_MOVING_DOWN;
        assessAction(SA_MOUSE_DOWN, 0);
      }
      break;
    case NUDGE_LEFT:
      if (horizontalMouseState == MOUSE_MOVING_RIGHT) {
        horizontalMouseState = MOUSE_STILL;
      } else if (horizontalMouseState == MOUSE_STILL) {
        horizontalMouseState = MOUSE_MOVING_LEFT;
        assessAction(SA_MOUSE_LEFT, 0);
      }
      break;
    case NUDGE_RIGHT:
      if (horizontalMouseState == MOUSE_MOVING_LEFT) {
        horizontalMouseState = MOUSE_STILL;
      } else if (horizontalMouseState == MOUSE_STILL) {
        horizontalMouseState = MOUSE_MOVING_RIGHT;
        assessAction(SA_MOUSE_RIGHT, 0);
      }
      break;
    case NUDGE_STOP:
      horizontalMouseState = MOUSE_STILL;
      verticalMouseState = MOUSE_STILL;
      break;
  }
}

// If mouse nudging is active it is handled by pretending 
// there are mouse repeat actions.
void MouseControl::checkAction() {
    if (verticalMouseState == MOUSE_MOVING_UP) {
      assessAction(SA_MOUSE_UP, 1); 
    } else if (verticalMouseState == MOUSE_MOVING_DOWN) {
      assessAction(SA_MOUSE_DOWN, 1); 
    }
    if (horizontalMouseState == MOUSE_MOVING_LEFT) {
      assessAction(SA_MOUSE_LEFT, 1); 
    } else if (horizontalMouseState == MOUSE_MOVING_RIGHT) {
      assessAction(SA_MOUSE_RIGHT, 1); 
    }
}

// === Keyboard Control === //
void KeyboardControl::doAction(long param) {
  int i;
  for(i=0; i<4; i++){
    int character = (param >> 8 * (3-i)) & 0xff;
    if (character != 0) {
      kc_write(character);
    }
  }  
}

#ifdef __AVR_ATmega32U4__  // Leonardo
// === HID === //
// --- HID Mouse --- //
void HIDMouse::mc_move(int x, int y) {
    Mouse.move(x, y);
}

void HIDMouse::mc_button(int val) {
  switch(val) {
    case MC_LEFT_CLICK:
      Mouse.click();
      break;
    case MC_RIGHT_CLICK:
      Mouse.click(MOUSE_RIGHT);
      break;
    case MC_PRESS:
      Mouse.press();
      break;
    case MC_RELEASE:
      Mouse.release();
      break;
  }
}

// --- HID Keyboard --- //
void HIDKeyboard::kc_write(char character) {
  Keyboard.write(character);
}
#endif

// === Bluetooth === //
#define BT_TX_PIN 0
#define BT_RX_PIN 1

// Make the bluetooth pointer a singleton.
// shared by BTKeyboard and BTMouse.
static SoftwareSerial *pBlueHID = 0;
SoftwareSerial *getBT() {
  if (pBlueHID == 0) {
    pBlueHID = new SoftwareSerial(BT_TX_PIN, BT_RX_PIN);
    delay(1000);
    pBlueHID->begin(115200);  // Bluetooth defaults to 115200bps
    
    pBlueHID->print("$");    // Print three times individually
    pBlueHID->print("$");
    pBlueHID->print("$");    // Enter command mode
    delay(100);    // Short delay
    pBlueHID->println("U,9600,N"); //Change baud rate to 9600 - no parity
    pBlueHID->begin(9600);  // Start bluetooth at 9600    
  }
  return pBlueHID;
}


// --- Bluetooth Mouse --- //
void BTMouse::init() {
  pMouse = new BTMouseCtl( getBT() );
}

void BTMouse::mc_move(int x, int y) {
    pMouse->move(x, y);
}

void BTMouse::mc_button(int val) {
  switch(val) {
    case MC_LEFT_CLICK:
      pMouse->click(BT_LEFT_BUTTON);
      break;
    case MC_RIGHT_CLICK:
      pMouse->click(BT_RIGHT_BUTTON);
      break;
    case MC_PRESS:
      pMouse->press(BT_LEFT_BUTTON);
      break;
    case MC_RELEASE:
      pMouse->release(BT_LEFT_BUTTON);
      break;
  }
}

// --- Bluetooth Keyboard --- //
void BTKeyboard::init() {
  pBlueHID = getBT();
}

void BTKeyboard::kc_write(char character) {
  pBlueHID->write(character);
}

// === IR TV === //
#define TV_ON_OFF    1
#define VOLUME_UP    2
#define VOLUME_DOWN  3
#define CHANNEL_UP   4
#define CHANNEL_DOWN 5

// Hard-wired IR values - for now.
#ifdef MY_TV
// Codes for Andrew's home TV - an LG
byte protocol    = NEC;
byte bits        = 32;
byte khz         = 38;
unsigned long code_OnOff        = 0x20DF10EF;
unsigned long code_VolumeUp     = 0x20DF40BF;
unsigned long code_VolumeDown   = 0x20DFC03F;
unsigned long code_ChannelUp    = 0;
unsigned long code_ChannelDown  = 0;
#else
// Codes for Bruyere TVs
byte protocol    = NECX;
byte bits        = 32;
byte khz         = 38;
unsigned long code_OnOff        = 0xE0E040BF;
unsigned long code_VolumeUp     = 0xE0E0E01F;
unsigned long code_VolumeDown   = 0xE0E0D02F;
unsigned long code_ChannelUp    = 0xE0E048B7;
unsigned long code_ChannelDown  = 0xE0E008F7;
#endif

IRsend irSender;

void IRTV::init() {
}

void IRTV::doAction(long param) {
  int option = param & 0xff;
  
  switch(option) {
    case TV_ON_OFF:
      irSender.send(protocol, code_OnOff, bits, khz);
      break;
    case VOLUME_UP:
      irSender.send(protocol, code_VolumeUp, bits, khz);
      break;
    case VOLUME_DOWN:
      irSender.send(protocol, code_VolumeDown, bits, khz);
      break;
    case CHANNEL_UP:
      irSender.send(protocol, code_ChannelUp, bits, khz);
      break;
    case CHANNEL_DOWN:
      irSender.send(protocol, code_ChannelDown, bits, khz);
      break;
  }
}

void SerialSend::kc_write(char ch) {
  Serial.print(ch);
}

#define MCP23008_ADDRESS 0x20
void LightBox::doAction(long param) {
  byte val = param &0xff;
  Wire.begin();
  Wire.beginTransmission(MCP23008_ADDRESS);
  Wire.write((byte)0);
  Wire.write((byte)0x00);  // Set all pins to output mode
  Wire.endTransmission(); 

  Wire.beginTransmission(MCP23008_ADDRESS);
  Wire.write((byte)9);  // GPIO Address
  Wire.write(val);
  Wire.endTransmission();  
}


