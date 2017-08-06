#
# SACustom.py
#
# Customize sets of action widgets for each action.
# These are functions which return a frame.
# These functions are referenced as optionfunc
# in the action class definition in
# SAModel.py.
#

from tkinter import *
from tkinter import ttk
from tkinter import messagebox
from SAWidgets import *
import SAModel

# For actions with no additional parameters - an empty frame.
def SAC_None(parent, t):
	frame = ttk.Frame(parent)
	return frame
	

# For actions which take a printable character (or a string of up to 4 characters)
class KeyOption(SAEntry):
	def __init__(self, parent, t):
		SAEntry.__init__(self, parent, self.callback, "Character:", 5)
		
		self.trigger = t
		
		strval = ""
		for i in range(4):
			byte = t.actionParam >> (8 * (3-i)) & 0xff
			if byte != 0:
				strval += chr(byte)
				
		self.value.set( strval )
		
	def callback(self, newval):
		length = len(newval)
		if length > 4:
			return False
		
		pval = 0
		for i in range(length):
			pval <<= 8
			pval += ord(newval[i])
		
		self.trigger.actionParam = pval
		return True

def SAC_KeyOption(parent, t):	
	return KeyOption(parent, t)


# ValueLabelPair is used to link labels for a Combobox to the 
# values associated with the labels.
class ValueLabelPair:
	def __init__(self, value, label, doRepeat = False):
		self.value = value
		self.label = label
		self.repeat = doRepeat
		
	def getKey(self):
		return self.label
		
	def getValue(self):
		return self.value
		
	def doRepeat(self):
		return self.repeat

# Define special keyboard characters	
HIDKeys = []

HIDKeys.append(ValueLabelPair( 0xDA, "UP ARROW" ))
HIDKeys.append(ValueLabelPair( 0xD9, "DOWN ARROW" ))
HIDKeys.append(ValueLabelPair( 0xD8, "LEFT ARROW" ))
HIDKeys.append(ValueLabelPair( 0xD7, "RIGHT ARROW" ))
HIDKeys.append(ValueLabelPair( 0xB2, "BACKSPACE" ))
HIDKeys.append(ValueLabelPair( 0xB3, "TAB" ))
HIDKeys.append(ValueLabelPair( 0xB0, "RETURN" ))
HIDKeys.append(ValueLabelPair( 0xB1, "ESC" ))
HIDKeys.append(ValueLabelPair( 0xD1, "INSERT" ))
HIDKeys.append(ValueLabelPair( 0xD4, "DELETE" ))
HIDKeys.append(ValueLabelPair( 0xD3, "PAGE UP" ))
HIDKeys.append(ValueLabelPair( 0xD6, "PAGE DOWN" ))
HIDKeys.append(ValueLabelPair( 0xD2, "HOME" ))
HIDKeys.append(ValueLabelPair( 0xD5, "END" ))

BTKeys = []

BTKeys.append(ValueLabelPair( 14, "UP ARROW" ))
BTKeys.append(ValueLabelPair( 12, "DOWN ARROW" ))
BTKeys.append(ValueLabelPair( 11, "LEFT ARROW" ))
BTKeys.append(ValueLabelPair( 7, "RIGHT ARROW" ))
BTKeys.append(ValueLabelPair( 8, "BACKSPACE" ))
BTKeys.append(ValueLabelPair( 9, "TAB" ))
BTKeys.append(ValueLabelPair( 10, "RETURN" ))
BTKeys.append(ValueLabelPair( 27, "ESC" ))
BTKeys.append(ValueLabelPair( 1, "INSERT" ))
BTKeys.append(ValueLabelPair( 4, "DELETE" ))
BTKeys.append(ValueLabelPair( 3, "PAGE UP" ))
BTKeys.append(ValueLabelPair( 6, "PAGE DOWN" ))
BTKeys.append(ValueLabelPair( 2, "HOME" ))
BTKeys.append(ValueLabelPair( 5, "END" ))

class specialKeys(SACombo):
	def __init__(self, parent, t, keyset):
		SACombo.__init__(self, parent, keyset, self.callback, "Key:")
		self.trigger = t
		for key in keyset:
			if t.actionParam == key.getValue():
				self.value.set(key.getKey())
		
	def callback(self, keyvalPair):
		self.trigger.actionParam = keyvalPair.getValue()
		
	
def SAC_HIDSpecial(parent, t):
	return specialKeys(parent, t, HIDKeys)
	
	
def SAC_BTSpecial(parent, t):
	return specialKeys(parent, t, BTKeys)

# Define Mouse Actions
MOUSE_UP = 1
MOUSE_DOWN = 2
MOUSE_LEFT = 3
MOUSE_RIGHT = 4
MOUSE_CLICK = 5
MOUSE_PRESS = 6
MOUSE_RELEASE = 7
MOUSE_RIGHT_CLICK = 8
NUDGE_UP = 10
NUDGE_DOWN = 11
NUDGE_LEFT = 12
NUDGE_RIGHT = 13

mice1 = []
mice2 = []

mice1.append( ValueLabelPair(MOUSE_UP, "Mouse Up", True))
mice1.append( ValueLabelPair(MOUSE_DOWN, "Mouse Down", True))
mice1.append( ValueLabelPair(MOUSE_LEFT, "Mouse Left", True))
mice1.append( ValueLabelPair(MOUSE_RIGHT, "Mouse Right", True))
mice1.append( ValueLabelPair(MOUSE_CLICK, "Mouse Click"))
mice1.append( ValueLabelPair(NUDGE_UP, "Nudge Up"))
mice1.append( ValueLabelPair(NUDGE_DOWN, "Nudge Down"))
mice1.append( ValueLabelPair(NUDGE_LEFT, "Nudge Left"))
mice1.append( ValueLabelPair(NUDGE_RIGHT, "Nudge Right"))

mice2.append( ValueLabelPair(MOUSE_UP, "Mouse Up", True))
mice2.append( ValueLabelPair(MOUSE_DOWN, "Mouse Down", True))
mice2.append( ValueLabelPair(MOUSE_LEFT, "Mouse Left", True))
mice2.append( ValueLabelPair(MOUSE_RIGHT, "Mouse Right", True))
mice2.append( ValueLabelPair(MOUSE_CLICK, "Mouse Click"))
mice2.append( ValueLabelPair(MOUSE_RIGHT_CLICK, "Mouse Right Click"))
mice2.append( ValueLabelPair(MOUSE_PRESS, "Mouse Press"))
mice2.append( ValueLabelPair(MOUSE_RELEASE, "Mouse Release"))
mice2.append( ValueLabelPair(NUDGE_UP, "Nudge Up"))
mice2.append( ValueLabelPair(NUDGE_DOWN, "Nudge Down"))
mice2.append( ValueLabelPair(NUDGE_LEFT, "Nudge Left"))
mice2.append( ValueLabelPair(NUDGE_RIGHT, "Nudge Right"))

class MouseOption(SACombo):
	def __init__(self, parent, t, keyset):
		SACombo.__init__(self, parent, keyset, self.callback, "Mouse Action:")
		self.trigger = t
			
		for key in keyset:
			if t.actionParam == key.getValue():
				self.value.set(key.getKey())
				self.trigger.repeat = key.doRepeat()
		
	def callback(self, keyvalPair):
		self.trigger.actionParam = keyvalPair.getValue()
		self.trigger.repeat = keyvalPair.doRepeat()

		
def SAC_MouseOption(parent, t):
	if SAModel.sensactVersionID >= 301:
		keys = mice2
	else:
		keys = mice1
		
	return MouseOption(parent, t, keys)

# Define IR TV Control		
TV_ON_OFF = 1
VOLUME_UP = 2
VOLUME_DOWN = 3
CHANNEL_UP = 4
CHANNEL_DOWN = 5

IRActions = []
IRActions.append( ValueLabelPair(TV_ON_OFF, "On/Off"))
IRActions.append( ValueLabelPair(VOLUME_UP, "Volume Up"))
IRActions.append( ValueLabelPair(VOLUME_DOWN, "Volume Down"))
IRActions.append( ValueLabelPair(CHANNEL_UP, "Channel Up"))
IRActions.append( ValueLabelPair(CHANNEL_DOWN, "Channel Down"))

class IROption(SACombo):
	def __init__(self, parent, t, keyset):
		SACombo.__init__(self, parent, keyset, self.callback, "IR Action:")
		self.trigger = t
		for key in keyset:
			if t.actionParam == key.getValue():
				self.value.set(key.getKey())
		
	def callback(self, keyvalPair):
		self.trigger.actionParam = keyvalPair.getValue()

		
def SAC_IROption(parent, t):
	f = ttk.Frame(parent)
	ir = IROption(f, t, IRActions)
	ir.pack(side=LEFT)
	repeat = SARepeatControl(f, t)
	repeat.pack(side=LEFT, padx=5)
	return f
#	return IROption(parent, t, IRActions)

# Define Buzzer parameters
class Frequency(SAEntry):
	def __init__(self, parent, t):
		SAEntry.__init__(self, parent, self.callback, "Frequency:", 4)
		
		self.trigger = t
		
		self.frequency = (t.actionParam >> 16) & 0xffff			
		self.value.set( str(self.frequency) )
		
	def callback(self, newval):
		try:
			val = int(newval)
			if not (50 <= val <= 2000):
				messagebox.showerror(title="Entry error", 
					message="Frequency must be a value between 50 and 2000.")						
				if val < 50:
					val = 50
				elif val > 2000:
					val = 2000
				
			self.frequency = val
			duration = self.trigger.actionParam & 0xffff
			
			self.trigger.actionParam = (self.frequency << 16) + duration
			self.value.set( str(self.frequency) )
			return True
				
		except Exception:
			messagebox.showerror(title="Entry error", 
				message="Frequency must be a numeric value between 50 and 2000.")						
			self.value.set( str(self.frequency) )
			return False
			
class Duration(SAEntry):
	def __init__(self, parent, t):
		SAEntry.__init__(self, parent, self.callback, "Duration:", 4)
		
		self.trigger = t
		
		self.duration = t.actionParam & 0xffff			
		self.value.set( str(self.duration) )
		
	def callback(self, newval):
		try:
			val = int(newval)
			if not (0 <= val <= 1000):
				messagebox.showerror(title="Entry error", 
					message="Duration must be a value between 0 and 1000.")						
				if val < 0:
					val = 0
				elif val > 1000:
					val = 1000
				
			self.duration = val
			frequency = (self.trigger.actionParam >> 16) & 0xffff
			
			self.trigger.actionParam = (frequency << 16) + self.duration
			self.value.set( str(self.duration) )
			return True
				
		except Exception:
			messagebox.showerror(title="Entry error", 
				message="Duration must be a numeric value between 0 and 1000.")						
			self.value.set( str(self.duration) )
			return False
			
def SAC_Buzzer(parent, t):
	frame = ttk.Frame(parent)
	freq = Frequency(frame, t)
	freq.pack(side=LEFT)
	dur = Duration(frame, t)
	dur.pack(side=LEFT)
	return frame


		
