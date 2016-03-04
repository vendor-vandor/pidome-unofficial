/*
  simple_rgb_led_driver.ino - Arduino file to be used with the PiDome default led driver.
  
  Below are the supported serial commands shown, when a command definition shows a range it is displayed 
  as "(0-255)" this means the command supports a number from and including the 0 until and including 255. Example:
  R(0-255) means you can user "R0" until and including "R255" (without the " characters).
  
  This file supports the following serial commands:
  T - Turn on leds.
  F - Turn off leds. When the leds are turned off you can still set the colors.
  
  R(0-255) - Turn red value in the range from 0 to 255.
  G(0-255) - Turn green value in the range from 0 to 255.
  B(0-255) - Turn blue value in the range from 0 to 255.
   
  C(0-255),(0-255),(0-255)
  
*/
#include <PiDomeSimpleRgbLedStripController.h>

PiDomeSimpleRgbLedStripController controller(9,10,11);

void setup(){ 
  Serial.begin(9600);
} 

void loop(){
  while (Serial.available() > 0) {
    int inByte = Serial.read();
    if(inByte!='C'){
      if(inByte=='R' || inByte=='G' || inByte=='B'){
        controller.setSingleColor(inByte, Serial.parseInt());
      } else {
        controller.execCommand(inByte);
      }
    } else {
      int red = Serial.parseInt();
      int green = Serial.parseInt();
      int blue = Serial.parseInt();
      if (Serial.read() == '\n') {
        controller.setRGB(inByte, red, green, blue);
      }
    }
  }
}


