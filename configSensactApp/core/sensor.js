
/*
 * TODO: This class needs to be rewritten once we know how many triggers are wanted per sensor.
 * 
 */
function Sensor(num,inv, relA,relB,blutHID,key,click,joy,buzz,trigL,blueDetail,keyDetail,mouseDirec){
	this.num = num;
	this.invert = inv;
	this.relayA = relA;
	this.relayB = relB;
	this.bluetoothHID = blutHID;
	this.keyboard = key;
	this.click = click;
	this.joystick = joy;
	this.buzzer = buzz;
	
	if(trigL > 100){
		trigL = 100;
	}else if (trigL < 0){
		trigL = 0;
	}
	this.trigLevel = trigL;
	this.blueDetail = blueDetail;
	this.keyDetail = keyDetail;
	this.mouseDirec = mouseDirec;
	
	
	//Takes the sensor and creates a string of data to send to the Arduino
	//The boolean data is sent separated by commas, then a space, then the detail data
	// ie("0,0,0,0,0,1,0,1 50,65,65,0")
	this.getSensorCommand = function(){
		var out = "" + (this.invert ? 1: 0).toString() + "," + 
					(this.relayA ? 1:0).toString() + "," + 
					(this.relayB ? 1:0).toString() + "," + 
					(this.bluetoothHID ? 1:0).toString() + "," + 
					(this.keyboard ? 1:0).toString() + "," + 
					(this.click ? 1:0).toString() + "," + 
					(this.joystick ? 1:0).toString() + "," + 
					(this.buzzer ? 1:0).toString();
		
		out += " " + this.trigLevel + "," + this.blueDetail.charCodeAt(0) +
						"," + this.keyDetail.charCodeAt(0) + "," +
							this.mouseDirec;
		
		return out;
	};
	
	//Takes data exactly as created in getSensorCommand and changes the current sensor
	//to mimic the data.
	// ie("0,0,0,0,0,1,0,1 50,65,65,0")
	this.setSensor = function(newSensor){
		var bools = newSensor.split(' ')[0].split(',');
		var details = newSensor.split(' ')[1].split(',');
		
		this.invert = bools[0] == 1? true:false;
		this.relayA = bools[1] == 1? true:false;
		this.relayB = bools[2] == 1? true:false;
		this.bluetoothHID = bools[3] == 1? true:false;
		this.keyboard = bools[4] == 1? true:false;
		this.click = bools[5] == 1? true:false;
		this.joystick = bools[6] == 1? true:false;
		this.buzzer = bools[7] == 1? true:false;
		
		this.trigLevel = parseInt(details[0]);
		this.blueDetail = String.fromCharCode(details[1]);
		this.keyDetail = String.fromCharCode(details[2]);
		this.mouseDirec = parseInt(details[3]);
	};
};
