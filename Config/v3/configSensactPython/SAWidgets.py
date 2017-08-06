import SAModel 
#
# SAWidgets.py
#
# A collection of classes which define widgets needed by the UI
#
# A widget define a UI element and also communicates with an underlying
# trigger class.  If multiple widgets of a similar type are needed they
# are subclassed with code which handles the trigger updates.

from tkinter import *
from tkinter import ttk
from tkinter import messagebox

SCALE_SIZE = 150
#
# SAScale
# A complex widget used to both show and set the levels for sensors.
# A Scale object is used to set values.
# An overlaid canvas is used to draw the current signal level.
# A check button is included to control whether the signal should be
# active-high or active-low (inverted)
# A showValues method can be used to turn on and off the display
# of the value bar.
class SAScale(ttk.Frame):		
	def __init__ (self, parent, t):
		ttk.Frame.__init__(self, parent)
		
		self.trigger = t
		self._max = t.sensor.maxval
		self._min = t.sensor.minval
		self._value = 0
		
		self.frame = ttk.Frame(self)
		self.scale = ttk.Scale(self.frame, orient=HORIZONTAL, length=SCALE_SIZE,
			from_ = self._min, to = self._max, command=self.scaleChange)
		self.canvas = Canvas(self.frame)
		self.canvas.configure(width=SCALE_SIZE, height=5)
			
		self.scale.grid(column=0, row=1, sticky = "S,E,W", pady=5, padx=5)
		self.canvas.grid(column=0, row=1, sticky = "S,E,W", padx=4)
		
		self.frame.grid(column=0, row=0)
		
		self.invValue = StringVar()
		self.inv = ttk.Checkbutton(self, text="Inv", variable=self.invValue,
			command=self.invChange)
		self.inv.grid(column=1, row=0)
		self.scale['value'] = t.triggerValue
		self.showValues(False)
		
		if self.trigger.condition == SAModel.TRIGGER_ON_LOW:
			self.invValue.set(1)
		else:
			self.invValue.set(0)
	
	def scaleChange(self, triggerValue):
		if self.showValues:
			self.setValue(self._value)
		# triggerValue here is a string representation of a floating point value.
		# Yuch!
		self.trigger.triggerValue = int(float(triggerValue))
		
	def invChange(self):
		if self.showValues:
			self.setValue(self._value)
		if self.invValue.get() == '1':
			self.trigger.condition = SAModel.TRIGGER_ON_LOW
		else:
			self.trigger.condition = SAModel.TRIGGER_ON_HIGH
		
	def setValue(self, value):
		if (value < self._min):
			value = self._min
		if (value > self._max):
			value = self._max
		diff = value - self._min
		inv = self.invValue.get()
		if (self.scale.get() <= value):
			if (inv == '1'):
				c = 'blue'
			else:
				c = 'red'
		else:
			if (inv == '1'):
				c = 'red'
			else:
				c = 'blue'
		
		self._value = value
		self.canvas.delete("all")
		if (inv == '1'):
			length = SCALE_SIZE * (self._max - value) / (self._max - self._min)
			self.canvas.create_line(SCALE_SIZE, 3, SCALE_SIZE-length, 3, fill=c, width=5)		
		else:
			length = SCALE_SIZE * (value - self._min) / (self._max - self._min)
			self.canvas.create_line(0, 3, length, 3, fill=c, width=5)	
	
	def showValues(self, option):
		self.showValue = option
		if (option == True):
			self.canvas.grid()
		else:
			self.canvas.grid_remove()	
	

#
# SACombo
# A simple combo box.
# It takes a list of 'items' as one argument.
# The items MUST provide a getKey() method.  This is used to generate
# a list of keys, and a dictionary to map keys to values.
# It then displays the keys, and when a selection is made
# it reports the value associated with the key.
class SACombo(ttk.Frame):
	def __init__(self, parent, items, callback, labelStr):
		ttk.Frame.__init__(self, parent)
		
		# From the list of items
		# generate keys and a dictionary		
		self.dictionary = {}
		keys = []
		for item in items:
			key = item.getKey()
			keys.append(key)
			self.dictionary[key] = item
			
		self.callback = callback
		self.value = StringVar()
		self.value.set(keys[0])
		
		# create the box
		self.combo = ttk.Combobox(self, textvariable=self.value)
		self.combo['values'] = keys
		self.state(['readonly'])
		self.combo.bind('<<ComboboxSelected>>', self.selectionChanged)
		
		# create the label
		label = ttk.Label(self, text=labelStr)
		
		# position
		label.pack(side=LEFT, padx=(5,3))
		self.combo.pack(side=LEFT, padx=(0,5))
		
	def selectionChanged(self, event):
		self.combo.selection_clear()
		val = self.dictionary[self.value.get()]
		self.callback(val)
		
	def setValue(self, value):
		self.value.set(value)
	
class ActionCombo(SACombo):
	def __init__(self, parent, t, uiCallback):
		SACombo.__init__(self, parent, SAModel.actions, uiCallback, 'Action:')
		
		self.trigger = t
		self.setValue(t.action.name)
	
	
class SASpinbox(ttk.Frame):
	def __init__(self, parent, keys, callback, labelStr):
		ttk.Frame.__init__(self, parent)
		
		self.value = StringVar()
		self.callback = callback
		
		# create the spinner
		self.spinner = Spinbox(self, textvariable=self.value, command=self.selectionChanged)
		self.spinner['values'] = keys
		self.spinner['width'] = 4
		self.spinner['state'] = 'readonly'
		
		# create the label
		label = ttk.Label(self, text=labelStr)
		
		# position
		label.pack(side=LEFT, padx=(5,3))
		self.spinner.pack(side=LEFT, padx=(0,5))
		
	def selectionChanged(self):
		self.callback(self.value.get())
		

class RequiredState(SASpinbox):
	def __init__(self, parent, t):
		keys = ['Any', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15']
		SASpinbox.__init__(self, parent, keys, self.callback, "State:")
		
		self.trigger = t	
		if (self.trigger.reqdState == 0):
			self.value.set('Any')
		else:
			self.value.set( str(self.trigger.reqdState) )
			
	def callback(self, value):
		if (value == 'Any'):
			self.trigger.reqdState = 0
		else:
			self.trigger.reqdState = int(value)

class ActionState(SASpinbox):
	def __init__(self, parent, t):
		keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15']
		SASpinbox.__init__(self, parent, keys, self.callback, "New State:")
		
		self.trigger = t	
		self.value.set( str(self.trigger.actionState) )
			
	def callback(self, value):
		self.trigger.actionState = int(value)
		
		
class SAEntry(ttk.Frame):
	def __init__(self, parent, cb, labelStr, width):
		ttk.Frame.__init__(self, parent)
		
		self.value = StringVar()
		self.cb = cb
		
		# create the entry box
		self.entry = ttk.Entry(self, textvariable=self.value)
		vcmd = (self.entry.register(self.validate), '%P')
		self.entry['width'] = width
		self.entry['validatecommand'] = vcmd
		self.entry['validate'] = 'key'
		
		# create the label
		label = ttk.Label(self, text=labelStr)
		
		# position
		label.pack(side=LEFT, padx=(5,3))
		self.entry.pack(side=LEFT, padx=(0,5))
		
	def validate(self, newval):
		if newval == '':
			return True
		return self.cb(newval)	

class Delay(SAEntry):
	def __init__(self, parent, t):
		SAEntry.__init__(self, parent, self.callback, "Delay:", 5)
		
		self.trigger = t
		self.value.set( str(self.trigger.delay) )
		
	def callback(self, newval):
		try:
			val = int(newval)
			if 0 <= val <= 30000:
				self.trigger.delay = val
				return True
			else:
				messagebox.showerror(title="Entry error", 
					message="Delay must be a value between 0 and 30000.")						
				self.value.set( str(self.trigger.delay) )
				return False
		except Exception:
			messagebox.showerror(title="Entry error", 
				message="Delay must be a numeric value between 0 and 30000.")						
			self.value.set( str(self.trigger.delay) )
			return False

# Value for non-continuous sensors (e.g. USB keyboard)
class SAValue(SAEntry):			
	def __init__(self, parent, t):
		SAEntry.__init__(self, parent, self.callback, "Value:", 3)
		
		self.trigger = t
		self.value.set( chr(self.trigger.triggerValue) )
		
	def callback(self, newval):
		if len(newval) > 1:
			return False
		else:
			self.trigger.triggerValue = ord(newval[0])
			return True


# Repeat control
class SARepeatControl(ttk.Frame):
	def __init__(self, parent, t):
		ttk.Frame.__init__(self, parent)
		self.trigger = t
		
		self.value = StringVar()
		if t.repeat:
			self.value.set(1)
		else:
			self.value.set(0)
					
		# create the checkbox
		self.checkbox = ttk.Checkbutton(self, text='Repeat', 
			variable=self.value, command=self.callback)
		
		# position
		self.checkbox.pack(side=LEFT, padx=(0,5))
		
	def callback(self):
		if self.value.get() == '1':
			self.trigger.repeat = True
		else:
			self.trigger.repeat = False
			
	
