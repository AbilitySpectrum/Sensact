#
# SATopFrames.py
# Definition of the top-most frames of the UI
# The buttons, the status bar and the main area.
#

from tkinter import *
from tkinter import ttk
from tkinter import filedialog
import SASerial
import SAModel
import SASensorUI
import SAStream

# Status Area
def statusFrame(parent):
	global statusMessage
	statusMessage = StringVar()
	
	f = ttk.Frame(parent, borderwidth=2, relief='groove')
	statusLbl = ttk.Label(f, textvariable=statusMessage)
	statusLbl.pack(side=LEFT, fill=Y, padx=10)
	return f

# Buttons
def runCommand(event = None):
	SASensorUI.showValues(False)
	SASerial.write(SAModel.RUN_SENSACT) 
	
def reportCommand(event = None):
	SASensorUI.showValues(True) 
	SASerial.write(SAModel.REPORT_MODE)
	
def idleMode(event = None):
	SASensorUI.showValues(False) 
	SASerial.write(SAModel.GET_VERSION)
	
def exportCommand():
	filename = filedialog.asksaveasfilename()
	if filename == '':
		return
	data = SAModel.prepareTriggersForFile()
	f = open(filename, 'wb')
	f.write(data)
	f.close()
	
def doTest():
	data = SAModel.prepareTriggersForFile()
	print(data.decode())
	
def importCommand():
	filename = filedialog.askopenfilename()
	if filename == '':
		return
	f = open(filename, 'rb')
	data = f.read()
	f.close()
	istream = SAStream.InputStream(data)
	try:
		SAModel.loadTriggers(istream)
		SASensorUI.reloadTriggers()	
	except SAStream.IOError as err:
		messagebox.showerror(title="Import Failed", 
			message="Error reading triggers:\n" + err.message)

	
def buttonFrame(parent):
	f = ttk.Frame(parent, borderwidth=2, relief='groove')
	global SAVersionStr
	SAVersionStr = StringVar()
	versionLbl = ttk.Label(f, textvariable=SAVersionStr)
	versionLbl['font'] = 'TKHeadingFont 11'
	SAVersionStr.set("Unknown")
	
	getBtn = ttk.Button(f, text="Get Tiggers")
	setBtn = ttk.Button(f, text="Set Triggers")
	runBtn = ttk.Button(f, text="Run")
	reportBtn = ttk.Button(f, text="Display")
	idleBtn = ttk.Button(f, text="Idle")
	exBtn = ttk.Button(f, text="Export")
	imBtn = ttk.Button(f, text="Import")
	testBtn = ttk.Button(f, text="Test")
	
	getBtn['command'] = lambda: SASerial.write(SAModel.REQUEST_TRIGGERS)
	setBtn['command'] = SAModel.sendTriggersToSensact
	runBtn['command'] = runCommand
	reportBtn['command'] = reportCommand
	idleBtn['command'] = idleMode
	exBtn['command'] = exportCommand
	imBtn['command'] = importCommand

	versionLbl.pack(pady=(10, 5), padx=5)
	getBtn.pack(fill=X, pady=(20, 5), padx=5)
	setBtn.pack(fill=X, pady=3, padx=5)
	runBtn.pack(fill=X, pady=(20,3), padx=5)
	reportBtn.pack(fill=X, pady=3, padx=5)
	idleBtn.pack(fill=X, pady=3, padx=5)
	exBtn.pack (fill=X, pady=(20,3), padx=5)
	imBtn.pack (fill=X, pady=3, padx=5)
	
#	testBtn = ttk.Button(f, text="Test")
#	testBtn['command'] = doTest
#	testBtn.pack (fill=X, pady=3, padx=5)

	getBtn.bind('<Enter>', lambda x: statusMessage.set("Get configuration from sensact"))
	setBtn.bind('<Enter>', lambda x: statusMessage.set("Re-configure the sensact"))
	runBtn.bind('<Enter>', lambda x: statusMessage.set("Put the sensact into Run mode"))
	reportBtn.bind('<Enter>', lambda x: statusMessage.set("Display sensor values"))
	idleBtn.bind('<Enter>', lambda x: statusMessage.set("RPut the sensact into Idle mode"))
	exBtn.bind('<Enter>', lambda x: statusMessage.set("Save configuration to a file"))
	imBtn.bind('<Enter>', lambda x: statusMessage.set("Read configuration from a file"))
	
	getBtn.bind('<Leave>', lambda x: statusMessage.set(""))
	setBtn.bind('<Leave>', lambda x: statusMessage.set(""))
	runBtn.bind('<Leave>', lambda x: statusMessage.set(""))
	reportBtn.bind('<Leave>', lambda x: statusMessage.set(""))
	idleBtn.bind('<Leave>', lambda x: statusMessage.set(""))
	exBtn.bind('<Leave>', lambda x: statusMessage.set(""))
	imBtn.bind('<Leave>', lambda x: statusMessage.set(""))
	
	return f


# Tabs

def tabFrame(parent):
	global tabsFrame
	tabsFrame = ttk.Frame(parent, borderwidth=2, relief='groove')
	
	return tabsFrame

def onconfigure(x):
	canvas.configure(scrollregion=canvas.bbox('all'))
	
def saveTabId():
	global tabid
	global tabs
	
	tabid = tabs.select()
#	print("Saved tab id:", tabid)
	
def loadSavedTab():
	global tabid
	global tabs
	
#	print("Restoring tab id:", tabid)
	tabs.select(tabid)
	
def loadTabs():	
	global canvas
	global tabs
		
	tabs = ttk.Notebook(tabsFrame)
	tabs.grid(column=0, row=0, sticky=(N,E,S,W))
	tabsFrame.columnconfigure(0, weight=1)
	tabsFrame.rowconfigure(0, weight=1)

	for grp in SAModel.sensorGroups:
		frame = ScrollingSensorUI(tabs, grp)
		tabs.add(frame, text=grp.getName())
	
	
class ScrollingSensorUI(ttk.Frame):
	def __init__(self, parent, grp):
		ttk.Frame.__init__(self, parent)
		
		self.canvas = Canvas(self)
		self.scroll = ttk.Scrollbar(self, orient="vertical", command=self.canvas.yview)
		self.canvas['yscrollcommand'] = self.scroll.set
		
		self.scroll.pack(side="right", fill="y")
		self.canvas.pack(side="left", fill="both", expand=True)
		
		self.innerFrame = SASensorUI.createSensorFrame(self.canvas, grp)
		self.canvas.create_window((0,0), window=self.innerFrame, anchor='nw')
		self.innerFrame.bind('<Configure>', self.onconfigure)
		
	def onconfigure(self, event):
		self.canvas.configure(scrollregion=self.canvas.bbox('all'))
	

