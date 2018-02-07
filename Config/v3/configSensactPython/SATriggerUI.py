#
# SATriggerUI.py
#
# User interface elements to support triggers
#
import tkinter as tk
from tkinter import ttk
import SAModel
from SAWidgets import ActionCombo, ActionState, Delay
from SAWidgets import RequiredState, SAScale, SAValue


class TriggerUI(tk.Frame):
    def __init__(self, parent, t, dropUIFunc):
        ttk.Frame.__init__(self, parent)

        self.trigger = t
        self.dropUIFunc = dropUIFunc

        self['borderwidth'] = 1
        self['relief'] = 'groove'
        self['padding'] = (5, 3, 5, 8)

        self.deleteBtn = tk.Button(self, text="x", width=2,
                                   command=self.deleteSelf)
        self.conditionFrame = tk.LabelFrame(self, text="Condition")
        self.actionFrame = tk.LabelFrame(self, text="Action")
        self.deleteBtn.grid(column=0, row=0, sticky=("NW"), padx=(0, 2))
        self.conditionFrame.grid(column=1, row=0, sticky="W")
        self.actionFrame.grid(column=1, row=1, sticky="W")
#        self.conditionFrame.pack(anchor='w')
#        self.actionFrame.pack(anchor='w')

        if self.trigger.sensor.isContinuous:
            self.scale = SAScale(self.conditionFrame, self.trigger)
            self.scale.pack(side=tk.LEFT)
        else:
            self.value = SAValue(self.conditionFrame, self.trigger)
            self.value.pack(side=tk.LEFT)

        reqdState = RequiredState(self.conditionFrame, self.trigger)
        reqdState.pack(side=tk.LEFT)
        delay = Delay(self.conditionFrame, self.trigger)
        delay.pack(side=tk.LEFT)

        actionState = ActionState(self.actionFrame, self.trigger)
        actionState.pack(side=tk.LEFT)
        actions = ActionCombo(self.actionFrame, self.trigger,
                              self.actionChanged)
        actions.pack(side=tk.LEFT)

        self.custom = self.trigger.action.optionFunc(self.actionFrame,
                                                     self.trigger)
        self.custom.pack(side=tk.LEFT)

        # Separation Line
#        dummyFrame = tk.Frame(self)
#        dummyFrame.pack(fill=X, anchor='w', pady=10, ipadx=200)
#        dummyFrame['borderwidth'] = 2
#        dummyFrame['relief'] = 'groove'

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
        self.custom.pack(side=tk.LEFT)

    def deleteUIOnly(self):
        self.dropUIFunc(self)
        self.destroy()

    # Delete the UI and the underlying trigger
    def deleteSelf(self, ask=True):
        if ask:
            val = tk.messagebox.askyesno(title="Deletion Warning",
                                         message="Delete trigger?")
            if val is False:
                return

        SAModel.allTriggers.deleteTrigger(self.trigger)
        # Callback into SASensor to remove this
        # object from SASensor.triggerUIList
        self.dropUIFunc(self)
        self.destroy()
