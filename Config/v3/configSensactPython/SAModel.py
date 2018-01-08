#
# SAModel.py
#
# Core Model classes which describe
# Sensors, Triggers, Actions and collections of these
#

# PROTOCOL VALUES
# Command and Block Headers
from SAStream import *
from SACustom import *
import SASerial
import math

REPORT_MODE = 				b'Q'
RUN_SENSACT = 				b'R'

START_OF_SENSOR_DATA = 		b'S'
START_OF_SENSOR_DATA_ORD =  ord(START_OF_SENSOR_DATA)
START_OF_TRIGGER_BLOCK = 	b'T'
START_OF_TRIGGER_BLOCK_ORD = ord(START_OF_TRIGGER_BLOCK)

REQUEST_TRIGGERS = 			b'U'
GET_VERSION = 				b'V'
GET_VERSION_ORD =			ord(GET_VERSION)

MOUSE_SPEED = 				b'Y'
MOUSE_SPEED_ORD = 			ord(MOUSE_SPEED)

MIN_COMMAND = 				ord('Q')
MAX_COMMAND = 				ord('V')

# Data block separators
TRIGGER_START_ORD =			ord('t')
TRIGGER_END_ORD =			ord('z')
END_OF_BLOCK = 				b'Z'
END_OF_BLOCK_ORD =			ord(END_OF_BLOCK)

TRIGGER_ON_LOW   = 1
TRIGGER_ON_HIGH  = 2
TRIGGER_ON_EQUAL = 3

DEFAULT_STATE = 1

# Version id - overwritten when version info is received.
sensactVersionID = 0

sensorGroups = []	# All sensor groups - set up during startup
sensors = []	# All sensors - set up during startup

def setupLists():
	createSensorList()
	createActionList()

class SensorGroup:
	def __init__(self, name):
		self.name = name
		self.llist = []
		
	def append(self, x):
		self.llist.append(x)
		
	def getList(self):
		return self.llist
		
	def getName(self):
		return self.name	
		
#
# --- Sensors --- #
#
class Sensor:
	def __init__(self, i, n, minval, maxval, c):
		self.id = i
		self.name = n
		self.minval = minval
		self.maxval = maxval
		self.currentValue = 0
		self.isContinuous = c  	# If true the sensor delivers continuous values
								# if false the sensor delivers descrete values
	def getKey(self):
		return self.name

def createSensorList():
	grp = SensorGroup("Input 1")
	grp.append( Sensor(1, "Input 1A", 0, 1023, True) )	
	grp.append( Sensor(2, "Input 1B", 0, 1023, True) )	
	sensorGroups.append(grp)
	
	grp = SensorGroup("Input 2")
	grp.append( Sensor(3, "Input 2A", 0, 1023, True) )
	grp.append( Sensor(4, "Input 2B", 0, 1023, True) )	
	sensorGroups.append(grp)
	
	grp = SensorGroup("Input 3")
	grp.append( Sensor(5, "Input 3A", 0, 1023, True) )	
	grp.append( Sensor(6, "Input 3B", 0, 1023, True) )	
	sensorGroups.append(grp)
	
	grp = SensorGroup("Accel")
	grp.append( Sensor(8, "Accel-X", -16000, 16000, True) )	
	grp.append( Sensor(9, "Accel-Y", -16000, 16000, True) )	
	grp.append( Sensor(10, "Accel-Z", -16000, 16000, True) )	
	sensorGroups.append(grp)
	
	#
	# Gyro values go from -32768 to + 32767, but the high values
	# are for very violent motions.  
	# Here we chop off the highest values to improve sensitivity 
	# for the smaller motions.
	# Simlarly the "Any Motion sensor will deliver values from
	# 0 to 28,377 but we chop off the most violent motions.
	grp = SensorGroup("Gyro")
	grp.append( Sensor(11, "Gyro-X", -15000, 15000, True) )	
	grp.append( Sensor(12, "Gyro-Y", -15000, 15000, True) )	
	grp.append( Sensor(13, "Gyro-Z", -15000, 15000, True) )	
	if sensactVersionID >= 401:
		grp.append( Sensor(14, "Any Motion", 0, 13000, True) )	
	sensorGroups.append(grp)

	grp = SensorGroup("USB Input")
	grp.append( Sensor(7, "USB Input", 0, 255, False) )	
	sensorGroups.append(grp)

	# Create a simple list of all sensors
	for grp in sensorGroups:
		for sens in grp.getList():
			sensors.append(sens)
	
def getSensorById(id):
	for sens in sensors:
		if sens.id == id:
			return sens
	return None


#		
# --- ACTIONS --- #
#
actions = []	# All actions - set up during startup
gActionDict = {}
	
class Action:
	def __init__(self, i, n, optionFunc, default, pcheck):
		self.id = i
		self.name = n
		self.optionFunc = optionFunc
		self.default = default
		self.pcheck = pcheck # A function which checks if parameters are compatible
							 # Used to distinguish BT/HID Keyboard v.s. Special.
		
	def getKey(self):
		return self.name
		
def createActionList():
	actions.append( Action(0, "None",          SAC_None,  0, None) )
	actions.append( Action(1, "Relay A",       SAC_None,  0, None) )
	actions.append( Action(2, "Relay B",       SAC_None,  0, None) )
	if sensactVersionID >= 301:
		actions.append( Action(3, "BT Keyboard",  SAC_KeyOption, 65, lambda p: p >= 32) )
		actions.append( Action(3, "BT Special",   SAC_BTSpecial, 10, lambda p: p < 32) )
		actions.append( Action(9, "Bt Mouse",     SAC_MouseOption,  MOUSE_UP, None) )
		actions.append( Action(4, "HID Keyboard", SAC_KeyOption, 65, lambda p: not (0x100 > p > 0x7f) ) )
		actions.append( Action(4, "HID Special",  SAC_HIDSpecial, 0xB0, lambda p: 0x100 > p > 0x7f) )
	else:  # Older version
		actions.append( Action(3, "BT Keyboard",  SAC_KeyOption, 65, None) )		
		actions.append( Action(4, "HID Keyboard", SAC_KeyOption, 65, None) )
	actions.append( Action(5, "HID Mouse",     SAC_MouseOption,  MOUSE_UP, None) )
#	actions.append( Action(6, "Joystick",      SAC_None,  0, None) )
	actions.append( Action(7, "Buzzer",        SAC_Buzzer, (400 << 16) + 250, None) )
	actions.append( Action(8, "IR",            SAC_IROption, TV_ON_OFF, None) )
	if sensactVersionID >= 400:
		actions.append( Action(6, "Serial", SAC_KeyOption, 65, None) )
	if sensactVersionID >= 302:
		actions.append( Action(10, "Set State", SAC_SetState, 0x101, None) )
	
	# Create action dictionary.  Allows look-up by name
	global gActionDict
	for a in actions:
		gActionDict[a.name] = a
	
	
# getActionById uses param to determine whether the
# action is 'Keyboard' or 'Special'.  Different guis are required.
def getActionByID(id, param):
	global sensactVersionID
	
	for act in actions:
		if act.id == id:
			# No parameter check or a successful parameter check
			# plus and ID match means we have a match.
			if not act.pcheck or act.pcheck(param):
				return act
	return None

def getActionByName(name):
	return gActionDict[name]
	
#	
# --- Triggers --- #
#
# A single trigger
class Trigger:
	def __init__(self, sensor):		
		self.reqdState = DEFAULT_STATE
		if sensor:
			self.setSensor(sensor)
		else:
			self.triggerValue = 0
			self.condition = TRIGGER_ON_HIGH
		self.delay = 0
		self.repeat = False
		self.action = actions[0]
		self.actionParam = 0
		self.actionState = DEFAULT_STATE
		
	def setSensor(self, s):
		self.sensor = s
		if s.isContinuous:
			self.triggerValue = int((s.minval + s.maxval) / 2)
			self.condition = TRIGGER_ON_HIGH
		else:
			self.triggerValue = ord('a')
			self.condition = TRIGGER_ON_EQUAL
			
	def toStream(self, ostream):
		ostream.putChar(ord('\n'))
		ostream.putChar(TRIGGER_START_ORD)
		ostream.putID(self.sensor.id, 2)
		ostream.putID(self.reqdState, 1)
		ostream.putNum(self.triggerValue, 2)
		ostream.putCondition(self.condition)
		ostream.putID(self.action.id, 2)
		ostream.putID(self.actionState, 1)
		ostream.putNum(self.actionParam, 4)
		ostream.putNum(self.delay, 2)
		ostream.putBoolean(self.repeat)
		ostream.putChar(TRIGGER_END_ORD)
	
	def fromStream(self, istream):
		if istream.getChar() != TRIGGER_START_ORD:
			raise IOError("Invalid start of trigger")
		sensorID = istream.getID(2)
		sensor = getSensorById(sensorID)
		if (sensor == None):
			raise IOError("Invalid Sensor ID")
		self.setSensor(sensor)
		self.reqdState = istream.getID(1)
		self.triggerValue = istream.getNum(2)
		self.condition = istream.getCondition()
		actionID = istream.getID(2)
		self.actionState = istream.getID(1)
		self.actionParam = istream.getNum(4)
		self.action = getActionByID(actionID, self.actionParam)
		if (self.action == None):
			raise IOError("Invalid Action ID")
		self.delay = istream.getNum(2)
		self.repeat = istream.getBoolean()
		if (istream.getChar() != TRIGGER_END_ORD):
			raise IOError("Invalid end of trigger")
		
	
# Triggers
#  A collection of triggers
class Triggers:
	def __init__(self):
		self.triggerList = []
	
	def length(self):
		return len(self.triggerList)
	
	def get(self, index):
		return self.triggerList[index]
	
	def replaceTriggers(self, newList):
		self.triggerList = newList

	def newTrigger(self, sensor):
		t = Trigger(sensor)
		self.triggerList.append(t)
		return t
	
	def deleteTriggerSet(self, sensor):
		for t in self.triggerList[:]:
			if t.sensor == sensor:
				self.deleteTrigger(t)
		
	def deleteTrigger(self, t):
		if t:
			if self.triggerList.count(t):
				self.triggerList.remove(t)


allTriggers = Triggers()

# Reading Triggers (from sensact or a file)
def loadTriggers(stream):
	tmpTriggers = []
	readTriggers(tmpTriggers, stream)
	# If there is a read error an exception
	# will be thrown and triggers will not be
	# replaced.
	allTriggers.replaceTriggers(tmpTriggers)

		
def readTriggers(tmpTriggers, stream):
	if stream.getChar() != START_OF_TRIGGER_BLOCK_ORD:
		raise IOError("Invalid start of transmission")
		
	triggerCount = stream.getNum(2)
	for i in range(triggerCount):
		t = Trigger(None)
		t.fromStream(stream)
		tmpTriggers.append(t)
	
	ch = stream.getChar()
	if (ch == MOUSE_SPEED_ORD):
		mouseMovingParams.fromStream(stream)
		ch = stream.getChar()
		
	if ch != END_OF_BLOCK_ORD:
		raise IOError("Invalid end of transmission")
		
# Writing Triggers (to sensact or a file)
def toSensact(data):
	SASerial.write(data)
	
def dummyFunc(data):
	pass
	
def sendTriggersToSensact():
	ostream = OutputStream(toSensact)
	putTriggers(ostream)
	ostream.flush()
	
def prepareTriggersForFile():
	ostream = OutputStream(dummyFunc)
	putTriggers(ostream)
	return ostream._bytes
	
def putTriggers(ostream):
	ostream.putChar(START_OF_TRIGGER_BLOCK_ORD)
	ostream.putNum( allTriggers.length(), 2)
	
	for t in allTriggers.triggerList:
		t.toStream(ostream)
	
	if (sensactVersionID >= 400):
		mouseMovingParams.toStream(ostream)
		
	ostream.putChar(END_OF_BLOCK_ORD)
			
		
class MouseMovingParams:
	def __init__(self):
		pass
		
	# This function allows the user interface component
	# to register it's location with this class.  The
	# UI will be asked for it's raw values whenever there
	# is a need to send them, and will be updated whenever
	# values are read.
	def registerUI(self, ui):
		self.ui = ui
		
	def toStream(self, ostream):
		# Get three speeds and two speed-change times.
		(s1, s2, s3, t1, t2) = self.ui.getRawValues()
		
#		print("P Raw {}, {}, {}, {}, {}".format(s1, s2, s3, t1, t2))
		
		# d* is the # of milliseconds between mouse moves
		# j* is the # of pixels the mouse will go in a single move
		# tt* is the number of moves until the speed changes.
		(d1, j1) = self.convertSpeed(s1)
		(d2, j2) = self.convertSpeed(s2)
		(d3, j3) = self.convertSpeed(s3)
		
#		print("Put d1 {} j1 {} d2 {} j2 {} d3 {} j3 {} t1 {} t2 {}"
#			.format(d1, j1, d2, j2, d3, j3, t1, t2))

		ostream.putChar(ord('\n'))
		ostream.putChar(MOUSE_SPEED_ORD)
		ostream.putNum( 20, 2 ) # Data length
		# Reduce the delay by one.
		# The actual delay will be the first 'tick' where
		# the elapsed time is greater than the delay.
		# We reduce delay by one to ensure we don't overshoot the 'tick'.
		ostream.putID(d1 - 1, 2)
		ostream.putID(j1, 2)
		ostream.putID(d2 - 1, 2)
		ostream.putID(j2, 2)
		ostream.putID(d3 - 1, 2)
		ostream.putID(j3, 2)
		ostream.putNum(t1, 2)
		ostream.putNum(t2, 2)
		
	def fromStream(self,istream):
		num = istream.getNum(2)
		if num != 20:
			for i in range (0, num):
				istream.getChar();
			return
		d1 = istream.getID(2) + 1	
		j1 = istream.getID(2)	
		d2 = istream.getID(2) + 1	
		j2 = istream.getID(2)	
		d3 = istream.getID(2) + 1	
		j3 = istream.getID(2)	
		t1 = istream.getNum(2)	
		t2 = istream.getNum(2)
		
#		print("Got d1 {} j1 {} d2 {} j2 {} d3 {} j3 {} t1 {} t2 {}"
#			.format(d1, j1, d2, j2, d3, j3, t1, t2))
		
		# Now, convert to raw parameters.
		s1 = self.convertBack(d1, j1)
		s2 = self.convertBack(d2, j2)
		s3 = self.convertBack(d3, j3)
		
#		print("G Raw {}  {}  {}  {}  {}"
#			.format(s1, s2, s3, t1, t2))
		
		self.ui.setRawValues(s1, s2, s3, t1, t2)
		
	# Convert the logarithmic value for pixels/second from the Widget
	# to number-of-milliseconds between mouse moves
	# and size of mouse jump.
	# Aim for a speed with a mouse jump of less than 10,
	# but for the highest speeds we get a delay of 24 
	# and a jump of 15.
	def convertSpeed(self, lspeed):
		speed = math.exp(lspeed)
		for delay in range(60, 23, -12): # 60, 48, 36, 24
			jump = int ((speed * delay) / 1000.0 + 0.5)
			if jump < 10:
				break
		
		return (delay, jump)
		
	# Reverse the conversion above.
	def convertBack(self, delay, jump):
		speed = (jump * 1000.0) / delay
		return math.log(speed)
		
# Singleton mouse parameters
mouseMovingParams = MouseMovingParams()


## --- Test Code - Probably obsolete --- ##
def outfunc(data):
	print(data.decode())
	
def doTest():
	global sensactVersionID	
	sensactVersionID = 301
	
	print("Testing 1 2 3")
	setupLists()
	t = allTriggers.newTrigger(sensors[0])
	print("P1 Len ", allTriggers.length())
	ostream = OutputStream(outfunc)
	t.toStream(ostream)
	ostream.flush()
	t = allTriggers.newTrigger(sensors[2])
	print("P2 Len ", allTriggers.length())
	t.actionParam = 10000000
	t.repeat = True
	t.action = actions[3]
	t.actionState = 4
	t.condition = TRIGGER_ON_LOW
	t.delay = 1000
	ostream = OutputStream(outfunc)
	t.toStream(ostream)
	ostream.flush()

	allTriggers.deleteTrigger(t)
	print("P3 Len ", allTriggers.length())
	
	data = b't@CA`aoo2@DD``````md`cnhpz'
	instream = InputStream(data)
	t = allTriggers.newTrigger(None)
	t.fromStream(instream)
	print("S ID = ", t.sensor.id)
	print("reqd = ", t.reqdState)
	print("tVal = ", t.triggerValue)
	print("cond = ", t.condition)
	print("A ID = ", t.action.id, " - " , t.action.name)
	print("A st = ", t.actionState)
	print("Parm = ", t.actionParam)
	print("delay = ", t.delay)
	print("repeat = ", t.repeat)
	
	t = allTriggers.get(0)
	print("S ID = ", t.sensor.id)
	print("reqd = ", t.reqdState)
	print("tVal = ", t.triggerValue)
	print("cond = ", t.condition)
	print("A ID = ", t.action.id, " - " , t.action.name)
	print("A st = ", t.actionState)
	print("Parm = ", t.actionParam)
	print("delay = ", t.delay)
	print("repeat = ", t.repeat)
	
	try:
		data = b't@AA`aoo3@CD````a`d`cnhpz'
		instream = InputStream(data)
		t = allTriggers.newTrigger(None)
		t.fromStream(instream)
	except IOError as err:
		print("ERROR:", err.message)
	
if __name__ == "__main__":
	doTest()	
				
