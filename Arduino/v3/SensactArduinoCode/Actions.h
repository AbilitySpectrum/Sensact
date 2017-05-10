// -------------------------------------
// Actions.h
// -------------------------------------

#ifndef ActionData_H
#define ActionData_H
#include "Sensact.h"
#include <SoftwareSerial.h>

// Action - Identifies a single action.
struct Action {
  char actionID;
  long actionParameter;
};

// ActionData - A collection of Action objects
class ActionData {
  struct Action aActions[MAX_ACTIONS];
  int nActions;
  
  public:
    void reset() {nActions = 0;}
    void addAction(int id, long param) {
      if (nActions < MAX_ACTIONS) {
        aActions[nActions].actionID = id;
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
    
  public:
    virtual void init() {}
    // Reset is called when new triggers are set.
    // An actor should reset stored variable values to initial state when reset is called.
    virtual void reset() {}
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

class HIDKeyboard: public Actor {
  public:
    HIDKeyboard(int i) {
      id = i;
    }
    void doAction(long param);
};

// Mouse movement states - for NUDGE actions
#define MOUSE_MOVING_UP     1
#define MOUSE_MOVING_DOWN   2
#define MOUSE_MOVING_LEFT   3
#define MOUSE_MOVING_RIGHT  4
#define MOUSE_STILL         5

class HIDMouse: public Actor {
  private:
    // Variables for managing nudge actions
    int verticalMouseState;
    int horizontalMouseState;
    long lastMouseMoveTime;
    
  public:
    HIDMouse(int i) {
      id = i;
      verticalMouseState = MOUSE_STILL;
      horizontalMouseState = MOUSE_STILL;
      lastMouseMoveTime = 0;
    }
    void reset() {
      verticalMouseState = MOUSE_STILL;
      horizontalMouseState = MOUSE_STILL;
      lastMouseMoveTime = 0;      
    }
    void doAction(long param);
    void checkAction();
};

class Bluetooth: public Actor {
  private:
    SoftwareSerial *pBlueHID;
    
  public:
    Bluetooth(int i) {
      id = i;
    }
    void init();
    void doAction(long param);
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
