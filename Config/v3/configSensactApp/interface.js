// JavaScript Document
// interface.js

function startup() {
	var bypassConnection = false; // Make it true to bypass the connection phase.
								  // Useful when in testing/development mode.
	if (bypassConnection) {  
		// Turn off transitions - to speed things up.
		document.getElementById("buttons").style.transition = 'none';
		document.getElementById("connection").style.transition = 'none';
		document.getElementById("mainContent").style.transition = 'none';
		connectionComplete();
		return;
	}
	
    if ("WebSocket" in window) {
        webSocket.connect();  // Calls connectionComplete when the connection is completed.
    } else {
        // The browser doesn't support WebSocket
        webSocket.status(1, "Sorry, WebSocket is NOT supported by your Browser!");
        webSocket.status(2, "You will need to use a different browser.");
    }	
};

// Called from WebSocket when the connection is complete.
function connectionComplete() {
	getVersion();   // After the version is determined this will 
					// also set up the sensor and action lists.

	// A transition in CSS will make the connection box slide up out of the window
	// after a short delay.
	document.getElementById("connection").style.top = "-200px";
	// After that the buttons panel will appear.
	document.getElementById("buttons").style.left = "3px";
	document.getElementById("mainContent").style.opacity = "1";
	
	createSensorBlocks();
}

// --- Button Functions --- //
function getTriggers() {
	webSocket.send(REQUEST_TRIGGERS);
}

function setTriggers() {
	sendTriggersToSensact();
}

function readSensors() {
	showMeters();
	webSocket.send(REPORT_MODE);
}

function runSensact() {
	hideMeters();
	webSocket.send(RUN_SENSACT);
}

function saveTriggers() {
	var saveDiv = document.getElementById("savediv");
	writeTriggersToSaveDiv();
	saveDiv.style.display = "block";
}

function closeSave() {
	var saveDiv = document.getElementById("savediv");
	saveDiv.style.display = "none";
}

function restoreTriggers() {
	var input = document.getElementById("restoreInput");
	input.value = "";
	var restoreDiv = document.getElementById("restorediv");
	restoreDiv.style.display = "block";
}

function disconnect() {
	webSocket.restart();
}

// --- CREATE SENSOR BLOCKS --- //
function createSensorBlocks() {
	var mainContentDiv = document.getElementById("mainContent");
	var senLen = sensors.length;
	for(var i=0; i<senLen; i++) {
		mainContentDiv.appendChild( newSensorBlock(sensors[i]) );	
	}
}

function newSensorBlock(s) {
	
	// === Create the Elements ===
	var enclosingDiv = document.createElement("div");
	
	// iconDiv div will hold a triangle showing whether the div is opened or not
	var iconDiv = newDiv("icon");
	
	var sensorHead = newH1("sensorHead", 0, s.name);s
	sensorHead.xxHasTriggers = false;
	
	var sensorGroup = newDiv("sensorGroup");
	sensorGroup.style.display = "none";
	
	var triggerDiv = newDiv("triggerDiv", "triggerDiv" + s.id);

	var buttonDiv = newButtonDiv(s, triggerDiv, sensorHead);
	

	// === Create the Structure ===
	// <enclosingDiv>
	//		<iconDiv>
	//		<sensorHead>
	//		<sensorGroup>
	//			<buttonDiv>
	//				...
	//			<triggerDiv>
	
	enclosingDiv.appendChild(iconDiv);
	enclosingDiv.appendChild(sensorHead);
	enclosingDiv.appendChild(sensorGroup);
	
	sensorGroup.appendChild(buttonDiv);
	sensorGroup.appendChild(triggerDiv);
	
	// === Define the Actions ===
	// On click toggle the div contents from invisible to visible
	// and change the triangle
	sensorHead.onclick = function() {
		if (sensorGroup.style.display == "block") {
			sensorGroup.style.display = "none";
			if (sensorHead.xxHasTriggers) {
				iconDiv.style.background = "url(triangles.png) -15px -45px";
			} else {
				iconDiv.style.background = "url(triangles.png) -15px -15px";
			}
		} else {
			sensorGroup.style.display = "block";
			if (sensorHead.xxHasTriggers) {
				iconDiv.style.background = "url(triangles.png) 0px -45px";
			} else {
				iconDiv.style.background = "url(triangles.png) 0px -15px";
			}
		}			
	}
	
	// On hover change the colors of the text and triangle.
	sensorHead.onmouseenter = function() {
		if (sensorHead.xxHasTriggers) {
			sensorHead.style.color = "#003300";
			if (sensorGroup.style.display == "block") {
				iconDiv.style.background = "url(triangles.png) 0px -45px";
			} else {
				iconDiv.style.background = "url(triangles.png) -15px -45px";
			}
		} else {
			sensorHead.style.color = "#0066FF";
			if (sensorGroup.style.display == "block") {
				iconDiv.style.background = "url(triangles.png) 0px -15px";
			} else {
				iconDiv.style.background = "url(triangles.png) -15px -15px";
			}
		}
	}
	
	sensorHead.onmouseleave = function() {
		if (sensorHead.xxHasTriggers) {
			sensorHead.style.color = "#006600";
			if (sensorGroup.style.display == "block") {
				iconDiv.style.background = "url(triangles.png) 0px -30px";
			} else {
				iconDiv.style.background = "url(triangles.png) -15px -30px";
			}
		} else {
			sensorHead.style.color = "white";
			if (sensorGroup.style.display == "block") {
				iconDiv.style.background = "url(triangles.png) 0px 0px";
			} else {
				iconDiv.style.background = "url(triangles.png) -15px 0px";
			}
		}
	}
	
	// iconDiv click and hover actions are the same as for text.
	iconDiv.onclick = sensorHead.onclick;
	iconDiv.onmouseenter = sensorHead.onmouseenter;
	iconDiv.onmouseleave = sensorHead.onmouseleave;
		
	return enclosingDiv;
}

// Create the controls for a sensor
function newButtonDiv(s, triggerDiv, sensorHead) {
	// === Create the Elements ===
	var buttonDiv = newDiv("buttonGroup");
	
	var addTriggerBtn = newButton ("sensControl", 0, "Add Trigger");

	var viewp = newPara("sensControl", 0, "View:");
	var viewDiv = newDiv("sensControl");
	
	var basicLabel = newLabel("sensControl", "basicRadio" + s.id, "Basic");
	var basicRadioBtn = newRadio("sensControl", "basicRadio" + s.id, "view" + s.id, true);
	var fullLabel = newLabel("sensControl", "fullRadio" + s.id, "Full");
	var fullRadioBtn = newRadio("sensControl", "fullRadio" + s.id, "view" + s.id, false);
	
	// === Create the Structure ===
	//	<buttonDiv>
	//		<addTriggerBtn>
	//		<viewp>
	//		<viewDiv>
	//			<basicLabel>
	//			<basicRadioBtn>
	//			<fullLabel>
	//			<fullRadioBtn>
	//		<lockValuesLabel>  - optional
	//		<lockBox>		   - optional
	buttonDiv.appendChild(addTriggerBtn);
	buttonDiv.appendChild(viewp);
	buttonDiv.appendChild(viewDiv);

	viewDiv.appendChild(basicLabel);
	viewDiv.appendChild(basicRadioBtn);	
	viewDiv.appendChild(fullLabel);	
	viewDiv.appendChild(fullRadioBtn);	
	
	// === Define the Actions ===
	// Create a new trigger.
	addTriggerBtn.onclick = function() {
		var trigger = Triggers.newTrigger(s);
		var newTrigger = createTriggerUI(triggerDiv, trigger, fullRadioBtn.checked);
		triggerDiv.appendChild(newTrigger);
		sensorHead.xxHasTriggers = true;
		sensorHead.onmouseleave();
	}
	
	// Turn on basic view
	basicRadioBtn.onchange = function() {
		turnBasicViewOn(triggerDiv, fullRadioBtn);
	}
	
	// Turn on full view
	fullRadioBtn.onchange = function() {
		var nodes = triggerDiv.getElementsByClassName("extras");
		for(var i=0; i<nodes.length; i++) {
			nodes[i].style.display = "block";
		}
	}
		
	// === Optional Elements ===	
	// If continuous add elements to lock values.
	if (s.isContinuous) {
		var lockLabel = newLabel( "lockLabel sensControl", "lockbox" + s.id, "Lock Values:");	
		var lockBox = newCheckbox("lockBox sensControl", "lockbox" + s.id, false); 

		buttonDiv.appendChild(lockLabel);
		buttonDiv.appendChild(lockBox);
		
		lockBox.onchange = function() {
			if (this.checked == true) {
				var nodes = triggerDiv.querySelectorAll("input[type=range]");
				if (nodes.length > 0) {
					setAllNodesEqual( triggerDiv, nodes[0].value );
				}
			}
		}
	}
	
	return buttonDiv;
}


// -- Start of turnBasicViewOn -- //
function turnBasicViewOn(triggerDiv, fullRadio) {
	// First see if any extra values are non-default values.
	var nodesAreDefaults = true;  // Innocent until proven guilty
	var nodes = triggerDiv.getElementsByClassName("reqstate");
	for(var i=0; i<nodes.length; i++) {
		if (nodes[i].value != DEFAULT_STATE) {
			nodesAreDefaults = false;
			break;
		}
	}
	if (nodesAreDefaults) {
		nodes = triggerDiv.getElementsByClassName("actstate");
		for(var i=0; i<nodes.length; i++) {
			if (nodes[i].value != DEFAULT_STATE) {
				nodesAreDefaults = false;
				break;
			}
		}
	}
	if (nodesAreDefaults) {
		nodes = triggerDiv.getElementsByClassName("delay");
		for(var i=0; i<nodes.length; i++) {
			if (nodes[i].value != 0) {
				nodesAreDefaults = false;
				break;
			}
		}
	}
	if (nodesAreDefaults) {
		nodes = triggerDiv.getElementsByClassName("repeat");
		for(var i=0; i<nodes.length; i++) {
			if (nodes[i].checked != false) {
				nodesAreDefaults = false;
				break;
			}
		}
	}
	if (! nodesAreDefaults) {
		var modal = document.getElementById("twoBtnModal");
		var modalp = document.getElementById("twoBtnModalp");
		var okBtn = document.getElementById("twoBtnOK");
		var cancelBtn = document.getElementById("twoBtnCancel");
		
		modalp.innerHTML = "All advanced values will be force to defaults.<br/>" +
		"Do you want to continue?";
		
		okBtn.onclick = function() { forceDefaults(modal, triggerDiv); }
		cancelBtn.onclick = function() { cancelChange(modal, fullRadio); }
		
		modal.style.display = "block";
		return;
	}
	
	// Checks and corrections are complete.  We can finally hide the extra elements.
	var nodes = triggerDiv.getElementsByClassName("extras");
	for(var i=0; i<nodes.length; i++) {
		nodes[i].style.display = "none";
	}	
}

function cancelChange(modal, fullRadio) {
	fullRadio.checked = true;
	modal.style.display = "none";
}

function forceDefaults(modal, triggerDiv) {
	modal.style.display = "none";
	
	var nodes = triggerDiv.getElementsByClassName("reqstate");
	for(var i=0; i<nodes.length; i++) {
		if (nodes[i].value != DEFAULT_STATE) {
			nodes[i].value = DEFAULT_STATE;
			nodes[i].onchange();
		}
	}
	
	nodes = triggerDiv.getElementsByClassName("actstate");
	for(var i=0; i<nodes.length; i++) {
		if (nodes[i].value != DEFAULT_STATE) {
			nodes[i].value = DEFAULT_STATE;
			nodes[i].onchange();
		}
	}

	nodes = triggerDiv.getElementsByClassName("delay");
	for(var i=0; i<nodes.length; i++) {
		if (nodes[i].value != 0) {
			nodes[i].value = 0;
			nodes[i].onchange();
		}
	}

	nodes = triggerDiv.getElementsByClassName("repeat");
	for(var i=0; i<nodes.length; i++) {
		if (nodes[i].checked != false) {
			nodes[i].checked = false;
			nodes[i].onchange();
		}
	}
	
	var nodes = triggerDiv.getElementsByClassName("extras");
	for(var i=0; i<nodes.length; i++) {
		nodes[i].style.display = "none";
	}	
}
// -- End of turnBasicViewOn & related code -- //

// Check to see which sensors have triggers and change the sensor text
// to show this.  Called whenever triggers are changed.
// Currently the text of active sensors is underlined - might try something else later.
function showActiveSensors() {
	var nodes = document.getElementsByClassName("sensorHead");
	for(var i=0; i<nodes.length; i++) {
		var parent = nodes[i].parentNode;
		var tnodes = parent.getElementsByClassName("trigger");
		nodes[i].xxHasTriggers = (tnodes.length > 0);
		nodes[i].onmouseleave();
	}
}

// -- createTriggerUI --- //

// The next 400 or so lines of code create all the labels and input
// widgets needed to support a trigger, and connect the trigger actions
// to the trigger object.
// tdiv - the <div> to which the trigger will be attached.
// t - the Trigger object
// fullView - boolean.  True if it should be created in full view.
function createTriggerUI(tdiv, t, fullView) {

	var form = newForm("trigger");
	var deleteBtn = newSpan("deleteBtn", 0, 'x');

	// --- SENSOR SETTINGS --- //
	// A fieldset for sensor-related elements.
	var sensorFieldset = newFieldset("Sensor Settings");
		
	if (t.sensor.isContinuous) {
		// Create a div for continuous values
		var continuousDiv = createContinuousDiv(t);
		sensorFieldset.appendChild(continuousDiv);
		
	} else {  // Sensor supplies discrete values.	
		// Create a div for discrete values 
		var discreteDiv = createDiscreteDiv(t);
		sensorFieldset.appendChild(discreteDiv);
	}	

	var sensExtraDiv = newSensorExtras(t);
	sensExtraDiv.style.display = fullView ? "block" : "none";
		
	sensorFieldset.appendChild(sensExtraDiv);
	
	// --- End of Sensor Fieldset

		
	// --- Action Settings --- //
	// A fieldset for action-related elements.
	var actionFieldset = newFieldset("Action Settings");
	
	// Action - pick list
	var actionLabel = newLabel(0, "actSelect" + t.id, "Action:");	
	var actionSelect = newActionSelect(t);
	
	// Action parameters - depends on Action
	// The UI for action parameters is created by the action.optionFunc()
	var actionParamDiv = newDiv(0, "actionParamDiv" + t.id);
	var actionParamUI = t.action.optionFunc(t);
	
	var actExtraDiv = newActionExtrasDiv(t);
	actExtraDiv.style.display = fullView ? "block" : "none";

	//	<actionFieldSet>
	//		<actionLabel>
	//		<actionSelect>
	//		<actionParamDiv>
	//			<actionParamUI>
	//		<actionExtraDiv>
	actionFieldset.appendChild(actionLabel);
	actionFieldset.appendChild(actionSelect);
	actionFieldset.appendChild(actionParamDiv);
	actionParamDiv.appendChild(actionParamUI);
	actionFieldset.appendChild(actExtraDiv);
	
	// Delete button action //
	deleteBtn.onclick = function() {
		var modal = document.getElementById("twoBtnModal");
		var modalp = document.getElementById("twoBtnModalp");
		var okBtn = document.getElementById("twoBtnOK");
		var cancelBtn = document.getElementById("twoBtnCancel");
		
		modalp.innerHTML = "Delete this trigger?";
		
		okBtn.onclick = function() { completeDelete(modal, tdiv, form, t); }
		cancelBtn.onclick = function() { cancelDelete(modal); }
		
		modal.style.display = "block";
		return;		
	}
	
	// Enclose all in a form	
	form.appendChild(deleteBtn);
	form.appendChild(sensorFieldset);
	form.appendChild(actionFieldset);
	
	return form;
}

function cancelDelete(modal) {
	modal.style.display = "none";
}

function completeDelete(modal, tdiv, tform, t) {
	tdiv.removeChild(tform);
	Triggers.deleteTrigger(t);
	modal.style.display = "none";
	showActiveSensors();
}


// === CONTINUOUS DIV === //
// This section assembles the continuous values area.
// This includes the slider, meter, high/low buttons and their labels.
// These are all related and some functions need to know about all of them.
// This is accomplished using closure.

var blockRecursion = false;  // Used to prevent infinite recursion when lockbox is checked.

function createContinuousDiv(t) {
	// Step 1.  Gather the parts
	var continuousDiv = newDiv("continuous", "continuous" + t.id);

	// InnerDiv is used to align the slider and the meter
	var innerDiv = newDiv("innerDiv");
		
	// Slider for continuous values & label
	var sliderLabel = newLabel(0, "valSlider" + t.id, "Value:");
	var slider = newSlider("slider sliderGroup", "valSlider" + t.id, 
						   t.sensor.minval, t.sensor.maxval, t.getSliderValue());
	slider.xxTrigger = t;
	
	// Meter for showing reported sensor values
	var meter = newMeter("meter sliderGroup", 0, t.sensor.minval, t.sensor.maxval, 0);
	meter.xxTrigger = t;
	if (metersAreHidden) {
		meter.style.display = "none";
	}
		
	// Invert checkbox & label
	var invertLabel = newLabel("invert", "invert" + t.id, "Invert");
	var invertBox = newCheckbox("invertBox", "invert" + t.id, false);
	if (t.condition == TRIGGER_ON_LOW) {
		invertBox.checked = true;
	} else {
		invertBox.checked = false;
	}
		
	// Step 2. Assemble the parts
	continuousDiv.appendChild(sliderLabel);
	continuousDiv.appendChild(innerDiv);
		innerDiv.appendChild(meter);
		innerDiv.appendChild(slider); // Slider must be 2nd so it goes on top of the meter.
	continuousDiv.appendChild(invertLabel);
	continuousDiv.appendChild(invertBox);
	
	// Step 3. Handle the actions
	slider.onchange = function() {
		t.setSliderValue(this.value);
		updateMeter(meter, slider);
		if (blockRecursion) return;
		var lockbox = document.getElementById("lockbox" + t.sensor.id);
		if (lockbox.checked == true) {
			var tDiv = document.getElementById("triggerDiv" + t.sensor.id);
			blockRecursion = true;
			setAllNodesEqual( tDiv, t.getValue() );
			blockRecursion = false;
		}
	}
	invertBox.onchange = function() {
		if (invertBox.checked) {
			t.condition = TRIGGER_ON_LOW;
		} else {
			t.condition = TRIGGER_ON_HIGH;
		}
		slider.value = t.getSliderValue();
		updateMeter(meter, slider);
	}
	updateMeter(meter, slider); // Do initial meter setting.
	
	return continuousDiv;
}

// If lockbox is checked this routine finds all sliders for the same sensor
// and sets all of them to 'val'.  Value is the true value, which may need to be
// inverted for some triggers.
function setAllNodesEqual( tDiv, trueValue ) {
	var nodes = tDiv.querySelectorAll("input[type=range]");
	for (var i=0; i<nodes.length; i++) {
		var t = nodes[i].xxTrigger;
		t.setValue(trueValue);
		nodes[i].value = t.getSliderValue();
		nodes[i].onchange();  // This will update the meter for this slider.
	}
}

// Update the meter so that it will change color when sensor value would trigger it.
function updateMeter(meter, slider) {
	var svalue = parseInt(slider.value);
	if (svalue == slider.max) svalue -= 2;
	meter.high = svalue;
	meter.optimum = svalue - 1;
	meter.low = meter.min;
}

function updateMeterValues(stream) {
	try {
		if (stream.getChar() != START_OF_SENSOR_DATA) {
			throw("Invalid start of sensor data");
		}
		
		var sensorCount = stream.getNum(4);
		for(var i=0; i<sensorCount; i++) {
			var id = stream.getID(2);
			var value = stream.getNum(4); 
			
			var tdiv = document.getElementById("triggerDiv" + id);
			if (!tdiv) return;  // tdiv may disappear if page is refreshed.
			var nodes = tdiv.getElementsByClassName("meter");
			for(var j=0; j < nodes.length; j++) {
				if (nodes[j].xxTrigger.condition == TRIGGER_ON_LOW) {
					nodes[j].value = nodes[j].min + (nodes[j].max - value);
				} else {
					nodes[j].value = value;
				}
			}
		}
		
		if (stream.getChar() != END_OF_BLOCK) {
			throw("Invalid end of sensor data");
		}
		
	} catch(err) {
		alert("Sensor data transfer: " + err);
	}
}

var metersAreHidden = true;
function showMeters() {
	metersAreHidden = false;
	var nodes = document.getElementsByClassName("meter");
	for(var j=0; j < nodes.length; j++) {
		nodes[j].style.display = "block";
	}
}

function hideMeters() {
	metersAreHidden = true;
	var nodes = document.getElementsByClassName("meter");
	for(var j=0; j < nodes.length; j++) {
		nodes[j].style.display = "none";
	}
}

// === END OF CONTINUOUS DIV === //

// --- Create Discrete Div --- //
function createDiscreteDiv(t) {
	var minval = t.sensor.minval;
	var maxval = t.sensor.maxval;

	// === Create Items ===
	var discreteDiv = document.createElement("div");
	
	var numLabel = newLabel(0, "discrete" + t.id, "Value:");
	
	var value;
	if (t.triggerValue == minval) {
		value = '';
	} else {
		value = String.fromCharCode(t.triggerValue);
	}	
	var numInput = newTextInput("smallnum", "discrete" + t.id, value);
	
	// === Define Structure ===
	discreteDiv.appendChild(numLabel);
	discreteDiv.appendChild(numInput);
		
	// === Define Actions ===
	numInput.onchange = function() {
		var val = this.value.charCodeAt(0);
		if (val >= minval && val <= maxval) {
			t.triggerValue = val;
		} else {
			this.value = String.fromCharCode(t.triggerValue);
		}
	}
	
	return discreteDiv;
}

// --- Sensor Extras --- //
// Basically, just the required state field.
function newSensorExtras(t) {
	var sensExtraDiv = newDiv("extras");
	
	// Required State (0-15)
	var reqdStateLabel = newLabel(0, "rstate" + t.id, "Required State:");
	var label = addToolTip(reqdStateLabel, "A setting of 0 means any state will match.");
	var reqdState = newNumericInput("value smallnum reqstate", "rstate" + t.id, 0, 15, t.reqdState);
	
	//	<sensExtraDiv>
	//		<label>
	//		<reqdState>
	sensExtraDiv.appendChild(label);
	sensExtraDiv.appendChild(reqdState);

	reqdState.onchange = function() {
		var val = this.value;  
		if (val >= 0 && val <= 15) {
			t.reqdState = val;
		} else {
			this.value = t.reqdState;
		}
	}
	
	return sensExtraDiv;
}

// --- ACTION ITEMS --- //

function newActionSelect(t) {
	var actSelect = document.createElement("select");
	actSelect.id = "actSelect" + t.id;
	
	var actLen = actions.length;
	for( var i = 0; i < actLen; i++) {
		var action = actions[i];
		var op = document.createElement("option");
		op.innerHTML = action.name;
		op.value = action.name;
		op.xxAction = action;
		if (action === t.action) {
			op.selected = true;
		} else {
			op.selected = false;
		}
		actSelect.appendChild(op);
	}
	
	actSelect.onchange = function() {
		// Set action choice
		var choice = this.options[this.selectedIndex];
		var oldAction = t.action;
		t.action = choice.xxAction;
		
		// See if a default action parameter needs to be set
		if (t.action.defaultVal) {
			t.actionParam = t.action.defaultVal;
		}

		// Generate and display action parameters
		var paramDiv = document.getElementById("actionParamDiv" + t.id);
		paramDiv.replaceChild(t.action.optionFunc(t), paramDiv.firstChild);
	}
	
	return actSelect;
}
		
function newActionExtrasDiv(t) {
	// === Create Elements ===
	var actExtraDiv = newDiv("extras");

	// Action State (0-15)
	var actionStateLbl = newLabel(0, "astate" + t.id, "Action State:");
	var actionState = newNumericInput("smallnum actstate", "astate" + t.id, 1, 15, t.actionState);

	var delayLbl = newLabel(0, "delay" + t.id, "Delay:");
	var delayField = newNumericInput("largenum delay", "astate" + t.id, 0, 30000, t.delay);
	
	var repeatLbl = newLabel(0, "repeat" + t.id, "Repeat:");
	var repeatBox = newCheckbox("repeat", "repeat" + t.id, t.repeat);

	// === Define Structure ===
	actExtraDiv.appendChild(actionStateLbl);
	actExtraDiv.appendChild(actionState);
	actExtraDiv.appendChild(delayLbl);
	actExtraDiv.appendChild(delayField);
	actExtraDiv.appendChild(repeatLbl);
	actExtraDiv.appendChild(repeatBox);

	// === Define Actions ===
	actionState.onchange = function() {
		var val = this.value;  
		if (val >= 0 && val <= 15) {
			t.actionState = val;
		} else {
			this.value = t.actionState;
		}
	}

	delayField.onchange = function() {
		var val = this.value;  
		if (val < 0) {
			this.value = 0;
		} else if (val > 30000) {
			this.value = 30000;
		}
		t.delay = this.value;
	}

	repeatBox.onchange = function() {
		t.repeat = this.checked;
	}
	
	return actExtraDiv;
}

// --- Reload triggers.  --- //
// Called when a new set of triggers is loaded from sensact
function reloadTriggers() {
	var senCount = sensors.length;
	var i;
	
	// Throw out all UI triggers
	for(i=0; i<senCount; i++) {
		var sensor = sensors[i];
		var tdiv = document.getElementById("triggerDiv" + sensor.id);
		tdiv.innerHTML = '';
	}
	
	// Recreate them with the new triggers.
	var trigCount = Triggers.length();
	for(i=0; i<trigCount; i++) {
		var trig = Triggers.get(i);
		var sensor = trig.sensor;
		var tdiv = document.getElementById("triggerDiv" + sensor.id);
		var fullBtn = document.getElementById("fullRadio" + sensor.id);
		var newTrigger = createTriggerUI(tdiv, trig, fullBtn.checked);
		tdiv.appendChild(newTrigger);
	}
	
	showActiveSensors();
}


