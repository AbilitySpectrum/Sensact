// -------------------------------------
// Actions.h
// -------------------------------------

#ifndef ActionData_H
#define ActionData_H
#include "Sensact.h"
#include <SoftwareSerial.h>
#include "BTMouseCtl.h"
#include "IO.h"

int readMouseSpeed(InputStream *is);
void sendMouseSpeed(OutputStream *os);

// Action - Identifies a single action.
#define ACTION_ID_MASK   0x7F
#define REPEAT_BIT  0x80
struct Action {
  char actionID;    // ID and repeat bit.
  long actionParameter;
};

// ActionData - A collection of Action objects
class ActionData {
  struct Action aActions[MAX_ACTIONS];
  int nActions;
  
  public:
    void reset() {nActions = 0;}
    void addAction(int id, long param, boolean repeat) {
      if (nActions < MAX_ACTIONS) {
        aActions[nActions].actionID = id | (repeat ? REPEAT_BIT : 0);
        aActions[nActions].actionParameter = param;
        nActions++;
      }
    }
    
    int length() const { return nActions; }
    const Action *getAction(int i) const { return &aActions[i]; }
};

// Actor - Base class for all actors
class Actor { 
  public:
    int id;
    // lastActionTime tracks the last time an action was performed
    // for the purpose of tracking repeated actions.
    // The first action of a repeated group will always arrive without 
    // the repeat bit set - so lastActionTime can be properly initialized
    // at that time.
    int lastActionTime;
    
  public:
    virtual void init() {}
    // Reset is called when new triggers are set.
    // An actor should reset stored variable values to initial state when reset is called.
    virtual void reset() {}
    // assessAction determines whether or not the action should be performed.
    // If repeat is false the action is always performed (by calling doAction).
    // If repeat is true the action is only performed if the repeat interval
    // since the last performance of the action has passed.
    // This structure allows individual actions to have unique repeat-interval logic.
    virtual void assessAction(long param, int repeat);
    virtual void doAction(long param) = 0;
    // checkAction is used to turn actions off
    // e.g. to turn off a relay a short time after the action.
    // Also used for mouse nudge actions
    virtual void checkAction() {}
};

// Actors - A collection of Actor objects
class Actors {
  private:
    Actor *apActors[MAX_ACTORS];
    int nActors;
    
  public:
    Actors() {nActors = 0;}
    void init();
    void reset();
    
    void addActor(Actor *pActor) {
      if (nActors < MAX_ACTORS) {
        apActors[nActors++] = pActor;
      }
    }
    
    void doActions(const ActionData *);
};

// === Actor sub-classes === //
class Relay: public Actor {
  private:
    int pin;
    long actionStartTime;
  
  public:
    Relay(int i, int p) {
      id = i;
      pin = p;
      actionStartTime = 0;
    }
    void init();
    void reset() {
      actionStartTime = 0;
    }
    void doAction(long param);
    void checkAction();
};

class Buzzer: public Actor {
  private:
    int pin;
    
  public:
    Buzzer(int i, int p) {
      id = i;
      pin = p;
    }
    void init();
    void doAction(long param);
};



// Mouse movement states - for NUDGE actions
#define MOUSE_MOVING_UP     1
#define MOUSE_MOVING_DOWN   2
#define MOUSE_MOVING_LEFT   3
#define MOUSE_MOVING_RIGHT  4
#define MOUSE_STILL         5

// MouseControl button options
#define MC_LEFT_CLICK  1
#define MC_RIGHT_CLICK 2
#define MC_PRESS       3
#define MC_RELEASE     4

class MouseControl: public Actor {
  private:
    // Variables for managing nudge actions
    char verticalMouseState;
    char horizontalMouseState;

    // Mouse vertical and horizontal motion can be happening at
    // the same time - so we need two repeat timers.
    unsigned int lastMouseVerticalMove;
    unsigned int lastMouseHorizontalMove;
    unsigned char repeatCount;
    unsigned char jumpSize;

  public:
    MouseControl() {
      verticalMouseState = MOUSE_STILL;
      horizontalMouseState = MOUSE_STILL;
    }
    void reset() {
      verticalMouseState = MOUSE_STILL;
      horizontalMouseState = MOUSE_STILL;
    }
    void assessAction(long param, int repeat);
    void doAction(long param);
    void checkAction();
    virtual void mc_move(int x, int y) = 0;
    virtual void mc_button(int val) = 0;
};

class KeyboardControl: public Actor {
  public:
    KeyboardControl() {}
    void doAction(long param);
    virtual void kc_write(char character) = 0;
};

class HIDMouse: public MouseControl {
  private:
    
  public:
    HIDMouse(int i) {
      id = i;
    }
    void mc_move(int x, int y);
    void mc_button(int val); 
};

class HIDKeyboard: public KeyboardControl {
  public:
    HIDKeyboard(int i) {
      id = i;
    }
    void kc_write(char character);
};


class BTMouse: public MouseControl {
  private: 
    BTMouseCtl *pMouse;
    
  public:
    BTMouse(int i) {
      id = i;
    }
    void init();
    void mc_move(int x, int y);
    void mc_button(int val); 
};

class BTKeyboard: public KeyboardControl {
  private:
    SoftwareSerial *pBlueHID;
    
  public:
    BTKeyboard(int i) {
      id = i;
    }
    void init();
    void kc_write(char character);
};

class IRTV: public Actor {
  private:
    
  public:
    IRTV(int i) {
      id = i;
    }
    void init();
    void doAction(long param);
};
#endif
