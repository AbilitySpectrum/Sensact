# generic input code for Arduino


## generic listing
Generic code to read digital, analog pins and the I2C-based MPR121 12-key touchpad, and output them in the following format:

-  digital signals are sent as "-:0", "-:1"", etc
-  the 3 analog signals are sent as "YPR:?:?:?"
- The touchpad signals are sent out as "+:0", "+:1", ..., "+:11"

These conventions are compatible with the p5 browser app:  ~/0/gendisplay

Use #define SERIAL_OUT

Compile and upload to a Leonardo or any HID compatible Arduino board.

### output to a browser

Run **serial port JSON server**. The binarny executable can be downloaded from <https://github.com/chilipeppr/serial-port-json-server>. Mac, Windows, Linux versions are available.

Then run **gendisplay.html** in a browser.

## mouse control
The Joystick's A0, A1 lines output USB HID mouse control. Digital pins 3 and 2 emit left and clicks.

Comment out #define SERIAL_OUT.

Compile and upload to a Leonardo or any HID compatible Arduino board. Then connect the USB cable to a computer.

Use an OTG cable to connect to a tablet.

