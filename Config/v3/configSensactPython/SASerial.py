#
# SASerial.py
#
# Support for connection to and communication with
# serial ports.
#

import time
import serial
from serial.tools import list_ports
from threading import Thread

def get_list():
	baseList = list_ports.comports()
	# Filter out 'n/a' ports on the Mac
	filteredList = []
	for p in baseList:
		if p.description.count('n/a') == 0:
			filteredList.append(p)
			
	return filteredList
	
def open_port(port_name):
	global _serial
	_serial = serial.Serial(port=port_name, write_timeout=1.0)
	
def close_port():
	global _read_loop_active
	global _serial
	if not "_serial" in globals():
		return
	_read_loop_active = False
	_serial.close()
	del _serial
		
def write(data):
	try:
		# Do not write more than ~400 bytes at a time.
		# The Mac can't handle it!!
		count = len(data)
		if count > 300:
			start = data[:300]
			data = data[300:]
			_serial.write(start)
			count = len(data)
		_serial.write(data)
		_serial.flush()
		time.sleep(0.1)  # Precaution for the mac.
		
	except serial.SerialTimeoutException:
		from __main__ import attemptReconnection
		attemptReconnection()
		
	except Exception as e:
		print("Unexpected write error {}".format(type(e).__name__))
		from __main__ import attemptReconnection
		attemptReconnection()
			
def _doRead():
	try:
		b = _serial.read(1)
		return b
	except TypeError: # Happens on PI when USB->UART disconnects
					  # Seems to happen spuriously during a reconnect
					  # and thus should be ignored.
		return None	
		
	except serial.SerialException:	
		if _read_loop_active == False:
			return None
		from __main__ import attemptReconnection
		attemptReconnection()

	except Exception as e:
		print("Unexpected read error {}".format(type(e).__name__))
		if _read_loop_active == False:
			return None
		from __main__ import attemptReconnection
		attemptReconnection()

def _read_loop(dispatch_function):
	_buffer = bytearray()
	while(_read_loop_active):
		b = _doRead()
		if b is None:
			return  # Read must have failed. 
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
	
	

	
	

