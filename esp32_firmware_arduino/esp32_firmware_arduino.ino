#include "wifi_secrets.h"
#include <Arduino.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <WebServer.h>
#include <WiFi.h>

// DHT Sensor setup
#define DHTPIN 4      // Digital pin connected to the DHT sensor
#define DHTTYPE DHT22 // DHT 22 (AM2302)
DHT dht(DHTPIN, DHTTYPE);

// Web Server setup
WebServer server(80);

void handleRoot() {
  float h = dht.readHumidity();
  float t = dht.readTemperature();

  String html = "<html><body>";
  html += "<h1>ESP32 Weather Station</h1>";

  if (isnan(h) || isnan(t)) {
    html += "<p>Failed to read from DHT sensor!</p>";
  } else {
    html += "<p>Temperature: " + String(t) + " &deg;C</p>";
    html += "<p>Humidity: " + String(h) + " %</p>";
  }

  html += "<p><a href='/data'>View JSON Data</a></p>";
  html += "</body></html>";

  server.send(200, "text/html", html);
}

void handleData() {
  float h = dht.readHumidity();
  float t = dht.readTemperature();

  StaticJsonDocument<200> doc;

  // Check if any reads failed and exit early (to try again).
  if (isnan(h) || isnan(t)) {
    doc["error"] = "Failed to read from DHT sensor!";
    doc["temperature"] = 0.0;
    doc["humidity"] = 0.0;
  } else {
    doc["temperature"] = t;
    doc["humidity"] = h;
  }

  String jsonString;
  serializeJson(doc, jsonString);

  server.send(200, "application/json", jsonString);
}

void setup() {
  Serial.begin(115200);

  // Initialize DHT
  dht.begin();

  // Connect to Wi-Fi
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  // Setup Server
  server.on("/", handleRoot);
  server.on("/data", handleData);

  server.begin();
  Serial.println("HTTP server started");
}

void loop() { server.handleClient(); }
