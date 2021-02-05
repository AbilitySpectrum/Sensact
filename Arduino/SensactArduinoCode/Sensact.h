// --------------------------------------
// Sensors.h
// --------------------------------------
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * 
    This file is part of the Sensact Arduino software.

    Sensact Arduino software is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sensact Arduino software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this Sensact Arduino software.  
    If not, see <https://www.gnu.org/licenses/>.   
 * * * * * * * * * * * * * * * * * * * * * * * * * * * */ 
 
#ifndef SENSACT_H
#define SENSACT_H
#include <Arduino.h>

// timeDiff function.  Defined in Actions.cpp
unsigned int timeDiff(unsigned int now, unsigned int prev);

// #define MEMCHECK 1
#ifdef MEMCHECK
struct brkPoints {
  int atStart;
  int sensorsAlloc;
  int sensorsInit;
  int actorsAlloc;
  int actorsInit;
  int triggersInit;
};

extern int *__brkval;
extern brkPoints BreakPoints;
#endif

// Various Constants used throughout the code //

// === Protocol Values === //
// -- Commands and Block Headers -- //
#define VERSION         "4.7"
#define REPORT_MODE       'Q'
#define RUN_SENSACT       'R'
#define START_OF_SENSOR_DATA 'S'
#define START_OF_TRIGGER_BLOCK 'T'
#define REQUEST_TRIGGERS  'U'
#define GET_VERSION       'V'
#define KEYBOARD_CMD      'W'
#define MOUSE_SPEED       'Y'
#define MIN_COMMAND  'Q'
#define MAX_COMMAND  'W'

// -- Data block separators -- //
#define TRIGGER_START  't'
#define TRIGGER_END    'z'
#define END_OF_BLOCK   'Z'

// -- Value encoding -- //
#define NUMBER_MASK 0x60
#define ID_MASK     0x40
#define CONDITION_MASK '0'
#define BOOL_TRUE   'p'
#define BOOL_FALSE  'q'

// === Timing Constants - all in ms === //
#define REPORTING_INTERVAL  200  // Interval between reports of signal levels
#define READING_INTERVAL     10  // Interval between reading signals in run mode
#define REFRACTORY          800  // Interval within which the output will not re-trigger. (unused in this version)
#define PULSE_WIDTH         500  // Output pulse width - for relays
#define DEFAULT_REPEAT_INTERVAL 250 // Repeat interval for most things.
#define MOUSE_REPEAT_INTERVAL   100 // Time between repeats of mouse move actions.

// === Special Action ID === //
// This action is actually performed in the trigger code.
#define CHANGE_SENSOR_STATE 10  // Must not conflict with any action state defined in Actions.cpp

// === LED Values === //
#define LED_RED    5
#define LED_GREEN  6
#define LED_BLUE   4  // Different for Hub 2.0

// === Sensor Pins === //
#define SENSACT_IN1A A0
#define SENSACT_IN1B A1
#define SENSACT_IN2A A2
#define SENSACT_IN2B A3
#define SENSACT_IN3A A4
#define SENSACT_IN3B A5

// === Action Pins === //
#define SENSACT_IR_OUT  9
#define SENSACT_BUZZER  10
// - Modified for netCle Hub 2.0
// #define SENSACT_RELAY_1 11
#define SENSACT_RELAY 12

// ==== Some Limits === //
#define MAX_TRIGGERS 15         // Maximum number of triggers allowed - 15 bytes each.
#define MAX_ACTIONS 10            // Maximum number of actions allowed per trigger check - 5 bytes each.
#define MAX_SENSORS 10            // 2 bytes each
#define MAX_ACTORS  11           // 2 bytes each

#endif
