// -------------------------------------
// IO.cpp
// -------------------------------------

#include <Arduino.h>
#include "IO.h"

// --- Input Stream --- //
// Getchar filters out any end-of-line characters that may have been
// added to improve readability.
int InputStream::getChar() {
  int val;
  do {
    val = _getChar();
  } while (val == '\n' || val == '\r');
  return val;
}

long InputStream::_getNumber(int nbytes) {
  boolean negative = false;
  long val = 0;
  int i, ch;
  
  for(i=0; i<nbytes; i++) {
    ch = getChar() - NUMBER_MASK;
    if (ch < 0 || ch > 15) { // ERROR
      return IO_ERROR;
    }
    val = (val << 4) + ch;
    if ( (i == 0) && (ch & 0x8) ) {
      negative = true;
    }
  }
  
  if (negative) {
    if (nbytes == 4) {
      val = val - 0x10000L;
    } else {
      return IO_ERROR;
    }
  }
  return val;
}

char InputStream::_getChar(int nbytes){
  char val = 0;
  int ch, i;
  
  for(i=0; i<nbytes; i++) {
    ch = getChar() - ID_MASK;
    if (ch < 0 || ch > 15) { // ERROR
      return IO_ERROR;
    }
    val = (val << 4) + ch;
  }
  return val;
}

char InputStream::getCondition(){
  char val = 0;
  
  val = getChar() - CONDITION_MASK;
  if (val < 1 || val > 3) {
    return IO_ERROR;
  }
  return val;
}

char InputStream::getBool(){
  int ch = getChar();
  if (ch == BOOL_TRUE) return 1;
  else if (ch == BOOL_FALSE) return 0;
  else return IO_ERROR;
}

// --- Output Stream --- //
  void OutputStream::_putNumber(long val, int nbytes) {
    for(int i=(nbytes-1); i>=0; i--) {
      putChar(( (val >> i*4) & 0xf) + NUMBER_MASK);
    }
  }
  
  void OutputStream::putID(char ID) {
    for(int i=1; i>=0; i--) {
      putChar(( (ID >> i*4) & 0xf) + ID_MASK);
    }
  }
  
  void OutputStream::putState(char state) {
    putChar( (state & 0xf) + ID_MASK);
  }
  
  void OutputStream::putCondition(char condition) {
    putChar(condition + CONDITION_MASK);
  }
  
  void OutputStream::putBool(char b) {
    if (b) putChar(BOOL_TRUE);
    else putChar(BOOL_FALSE);
  }
  

  
