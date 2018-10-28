#Bluno Beetle

This file contains code to read from a BNO055 9dof sensor and connect via bluetooth to a DFRobot Bluetooth device. 

It uses gyroscope readings to detect head tilt and output the 3 degrees over bluetooth.

Each axis has one of 3 discrete values: 0, 50, 100. A rotation around the X axis will change the 50 to 100. It will continue outputting 100 until it is rotated the opposite direction. There is a timer to prevent a quick rotation change the axis data after it has reached 50. This prevents the mouse from changing direction rapidly.

The threshold to change the axis data from 50 to 100/0 is larger than the threshold to change from 100/0 to 50. This allows easier stops.
