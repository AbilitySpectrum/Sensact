# gendisplay

Takes input via websocket() which connects to a serial port.

Displays hits as keys on a canvas. Also displays 'meters'.

####Protocol

tag: value1 [ : value2 [: value3] ]

E.g. "YPR: 123: 456: 789" would be yaw pitch row input. These are display on 3 dials.


"+:1" shows the '1' key as 'hit'.

#### Set-up


Run **serial port JSON server**. The binarny executable can be downloaded from <https://github.com/chilipeppr/serial-port-json-server>. Mac, Windows, Linux versions are available.

Then run **gendisplay.html** in a browser.

Press 'Refresh Port', then 'Next port' (press untl the right one shows up) then 'Select Port'.
