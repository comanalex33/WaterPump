#include <SoftwareSerial.h>
#include <ESP8266WiFi.h>
#include <ThingSpeak.h>
#include <ArduinoJson.h>

char ssid[] = "DIGI-kV92";
char pass[] = "pmN8N3r5";

long channelNumber = 1708402;
long writeChannelNumber = 1706158;
const char readAPIKey[] = "LRAYJR0X3AZS7KVC";
const char writeAPIKey[] = "7UZAJ4MMTG6GPPJ6";

WiFiClient client;

SoftwareSerial nodemcu(D5, D6);

char rdata;
String sdata;
String lastCommand = "";

void setup() {
  nodemcu.begin(9600);
  Serial.begin(9600);

  WiFi.begin(ssid, pass);
  while(WiFi.status() != WL_CONNECTED){
    Serial.print(".");
    delay(500);
  }
  Serial.print("Connected to");
  Serial.println(ssid);
  ThingSpeak.begin(client);
}

void loop() {
  if(nodemcu.available() > 0) {
     String str = nodemcu.readString();

     StaticJsonDocument<1000> data;
     auto error = deserializeJson(data, str);
     if(error) {
       Serial.print(F("deserializeJson() failed with code "));
       Serial.println(error.c_str());
       return;
     }
     int intensity = data["intensity"];
     int moisture = data["moisture"];
     Serial.print("Intensity: ");
     Serial.println(intensity);
     Serial.print("Moisture: ");
     Serial.println(moisture);

     ThingSpeak.setField(1, intensity);
     ThingSpeak.setField(2, moisture); 
     ThingSpeak.writeFields(writeChannelNumber, writeAPIKey);

     //Read command from server and send it to arduino
     long command = ThingSpeak.readLongField(channelNumber, 1, readAPIKey);
     String createdAt = ThingSpeak.readCreatedAt(channelNumber, readAPIKey);
     int statusCode = ThingSpeak.getLastReadStatus();
     if(statusCode == 200) {
       Serial.print("Command: ");
       Serial.println(command);
     } else {
       Serial.print(statusCode);
       Serial.println(" - Unable to read channel / No internet connection");
     }

     if(createdAt != lastCommand) {
       lastCommand = createdAt;
       nodemcu.print(command);
     }
  }
}
