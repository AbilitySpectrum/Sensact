"use strict";

// --- Web Socket class ---
var webSocket = {
	ws: null,
	isARestart: false, 
	portname: null,
	quickConnect: false,
	
	connect: function() {
		// Let us open a web socket
	
		if (this.ws && this.ws.readyState == 1) { // open
			this.status(1, 'Socket connection is complete.');
			return;
		}
		if (this.ws && this.ws.readyState != 3) { //not closed
			setTimeout(function () {
				webSocket.debug('in web_socket() - retrying ');
				webSocket.connect();
			}, 2000);
			return;
		}
		
		if (this.ws && !this.isARestart) {
			this.status(1, 'Connection failed.  Retrying ...');
		} else {
			this.status(1, 'Connecting to socket ...');
		}
		this.ws = new WebSocket("ws://localhost:8989/ws");
	
		this.ws.onopen = function () {
			webSocket.status(1, 'Connection to socket complete');
			webSocket.getList();
		};
	
		this.ws.onmessage = function (evt) {
			webSocket.parseJSPS(evt.data);
		};
	
		this.ws.onclose = function () {
			// websocket is closed.
			webSocket.debug("Connection is closed. Retrying");
			webSocket.connect();
		};
	
		this.ws.onerror = function () {
			webSocket.debug("Connection error.");
			webSocket.ws.close();	// This will cause onclose - which will call web_socket to retry.
    	}
	},  // End connect()
	
	parseJSPS: function (data) {
		this.debug("Got Data - " + data);
		if (data.charAt(0) != '{') {
			this.debug("Not JSON");
			return;
		}
    	var jObj = JSON.parse(data);
    	if (jObj.SerialPorts) { // port list received
			this.onListReceived(jObj.SerialPorts);
		} 
		 else if (jObj.Cmd && jObj.Cmd == "Open") {  // Response to Open request
			this.status(2, "Serial connection to " + jObj.Port + " complete");
			this.operationComplete();
		}
		else if (jObj.P && jObj.D) {
			this.onDataReceived(jObj.D);
		}
	},
	
	restart: function() {
		document.getElementById("portChoices").innerHTML = "";
		var con = document.getElementById("connection");
		var para = con.getElementsByTagName("p");
		para[0].innerHTML = "";
		para[1].innerHTML = "";
		this.isARestart = true;
		this.ws.send("restart"); // Triggers onclose which calls connect()
	},
	
	getList: function() {
		if (!this.ws || this.ws.readyState != 1) {
			this.debug("In getList with an unopened socket!");
			return;
		}
    	this.debug('Requesting port list');
		this.ws.send("list");	
	},
		
	openPort: function(port) {
		if (!this.ws || this.ws.readyState != 1) {
			this.debug("In openPort with an unopened socket!");
			return;
		}
		this.status(2, "Port access requested ...");
		this.portname = port;
		this.ws.send('open ' + this.portname + ' 9600');	
	},
	
	send: function(data) {
		if (!this.ws || this.ws.readyState != 1) {
			this.debug("In send with an unopened socket!");
			return;
		}
		this.ws.send('send ' + this.portname + ' ' + data);
	},

// --- Functions in webSocket below here interact with the UI and the rest of the program. ---/
	debug: function(msg) {
//		document.getElementById("debug").innerHTML += '<br />' + msg;
	},
	
	status: function(location, msg) {
		var con = document.getElementById("connection");
		var para = con.getElementsByTagName("p");
		if (location == 1) {
			para[0].innerHTML = msg;
		} else {
			para[1].innerHTML = msg;
		}
	},
	
	onListReceived: function(serialPorts) {
		var portChoices = document.getElementById("portChoices");
		portChoices.innerHTML = "";
		
		if (serialPorts.length == 0) {
			this.status(2, "No ports available");
			setTimeout(function() {	webSocket.getList(); }, 3000);
			return;
		}
		this.status(2, "Select a port");
	    var jsize = serialPorts.length;
		
		// Check to see if a port is already open
		var foundOpenPorts = false;
		for (var i = 0; i < jsize; i++) {
			if (serialPorts[i].IsOpen) {
				foundOpenPorts = true;
				if (this.quickConnect) {
					// If using quick connect just connect. Ask no questions.
					this.status(2, "Using open port " + serialPorts[i].Friendly);
					this.portname = serialPorts[i].Name;
					this.operationComplete();
					return;
				}
				// otherwise ask if this port is wanted.
				this.status(2, "Use open port " + serialPorts[i].Friendly + "?");
				portChoices.append( this.createOKBtn(serialPorts[i].Name) );
			}				
		}  
		
		if (!foundOpenPorts) {
			for (var i = 0; i < jsize; i++) {
				portChoices.append( 
					this.createPortBtn(serialPorts[i].Friendly, serialPorts[i].Name)
				);
			}
		}
		var refreshx = document.createElement("input");
		refreshx.type = "button";
		refreshx.value = "Refresh";
		refreshx.onclick = function() {webSocket.restart()};
		portChoices.appendChild(refreshx);
	},
	
	// Create the port btn in a closure to capture port name for openPort function.
	createPortBtn: function(friendly, name) {
		var input = document.createElement("input");
		input.type = "button";
		input.value = friendly;
		input.onclick = function() {
			webSocket.openPort( name );
		};
		return input;
	},
	
	createOKBtn: function(name) {
		var input = document.createElement("input");
		input.type = "button";
		input.value = "OK";
		input.onclick = function() {
			webSocket.portname = name;
			webSocket.operationComplete();
		};
		return input;
	},
	
	operationComplete: function() {
		connectionComplete(); // In interface.js
	},
	
	receivedData: '',
	onDataReceived: function(data) {
		this.debug("GOT DATA: " + data);
		this.receivedData += data;
		if (this.receivedData[this.receivedData.length-1] == 'Z') {
			if (this.receivedData[0] == 'T') {
				inputStream.init(this.receivedData);
				loadTriggers(inputStream);
			}
			if (this.receivedData[0] == 'S') {
				inputStream.init(this.receivedData);				
				updateMeterValues(inputStream);
			}
			if (this.receivedData[0] == 'V') {
				setVersion(this.receivedData);
			}
			
			// processData(receivedData);
			this.receivedData = '';
		}
	}
	
}; // --- End webSocket --- //

function doTriggerRestore() {
	var restore = document.getElementById("restoreInput");
	webSocket.onDataReceived(restore.value.trim());
	var restoreDiv = document.getElementById("restorediv");
	restoreDiv.style.display = "none";
}

// === General Purpose Input Stream === //
var inputStream = {
	data: null,
	
	init: function(newData) {
		this.data = newData.split('');
	},
	
	getChar: function() {
		var tmp = this.data.shift();
		// Filter out white space that may have been added for readability.
		while(tmp == '\n' || tmp == '\r' || tmp == ' ') {
			tmp = this.data.shift();
		}
		return tmp;
	},
	
	// Note: 2-byte values may be negative.
	getNum: function(count) {
		var negative = false;
		var value = 0;
		for(var i=0; i< count; i++) {
			var tmp = this.getChar().charCodeAt(0) - NUMBER_MASK;
			if (tmp < 0 || tmp > 15) {
				throw "Invalid Number";
			}
			value = (value << 4) + tmp;
			if ((i == 0) && (tmp & 0x8)) {
				// High order bit is set.  This is a negative number.
				negative = true;
			}
		}
		
		if (negative) {
			if (count == 4) { // 4 nibbles - two bytes
				// We will have a fairly large positive number at this point.
				// Turn it into the correct small number.
				value = value - 0x10000;
			} 
		}
		return value;
	},
	
	getID: function(count) {
		var value = 0;
		for(var i=0; i< count; i++) {
			var tmp = this.getChar().charCodeAt(0) - ID_MASK;
			if (tmp < 0 || tmp > 15) {
				throw "Invalid ID";
			}
			value = (value << 4) + tmp;
		}
		return value;
	},
	
	getCondition: function() {
		var tmp = this.getChar();
		switch(tmp) {
			case '1': 
				return TRIGGER_ON_LOW;
				
			case '2': 
				return TRIGGER_ON_HIGH;
				
			case '3': 
				return TRIGGER_ON_EQUAL;
				
			default:
				throw "Invalid condition";
		}
	},
	
	getBoolean: function() {
		var tmp = this.getChar();
		switch(tmp) {
			case BOOL_TRUE:
				return true;
				break;
			case BOOL_FALSE:
				return false;
				break;
			default:
				throw "Invalid boolean";
				break;
		}
	}	
};

// === General Purpose Output Stream === //
var outputStream = {
  data: null,
  outputFunction: null,
  
  init: function(outFunc) {
	  this.data = [];
	  this.outputFunction = outFunc;
  },
  
  putChar: function(ch) {
	  this.data.push(ch);
  },
  
  putNum: function(n, length) {
	switch(length) { // Length is the number of bytes to send
		case 4:		 // All cases fall through
			this.putChar( String.fromCharCode(((n >> 28) & 0xf) | NUMBER_MASK) );
			this.putChar( String.fromCharCode(((n >> 24) & 0xf) | NUMBER_MASK) );
		case 3:
			this.putChar( String.fromCharCode(((n >> 20) & 0xf) | NUMBER_MASK) );
			this.putChar( String.fromCharCode(((n >> 16) & 0xf) | NUMBER_MASK) );
		case 2:
			this.putChar( String.fromCharCode(((n >> 12) & 0xf) | NUMBER_MASK) );
			this.putChar( String.fromCharCode(((n >> 8) & 0xf) | NUMBER_MASK) );
		case 1:
			this.putChar( String.fromCharCode(((n >> 4) & 0xf) | NUMBER_MASK) );
			this.putChar( String.fromCharCode((n & 0xf) | NUMBER_MASK) );
	}
   },

   putID: function(n, length) {
	switch(length) { // Length is the number of bytes to send
		case 2:
			this.putChar( String.fromCharCode(((n >> 4) & 0xf) | ID_MASK) );
		case 1:
			this.putChar( String.fromCharCode((n & 0xf) | ID_MASK) );
	}
   },
    
   putCondition: function(cond) {
	   this.putChar(cond);
   },
   
   putBoolean: function(b) {
	   if (b) {
		   this.putChar('p');
	   } else {
		   this.putChar('q');
	   }
   },
   
   flush: function() {
	   var output = this.data.join('');
	   this.outputFunction(output);
   }

};

						   