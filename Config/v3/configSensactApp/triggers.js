"use strict";

// ----------------------------------------------
// This file defines the main data structures used to hold the data.
// Also the functions required to covert this data into a stream (to be sent
// to SensAct) and convert from a stream are defined here.
//
// No user interfact elements are defined here.
// ----------------------------------------------

// === PROTOCOL VALUES === //
// -- Command and Block Headers -- //
var REPORT_MODE   = 'Q';
var RUN_SENSACT   = 'R';
var START_OF_SENSOR_DATA  = 'S';
var START_OF_TRIGGER_BLOCK  ='T';
var REQUEST_TRIGGERS = 'U';
var GET_VERSION = 'V';
var MIN_COMMAND = 'Q';
var MAX_COMMAND = 'V';

// -- Data block separators -- //
var TRIGGER_START = 't';
var TRIGGER_END   = 'z';
var END_OF_BLOCK  = 'Z';

// -- Value encoding -- //
var NUMBER_MASK	= 0x60;
var ID_MASK		= 0x40;
var CONDITION_MASK = '0';
var BOOL_TRUE	= 'p';
var BOOL_FALSE	= 'q';


var sensActVersion;
var sensors = [];
var actions = [];

function getVersion() {
	// TBD
	createSensorList();
	createActionList();
}

// -- Sensor - defines a 'Sensor' class -- //
//    This holds sensor names and properties
function Sensor(i, n, minval, maxval, c) {
	this.id = i;
    this.name = n;
	this.minval = minval;
	this.maxval = maxval; 
    this.isContinuous = c;   // If true the sensor delivers continuous data.
						   // If false the sensor delivers descrete values.
}

// -- sensors - holds a list of all possible sensors -- //


// createSensorList runs once -after the version number is discovered.
// It creates the list of sensors.
function createSensorList() {
	sensors.push( new Sensor(1, "Input 1A", 0, 1023, true) );
	sensors.push( new Sensor(2, "Input 1B", 0, 1023, true) );
	sensors.push( new Sensor(3, "Input 2A", 0, 1023, true) );
	sensors.push( new Sensor(4, "Input 2B", 0, 1023, true) );
	sensors.push( new Sensor(5, "Input 3A", 0, 1023, true) );
	sensors.push( new Sensor(6, "Input 3B", 0, 1023, true) );
	sensors.push( new Sensor(7, "Keyboard", 0, 255, false) );
	sensors.push( new Sensor(8, "Accel-X", -16000, 16000, true) );
	sensors.push( new Sensor(9, "Accel-Y", -16000, 16000, true) );
	sensors.push( new Sensor(10, "Accel-Z", -16000, 16000, true) );
	sensors.push( new Sensor(11, "Gyro-X", -7000, 7000, true) );
	sensors.push( new Sensor(12, "Gyro-Y", -7000, 7000, true) );
	sensors.push( new Sensor(13, "Gyro-Z", -7000, 7000, true) );
}

function getSensorByID(id) {
	for(var i=0; i<sensors.length; i++) {
		if (sensors[i].id == id) {
			return sensors[i];
		}
	}
	return null;
}

// -- Action - defines an 'Action' class -- //
//    This holds action names and properties
function Action(i, n, oFunc, dft) {
	this.id = i;
    this.name = n;
	this.optionFunc = oFunc;
	this.defaultVal = dft;		// Default actionParam value - may be undefined
}


// See ACTION PARAMETERS section below to understand 3rd parameter.
function createActionList() {
	actions.push( new Action(0, "None",         noOption) );
	actions.push( new Action(1, "Relay A",      noOption) );
	actions.push( new Action(2, "Relay B",      noOption) );
	actions.push( new Action(3, "Bluetooth",    keyOption, 65) ); // 'A'
	actions.push( new Action(4, "HID Keyboard", keyOption, 65) ); // 'A'
	actions.push( new Action(5, "HID Mouse",    mouseOption, MOUSE_UP) );
	actions.push( new Action(6, "Joystick",     noOption) );
	actions.push( new Action(7, "Buzzer",       BuzzOption, (400 << 16) + 250 ) );
	actions.push( new Action(8, "IR",           IROption, TV_ON_OFF) );
}

function getActionByID(id) {
	for(var i=0; i<actions.length; i++) {
		if (actions[i].id == id) {
			return actions[i];
		}
	}
	return null;
}

// Condition values
var TRIGGER_ON_LOW   = 1;		// Trigger when below the threshold value
var TRIGGER_ON_HIGH  = 2;		// Trigger when above the threshold value
var TRIGGER_ON_EQUAL = 3;		// Trigger when equal to the threshold value

var nextTriggerID = 1;	// Insures each trigger has a unique ID.
					// This is used to make sure various HTML elements also have a unique ID.
					// The ID has no other meaning.  

var DEFAULT_STATE = 1; // The default state
// A trigger is created with reasonable default values
function Trigger(sensor) {
	this.id = nextTriggerID++;
	if (sensor) {
	   this.setSensor(sensor);	// The sensor which triggers the action
	}							// This will not change after trigger creation.
	this.reqdState = DEFAULT_STATE;		// The state (1 to 15) required for the action to trigger.
							// 0 means Any State.
	this.triggerValue 		// The value of the sensor which will cause the trigger.
	this.condition;
	this.delay = 0;			// A time in ms - max 30000 (2 bytes)
	this.repeat = false;		// Boolean
	this.action = actions[0];// The action to be performed
	this.actionParam = 0;	// A 4-byte value.  May encode multiple parameters.
	this.actionState = DEFAULT_STATE;	// The state (1 to 15) to be set if the action triggers	
}

Trigger.prototype.setSensor = function(s) {
	this.sensor = s;
	// The default value of triggerValue and condition depends on the type of sensor.
	if (s.isContinuous) {
		this.triggerValue = (s.minval + s.maxval) / 2;
		this.condition = TRIGGER_ON_HIGH;
	} else {
		this.triggerValue = s.minval;
		this.condition = TRIGGER_ON_EQUAL;
	}
}

Trigger.prototype.toStream = function(ostream) {
	ostream.putChar('\n');
	ostream.putChar(TRIGGER_START);
	ostream.putID(this.sensor.id, 2);
	ostream.putID(this.reqdState, 1);
	ostream.putNum(this.triggerValue, 2);
	ostream.putCondition(this.condition);
	ostream.putID(this.action.id, 2);
	ostream.putID(this.actionState, 1);
	ostream.putNum(this.actionParam, 4);
	ostream.putNum(this.delay, 2);
	ostream.putBoolean(this.repeat);
	ostream.putChar(TRIGGER_END);
}

Trigger.prototype.fromStream = function(stream) {
	if (stream.getChar() != TRIGGER_START) {
		throw "Invalid start of trigger";
	}
	var sensorID = stream.getID(2);
	var sensor = getSensorByID(sensorID);
	if (sensor == null) {
		throw ("Invalid Sensor ID");
	}
	this.setSensor(sensor);
	this.reqdState = stream.getID(1);
	this.triggerValue = stream.getNum(4);
	this.condition = stream.getCondition();
	var actionID = stream.getID(2);
	this.action = getActionByID(actionID);
	if (this.action == null) {
		throw ("Invalid Action ID");
	}
	this.actionState = stream.getID(1);
	this.actionParam = stream.getNum(8);
	this.delay = stream.getNum(4);
	this.repeat = stream.getBoolean();
	if (stream.getChar() != TRIGGER_END) {
		throw "Invalid end of trigger";
	}
}

// Trigger functions for handling value and inverted value
// getValue() and setValue() get and set the actual value.
// getSliderValue() and setSliderValue() invert the value if the condition is TRIGGER_ON_LOW
Trigger.prototype.getValue = function() {
	return this.triggerValue;
}

Trigger.prototype.setValue = function(value) {
	this.triggerValue = value;
}

Trigger.prototype.getSliderValue = function() {
	if (this.condition == TRIGGER_ON_LOW) {
		var min = this.sensor.minval;
		var max = this.sensor.maxval;
		return (min + (max - this.triggerValue));
	} else {
		return this.triggerValue;
	}
}

Trigger.prototype.setSliderValue = function(value) {
	if (this.condition == TRIGGER_ON_LOW) {
		var min = this.sensor.minval;
		var max = this.sensor.maxval;
		this.triggerValue = (min + (max - value));
	} else {
		this.triggerValue = value;
	}
}
	
// The Triggers object controls all access to the list of triggers.
// Well - there is no real enforcement of this in Javascript, but this
// is the intention.
var Triggers = {
	triggerList: [],
	
	length: function() {
		return this.triggerList.length;
	},
	
	get: function(i) {
		return this.triggerList[i];
	},
	
	// replace triggers is called after new triggers are received.
	replaceTriggers: function(newList) {
		this.triggerList = newList;
	},
	
	newTrigger: function(sensor) {
		var t = new Trigger(sensor);
		this.triggerList.push(t);
		return t;
	},
		
	deleteTrigger: function(t) {
		if (t) {
			for(var i=0; i < this.triggerList.length; i++) {
				if (t === this.triggerList[i]) {
					this.triggerList.splice(i, 1);
					break;
				}
			}
		}
	}
};

// --- SEND and RECEIVE LOGIC --- //
function sendTriggersToSensact() {
	outputStream.init( toSensact );
	putTriggers(outputStream);
}

function writeTriggersToSaveDiv() {
	outputStream.init( writeToSaveDiv );
	putTriggers(outputStream);
}

function writeToSaveDiv(data) {
	var savePre = document.getElementById("savepre");
	savePre.innerHTML = data;
}

function toSensact(data) {
	webSocket.send(data);
}

function putTriggers(ostream) {
	ostream.putChar(START_OF_TRIGGER_BLOCK);
	var ntrig = Triggers.length();
	ostream.putNum(ntrig, 2);
	
	for(var i=0; i<ntrig; i++) {
		Triggers.get(i).toStream(ostream);
	}
	ostream.putChar(END_OF_BLOCK);  // Write end of transmission block byte
	ostream.flush();
}

function loadTriggers(stream) {
	var tmpTriggers = [];
	
	try {
		readTriggers(tmpTriggers, stream);
		
		// Now that data has been safely received ...
		Triggers.replaceTriggers(tmpTriggers); //This updates the storage
		reloadTriggers();		// This updates the UI
		
	} catch(err) {
		alert("Trigger load failed: " + err);
	}
}
	
function readTriggers (tmpTriggers, stream) {
	if (stream.getChar() != START_OF_TRIGGER_BLOCK) {
		throw("Invalid start of transmission");
	}
	var triggerCount = stream.getNum(4);
	for(var i=0; i<triggerCount; i++) {
		var t = new Trigger(); 
		t.fromStream(stream);
		tmpTriggers.push(t);
	}
	if (stream.getChar() != END_OF_BLOCK) {
		throw("Invalid end of transmission");
	}
}


// --- ACTION PARAMETERS SECTION --- //
// -- OK, so I lied.  I am putting a bit of UI in here.  
//  This is possible UI for various action options.
// Functions receive a trigger object.

// One tricky thing here is that javascript stores the "value" of
// a numeric parameter as a string.  So it needs to be converted to a
// number when stored into actionParam.

// --- NO PARAMETERS --- //
var noOption = function(t) {
	return document.createElement("div");
}

// --- KEYBOARD PARAMETERS --- //
// Parameter is a single character.
var keyOption = function(t) {
	var div = newDiv("actionOption");
	var txtLabel = newLabel(0, "keyTextArea" + t.id, "Character:");
	var txt = newTextInput(0, "keyTextArea" + t.id, String.fromCharCode(t.actionParam));
	txt.maxlength = 1;
	
	div.appendChild(txtLabel);
	div.appendChild(txt);	
	
	txt.onchange = function() {
		t.actionParam = this.value.charCodeAt(0);
	}
	
	return div;
}

// --- MOUSE PARAMETERS --- //
var MOUSE_UP = 1;
var MOUSE_DOWN = 2;
var MOUSE_LEFT = 3;
var MOUSE_RIGHT = 4;
var MOUSE_CLICK = 5;
var NUDGE_UP = 10;
var NUDGE_DOWN = 11;
var NUDGE_LEFT = 12;
var NUDGE_RIGHT = 13;

function ValueLabelPair(v, l) {
	this.value = v;
	this.label = l;
}

var mice = [
	new ValueLabelPair(MOUSE_UP, "Mouse Up"),
	new ValueLabelPair(MOUSE_DOWN, "Mouse Down"),
	new ValueLabelPair(MOUSE_LEFT, "Mouse Left"),
	new ValueLabelPair(MOUSE_RIGHT, "Mouse Right"),
	new ValueLabelPair(MOUSE_CLICK, "Mouse Click"),
	new ValueLabelPair(NUDGE_UP, "Nudge Up"),
	new ValueLabelPair(NUDGE_DOWN, "Nudge Down"),
	new ValueLabelPair(NUDGE_LEFT, "Nudge Left"),
	new ValueLabelPair(NUDGE_RIGHT, "Nudge Right")
];

var mouseOption = function(t) {
	var div = newDiv("actionOption");
	
	var txtLabel = newLabel(0, "mousesel" + t.id, "Mouse Action:");
	var sel = document.createElement("select");
	sel.id = "mousesel" + t.id;
	
	var nmice = mice.length;
	for(var i=0; i<nmice; i++) {
		var opt = document.createElement("option");
		opt.innerHTML = mice[i].label;
		opt.value = mice[i].value;
		opt.selected = (t.actionParam == mice[i].value);
		sel.appendChild(opt);
	}
	
	div.appendChild(txtLabel);
	div.appendChild(sel);
	
	sel.onchange = function() {
		var choice = this.options[this.selectedIndex];
		t.actionParam = parseInt(choice.value);
	}
	
	return div;
}

// --- IR PARAMETERS --- //
var TV_ON_OFF = 1;
var VOLUME_UP = 2;
var VOLUME_DOWN = 3;
var CHANNEL_UP = 4;
var CHANNEL_DOWN = 5;

var IRActions = [
	new ValueLabelPair(TV_ON_OFF, "On/Off"),
	new ValueLabelPair(VOLUME_UP, "Volume Up"),
	new ValueLabelPair(VOLUME_DOWN, "Volume Down"),
	new ValueLabelPair(CHANNEL_UP, "Channel Up"),
	new ValueLabelPair(CHANNEL_DOWN, "Channel Down"),
];

var IROption = function(t) {
	var div = newDiv("actionOption");

	var selectLabel = newLabel(0, "irselect" + t.id, "IR Action:");
	var sel = document.createElement("select");
	sel.id = "irselect" + t.id;
	
	var ircount = IRActions.length;
	for(var i=0; i<ircount; i++) {
		var opt = document.createElement("option");
		opt.innerHTML = IRActions[i].label;
		opt.value = IRActions[i].value;
		opt.selected = (t.actionParam == IRActions[i].value);
		sel.appendChild(opt);
	}
	
	div.appendChild(selectLabel);
	div.appendChild(sel);

	sel.onchange = function() {
		var choice = this.options[this.selectedIndex];
		t.actionParam = parseInt(choice.value);
	}
		
	return div;
}

// --- BUZZER PARAMETERS --- //
// Frequency and duration.
var BuzzOption = function(t) {
	var div = newDiv("actionOption");

	var frequencyLabel = newLabel(0, "buzzfreq" + t.id, "Frequency (hz):");
	var frequency = newNumericInput("largenum", "buzzfreq" + t.id, 50, 2000, 50);
	frequency.value = (t.actionParam >> 16) & 0xffff;
	
	var durationLabel = newLabel(0, "buzzdur" + t.id, "Duration (ms):");
	var duration = newNumericInput("largenum", "buzzdur" + t.id, 0, 1000, 0);
	duration.value = t.actionParam & 0xffff;
	
	div.appendChild(frequencyLabel);
	div.appendChild(frequency);
	div.appendChild(durationLabel);
	div.appendChild(duration);
	
	frequency.onchange = function() {
		var val = this.value;
		if (val < 0) {
			this.value = 0;
		} else if (val > 2000) {
			this.value = 2000;
		}
		newBuzzerValue(t, fw, dw);
	}
	
	duration.onchange = function() {
		var val = this.value;
		if (val < 0) {
			this.value = 0;
		} else if (val > 1000) {
			this.value = 1000;
		}
		newBuzzerValue(t, fw, dw);
	}
	
	return div;
}

function newBuzzerValue(t, f, d) {
	var fval = parseInt(f.value);
	var dval = parseInt(d.value);
	t.actionParam = (fval << 16) + dval;
}



