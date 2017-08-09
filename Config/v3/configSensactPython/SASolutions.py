from tkinter import *
from tkinter import ttk
import SASerial
import SAModel, SASensorUI, SACustom
import time, threading

gSyncEvent = threading.Event()
gCancelAction = False
					 
class SolutionUI(Toplevel):
	def __init__(self, parent, group):
		Toplevel.__init__(self, parent)
		
		self.group = group
		self.titleTxt = "Available Solutions"
		self.title(self.titleTxt)
		
		self.message = "Choose a solution"
		self.messageLbl = ttk.Label(self, text=self.message)
		self.messageLbl['font'] = "TkHeadingFont 14"
		self.messageLbl.pack(pady=(15,3))
		
		self.iframe = ttk.Frame(self)
		self.iframe.pack()
		
		location = ttk.Label(self.iframe, text="for " + self.group.name)
		location['font'] = "TkHeadingFont 11"
		location.pack(pady=(0, 15))
				
		self.cancel = ttk.Button(self, text="Cancel", command=self.cancelFn)
		self.cancel.pack(pady=20)
		
		self.geometry("400x300+100+100")
		
		self.bind('<<UpdateMsg>>', self.updateMessage)
		self.bind('<<OperationComplete>>', self.finish)
		self.bind('<<MouseSelect>>', self.mouseSelection)
		self.bind('<<IntervalSelect>>', self.intervalSelection)
		self.protocol('WM_DELETE_WINDOW', self.cancelFn)
		
	def clearButtons(self):
		for kid in self.iframe.winfo_children():
			kid.destroy()
		x = ttk.Label(self.iframe, text='')
		x.pack()
		
# --- Change Message --- #
	def setMessage(self, title, msg):
		self.titleTxt = title
		self.message = msg
		
	def updateMessage(self, event):
		self.title(self.titleTxt)
		self.messageLbl['text'] = self.message
		gSyncEvent.set()
		
# --- Mouse Selection --- #
	def mouseSelection(self, event):
		self.mouseType = ''
		self.title("Mouse Selection")
		self.messageLbl['text'] = "What kind of mouse?"
		hid = ttk.Button(self.iframe, text="HID Mouse", command=self.hidBtn)
		hid.pack(pady=5)
		bt = ttk.Button(self.iframe, text="Bluetooth Mouse", command=self.btBtn)
		bt.pack(pady=5)
		return
		
	def hidBtn(self):
		self.mouseType = SAModel.getActionByName("HID Mouse")
		self.clearButtons()
		gSyncEvent.set()
		
	def btBtn(self):
		self.mouseType = SAModel.getActionByName("Bt Mouse")
		self.clearButtons()
		gSyncEvent.set()
			
# --- Interval Selection --- #
	def intervalSelection(self, event):
		self.interval = 1000
		self.intervalStr = StringVar()
		self.intervalStr.set( str(self.interval) )
		self.title("Interval Selection")
		self.messageLbl['text'] = "Enter time between beeps"
		entry = ttk.Entry(self.iframe, width = 5, textvariable=self.intervalStr)
		vcmd = (entry.register(self.validate), '%P')
		entry['validatecommand'] = vcmd
		entry['validate'] = 'key'
		entry.pack(pady=5)
		btn = ttk.Button(self.iframe, text='OK', command=self.valueOK)
		btn.pack(pady=5)
		return
		
	def validate(self, newval):
		if newval == '':
			return True	
		try:
			val = int(newval)
			if 0 <= val <= 30000:
				self.interval = val
				return True
			else:
				messagebox.showerror(title="Entry error", 
					message="Delay must be a value between 0 and 30000.")						
				self.intervalStr.set( str(self.interval) )
				return False
		except Exception:
			messagebox.showerror(title="Entry error", 
				message="Delay must be a numeric value between 0 and 30000.")						
			self.intervalStr.set( str(self.interval) )
			return False	
			
	def valueOK(self):
		self.interval = int(self.intervalStr.get())
		self.clearButtons()
		gSyncEvent.set()
		
# --- Termination --- #	
	def finish(self, event):
		self.cancelFn()
		
	def cancelFn(self):
		global gCancelAction
		gCancelAction = True
		gSyncEvent.set()
		self.destroy()
		return

# Solutions selections for Input-1, Input-2 and Input-3	
class InputSolutionUI(SolutionUI):
	def __init__(self, parent, group):
		SolutionUI.__init__(self, parent, group)
		
		obm = ttk.Button(self.iframe, text="One button mouse", command=self.oneBtnMouse)
		obm.pack(pady=5)
		lmb = ttk.Button(self.iframe, text="Left mouse button", command=self.leftMouseBtn)
		lmb.pack(pady = 5)
		jsm = ttk.Button(self.iframe, text="Joystick mouse", command=self.joystickMouse)
		jsm.pack(pady=5)
		
	def oneBtnMouse(self):
		self.clearButtons()
		self.cthread = threading.Thread(target=oneBtnMouse, args=(self.group,))
		self.cthread.start()
		return
		
	def leftMouseBtn(self):
		self.clearButtons()
		self.cthread = threading.Thread(target=leftMouseBtn, args=(self.group,))
		self.cthread.start()
		return
		
	def joystickMouse(self):
		self.clearButtons()
		self.cthread = threading.Thread(target=joystickMouse, args=(self.group,))
		self.cthread.start()
		return

			
# UI Solutions button for InputSolutionUI (Input-1/2/3) 
class SolutionBtn(ttk.Button):
	def __init__(self, parent, winParent, group, **keywords):
		ttk.Button.__init__(self, parent)
		for kwd in keywords:
			self[kwd] = keywords[kwd]
		self.group = group
		self.winParent = winParent
		
		self['command'] = self.btnPressed
		
	def btnPressed(self):
		global gSolutionWindow
		gSolutionWindow = InputSolutionUI(self.winParent, self.group)

# 
# --- Calibration Support --- #
#		
# MinMax - a class for gathering min & max values for a sensor.
class MinMax:
	def __init__(self, sensor):
		self.valsSet = False
		self.sensor = sensor
		self.allowance = int((sensor.maxval - sensor.minval) / 10)
	
	# Gather max and min values for this sensor
	def checkValue(self):
		val = self.sensor.currentValue
		if (self.valsSet):
			if (val < self.min):
				self.min = val
			if (val > self.max):
				self.max = val
		else:
			self.min = self.max = val
			
	# Is this sensor within the recorded max/min (+ allowance) range?
	def inRange(self):
		val = self.sensor.currentValue
		if (self.min - self.allowance) <= val <= (self.max + self.allowance):
			return True
		else:
			return False
			
# TLocation - A holder for trigger location information
class TLocation:
	def __init__ (self, s, v, c):
		self.sensor = s
		self.value = v
		self.condition = c
					
class Calibration:
	def __init__(self, group):
		self.groupList = group.getList()
		
	def startCalibration(self):
		SASensorUI.showValues(False)
		SASensorUI.calibrating = True
		SASerial.write(SAModel.REPORT_MODE)
		
	def endCalibration(self):
		SASerial.write(SAModel.RUN_SENSACT)
		SASensorUI.calibrating = False		
		
	def getInitialValues(self):
		# Create a list of min/max counters the same length and order as the group
		self.startValues = []
		for sens in self.groupList:
			self.startValues.append(MinMax(sens))
		
		# Gather start max/min values
		for i in range(5):
			time.sleep(100.0/1000.0)
			for minmax in self.startValues:
				minmax.checkValue()

	def getLocation(self, prompt):
		# Ask the user to activate the sensor
		updateMessage("Calibration", prompt)
		time.sleep(1)
		if gCancelAction:
			return None
			
		# Detect the activated sensor
		found = False
		count = 0
		while(not found):
			count += 1
			if count > 50:
				break
			time.sleep(100.0/1000.0)
			
			for minmax in self.startValues:
				if not minmax.inRange():
					target = MinMax(minmax.sensor)
					restValue = minmax
					found = True
					break
			
		if  not found:
			return None
			
		# Get max/min values on the activated sensor
		for i in range(5):
			time.sleep(100.0/1000.0)
			target.checkValue()
			
		updateMessage("Calibration", "Thank you")
		time.sleep(1)
		if gCancelAction:
			return None
						
		# Check for appropriate separation in signals
		# and then return location info
		if target.min < restValue.min:
			# Active location is the smaller value
			if (target.max + target.allowance >= restValue.min - restValue.allowance):
				return None # Insufficient separation
			return TLocation(target.sensor, target.max + target.allowance, 
				SAModel.TRIGGER_ON_LOW)
		else:
			if (target.min - target.allowance <= restValue.max + restValue.allowance):
				return None # Insufficient separation
			return TLocation(target.sensor, target.min - target.allowance, 
				SAModel.TRIGGER_ON_HIGH)
	
	# Return the trigger point for a return-to-rest location from a trigger point
	def getRestPositionRelativeTo(self, location):
		# find corresponding rest sensor
		for minmax in self.startValues:
			if location.sensor == minmax.sensor:
				restValue = minmax
				break
		else:
			return None
			
		if (location.condition == SAModel.TRIGGER_ON_HIGH):
			return TLocation(restValue.sensor, restValue.max + restValue.allowance,
					SAModel.TRIGGER_ON_LOW)
		else:
			return TLocation(restValue.sensor, restValue.min - restValue.allowance,
					SAModel.TRIGGER_ON_HIGH)
			
		

# Message Updating	
def updateMessage(title, msg):
	gSyncEvent.clear()
	if gCancelAction: 
		return
	gSolutionWindow.setMessage(title, msg)
	gSolutionWindow.event_generate('<<UpdateMsg>>')
	gSyncEvent.wait()
	return
	
# Exit request
def endSolutionProcessing():
	gSyncEvent.clear()
	if gCancelAction: 
		return
	gSolutionWindow.event_generate('<<OperationComplete>>')
	return
	
# Mouse selection
def mouseSelection():
	gSyncEvent.clear()
	if gCancelAction: 
		return None
	gSolutionWindow.event_generate('<<MouseSelect>>')
	gSyncEvent.wait()
	if gCancelAction: 
		return None
	return gSolutionWindow.mouseType

# Interval selection
def intervalSelection():
	gSyncEvent.clear()
	if gCancelAction:
		return None
	gSolutionWindow.event_generate('<<IntervalSelect>>')
	gSyncEvent.wait()
	if gCancelAction: 
		return None
	return gSolutionWindow.interval
		
#
# --- Main solution logic --- #
#	

# One Button Mouse
class Trig:
	def __init__(self, c, d, cst, a, ast, ap):
		self.condition = c
		self.delay = d
		self.cState = cst
		self.action = a
		self.aState = ast
		self.aParam = ap
		
oneBtnMouseList = []
#                         Condition, Delay, State,  Action, New-State, Action-Parameter
oneBtnMouseList.append( Trig('H',      0,     1,     'N',     2,         0) )
oneBtnMouseList.append( Trig('L',      0,     2,     'A',     1,         SACustom.MOUSE_CLICK) )
oneBtnMouseList.append( Trig('H',      1,     2,     'M',     3,         ((800 << 16) + 250) ) )
oneBtnMouseList.append( Trig('L',      0,     3,     'N',     4,         0) )
oneBtnMouseList.append( Trig('H',      0,     4,     'A',     4,         SACustom.MOUSE_UP) )
oneBtnMouseList.append( Trig('H',      1,     3,     'M',     5,         ((400 << 16) + 250) ) )
oneBtnMouseList.append( Trig('L',      0,     5,     'N',     6,         0) )
oneBtnMouseList.append( Trig('H',      0,     6,     'A',     6,         SACustom.MOUSE_DOWN) )
oneBtnMouseList.append( Trig('H',      1,     5,     'M',     7,         ((600 << 16) + 250) ) )
oneBtnMouseList.append( Trig('L',      0,     7,     'N',     8,         0) )
oneBtnMouseList.append( Trig('H',      0,     8,     'A',     8,         SACustom.MOUSE_LEFT) )
oneBtnMouseList.append( Trig('H',      1,     7,     'M',     9,         ((500 << 16) + 250) ) )
oneBtnMouseList.append( Trig('L',      0,     9,     'N',    10,         0) )
oneBtnMouseList.append( Trig('H',      0,    10,     'A',    10,         SACustom.MOUSE_RIGHT) )
oneBtnMouseList.append( Trig('L',      2,     0,     'M',     1,         ((200 << 16) + 250) ) )

def oneBtnMouse(group):
	global gCancelAction	
	gCancelAction = False
	
	success = False
	cal = Calibration(group)
	cal.startCalibration()
	cal.getInitialValues()
	activeLocation = cal.getLocation("Please press the button")
	if not activeLocation == None:
		restLocation = cal.getRestPositionRelativeTo(activeLocation)
		if not restLocation == None:
			success = True
	cal.endCalibration()

	if gCancelAction:
		return
	if not success:
		updateMessage("Solution Failure", "Sorry, no button detected")
		time.sleep(2)
		endSolutionProcessing()
		return
		
	action = mouseSelection()
	if gCancelAction:
		return
		
	buzzer = SAModel.getActionByName("Buzzer")
	
	delay = intervalSelection()
	if gCancelAction:
		return
	
	SAModel.allTriggers.deleteTriggerSet(activeLocation.sensor)
	for t in oneBtnMouseList:
		tr = SAModel.allTriggers.newTrigger(activeLocation.sensor)
		if t.condition == 'H':
			tr.triggerValue = activeLocation.value
			tr.condition = activeLocation.condition
		else:
			tr.triggerValue = restLocation.value
			tr.condition = restLocation.condition
		
		tr.delay = delay * t.delay
		
		tr.reqdState = t.cState
		
		if t.action == 'A':
			tr.action = action
		elif t.action == 'M':
			tr.action = buzzer
			
		tr.actionParam = t.aParam
		
		tr.actionState = t.aState
		
	SASensorUI.reloadTriggers()
	endSolutionProcessing()
	return

leftMouseBtnList = []
#                         Condition, Delay, State,  Action, New-State, Action-Parameter
leftMouseBtnList.append( Trig('H',      0,     1,     'A',     1,         SACustom.MOUSE_PRESS) )
leftMouseBtnList.append( Trig('L',      0,     1,     'A',     1,         SACustom.MOUSE_RELEASE) )

def leftMouseBtn(group):
	global gCancelAction	
	gCancelAction = False
	
	success = False
	cal = Calibration(group)
	cal.startCalibration()
	cal.getInitialValues()
	activeLocation = cal.getLocation("Please press the button")
	if not activeLocation == None:
		restLocation = cal.getRestPositionRelativeTo(activeLocation)
		if not restLocation == None:
			success = True
	cal.endCalibration()

	if gCancelAction:
		return
	if not success:
		updateMessage("Solution Failure", "Sorry, no button detected")
		time.sleep(2)
		endSolutionProcessing()
		return
		
	action = mouseSelection()
	if gCancelAction:
		return
	
	delay = 0
		
	SAModel.allTriggers.deleteTriggerSet(activeLocation.sensor)
	for t in leftMouseBtnList:
		tr = SAModel.allTriggers.newTrigger(activeLocation.sensor)
		if t.condition == 'H':
			tr.triggerValue = activeLocation.value
			tr.condition = activeLocation.condition
		else:
			tr.triggerValue = restLocation.value
			tr.condition = restLocation.condition
		
		tr.delay = delay * t.delay
		
		tr.reqdState = t.cState
		
		tr.action = action
			
		tr.actionParam = t.aParam
		
		tr.actionState = t.aState

	SASensorUI.reloadTriggers()
		
	endSolutionProcessing()
	return

def joystickMouse(group):
	global gCancelAction	
	gCancelAction = False
	
	success = False
	cal = Calibration(group)
	cal.startCalibration()
	cal.getInitialValues()
	upLocation = cal.getLocation("Move joystick to UP position")
	if not upLocation == None:
		downLocation = cal.getLocation("Move joystick to DOWN position")
		if not downLocation == None:
			leftLocation = cal.getLocation("Move joystick to LEFT position")
			if not leftLocation == None:
				rightLocation = cal.getLocation("Move joystick to RIGHT position")
				if not rightLocation == None:
					success = True
	cal.endCalibration()

	if gCancelAction:
		return
	if not success:
		updateMessage("Solution Failure", "Sorry, Joystick motion not detected")
		time.sleep(2)
		endSolutionProcessing()
		return

	action = mouseSelection()
	if gCancelAction:
		return

	SAModel.allTriggers.deleteTriggerSet(upLocation.sensor)
	SAModel.allTriggers.deleteTriggerSet(leftLocation.sensor)
	
	t = SAModel.allTriggers.newTrigger(upLocation.sensor)
	t.triggerValue = upLocation.value
	t.condition = upLocation.condition
	t.action = action
	t.actionParam = SACustom.MOUSE_UP
	
	t = SAModel.allTriggers.newTrigger(downLocation.sensor)
	t.triggerValue = downLocation.value
	t.condition = downLocation.condition
	t.action = action
	t.actionParam = SACustom.MOUSE_DOWN
	
	t = SAModel.allTriggers.newTrigger(leftLocation.sensor)
	t.triggerValue = leftLocation.value
	t.condition = leftLocation.condition
	t.action = action
	t.actionParam = SACustom.MOUSE_LEFT

	t = SAModel.allTriggers.newTrigger(rightLocation.sensor)
	t.triggerValue = rightLocation.value
	t.condition = rightLocation.condition
	t.action = action
	t.actionParam = SACustom.MOUSE_RIGHT

	SASensorUI.reloadTriggers()
		
	endSolutionProcessing()
	return
		
