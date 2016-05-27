/* value: threshold value
 * trigType: string with value rising, falling, both, above, or below
 * responseType: string with value keyboard, mouse, digital
 * detail: string with keyboard character, mouse movement (up,down,left,right,left_click,right_click), digital signal (pulse, step)
 */
function Trigger(value, trigType, responseType, detail){
	this.type = trigType;
	this.value = value;
	this.response = responseType;
	this.detail = detail;
};

function Sensor(num, triggers){
	this.num = num;
	this.triggers = triggers;
};

/*
 * Alternative grouping of Sensor. Sensor has multiple modes,
 * each mode will have a response, and the responses can be changed.
 */
function TriggerMode(trigType, responseType, detail){
	this.type = trigType;
	this.response = reponseType;
	this.detail = detail;
}
