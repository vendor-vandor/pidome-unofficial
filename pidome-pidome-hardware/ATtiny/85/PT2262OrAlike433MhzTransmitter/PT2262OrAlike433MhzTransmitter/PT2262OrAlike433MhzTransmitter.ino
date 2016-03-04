#include "TinyWireS.h"
#include "RemoteTransmitter.h"

#define I2C_SLAVE_ADDR  0x25 // i2c slave address

// Instantiate a new ActionTransmitter remote, use pin 1
ActionTransmitter actionTransmitter(1);
// Instantiate a new KaKuTransmitter remote, also use pin 1
KaKuTransmitter kaKuTransmitter(1);
// Instantiate a new Blokker remote, also use pin 1
BlokkerTransmitter blokkerTransmitter(1);
// Instantiate a new Elro remote, also use pin 1
ElroTransmitter elroTransmitter(1);
// Instantiate a Famingo AB400S remote, use pin 1
FlamingoSwitch flamingoSwitch(1);

///The first character selects the device to switch: A = Action, B = Blokker, F = flamingo on/off switch (keep char), E=Elro and KS=Kaku short else Kaku long (the S is outside the A....P range)

byte    systemCode;
byte    group;
char    device;
boolean on;
 
void setup(){
  TinyWireS.begin(I2C_SLAVE_ADDR);      // init I2C Slave mode
}

//// We are going to use capital T as a true (84)

void loop(){
  if (TinyWireS.available()){
    byte deviceType = TinyWireS.receive();
    switch(deviceType){
      case 75:
        if(deviceType==75){ /// K
          if(TinyWireS.receive()==83){ /// S
            systemCode = TinyWireS.receive();
            device     = TinyWireS.receive();
            on         = (TinyWireS.receive()==84)?true:false;
            kaKuTransmitter.sendSignal(systemCode,device, on);
          } else {
            systemCode = TinyWireS.receive();
            group      = TinyWireS.receive();
            device     = TinyWireS.receive();
            on         = (TinyWireS.receive()==84)?true:false;
            kaKuTransmitter.sendSignal(systemCode, group, device, on);
          }
        }
      break;
      case 65: /// A
      case 70: /// F
      case 69: /// E
        systemCode = TinyWireS.receive();
        device     = TinyWireS.receive();
        on         = (TinyWireS.receive()==84)?true:false;
        if(deviceType==65){ /// A
          actionTransmitter.sendSignal(systemCode,device,on);
        } else if (deviceType==70){ //// F
          flamingoSwitch.sendSignal(systemCode,device,on);
        } else { /// 69==E
          elroTransmitter.sendSignal(systemCode,device,on);
        }
      break;
      case 66:
        device     = TinyWireS.receive();
        on         = (TinyWireS.receive()==84)?true:false;
        blokkerTransmitter.sendSignal(device, on);
      break;
    }
    deviceType = 0;
    systemCode = 0;
    group      = 0;
    device     = 0;
    on         = 0;
  }
}

