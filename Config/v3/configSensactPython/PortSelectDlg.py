
from tkinter import *
from tkinter import ttk
from tkinter import messagebox

class PortSelectDlg:
	def __init__(self, parent, portList):
		self.dlg = Toplevel(parent)
		self.dlg.grab_set()
		self.dlg.transient(parent)
		
		lbl = ttk.Label(self.dlg, text="Select Serial Port")
		lbl['font'] = 'TKHeadingFont 11'

		lbl.pack(pady=5)
		
		for onePort in portList:
			btn = ttk.Button(self.dlg, text=onePort.description,
				command=lambda p=onePort: self.btn(p))
			btn.pack(pady=5)
		
		cancelBtn = ttk.Button(self.dlg, text="Cancel", command=self.cancel)
		cancelBtn.pack(pady=5)
		self.cancelled = True # default.
		
		self.dlg.focus_set()
		self.dlg.update_idletasks()
		self.dlg.geometry("{}x{}+400+400".format(
			self.dlg.winfo_reqwidth()+10, 
			self.dlg.winfo_reqheight()))
		
	def btn(self, port):
		self.selected = port
		self.cancelled = False
		self.dlg.destroy()
		
	def cancel(self):
		self.cancelled = True
		self.dlg.destroy()
		
