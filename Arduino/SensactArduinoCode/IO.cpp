// -------------------------------------
// IO.cpp
// -------------------------------------
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * 
    This file is part of the Sensact Arduino software.

    Sensact Arduino software is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sensact Arduino software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this Sensact Arduino software.  
    If not, see <https://www.gnu.org/licenses/>.   
 * * * * * * * * * * * * * * * * * * * * * * * * * * * */ 
 
#include <Arduino.h>
#include "IO.h"

// --- Input Stream --- //
// Getchar filters out any end-of-line characters that may have been
// added to improve readability.
int InputStream::getChar() {
  int val;
  do {
    val = _getChar();
  } while (val == '\n' || val == '\r' || val == ' ');
  return val;
}

long InputStream::_getNumber(int nbytes) {
  boolean negative = false;
  long val = 0;
  int i, ch;
  
  for(i=0; i<nbytes; i++) {
    ch = getChar() - NUMBER_MASK;
    if (ch < 0 || ch > 15) { // ERROR
      return IO_NUMERROR;
    }
    val = (val << 4) + ch;
    if ( (i == 0) && (ch & 0x8) ) {
      negative = true;
    }
  }
  
  if (negative) {
    if (nbytes == 4) {
      val = val - 0x10000L;
    } 
  }
  return val;
}

int InputStream::_getChar(int nbytes){
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

int InputStream::getCondition(){
  char val = 0;
  
  val = getChar() - CONDITION_MASK;
  if (val < 1 || val > 3) {
    return IO_ERROR;
  }
  return val;
}

int InputStream::getBool(){
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
  

  
