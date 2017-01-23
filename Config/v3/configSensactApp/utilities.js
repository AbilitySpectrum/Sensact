// JavaScript Document
// utilities.ls

// Contains functions to create basic UI elements.
//
// Parameters are:
//		#1 - class name
//		#2 - id - or 0 if no id is needed
//		#3 - other parameters if required.  Value is always last.

// Create a div
function newDiv( className, id ) {
	var div = document.createElement("div");
	if (className) div.className = className;
	if (id) div.id = id;
	return div;
}

// Create a Label
function newLabel( className, targetid, text) {				  
	var label = document.createElement("label");
	if (className) label.className = className;
	label.htmlFor = targetid;
	label.innerHTML = text;
	return label;
}

// Create a header 1
function newH1( className, id, text ) {
	var h1 = document.createElement("h1");
	if (className) h1.className = className;	
	if (id) h1.id = id;
	h1.innerHTML = text;
	return h1;
}

// Create a button
function newButton( className, id, value ) {
	var button = document.createElement("input");
	button.type = "button";
	button.className = className;
	if (id) button.id = id;
	button.value = value;
	return button;
}

// Create a paragraph
function newPara( className, id, text ) {
	var para = document.createElement("p");
	if (className) para.className = className;	
	if (id) para.id = id;
	para.innerHTML = text;
	return para;
}

// Create a radio button
function newRadio( className, id, group, value ) {
	var radio = document.createElement("input");
	radio.type = "radio";
	radio.className = "sensControl";
	radio.id = id
	radio.name = group;
	radio.checked = value;
	return radio;
}

// Create a check box
function newCheckbox( className, id, value ) {
	var box = document.createElement("input");
	box.type = "checkbox";
	box.className = className;
	if (id) box.id = id;
	box.checked = value;
	return box;
}

// Create numeric input box
function newNumericInput( className, id, minval, maxval, value ) {
	var num = document.createElement("input");
	num.type = "number";
	if (className) num.className = className;
	if (id) num.id = id;
	num.min = minval;
	num.max = maxval;
	num.value = value;
	return num;
}

// Create text input box
function newTextInput( className, id, value ) {
	var text = document.createElement("input");
	text.type = "text";
	if (className) text.className = className;
	if (id) text.id = id;
	text.value = value;
	return text;
}	

// Create a field set
function newFieldset( label ) {
	var fieldset = document.createElement("fieldset");
	var legend = document.createElement("legend");
	legend.innerHTML = label;	
	fieldset.appendChild(legend);
	return fieldset;
}

function newForm( className, id ) {
	var form = document.createElement("form");
	if (className) form.className = className;
	if (id) form.id = id;
	return form;
}

function newSpan( className, id, value ) {
	var span = document.createElement("span");
	if (className) span.className = className;
	if (id) span.id = id;
	span.innerHTML = value;
	return span;
}

function newSlider( className, id, minval, maxval, value ) {
	var slider = document.createElement("input");
	slider.type = "range";
	if (className) slider.className = className;
	if (id) slider.id = id;
	slider.min = minval;
	slider.max = maxval;
	slider.value = value;
	
	return slider;
}

function newMeter( className, id, minval, maxval, value ) {
	var meter = document.createElement("meter");
	if (className) meter.className = className;
	if (id) meter.id = id;
	meter.min = minval;
	meter.max = maxval;
	meter.value = value;
	
	return meter;
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


	
	
	
