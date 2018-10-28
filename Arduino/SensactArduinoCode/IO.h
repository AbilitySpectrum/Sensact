// -------------------------------------
// IO.h
// -------------------------------------

#ifndef IO_H
#define IO_H

#include <EEPROM.h>
#include "Sensact.h"

/*
 * Code to get and put various data types from/to a stream.
 * The types are:
 *  char  - a char
 *  num   - a 2 byte (4 nibble) number (may be negative)
 *  long  - a 4 byte (8 nibble) number
 *  ID    - a 1 byte (2 nibble) value
 *  State - a 1 byte (1 nibble) value
 *  Condition - a '1', '2' or '3'
 *  Boolean - 'p' for true, 'q' for false
 *
 *  Two stream types are supported: Serial and EEPROM
 */

// getNum() can return 2-byte negative numbers, so IO_NUMERROR 
// needs to be a negative value that cannot fit in 2 bytes.
// This will mean that a 4-byte value (action parameters) cannot 
// be this large negative number.
#define IO_NUMERROR -66000
#define IO_ERROR -1

class InputStream {
  public:
    virtual void init() = 0;
    virtual int _getChar() = 0;
    
    int getChar();    // Filters out end-of-line characters.
    long getNum() { return _getNumber(4); }
    long getLong() { return _getNumber(8); }
    int  getID() { return _getChar(2); }
    int  getState() { return _getChar(1); }
    int  getCondition();
    int  getBool(); 
    long _getNumber(int nbytes);
    int  _getChar(int nBytes);
};

class OutputStream {
  public:
    virtual void init() = 0;
    virtual void putChar(int c) = 0;
    
    void putLong(long val) { _putNumber(val, 8); }
    void putNum(int val) { _putNumber(val, 4); }   
    void putID(char ID);
    void putState(char state);
    void putCondition(char condition);
    void putBool(char b);

    void _putNumber(long val, int nbytes);
};

// --- Serial Port I/O --- //
class SerialInputStream : public InputStream {
  public:
    void init() {}
    
    int _getChar() {
      int count = 0;
      while (!Serial.available()) {
        delay(10);
        count++;
        if (count > 50) { // Waiting for over 1/2 second
          return IO_ERROR;
        }
      }
      return Serial.read();
    }
};

class SerialOutputStream : public OutputStream {
  public:
    void init() {}
    void putChar(int c) {
      Serial.write(c);
    }
};

// --- EEPROM I/O --- //

class EEPROMInputStream : public InputStream {
  private:
    int index;
  
  public:
    EEPROMInputStream() { index = 0; }
    void init() { index = 0; }
    int _getChar() {
      return EEPROM.read(index++);
    }
};

class EEPROMOutputStream : public OutputStream {
  private:
    int index;
    
  public:
    EEPROMOutputStream() { index = 0; }
    void init() { index = 0; }
    void putChar(int c) {
      EEPROM.write(index++, c);
    }
};
    
#endif
