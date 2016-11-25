// JavaScript Document

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

// --- CREATE SENSOR BLOCKS --- //
function createSensorBlocks() {
	var trigDiv = document.getElementById("mainContent");
	var senLen = sensors.length;
	for(var i=0; i<senLen; i++) {
		trigDiv.appendChild(newSensorBlock(sensors[i]));	
	}
}

// <enclosingDiv>
//    <head class="sensorHead">
//    <groupDiv class="sensorGroup">
//        <buttonDiv class="sensorButtons">
//            <button><p class="sensControl">Text <div class="sensControl"> <radio > <radio>
//        <triggerDiv class="triggerDiv" id="triggerDiv#s">
function newSensorBlock(s) {
	var enclosingDiv = document.createElement("div");
	
	// icon is a triangle showing whether the div is opened or not
	var icon = document.createElement("div");
	icon.className = "icon";
	enclosingDiv.appendChild(icon);
	
	var head = document.createElement("h1");
	head.innerHTML = s.name;
	head.className = "sensorHead";
	enclosingDiv.appendChild(head);
	
	var groupDiv = document.createElement("div");
	groupDiv.className = "sensorGroup";
	groupDiv.style.display = "none";
	enclosingDiv.appendChild(groupDiv);
	
	// On click toggle the div contents visible or not
	// and change the triangle
	head.onclick = function() {
		if (groupDiv.style.display == "block") {
			groupDiv.style.display = "none";
			icon.style.background = "url(triangles.png) -15px -15px";
		} else {
			groupDiv.style.display = "block";
			icon.style.background = "url(triangles.png) 0px -15px";
		}
	}
	
	// On hover change the colors of the text and triangle.
	head.onmouseenter = function() {
		head.style.color = "#0066FF";
		if (groupDiv.style.display == "block") {
			icon.style.background = "url(triangles.png) 0px -15px";
		} else {
			icon.style.background = "url(triangles.png) -15px -15px";
		}
	}
	
	head.onmouseleave = function() {
		head.style.color = "white";
		if (groupDiv.style.display == "block") {
			icon.style.background = "url(triangles.png) 0px 0px";
		} else {
			icon.style.background = "url(triangles.png) -15px 0px";
		}
	}
	
	// Icon click and hover actions are the same as for text.
	icon.onclick = head.onclick;
	icon.onmouseenter = head.onmouseenter;
	icon.onmouseleave = head.onmouseleave;
	
	var buttonDiv = document.createElement("div");
	buttonDiv.className = "buttonGroup";
	groupDiv.appendChild(buttonDiv);
	
	var newBtn = document.createElement("input");
	newBtn.type = "button";
	newBtn.className = "sensControl";
	newBtn.value = "Add Trigger";
	buttonDiv.appendChild(newBtn);
	
	var viewp = document.createElement("p");
	viewp.className = "sensControl";
	viewp.innerHTML = "View:";
	buttonDiv.appendChild(viewp);
	
	var viewDiv = document.createElement("div");
	viewDiv.className = "sensControl";
	buttonDiv.appendChild(viewDiv);
	
	var basicLabel = document.createElement("label");
	basicLabel.className = "sensControl";
	basicLabel.htmlFor = "basicRadio" + s.id;
	basicLabel.innerHTML = "Basic";
	viewDiv.appendChild(basicLabel);
	
	var basicRadio = document.createElement("input");
	basicRadio.type = "radio";
	basicRadio.id = "basicRadio" + s.id;
	basicRadio.className = "sensControl";
	basicRadio.name = "view" + s.id;
	basicRadio.checked = true;
	viewDiv.appendChild(basicRadio);
	
	var fullLabel = document.createElement("label");
	fullLabel.className = "sensControl";
	fullLabel.htmlFor = "fullRadio" + s.id;
	fullLabel.innerHTML = "Full";
	viewDiv.appendChild(fullLabel);
	
	var fullRadio = document.createElement("input");
	fullRadio.type = "radio";
	fullRadio.id = "fullRadio" + s.id;
	fullRadio.className = "sensControl";
	fullRadio.name = "view" + s.id;
	viewDiv.appendChild(fullRadio);
	
	var triggerDiv = document.createElement("div");
	triggerDiv.className = "triggerDiv";
	triggerDiv.id = "triggerDiv" + s.id;
	groupDiv.appendChild(triggerDiv);
	
	if (s.isContinuous) {
		var lockLabel = document.createElement("label");
		lockLabel.htmlFor = "lockbox" + s.id;
		lockLabel.className = "lockLabel sensControl";
		lockLabel.innerHTML = "Lock Values:";
		buttonDiv.appendChild(lockLabel);
	
		var lockBox = document.createElement("input");
		lockBox.type = "checkbox";
		lockBox.id = "lockbox" + s.id;
		lockBox.className = "lockBox sensControl";
		lockBox.onchange = function() {
			if (this.checked == true) {
				var nodes = triggerDiv.querySelectorAll("input[type=range]");
				if (nodes.length > 0) {
					setAllNodesEqual( triggerDiv, nodes[0].value );
				}
			}
		}
		buttonDiv.appendChild(lockBox);
	}
	
	// Sens Control Functions
	// Create a new trigger.
	newBtn.onclick = function() {
		var trigger = Triggers.newTrigger(s);
		createTriggerUI(triggerDiv, trigger, fullRadio.checked);
	}
	
	// Turn on basic view
	basicRadio.onchange = function() {
		turnBasicViewOn(triggerDiv, fullRadio);
	}
	
	// Turn on full view
	fullRadio.onchange = function() {
		var nodes = triggerDiv.getElementsByClassName("extras");
		for(var i=0; i<nodes.length; i++) {
			nodes[i].style.display = "block";
		}
	}
	
	return enclosingDiv;
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
				nodes[i].value = 0;
				nodes[i].onchange();
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


// -- createTriggerUI --- //

// The next 300 or so lines of code create all the labels and input
// widgets needed to support a trigger, and connect the trigger actions
// to the trigger object.
// tdiv - the form to which the trigger should be attached.
// t - the Trigge object
// fullView - boolean.  True if it should be created in full view.
function createTriggerUI(tdiv, t, fullView) {

	// --- SENSOR SETTINGS --- //
	// A fieldset for sensor-related elements.
	var sensorFieldset = document.createElement("fieldset");
	var sfLegend = document.createElement("legend");
	sfLegend.innerHTML = "Sensor Settings";
	
	sensorFieldset.appendChild(sfLegend);
	
	// Sensor - pick list
//	var items = createSensorSelector(t);
//	sensorFieldset.appendChild(items.Label);
//	sensorFieldset.appendChild(items.Widget);
	
	if (t.sensor.isContinuous) {
		// Create a div for continuous values
		// Needed for positioning of active high/low buttons.
		var continuousDiv = createContinuousDiv(t);
		sensorFieldset.appendChild(continuousDiv);
		
	} else {  // Sensor supplies discrete values.	
		// Create a div for discrete values 
		var discreteDiv = document.createElement("div");
		discreteDiv.id = "discrete" + t.id;
	
		items = createDiscreteValueSelector(t);
		discreteDiv.appendChild(items.Label);
		discreteDiv.appendChild(items.Widget);
		sensorFieldset.appendChild(discreteDiv);
	}	

	var sensExtraDiv = document.createElement("div");
	sensExtraDiv.className = "extras";
	sensExtraDiv.style.display = fullView ? "block" : "none";
	
	// Required State (0-15)
	items = createRequiredStateSelector(t);
	var label = addToolTip(items.Label, "A setting of 0 means any state will match.");
	
	sensExtraDiv.appendChild(label);
	sensExtraDiv.appendChild(items.Widget);
	
	sensorFieldset.appendChild(sensExtraDiv);
	
	// --- End of Sensor Fieldset

		
	// --- Action Settings --- //
	// A fieldset for action-related elements.
	var actionFieldset = document.createElement("fieldset");
	var afLegend = document.createElement("legend");
	afLegend.innerHTML = "Action Settings";	
	actionFieldset.appendChild(afLegend);
	
	// Action - pick list
	items = actionSelect(t);
	actionFieldset.appendChild(items.Label);
	actionFieldset.appendChild(items.Widget);
	
	// Action parameters - depends on Action
	// The UI for action parameters is created by the action.optionFunc()
	var actionParamDiv = document.createElement("div");
	actionParamDiv.id = "actionParamDiv" + t.id;
	actionParamDiv.appendChild(t.action.optionFunc(t));
	actionFieldset.appendChild(actionParamDiv);

	var actExtraDiv = document.createElement("div");
	actExtraDiv.className = "extras";
	actExtraDiv.style.display = fullView ? "block" : "none";

	// Action State (0-15)
	items = createActionStateSelector(t);
	actExtraDiv.appendChild(items.Label);
	actExtraDiv.appendChild(items.Widget);
	
	// Delay - number (0-30000)
	items = createDelaySelector(t);
	actExtraDiv.appendChild(items.Label);
	actExtraDiv.appendChild(items.Widget);
	
	// Repeat - checkbox 
	items = createRepeatCheckbox(t);
	actExtraDiv.appendChild(items.Label);
	actExtraDiv.appendChild(items.Widget);
	
	actionFieldset.appendChild(actExtraDiv);
	
	// Delete button //
	var deletep = document.createElement("span");
	deletep.innerHTML = 'x';
	deletep.className = "deleteBtn";
	deletep.onclick = function() {
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
	var form = document.createElement("form");
	form.className = "trigger";
	form.appendChild(deletep);
	form.appendChild(sensorFieldset);
	form.appendChild(actionFieldset);
	
	tdiv.appendChild(form);
}

function cancelDelete(modal) {
	modal.style.display = "none";
}

function completeDelete(modal, tdiv, tform, t) {
	tdiv.removeChild(tform);
	Triggers.deleteTrigger(t);
	modal.style.display = "none";
}

// --- Widget Creation Routines --//
//   These each create a widget and a label and return them
//   in an object labelled "Label" and "Widget"
// --- Create Sensor Selector drop-down --- //
function createSensorSelector(t) {
	// The selector
	var sel = document.createElement("select");
	sel.id = "senSelect" + t.id;
	sel.className = "value";
	
	var senLen = sensors.length;
	for( var i = 0; i < senLen; i++) {
		var sen = sensors[i];
		var op = document.createElement("option");
		op.innerHTML = sen.name;
		op.value = sen.name;
		op.xxSensor = sen;
		if (sen === t.sensor) {
			op.selected = true;
		} else {
			op.selected = false;
		}
		sel.appendChild(op);
	}
	
	sel.onchange = function() {
		var choice = this.options[this.selectedIndex];
		t.sensor = choice.xxSensor;
		
		// Adjust condition to match sensor type.
		if (t.sensor.isContinuous && t.condition == TRIGGER_ON_EQUAL) {
			t.condition = TRIGGER_ON_HIGH;
		}
		if (!t.sensor.isContinuous && t.condition != TRIGGER_ON_EQUAL) {
			t.condition = TRIGGER_ON_EQUAL;
		}
		displayCorrectValueField(t);
	};
	
	// The label
	var sslabel = document.createElement("label");
	sslabel.htmlFor = "senSelect" + t.id;
	sslabel.innerHTML = "Sensor:";
	
	return {"Label": sslabel, "Widget": sel}; 
}

// --- Create Required State number box --- //
function createRequiredStateSelector(t) {
	// Required State (0-15)
	var items = createNumericSelector("Required State:", 0, 15, "rstate" + t.id);
	var widget = items.Widget;
	widget.className = "value smallnum reqstate";
	widget.value = t.reqdState;
	widget.onchange = function() {
		var val = this.value;  
		if (val >= 0 && val <= 15) {
			t.reqdState = val;
		} else {
			this.value = t.reqdState;
		}
	}
		
	return items;
}

// === CONTINUOUS DIV === //
// This section assembles the continuous values area.
// This includes the slider, meter, high/low buttons and their labels.
// These are all related and some functions need to know about all of them.
// This is accomplished using closure.

var blockRecursion = false;  // Used to prevent infinite recursion when lockbox is checked.

function createContinuousDiv(t) {
	// Step 1.  Gather the parts
	var continuousDiv = document.createElement("div");
	continuousDiv.id = "continuous" + t.id;
	continuousDiv.className = "continuous";

	// InnerDiv is used to align the slider and the meter
	var innerDiv = document.createElement("div");
	innerDiv.className = "innerDiv";
		
	// Slider for continuous values & label
	var items = createValueSlider(t);
	var sliderLabel = items.Label;
	var slider = items.Widget
	
	// Meter for showing reported sensor values
	var meter = createMeter(t);
	
	// Active-Low button & label
	items = createHighLowButton(t, "low");
	var lowBtnLabel = items.Label;
	var lowBtn = items.Widget;
	
	// Active-High button & label
	items = createHighLowButton(t, "high");
	var hiBtnLabel = items.Label;
	var hiBtn = items.Widget;
	
	// Step 2. Assemble the parts
	continuousDiv.appendChild(sliderLabel);
	continuousDiv.appendChild(innerDiv);
		innerDiv.appendChild(meter);
		innerDiv.appendChild(slider); // Slider must be 2nd so it goes on top of the meter.
	continuousDiv.appendChild(lowBtnLabel);
	continuousDiv.appendChild(lowBtn);
	continuousDiv.appendChild(hiBtnLabel);
	continuousDiv.appendChild(hiBtn);
	
	// Step 3. Handle the actions
	slider.onchange = function() {
		t.triggerValue = this.value;
		updateMeter(meter, slider, hiBtn);
		if (blockRecursion) return;
		var lockbox = document.getElementById("lockbox" + t.sensor.id);
		if (lockbox.checked == true) {
			var tDiv = document.getElementById("triggerDiv" + t.sensor.id);
			blockRecursion = true;
			setAllNodesEqual( tDiv, this.value );
			blockRecursion = false;
		}
	}
	lowBtn.onchange = function() {
		updateMeter(meter, slider, hiBtn);
		t.condition = TRIGGER_ON_LOW;
	}
	hiBtn.onchange = function() {
		updateMeter(meter, slider, hiBtn);
		t.condition = TRIGGER_ON_HIGH;
	}
	updateMeter(meter, slider, hiBtn); // Do initial meter setting.
	
	return continuousDiv;
}
	
function createMeter(t) {
	var cvalMeter = document.createElement("meter");
	cvalMeter.min = t.sensor.minval;
	cvalMeter.max = t.sensor.maxval;
	cvalMeter.className = "meter sliderGroup";
	cvalMeter.value = "0";
	if (metersAreHidden) {
		cvalMeter.style.display = "none";
	}
	
	return cvalMeter;
}

// --- Create a slider to display continuous value --- //
function createValueSlider(t) {	
	var cvalSlider = document.createElement("input");
	cvalSlider.type = "range";
	cvalSlider.className = "slider sliderGroup";
	cvalSlider.min = t.sensor.minval;
	cvalSlider.max = t.sensor.maxval;
	cvalSlider.value = t.triggerValue;
	cvalSlider.id = "valSlider" + t.id;
	
	var cvalLabel = document.createElement("label");
	cvalLabel.htmlFor = "valSlider" + t.id;
	cvalLabel.innerHTML = "Value:";
	
	return {"Label" : cvalLabel, "Widget" : cvalSlider};;
}

// --- Create a radio button for active high or active low --- //
function createHighLowButton(t, hl) {
	var theID, setOnChange, baseClass, labelTxt;
	if (hl == "low") {
		theID = "lowCondition" + t.id;
		setOnChange = TRIGGER_ON_LOW;
		baseClass = "activeLow";
		labelTxt = "Active Low";
	} else { // high
		theID = "highCondition" + t.id;
		setOnChange = TRIGGER_ON_HIGH;
		baseClass = "activeHigh";
		labelTxt = "Active High";
	}
	
	var condition = document.createElement("input");
	condition.type = "radio";
	condition.name = "condition";
	condition.id = theID;
	condition.className = baseClass + "Dot";
	if (t.condition == setOnChange) {
		condition.checked = true;
	}
	
	var lcTxt = document.createElement("label");
	lcTxt.className = baseClass;
	lcTxt.htmlFor = theID;
	lcTxt.innerHTML = labelTxt;
	
	return {"Label" : lcTxt , "Widget" : condition };
}

// If lockbox is checked this routine finds all sliders for the same sensor
// and sets all of them to 'val'
function setAllNodesEqual( tDiv, val ) {
	var nodes = tDiv.querySelectorAll("input[type=range]");
	for (var i=0; i<nodes.length; i++) {
		nodes[i].value = val;
		nodes[i].onchange();
	}
}

// Update the meter so that it will change color when sensor value would trigger it.
function updateMeter(meter, slider, hiBtn) {
	var svalue = slider.value;
	if (svalue == slider.max) svalue -= 2;
	if (hiBtn.checked) {		
		meter.high = svalue;
		meter.optimum = svalue - 1;
	} else {
		meter.high = svalue;
		meter.optimum = svalue + 1;
	}
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
				nodes[j].value = value;
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

// --- Create Discrete Value Selector --- //
function createDiscreteValueSelector(t) {
	var minval = t.sensor.minval;
	var maxval = t.sensor.maxval;
	var items = createNumericSelector("Value:", minval, maxval, "discrete" + t.id);
	var widget = items.Widget;
	widget.className = "largenum";
	widget.value = t.triggerValue;
	widget.onchange = function() {
		var val = this.value;
		if (val >= minval && val <= maxval) {
			t.triggerValue = val;
		} else {
			this.value = t.triggerValue;
		}
	}
	return items;
}

// --- ACTION ITEMS --- //

function actionSelect(t) {
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
	
	var actlabel = document.createElement("label");
	actlabel.htmlFor = "actSelect" + t.id;
	actlabel.innerHTML = "Action:";
	
	return {"Label" : actlabel, "Widget" : actSelect};	
}

// --- Create Action State number box --- //
function createActionStateSelector(t) {
	var items = createNumericSelector("Action State:", 1, 15, "astate" + t.id);
	var widget = items.Widget;
	widget.className = "smallnum actstate";
	widget.value = t.actionState;
	widget.onchange = function() {
		var val = this.value;  
		if (val >= 0 && val <= 15) {
			t.actionState = val;
		} else {
			this.value = t.reqdactionStateState;
		}
	}
		
	return items;
}

function createDelaySelector(t) {
	var items = createNumericSelector("Delay:", 0, 30000, "delay" + t.id);
	var widget = items.Widget;
	widget.className = "largenum delay";
	widget.value = t.delay;
	widget.onchange = function() {
		var val = this.value;  
		if (val < 0) {
			this.value = 0;
		} else if (val > 30000) {
			this.value = 30000;
		}
		t.delay = this.value;
	}
	
	return items;
}

function createRepeatCheckbox(t) {
	var repeat = document.createElement("input");
	repeat.type = "checkbox";
	repeat.id = "repeat" + t.id;
	repeat.value = t.repeat;
	repeat.className = "repeat";
	repeat.onchange = function() {
		t.repeat = this.checked;
	}
	
	var rlabel = document.createElement("label");
	rlabel.htmlFor = "repeat" + t.id;
	rlabel.innerHTML = "Repeat:";
	
	return {"Label" : rlabel, "Widget" : repeat};
}
		
// Basic numeric selector creation.
// Calls needs to provide custom className and onchange code
// and custom assignment to/from appropriate trigger member.
function createNumericSelector(labeltxt, themin, themax, labelid) {
	var num = document.createElement("input");
	num.type = "number";
	num.min = themin;
	num.max = themax;
	num.id = labelid;
	
	var label = document.createElement("label");
	label.htmlFor = labelid;
	label.innerHTML = labeltxt;
	return { "Label": label, "Widget": num };
}

// Wraps an element in a tooltip and returns the tooltip div.
function addToolTip(element, tiptext) {
	var tipdiv = document.createElement("div");
	tipdiv.className = "tooltip";
	var tiptxt = document.createElement("span");
	tiptxt.className = "tooltiptext";
	tiptxt.innerHTML = tiptext;
	
	tipdiv.appendChild(element);
	tipdiv.appendChild(tiptxt);

	return tipdiv;
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
		createTriggerUI(tdiv, trig, fullBtn.checked);
	}
}


