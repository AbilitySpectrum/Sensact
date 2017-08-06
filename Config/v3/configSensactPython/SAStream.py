#
# SAStream.py
#
# Defines input and output streams

# Value Encoding
NUMBER_MASK = 0x60
ID_MASK = 0x40
CONDITION_MASK = ord(b'0')
BOOL_TRUE = ord(b'p')
BOOL_FALSE = ord(b'q')
TRIGGER_MASK = ord(b'0')

class IOError(Exception):
	def __init__(self, message):
		self.message = message

# -------------------------------------------------		
# Class
# InputStream: Convert a byte array into a stream of values
#
class InputStream :
	
	def __init__(self, data):
		self._bytes = data
		self._index = 0
		
	def _next(self):
		if self._index == len(self._bytes):
			raise IOError("End of stream")
		tmp = self._bytes[self._index]
		self._index += 1
		return tmp
		
	def getChar(self):
		tmp = self._next()
		
		while tmp == ord(b'\n') or tmp == ord(b'\r') or tmp == ord(b' '):
				tmp = self._next()
		return tmp
		
	# Get a number of 'length' bytes.
	# One character per nibble
	# 2 byte values can be negative (e.g. trigger values for gyro).
	# 4 byte values are parameters, bit are interpreted by action.
	def getNum(self, length):
		negative = False
		value = 0
		for i in range(length*2):
			tmp = self.getChar() - NUMBER_MASK
			if tmp < 0 or tmp > 15:
				raise IOError("Invalid Number")
			value = (value << 4) + tmp
			if (i == 0) and (tmp & 0x8) :
				negative = True
				
		if negative:
			if length == 2:
				value = value - 0x10000
				
		return value
		
	def getID(self, nibbles):
		value = 0
		for i in range(nibbles):
			tmp = self.getChar() - ID_MASK
			if tmp < 0 or tmp > 15:
				raise IOError("Invalid ID")
			value = (value << 4) + tmp
		return value
		
	def getCondition(self):
		tmp = self.getChar() - CONDITION_MASK
		if tmp < 1 or tmp > 3:
			raise IOError("Invalid Condition")
		return tmp
	
	def getBoolean(self):
		tmp = self.getChar()
		if tmp == BOOL_TRUE:
			return True
		elif tmp == BOOL_FALSE:
			return False
		else:
			raise IOError("Invalid Boolean")
		
		
# -----------------------------------------------------------------		
# Class
# OutputStream: Convert a stream of values into a byte array
#
class OutputStream:
	
	def __init__(self, out):
		self._bytes = bytearray()
		self._outfunc = out
	
	def flush(self):
		self._outfunc(self._bytes)
		
	def putChar(self, val):
		self._bytes.append(val)
	
	# Encode a value of 'length' bytes.
	def putNum(self, val, length):
		if length >= 4:
			self.putChar( ((val >> 28) & 0xf) + NUMBER_MASK)
			self.putChar( ((val >> 24) & 0xf) + NUMBER_MASK)
		if length >= 3:
			self.putChar( ((val >> 20) & 0xf) + NUMBER_MASK)
			self.putChar( ((val >> 16) & 0xf) + NUMBER_MASK)
		if length >= 2:
			self.putChar( ((val >> 12) & 0xf) + NUMBER_MASK)
			self.putChar( ((val >> 8) & 0xf) + NUMBER_MASK)
		if length >= 1:
			self.putChar( ((val >> 4) & 0xf) + NUMBER_MASK)
			self.putChar( (val & 0xf) + NUMBER_MASK)
		
	def putID(self, val, nibbles):
		if nibbles >= 2:
			self.putChar( ((val >> 4) & 0xf) + ID_MASK)
		if nibbles >= 1:
			self.putChar( (val & 0xf) + ID_MASK)
			
	def putCondition(self, val):
		self.putChar(val + CONDITION_MASK)
		
	def putBoolean(self, val):
		if val:
			self.putChar(BOOL_TRUE)
		else:
			self.putChar(BOOL_FALSE)


# ------------------------------------------
# Test Code			

class Flush:
	def __init__(self, title):
		self.title = title
		
	def flushFunc(self, b):
		print (self.title, b.decode())
	
def doTest():
	fl = Flush("Output: ");
	bar = OutputStream(fl.flushFunc)
	bar.putNum(2730, 2)
	bar.putNum(-40, 2)
	bar.putNum(268448773, 4)
	bar.putID(18,2)
	bar.putID(6,1)
	bar.putCondition(TRIGGER_ON_HIGH)
	bar.putBoolean(True)
	bar.putChar(ord(b'Z'))
	bar.flush()
	
	foo = InputStream(b'`jjjo\nomha```cd  `eAB\rF2pZ')
	smallNum = foo.getNum(2)		# 2730
	negNum = foo.getNum(2)			# -40
	bigNum = foo.getNum(4)			# 268448773
	bigID = foo.getID(2)			# 18
	smallID = foo.getID(1)			# 6
	condition = foo.getCondition()	# 2
	boolval = foo.getBoolean()		# True
	charval = foo.getChar()			# 90
	
	print ("Smallnum: ", smallNum, " NegNum:", negNum, " Bignum: ", bigNum)
	print ("BigID: ", bigID, " SmallID: ", smallID)
	print ("Condition: ", condition, " Boolean: ", boolval, " Char: ", charval)
	
	foo = InputStream(b'aaaAzz')
	try:
		foo.getNum(2)
	except IOError as err:
		print(err)	# Invalid number
		
	foo = InputStream(b'aaaaaaa')
	try:
		foo.getNum(8)
	except IOError as err:
		print(err)	# End of stream

	foo = InputStream(b'AZx')
	try:
		foo.getID(2)
	except IOError as err:
		print(err)	# Invalid ID

	foo = InputStream(b'aaaaaaa')
	try:
		foo.getCondition()
	except IOError as err:
		print(err)	# Invalid Condition

	foo = InputStream(b'aaaaaaa')
	try:
		foo.getBoolean()
	except IOError as err:
		print(err)	# Invalid Boolean
	
if __name__ == "__main__":
	doTest()
