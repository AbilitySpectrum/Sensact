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
  addActor( new Bluetooth(3) );
  addActor( new HIDKeyboard(4) );
  addActor( new HIDMouse(5) );
  addActor( new Buzzer(7, SENSACT_BUZZER) );
  addActor( new IRTV(8) );

  for(int i=0; i<nActors; i++) {
    apActors[i]->init();
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
  int character = param & 0xff;
  Keyboard.write(character);
}

// === HID Mouse === //
#define SA_MOUSE_UP    1
#define SA_MOUSE_DOWN  2
#define SA_MOUSE_LEFT  3
#define SA_MOUSE_RIGHT 4
#define SA_MOUSE_CLICK 5
#define SA_MOUSE_SPEED 6

void HIDMouse::doAction(long param) {
  int option = param & 0xffff;
  switch(option) {
    case SA_MOUSE_UP:
      Mouse.move(0, -SA_MOUSE_SPEED);
      break;
    case SA_MOUSE_DOWN:
      Mouse.move(0, SA_MOUSE_SPEED);
      break;
    case SA_MOUSE_LEFT:
      Mouse.move(-SA_MOUSE_SPEED, 0);
      break;
    case SA_MOUSE_RIGHT:
      Mouse.move(SA_MOUSE_SPEED, 0);
      break;
    case SA_MOUSE_CLICK:
      Mouse.click();
      break;
  }
}

// === Bluetooth === //
#define BT_TX_PIN 0
#define BT_RX_PIN 1

void Bluetooth::init() {
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

void Bluetooth::doAction(long param) {
  int character = param & 0xff;
  
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
