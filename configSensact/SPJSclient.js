/*
  SPJSclient.js : 2015-11-28 Sat : ylh
  (C) Ability Spectrum, 2015
  
  DO NOT DISTRIBUTE

  connects to SPJS using websocket
  
  cf https://github.com/johnlauer/serial-port-json-server
  example: http://chilipeppr.com/serialport#com-chilipeppr-widget-serialport-scanHdr
  
  {"Commands" : ["list", "open [portName] [baud] [bufferAlgorithm (optional)]", "send [portName] [cmd]", "sendnobuf [portName] [cmd]", "sendjson {P:portName, Data:[{D:cmdStr, Id:idStr}]}",  "close [portName]", "bufferalgorithms", "baudrates", "restart", "exit", "broadcast [anythingToRegurgitate]", "hostname", "version", "program [portName] [core:architecture:name] [path/to/binOrHexFile]", "programfromurl [portName] [core:architecture:name] [urlToBinOrHexFile]"]} 
  
RESULT from "list"
{
	"SerialPorts": [
		{
			"Name": "/dev/cu.Bluetooth-Incoming-Port",
			"Friendly": "cu.Bluetooth-Incoming-Port",
			"SerialNumber": "",
			"DeviceClass": "",
			"IsOpen": false,
			"IsPrimary": false,
			"RelatedNames": null,
			"Baud": 0,
			"BufferAlgorithm": "",
			"AvailableBufferAlgorithms": [
				"default",
				"tinyg",
				"tinyg_old",
				"tinyg_linemode",
				"tinyg_tidmode",
				"tinygg2",
				"grbl",
				"marlin"
			],
			"Ver": 1.86,
			"UsbVid": "",
			"UsbPid": "",
			"FeedRateOverride": 0
		},
		{
....
*/

var myCanvas;
var input = [],
    nInputs = 4,
    X_MAIN = 50,
    Y_MAIN = 30,
    X_IN_WIDTH = 250,
    X_OUT_WIDTH = 250,
    VSKIP = 170,
    VSPACE = 20,
    H_CHK = 250,
    H_INPUT = 360,
    INVERT = 0,
    RELAY_A = 1,
    RELAY_B = 2,
    BLUETOOTH = 3,
    USB_HID = 4,
    CLICK = 5,
    JOYSTICK = 6,
    BUZZER = 7,
    nOutputs = 8,
    button = [],

    REFRESHPORT = 0,
    NEXTPORT = 1,
    SELECTPORT = 2,
    GETPROFILE = 3,
    SETPROFILE = 4,
    RUNSENSACT = 5,
    nButtons = 6,
    BACKGROUNDCOLOR = '240';

var x, y, button; // readings from the server
var results = [];
var configButton, runButton;
var portsequence = 0;
var portsList = [];

var ws = null,
    currentport = '',
    commandString = '',
    receiveddata = '';

if ("WebSocket" in window) {
    console.log("WebSocket is supported by your Browser!");
} else {
    // The browser doesn't support WebSocket
    console.log("WebSocket NOT supported by your Browser!");
}

web_socket();

function web_socket() {

    // Let us open a web socket

    if (ws && ws.readyState == 1) // open
        return;

    if (ws && ws.readyState != 3) { //not closed
        setTimeout(function () {
            console.log('in web_socket() - retrying');
            web_socket();
        }, 2000);
        return;
    }

    console.log('opening new socket');
    ws = new WebSocket("ws://localhost:8989/ws");

    ws.onopen = function () {
        // Web Socket is connected, send data using send()
        //    ws.send("list");
        //    console.log("sending list");
    };

    ws.onmessage = function (evt) {
        var received_msg = evt.data;
        //    console.log("Message is received...");
        //    console.log(received_msg);
        //    console.log(received_msg.substring(0, 30));
        parseJSPS(received_msg);
    };

    ws.onclose = function () {
        // websocket is closed.
        console.log("Connection is closed. Retry");
        web_socket();
    };

    ws.onerror = function () {
        console.log("Connection error... retry");
        web_socket();
    }

}

function setup() {
    var i, j;
    createCanvas(1000, 730);
    textSize(20);

    frameRate(15);

    for (i = 0; i < nInputs; i++) {

        input[i] = createDiv(""); //'INPUT ' + (i + 1));
        input[i].name = "" + (i + 1);
        input[i].x = X_MAIN;
        input[i].y = Y_MAIN + i * VSKIP;
        input[i].position(input[i].x, input[i].y);
        input[i].slider = createSlider(0, 100, 50);
        input[i].slider.position(input[i].x, input[i].y + 18);
        input[i].slider.style('width', '205px');
        input[i].slider.style('color', 'blue');
        // input[i].invert = createCheckbox('invert', false);
        //input[i].invert.position(input[i].x + 97, input[i].y);
        input[i].check = []; // the check box
        input[i].HID = []; // only used for HID boxes
        input[i].Click = [];
        input[i].JOY = [];
        for (j = 0; j < nOutputs; j++) {
            input[i].check[j] = createCheckbox('chk', false);
            // (j-1) below because the first element is 'invert'
            input[i].check[j].position(input[i].x + H_CHK, input[i].y + (j - 1) * 20);
            input[i].HID[j] = createInput("A");
            input[i].HID[j].position(input[i].x + H_INPUT,
                input[i].y + (j - 1) * 20);
            input[i].HID[j].style('width', '14px');
            input[i].HID[j].hide();

            input[i].Click[j] = createSelect();
            input[i].Click[j].position(input[i].x + H_INPUT,
                input[i].y + (j - 1) * 20);
            input[i].Click[j].option('mouse click');
            input[i].Click[j].option('other');
            input[i].Click[j].hide();

            input[i].JOY[j] = createSelect();
            input[i].JOY[j].position(input[i].x + H_INPUT,
                input[i].y + (j - 1) * 20);
            input[i].JOY[j].option('arrow L-R');
            input[i].JOY[j].option('arrow U-D');
            input[i].JOY[j].option('mouse L-R');
            input[i].JOY[j].option('mouse U-D');
            input[i].JOY[j].value('arrow L-R');
            input[i].JOY[j].hide();
        }
        // move the invert checkbox
        input[i].check[INVERT].position(input[i].x + 97, input[i].y);
    }
    //myCanvas.parent('myContainer');
    x = width / 2; // set X and Y in the middle of the screen
    y = width / 2;

    var name;
    for (i = 0; i < nButtons; i++) {
        switch (i) {
        case REFRESHPORT:
            name = "Refresh Port List";
            break;
        case NEXTPORT:
            name = "Next Port";
            break;
        case SELECTPORT:
            name = "Select Port";
            break;
        case GETPROFILE:
            name = "Get Profile";
            break;
        case SETPROFILE:
            name = "Set Profile";
            break;
        case RUNSENSACT:
            name = "Run Sensact";
            break;
        default:
            name = "unknown";
            break;
        }
        button[i] = createButton(name);
        button[i].position(X_MAIN + X_IN_WIDTH + X_OUT_WIDTH + 30, Y_MAIN + i * 50 - 5);
        button[i].style('color', 'blue');
        button[i].style('background', BACKGROUNDCOLOR);
        button[i].size(80, 40);
    }

    button[REFRESHPORT].mousePressed(refreshport);
    button[NEXTPORT].mousePressed(nextport);
    button[SELECTPORT].mousePressed(selectport);
    button[GETPROFILE].mousePressed(getprofile);
    button[SETPROFILE].mousePressed(setprofile);
    button[RUNSENSACT].mousePressed(runsensact);
}

function resetAllButtons() {
    for (i = 0; i < nButtons; i++) {
        button[i].style('color', 'blue');
    }
}

function draw() {
    background(BACKGROUNDCOLOR); // make the screen white
    var fillColor = 127; // set the fill color to black
    noStroke();
    fill(20, 20, 200); // set the fill color
    if (button > 1) { // if the button is not pressed
        fillColor = color(0x44, 0xff, button); // blue fill color
        fill(fillColor);
    }

    var val;

    for (i = 0; i < nInputs; i++) {
        fill(BACKGROUNDCOLOR); // gray box

        stroke(180);
        strokeWeight(1);
        // input bounding box
        rect(input[i].x - 15, input[i].y - 15, X_IN_WIDTH, VSKIP - VSPACE, 10, 10, 10, 10);

        fill(20, 20, 200); // set the fill color
        noStroke();
        textSize(12);
        //stroke(20,20,200);
        //strokeWeight(1);
        text('INPUT ' + input[i].name, input[i].x - 8, input[i].y + 5);
        noStroke();
        textSize(12);
        text('invert', input[i].x + 110, input[i].y + 5);
        text('relay A', input[i].x + H_CHK + 13, input[i].y + 5);
        text('relay B', input[i].x + H_CHK + 13, input[i].y + 25);
        text('bluetooth HID', input[i].x + H_CHK + 13, input[i].y + 45);
        text('USB Keyboard', input[i].x + H_CHK + 13, input[i].y + 65);
        text('Click', input[i].x + H_CHK + 13, input[i].y + 85);
        text('Joystick', input[i].x + H_CHK + 13, input[i].y + 105);
        text('buzzer', input[i].x + H_CHK + 13, input[i].y + 125);
        text(portsList[portsequence], X_MAIN + X_IN_WIDTH + X_OUT_WIDTH + 115, Y_MAIN + 50 + 12);

        fill(150, 150, 240);
        // check if we invert the input signal
        val = results[i];
        if (input[i].check[INVERT].checked()) {
            val = 100 - val;
        }

        if (input[i].slider.value() <= val) {
            fill(200, 20, 20);
        }

        //display input value and bar
        text(results[i], input[i].x + 182, input[i].y + 5);
        rect(input[i].x - 10, input[i].y + 12, 10 + val * 2, 16);

        noFill();
        stroke(180);
        if (input[i].slider.value() <= val) {
            stroke(200, 20, 20);
            strokeWeight(2);
        }
        // output bounding box, color depending on if threshold exceeded
        rect(input[i].x + X_IN_WIDTH, input[i].y - 15,
            X_OUT_WIDTH, VSKIP - VSPACE, 10, 10, 10, 10);
        //VSKIP - 25, VSKIP - 40, 10, 10, 10, 10);

        if (input[i].check[BLUETOOTH].checked())
            input[i].HID[BLUETOOTH].show();
        else
            input[i].HID[BLUETOOTH].hide();
        if (input[i].check[USB_HID].checked())
            input[i].HID[USB_HID].show();
        else
            input[i].HID[USB_HID].hide();
        if (input[i].check[CLICK].checked())
            input[i].Click[CLICK].show();
        else
            input[i].Click[CLICK].hide();
        if (input[i].check[JOYSTICK].checked())
            input[i].JOY[JOYSTICK].show();
        else
            input[i].JOY[JOYSTICK].hide();
        //text( input[i].HID[BLUETOOTH].value(), input[i].x + 500, input[i].y + 5);
    }
}

function refreshport() {
    resetAllButtons();
    button[REFRESHPORT].style('color', 'red');
    if (ws && ws.readyState == 1)
        ws.send("list");
    else web_socket();
    return;
}

function nextport() {
    resetAllButtons();
    button[NEXTPORT].style('color', 'red');
    portsequence++;
    if (portsequence == portsList.length) portsequence = 0;
}

function selectport() {
    resetAllButtons();
    button[SELECTPORT].style('color', 'red');
    currentport = portsList[portsequence];

    if (ws && ws.readyState == 1)
        ws.send('open ' + currentport + ' 9600');
    else web_socket();
}

function getprofile() {
    resetAllButtons();
    button[GETPROFILE].style('color', 'red');
    if (ws && ws.readyState == 1)
        ws.send('send ' + currentport + ' 8\n');
    else web_socket();
    //  socket.emit("message", "8\n");
}

/* interaction with Sensact board
0, followed by 
nInputs[] of 0 or 1 depending on each of nOutputs[], whether they are checked
followed by
nInputs of 
  - threshold value
  - Bluetooth char to output
  - USB char to output

*/
function setprofile() {
    resetAllButtons();
    button[SETPROFILE].style('color', 'red');
    //  socket.emit("message", "0");
    commandString = ' 0';
    for (var i = 0; i < nInputs; i++) {
        for (var j = 0; j < nOutputs; j++) {
            //      socket.emit("message", input[i].check[j].checked() ? ',1' : ',0');
            commandString += input[i].check[j].checked() ? ',1' : ',0';
        }
    }
    for (var i = 0; i < nInputs; i++) {
        commandString += ',' + input[i].slider.value() +
            ',' + input[i].HID[BLUETOOTH].value().charCodeAt(0) +
            ',' + input[i].HID[USB_HID].value().charCodeAt(0) +
            ',' + from_joy(input[i].JOY[JOYSTICK].value());
        /*
            socket.emit("message", ',' + input[i].slider.value());
            socket.emit("message", ',' + input[i].HID[BLUETOOTH].value().charCodeAt(0));
            socket.emit("message", ',' + input[i].HID[USB_HID].value().charCodeAt(0));
        */
    }
    commandString += '\n';

    ws.send('send ' + currentport + commandString);

}

function runsensact() {
    resetAllButtons();
    button[RUNSENSACT].style('color', 'red');
    ws.send('send ' + currentport + ' 9\n');
    //  socket.emit("message", "9\n");
}

function parseJSPS(data) {
    var jObj = JSON.parse(data);
    if (jObj.SerialPorts) { // port list received
        portsList = [];
        portsequence = 0;
        var jsize = jObj.SerialPorts.length;
        for (var i = 0; i < jsize; i++) {
            //      console.log(jObj.SerialPorts[i].Name);
            portsList[i] = jObj.SerialPorts[i].Name;
        }
    } else
    if (jObj.P && jObj.D) { // data received
        var data = jObj.D;
        //    console.log('--->' + data + '<---');
        receiveddata += data;
        if (data[data.length - 1] == '\n') {
            readData(receiveddata);
        }
    } else {
        console.log(data);
    }
}

var joy_values = {
    1: 'arrow L-R',
    2: 'arrow U-D',
    3: 'mouse L-R',
    4: 'arrow U-D'
};

function to_joy(v) {
    return joy_values[v];
}

function to_joy2(v) {
    var ret = 'arrow L-R';
    if (v == 1) ret = 'arrow U-D';
    if (v == 2) ret = 'mouse L-R';
    if (v == 3) ret = 'mouse U-D';
    return ret;
}

function from_joy(v) {
    if (v == 'arrow L-R') return 0;
    if (v == 'arrow U-D') return 1;
    if (v == 'mouse L-R') return 2;
    if (v == 'mouse U-D') return 3;
    return 0;
}

function readData(data) {
    console.log('data->' + data);
    var res = data.split(','); // split the data on the commas
    if (res[0] == 9999) {
        var pos = 1;
        for (var i = 0; i < nInputs; i++) {
            for (var j = 0; j < nOutputs; j++) {
                input[i].check[j].checked(res[pos++] == 1 ? true : false);
                //input[i].check[j].value (1);
            }
        }
        for (var i = 0; i < nInputs; i++) {
            input[i].slider.value(res[pos++]);
            input[i].HID[BLUETOOTH].value(String.fromCharCode(res[pos++]));
            input[i].HID[USB_HID].value(String.fromCharCode(res[pos++]));
            input[i].JOY[JOYSTICK].value(to_joy(res[pos++]));
        }
    } else {
        results = data.split(',');
    }
    //  console.log(data);
    receiveddata = '';
}