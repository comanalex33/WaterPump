#include <SoftwareSerial.h>

int ledPin = 7;
int intensityPin = 8;
int value = 1;
SoftwareSerial nodemcu(2,3);

String data;

void setup() {
  Serial.begin(9600);
  nodemcu.begin(9600);
  pinMode(ledPin, OUTPUT);
}

void loop() {
  if(nodemcu.available() == 0) {
    //if(value == 1) {
    //  data = "da";
    //  value = 0;
    //} else {
    //  data = "nu";
    //  value = 1;
    //}
    //Serial.println(data);
    int var = digitalRead(intensityPin);
    nodemcu.print(var);
    delay(1000);
  }

  if(nodemcu.available() > 0) {
    int var = nodemcu.parseInt();
    delay(100);
    Serial.print("Citit: ");
    Serial.println(var);

    if(var > 0) {
      digitalWrite(ledPin, HIGH);
    } else {
      digitalWrite(ledPin, LOW);
    }
  }
}
