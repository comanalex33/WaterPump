#include <SoftwareSerial.h>
#include <ArduinoJson.h>

int pumpPin = 7;            // Porneste pe LOW
int intensityPin = 8;       // Daca e lumina atunci e pe LOW
int moistureSensorPin = A0; // Uscat-1023 , Cu cat se descreste mai mult e mai multa apa

int value = 1;
SoftwareSerial nodemcu(2,3);

String data;

float value2 = 12.5;

void setup() {
  Serial.begin(9600);
  nodemcu.begin(9600);
  pinMode(pumpPin, OUTPUT);
}

void loop() {
  if(nodemcu.available() == 0) {
    int intensityData = digitalRead(intensityPin);
    int moistureData = analogRead(moistureSensorPin);
    StaticJsonDocument<1000> data;
    data["intensity"] = intensityData;
    data["moisture"] = moistureData;
    serializeJson(data, nodemcu);
    data.clear();
    delay(1000);
  }

  if(nodemcu.available() > 0) {
    int var = nodemcu.parseInt();
    delay(100);
    Serial.print("Citit: ");
    Serial.println(var);

    if(var > 0) {
      digitalWrite(pumpPin, HIGH);
    } else {
      digitalWrite(pumpPin, LOW);
      int startTime = millis();
      int endTime = startTime;
      while((endTime - startTime) < 2000) {
        endTime = millis();
      }
      digitalWrite(pumpPin, HIGH);
    }
  }
}
