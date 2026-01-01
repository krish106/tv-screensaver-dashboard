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
