
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
};

