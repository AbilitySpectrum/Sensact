#
# SATriggerUI.py
#
# User interface elements to support triggers
#
from tkinter import *
from tkinter import ttk
from tkinter import messagebox
import SAModel
from SAWidgets import *

class TriggerUI(ttk.Frame):
	def __init__(self, parent, t, dropUIFunc):
		ttk.Frame.__init__(self,parent)
		
		self.trigger = t
		self.dropUIFunc = dropUIFunc
		
		self['borderwidth'] = 1
		self['relief'] = 'groove'
		self['padding'] = (5, 3, 5, 8)
		
		self.deleteBtn = ttk.Button(self, text="x", width = 2, command=self.deleteSelf)
		self.conditionFrame = ttk.LabelFrame(self, text="Condition")
		self.actionFrame = ttk.LabelFrame(self, text="Action")
		self.deleteBtn.grid(column=0, row=0, sticky=(N,W), padx=(0,2))
		self.conditionFrame.grid(column=1, row=0, sticky=W)
		self.actionFrame.grid(column=1, row=1, sticky=W)
#		self.conditionFrame.pack(anchor='w')
#		self.actionFrame.pack(anchor='w')

		if self.trigger.sensor.isContinuous:
			self.scale = SAScale(self.conditionFrame, self.trigger)
			self.scale.pack(side=LEFT)
		else:
			self.value = SAValue(self.conditionFrame, self.trigger)
			self.value.pack(side=LEFT)
			
		reqdState = RequiredState(self.conditionFrame, self.trigger)
		reqdState.pack(side=LEFT)
		delay = Delay(self.conditionFrame, self.trigger)
		delay.pack(side=LEFT)
		
		actionState = ActionState(self.actionFrame, self.trigger)
		actionState.pack(side=LEFT)
		actions = ActionCombo(self.actionFrame, self.trigger, self.actionChanged)
		actions.pack(side=LEFT)
		
		self.custom = self.trigger.action.optionFunc(self.actionFrame, self.trigger)
		self.custom.pack(side=LEFT)
		
		# Separation Line
#		dummyFrame = ttk.Frame(self)
#		dummyFrame.pack(fill=X, anchor='w', pady=10, ipadx=200)
#		dummyFrame['borderwidth'] = 2
#		dummyFrame['relief'] = 'groove'

	def showValues(self, show):
		if self.trigger.sensor.isContinuous:
			self.scale.showValues(show)
		
	def setValue(self, value):
		if self.trigger.sensor.isContinuous:
			self.scale.setValue(value)
		
	def actionChanged(self, action):
		self.trigger.action = action
		self.trigger.actionParam = action.default
		self.trigger.repeat = False
		self.custom.destroy()
		self.custom = action.optionFunc(self.actionFrame, self.trigger)
		self.custom.pack(side=LEFT)
	
	# Delete the UI only
	def deleteUIOnly(self):
		self.dropUIFunc(self)
		self.destroy()
			
	# Delete the UI and the underlying trigger
	def deleteSelf(self, ask=True):
		if ask:
			val = messagebox.askyesno(title="Deletion Warning",
			message="Delete trigger?")
			if val == False:
				return
			
		SAModel.allTriggers.deleteTrigger(self.trigger)
		self.dropUIFunc(self) # Callback into SASensor to remove this
							  # object from SASensor.triggerUIList
		self.destroy()
		
