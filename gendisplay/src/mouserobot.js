
/* 2016-05-26 Thu : ylh

NOT WORKING

debating whether to use a node-based socket io server to control mouse, or not

using Socket.io version 1.0 or later (http://socket.io/)
using robot.js to control mouse

see ~/0/2015/p5/configSensact v1 for example of talking to serial port
https://github.com/voodootikigod/node-serialport

*/

//Move the mouse across the screen as a sine wave.
var robot = require("robotjs");

//Speed up the mouse.
robot.setMouseDelay(2);

var twoPI = Math.PI * 2.0;
var screenSize = robot.getScreenSize();
var height = (screenSize.height / 2) - 10;
var width = screenSize.width;

for (var x = 0; x < width; x++)
{
    y = height * Math.sin((twoPI * x) / width) + height;
    robot.moveMouse(x, y);
}


var express = require('express'); // include express.js

var io = require('socket.io'), // include socket.io
  app = express(), // make an instance of express.js
  server = app.listen(1871), // start a server with the express instance
  socketServer = io(server); // make a socket server using the express server

//  set up server and socketServer listener functions:
app.use(express.static('public')); // serve files from the public folder
app.get('/:name', serveFiles); // listener for all static file requests
socketServer.on('connection', openSocket); // listener for websocket data

function serveFiles(request, response) {
  var fileName = request.params.name; // get the file name from the request
  response.sendFile(fileName); // send the file
}

var gSocket = null;

function openSocket(socket) {
  gSocket = socket; //ylh

  console.log('--> new user address: ' + socket.handshake.address);
  // send something to the web client with the data:
  // socket.emit('message', 'Hello, ' + socket.handshake.address);

  // this function runs if there's input from the client:
  socket.on('message', function (data) {
    myPort.write(data); // send the data to the serial device
    //console.log( 'To serial:' + data );
  });
  socket.on('selectthisport', function (data) {
    console.log('=> request to connect to port ' + data);
    portName = data;
    myPort = new SerialPort(portName, portConfig, false);
    myPort.open( errHandler );
    reportPorts();
  });

  // this function runs if there's input from the serialport:
  myPort.on('data', function (data) {
    socket.emit('message', data); // send the data to the client
    //console.log( 'From serial:' + data );
  });

}

// serial port initialization:
var serialport = require('serialport'), // include the serialport library
  SerialPort = serialport.SerialPort, // make a local instance of serial
  portName = process.argv[2], // get the port name from the command line
  portConfig = {
    baudRate: 9600,
    // call myPort.on('data') when a newline is received:
    parser: serialport.parsers.readline('\n')
  };

// open the serial port:
//var myPort = new SerialPort(portName, portConfig);

var SerialPort = require("serialport").SerialPort;
var myPort = new SerialPort(portName, portConfig, false);

var aPort = require("serialport");
var portsList = [];

myPort.open(errHandler);


function reportPorts() {
    aPort.list(function (err, ports) {
      var i = 0;
      ports.forEach(function (port) {
        //console.log(port.comName);
        portsList[i++] = port.comName;
      });
      if (gSocket) {
        gSocket.emit('portsList', JSON.stringify(portsList));
      }
      //console.log(portsList);
      console.log ('    ports list sent to client');
    });
}

function errHandler (error) {
  if (error) {
    console.log('\nfailed to open: ' + error);
    reportPorts();
    setTimeout(function () {
      myPort.open(errHandler);
    }, 2000);

  } else {
    console.log('\n[serial opened: ' + portName + ']');
    reportPorts();
    // this function runs if there's input from the serialport:

    myPort.on('data', function (data) {
      if (gSocket != null)
        gSocket.emit('message', data); // send the data to the client
      //console.log( 'From serial:' + data );
    });


    //ylh
    myPort.on('close', function (data) {
      console.log('re-establishing serial connection to' + portName);
      myPort.open(errHandler);
    });
  }
}