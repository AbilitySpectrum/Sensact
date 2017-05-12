// -------------------------------------
// Actions.cpp
// -------------------------------------

#include <Keyboard.h>
#include <Mouse.h>
#include <IRLib.h>
#include "Actions.h"

Actors actors;

void Actors::init() {
  // Add actors
  addActor( new Relay(1, SENSACT_RELAY_1) );
  addActor( new Relay(2, SENSACT_RELAY_2) );
  addActor( new BTKeyboard(3) );
  addActor( new HIDKeyboard(4) );
  addActor( new BTMouse(9) );
  addActor( new HIDMouse(5) );
  addActor( new Buzzer(7, SENSACT_BUZZER) );
  addActor( new IRTV(8) );

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
    int id = pData->getAction(i)->actionID;
    long param = pData->getAction(i)->actionParameter;
    
    // Find the required actor
    for(int j=0; j<nActors; j++) {
      if (apActors[j]->id == id) {
        apActors[j]->doAction(param);
        break;
      }
    }
  }
  // Check for actions that may need to be terminated
  for(int i=0; i<nActors; i++) {
    apActors[i]->checkAction();
  }
}

// === RELAY === //
void Relay::init() {
  pinMode(pin, OUTPUT);
}

void Relay::doAction(long param) {
  actionStartTime = millis();
  digitalWrite(pin, HIGH);
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

// === HID Keyboard === //
void HIDKeyboard::doAction(long param) {
  int i;
  for(i=0; i<4; i++){
    int character = (param >> 8 * (3-i)) & 0xff;
    if (character != 0) {
      Keyboard.write(character);
    }
  }
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

#define SA_MOUSE_SPEED 6

// Mouse nudge commands - for Gyro Accel motions
#define NUDGE_UP      10
#define NUDGE_DOWN    11
#define NUDGE_LEFT    12
#define NUDGE_RIGHT   13

unsigned int MouseControl::timeDiff(unsigned int now, unsigned int prev) {
  if (now > prev) return (now - prev);
  // else
  return (0xffff - prev + now + 1);  // Counter rolled over.
}

void MouseControl::doAction(long param) {
  int option = param & 0xffff;
  switch(option) {
    case SA_MOUSE_UP:
      mc_move(0, -SA_MOUSE_SPEED);
      break;
    case SA_MOUSE_DOWN:
      mc_move(0, SA_MOUSE_SPEED);
      break;
    case SA_MOUSE_LEFT:
      mc_move(-SA_MOUSE_SPEED, 0);
      break;
    case SA_MOUSE_RIGHT:
      mc_move(SA_MOUSE_SPEED, 0);
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
      }
      break;
    case NUDGE_DOWN:
      if (verticalMouseState == MOUSE_MOVING_UP) {
        verticalMouseState = MOUSE_STILL;
      } else if (verticalMouseState == MOUSE_STILL) {
        verticalMouseState = MOUSE_MOVING_DOWN;
      }
      break;
    case NUDGE_LEFT:
      if (horizontalMouseState == MOUSE_MOVING_RIGHT) {
        horizontalMouseState = MOUSE_STILL;
      } else if (horizontalMouseState == MOUSE_STILL) {
        horizontalMouseState = MOUSE_MOVING_LEFT;
      }
      break;
    case NUDGE_RIGHT:
      if (horizontalMouseState == MOUSE_MOVING_LEFT) {
        horizontalMouseState = MOUSE_STILL;
      } else if (horizontalMouseState == MOUSE_STILL) {
        horizontalMouseState = MOUSE_MOVING_RIGHT;
      }
      break;
  }
}

void MouseControl::checkAction() {
  unsigned int now = millis() & 0xffff;
  if ( timeDiff(now, lastMouseMoveTime) > REPEAT_INTERVAL ) {
    if (verticalMouseState == MOUSE_MOVING_UP) {
      mc_move(0, -SA_MOUSE_SPEED); 
    } else if (verticalMouseState == MOUSE_MOVING_DOWN) {
      mc_move(0, SA_MOUSE_SPEED);
    }
    if (horizontalMouseState == MOUSE_MOVING_LEFT) {
      mc_move(-SA_MOUSE_SPEED, 0); 
    } else if (horizontalMouseState == MOUSE_MOVING_RIGHT) {
      mc_move(SA_MOUSE_SPEED, 0);
    }
    lastMouseMoveTime = now;
  }
}

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

// === Bluetooth Keyboard ===
void BTKeyboard::init() {
  pBlueHID = getBT();
}

void BTKeyboard::doAction(long param) {
  int i;
  for(i=0; i<4; i++){
    int character = (param >> 8 * (3-i)) & 0xff;
    if (character != 0) {
      pBlueHID->write(character);
    }
  }  
}

// === Bluetooth Mouse === //
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


// === IR TV === //
#define TV_ON_OFF    1
#define VOLUME_UP    2
#define VOLUME_DOWN  3
#define CHANNEL_UP   4
#define CHANNEL_DOWN 5

// Hard-wired IR values - for now.
#ifdef MY_TV
// Codes for Andrew's home TV - an LG
IRTYPES IRProtocol         = NEC;
unsigned long code_OnOff        = 0x20DF10EF;
unsigned long code_VolumeUp     = 0x20DF40BF;
unsigned long code_VolumeDown   = 0x20DFC03F;
unsigned long code_ChannelUp    = 0;
unsigned long code_ChannelDown  = 0;
#else
// Codes for Bruyere TVs
IRTYPES IRProtocol         = NECX;
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
      irSender.send(IRProtocol, code_OnOff, 0);
      break;
    case VOLUME_UP:
      irSender.send(IRProtocol, code_VolumeUp, 0);
      break;
    case VOLUME_DOWN:
      irSender.send(IRProtocol, code_VolumeDown, 0);
      break;
    case CHANNEL_UP:
      irSender.send(IRProtocol, code_ChannelUp, 0);
      break;
    case CHANNEL_DOWN:
      irSender.send(IRProtocol, code_ChannelDown, 0);
      break;
  }
}
