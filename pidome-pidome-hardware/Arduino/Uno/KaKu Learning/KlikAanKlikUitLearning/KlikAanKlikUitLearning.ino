/*
* Demo for RF remote switch receiver. 
* This example is for the new KaKu / Home Easy type of remotes!

* For details, see NewRemoteReceiver.h!
*
* This sketch shows the received signals on the serial port.
* Connect the receiver to digital pin 2.
*/

#include <NewRemoteReceiver.h>
#include <NewRemoteTransmitter.h>

boolean received = false;
boolean began    = false;
void setup() {
  Serial.begin(115200);
  NewRemoteReceiver::init(0, 2, showCode);
}

void loop() {
  if(began==false) {  Serial.println("stat:begin"); began=true; }
  while (Serial.available() > 0) {
    unsigned long address = Serial.parseInt(); 
    byte unit = Serial.parseInt(); 
    int switchType = Serial.parseInt(); 
    byte level = Serial.parseInt();
    unsigned int length = Serial.parseInt();
    
    // look for the newline. That's the end of your
    // sentence:
    if (Serial.read() == '\n') {
      received = true;
    }
    if(received==true){
      NewRemoteReceiver::disable();
      NewRemoteTransmitter transmitter(address, 11,length);
      if (switchType == 2) {
        // Dimmer signal received
        transmitter.sendDim(unit, level);
      } else {
        // On/Off signal received
        bool isOn = (switchType == 1 || switchType == 3);
        if (unit==999999) {
          // Send to the group
          transmitter.sendGroup(isOn);
        } else {
          // Send to a single unit
          transmitter.sendUnit(unit, isOn);
        }
      }
      NewRemoteReceiver::enable();
      Serial.println("stat:send");
      received = false;
    }
  }
}

// Callback function is called only when a valid code is received.
void showCode(NewRemoteCode receivedCode) {
  // Note: interrupts are disabled. You can re-enable them if needed.
  
  // Print the received code.
  Serial.print(receivedCode.address);
  Serial.print(":");
  
  if (receivedCode.groupBit) {
    Serial.print("999999");
  } else {
    Serial.print(receivedCode.unit);
  }
  Serial.print(":");
  switch (receivedCode.switchType) {
    case NewRemoteCode::off:
      Serial.print("0:0");
      break;
    case NewRemoteCode::on:
      Serial.print("1:1");
      break;
    case NewRemoteCode::dim:
      Serial.print("2:");
      Serial.print(receivedCode.dimLevel);
      break;
    case NewRemoteCode::on_with_dim:
      Serial.print("3:");
      Serial.print(receivedCode.dimLevel);
      break;
  }
  Serial.print(":");
  Serial.println(receivedCode.period);
}


