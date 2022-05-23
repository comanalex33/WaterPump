#include <SoftwareSerial.h>
#include <ArduinoJson.h>

int pumpPin = 7;            // Porneste pe LOW
int intensityPin = 8;       // Daca e lumina atunci e pe LOW
int moistureSensorPin = A0; // Uscat-1023 , Cu cat se descreste mai mult e mai multa apa

int value = 1;
SoftwareSerial nodemcu(2,3);

String data;

int background_command = 0;

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

    // Daca se primeste comanda 1, atunci pornim pompa timp de 2 secunde
    if(var == 1) {
      background_command = 0;
      digitalWrite(pumpPin, LOW);
      int startTime = millis();
      int endTime = startTime;
      while((endTime - startTime) < 2000) {
        endTime = millis();
      }
      digitalWrite(pumpPin, HIGH);
    }

    // Daca se primeste comanda 2, atunci pornim pompa
    if(var == 2) {
      background_command = 0;
      digitalWrite(pumpPin, LOW);
    }

    // Daca se primeste comanda 3, atunci oprim pompa
    if(var == 3) {
      background_command = 0;
      digitalWrite(pumpPin, HIGH);
    }

    // Daca se primeste comanda 4, atunci hardware-ul ia decizii singur
    if(var == 4) {
      background_command = 4;
    }
  }

  // Daca comanda este 4, atunci pornim pompa daca nu este lumina si umiditatea este scazuta
  if(background_command == 4) {
    int intensityData = digitalRead(intensityPin);
    int moistureData = analogRead(moistureSensorPin);
    Serial.print("Umiditate: ");
    Serial.println(moistureData);
    if(intensityData == HIGH && moistureData > 700) {
      digitalWrite(pumpPin, LOW);
    } else {
      digitalWrite(pumpPin, HIGH);
    }
    delay(500);
  }
}
