#include <SoftwareSerial.h>
#include <ESP8266WiFi.h>
#include <ThingSpeak.h>

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
    //rdata = nodemcu.read();
    //Serial.print("Caracter: ");Serial.println(rdata);
    //if(rdata == '\n') {
        //Read value from arduino and post it on server
        int var = nodemcu.parseInt();
        ThingSpeak.writeField(writeChannelNumber, 1, var, writeAPIKey);

        //Read command from server and send it to arduino
        long command = ThingSpeak.readLongField(channelNumber, 1, readAPIKey);
        int statusCode = ThingSpeak.getLastReadStatus();
        if(statusCode == 200) {
          Serial.print("Command: ");
          Serial.println(command);
        } else {
          Serial.print(statusCode);
          Serial.println(" - Unable to read channel / No internet connection");
        }
        nodemcu.print(command);
    //}
  }
}
