/*************** INCLUDES ***************/
#include <Wire.h>

/*************** DEFINE CONSTANTS ***************/
#define  AREF_VOLTAGE       5.0
#define  I2C_ADDR           0x32
#define  REG_MAP_SIZE       4
#define  MAX_SENT_BYTES     1
#define  IDENTIFICATION     0x04

/*************** GLOBAL VARIABLES ***************/
byte registerMap[REG_MAP_SIZE];
byte receivedCommands[MAX_SENT_BYTES];

byte registerMapSensor1[4];
byte registerMapSensor2[4];
byte registerMapSensor3[4];

int sensorPin1 = A0;
int sensorPin2 = A1;
int sensorPin3 = A2;

int thresholdSensor1 = 820;                      // Change these values to finetune your setup
int thresholdSensor2 = 70;
int thresholdSensor3 = 100;

int pulseCounterSensor1 = 0;
int pulseCounterSensor2 = 0;
int pulseCounterSensor3 = 0;

boolean validSensor1 = false;
boolean validSensor2 = false;
boolean validSensor3 = false;

boolean gotPulse1 = false;
boolean gotPulse2 = false;
boolean gotPulse3 = false;

int sensorLED1 = 10;
int sensorLED2 = 11;
int sensorLED3 = 12;

int ledDelay = 50;

/*************** GENERAL FUNCTIONS ***************/
void setup() {
  Serial.begin(115200);
  pinMode(sensorLED1, OUTPUT);
  pinMode(sensorLED2, OUTPUT);
  pinMode(sensorLED3, OUTPUT);
 
  ledTest();
  startWire();
  registerMap[REG_MAP_SIZE - 1] = IDENTIFICATION;   // Set ID register          
  Serial.println(F("Ready"));
}
void loop() {
  readSensor1();
  readSensor2();
  readSensor3();
}
/**************** WIRE FUNCTIONS ***************/
void startWire() {
  Wire.begin(I2C_ADDR);
  Wire.onRequest(requestEvent);                 // Set interrupt - master requesting data from slave
  Wire.onReceive(receiveEvent);                 // Set interrupt - master sending data to slave
}
/******************* MEM FUNCTIONS *****************/
int freeRam () {
  extern int __heap_start, *__brkval; 
  int v; 
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval); 
}
/*************** I2C-HANDLER FUNCTIONS ***************/
void requestEvent() {                          // The master requests data, so lets send it
  switch(receivedCommands[0]) {
    case 1:                                    // (0x01) Store sensor 1 pulses in memory spots (4 bytes = float)
      registerMapSensor1[0] = pulseCounterSensor1;
      Wire.write(registerMapSensor1, 4);
      pulseCounterSensor1 = 0;
    break;
    case 2:                                    // (0x02) Store sensor 2 pulses in memory spots (4 bytes = float)
      registerMapSensor2[0] = pulseCounterSensor2;
      Wire.write(registerMapSensor2, 4);
      pulseCounterSensor2 = 0;
    break;
    case 3:                                     // (0x03) Store sensor 3 pulses in memory spots (4 bytes = float)
      registerMapSensor3[0] = pulseCounterSensor3;
      Wire.write(registerMapSensor3, 4);
      pulseCounterSensor3 = 0;
    break;
  }
}

void receiveEvent(int bytesReceived) {          //The master sends data, lets handle it
  for (byte a = 0; a < bytesReceived; a++) {    // Read the bytes
    receivedCommands[a] = Wire.read();
  }
}

/*************** FUNCTIONS ***************/
void readSensor1() {                            // Electricity
  int s1 = analogRead(sensorPin1);
  if (!validSensor1 && s1<1000){validSensor1=true;}
  if (s1 >= (thresholdSensor1)) {gotPulse1=true;digitalWrite(sensorLED1, HIGH);}
  if (s1 < thresholdSensor1 && validSensor1 && pulseCounterSensor1 < 9999 && gotPulse1==true){
    gotPulse1=false;
    digitalWrite(sensorLED1, LOW);
    pulseCounterSensor1++;
  }
}
void readSensor2() {                            // Gas
  int s2 = analogRead(sensorPin2);
  if (!validSensor2 && s2<1000){validSensor2=true;}
  if (s2 >= (thresholdSensor2)) {gotPulse2=true;digitalWrite(sensorLED2, HIGH);}
  if (s2 < thresholdSensor2 && validSensor2 && pulseCounterSensor2 < 9999 && gotPulse2==true){
    gotPulse2=false;
    digitalWrite(sensorLED2, LOW);
    pulseCounterSensor2++;
  }
}
void readSensor3() {                            // Water
  int s3 = analogRead(sensorPin3);
  if (!validSensor3 && s3<1000){validSensor3=true;}
  if (s3 <= (thresholdSensor3)) {gotPulse3=true;digitalWrite(sensorLED3, HIGH);}
  if (s3 > thresholdSensor3 && validSensor3 && pulseCounterSensor3 < 9999 && gotPulse3==true){
    gotPulse3=false;
    digitalWrite(sensorLED3, LOW);
    pulseCounterSensor3++;
  }  
}
void ledTest() {
  digitalWrite(sensorLED1, HIGH);
  delay(ledDelay);
  digitalWrite(sensorLED2, HIGH);
  delay(ledDelay);
  digitalWrite(sensorLED3, HIGH);
  delay(ledDelay);
  digitalWrite(sensorLED1, LOW);
  delay(ledDelay);
  digitalWrite(sensorLED2, LOW);
  delay(ledDelay);
  digitalWrite(sensorLED3, LOW);
  delay(ledDelay);
  digitalWrite(sensorLED1, HIGH);
  delay(ledDelay);
  digitalWrite(sensorLED2, HIGH);
  delay(ledDelay);
  digitalWrite(sensorLED3, HIGH);
  delay(ledDelay); 
  digitalWrite(sensorLED1, LOW);
  delay(ledDelay);
  digitalWrite(sensorLED2, LOW);
  delay(ledDelay);
  digitalWrite(sensorLED3, LOW);
  delay(ledDelay); 
}

