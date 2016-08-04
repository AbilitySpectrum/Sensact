/*
 * The trigger and sensor class stores all of the data for a config package.
 *
 * In order to simplify the config app, half of the triggering types were removed (rising edge, below level, held below). These types still exist in the arduino code and in the class,
 * but they are hidden in the app by the invert option. If the trigger should invert the signal, then the Falling edge becomes a Rising edge, above level becomes below level, held above becomes held below.
 * Falling edge is 0, Rising edge is 1,
 * Above Level is 2, Below level is 3,
 * held above is 4, held below is 5.
 *
 */


function Trigger(level, trigType, responseType, detail){
	this.invert = false;
	this.level = level; //0-100 threshold
	this.event = trigType; //rising edge, falling edge, above level, etc
	this.response = responseType; //keyboard, mouse, Bluteooth HID, etc
	this.blueDetail = 65; //bluetooth keyboard character
	this.keyDetail = 65;  //keyboard character
	this.mouseDetail = 0; //mouse function
	this.IRDetail = "";   //IR function code
};

function Sensor(num, triggers){
	this.num = num;
	this.triggers = triggers;
	
	/* Takes all of the data from the sensor and turns it onto a comma separated string */
	this.toString = function(){
		var out = "";
		
		//loop for each trigger
		for(var i = 0; i < triggers.length; i++){
			
			if(triggers[i].invert){ //adjust for the inversion
				out += 100 - triggers[i].level + "," + (triggers[i].event + 1);
			}else {
				out += triggers[i].level + "," + triggers[i].event;
			}
			out +=  "," + 
					triggers[i].response + ",";
					
			switch(parseInt(triggers[i].response)){
				case 3:
					out += triggers[i].blueDetail;
					break;
				case 4:
					out += triggers[i].keyDetail;
					break;
				case 5:
					out += triggers[i].mouseDetail;
					break;
				case 7:
					out += triggers[i].IRDetail;
					break;
				default:
					out += "0";
					break;
			};
			out += ","
		};
		
		return out.substring(0,out.length-1);
	};
	
	/*
	 * This function is used to set the sensors from part of a config package
	 */
	this.updateSensor = function(data){
		if(data.length != this.triggers.length * 4){
			console.log("unexpected packet length");
			return;
		};
		
		for(var i = 0;i < triggers.length;i++){
			this.triggers[i].level = parseInt(data[i*4]);
			triggers[i].event = parseInt(data[i*4+1]);
			
			if(triggers[i].event % 2 == 1){ //This is to see if the trigger type is one of the inverted types
				triggers[i].event = triggers[i].event - 1;
				triggers[i].level = 100 - triggers[i].level;
				triggers[i].invert = true;
			}else{
				triggers[i].invert = false;
			};
			triggers[i].response = parseInt(data[i*4+2]);
			
			triggers[i].blueDetail = 65;
			triggers[i].keyDetail = 65;
			triggers[i].mouseDetail = 0;
			triggers[i].IRDetail = "";
			switch(triggers[i].response){
				case 3:
					triggers[i].blueDetail = parseInt(data[i*4+3]);
					if(triggers[i].blueDetail > 126 || triggers[i].blueDetail < 32){ //Ensures that it is a letter
						triggers[i].blueDetail = 65;
					};
					break;
				case 4:
					triggers[i].keyDetail = parseInt(data[i*4+3]);
					if(triggers[i].keyDetail > 126 || triggers[i].keyDetail < 32){
						triggers[i].keyDetail = 65;
					};
					break;
				case 5:
					triggers[i].mouseDetail = parseInt(data[i*4+3]);
					if(triggers[i].mouseDetail > 4 || triggers[i].mouseDetail < 0){
						triggers[i].mouseDetail = 0;
					};
					break;
				case 7:
					triggers[i].IRDetail = parseInt(data[i*4+3]);
					break;
				default:
					break;
			};
		};

	};
};

function makeConfigPackage(sensArr){
	var out = "0,"; //'0' is necessary to tell the arduino that the following is a config package
	out += heldTime.toString() + ",";
	for (var i = 0; i < sensArr.length;i++){
		out += sensArr[i].toString() + ",";
	};
	return out.substring(0,out.length-1) + "\n";
};

function readConfigPackage(data, sensArr){
	if(data.length != sensArr.length*sensArr[0].triggers.length*4 + 1){ // + 1 for the held time
		console.log("unexpected config package size");
		return;
	};
	
	setHeldTime(data[0]);
	
	for(var i=0;i < sensArr.length; i++){
		 sensArr[i].updateSensor(data.slice(i*sensArr[i].triggers.length*4 +1, i*sensArr[i].triggers.length*4 + 8 + 1));
	};
};
