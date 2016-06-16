"use strict";

//// npm imports
//const jQuery = require("jquery");
//
//// File imports
//const menuButton = require("./menuButton.js");
//const util = require("./util.js");

var serialio = (function serialio() {
    var ws = null,
        currentport = 'none',
        commandString = '',
        receiveddata = '',
        portsequence = 0,
        portsList = [];

    if ("WebSocket" in window) {
        console.log("WebSocket is supported by your Browser!");
    } else {
        // The browser doesn't support WebSocket
        console.log("WebSocket NOT supported by your Browser!");
    }

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

    function refreshport() {
        //    setThisMenu(this);
        if (ws && ws.readyState == 1)
            ws.send("list");
        else web_socket();
        return;
    }

    function nextport() {
        //    setThisMenu(this);
        //    console.log(currentport = portsList[portsequence] )
        portsequence++;
        if (portsequence == portsList.length) portsequence = 0;
        currentport = portsList[portsequence];
    }

    function selectport() {
        //    setThisMenu(this);
        //    currentport = portsList[portsequence];
        //    console.log(currentport);

        if (ws && ws.readyState == 1)
            ws.send('open ' + currentport + ' 9600');
        else web_socket();
    }

    function parseJSPS(data) {
//        console.log(data);
        var jObj = JSON.parse(data);
        if (jObj.SerialPorts) { // port list received
            portsList = [];
            portsequence = 0;
            var jsize = jObj.SerialPorts.length;
            for (var i = 0; i < jsize; i++) {
                //            console.log(jObj.SerialPorts[i].Name);
                portsList[i] = jObj.SerialPorts[i].Name;
            }
        } else {
            if (jObj.P && jObj.D) { // data received
                var data = jObj.D;
                //    console.log('--->' + data + '<---');
                receiveddata += data;
                if (data[data.length - 1] == '\n') {
                    readData(receiveddata);
                    receiveddata = '';
                }
            } else {
                //            console.log(data);
            }
        }
    }

    //    function thisport() { return function() {return currentport;}; }

    return {
        web_socket: web_socket,
        refreshport: refreshport,
        nextport: nextport,
        selectport: selectport,
        thisport: function () {
            return currentport;
        }
    };

}());

function matrix(rows, cols, defaultValue) {
    var arr = [];

    // Creates all lines:
    for (var i = 0; i < rows; i++) {

        // Creates an empty line
        arr.push([]);

        // Adds cols to the empty line:
        arr[i].push(new Array(cols));

        for (var j = 0; j < cols; j++) {
            // Initializes:
            arr[i][j] = defaultValue;
        }
    }

    return arr;
}