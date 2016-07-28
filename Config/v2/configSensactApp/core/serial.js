
/*
 * SerialPort class controls all opening, closing, and communication with the Serial port
 */
function SerialPort() {
	this.connectId = -1;
	this.connected = false; //if the serial port is open or not
	
	//will open serial port with the given path
	this.openSerial = function(path){
		chrome.serial.connect(path, function callback(conf){
			console.log(conf);
			serial.connectId = conf.connectionId;
			serial.connected = true;
		});
	};

	//takes in a string and sends it across the serial port
	this.write = function (x){
		if(serial.connected){
			chrome.serial.send(this.connectId, serial.str2ab(x), function callback(stat){
				console.log(stat)
			});
		}
	};

	this.closeSerial = function (){
		console.log(serial.connected);
		if(serial.connected){
			chrome.serial.disconnect(serial.connectId, function callback(info){
				console.log(info);
				serial.connected = false;
			});
		}
	};

	

	this.str2ab = function(str) {
	  var encodedString = unescape(encodeURIComponent(str));
	  var bytes = new Uint8Array(encodedString.length);
	  for (var i = 0; i < encodedString.length; ++i) {
		bytes[i] = encodedString.charCodeAt(i);
	  }
	  return bytes.buffer;
	};
	
	this.ab2str = function(buf) {
	  return String.fromCharCode.apply(null, new Uint8Array(buf));
	};

}


//create the instance of SerialPort that we use
var serial = new SerialPort();

document.getElementById('close').addEventListener('click',serial.closeSerial);
document.getElementById('send').addEventListener('click',function(){
 	serial.write(makeConfigPackage(sensors))
	console.log(makeConfigPackage(sensors)); 
});

//Sends the DEBUG identifier to the Sensact
document.getElementById('debug').addEventListener('click',function(){
	serial.write("7\n");
});

//Requests a config packet from the Sensact
document.getElementById('request').addEventListener('click',function(){
	serial.write("8\n");
});

//Sends the RUN identifier to the Sensact
document.getElementById('run').addEventListener('click',function(){
	serial.write("9\n");
});



//this is to add the incoming data together until a new line is detected
var stringReceived = '';
var onReceiveCallback = function(info) {
    if (info.connectionId == serial.connectId && info.data) {
      var str = serial.ab2str(info.data);
      if (str.charAt(str.length-1) === '\n') {
        stringReceived += str.substring(0, str.length-1);
		// console.log(stringReceived);
		var inArray = stringReceived.split(',');
		
		if(inArray.length == sensors.length){
			updateSensorValues(stringReceived);
		}else if(inArray[0] == "9999"){
			readConfigPackage(inArray.slice(1),sensors);
			// updateSensorWidgets(sensors);
		}else{
			// console.log(stringReceived);
		}
		
        stringReceived = '';
      } else {
        stringReceived += str;
      }
    }
};

chrome.serial.onReceive.addListener(onReceiveCallback);
