#include <SoftwareSerial.h>
#include <ESP8266WiFi.h>
#include <ThingSpeak.h>
#include <ArduinoJson.h>

// Date conectare Wifi
char ssid[] = "DIGI-kV92";
char pass[] = "pmN8N3r5";

// Date canale ThingSpeak
long channelNumber = 1708402;
long writeChannelNumber = 1706158;
const char readAPIKey[] = "LRAYJR0X3AZS7KVC";
const char writeAPIKey[] = "7UZAJ4MMTG6GPPJ6";

WiFiClient client;   // Pornim un client de Wifi pentru a putea comunica cu severul

SoftwareSerial nodemcu(D5, D6); // Definim comunicatia seriala cu Arduino definid pinii specifici

String lastCommand = "";  // Variabila folosita pentru a salva data ultimei noi comenzi venite de pe server

void setup() {
  nodemcu.begin(9600);
  Serial.begin(9600);

  WiFi.begin(ssid, pass);                 // Definim conexiunea wifi pentru modul
  while(WiFi.status() != WL_CONNECTED){   // Asteptam cat timp cu este conectat
    Serial.print(".");
    delay(500);
  }
  Serial.print("Connected to");
  Serial.println(ssid);
  ThingSpeak.begin(client);               // Cand este conectat la internet precizam faptul ca vom folosi clientul pentru a comunica cu ThingSpeak
}

void loop() {
  if(nodemcu.available() > 0) {           // Daca avem ceva de primit pe comunicatia seriala
     String str = nodemcu.readString();   // Luam datele legate de senzori 

     StaticJsonDocument<1000> data;
     auto error = deserializeJson(data, str);     // Decodificam string-ul primit
     if(error) {
       Serial.print(F("deserializeJson() failed with code "));
       Serial.println(error.c_str());
       return;
     }
     int intensity = data["intensity"];   // Salvam valoarea intensitatii intr-o variabila
     int moisture = data["moisture"];     // Salvam valoarea umiditatii intr-o variabila

     // Afisam datele in consola
     Serial.print("Intensity: ");
     Serial.println(intensity);
     Serial.print("Moisture: ");
     Serial.println(moisture);

     // Trimitem datele pe server, field1 e folosit pentru intensitate, field2 pentru umiditate
     ThingSpeak.setField(1, intensity);
     ThingSpeak.setField(2, moisture); 
     ThingSpeak.writeFields(writeChannelNumber, writeAPIKey);

     long command = ThingSpeak.readLongField(channelNumber, 1, readAPIKey);   // Citeste comanda de pe server
     String createdAt = ThingSpeak.readCreatedAt(channelNumber, readAPIKey);  // Citeste data la care a fost adaugata aceasta comanda
     int statusCode = ThingSpeak.getLastReadStatus();                         // Verificam statusul request-ului
     if(statusCode == 200) {
       Serial.print("Command: ");
       Serial.println(command);
     } else {
       Serial.print(statusCode);
       Serial.println(" - Unable to read channel / No internet connection");
     }

     if(createdAt != lastCommand) {     // Daca comanda citita este una noua, atunci o trimitem catre Arduino
       lastCommand = createdAt;
       nodemcu.print(command);
     }
  }
}
