
function Trigger(level, trigType, responseType, detail){
	this.level = level;
	this.event = trigType;
	this.response = responseType;
	this.blueDetail = 65;
	this.keyDetail = 65;
	this.mouseDetail = 0;
	this.IRDetail = "";
};

function Sensor(num, triggers){
	this.num = num;
	this.triggers = triggers;
	
	/* Takes all of the data from the sensor and turns it onto a comma separated string */
	this.toString = function(){
		var out = "";
		
		//loop for each trigger
		for(var i = 0; i < triggers.length; i++){
			out += triggers[i].level + "," + triggers[i].event + "," + 
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
		}
		
		return out.substring(0,out.length-1);
	};
	
	this.updateSensor = function(data){
		if(data.length != this.triggers.length * 4){
			console.log("unexpected packet length");
			return;
		}
		
		for(var i = 0;i < triggers.length;i++){
			this.triggers[i].level = parseInt(data[i*4]);
			triggers[i].event = parseInt(data[i*4+1]);
			triggers[i].response = parseInt(data[i*4+2]);
			
			triggers[i].blueDetail = 65;
			triggers[i].keyDetail = 65;
			triggers[i].mouseDetail = 0;
			triggers[i].IRDetail = "";
			switch(triggers[i].response){
				case 3:
					triggers[i].blueDetail = parseInt(data[i*4+3]);
					if(triggers[i].blueDetail > 126 || triggers[i].blueDetail < 32){
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
		}

	};
};

function makeConfigPackage(sensArr){
	var out = "0,"; //'0' is necessary to tell the arduino that the following is a config package
	for (var i = 0; i < sensArr.length;i++){
		out += sensArr[i].toString() + ",";
	}
	return out.substring(0,out.length-1) + "\n";
}

function readConfigPackage(data, sensArr){
	if(data.length != sensArr.length*sensArr[0].triggers.length*4){
		console.log("unexpected config package size");
		return;
	}
	
	for(var i=0;i < sensArr.length; i++){
		sensArr[i].updateSensor(data.slice(i*sensArr[i].triggers.length*4, i*sensArr[i].triggers.length*4 + 8));
	}
}
