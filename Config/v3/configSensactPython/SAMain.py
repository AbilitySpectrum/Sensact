#!/usr/bin/env python3
#
# SAMain.py
#

from tkinter import *
from tkinter import ttk
from tkinter import messagebox
import SASerial
import SAStream
import SATopFrames
import SAModel
import SASensorUI
import PortSelectDlg
import threading

versionEvent = threading.Event()
versionStr = ""

#
# dispatcher gets data from the thread that is reading serial
# data.  When a 'Z' is read (end of a data stream) the block of 
# data read is passed to the dispatcher, which figures out
# what to do with it.
#
def dispatcher(data):
	global versionStr
	if (data[0] == SAModel.GET_VERSION_ORD):
		# Version string is in the form Vn.mZ
		# where n and m are the major and minor version numbers.
		substr = data[1:-1]
		versionStr = substr.decode()
		parts = substr.split(b'.')
		SAModel.sensactVersionID = int(parts[0]) * 100 + int(parts[1])
		versionEvent.set()	# Signal that version number was received.
		
	elif (data[0] == SAModel.START_OF_SENSOR_DATA_ORD):
		# This is current sensor value information
		try:
			istream = SAStream.InputStream(data)
			SASensorUI.doReport(istream)
		except IOError as err:
			messagebox.showerror(title="Reporting error", 
				message="Error receiving sensor values:\n" + err.message)			
			
		
	elif (data[0] == SAModel.START_OF_TRIGGER_BLOCK_ORD):
		# This is the report of currently loaded triggers
		istream = SAStream.InputStream(data)
		try:
			SAModel.loadTriggers(istream)
			SASensorUI.reloadTriggers()
			SATopFrames.statusMessage.set("Trigger load successful")
			
		except IOError as err:
			messagebox.showerror(title="Load Failed", 
				message="Error reading triggers:\n" + err.message)
		
	else:
		messagebox.showerror(title="Unknown Data", message="Unknown data received")

#
# Connection establishment and recovery logic and UI
#
# Global
SavedGoodPortDesc = None

def serialConnect(root):	
	connectionSuccess = False
	tryAuto = True  # First time auto-select port based on name.
	while not connectionSuccess:
		SASerial.close_port()
		port = portSelection(root, tryAuto)
		if port is None:
			return False

		connected = False  # Assumed for now
		try:
			SASerial.open_port(port.device)	
			SASerial.init_reading(dispatcher)	
			connected = True
		
		except Exception:
			val = messagebox.askyesno(title="Connection failed", 
				message="Connection failed.\nWould you like to try again?")
			if val == False:
				return False
		
		if connected:
			SASerial.write(SAModel.GET_VERSION) 
			# Wait for recipt of version number
			if versionEvent.wait(timeout=2) == False:
				val = messagebox.askyesno(title="Communication Error", 
				 message="This does not appear to be a Sensact device.\nWould you like to try again?")
				if val == False:
					return False
			else:
				connectionSuccess = True
		tryAuto = False  # On a retry display the list of available ports.
	
	global SavedGoodPortDesc
	SavedGoodPortDesc = port.description
	return True
	
def portSelection(root, tryAuto):
	matchPort = None
	
	if (SavedGoodPortDesc != None):
		matchPort =  SavedGoodPortDesc
	else:
		matchPort = "Leonardo"
	
	availablePorts = SASerial.get_list()

	if tryAuto:
		for onePort in availablePorts:
			if (onePort.description.count(matchPort) > 0):
				return onePort

	psd = PortSelectDlg.PortSelectDlg(root, availablePorts)
	root.wait_window(psd.dlg)
	if (psd.cancelled):
		return None
	target = psd.selected
	del psd
	return target
	
def attemptReconnection():
	val = messagebox.askyesno(title="Communication Error", 
	 message="The connection to Sensact has been lost.\nWould you like to try to re-establish it?")
	if (val):
		messagebox.showinfo(title="Reconnection",
		  message="Please ensure the Sensact is connected")
		if serialConnect(globalRoot):
			SATopFrames.statusMessage.set("Connected to {}".format(SavedGoodPortDesc))
			return
		else:
			exit()
	else:
		exit() 

	
#
# Main Line
#	
def mainScreen(root):
	# Main frames
	tabsFrame =   SATopFrames.tabFrame(root)
	statusFrame = SATopFrames.statusFrame(root)
	buttonFrame = SATopFrames.buttonFrame(root)

	# Frame layout
	buttonFrame.grid(column=0, row=0, sticky=(N,S),     padx = 2, pady = 2)
	tabsFrame.grid  (column=1, row=0, sticky=(N,S,E,W), padx = 2, pady = 2)
	statusFrame.grid(column=0, row=1, sticky=(E,W),     padx = 2, pady = 2, columnspan=2)

	root.columnconfigure(0, weight=0)
	root.columnconfigure(1, weight=1)
	root.rowconfigure(0, weight=1)
	root.rowconfigure(1, weight=0)	
	
	root.bind('<Control-Key-r>', SATopFrames.runCommand)	
	root.bind('<Control-Key-d>', SATopFrames.reportCommand)	
	root.bind('<Control-Key-i>', SATopFrames.idleMode)	
	root.geometry("1000x800+50+50")

def defineStyles():
	s = ttk.Style()
#	s.configure('TLabelFrame', relief='groove', foreground='black')
#	s.configure('toggle.TButton', font='TKHeadingFont 14')
	
	# Font for all buttons
	s.configure('TButton', font='TKHeadingFont 11')
	
	# Font for the tab text.
	s.configure('TNotebook.Tab', font='TKHeadingFont 11', padding = 6)
#	s.map('toggle.TFrame', foreground=[('active', 'red')])
#	s.map('toggle.TButton', foreground=[('active', 'red')])
	
def main():
	global globalRoot
	root = Tk()
	globalRoot = root
	root.title("Sensact Configuration Tool")
	defineStyles()	
	mainScreen(root)	
	
	if serialConnect(root) == False:
		return 
	
	SATopFrames.SAVersionStr.set("Version " + versionStr)
	SATopFrames.statusMessage.set("Connected to {}".format(SavedGoodPortDesc))
	SAModel.setupLists()	# Version number (above) is needed for this to be correct
	SATopFrames.loadTabs() 	# sensor and action lists must be accurate (previous line)
	mainloop()
		
if __name__ == '__main__':
	main()
	
