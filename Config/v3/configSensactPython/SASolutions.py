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
		self.mouseType = "HID"
		self.clearButtons()
		gSyncEvent.set()
		
	def btBtn(self):
		self.mouseType = "BT"
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
class MinMax:
	def __init__(self, allowance):
		self.valsSet = False
		self.allowance = allowance
	
	def nextValue(self, val):
		if (self.valsSet):
			if (val < self.min):
				self.min = val
			if (val > self.max):
				self.max = val
		else:
			self.min = self.max = val
			
	def inRange(self, val):
		if (self.min - self.allowance) <= val <= (self.max+self.allowance):
			return True
		else:
			return False
			
			
class Calibration:
	def __init__(self, group):
		self.groupList = group.getList()
		self.allowance = int((self.groupList[0].maxval - self.groupList[0].minval) / 10)
		self.startValues = []
		# Create a list of min/max counters the same length and order as the group
		for sens in self.groupList:
			self.startValues.append(MinMax(self.allowance))
		
	def run(self):
		# get initial values
		SASensorUI.showValues(False)
		SASensorUI.calibrating = True
		SASerial.write(SAModel.REPORT_MODE)
		
		retval = self.doDetails()
		
		SASerial.write(SAModel.RUN_SENSACT)
		SASensorUI.calibrating = False
		
		return retval
		
	def doDetails(self):
		for i in range(5):
			time.sleep(100.0/1000.0)
			for j in range(len(self.startValues)):
				self.startValues[j].nextValue( self.groupList[j].currentValue )
			
		updateMessage("Calibration", "Please press the button")
		if gCancelAction:
			return False
		
		found = False
		count = 0
		while(not found):
			count += 1
			if count > 50:
				break
			time.sleep(100.0/1000.0)
			
			for j in range(len(self.startValues)):
				if (not self.startValues[j].inRange( self.groupList[j].currentValue )):
					self.targetSensor = self.groupList[j]
					self.offValue = self.startValues[j]
					found = True
					break
			
		if  not found:
			return False
			
		self.onValue = MinMax(self.allowance)
		for i in range(5):
			time.sleep(100.0/1000.0)
			self.onValue.nextValue( self.targetSensor.currentValue )
					
		updateMessage("Calibration", "Thank you")
		time.sleep(2)
		if gCancelAction:
			return False
						
		if self.onValue.min < self.offValue.min:
			# On is the smaller value
			self.activeAt = self.onValue.max + self.allowance
			self.inactiveAt = self.offValue.min - self.allowance
			self.activeInv = True
		else:
			self.activeAt = self.onValue.min - self.allowance
			self.inactiveAt = self.offValue.max + self.allowance
			self.activeInv = False
			
#		print ("Sensor:", self.targetSensor.name)
#		print ("Active At:", self.activeAt)
#		print ("Inactive At:", self.inactiveAt)
#		print ("Active Inv:", self.activeInv)
		
		return True

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
		return
	gSolutionWindow.event_generate('<<MouseSelect>>')
	gSyncEvent.wait()
	return gSolutionWindow.mouseType

# Interval selection
def intervalSelection():
	gSyncEvent.clear()
	if gCancelAction:
		return
	gSolutionWindow.event_generate('<<IntervalSelect>>')
	gSyncEvent.wait()
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
	
	cal = Calibration(group)
	success = cal.run()
	if gCancelAction:
		return
	if not success:
		updateMessage("Solution Failure", "Sorry, no button detected")
		time.sleep(2)
		endSolutionProcessing()
		return
		
	mouse = mouseSelection()
	if gCancelAction:
		return
		
	if cal.activeInv:
		onCondition = SAModel.TRIGGER_ON_LOW
		offCondition = SAModel.TRIGGER_ON_HIGH
	else:
		onCondition = SAModel.TRIGGER_ON_HIGH
		offCondition = SAModel.TRIGGER_ON_LOW

	if mouse == 'BT':
		action = SAModel.getActionByID(9, 0) # BT id is 9
	else:
		action = SAModel.getActionByID(5, 0) # HID id is 5
	buzzer = SAModel.getActionByID(7,0) # Buzzer
	
	delay = intervalSelection()
	if gCancelAction:
		return
	
	SAModel.allTriggers.deleteTriggerSet(cal.targetSensor)
	for t in oneBtnMouseList:
		tr = SAModel.allTriggers.newTrigger(cal.targetSensor)
		if t.condition == 'H':
			tr.triggerValue = cal.activeAt
			tr.condition = onCondition
		else:
			tr.triggerValue = cal.inactiveAt
			tr.condition = offCondition
		
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

def leftMouseBtn(group):
	global gCancelAction	
	gCancelAction = False
	
	cal = Calibration(group)
	success = cal.run()
	if gCancelAction:
		return
	if not success:
		updateMessage("Solution Failure", "Sorry, no button detected")
		time.sleep(2)
		endSolutionProcessing()
		return
		
	mouse = mouseSelection()
	if gCancelAction:
		return
		
	SAModel.allTriggers.deleteTriggerSet(cal.targetSensor)
	t1 = SAModel.allTriggers.newTrigger(cal.targetSensor)
	t2 = SAModel.allTriggers.newTrigger(cal.targetSensor)
	if mouse == 'BT':
		t1.action = t2.action = SAModel.getActionByID(9, 0) # BT id is 9
	else:
		t1.action = t2.action = SAModel.getActionByID(5, 0) # HID id is 5
	
	t1.triggerValue = cal.activeAt
	t2.triggerValue = cal.inactiveAt
	
	t1.actionParam = SACustom.MOUSE_PRESS
	t2.actionParam = SACustom.MOUSE_RELEASE
	
	if cal.activeInv:
		t1.condition = SAModel.TRIGGER_ON_LOW
		t2.condition = SAModel.TRIGGER_ON_HIGH
	else:
		t2.condition = SAModel.TRIGGER_ON_LOW
		t1.condition = SAModel.TRIGGER_ON_HIGH

	SASensorUI.reloadTriggers()
		
	endSolutionProcessing()
	return

def joystickMouse(group):
	global gCancelAction	
	gCancelAction = False
