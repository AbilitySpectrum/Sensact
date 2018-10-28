// -------------------------------
// BTMouseCtl.h
// -------------------------------
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
 
#ifndef BTMouseCtl_H
#define BTMouseCtl_H

#include <SoftwareSerial.h>

#define BT_LEFT_BUTTON 1
#define BT_RIGHT_BUTTON 2
#define BT_CENTER_BUTTON 4

class BTMouseCtl {
  SoftwareSerial *pBTHID;
  char buttonState = 0;

  void send(char x, char y) {
    char xmit[7];
    xmit[0] = 0xFD;
    xmit[1] = 5;
    xmit[2] = 2;
    xmit[3] = buttonState;
    xmit[4] = x;
    xmit[5] = y;
    xmit[6] = 0;
    pBTHID->write(xmit, 7);
  }
     
  public:
    BTMouseCtl(SoftwareSerial *p) {
      pBTHID = p;
    }

    void move(char x, char y) {
      send(x, y);
    }

    void press(int btn) {
      buttonState |= btn;
      send(0,0);
    }

    void release(int btn) {
      buttonState &= ~btn;
      send(0,0);
    }

    void click(int btn) {
      press(btn);
      release(btn);
    }   
};

#endif

