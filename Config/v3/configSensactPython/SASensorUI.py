
from tkinter import *
from tkinter import ttk
from tkinter import messagebox
import SAWidgets
import SAModel
import SATriggerUI
from threading import Lock
import SATopFrames
import SASolutions

triggerLock = Lock()
calibrating = False

# SensorUIFrame
# A frame which holds sensor widgets and content.
class SensorUIFrame(ttk.Frame):
	def __init__(self, parent, s):
		ttk.Frame.__init__(self, parent)
		
		self.triggerUIList = []
		self.sensor = s
		
		controlFrame = ttk.Frame(self)
		self.triggerFrame = ttk.Frame(self)
		
		controlFrame.grid(column=0, row=0, sticky=(N,E,W), pady=5, ipadx=5)

		self.columnconfigure(0, weight=1)
		self.triggerFrame.grid(column=0, row=1, sticky=(N,E,W))
		
		# Control Frame content
		nameLabel = ttk.Label(controlFrame, text=s.name, width=10)
		nameLabel['font'] = "TkHeadingFont 11"
		nameLabel.pack(fill=Y, side=LEFT, padx=10)
		newButton = ttk.Button(controlFrame, text="New Trigger", command=self.newTrigger)
		delButton = ttk.Button(controlFrame, text="Delete All", command=self.deleteAllTriggersAfterAsking)
		newButton.pack(fill=Y, side=LEFT, padx = 5)
		delButton.pack(fill=Y, side=LEFT, padx = 5)
		
		# Trigger Frame content
		dummyFrame = ttk.Frame(self.triggerFrame)
		dummyFrame.pack(fill=X, pady=(2,5), ipadx=200)
		dummyFrame['borderwidth'] = 2
		dummyFrame['relief'] = 'groove'
		
		# initial settings
		self._show_values = False
							
	def newTrigger(self): # User pressed the "New Trigger" button
		t = SAModel.allTriggers.newTrigger(self.sensor)
		self.addTrigger(t)
	
	def addTrigger(self, t): # Loading pre-defined triggers
		tui = SATriggerUI.TriggerUI(self.triggerFrame, t, self.dropTriggerUI)
		tui.showValues(self._show_values)
		tui.pack(fill=X, padx=(15,0), pady=5)		
		triggerLock.acquire()
		self.triggerUIList.append(tui)
		triggerLock.release()

	# dropTriggerUI is called by SATriggerUI when there has been
	# a request to delete a single trigger.
	# This call tells SensorUI that a triggerUI is no longer being used
	# and must be deleted from triggerUIList
	def dropTriggerUI(self, tui):
		if self.triggerUIList.count(tui):
			self.triggerUIList.remove(tui)
			
	def deleteAllTriggersAfterAsking(self):
		val = messagebox.askyesno(title="Deletion Warning",
			message="Delete all triggers for " + self.sensor.name + "?")
		if val == False:
			return
		triggerLock.acquire()
		for t in self.triggerUIList[:]: # Note this iterates on a copy
										 # of the list!
			t.deleteSelf(False)
		self.triggerUIList = [] # Probably unnessary since the callbacks
								# to dropTrigger will have emptied
								# the list.
		triggerLock.release()
		
		
	def deleteAllTriggerUI(self):
		triggerLock.acquire()
		for t in self.triggerUIList[:]: # Note this iterates on a copy
										 # of the list!
			t.deleteUIOnly()
		self.triggerUIList = [] # Probably unnessary since the callbacks
								# to dropTrigger will have emptied
								# the list.
		triggerLock.release()
		
	def setValue(self, value):
		triggerLock.acquire()
		for t in self.triggerUIList:
			t.setValue(value)
		triggerLock.release()
			
	def showValues(self, show):
		triggerLock.acquire()
		for t in self.triggerUIList:
			t.showValues(show)
		triggerLock.release()
		self._show_values = show
		
			
	
sensorUIList = []

	
def createSensorFrame(parent, grp):
	enclosingFrame = ttk.Frame(parent)
	
	if (grp.name in ("Input 1", "Input 2", "Input 3")):
		btn = SASolutions.SolutionBtn(enclosingFrame, enclosingFrame, grp, text="Solutions")
		btn.pack(anchor='w', pady=5, padx=5)
	
	for s in grp.getList():
		sui = SensorUIFrame(enclosingFrame, s)
		sensorUIList.append(sui)
		sui.pack(fill=X, pady=5, expand=True)
	
	return enclosingFrame

def reloadTriggers():
	# Called after successful parsing of trigger info is received.
	# First clear all UI triggers
	for s in sensorUIList:
		s.deleteAllTriggerUI()
		
	# Then load the new ones
	tcount = SAModel.allTriggers.length()
	for i in range(tcount):
		t = SAModel.allTriggers.get(i)
		for s in sensorUIList:
			if t.sensor == s.sensor:
				s.addTrigger(t)
	
# Display and record reported values from sensact.
def doReport(istream):
	if istream.getChar() != SAModel.START_OF_SENSOR_DATA_ORD:
		raise IOError("Invalid start of sensor data")
		
	sensorCount = istream.getNum(2)
	for i in range(sensorCount):
		id = istream.getID(2)
		value = istream.getNum(2)
		for s in sensorUIList:
			if s.sensor.id == id:
				if not calibrating:
					s.setValue(value)
				s.sensor.currentValue = value


# Turn on value reporting mode in all triggers
def showValues(show):
	for s in sensorUIList:
		s.showValues(show)
			
	
	
	
