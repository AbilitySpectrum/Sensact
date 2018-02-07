import tkinter as tk
# import SAWidgets
import SAModel
import SATriggerUI
from threading import Lock
# import SATopFrames
import SASolutions

triggerLock = Lock()
calibrating = False


class SensorUIFrame(tk.Frame):
    ''' A frame which holds sensor widgets and content. '''
    def __init__(self, parent, s):
        tk.Frame.__init__(self, parent)

        self.triggerUIList = []
        self.sensor = s

        controlFrame = tk.Frame(self)
        self.triggerFrame = tk.Frame(self)

        controlFrame.grid(column=0, row=0, sticky=("NEW"),
                          pady=5, ipadx=5)

        self.columnconfigure(0, weight=1)
        self.triggerFrame.grid(column=0, row=1, sticky=("NEW"))

        # Control Frame content
        nameLabel = tk.Label(controlFrame, text=s.name, width=10)
        nameLabel['font'] = "TkHeadingFont 11"
        nameLabel.pack(fill=tk.Y, side=tk.LEFT, padx=10)
        newButton = tk.Button(controlFrame, text="New Trigger",
                              command=self.newTrigger)
        delButton = tk.Button(controlFrame, text="Delete All",
                              command=self.deleteAllTriggersAfterAsking)
        newButton.pack(fill=tk.Y, side=tk.LEFT, padx=5)
        delButton.pack(fill=tk.Y, side=tk.LEFT, padx=5)

        # Trigger Frame content
        dummyFrame = tk.Frame(self.triggerFrame)
        dummyFrame.pack(fill=tk.X, pady=(2, 5), ipadx=200)
        dummyFrame['borderwidth'] = 2
        dummyFrame['relief'] = 'groove'

        # initial settings
        self._show_values = False

    def newTrigger(self):  # User pressed the "New Trigger" button
        t = SAModel.allTriggers.newTrigger(self.sensor)
        self.addTrigger(t)

    def addTrigger(self, t):  # Loading pre-defined triggers
        tui = SATriggerUI.TriggerUI(self.triggerFrame, t, self.dropTriggerUI)
        tui.showValues(self._show_values)
        tui.pack(fill=tk.X, padx=(15, 0), pady=5)
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
        val = tk.messagebox.askyesno(title="Deletion Warning",
                                     message="Delete all triggers for " +
                                     self.sensor.name + "?")
        if val is False:
            return
        triggerLock.acquire()
        for t in self.triggerUIList[:]:  # Note this iterates on a copy
                                            # of the list!
            t.deleteSelf(False)
        # Probably unnessary since the callbacks
        # to dropTrigger will have emptied
        # the list.
        self.triggerUIList = []
        triggerLock.release()

    def deleteAllTriggerUI(self):
        triggerLock.acquire()
        # Note this iterates on a copy of the list!
        for t in self.triggerUIList[:]:
            t.deleteUIOnly()
        # Probably unnessary since the callbacks
        # to dropTrigger will have emptied
        # the list.
        self.triggerUIList = []
        triggerLock.release()

    def setValue(self, value):
        triggerLock.acquire()
        tmpList = self.triggerUIList
        triggerLock.release()

        for t in tmpList:
            t.setValue(value)

    def setNewValue(self):  # Called by reporting, after currentValue is set.
        self.setValue(self.sensor.currentValue)

    def showValues(self, show):
        triggerLock.acquire()
        tmpList = self.triggerUIList
        triggerLock.release()
        for t in tmpList:
            t.showValues(show)
        self._show_values = show


sensorUIList = []


def createSensorFrame(parent, grp):
    enclosingFrame = tk.Frame(parent)

    if (grp.name in ("Input 1", "Input 2", "Input 3")):
        btn = SASolutions.SolutionBtn(enclosingFrame, enclosingFrame, grp,
                                      text="Solutions")
        btn.pack(anchor='w', pady=5, padx=5)

    for s in grp.getList():
        sui = SensorUIFrame(enclosingFrame, s)
        sensorUIList.append(sui)
        sui.pack(fill=tk.X, pady=5, expand=True)

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


def doReport(istream):
    ''' Display and record reported values from sensact. '''
    if istream.getChar() != SAModel.START_OF_SENSOR_DATA_ORD:
        raise IOError("Invalid start of sensor data")

    sensorCount = istream.getNum(2)
    for i in range(sensorCount):
        id = istream.getID(2)
        value = istream.getNum(2)
        for s in sensorUIList:
            if s.sensor.id == id:
                s.sensor.currentValue = value
                if not calibrating:
                    s.after(1, s.setNewValue)
#                    s.setValue(value)
#                    s.update_idletasks()


# Turn on value reporting mode in all triggers
def showValues(show):
    for s in sensorUIList:
        s.showValues(show)
