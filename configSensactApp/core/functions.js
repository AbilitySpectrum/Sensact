/*
 * TODO: Create Class to store all values of sensors and can convert all values into a string form
 * 			to be sent over Serial port, method to change string into values for all Sensors
 *
 * TODO: How many sensors? How many Triggers per sensor?
 *
 */


var sensors = [new Sensor(0,false,false,false,false,false,false,false,false,50,"A","A",0),
				new Sensor(1,false,false,false,false,false,false,false,false,50,"A","A",0),
				new Sensor(2,false,false,false,false,false,false,false,false,50,"A","A",0),
				new Sensor(3,false,false,false,false,false,false,false,false,50,"A","A",0)];
							 
//find the list of all com ports
var ports;

//Creates string of all sensor data and sends it to over the serial
//Should be in the controller class
function sendConfigData(){
	var outBool = "";
	var outDetail = "";
	for(var i=0;i<sensors.length;i++){
		var temp = sensors[i].getSensorCommand().split(" ");
		outBool += "," + temp[0];
		outDetail += "," + temp[1];
	}
	return " 0" + outBool + outDetail;
}

//Takes the configuration package from the Sensact and updates the sensors object
//called when a config package is sent from Sensact
function receiveSensactConfig(incoming){
	var data = incoming.split(',');
	console.log(incoming);
	if(data.length%12 != 0){
		console.log('Receive Config Package Error: Not expected package size');
		return;
	}
	var num = data.length/12;
	if(num != sensors.length){
		console.log('Receive Config Package Error: Not expected number of sensors');
		return;
	}
	
	for(var i = 0;i < num; i++){
		var bool = incoming.substring(i*16,i*16+15); //grab all boolean data. Always the same number of characters
		bool += " " + data[num*8 + i*4] + "," + data[num*8 + i*4 + 1] +
				"," + data[num*8 + i*4 + 2] + "," + data[num*8 + i*4 + 3];
		
		sensors[i].setSensor(bool);
	}
	
	console.log(sensors);
	updateSensorWidgets();
}

//populate list of Serial ports on load
window.addEventListener('load',function(){
	chrome.serial.getDevices(function(arr) {
		ports = arr;
		displayPorts();
	});
	for(var i=0;i<sensors.length;i++){
		drawSensor(sensors[i]);
	}
	updateSensorWidgets();
});

// This function updates the data structure threshold values, and the html element values (range bar and spinner)
// n is sensor number, i is thresh number, val is the new value
// called when Sensact sends current sensor readings
function updateThreshold(n,val){
	sensors[n].trigLevel = val;
	document.getElementById('tThresh' + n).value = val;
	document.getElementById('thresh' + n).value = val;
}

// This function updates all widgets to match the sensors object
function updateSensorWidgets(){
	for(var i = 0; i < sensors.length;i++){
		updateThreshold(i,sensors[i].trigLevel);
		document.getElementById('inv' + i).checked = sensors[i].invert? true:false;
		document.getElementById('relA' + i).checked = sensors[i].relayA? true:false;
		document.getElementById('relB' + i).checked = sensors[i].relayB? true:false;
		document.getElementById('blueHID' + i).checked = sensors[i].bluetoothHID? true:false;
		document.getElementById('blueChar' + i).setAttribute('value',sensors[i].blueDetail);
		document.getElementById('keyHID' + i).checked = sensors[i].keyboard? true:false;
		document.getElementById('keyChar' + i).setAttribute('value',sensors[i].keyDetail);
		document.getElementById('click' + i).checked = sensors[i].click? true:false;
		document.getElementById('joy' + i).checked = sensors[i].joystick? true:false;
		document.getElementById('joySel' + i).value = sensors[i].mouseDirec;
		document.getElementById('buzz' + i).checked = sensors[i].buzzer? true:false;
	}
}

//Takes data from the arduino and updates the sensors. 
// incoming is a string with the sensor value separated by commas
function updateSensorValues(incoming){
	var vals = incoming.split(",");
	for(var i = 0;i < vals.length; i++){
		if(i < sensors.length){
			document.getElementById('sensorValue'+i).value = vals[i];
		}
	}
}

// Creates Button panel to allow user to choose a port
function displayPorts(){
	var div = document.createElement('div');
	div.setAttribute('class','ports');
	div.setAttribute('id','portsContainer');
	var body = document.getElementById('sensorContainer').parentNode;
	body.appendChild(div);
	
	div.appendChild(document.createTextNode("Choose the port to connect to:"));
	div.appendChild(document.createElement("br"));
	
	var buttonGroup = document.createElement('div');
	buttonGroup.setAttribute('class','buttonGroup');
	
	for(var i=0;i<ports.length;i++){
		console.log(ports[i]);	
		var butt = document.createElement("button");
		var portName = ports[i].displayName;
		if(portName.length > 20){
			portName = portName.substring(0,21);
		}
		butt.appendChild(document.createTextNode(ports[i].path + " - " + portName));
		butt.setAttribute('class','portButton');
		butt.addEventListener('click',function(){
			var chosenPort = this.innerHTML.split(" ")[0];
			console.log(chosenPort);
			document.getElementById('portsContainer').setAttribute('class','hidden');
			document.getElementById('sensorContainer').setAttribute('class','');
			
			serial.openSerial(chosenPort);
		});
		if(i!=0)
			buttonGroup.appendChild(document.createElement('br'));
		buttonGroup.appendChild(butt);
	}
	
	if(ports.length == 0){
		var label = document.createElement('a');
		label.setAttribute('id','noPorts');
		label.appendChild(document.createTextNode("There are no available ports"));
		buttonGroup.appendChild(label);
	}
	
	div.appendChild(buttonGroup);
}

/* Takes in a sensor and creates the html elements for it
 */
function drawSensor(sens){
	
	var check = document.getElementById("sensor" + sens.num);
	if(check != null){
		return;
	};
	
	var div = document.createElement("div");
	div.setAttribute("id", "sensor" + sens.num);
	var head = document.createElement("h1"); 
	head.appendChild(document.createTextNode("Sensor " + (parseInt(sens.num)+1)));
	head.setAttribute('id','sensHead' + sens.num);
	
	//when the text is clicked it will toggle the trigger container
	head.addEventListener('click',function(){
		var num = this.id.substring(8)
		var container = document.getElementById("threshContainer" + num);
		console.log(container);
		if(container.getAttribute('class').includes('hidden')){
			container.setAttribute('class', 'threshContainer');
		}else{
			container.setAttribute('class','threshContainer hidden');
		}
	});
	div.appendChild(head);
	
	//progress bar to display the incoming value from the sensor
	var sensProg = document.createElement('progress');
	sensProg.setAttribute('id','sensorValue'+sens.num);
	sensProg.setAttribute('max','100');
	sensProg.setAttribute('value','50');
	head.appendChild(sensProg);
	
	var threshContainer = document.createElement('div');
	threshContainer.setAttribute('id','threshContainer'+sens.num);
	threshContainer.setAttribute('class','threshContainer hidden');

	var trig = document.createElement("div");
	trig.setAttribute('class','trigOptions');
	
	//range slider for threshold
	var slide = document.createElement("input");
	slide.setAttribute("type", "range");
	slide.setAttribute("id", "thresh" + sens.num);
	slide.addEventListener("change",function(){
		var temp = this.getAttribute("id").substring(6);
		var n = temp.substring(0,1); //sensor number
		
		updateThreshold(n,this.value);
	});
	threshContainer.appendChild(slide);
	
	//text input for threshold
	var textThresh = document.createElement('input');
	textThresh.setAttribute('id','tThresh' + sens.num);
	textThresh.setAttribute('type','number');
	textThresh.setAttribute('min','0');
	textThresh.setAttribute('max','100');
	textThresh.addEventListener('change',function(){
		var temp = this.getAttribute("id").substring(7);
		var n = temp.substring(0,1); //sensor number
		
		updateThreshold(n,this.value);
	});
	threshContainer.appendChild(textThresh);
	
	/* This code is for the Sensact Config setup */
	
	//invert
	var lab = document.createElement('label');
	var ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','inv' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(3);
		sensors[num].invert = this.checked;	
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode("Invert"));
	trig.appendChild(lab);
	
	trig.appendChild(document.createElement('br'));
	
	//relays
	lab = document.createElement('label');
	ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','relA' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(4);
		sensors[num].relayA = this.checked;		
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode('Relay A'));
	trig.appendChild(lab);
	
	trig.appendChild(document.createElement('br'));
	
	lab = document.createElement('label');
	ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','relB' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(4);
		sensors[num].relayB = this.checked;		
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode('Relay B'));
	trig.appendChild(lab);
	
	trig.appendChild(document.createElement('br'));
	
	//bluetoothHID
	lab = document.createElement('label');
	ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','blueHID' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(7);
		sensors[num].bluetoothHID = this.checked;	
		// var charInput = document.getElementById('blueChar' + num);
		// if(charInput.getAttribute('class') == "hidden"){
			// charInput.setAttribute('class','');
		// }else{
			// charInput.setAttribute('class','hidden');
		// }
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode('BlueT HID'));
	trig.appendChild(lab);
	
	//blueHID char
	ele = document.createElement('input');
	ele.setAttribute('type','text');
	ele.setAttribute('maxlength','1');
	// ele.setAttribute('class', 'hidden');
	ele.setAttribute('id','blueChar' + sens.num);
	ele.addEventListener('change',function(){
		var num = this.getAttribute('id').substring(8);
		sensors[num].blueDetail = this.value;
	});
	trig.appendChild(ele);
	
	trig.appendChild(document.createElement('br'));
	
	//Keyboard
	lab = document.createElement('label');
	ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','keyHID' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(6);
		sensors[num].keyboard = this.checked;	
		// var charInput = document.getElementById('keyChar' + num);
		// if(charInput.getAttribute('class') == "hidden"){
			// charInput.setAttribute('class','');
		// }else{
			// charInput.setAttribute('class','hidden');
		// }
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode('Keyboard'));
	trig.appendChild(lab);
	
	//keyboard char
	ele = document.createElement('input');
	ele.setAttribute('type','text');
	ele.setAttribute('maxlength','1');
	// ele.setAttribute('class', 'hidden');
	ele.setAttribute('id','keyChar' + sens.num);
	ele.addEventListener('change',function(){
		var num = this.getAttribute('id').substring(7);
		sensors[num].keyDetail = this.value;
		
	});
	trig.appendChild(ele);
	
	trig.appendChild(document.createElement('br'));
	
	//click
	lab = document.createElement('label');
	ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','click' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(5);
		sensors[num].click = this.checked;		
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode('Click'));
	trig.appendChild(lab);
	
	trig.appendChild(document.createElement('br'));
	
	//joystick 
	lab = document.createElement('label');
	ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','joy' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(3);
		sensors[num].joystick = this.checked;		
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode('Joystick'));
	trig.appendChild(lab);
	
	//joy option
	var joySelect = document.createElement('select');
	joySelect.setAttribute('id','joySel' + sens.num);
	var opt = document.createElement("option");
	opt.setAttribute("value","0");
	opt.appendChild(document.createTextNode("Arrow L/R"));
	joySelect.appendChild(opt);
	opt = document.createElement("option");
	opt.setAttribute("value","1");
	opt.appendChild(document.createTextNode("Arrow U/D"));
	joySelect.appendChild(opt);
	opt = document.createElement("option");
	opt.setAttribute("value","2");
	opt.appendChild(document.createTextNode("Mouse L/R"));
	joySelect.appendChild(opt);
	opt = document.createElement("option");
	opt.setAttribute("value","3");
	opt.appendChild(document.createTextNode("Mouse U/D"));
	joySelect.appendChild(opt);
	
	joySelect.addEventListener('change',function(){
		var num = this.getAttribute('id').substring(6);
		sensors[num].mouseDirec = this.value;			
	});
	trig.appendChild(joySelect);
	
	trig.appendChild(document.createElement('br'));
	
	//buzzer
	lab = document.createElement('label');
	ele = document.createElement('input');
	ele.setAttribute('type','checkbox');
	ele.setAttribute('id','buzz' + sens.num);
	ele.addEventListener('click',function(){
		var num = this.getAttribute('id').substring(4);
		sensors[num].buzzer = this.checked;		
	});
	lab.appendChild(ele);
	lab.appendChild(document.createTextNode('Buzzer'));
	trig.appendChild(lab);

	
	/* This code is for the SASHIMI configuration setup */
	
	/* 
	//Combo Box for the trigger response
	var response = document.createElement("select");
	response.setAttribute("id", "response" + sens.num + "_" + i);
	response.setAttribute("class", "response");
	//whenever the response box changes, the detail box should be remade
	response.addEventListener("change",function(){
		var temp = this.getAttribute("id").substring(8); //substring(8) will grab the sensor number and trigger number with an underscore inbetween
		var detail = document.getElementById("detail"+temp)
		var p = detail.parentNode;
		var n = temp.substring(0,1); //sensor number
		var i = temp.substring(2,3); //trigger number
		
		sensors[n].triggers[i].response = this.value;
		console.log(this.value);
		
		p.removeChild(detail);
		detail = createDetail(p,this.value,n,i);
		
		p.appendChild(detail);
	});
	
	var opt = document.createElement("option");
	opt.setAttribute("value","none");
	opt.appendChild(document.createTextNode("None"));
	response.appendChild(opt);
	
	opt = document.createElement("option");
	opt.setAttribute("value","keyboard");
	opt.appendChild(document.createTextNode("Keyboard"));
	response.appendChild(opt);
	
	opt = document.createElement("option");
	opt.setAttribute("value","mouse");
	opt.appendChild(document.createTextNode("Mouse"));
	response.appendChild(opt);
	
	opt = document.createElement("option");
	opt.setAttribute("value","digital");
	opt.appendChild(document.createTextNode("Digital"));
	response.appendChild(opt);
	
	trig.appendChild(response);
	
	//Combo box for triggering type
	var trigType = document.createElement("select");
	trigType.setAttribute("id", "trigType" + sens.num + "_" + i);
	trigType.addEventListener("change", function(){
		var temp = this.getAttribute("id").substring(8);
		var n = temp.substring(0,1); //sensor number
		var i = temp.substring(2,3); //trigger number
		
		sensors[n].triggers[i].type = this.value;
		console.log(this.value);
	});
	
	opt1 = document.createElement("option");
	opt1.setAttribute("value","rising");
	opt1.appendChild(document.createTextNode("Rising Edge"));
	trigType.appendChild(opt1);
	
	opt = document.createElement("option");
	opt.setAttribute("value","falling");
	opt.appendChild(document.createTextNode("Falling Edge"));
	trigType.appendChild(opt);
	
	opt = document.createElement("option");
	opt.setAttribute("value","above");
	opt.appendChild(document.createTextNode("Above Level"));
	trigType.appendChild(opt);
	
	opt = document.createElement("option");
	opt.setAttribute("value","below");
	opt.appendChild(document.createTextNode("Below Level"));
	trigType.appendChild(opt);
	
	opt = document.createElement("option");
	opt.setAttribute("value","both");
	opt.appendChild(document.createTextNode("Falling & Rising"));
	trigType.appendChild(opt);
	
	trig.appendChild(trigType);
	
	var detail = createDetail(trig,sens.triggers[i].response,sens.num,i);
	trig.appendChild(detail); 
	
	response.value = sens.triggers[i].response;	*/
	
	threshContainer.appendChild(trig);	
	
	div.appendChild(threshContainer);

	var element = document.getElementById("sensorContainer");
	element.appendChild(div);
}

