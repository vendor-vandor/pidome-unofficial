/*
 * PiDomeIRTranceiver
 
 Script used to receive and send IR commands. 
 Usage should be as follows:
 Attach with an Arduino to the server's USB port. The server will automatically recognize the device and you can start recording (if set in the media plugin).
 Use the web interface to select a button to assign the IR command to. 
 Point a remote to the receiving led, the receive led will blink. Use the test button to test the received IR. Use this button until the device to control
 responses to it. Most of the times it will be the first time. If after three times the device does not respond, press the record button twice to stop and 
 re-record the signal and try again.
 When the signal is recognized press confirm. The server's web interface will respond to it. When the signal is assigned to the selected button you can
 immediately test it from the web interface by pressing the button.
 
 Repeat the above steps for all the IR commando's you want to record and use in the server.
  
 * http://pidome.org
 */

#include <IRremote.h>

int ON_PIN = 2;

int RECV_PIN = 11;
int BUTTONTEST_PIN = 5;
int BUTTONRECORD_PIN = 6;
int BUTTONCONFIRM_PIN = 7;
int STATUS_PIN = 13;
int RECORDSTATUS_PIN = 4;

int pressCount = 0;

char _REC[4] = {'R','E','C','\0'}; /// Start recording
char _TST[4] = {'T','S','T','\0'}; /// Send a test signal, used to set different Khz modes for raw codes.
char _CNF[4] = {'C','N','F','\0'}; /// Used to send a confirmation back after this string is received. This is the recorded IR signal should only be used with raw commands.
char _SON[4] = {'S','O','N','\0'}; /// Got Sony proto
char _NEC[4] = {'N','E','C','\0'}; /// Got NEC proto
char _RC5[4] = {'R','C','5','\0'}; /// Got RC5 proto
char _RC6[4] = {'R','C','6','\0'}; /// Got RC6 proto
char _PAN[4] = {'P','A','N','\0'}; /// Got RC6 proto
char _JVC[4] = {'J','V','C','\0'}; /// Got RC6 proto
char _RAW[4] = {'R','A','W','\0'}; /// Got RAW proto

byte readCounter = 0;

char IR_ID[4];

boolean RECORD = false;
boolean serialAvailable = false;

IRrecv irrecv(RECV_PIN);
IRsend irsend;

decode_results results;

void setup()
{
  Serial.begin(115200);
  Serial.setTimeout(50);
  irrecv.enableIRIn(); // Start the receiver
  pinMode(BUTTONTEST_PIN, INPUT);
  pinMode(BUTTONRECORD_PIN, INPUT);
  pinMode(BUTTONCONFIRM_PIN, INPUT);
  pinMode(STATUS_PIN, OUTPUT);
  pinMode(RECORDSTATUS_PIN, OUTPUT);
  pinMode(ON_PIN, OUTPUT);
  digitalWrite(ON_PIN, HIGH);
  serialAvailable = true;
}

// Storage for the recorded code
int codeType = -1; // The type of code
unsigned long codeValue; // The code value if not raw
unsigned int rawCodes[RAWBUF]; // The durations if raw
int codeLen; // The length of the code
int toggle = 0; // The RC5/6 toggle state

unsigned int panaAddress; //// The panasonic address
int rawBits; //// used with raw incoming commands

// Stores the code for later playback
// Most of this code is just logging
void storeCode(decode_results *results) {
  codeType = results->decode_type;
  int count = results->rawlen;
  if (codeType == UNKNOWN) {
    codeLen = results->rawlen - 1;
    // To store raw codes:
    // Drop first value (gap)
    // Convert from ticks to microseconds
    // Tweak marks shorter, and spaces longer to cancel out IR receiver distortion
    for (int i = 1; i <= codeLen; i++) {
      if (i % 2) {
        rawCodes[i - 1] = results->rawbuf[i]*USECPERTICK - MARK_EXCESS;
      } 
      else {
        rawCodes[i - 1] = results->rawbuf[i]*USECPERTICK + MARK_EXCESS;
      }
    }
  } else {
    if (codeType == PANASONIC) {
      panaAddress = results->panasonicAddress;
    } else if (codeType == NEC) {
      if (results->value == REPEAT) {
        // Don't record a NEC repeat value as that's useless.
        return;
      }
    } 
    codeValue = results->value;
    codeLen = results->bits;
  }
}

void sendCode(int repeat) {
  digitalWrite(STATUS_PIN, HIGH);
  if (codeType == NEC) {
    if (repeat) {
      irsend.sendNEC(REPEAT, codeLen);
    } 
    else {
      irsend.sendNEC(codeValue, codeLen);
    }
  } else if (codeType == PANASONIC){
    irsend.sendPanasonic(panaAddress,codeValue);
  } else if (codeType == JVC){
    irsend.sendJVC(codeValue, codeLen, repeat); 
  } else if (codeType == SONY) {
    irsend.sendSony(codeValue, codeLen);
  } else if (codeType == RC5 || codeType == RC6) {
    if (!repeat) {
      // Flip the toggle bit for a new button press
      toggle = 1 - toggle;
    }
    // Put the toggle bit into the code to send
    codeValue = codeValue & ~(1 << (codeLen - 1));
    codeValue = codeValue | (toggle << (codeLen - 1));
    if (codeType == RC5) {
      irsend.sendRC5(codeValue, codeLen);
    } 
    else {
      irsend.sendRC6(codeValue, codeLen);
    }
  } 
  else if (codeType == UNKNOWN /* i.e. raw */) {
    if(pressCount==5){
      pressCount = 0;
    }
    switch(pressCount){
      case 0:
        // Assume 36 KHz
        irsend.sendRaw(rawCodes, codeLen, 36);
      break;
      case 1:
        // Assume 37 KHz
        irsend.sendRaw(rawCodes, codeLen, 37);
      break;
      case 2:
        // Assume 38 KHz
        irsend.sendRaw(rawCodes, codeLen, 38);      
      break;
      case 3:
        // Assume 39 KHz
        irsend.sendRaw(rawCodes, codeLen, 39);
      break;
      case 4:
        // Assume 40 KHz
        irsend.sendRaw(rawCodes, codeLen, 40);
      break;
    }
    pressCount++; 
  }
  digitalWrite(STATUS_PIN, LOW);
}

void sendComfirmCode(){
  if(serialAvailable){
    if (codeType == RC5 || codeType == RC6 || codeType == NEC || codeType == SONY || codeType == JVC || codeType == PANASONIC) {
      if (codeType == RC5) {
        Serial.print("RC5|");
      } else if (codeType == RC6) {
        Serial.print("RC6|");
      } else if (codeType == NEC) {
        Serial.print("NEC|");
      } else if (codeType == SONY) {
        Serial.print("SON|");
      } else if (codeType == JVC) {
        Serial.print("JVC|");
      } else if (codeType == PANASONIC){
        Serial.print("PAN|");
        Serial.print(panaAddress, DEC);
        Serial.print("|");
      }
      Serial.print(codeLen);
      Serial.print("|");
      Serial.print(codeValue, DEC);
    } 
    else if (codeType == UNKNOWN /* i.e. raw */) {
      Serial.print("RAW|");
      Serial.print(codeLen);
      Serial.print("|");
      for (int i = 1; i <= codeLen; i++) {
        Serial.print(rawCodes[i - 1], DEC);
        if(i!=codeLen) { Serial.print(","); }
      }
      Serial.print("|");
      switch(pressCount){
        case 0:
          // Assume 36 KHz
          Serial.print("36");
        break;
        case 1:
          // Assume 37 KHz
          Serial.print("37");
        break;
        case 2:
          // Assume 38 KHz
          Serial.print("38");
        break;
        case 3:
          // Assume 39 KHz
          Serial.print("39");
        break;
        case 4:
          // Assume 40 KHz
          Serial.print("40");
        break;  
      }
    }
    Serial.print("!");
    pressCount = 0;
  }
}

int lastButtonTestState;
int lastButtonRecordState;
int lastButtonConfirmState;

void loop() {
  // If button pressed, send the code.
  int buttonTestState = digitalRead(BUTTONTEST_PIN);
  int buttonRecordState = digitalRead(BUTTONRECORD_PIN);
  int buttonConfirmState = digitalRead(BUTTONCONFIRM_PIN);
  
  if (buttonRecordState == HIGH && lastButtonRecordState == LOW) {
    if(RECORD){
      disableReceive();
    } else {
      enableReceive();
    }
  }

  if(RECORD && (buttonConfirmState == HIGH && lastButtonConfirmState == LOW)){
    sendComfirmCode();
  } else if (RECORD && buttonTestState) {
    sendCode(lastButtonTestState == buttonTestState);
    delay(50); // Wait a bit between retransmissions
  } else if (RECORD && irrecv.decode(&results)) {
    digitalWrite(STATUS_PIN, HIGH);
    storeCode(&results);
    delay(50); /// now you know something is received
    digitalWrite(STATUS_PIN, LOW);
    irrecv.resume();
    pressCount = 0;
    if(serialAvailable) Serial.print("RCVD");
  }
  lastButtonTestState = buttonTestState;
  lastButtonRecordState = buttonRecordState;
  lastButtonConfirmState = buttonConfirmState;
  
 
  if (Serial.available() > 0) {
    // read the incoming byte:
    char incomingByte = Serial.read();
    if(readCounter<3){
      IR_ID[readCounter] = incomingByte;
      readCounter++;
    } 
    if (readCounter==3){
      IR_ID[3] = '\0';
      if (strcmp(IR_ID, _REC) == 0){
        if(RECORD==true){
          disableReceive();
        } else {
          enableReceive();
        }
      } else if(strcmp(IR_ID, _TST) == 0){
        pressCount++;
        if(pressCount==5){
          pressCount = 0;
        }
        sendCode(false);
      } else if(strcmp(IR_ID, _CNF) == 0){
        if(serialAvailable) sendComfirmCode();
      } else if(strcmp(IR_ID, _NEC) == 0){
        codeType = NEC;
        codeLen = Serial.parseInt();
        codeValue = Serial.parseInt();
        Serial.read();
        sendCode(false);
      } else if(strcmp(IR_ID, _SON) == 0){
        codeType = SONY;
        codeLen = Serial.parseInt();
        codeValue = Serial.parseInt();
        Serial.read();
        //// Sony requires repeats of the same code.
        sendCode(false);
        delay(100);
        sendCode(false);
        delay(100);
        sendCode(false);
      } else if(strcmp(IR_ID, _RC5) == 0){
        codeType = RC5;
        codeLen = Serial.parseInt();
        codeValue = Serial.parseInt();
        Serial.read();
        sendCode(false);
      } else if(strcmp(IR_ID, _RC6) == 0){
        codeType = RC6;
        codeLen = Serial.parseInt();
        codeValue = Serial.parseInt();
        Serial.read();
        sendCode(false);
      } else if(strcmp(IR_ID, _PAN) == 0){
        codeType = PANASONIC;
        panaAddress = Serial.parseInt();
        Serial.parseInt(); /// Skip code length, this is fixed.
        codeValue = Serial.parseInt();
        Serial.read();
        sendCode(false);
      } else if(strcmp(IR_ID, _JVC) == 0){
        codeType = JVC;
        codeLen = Serial.parseInt();
        codeValue = Serial.parseInt();
        Serial.read();
        sendCode(false);
      } else if(strcmp(IR_ID, _RAW) == 0){
        digitalWrite(STATUS_PIN, HIGH);
        codeType = UNKNOWN;
        codeLen = Serial.parseInt();
        for (int i = 1; i <= codeLen; i++) {
          rawCodes[i - 1] = Serial.parseInt();
        }
        rawBits = Serial.parseInt();
        Serial.read();
        irsend.sendRaw(rawCodes, codeLen, rawBits);
        digitalWrite(STATUS_PIN, LOW);
      }
      IR_ID[1] = '\0';
      readCounter = 0;
    }
  }
}

void enableReceive(){
  RECORD = true;
  irrecv.enableIRIn(); // Re-enable receiver
  digitalWrite(RECORDSTATUS_PIN, HIGH);
  if(serialAvailable){
    Serial.print("R");
  }
}

void disableReceive(){
  RECORD = false;
  digitalWrite(RECORDSTATUS_PIN, LOW);
  if(serialAvailable){
    Serial.print("!R");
  }
}

