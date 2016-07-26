
var sensors = [new Sensor(0,[new Trigger(50,0,0,""),
							 new Trigger(50,0,0,"")]),
				new Sensor(1,[new Trigger(50,0,0,""),
							 new Trigger(50,0,0,"")]),
				new Sensor(2,[new Trigger(50,0,0,""),
							 new Trigger(50,0,0,"")]),
				new Sensor(3,[new Trigger(50,0,0,""),
							 new Trigger(50,0,0,"")]),
				new Sensor(4,[new Trigger(50,0,0,""),
							 new Trigger(50,0,0,"")])];
							 


//find the list of all com ports
var ports;


function loadPorts(){
	chrome.serial.getDevices(function(arr) {
		ports = arr;
		displayPorts();
	});
	for(var i=0;i<sensors.length;i++){
		drawSensor(sensors[i]);
	}
	updateSensorWidgets(sensors);
};


//populate list of Serial ports on load
window.addEventListener('load',loadPorts);



// n is sensor number, i is thresh number, val is the new value
function updateThreshold(n,i,val){
	sensors[n].triggers[i].level = val;
	document.getElementById('tThresh' + n + "_" + i).value = val;
	document.getElementById('thresh' + n + "_" + i).value = val;
}

//when the sensact sends the current sensor values, this function updates the progressbars
function updateSensorValues(incoming){
	var vals = incoming.split(",");
	for(var i = 0;i < vals.length; i++){
		// this is for the progress bar beside the 'Sensor X' headers
		//document.getElementById('sensorValue'+i).value = vals[i]; 

		//this is for the progress bars inside each Action
		document.getElementById('sensorValue'+i+'_0').value = vals[i];
		document.getElementById('sensorValue'+i+'_1').value = vals[i];
	}
}

//changes what detail is displayed based on the response
function displayDetail(n,i,r){
	setHidden(document.getElementById('blueDet' + n + '_' + i),true);
	setHidden(document.getElementById('keyDet' + n + '_' + i),true);
	setHidden(document.getElementById('mouseDet' + n + '_' + i),true);
	setHidden(document.getElementById('IRDet' + n + '_' + i),true);
	
	switch(parseInt(r)){
		case 3:
			setHidden(document.getElementById('blueDet' + n + '_' + i),false);
			break;
		case 4:
			setHidden(document.getElementById('keyDet' + n + '_' + i),false);
			break;
		case 5:
			setHidden(document.getElementById('mouseDet' + n + '_' + i),false);
			break;
		case 7:
			setHidden(document.getElementById('IRDet' + n + '_' + i),false);
			break;
		default:
			break;
	};
}

function toggleHidden(widget){
	var clazz = widget.getAttribute('class').trim();
	if(clazz.includes('hidden')){
		widget.setAttribute('class',clazz.replace('hidden',''));
	}else{
		widget.setAttribute('class',clazz.concat(' hidden'));
	}
}

function setHidden(widget, flag){
	var clazz = widget.getAttribute('class').trim();
	if(flag){
		if(clazz.includes('hidden')){
			return;
		}
		widget.setAttribute('class',clazz.concat(' hidden'));
	}else{
		if(clazz.includes('hidden')){
			widget.setAttribute('class',clazz.replace('hidden',''));
		}		
	}
}

//This function makes the widget display whatever is inside the sensors array
function updateSensorWidgets(sensorArr){
	for(var n = 0; n < sensorArr.length; n++){
		for(var i = 0; i < sensorArr[n].triggers.length; i++){
			updateThreshold(n,i,sensorArr[n].triggers[i].level);
			
			var res = document.getElementById('response' + n + '_' + i)
			res.value = String(sensorArr[n].triggers[i].response);
			document.getElementById('trigEvent' + n + '_' + i).value = String(sensorArr[n].triggers[i].event);
			document.getElementById('blueDet' + n + '_' + i).value = String.fromCharCode(sensorArr[n].triggers[i].blueDetail);
			document.getElementById('keyDet' + n + '_' + i).value = String.fromCharCode(sensorArr[n].triggers[i].keyDetail);
			document.getElementById('mouseDet' + n + '_' + i).value = String(sensorArr[n].triggers[i].mouseDetail);
			document.getElementById('IRDet' + n + '_' + i).value = String(sensorArr[n].triggers[i].IRDetail);
		
			res.dispatchEvent(new Event('change'));
		}
	}
}

function displayPorts(){
	var check = document.getElementById('portsContainer');
	if(check != null){
		console.log("already found a ports container");
		check.parentNode.removeChild(check);
	}
	
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
		if(ports[i].displayName == undefined){ //There are no display names on Macs or Linux
			portName = ports[i].path;
		}
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
	
	div.appendChild(buttonGroup);
	
	var refreshButton = document.createElement('button');
	refreshButton.appendChild(document.createTextNode('Refresh'));
	refreshButton.addEventListener('click',function(){
		loadPorts();
	});
	div.appendChild(refreshButton);
}

/* Takes in a sensor and creates the html elements for it

	<div id = sensorX>
		<h1>Sensor X</h1>
		
		for threshold I
		
		<div>
			<input type = "slide" id = threshX_I></input>
			<select id = responseX_I class="response">
				<option value = "none">None</option>
				<option value = "keyboard">Keyboard</option>
				<option value = "mouse">Mouse</option>
				<option value = "digital">Digital</option>
			</select>
			<select id = trigTypeX_I>
				<option value = "rising">Rising Edge</option>
				...
			</select>
			
			The detail goes here, but changes depending on the response type
		<div>
		
		repeated for each threshold in the sensor
	</div>
 */
function drawSensor(sens){
	
	var check = document.getElementById("sensor" + sens.num);
	if(check != null){
		return;
	};
	
	var numTrigs = sens.triggers.length;
	var div = document.createElement("div");
	div.setAttribute("id", "sensor" + sens.num);
	var head = document.createElement("h1"); 
	head.appendChild(document.createTextNode("Sensor " + (parseInt(sens.num)+1)));
	head.setAttribute('id','sensHead' + sens.num);
	
	//when the text is clicked it will toggle the trigger container
	head.addEventListener('click',function(){
		var container = document.getElementById("threshContainer" + this.id.substring(8));
		console.log(container);
		if(container.getAttribute('class') == 'hidden'){
			container.setAttribute('class', '');
		}else{
			container.setAttribute('class','hidden');
		}
	});
	div.appendChild(head);
	
/* 	//progress bar to display the incoming value from the sensor
	var sensProg = document.createElement('progress');
	sensProg.setAttribute('id','sensorValue'+sens.num);
	sensProg.setAttribute('max','100');
	sensProg.setAttribute('value','50');
	head.appendChild(sensProg); */
	
	var threshContainer = document.createElement('div');
	threshContainer.setAttribute('id','threshContainer'+sens.num);
	threshContainer.setAttribute('class','hidden');
	
	for(var i=0;i<numTrigs;i++){
		var trig = document.createElement("div");
		
		
		var action = document.createElement("a");
		action.appendChild(document.createTextNode("Action " + (i+1)));
		action.setAttribute("class", "actionText");
		trig.appendChild(action);

		var sensProg = document.createElement('progress');
		sensProg.setAttribute('id','sensorValue'+sens.num+'_'+i);
		sensProg.setAttribute('max','100');
		sensProg.setAttribute('value','50');
		trig.appendChild(sensProg);
		
		//range slider for threshold
		var slide = document.createElement("input");
		slide.setAttribute("type", "range");
		slide.setAttribute('min','0');
		slide.setAttribute('max','100');
		slide.setAttribute("value", '0');
		slide.setAttribute("id", "thresh" + sens.num + "_" + i);
		slide.addEventListener("change",function(){
			var temp = this.getAttribute("id").substring(6);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			updateThreshold(n,i,this.value);
		});
		trig.appendChild(slide); 
		
		//text input for threshold
		var textThresh = document.createElement('input');
		textThresh.setAttribute('id','tThresh' + sens.num + "_" + i);
		textThresh.setAttribute('type','number');
		textThresh.setAttribute('value','0');
		textThresh.setAttribute('min','0');
		textThresh.setAttribute('max','100');
		textThresh.addEventListener('change',function(){
			var temp = this.getAttribute("id").substring(7);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			updateThreshold(n,i,this.value);
		});
		trig.appendChild(textThresh);
		
		
		
		//Combo Box for the trigger response
		var response = document.createElement("select");
		response.setAttribute("id", "response" + sens.num + "_" + i);
		response.setAttribute("class", "response");
		//whenever the response box changes, the detail box should be remade
		response.addEventListener("change",function(){
			var temp = this.getAttribute("id").substring(8); //substring(8) will grab the sensor number and trigger number with an underscore inbetween
			var detail = document.getElementById("detail"+temp)
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			sensors[n].triggers[i].response = parseInt(this.value);
			console.log(sensors[n].triggers[i].event);
			displayDetail(n,i,parseInt(this.value));
		});
	
		var opt = document.createElement("option");
		opt.setAttribute("value","0");
		opt.appendChild(document.createTextNode("None"));
		response.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","1");
		opt.appendChild(document.createTextNode("Relay A"));
		response.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","2");
		opt.appendChild(document.createTextNode("Relay B"));
		response.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","3");
		opt.appendChild(document.createTextNode("BT HID"));
		response.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","4");
		opt.appendChild(document.createTextNode("Keyboard"));
		response.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","5");
		opt.appendChild(document.createTextNode("Mouse"));
		response.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","6");
		opt.appendChild(document.createTextNode("Buzzer"));
		response.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","7");
		opt.appendChild(document.createTextNode("Infrared"));
		response.appendChild(opt);
		
		response.value = "0";	
		trig.appendChild(response);
		
		//Combo box for triggering event
		var trigType = document.createElement("select");
		trigType.setAttribute("id", "trigEvent" + sens.num + "_" + i);
		trigType.addEventListener("change", function(){
			var temp = this.getAttribute("id").substring(9);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			sensors[n].triggers[i].event = parseInt(this.value);
			console.log(this.value);
		});
		
		opt1 = document.createElement("option");
		opt1.setAttribute("value","0");
		opt1.appendChild(document.createTextNode("Rising Edge"));
		trigType.appendChild(opt1);
		
		opt = document.createElement("option");
		opt.setAttribute("value","1");
		opt.appendChild(document.createTextNode("Falling Edge"));
		trigType.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","2");
		opt.appendChild(document.createTextNode("Above Level"));
		trigType.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","3");
		opt.appendChild(document.createTextNode("Below Level"));
		trigType.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","4");
		opt.appendChild(document.createTextNode("Held Above"));
		trigType.appendChild(opt);
		
		opt = document.createElement("option");
		opt.setAttribute("value","5");
		opt.appendChild(document.createTextNode("Held Below"));
		trigType.appendChild(opt);
		
		trig.appendChild(trigType);
		
		//Create the Detail widgets
		
		//the text detail will be the char input for the keyboard and
		//the bluetooth HID.
		var det = document.createElement('input');
		det.setAttribute('type','text');
		det.setAttribute('id','blueDet' + sens.num + "_" + i);
		det.setAttribute('maxlength','1');
		det.setAttribute('class','hidden');
		det.addEventListener('change',function(){
			var temp = this.getAttribute('id').substring(7);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			sensors[n].triggers[i].blueDetail = this.value.charCodeAt(0);
			console.log(sensors[n].triggers[i].blueDetail);
		});
		trig.appendChild(det);
		
		var det = document.createElement('input');
		det.setAttribute('type','text');
		det.setAttribute('id','keyDet' + sens.num + "_" + i);
		det.setAttribute('maxlength','1');
		det.setAttribute('class','hidden');
		det.addEventListener('change',function(){
			var temp = this.getAttribute('id').substring(6);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			sensors[n].triggers[i].keyDetail = this.value.charCodeAt(0);
			console.log(sensors[n].triggers[i].keyDetail);
		});
		trig.appendChild(det);
		
		//select detail for the  mouse
		det = document.createElement("select");
		det.setAttribute('class','hidden');
		var opt = document.createElement("option");
		opt.setAttribute("value","0");
		opt.appendChild(document.createTextNode("Move Up"));
		det.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","1");
		opt.appendChild(document.createTextNode("Move Down"));
		det.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","2");
		opt.appendChild(document.createTextNode("Move Left"));
		det.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","3");
		opt.appendChild(document.createTextNode("Move Right"));
		det.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","4");
		opt.appendChild(document.createTextNode("Left Click"));
		det.appendChild(opt);

		
		det.setAttribute("id","mouseDet" + sens.num + "_" + i);
		det.addEventListener("change", function(){
			var temp = this.getAttribute("id").substring(8);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			sensors[n].triggers[i].mouseDetail = parseInt(this.value);
			console.log(this.value);
		});
		trig.appendChild(det);
		
		//Text box for the IR code
		det = document.createElement('input');
		det.setAttribute('type','text');
		det.setAttribute('id','IRDet' + sens.num + "_" + i);
		det.setAttribute('class','largeText hidden');
		det.addEventListener('change',function(){
			var temp = this.getAttribute('id').substring(5);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			sensors[n].triggers[i].IRDetail = this.value;
			console.log(this.value);
		});
		
		trig.appendChild(det);
		
		threshContainer.appendChild(trig);	
	};
	
	div.appendChild(threshContainer);

	var element = document.getElementById("sensorContainer");
	element.appendChild(div);
}


/*
 * Creates the detail element for a given sensor and response type
 * 
 * Takes the parent, a response type, sensor number, and trigger number. Returns an appropriate element
 */
function createDetail(p,r,n,i){
	var detail;
	if(r == "keyboard"){
		detail = document.createElement("input");
		detail.setAttribute("type", "text");
		detail.setAttribute("defaultValue", "Key");
		detail.setAttribute("maxlength","1");
	} else if (r == "mouse"){
		detail = document.createElement("select");
		var opt = document.createElement("option");
		opt.setAttribute("value","moveUp");
		opt.appendChild(document.createTextNode("Move Up"));
		detail.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","moveDown");
		opt.appendChild(document.createTextNode("Move Down"));
		detail.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","moveLeft");
		opt.appendChild(document.createTextNode("Move Left"));
		detail.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","moveRight");
		opt.appendChild(document.createTextNode("Move Right"));
		detail.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","leftClick");
		opt.appendChild(document.createTextNode("Left Click"));
		detail.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","rightClick");
		opt.appendChild(document.createTextNode("Right Click"));
		detail.appendChild(opt);
		
	} else {
		detail = document.createElement("select");
		detail.setAttribute("class","hidden");
	}
	detail.setAttribute("id","detail" + n + "_" + i);
	detail.addEventListener("change", function(){
			var temp = this.getAttribute("id").substring(6);
			var n = temp.substring(0,1); //sensor number
			var i = temp.substring(2,3); //trigger number
			
			sensors[n].triggers[i].detail = this.value;
			console.log(this.value);
		});
	
	detail.dispatchEvent(new Event('change'));
	return detail;
}