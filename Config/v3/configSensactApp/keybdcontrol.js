
function startup() {
	var bypassConnection = false; // Make it true to bypass the connection phase.
								  // Useful when in testing/development mode.
	if (bypassConnection) {  
		// Turn off transitions - to speed things up.
		document.getElementById("buttons").style.transition = 'none';
		document.getElementById("connection").style.transition = 'none';
		connectionComplete();
		return;
	}
	
    if ("WebSocket" in window) {
        webSocket.connect();  // Calls connectionComplete when the connection is completed.
    } else {
        // The browser doesn't support WebSocket
        webSocket.status(1, "Sorry, WebSocket is NOT supported by your Browser!");
        webSocket.status(2, "You will need to use a different browser.");
    }	
};

// Called from WebSocket when the connection is complete.
function connectionComplete() {
	// A transition in CSS will make the connection box slide up out of the window
	// after a short delay.
	document.getElementById("connection").style.top = "-200px";
	// After that the buttons panel will appear.
	document.getElementById("buttons").style.opacity = "1";
}

function sendCmd(value) {
	webSocket.send('W' + value);
}
