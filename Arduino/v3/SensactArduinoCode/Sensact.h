// --------------------------------------
// Sensors.h
// --------------------------------------
#ifndef SENSACT_H
#define SENSACT_H

#include <Arduino.h>
// Various Constants used throughout the code //

// === Protocol Values === //
// -- Commands and Block Headers -- //
#define REPORT_MODE       'Q'
#define RUN_SENSACT       'R'
#define START_OF_SENSOR_DATA 'S'
#define START_OF_TRIGGER_BLOCK 'T'
#define REQUEST_TRIGGERS  'U'
#define GET_VERSION       'V'
#define KEYBOARD_CMD      'W'
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

// === LED Pins === //
#define LED_RED    A3
#define LED_GREEN  5
#define LED_BLUE   4

// === Sensor Pins === //
#define SENSACT_IN2   A2
#define SENSACT_IN3   A1

// === Action Pins === //
#define SENSACT_IR_OUT  13
#define SENSACT_BUZZER  10
#define SENSACT_RELAY_1  9
#define SENSACT_RELAY_2 11

// === Timing Constants - all in ms === //
#define REPORTING_INTERVAL  200  // Interval between reports of signal levels
#define READING_INTERVAL     10  // Interval between reading signals in run mode
#define REFRACTORY          800  // Interval within which the output will not re-trigger.
#define PULSE_WIDTH          50  // Output pulse width
#define REPEAT_INTERVAL     100  // Time between repeats of repeated actions.

// ==== Some Limits === //
#define MAX_TRIGGERS 25           // Maximum number of triggers allowed.
#define MAX_ACTIONS 10            // Maximum number of actions allowed per trigger check.
#define MAX_SENSORS 10
#define MAX_ACTORS  10

#endif
