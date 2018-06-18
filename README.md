## The Sensact Hub
### An Arduino shield with analog inputs and IR and relay outputs
### - with generic 'sensing' and 'acting' functions configurable by software

#### Features

1. a shield that is compatible with the Arduino board
2. Arduino code that works in tandem with a web based app
3. The web-based app configures the Arduino+Sensact combo
4. Once configured, the combo may be plugged into any USB device for access control
5. The shield also has on-board IR transceivers and is Bluetooth capable

#### Motivation

Existing switch controls for computer users with physical impairment tend to be specialized and expensive. They general use one kind of sensors and have restricted functionality.

We provide a generic device that allows the user to experiment with different sensors, to find something that works best for him. We provide on-board IR output, a socket for an optional Bluetooth board and also configurable key strokes for USB HID controls. 

The Sensact Hub brings together 3 analog inputs (one of them can be configured as an I2C input) and 2 relay outputs as well as HID, IR and Bluetooth outputs.

We use 3.5mm audio sockets to make it easy to swap different input sensors. The tip is VCC, sleeve is GND and Ring is Signal. The first socket can be used by an I2C device where the two rings are SDA and SCL.

#### Use cases

We have used a number of sensors: light, capacitive touch, microphone as touch detectors. The signal can be used to trigger a call bell on one of the relays. Or it can be used to send specific letters over USB to control a smartphone or tablet. With Bluetooth, the hub sends out the letters wirelessly. The IR transceivers can function as a remote control for TV.

We have also been able to plug in a 6DO gyroscope+accelerator module. The user can then use movements to control a mouse.

#### Compatibility:

Board v1 + Arduino v1 + Config v1 or /Serial_port_version

Board v2 + Arduino v1 + Config v1 or /Chrome_version

Board v2 + Arduino v2 + Config v2

#### Set up Config v1

The web-based configuration software is in the *configSensact* directory.

The web-based software requires a serial port server. Its binary is downloadable from <https://github.com/johnlauer/serial-port-json-server>. It is compatible with OSX, Linux and Windows.

#### Set up Config v2

The Config v2 is a Chrome App. The a production release would be downloadable as a Chrome App but it currently must be added as an extension. 

More Tools -> Extensions. Check "Developer Mode". Load unpacked extension. Direct it to the configSensactApp directory.

#### To do
Currently no IR capability.

#### Partners

This work is performed by volunteers and staff at Bruyère's Saint Vincent Hospital AAC Clinic and Nathan Lim from Carleton University, all in Ottawa. 

Bruyère, Carleton and Ability Spectrum are not responsible for the use of this material. Please attribute the source if you republish or use partiallly or in whole, using License below.

#### Disclaimer

All design and code are provided with no warranty whatsoever.

#### License
<https://github.com/abilityspectrum>

Copyright (c) 2015-2016 Ability Spectrum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

