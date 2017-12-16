#
# SAMouseSpeed.py
#
# User interface elements to support mouse speed selection.
#
# Notes on mouse speed.
# The Sensact's mouse-control loop takes about 12 ms to execute.
# The code logic tracks time since the last action, and takes the 
# next action after a given delay-interval has elapsed.
#
# Since the loop takes 12 ms, the delay-interval time should be
# some multiple of 12.  Setting the delay to (e.g.) 18 is useless, 
# since this simply means that the mouse motion will trigger after
# two loops - or 24 ms.  We will set the delay-interval to 12 * N - 1
# to force a mouse move every 12 * N ms.
#
# For each mouse move action there is a parameter giving how many pixels
# the mouse will move on a given action.  We call this the jump
# parameter. Mouse speed can be controlled
# by setting both the delay-interval and the jump parameter.
#
# Setting a delay-interval that is too small can result in jumpy 
# behaviour over blue tooth.  For now we will not set this value
# less than 23 (for action every ~24ms)
#
# Fastest desired motion is:
#  delay: 24ms  jump: 15  = moving 625 pixels / second
#
# Slowest:
#  delay: 60ms  jump: 2   = moving  33 pixels / second
#
# The user interface presents the log of pixels per second.
# This allow slow, medium and fast regions of the scale to be
# roughly equal.  It means a whole bunch of conversions are needed.
# These are in SAModel.py
#
import math
from tkinter import *
from tkinter import ttk
from tkinter import messagebox
import SAModel
import SAWidgets

# Mouse speed sliders.  A slider with words to the right indicating 
# slow, fast etc.
class SpeedSelection(ttk.Frame):
	def __init__ (self, parent, initialValue):
		ttk.Frame.__init__(self, parent)
		
		initialLogValue = math.log(initialValue)
		
		self.scale = ttk.Scale(self, orient=HORIZONTAL, length=300, from_=3.5, to=6.5)
		self.scale['value'] = initialLogValue
		self.scale['command'] = self.valueChange;
		self.scale.pack(side=LEFT)
		
		self.label = ttk.Label(self)
		self.label['font'] = "TkHeadingFont 11"
		self.label.pack(side=LEFT, padx=(5,0))
		
		self.valueChange(str(initialLogValue))
		
	def valueChange(self, newValue):
		# The scale used is logarithmic so that the "Very slow", "Slow"
		# etc. sections are roughy the same size.
		# To translate to real terms:
		#  Log Value          Pixels per sec
		#    3.6                 33                 
		#    4.2                 66                
		#    4.8                133                 
		#    5.4                222                  
		#    6.0                416                 
		#    6.5                666                 
		
		self.value = float(newValue)
		if self.value < 4.0:
			self.label['text'] = "Very slow"
		elif self.value < 4.7:
			self.label['text'] = "Slow"
		elif self.value < 5.4:
			self.label['text'] = "Medium"
		elif self.value < 6.1:
			self.label['text'] = "Fast"
		else:
			self.label['text'] = "Very Fast"
		
	def getValue(self):
		return self.value
	
	def setValue(self, val):
		self.scale['value'] = val
		self.valueChange(val)

# A widget for getting the speed-change times.
# Uses the error checking and correction logic of SANumericEntry
class MouseTimer(SAWidgets.SANumericEntry):
	def __init__(self, parent, initial):
		SAWidgets.SANumericEntry.__init__(self, parent, "", 5, 0, 10000, self.final)
		
		self.setInitialValue( str(initial) )
		self.tvalue = initial
		
	def final(self, val):
		self.tvalue = val
		
	def getValue(self):
		return self.tvalue
		
	def setValue(self, val):
		self.tvalue = val
		self.setInitialValue(val)
		
		
class MouseSpeed(ttk.Frame):
	def __init__(self, parent):
		ttk.Frame.__init__(self, parent)
		
		# Create and layout the UI widgets.
		title = ttk.Label(self, text="Mouse Speed")
		title['font'] = "TkHeadingFont 12"
		title.grid(column=0, row=0, columnspan=2, pady=(15,10), padx = (5,0), sticky=W)
		
		# Row 1
		t1 = ttk.Label(self, text="Start speed:")
		t1['font'] = "TkHeadingFont 11"
		t1.grid(column=0, row=1, padx = (5,0), pady=(10,10), sticky=W)
		self.speed1 = SpeedSelection(self, 66)
		self.speed1.grid(column=1, row=1, sticky = W)
		
		# Row 2
		f2 = ttk.Frame(self)
		f2.grid(column=0, row=2, columnspan=2, padx=(5,0), pady=(10,10), sticky=W)
		l1 = ttk.Label(f2, text="after ")
		l1['font'] = "TkHeadingFont 11"
		l1.pack(side=LEFT)
		self.timer1 = MouseTimer(f2, 500)
		self.timer1.pack(side=LEFT)
		l1 = ttk.Label(f2, text=" milliseconds")
		l1['font'] = "TkHeadingFont 11"
		l1.pack(side=LEFT)
		
		# Row3
		t2 = ttk.Label(self, text="change to:")
		t2['font'] = "TkHeadingFont 11"
		t2.grid(column=0, row=3, padx=(5,0), pady=(10,10), sticky=W)
		self.speed2 = SpeedSelection(self, 133)
		self.speed2.grid(column=1, row=3, sticky = W)
		
		# Row 4
		f2 = ttk.Frame(self)
		f2.grid(column=0, row=4, columnspan=2, padx = (5,0), pady=(10,10), sticky=W)
		l1 = ttk.Label(f2, text="after an additional ")
		l1['font'] = "TkHeadingFont 11"
		l1.pack(side=LEFT)
		self.timer2 = MouseTimer(f2, 500)
		self.timer2.pack(side=LEFT)
		l1 = ttk.Label(f2, text=" milliseconds")
		l1['font'] = "TkHeadingFont 11"
		l1.pack(side=LEFT)
		
		# Row 5
		t3 = ttk.Label(self, text="change to:")
		t3['font'] = "TkHeadingFont 11"
		t3.grid(column=0, row=5, padx = (5,0), pady=(10,10), sticky=W)
		self.speed3 = SpeedSelection(self, 222)
		self.speed3.grid(column=1, row=5, sticky = W)
				
		# Register with the Model.
		# Code in the Model will call the following get/set functions
		# when data is transferred to or from the Sensact.
		SAModel.mouseMovingParams.registerUI(self)
		
	def getRawValues(self):		
		s1 = self.speed1.getValue()
		s2 = self.speed2.getValue()
		s3 = self.speed3.getValue()
		t1 = self.timer1.getValue()
		t2 = self.timer2.getValue()
		
		return (s1, s2, s3, t1, t2)
		
	def setRawValues(self, s1, s2, s3, t1, t2):
		self.speed1.setValue(s1)
		self.speed2.setValue(s2)
		self.speed3.setValue(s3)
		self.timer1.setValue(t1)
		self.timer2.setValue(t2)
		

		
