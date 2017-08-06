#
# SASerial.py
#
# Support for connection to and communication with
# serial ports.
#

import serial
from serial.tools import list_ports
from threading import Thread

def get_list():
	return list_ports.comports()
	
def open_port(port_name):
	global _serial
	_serial = serial.Serial(port=port_name)
	
def close_port():
	global _read_loop_active
	_read_loop_active = False
	_serial.close()
		
def write(data):
	_serial.write(data)
	_serial.flush()
		
#	print("Wrote ", data)
	
def _read_loop(dispatch_function):
	_buffer = bytearray()
	while(_read_loop_active):
		b = _serial.read(1)
#		print ("read" + b.decode())
		_buffer.extend(b)
		if (b == b'Z'):
			dispatch_function(_buffer)
			_buffer = bytearray()

def init_reading(dispatch_function):
	global _read_loop_active
	global _thread
	_read_loop_active = True
	_thread = Thread(target=_read_loop,args=(dispatch_function,))
	_thread.daemon = True
	_thread.start()
	
	

