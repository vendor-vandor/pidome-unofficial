/*
  PiDomeSimpleRgbLedStripController.h - Library for controlling simple led strips.
  Created by John Sirach, June 16, 2014.
  http://www.pidome.org
*/
#ifndef PiDomeSimpleRgbLedStripController_h
#define PiDomeSimpleRgbLedStripController_h

#include "Arduino.h"

class PiDomeSimpleRgbLedStripController
{
  public:
    PiDomeSimpleRgbLedStripController(int pinR, int pinG, int pinB);
    void execCommand(int command);
    void setRGB(int colorChangeCommand, int r, int g, int b);
	void setSingleColor(int colorName, int value);
  private:
    int _pinR;
    int _pinG;
    int _pinB;
    
    int _cValueR;
    int _cValueG;
    int _cValueB;
    
	bool active;
	
    void ledsOn();
    void ledsOff();
    
    void setR(int r);
    void setG(int g);
    void setB(int b);
    
};

#endif
