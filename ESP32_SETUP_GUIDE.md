# 🔌 Hướng Dẫn Bước 5: Thiết Lập ESP32 với LCD 16x2

## 📦 Yêu Cầu Phần Cứng

### 1. ESP32 Development Board
- ESP32-WROOM-32 hoặc tương đương
- Hỗ trợ WiFi

### 2. LCD 16x2 với I2C Module
- LCD 16x2 (16 cột x 2 hàng)
- Module I2C (PCF8574 hoặc tương đương)
- Địa chỉ I2C mặc định: `0x27` hoặc `0x3F`

### 3. Dây Nối
- 4 dây jumper (Female-Female hoặc Male-Female)

### 4. Nguồn
- USB cable để cấp nguồn cho ESP32

---

## 🔧 Kết Nối Phần Cứng

### Sơ Đồ Kết Nối

```
ESP32          LCD I2C Module
-----          --------------
GND    ----    GND
3.3V   ----    VCC
GPIO21 ----    SDA
GPIO22 ----    SCL
```

### Chi Tiết Chân

| ESP32 Pin | LCD I2C Pin | Mô Tả |
|-----------|-------------|-------|
| GND       | GND         | Ground (đất) |
| 3.3V      | VCC         | Nguồn 3.3V (hoặc 5V nếu LCD yêu cầu) |
| GPIO21    | SDA         | I2C Data Line |
| GPIO22    | SCL         | I2C Clock Line |

**Lưu ý**: 
- Một số LCD I2C cần 5V, kiểm tra datasheet
- Nếu dùng 5V, kết nối VCC với pin 5V của ESP32
- SDA/SCL có thể thay đổi trong code nếu cần

---

## 💻 Cài Đặt Arduino IDE

### 1. Tải Arduino IDE
- Tải từ: https://www.arduino.cc/en/software
- Phiên bản khuyến nghị: 2.x trở lên

### 2. Thêm ESP32 Board
1. Mở Arduino IDE
2. File > Preferences
3. Thêm URL vào "Additional Boards Manager URLs":
```
https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
```
4. Tools > Board > Boards Manager
5. Tìm "esp32" và cài đặt "esp32 by Espressif Systems"

### 3. Chọn Board
- Tools > Board > ESP32 Arduino > ESP32 Dev Module

### 4. Chọn Port
- Tools > Port > [Chọn COM port của ESP32]
- Trên Linux: thường là `/dev/ttyUSB0` hoặc `/dev/ttyACM0`
- Trên Windows: `COM3`, `COM4`, etc.

---

## 📚 Cài Đặt Thư Viện

### 1. Firebase ESP32
1. Sketch > Include Library > Manage Libraries
2. Tìm "Firebase ESP32 Client"
3. Cài đặt phiên bản mới nhất (by Mobizt)

### 2. LiquidCrystal I2C
1. Sketch > Include Library > Manage Libraries
2. Tìm "LiquidCrystal I2C"
3. Cài đặt phiên bản by Frank de Brabander

### 3. ArduinoJson (Optional, nếu cần parse phức tạp)
1. Sketch > Include Library > Manage Libraries
2. Tìm "ArduinoJson"
3. Cài đặt phiên bản 6.x

---

## 📝 Code ESP32

### File: `ESP32_Firebase_LCD.ino`

```cpp
#include <WiFi.h>
#include <FirebaseESP32.h>
#include <LiquidCrystal_I2C.h>

// ========== CẤU HÌNH WIFI ==========
#define WIFI_SSID "TEN_WIFI_CUA_BAN"        // Thay bằng tên WiFi
#define WIFI_PASSWORD "MAT_KHAU_WIFI"       // Thay bằng mật khẩu WiFi

// ========== CẤU HÌNH FIREBASE ==========
#define FIREBASE_HOST "your-project.firebaseio.com"  // Thay bằng Firebase URL (không có https://)
#define FIREBASE_AUTH "YOUR_DATABASE_SECRET"         // Thay bằng Database Secret (hoặc để trống nếu dùng test mode)

// ========== CẤU HÌNH LCD ==========
#define LCD_ADDRESS 0x27    // Địa chỉ I2C (thử 0x3F nếu không hoạt động)
#define LCD_COLS 16         // Số cột
#define LCD_ROWS 2          // Số hàng

// ========== KHỞI TẠO ==========
FirebaseData firebaseData;
FirebaseConfig config;
FirebaseAuth auth;

LiquidCrystal_I2C lcd(LCD_ADDRESS, LCD_COLS, LCD_ROWS);

// ========== BIẾN TOÀN CỤC ==========
int currentIndex = 0;           // Chỉ số từ hiện tại
int totalWords = 0;             // Tổng số từ
unsigned long lastUpdateTime = 0;
const unsigned long UPDATE_INTERVAL = 5000;  // 5 giây mỗi từ

// ========== SETUP ==========
void setup() {
  Serial.begin(115200);
  Serial.println("\n=== ESP32 Firebase LCD ===");

  // Khởi tạo LCD
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Connecting WiFi");
  
  // Kết nối WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  
  int wifiAttempts = 0;
  while (WiFi.status() != WL_CONNECTED && wifiAttempts < 20) {
    delay(500);
    Serial.print(".");
    wifiAttempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi Connected!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
    
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("WiFi Connected!");
    delay(2000);
  } else {
    Serial.println("\nWiFi Failed!");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("WiFi Failed!");
    while(1) delay(1000);  // Dừng nếu không kết nối được
  }

  // Cấu hình Firebase
  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
  
  Serial.println("Firebase Initialized");
  
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Firebase Ready!");
  delay(2000);
  
  // Lấy tổng số từ
  updateTotalWords();
  
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Loading words...");
  delay(1000);
}

// ========== LOOP ==========
void loop() {
  // Kiểm tra WiFi
  if (WiFi.status() != WL_CONNECTED) {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("WiFi Lost!");
    Serial.println("WiFi disconnected, reconnecting...");
    WiFi.reconnect();
    delay(5000);
    return;
  }

  // Cập nhật từ vựng theo chu kỳ
  if (millis() - lastUpdateTime >= UPDATE_INTERVAL) {
    displayNextWord();
    lastUpdateTime = millis();
  }
}

// ========== HÀM CẬP NHẬT TỔNG SỐ TỪ ==========
void updateTotalWords() {
  if (Firebase.getJSON(firebaseData, "/unlearnedWords")) {
    FirebaseJson &json = firebaseData.jsonObject();
    size_t count = 0;
    json.iteratorBegin();
    
    while (json.iteratorGet(count, 0, "") != "") {
      count++;
    }
    json.iteratorEnd();
    
    totalWords = count;
    Serial.print("Total words: ");
    Serial.println(totalWords);
  } else {
    Serial.println("Failed to get total words");
    Serial.println(firebaseData.errorReason());
    totalWords = 0;
  }
}

// ========== HÀM HIỂN THỊ TỪ TIẾP THEO ==========
void displayNextWord() {
  if (totalWords == 0) {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("No words found!");
    lcd.setCursor(0, 1);
    lcd.print("Sync from app");
    
    // Thử cập nhật lại
    updateTotalWords();
    return;
  }

  // Lấy từ vựng từ Firebase
  String path = "/unlearnedWords/" + String(currentIndex);
  
  if (Firebase.getJSON(firebaseData, path)) {
    FirebaseJson &json = firebaseData.jsonObject();
    FirebaseJsonData wordData;
    FirebaseJsonData meaningData;
    
    // Lấy word
    String word = "";
    if (json.get(wordData, "word")) {
      word = wordData.stringValue;
    }
    
    // Lấy meaning
    String meaning = "";
    if (json.get(meaningData, "meaning")) {
      meaning = meaningData.stringValue;
    }
    
    // Hiển thị lên LCD
    lcd.clear();
    
    // Dòng 1: Word (tối đa 16 ký tự)
    lcd.setCursor(0, 0);
    if (word.length() > 16) {
      lcd.print(word.substring(0, 16));
    } else {
      lcd.print(word);
    }
    
    // Dòng 2: Meaning (tối đa 16 ký tự)
    lcd.setCursor(0, 1);
    if (meaning.length() > 16) {
      lcd.print(meaning.substring(0, 16));
    } else {
      lcd.print(meaning);
    }
    
    // Debug
    Serial.print("Displaying [");
    Serial.print(currentIndex + 1);
    Serial.print("/");
    Serial.print(totalWords);
    Serial.print("]: ");
    Serial.print(word);
    Serial.print(" - ");
    Serial.println(meaning);
    
    // Chuyển sang từ tiếp theo
    currentIndex++;
    if (currentIndex >= totalWords) {
      currentIndex = 0;  // Quay lại từ đầu
      updateTotalWords(); // Cập nhật lại tổng số từ
    }
    
  } else {
    Serial.println("Failed to get word");
    Serial.println(firebaseData.errorReason());
    
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Error loading");
    lcd.setCursor(0, 1);
    lcd.print("word #");
    lcd.print(currentIndex);
    
    // Thử từ tiếp theo
    currentIndex++;
    if (currentIndex >= totalWords) {
      currentIndex = 0;
      updateTotalWords();
    }
  }
}

// ========== HÀM HIỂN THỊ TEXT DÀI (SCROLL) ==========
// Nếu muốn scroll text dài, dùng hàm này
void scrollText(String text, int row, int delayTime) {
  if (text.length() <= LCD_COLS) {
    lcd.setCursor(0, row);
    lcd.print(text);
    return;
  }
  
  // Scroll text
  for (int i = 0; i <= text.length() - LCD_COLS; i++) {
    lcd.setCursor(0, row);
    lcd.print(text.substring(i, i + LCD_COLS));
    delay(delayTime);
  }
}
```

---

## 🔑 Lấy Firebase Database Secret

### Cách 1: Test Mode (Đơn Giản)
Nếu bạn đã đặt Firebase Rules thành test mode:
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

Thì có thể để trống `FIREBASE_AUTH`:
```cpp
#define FIREBASE_AUTH ""
```

### Cách 2: Dùng Database Secret (Bảo Mật Hơn)
1. Vào Firebase Console
2. Project Settings (⚙️) > Service accounts
3. Database secrets > Show
4. Copy secret và paste vào code

---

## 📤 Upload Code Lên ESP32

### 1. Cấu Hình Code
Thay đổi các giá trị sau trong code:
```cpp
#define WIFI_SSID "TEN_WIFI_CUA_BAN"
#define WIFI_PASSWORD "MAT_KHAU_WIFI"
#define FIREBASE_HOST "your-project.firebaseio.com"
#define FIREBASE_AUTH ""  // Hoặc database secret
```

### 2. Kiểm Tra Địa Chỉ I2C LCD
Nếu LCD không hiển thị, thử đổi địa chỉ:
```cpp
#define LCD_ADDRESS 0x3F  // Thay vì 0x27
```

Hoặc dùng I2C Scanner để tìm địa chỉ:
```cpp
#include <Wire.h>

void setup() {
  Wire.begin();
  Serial.begin(115200);
  Serial.println("\nI2C Scanner");
}

void loop() {
  byte error, address;
  int nDevices = 0;
  
  for(address = 1; address < 127; address++ ) {
    Wire.beginTransmission(address);
    error = Wire.endTransmission();
    
    if (error == 0) {
      Serial.print("I2C device found at address 0x");
      if (address<16) Serial.print("0");
      Serial.println(address,HEX);
      nDevices++;
    }
  }
  delay(5000);
}
```

### 3. Upload
1. Kết nối ESP32 với máy tính qua USB
2. Chọn đúng Board và Port
3. Nhấn nút Upload (→) trong Arduino IDE
4. Chờ upload hoàn tất

### 4. Kiểm Tra Serial Monitor
1. Tools > Serial Monitor
2. Đặt baud rate: 115200
3. Xem log kết nối WiFi và Firebase

---

## 🧪 Test Hệ Thống

### 1. Kiểm Tra Kết Nối
- LCD hiển thị "WiFi Connected!"
- Serial Monitor hiển thị IP address
- LCD hiển thị "Firebase Ready!"

### 2. Kiểm Tra Hiển Thị Từ
- LCD hiển thị từ vựng (dòng 1: word, dòng 2: meaning)
- Mỗi 5 giây đổi sang từ mới
- Serial Monitor hiển thị: `Displaying [1/5]: hello - xin chào`

### 3. Kiểm Tra Tuần Hoàn
- Sau khi hiển thị hết tất cả từ, quay lại từ đầu
- Tự động cập nhật nếu có từ mới từ app

---

## 🔧 Troubleshooting

### Lỗi 1: LCD Không Hiển Thị
**Nguyên nhân**: Địa chỉ I2C sai hoặc kết nối lỏng

**Giải pháp**:
1. Kiểm tra kết nối dây
2. Chạy I2C Scanner để tìm địa chỉ
3. Thử đổi `LCD_ADDRESS` thành `0x3F`
4. Điều chỉnh biến trở trên LCD để tăng độ tương phản

### Lỗi 2: "WiFi Failed!"
**Nguyên nhân**: SSID hoặc password sai

**Giải pháp**:
1. Kiểm tra lại tên WiFi và mật khẩu
2. Đảm bảo WiFi là 2.4GHz (ESP32 không hỗ trợ 5GHz)
3. Kiểm tra WiFi có hoạt động không

### Lỗi 3: "No words found!"
**Nguyên nhân**: Chưa sync từ app hoặc Firebase path sai

**Giải pháp**:
1. Mở app Android và nhấn "Sync Firebase"
2. Kiểm tra Firebase Console có dữ liệu trong `/unlearnedWords`
3. Kiểm tra `FIREBASE_HOST` đúng chưa

### Lỗi 4: "Error loading word"
**Nguyên nhân**: Firebase Rules chặn hoặc không có quyền đọc

**Giải pháp**:
1. Kiểm tra Firebase Rules (xem phần Lấy Database Secret)
2. Kiểm tra `FIREBASE_AUTH` nếu dùng bảo mật
3. Xem Serial Monitor để biết lỗi cụ thể

### Lỗi 5: ESP32 Restart Liên Tục
**Nguyên nhân**: Nguồn không đủ hoặc code lỗi

**Giải pháp**:
1. Dùng nguồn USB tốt (ít nhất 500mA)
2. Kiểm tra code có lỗi syntax không
3. Thêm delay trong loop nếu cần

---

## 🎨 Tùy Chỉnh

### 1. Thay Đổi Thời Gian Hiển Thị
```cpp
const unsigned long UPDATE_INTERVAL = 10000;  // 10 giây
```

### 2. Hiển Thị Thêm Thông Tin
```cpp
// Hiển thị số thứ tự
lcd.setCursor(14, 0);
lcd.print(currentIndex + 1);
lcd.print("/");
lcd.print(totalWords);
```

### 3. Thêm Scroll Cho Text Dài
```cpp
// Trong displayNextWord(), thay vì:
lcd.print(meaning);

// Dùng:
scrollText(meaning, 1, 300);  // Scroll dòng 2, delay 300ms
```

### 4. Hiển Thị Phonetic
```cpp
// Lấy phonetic
FirebaseJsonData phoneticData;
String phonetic = "";
if (json.get(phoneticData, "phonetic")) {
  phonetic = phoneticData.stringValue;
}

// Hiển thị
lcd.setCursor(0, 1);
lcd.print(phonetic);
```

---

## ✅ Hoàn Thành!

Sau khi hoàn thành bước này, bạn đã có:
- ✅ ESP32 kết nối WiFi
- ✅ ESP32 đọc dữ liệu từ Firebase
- ✅ LCD hiển thị từ vựng tuần hoàn
- ✅ Tự động cập nhật khi có từ mới

### Luồng Hoạt Động Hoàn Chỉnh

```
Android App
    ↓
Thêm từ vựng (NOT_LEARNED)
    ↓
Nhấn "Sync Firebase"
    ↓
Firebase Realtime Database
    ↓
ESP32 đọc dữ liệu
    ↓
LCD hiển thị tuần hoàn (5s/từ)
```

---

## 🚀 Mở Rộng Trong Tương Lai

1. **Thêm nút bấm**: Chuyển từ thủ công
2. **Thêm speaker**: Phát âm thanh
3. **Thêm LED**: Báo hiệu trạng thái
4. **Gửi feedback**: ESP32 gửi tín hiệu "đã xem" về Firebase
5. **Hiển thị ví dụ**: Thêm màn hình lớn hơn (LCD 20x4)

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra Serial Monitor (115200 baud)
2. Kiểm tra Firebase Console
3. Kiểm tra kết nối phần cứng
4. Đảm bảo thư viện đã cài đúng phiên bản

**Log quan trọng**:
- WiFi connection status
- Firebase connection status
- Word display logs
- Error messages
