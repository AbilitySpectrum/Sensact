## The Sensact Hub
### An Arduino shield with analog inputs and IR and relay outputs
### - with generic 'sensing' and 'acting' interface configurable by software

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

We use 2.5mm audio sockets to make it easy to swap different input sensors. The tip is VCC, sleeve is GND and Ring is Signal. The first socket can be used by an I2C device where the two rings are SDA and SCL.

#### Use cases

We have used a number of sensors: light, capacitive touch, microphone as touch detectors. The signal can be used to trigger a call bell on one of the relays. Or it can be used to send specific letters over USB to control a smartphone or tablet. With Bluetooth, the hub sends out the letters wirelessly. The IR transceivers can function as a remote control for TV.

We have also been able to plug in a 6DO gyroscope+accelerator module. The user can then use movements to control a mouse.

#### Set up

The shield design is place in the *hardware* directory. We plan to make the physical board available on cost recovery basis for users with physical impairment.

The Arduino code is contained in the *Sensact* directory. 

The web-based configuration software is in the *configSensact* directory.

The web-based software requires the a serial port server downloadable from <https://github.com/johnlauer/serial-port-json-server>. It is compatible with OSX, Linux and Windows.

#### Disclaimer

All design and code are provided with no warranty whatsoever.

#### License
MIT license.

