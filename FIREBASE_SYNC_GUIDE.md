# 🔥 Hướng Dẫn Hoàn Thành Bước 4: Cập Nhật ViewModel và UI

## ✅ Những Gì Đã Hoàn Thành

### 1. **Cập nhật VocabDao** ✓
- Đã thêm query `getUnlearnedVocabs()` để lấy tất cả từ có trạng thái `NOT_LEARNED`
- Query này sắp xếp theo `createdDate` (từ cũ đến mới)

### 2. **Thêm Firebase Dependencies** ✓
- Đã thêm Firebase BOM và Firebase Realtime Database vào `app/build.gradle.kts`
- Đã thêm Google Services plugin vào `build.gradle.kts` (project level)

### 3. **Cập nhật VocabRepository** ✓
Đã thêm các chức năng:
- `getUnlearnedVocabs()`: Lấy danh sách từ chưa học
- `syncUnlearnedVocabsToFirebase()`: Đồng bộ tất cả từ chưa học lên Firebase
- `clearFirebaseVocabs()`: Xóa tất cả từ vựng trên Firebase

### 4. **Cập nhật VocabViewModel** ✓
Đã thêm:
- State `syncStatus` để theo dõi trạng thái đồng bộ
- State `unlearnedVocabCount` để hiển thị số lượng từ chưa học
- Hàm `syncToFirebase()`: Đồng bộ lên Firebase
- Hàm `resetSyncStatus()`: Reset trạng thái
- Hàm `clearFirebaseVocabs()`: Xóa dữ liệu Firebase
- Sealed class `SyncStatus` với 4 trạng thái: Idle, Loading, Success, Error

### 5. **Cập nhật VocabListScreen** ✓
Đã thêm:
- Hiển thị số lượng từ chưa học
- Nút "🔄 Sync Firebase" (chỉ active khi có từ chưa học)
- Dialog `FirebaseSyncDialog` với các trạng thái:
  - **Idle**: Xác nhận đồng bộ
  - **Loading**: Hiển thị progress indicator
  - **Success**: Thông báo thành công với số lượng từ đã sync
  - **Error**: Hiển thị lỗi và hướng dẫn khắc phục

---

## 📋 Các Bước Tiếp Theo

### Bước 4.1: Sync Gradle Dependencies (BẮT BUỘC)

Mở Android Studio và sync Gradle để tải Firebase dependencies:

```bash
# Trong Android Studio:
# File > Sync Project with Gradle Files
# Hoặc nhấn nút "Sync Now" khi xuất hiện banner
```

**Lưu ý**: Nếu gặp lỗi, kiểm tra:
- File `google-services.json` đã có trong thư mục `app/`
- Kết nối Internet ổn định
- Gradle version tương thích

---

### Bước 4.2: Kiểm Tra Firebase Console

1. **Truy cập Firebase Console**: https://console.firebase.google.com/
2. **Chọn project** của bạn (hoặc tạo mới nếu chưa có)
3. **Vào Realtime Database**:
   - Sidebar > Build > Realtime Database
   - Nếu chưa có, nhấn "Create Database"
   - Chọn location (ví dụ: `asia-southeast1`)
   - Chọn "Start in test mode" (tạm thời)

4. **Cấu hình Rules** (tạm thời cho development):
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

⚠️ **Cảnh báo**: Rules này cho phép mọi người đọc/ghi. Sau khi test xong, hãy bảo mật hơn:
```json
{
  "rules": {
    "unlearnedWords": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

5. **Lấy Database URL**:
   - Trong Realtime Database, copy URL (dạng: `https://your-project.firebaseio.com`)
   - URL này sẽ dùng cho ESP32

---

### Bước 4.3: Test Chức Năng Sync trong App

1. **Build và chạy app**:
```bash
# Trong Android Studio:
# Run > Run 'app' (hoặc Shift+F10)
```

2. **Thêm từ vựng test**:
   - Vào tab "Tìm kiếm" hoặc "Từ vựng"
   - Thêm ít nhất 3-5 từ với trạng thái "Chưa học" (NOT_LEARNED)

3. **Test sync**:
   - Vào tab "Từ vựng"
   - Kiểm tra hiển thị: "Chưa học: X từ"
   - Nhấn nút "🔄 Sync Firebase"
   - Xác nhận trong dialog
   - Chờ thông báo "✅ Đã đồng bộ X từ thành công!"

4. **Kiểm tra Firebase Console**:
   - Refresh trang Realtime Database
   - Xem dữ liệu trong node `/unlearnedWords`
   - Cấu trúc dữ liệu:
```json
{
  "unlearnedWords": {
    "0": {
      "word": "hello",
      "phonetic": "/həˈloʊ/",
      "meaning": "xin chào",
      "example": "Hello, how are you?",
      "category": "Greeting",
      "createdDate": 1700000000000
    },
    "1": {
      "word": "world",
      ...
    }
  }
}
```

---

## 🎯 Cách Sử Dụng Trong App

### Giao Diện Người Dùng

1. **Màn hình Từ Vựng**:
   - Hiển thị tổng số từ và số từ chưa học
   - Nút "🔄 Sync Firebase" (màu xanh lá)
   - Nút chỉ active khi có từ chưa học

2. **Dialog Đồng Bộ**:
   - **Trước khi sync**: Hiển thị số lượng từ sẽ đồng bộ
   - **Đang sync**: Hiển thị loading spinner
   - **Thành công**: Hiển thị số từ đã sync
   - **Lỗi**: Hiển thị thông báo lỗi và hướng dẫn

### Flow Hoạt Động

```
User nhấn "Sync Firebase"
    ↓
Dialog hiển thị số từ chưa học
    ↓
User xác nhận "Đồng bộ"
    ↓
ViewModel gọi Repository.syncUnlearnedVocabsToFirebase()
    ↓
Repository lấy từ DB → Tạo Map → Gửi lên Firebase
    ↓
Thành công: Hiển thị "✅ Đã đồng bộ X từ"
Lỗi: Hiển thị "❌ Lỗi: [message]"
```

---

## 🔧 Troubleshooting

### Lỗi 1: "Firebase not initialized"
**Nguyên nhân**: Chưa có `google-services.json` hoặc chưa sync Gradle

**Giải pháp**:
1. Kiểm tra file `google-services.json` trong `app/`
2. Sync Gradle: File > Sync Project with Gradle Files
3. Clean và rebuild: Build > Clean Project > Rebuild Project

### Lỗi 2: "Permission denied"
**Nguyên nhân**: Firebase Rules chặn write

**Giải pháp**:
1. Vào Firebase Console > Realtime Database > Rules
2. Đổi thành test mode (xem Bước 4.2)
3. Publish rules

### Lỗi 3: "Network error"
**Nguyên nhân**: Không có Internet hoặc Firebase URL sai

**Giải pháp**:
1. Kiểm tra kết nối Internet
2. Kiểm tra Firebase Database đã được tạo chưa
3. Xem Logcat để biết chi tiết lỗi

### Lỗi 4: Nút "Sync Firebase" bị disable
**Nguyên nhân**: Không có từ nào có trạng thái NOT_LEARNED

**Giải pháp**:
1. Thêm từ vựng mới (mặc định là NOT_LEARNED)
2. Hoặc đổi trạng thái từ cũ về "Chưa học"

---

## 📱 Kiểm Tra Dữ Liệu

### Trong App (Logcat)
```
D/VocabViewModel: Synced 5 words to Firebase
```

### Trong Firebase Console
```json
{
  "unlearnedWords": {
    "0": { "word": "apple", ... },
    "1": { "word": "banana", ... },
    "2": { "word": "cat", ... }
  }
}
```

### Kiểm Tra Bằng Code (Optional)
Thêm vào `VocabViewModel.kt` để debug:
```kotlin
fun debugPrintUnlearnedVocabs() {
    viewModelScope.launch {
        vocabRepository.getUnlearnedVocabs().first().forEach { vocab ->
            Log.d("VocabViewModel", "Unlearned: ${vocab.word}")
        }
    }
}
```

---

## 🎉 Hoàn Thành Bước 4!

Sau khi test thành công, bạn đã hoàn thành:
- ✅ Bước 1: Chuẩn bị Firebase
- ✅ Bước 2: Cập nhật VocabDao
- ✅ Bước 3: Cập nhật VocabRepository
- ✅ Bước 4: Cập nhật ViewModel và UI

### Tiếp Theo: Bước 5 - Thiết Lập ESP32

Bước tiếp theo là lập trình ESP32 để:
1. Kết nối WiFi
2. Đọc dữ liệu từ Firebase
3. Hiển thị tuần hoàn trên LCD 16x2

Xem file `ESP32_SETUP_GUIDE.md` để tiếp tục!

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra Logcat trong Android Studio
2. Kiểm tra Firebase Console > Realtime Database
3. Đảm bảo `google-services.json` đúng project
4. Kiểm tra Internet connection

**Log quan trọng cần xem**:
- `VocabViewModel`: Kết quả sync
- `FirebaseDatabase`: Lỗi kết nối Firebase
- `VocabRepository`: Lỗi xử lý dữ liệu
