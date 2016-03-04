#include <RemoteTransmitter.h>



// Intantiate a new ActionTransmitter remote, use pin 11
ActionTransmitter actionTransmitter(3);
// Intantiate a new KaKuTransmitter remote, also use pin 11 (same transmitter!)
//KaKuTransmitter kaKuTransmitter(11);
// Intantiate a new Blokker remote, also use pin 11 (same transmitter!)
//BlokkerTransmitter blokkerTransmitter(11);
// Intantiate a new Elro remote, also use pin 11 (same transmitter!)
//ElroTransmitter elroTransmitter(11);

FlamingoSwitch flamingoSwitch(3);
 
int RF_RX_PIN = 2;
int RF_TX_PIN = 3;

int RF_RX_NOTIF_PIN = A0;
int RF_TX_NOTIF_PIN = A1;

int SRL_RX_NOTIF_PIN = 4;
int SRL_TX_NOTIF_PIN = 5;

int RF_ON_PIN     = 8;
int RF_OFF_PIN    = 7;

int POWER_PIN = 13;

boolean stringComplete = false;  // whether the string is complete

boolean RF_ON = false;

boolean inits = false;

// deviceType[0] = N = native, F = falamingo on/off switch (keep char) A=SelectRemote, E=Elro
// deviceType[1] = Flamingo syscode (1-5 (also on the remote device)) (convert to unsigned short) char '1' = int(49), so calculating 49 - 48 (represents 0) results in real int number 1. code example device 1: (deviceType[1])-48 = (49) - 48 = 1.
// deviceType[2] = Flamingo devicecode (char 1); A,B,C,D,E = 6,7,8,9,10 (on the remote flamingo device)
// deviceType[3] = Flamingo on/off single char: T=true,F=false
// deviceType[4] = Null terminarting character = '\0';
char deviceType[4];
char composeString[3]={""};
char ID[3]={'I','D','\0'};

byte charCounter = 0;

void setup(){
  Serial.begin(57600);
}
 
void loop(){
  if(stringComplete==true){
    if(!strcmp(composeString,ID)){
      sendSerial("NATIVE");
    } else {
      if(deviceType[0]=='A'){
        digitalWrite(RF_TX_NOTIF_PIN,  HIGH);
        actionTransmitter.sendSignal(deviceType[1]-48,deviceType[2],((deviceType[3]=='T')?true:false));
        digitalWrite(RF_TX_NOTIF_PIN,  LOW);
        sendSerial("SEND:ACTION");
      } else if(deviceType[0]=='F'){
        digitalWrite(RF_TX_NOTIF_PIN,  HIGH);
        flamingoSwitch.sendSignal(deviceType[1]-48,deviceType[2],((deviceType[3]=='T')?true:false));
        digitalWrite(RF_TX_NOTIF_PIN,  LOW);
        sendSerial("SEND:FLAMINGO");
      }
    }
    composeString[0] = '0';
    deviceType[0]='0';
    stringComplete = false;
    charCounter=0;
  }
}

void sendSerial(String srlMsg){
  digitalWrite(SRL_TX_NOTIF_PIN, HIGH); 
  Serial.print("ARD_RF_ELROBCLONES_");Serial.println(srlMsg);
  digitalWrite(SRL_TX_NOTIF_PIN, LOW);
}

void serialEvent() {
  digitalWrite(SRL_RX_NOTIF_PIN, HIGH);
  while (Serial.available()) {
    char inChar = (char)Serial.read(); 
    if (inChar == '\n') {
      stringComplete = true;
      composeString[2] = '\0';
      deviceType[charCounter+1] = '\0';
    } else {
      if(charCounter==0){
        deviceType[0]=inChar;
      } else {
        if (deviceType[0]=='N'){
          if(charCounter<=2){
            composeString[charCounter-1] = inChar;
          }
        } else {
          deviceType[charCounter] = inChar;
        }
      }
      charCounter++;
    }
  }
  digitalWrite(SRL_RX_NOTIF_PIN, LOW);
}
