
var sensors = [new Sensor(0,[new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none","")]),
				new Sensor(1,[new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none","")]),
				new Sensor(2,[new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none","")]),
				new Sensor(3,[new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none","")]),
				new Sensor(4,[new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none",""),
							 new Trigger(50,"rising","none","")])];
							 


//find the list of all com ports
var ports;



//populate list of Serial ports on load
window.addEventListener('load',function(){
	chrome.serial.getDevices(function(arr) {
		ports = arr;
		displayPorts();
	});
	for(var i=0;i<sensors.length;i++){
		drawSensor(sensors[i]);
	}
});

// n is sensor number, i is thresh number, val is the new value
function updateThreshold(n,i,val){
	sensors[n].triggers[i].value = val;
	document.getElementById('tThresh' + n + "_" + i).value = val;
	document.getElementById('thresh' + n + "_" + i).value = val;
}

function updateSensorValues(incoming){
	var vals = incoming.split(",");
	for(var i = 0;i < vals.length; i++){
		document.getElementById('sensorValue'+i).value = vals[i];
	}
}

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
	
	div.appendChild(buttonGroup);
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
	
	//progress bar to display the incoming value from the sensor
	var sensProg = document.createElement('progress');
	sensProg.setAttribute('id','sensorValue'+sens.num);
	sensProg.setAttribute('max','1024');
	sensProg.setAttribute('value','500');
	head.appendChild(sensProg);
	
	var threshContainer = document.createElement('div');
	threshContainer.setAttribute('id','threshContainer'+sens.num);
	threshContainer.setAttribute('class','hidden');
	
	for(var i=0;i<numTrigs;i++){
		var trig = document.createElement("div");
		
		//range slider for threshold
		var slide = document.createElement("input");
		slide.setAttribute("type", "range");
		slide.setAttribute("value", sens.triggers[i].value);
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
		textThresh.setAttribute('value',sens.triggers[i].value);
		textThresh.setAttribute('min','0');
		textThresh.setAttribute('max','1024');
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
		
		response.value = sens.triggers[i].response;	
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
		
	} else if (r == "digital"){
		detail = document.createElement("select");
		var opt = document.createElement("option");
		opt.setAttribute("value","pulse");
		opt.appendChild(document.createTextNode("Pulse"));
		detail.appendChild(opt);
		
		var opt = document.createElement("option");
		opt.setAttribute("value","step");
		opt.appendChild(document.createTextNode("Step"));
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