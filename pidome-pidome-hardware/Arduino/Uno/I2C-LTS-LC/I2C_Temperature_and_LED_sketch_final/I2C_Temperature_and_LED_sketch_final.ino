/*************** INCLUDES ***************/
#include <avr/eeprom.h>
#include <Wire.h>
#include <Adafruit_NeoPixel.h>


/*************** DEFINE CONSTANTS ***************/
#define  AREF_VOLTAGE       5.0
#define  I2C_ADDR           0x40
#define  REG_MAP_SIZE       50
#define  MAX_SENT_BYTES     1
#define  IDENTIFICATION     0x50

#define  LEDPIN             3
#define  TEMPPIN            A0
#define  LUXPIN             A1

/*************** GLOBAL VARIABLES ***************/
byte EEPROM_transitiondelay, EEPROM_numLEDS, EEPROM_USED;
byte EEPROM_LKCRed, EEPROM_LKCGreen, EEPROM_LKCBlue;

byte numLEDS = 60;

byte registerMap[REG_MAP_SIZE];
byte receivedCommands[MAX_SENT_BYTES];

byte validSETCommands[] = {0x16, 0x20, 0x24, 0x28, 0x32, 0x36, 0x41, 0x43, 0x45, 0x47, 0x49};   // The valid SET commands

byte registerMapTemp[5];
boolean newTmpDataAvailable = false;
float TEMP_collector = 0;
int TEMP_counter = 0;
byte LKTemp[4];

byte registerMapLux[4];
boolean newLuxDataAvailable = false;
float LUX_collector = 0;
int LUX_counter = 0;
byte LKLux[4];
float lightResistance;
float currentLightInLux;
float lightInputVoltage;
float hardResistance = 1.8;

byte endRed, endGreen, endBlue;        // Set the end color from the command received 
byte LKCRed, LKCGreen, LKCBlue;        // Last Known Color Setting
boolean setLKC = false;

byte rndArray[256];
byte oneLED = 0;

byte queueCommand = 0x00;
boolean newQueueData = false;
boolean runQueue = false;
byte queueRed = 0x00;
byte queueGreen = 0x00;
byte queueBlue = 0x00;
byte queueSteps = 0;
byte queueCurrentStep = 0;
byte transitiondelay = 15;

boolean stringComplete = false;
String inputString = "";
byte charcounter = 0;

byte counter = 0;

Adafruit_NeoPixel strip = Adafruit_NeoPixel(255, LEDPIN, NEO_GRB + NEO_KHZ800);

/*************** GENERAL FUNCTIONS ***************/
void setup() {
  analogReference(EXTERNAL);
  Serial.begin(115200);
  strip.begin();                                    // Initialize ledstrip
  strip.show();                                     // Set all pixels to 'off'
  readEEPROM();
  startWire();
  registerMap[REG_MAP_SIZE - 1] = IDENTIFICATION;   // Set ID register          
  Serial.println(F("Ready"));
}
void loop() {
  TEMPreadSignal();
  newTmpDataAvailable = false;
  storeTmpData();
  newTmpDataAvailable = true;
  setLKTemp();
  LUXreadSignal();
  newLuxDataAvailable = false;
  storeLuxData();
  newLuxDataAvailable = true;
  setLKLux();
  if (newQueueData == true) {
    newQueueData = false;
    queueCommand = receivedCommands[0];
    switch (receivedCommands[0]) {
      case 22:          // (0x16) (n) Set color immediatly for whole strip.
        queueSteps = 1;
        setLKC = true;
      break;
      case 32:          // (0x20) (c) Set color using fade for whole strip.
        queueSteps = 150;
        setLKC = true;
      break;
      case 36:          // (0x24) (l) Whipe strip from left to right (no fade)
        queueSteps = numLEDS;
        setLKC = true;
      break;
      case 40:          // (0x28) (r) Whipe strip from right to left (no fade)
        queueSteps = numLEDS;
        setLKC = true;
      break;
      case 50:          // (0x32) (x) Set strip to color with random leds (no fade)
        queueSteps = numLEDS;
        for (int x = 0; x < numLEDS; x++) {
          rndArray[x] = x;
        }
        setLKC = true;
      break;
      case 54:          // (0x36) (o) Fade one led to a color
        queueSteps = 150;
        setLKC = false;
      break;
      case 65:          // (0x41) (y) Turn entire LED-strip on to last known color
        queueSteps = 1;
        endRed = LKCRed;
        endGreen = LKCGreen;
        endBlue = LKCBlue;
        setLKC = false;
      break;
      case 67:          // (0x43) (z) Turn entire LED-strip off
        queueSteps = 1;
        endRed = 0;
        endGreen = 0;
        endBlue = 0;
        setLKC = false;
      break;
    }
    runQueue = true;
  }
  takeQueueStep();
  readSerial();
  serialEvent();
}
/**************** WIRE FUNCTIONS ***************/
void startWire() {
  Wire.begin(I2C_ADDR);
  Wire.onRequest(requestEvent);                     // Set interrupt - master requesting data from slave
  Wire.onReceive(receiveEvent);                     // Set interrupt - master sending data to slave
}
/*************** QUEUE FUNCTIONS ***************/
void takeQueueStep() {
  if (runQueue == true) {                    // Is the queue running?
    if (queueCurrentStep <= queueSteps) {
      switch(queueCommand) {
        case 22:      // 0x16 Direct
        case 65:      // 0x41 Turn on strip
        case 67:      // 0x43 Turn off strip
          setEntireStrip();
          queueRed = endRed;
          queueGreen = endGreen;
          queueBlue = endBlue;
        break;
        case 32:      // 0x20 Fade
          calculateColor();
          for (int x=0; x < numLEDS; x++) {
            strip.setPixelColor(x, strip.Color(queueRed, queueGreen, queueBlue));
          }
          strip.show();
        break;
        case 36:          // (0x24) (l) Whipe strip from left to right (no fade)
          strip.setPixelColor(queueCurrentStep, strip.Color(endRed, endGreen, endBlue));
          strip.show();
          queueRed = endRed;
          queueGreen = endGreen;
          queueBlue = endBlue;
        break;
        case 40:          // (0x28) (r) Whipe strip from right to left (no fade)
          strip.setPixelColor((queueSteps-queueCurrentStep), strip.Color(endRed, endGreen, endBlue));
          strip.show();
          queueRed = endRed;
          queueGreen = endGreen;
          queueBlue = endBlue;
        break;
        case 54:          // (0x36) (o) Fade one led to a color
          calculateColor();
          strip.setPixelColor(oneLED, strip.Color(queueRed, queueGreen, queueBlue));
          strip.show();
        break;
        case 50:          // (0x32) (x) Set strip to color with random leds (no fade)
          byte rndNumber;
          rndNumber = random(0, (queueSteps - queueCurrentStep));
          byte pixel;
          pixel = rndArray[rndNumber];
          byte cnt = 0;  // shift array and fill last value with a 0
          for (byte y = 0; y < numLEDS; y++) {
            if (y != rndNumber) {
              rndArray[cnt] = rndArray[y];
              cnt++;
            }
          }
          cnt++;
          rndArray[cnt] = 0;
          strip.setPixelColor(pixel, strip.Color(endRed, endGreen, endBlue));
          strip.show();
          queueRed = endRed;
          queueGreen = endGreen;
          queueBlue = endBlue;
        break;
      }
      startWire();
      if (setLKC == true) {
        LKCRed = queueRed;
        LKCGreen = queueGreen;
        LKCBlue = queueBlue;
      }
      queueCurrentStep++;
      delay(transitiondelay);
    } else {
      resetQueue();
    }
  }
}
void resetQueue() {
  queueCurrentStep = 0;
  queueSteps = 0;
  oneLED = 0;
  setLKC = false;
  runQueue = false; 
}
/******************* MEM FUNCTIONS *****************/
int freeRam () {
  extern int __heap_start, *__brkval; 
  int v; 
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval); 
}
/*************** RGB-STRIP FUNCTIONS ***************/
void setEntireStrip() {
  for (int x=0; x < numLEDS; x++) {
    strip.setPixelColor(x, strip.Color(endRed, endGreen, endBlue));
  }
  strip.show();
}
void calculateColor() {
  queueRed = queueRed + (endRed - queueRed) * queueCurrentStep / queueSteps;
  queueGreen = queueGreen + (endGreen - queueGreen) * queueCurrentStep / queueSteps;
  queueBlue = queueBlue + (endBlue - queueBlue) * queueCurrentStep / queueSteps;
}
/*************** I2C-HANDLER FUNCTIONS ***************/
void requestEvent() {  // The master requests data, so lets send it
  switch(receivedCommands[0]) {
    case 1:  // (0x01) Store temperature in memory spots (4 bytes = float)
      if(newTmpDataAvailable == true) {
        newTmpDataAvailable = false;
        Wire.write(registerMapTemp, 4);
      } else {
        Wire.write(LKTemp, 4);
      }
    break;
    case 6:  // (0x06)  Store the current Lux value in memory spots (4 bytes = float)
      if (newLuxDataAvailable == true) {
        newLuxDataAvailable = false;
        Wire.write(registerMapLux, 4);
      } else {
        Wire.write(LKLux, 4);
      }
    break;
    case 17:  // (0x11) Store current RGB color in memory spots (4 bytes = 4x int)
      registerMap[0] = LKCRed;
      registerMap[1] = LKCGreen;
      registerMap[2] = LKCBlue;
      registerMap[3] = transitiondelay;
      Wire.write(registerMap, 4);
    break;
  }
}
void receiveEvent(int bytesReceived) {  //The master sends data, lets handle it
  for (byte a = 0; a < bytesReceived; a++) {    // Read the bytes
    receivedCommands[a] = Wire.read();
  }
  switch(receivedCommands[0]) {
    case 22:                  // 0x16
    case 32:                  // 0x20
    case 36:                  // 0x24
    case 40:                  // 0x28
    case 50:                  // 0x32
      resetQueue();
      endRed = receivedCommands[1];
      endGreen = receivedCommands[2];
      endBlue = receivedCommands[3];
      newQueueData = true;
    break;
    case 54:                  // 0x36
      resetQueue();
      endRed = receivedCommands[1];
      endGreen = receivedCommands[2];
      endBlue = receivedCommands[3];
      oneLED = receivedCommands[4];
      newQueueData = true;
    break;
    case 65:                  // 0x41
    case 67:                  // 0x43
      resetQueue();
      newQueueData = true;
    break;
    case 69:                  // 0x45
      transitiondelay = receivedCommands[1];
    break;
    case 71:                  // 0x47
      numLEDS = receivedCommands[1];
    break;
    case 73:                  // 0x49
      writeEEPROM();
    break;
    default:                  // All others
      newQueueData = false;
    break;
  }
}
boolean isValidSetCommand(byte cmd) {
  for (int x=0; x<(sizeof(validSETCommands))-1; x++) {
    if (cmd == validSETCommands[x]) {
      return true;
    }
  }
  return false;
}
/***************** EEPROM FUNCTIONS ******************/
void readEEPROM() {
  EEPROM_transitiondelay = eeprom_read_byte((unsigned char *) 0);
  EEPROM_numLEDS = eeprom_read_byte((unsigned char *) 1);
  EEPROM_LKCRed = eeprom_read_byte((unsigned char *) 2);
  EEPROM_LKCGreen = eeprom_read_byte((unsigned char *) 3);
  EEPROM_LKCBlue = eeprom_read_byte((unsigned char *) 4);
  EEPROM_USED = eeprom_read_byte((unsigned char *) 5);
  if (EEPROM_USED == 0xFF) {
    EEPROM_transitiondelay = 15;
    EEPROM_numLEDS = 60;
    EEPROM_LKCRed = 0;
    EEPROM_LKCGreen = 0;
    EEPROM_LKCBlue = 0;
    Serial.println(F("Using defaults"));
  } else {
    transitiondelay = EEPROM_transitiondelay;
    numLEDS = EEPROM_numLEDS;
    endRed = EEPROM_LKCRed;
    endGreen = EEPROM_LKCGreen;
    endBlue = EEPROM_LKCBlue;
    LKCRed = endRed;
    LKCGreen = endGreen;
    LKCBlue = endBlue;
    queueCommand = 0x20;
    queueSteps = 150;
    runQueue = true;
    Serial.println(F("Using stored values"));
  }
}
void writeEEPROM() {
  eeprom_write_byte((unsigned char *) 0, transitiondelay);
  eeprom_write_byte((unsigned char *) 1, numLEDS);
  eeprom_write_byte((unsigned char *) 2, LKCRed);
  eeprom_write_byte((unsigned char *) 3, LKCGreen);
  eeprom_write_byte((unsigned char *) 4, LKCBlue);
  eeprom_write_byte((unsigned char *) 5, 0);
  Serial.println(F("Data written to EEPROM"));
}
/*************** TEMPERATURE FUNCTIONS ***************/
void TEMPreadSignal() {
    int TEMP_signal = analogRead(TEMPPIN);
    TEMP_collector += ((((TEMP_signal * AREF_VOLTAGE) / 1024) - 0.5) * 100);
    TEMP_counter++;
    if (TEMP_counter >= 1000) {
      TEMP_collector = TEMP_collector / TEMP_counter;
      TEMP_counter = 1;
    }
}
void storeTmpData() {
  byte * byteTmpPointer;                              // We declare a pointer as type byte
  byte arrayTmpIndex = 0;                             // We need to keep track of where we are storing data in the array
  float TEMP = TEMP_collector / TEMP_counter;
  byteTmpPointer = (byte*)&TEMP;                      // Temperature is 4 bytes
  for (int i = 0; i < 4; i++) {
    registerMapTemp[arrayTmpIndex] = byteTmpPointer[i];  // Increment pointer to store each byte but LSB first (LITTLE_ENDIAN)
    arrayTmpIndex++;
  }
}
void setLKTemp() {
  for (int i =0; i < 4;i++) {
    LKTemp[i] = registerMapTemp[i];
  } 
}
/*************** LUX FUNCTIONS ***************/
void LUXreadSignal() {
    float LUX_signal = getLux(analogRead(LUXPIN));
    LUX_collector += LUX_signal;
    LUX_counter++;
    if (LUX_counter >= 1000) {
      LUX_collector = LUX_collector / LUX_counter;
      LUX_counter = 1;
    }
}
float getLux(int reading) {
  float lightADCReading = reading;
  lightInputVoltage = 5.0 * ((float)lightADCReading / 1024.0);   // Calculating the voltage of the ADC for light
  lightResistance = (hardResistance * 5.0) / lightInputVoltage - hardResistance;     // Calculating the resistance of the photoresistor in the voltage divider
  currentLightInLux = 255.84 * pow(lightResistance, -10/9);    // Calculating the intensity of light in lux 
  return currentLightInLux;
}
void storeLuxData() {
  byte * byteLuxPointer;                              // We declare a pointer as type byte
  byte arrayLuxIndex = 0;                             // We need to keep track of where we are storing data in the array
  float LUX = LUX_collector / LUX_counter;
  byteLuxPointer = (byte*)&LUX;                       // Lux is 4 bytes
  for (int i = 0; i < 4; i++) {
    registerMapLux[arrayLuxIndex] = byteLuxPointer[i];  // Increment pointer to store each byte but LSB first (LITTLE_ENDIAN)
    arrayLuxIndex++;
  }
}
void setLKLux() {
  for (int i =0; i < 4;i++) {
    LKLux[i] = registerMapLux[i];
  } 
}
/*************** SERIAL FUNCTIONS ***************/
void readSerial() {
  if(stringComplete==true) {
    if(inputString == "T") {  // Get the current temperature
      Serial.println(TEMP_collector / TEMP_counter);
    } else if (inputString == "L") {  // Get the current Lux value
      Serial.println(LUX_collector / LUX_counter);
    } else if (inputString == "C") {  // Get the current RGB value
      Serial.print(LKCRed);Serial.print(F(":"));Serial.print(LKCGreen);Serial.print(F(":"));Serial.print(LKCBlue);Serial.print(F(":"));Serial.println(transitiondelay);
    }
    charcounter=0;
    inputString="";
    stringComplete = false;
  }  
}
void serialEvent() {
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    if (inChar == '\n') {
      stringComplete = true;
    } else {
      inputString += inChar;
    }
  }
}
