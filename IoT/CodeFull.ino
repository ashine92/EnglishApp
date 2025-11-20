#include <Wire.h>
#include <LiquidCrystal_PCF8574.h>   // Thư viện LCD ổn định cho ESP32
#include <ezButton.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include <time.h>

// -------------------------
// Firebase
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>

// -------------------------
// Pin & LCD
#define BUTTON_PIN 26
#define LCD_ADDR 0x27     // Địa chỉ LCD PCF8574
#define LCD_COLS 16
#define LCD_ROWS 2

LiquidCrystal_PCF8574 lcd(LCD_ADDR);
ezButton button(BUTTON_PIN);

// -------------------------
// WiFi & Firebase
#define WIFI_SSID "PTIT.HCM_SV"
#define WIFI_PASSWORD ""
#define FIREBASE_HOST "englishapp-416fb-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "jIRiR7G8OkCg9PW2vq5AkPGXWGkP4SuVFZoP4hK2"

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// -------------------------
// System state
bool systemOn = false;
unsigned long lastDisplayTime = 0;
const unsigned long DISPLAY_DURATION = 10000; // 10 giây

int currentPairIndex = 0;
int totalWords = 0;

// -------------------------
// Cấu hình múi giờ Việt Nam
const long gmtOffset_sec = 7 * 3600; // GMT+7
const int daylightOffset_sec = 0;

// -------------------------
// Setup
void setup() {
  Serial.begin(115200);

  pinMode(BUTTON_PIN, INPUT_PULLUP);
  button.setDebounceTime(50);

  // -----------------------------
  // I2C configuration
  Wire.begin(21, 22);
  Wire.setClock(50000);   // Giảm tốc độ tránh xung đột I2C/Firebase
  delay(200);

  // -----------------------------
  // LCD Init
  lcd.begin(LCD_COLS, LCD_ROWS);
  delay(200);             // Tránh lỗi LCD NACK
  lcd.setBacklight(255);  // Bật đèn PCF8574
  delay(200);

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Starting...");

  // -----------------------------
  // WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.println("Connecting to WiFi...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("\nWiFi Connected!");

  // -----------------------------
  // Firebase
  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  config.timeout.serverResponse = 10000;   // 10 giây
  config.timeout.socketConnection = 10000; // 10 giây

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  delay(300);

  // -----------------------------
  // Cấu hình NTP thời gian thực
  configTime(gmtOffset_sec, daylightOffset_sec, "pool.ntp.org", "time.nist.gov");

  Serial.println("Firebase ready");
  lcd.clear();
  lcd.print("Ready!");
}

// -------------------------
// Main loop
void loop() {
  button.loop();

  if (button.isPressed()) {
    systemOn = !systemOn;

    if (systemOn)
      turnSystemOn();
    else
      turnSystemOff();
  }

  if (systemOn) {
    unsigned long now = millis();

    if (now - lastDisplayTime >= DISPLAY_DURATION) {
      displayNextPair();
      lastDisplayTime = now;
    }
  }
}

// -------------------------
// Turn system ON
void turnSystemOn() {
  lcd.setBacklight(255);
  lcd.clear();
  lcd.print("System ON");
  Serial.println("System ON");

  delay(500);
  currentPairIndex = 0;
  lastDisplayTime = millis();

  displayNextPair();
}

// -------------------------
// Display next pair
void displayNextPair() {
  Serial.println("Reading Firebase...");

  DynamicJsonDocument doc(1024); // Giảm RAM

  if (Firebase.RTDB.getJSON(&fbdo, "/unlearnedWords")) {
    String jsonStr = fbdo.jsonString();

    // Lấy timestamp hiện tại (giờ Việt Nam)
    time_t nowTime;
    struct tm timeinfo;
    time(&nowTime);
    localtime_r(&nowTime, &timeinfo); // giờ đã là GMT+7

    char buf[32];
    strftime(buf, sizeof(buf), "%Y/%m/%d | %I:%M:%S%p | ", &timeinfo);

    Serial.print(buf);
    Serial.println(jsonStr);

    DeserializationError error = deserializeJson(doc, jsonStr);
    if (error) {
      lcd.clear();
      lcd.print("JSON error");
      Serial.println("JSON parse error!");
      delay(500);
      return;
    }

    JsonObject obj = doc.as<JsonObject>();
    totalWords = obj.size();

    if (totalWords == 0) {
      lcd.clear();
      lcd.print("No words!");
      delay(500);
      return;
    }

    int index1 = currentPairIndex * 2 + 1;
    int index2 = index1 + 1;

    String word1 = obj["word" + String(index1)] | "";
    String word2 = obj["word" + String(index2)] | "";

    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print(word1.substring(0, 16));
    lcd.setCursor(0, 1);
    lcd.print(word2.substring(0, 16));

    Serial.print(buf);
    Serial.print("Showing: ");
    Serial.print(word1);
    Serial.print(" | ");
    Serial.println(word2);

    // Cập nhật cặp tiếp theo
    int totalPairs = (totalWords + 1) / 2;
    currentPairIndex = (currentPairIndex + 1) % totalPairs;

    // Delay hợp lý để tránh SSL timeout
    delay(500);
  }
  else {
    lcd.clear();
    lcd.print("FB error");
    Serial.println("Firebase error:");
    Serial.println(fbdo.errorReason());
    delay(500);
  }
}

// -------------------------
// Turn system OFF
void turnSystemOff() {
  lcd.clear();
  lcd.setBacklight(0);
  Serial.println("System OFF");
}
