/*
  PiDomeSimpleRgbLedStripController.cpp - Library for controlling simple led strips.
  Created by John Sirach, June 16, 2014.
  http://www.pidome.org
*/

#include "Arduino.h"
#include "PiDomeSimpleRgbLedStripController.h"

  int _pinR = 9;
  int _pinG = 10;
  int _pinB = 11;

  int _cValueR = 0;
  int _cValueG = 0;
  int _cValueB = 0;

  bool active = false;
  
/**
  Constructor.
  @param pinR Pin number for red PWM.
  @param pinG Pin number for green PWM.
  @param pinB Pin number for blue PWM.
 */ 
PiDomeSimpleRgbLedStripController::PiDomeSimpleRgbLedStripController(int pinR, int pinG, int pinB){
  _pinR = pinR;
  _pinG = pinG;
  _pinB = pinB;
  pinMode(_pinR, OUTPUT);
  pinMode(_pinG, OUTPUT);
  pinMode(_pinB, OUTPUT);
  active = false;
}

/// public methods ///

/**
  Executes a predefined command.
  Use 'T' for True to turn on, use 'F' for False to turn off. Multiple commands will follow in the future.
  @param command Takes an integer mapped to the ascii decimal value.
 */ 
void PiDomeSimpleRgbLedStripController::execCommand(int command){
  switch(command){
    case'T':
      ledsOn();
    break;
    case 'F':
      ledsOff();
    break; 
  }
}

/**
  Sets the led color.
  @param r Color value for red (0-255).
  @param g Color value for green (0-255).
  @param b Color value for blue (0-255).
 */ 
void PiDomeSimpleRgbLedStripController::setRGB(int colorChangeCommand, int r, int g, int b){
  setR(r);
  setG(g);
  setB(b);
}

/**
  Sets the a single color value.
  @param colorName Mapped ascii values for 'R', 'G' or 'B', respectively Red, Green or Blue.
  @param value Value for color to set (0-255).
 */ 
void PiDomeSimpleRgbLedStripController::setSingleColor(int colorName, int value){
  switch(colorName){
    case 'R':
	  setR(value);
	break;
    case 'G':
	  setG(value);
	break;
    case 'B':
	  setB(value);
	break;
  }
}

/// private methods

/**
  Sets the red color value.
  @param r The value to set (0-255).
 */ 
void PiDomeSimpleRgbLedStripController::setR(int r){
  _cValueR = constrain(r, 0, 255);
  if(active) analogWrite(_pinR, _cValueR);
}

/**
  Sets the green color value.
  @param g The value to set (0-255).
 */ 
void PiDomeSimpleRgbLedStripController::setG(int g){
  _cValueG = constrain(g, 0, 255);
  if(active) analogWrite(_pinG, _cValueG);
}

/**
  Sets the blue color value.
  @param b The value to set (0-255).
 */ 
void PiDomeSimpleRgbLedStripController::setB(int b){
  _cValueB = constrain(b, 0, 255);
  if(active) analogWrite(_pinB, _cValueB);
}

/**
  Turns on based on the last known color.
 */ 
void PiDomeSimpleRgbLedStripController::ledsOn(){
  active = true;
  setR(_cValueR);
  setG(_cValueG);
  setB(_cValueB);
}

/**
  Turns off.
 */ 
void PiDomeSimpleRgbLedStripController::ledsOff(){
  active = false;
  analogWrite(_pinR, 0);
  analogWrite(_pinG, 0);
  analogWrite(_pinB, 0);
}
