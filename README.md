# EnglishApp - Ứng dụng học tiếng Anh thông minh với AI 🚀

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Google Gemini](https://img.shields.io/badge/Google%20Gemini%20AI-8E75B2?style=for-the-badge&logo=google&logoColor=white)

**Ứng dụng học từ vựng tiếng Anh thông minh được hỗ trợ bởi Google Gemini AI**

[Tính năng](#-tính-năng-chính) • [Gemini AI](#-tích-hợp-gemini-ai---trọng-tâm-của-ứng-dụng) • [Cài đặt](#-cài-đặt--chạy) • [Tài liệu](#-tài-liệu-tham-khảo)

</div>

---

## 🤖 Tích hợp Gemini AI - Trọng tâm của ứng dụng

EnglishApp sử dụng **Google Gemini AI** làm cốt lõi cho hai tính năng chính: tra từ thông minh và chấm điểm phát âm. Đây là điểm khác biệt quan trọng so với các ứng dụng học tiếng Anh truyền thống.

### 1. 🔍 Tra từ thông minh (Smart Word Lookup)

**Vị trí code:** `app/src/main/java/com/example/englishapp/data/remote/GeminiWordLookupService.kt`

#### Tính năng chính:
- **Model sử dụng:** `gemini-2.5-flash` - Model AI mới nhất từ Google
- **Hỗ trợ Level-Based Learning:** Định nghĩa theo cấp độ CEFR (A1-C2)
- **Thay thế hoàn toàn Dictionary API:** Không còn phụ thuộc vào API từ điển truyền thống
- **Ngữ cảnh thông minh:** Hiểu ngữ cảnh và cung cấp định nghĩa phù hợp
- **Ví dụ tự nhiên:** Câu ví dụ thực tế, dễ hiểu với từng cấp độ

#### Cách hoạt động:
```kotlin
class GeminiWordLookupService(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )
    
    // Tra từ cơ bản
    suspend fun lookupWord(word: String): Result<GeminiWordResponse>
    
    // Tra từ theo cấp độ (A1-C2)
    suspend fun lookupWordWithLevel(word: String, level: String): Result<GeminiWordResponse>
}
```

#### Prompt Engineering:
Service sử dụng prompt được thiết kế tối ưu để nhận JSON chuẩn:
```kotlin
"""
Look up the English word or phrase: "$word"

Provide the following information in JSON format:
{
  "word": "the word or phrase",
  "meaning": "clear definition in English",
  "example": "example sentence using the word",
  "synonyms": ["synonym1", "synonym2", "synonym3"],
  "phonetic": "IPA pronunciation notation"
}

Important:
- Return ONLY valid JSON, no additional text
- Provide at least 3 synonyms if available
- Use IPA (International Phonetic Alphabet) for pronunciation
- Make the example sentence natural and clear
"""
```

#### Response Format:
```json
{
  "word": "example",
  "meaning": "a thing characteristic of its kind or illustrating a general rule",
  "example": "This painting is a fine example of the artist's work",
  "synonyms": ["sample", "specimen", "instance"],
  "phonetic": "/ɪɡˈzɑːmp(ə)l/"
}
```

### 2. 🎤 Chấm điểm phát âm với AI (AI Pronunciation Scoring)

**Vị trí code:** `app/src/main/java/com/example/englishapp/data/remote/PronunciationScoringService.kt`

#### Quy trình chấm điểm:
1. **Input:** 
   - `expectedText` - Từ/câu chuẩn cần phát âm
   - `userText` - Văn bản từ SpeechRecognizer (phát âm của người dùng)

2. **AI Analysis:**
   - So sánh độ tương đồng giữa expectedText và userText
   - Phát hiện từ thiếu, từ sai, thứ tự sai
   - Đánh giá chất lượng phát âm tổng thể

3. **Output:** Score 0-100 + Feedback chi tiết

#### Thang điểm AI:
```kotlin
// Scoring criteria:
- 90-100: Excellent pronunciation, all words correct
- 75-89:  Good pronunciation, minor mistakes
- 60-74:  Fair pronunciation, several mistakes
- 40-59:  Needs improvement, many mistakes
- 0-39:   Poor pronunciation, major issues
```

#### Phản hồi thông minh:
```json
{
  "score": 85,
  "similarity": "High",
  "mistakes": [
    "Missing word: 'the'",
    "Wrong pronunciation: 'quick' → 'quik'"
  ],
  "feedback": "Great job! Focus on pronouncing 'quick' with the full 'ck' sound. Practice the article 'the' more slowly."
}
```

#### Cách sử dụng:
```kotlin
class PronunciationScoringService(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )
    
    suspend fun scorePronunciation(
        expectedText: String,
        userText: String
    ): Result<PronunciationScoreResponse>
}
```

### 3. 📊 Luồng xử lý dữ liệu với Gemini

#### Word Lookup Flow:
```
User Input (Search)
    ↓
SearchScreen → SearchViewModel
    ↓
VocabRepository
    ↓
GeminiWordLookupService
    ↓
Google Gemini API (gemini-2.5-flash)
    ↓
JSON Response
    ↓
GeminiWordResponse (DTO)
    ↓
Vocabulary (Domain Model)
    ↓
Room Database + UI Display
```

#### Pronunciation Scoring Flow:
```
User speaks into microphone
    ↓
Android SpeechRecognizer
    ↓
PronunciationScreen → PronunciationViewModel
    ↓
PronunciationRepository
    ↓
PronunciationScoringService
    ↓
Google Gemini API (gemini-2.5-flash)
    ↓
JSON Response with Score + Feedback
    ↓
PronunciationResult (Domain Model)
    ↓
Database (Progress Tracking) + UI Display
```

#### Architecture Diagram:
```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                  │
│  SearchScreen | PronunciationScreen | FlashcardUI   │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│                   ViewModel Layer                    │
│  SearchViewModel | PronunciationViewModel | etc.    │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│                  Repository Layer                    │
│   VocabRepository | PronunciationRepository         │
└─────────┬───────────────────────┬───────────────────┘
          ↓                       ↓
┌──────────────────┐    ┌─────────────────────────────┐
│  Room Database   │    │   Gemini AI Services        │
│  - VocabDao      │    │  - GeminiWordLookupService  │
│  - FlashcardDao  │    │  - PronunciationScoring     │
│  - ProgressDao   │    └─────────┬───────────────────┘
└──────────────────┘              ↓
                         ┌────────────────────┐
                         │ Google Gemini API  │
                         │ (gemini-2.5-flash) │
                         └────────────────────┘
```

---

## ✨ Tính năng chính

### 🔍 Tra từ với Gemini AI
- **Định nghĩa thông minh** được tạo bởi AI, phù hợp ngữ cảnh
- **Level-based learning** - Định nghĩa theo cấp độ A1-C2 CEFR
- **Synonyms & Examples** - Từ đồng nghĩa và câu ví dụ tự nhiên
- **IPA Phonetic** - Phiên âm quốc tế chuẩn

### 🎤 Luyện phát âm với AI Scoring
- **Chấm điểm 0-100** - Đánh giá chính xác bằng AI
- **Speech Recognition** - Android SpeechRecognizer tích hợp
- **Text-to-Speech** - Nghe phát âm chuẩn trước khi luyện tập
- **Detailed Feedback** - Phản hồi chi tiết về lỗi phát âm
- **Progress Tracking** - Theo dõi tiến độ và lịch sử luyện tập

### 🎴 Flashcards thông minh
- **Auto-Generated Decks** - Tự động tạo bộ thẻ theo trạng thái học:
  - 🆕 Unknown Words (từ mới)
  - 📖 Learning Words (đang học)
  - ✅ Known Words (đã thuộc)
  - 🔄 Review All (ôn tất cả)
- **Spaced Repetition (SM-2)** - Thuật toán ôn tập tối ưu
- **Swipe Navigation** - Vuốt trái/phải để điều hướng
- **Tap to Flip** - Chạm để lật thẻ

### 📝 Tạo bài kiểm tra
- **Multiple Choice** - Trắc nghiệm 4 đáp án
- **Fill in the Blank** - Điền từ vào chỗ trống
- **Matching** - Ghép từ với nghĩa (tối đa 3 từ/câu, yêu cầu tối thiểu 10 từ)

### 📊 Thống kê & Lịch sử
- **Test Results** - Lưu và xem lại kết quả bài kiểm tra
- **Learning Progress** - Theo dõi tiến độ học từ vựng
- **Pronunciation History** - Lịch sử luyện phát âm với điểm số

---

## 🏗️ Kiến trúc & Công nghệ

### Tech Stack

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) | Latest | Ngôn ngữ lập trình chính |
| ![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white) | BOM 2024.09.00 | Modern UI framework |
| ![Room](https://img.shields.io/badge/Room-4285F4?style=flat-square&logo=android&logoColor=white) | 2.6.1 | Local database |
| ![Koin](https://img.shields.io/badge/Koin-FF6F00?style=flat-square) | 3.5.3 | Dependency Injection |
| ![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=flat-square) | 2.9.0 | HTTP client |
| ![Gemini AI](https://img.shields.io/badge/Gemini%20AI-8E75B2?style=flat-square&logo=google&logoColor=white) | 0.1.2 | AI services |
| ![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=flat-square&logo=kotlin&logoColor=white) | 1.8.0 | Async programming |

### Clean Architecture

```
┌────────────────────────────────────────────┐
│           Presentation Layer               │
│  • Jetpack Compose UI                      │
│  • ViewModels                              │
│  • Navigation                              │
└───────────────┬────────────────────────────┘
                ↓
┌────────────────────────────────────────────┐
│            Domain Layer                    │
│  • Business Logic                          │
│  • Use Cases                               │
│  • Domain Models                           │
└───────────────┬────────────────────────────┘
                ↓
┌────────────────────────────────────────────┐
│             Data Layer                     │
│  • Repositories                            │
│  • Room Database                           │
│  • Gemini AI Services                      │
│  • DTOs & Entities                         │
└────────────────────────────────────────────┘
```

### MVVM Pattern

```
View (Compose) ←→ ViewModel ←→ Repository ←→ Data Sources
                                              ├─ Room DB
                                              └─ Gemini API
```

---

## 📁 Cấu trúc dự án

```
app/src/main/java/com/example/englishapp/
│
├── 📱 VocabApplication.kt                    # Entry point, khởi tạo Koin
│
├── 🔧 di/
│   └── AppModule.kt                          # Dependency Injection config
│
├── 🎨 ui/
│   ├── navigation/
│   │   ├── NavGraph.kt                       # Navigation routes
│   │   └── Screen.kt                         # Screen definitions
│   │
│   ├── screens/
│   │   ├── home/                             # 🏠 Home screen
│   │   ├── search/                           # 🔍 Search với Gemini AI
│   │   │   ├── SearchScreen.kt
│   │   │   └── SearchViewModel.kt
│   │   ├── pronunciation/                    # 🎤 Pronunciation với AI
│   │   │   ├── PronunciationScreen.kt
│   │   │   ├── PronunciationViewModel.kt
│   │   │   └── PronunciationWordSelectionScreen.kt
│   │   ├── flashcard/                        # 🎴 Flashcards
│   │   ├── test/                             # 📝 Tests
│   │   └── vocabulary/                       # 📚 Vocabulary management
│   │
│   ├── components/                           # Reusable UI components
│   └── theme/                                # Material3 theming
│
├── 📊 data/
│   ├── local/
│   │   ├── VocabDatabase.kt                  # Room database
│   │   ├── dao/                              # Data Access Objects
│   │   │   ├── VocabDao.kt
│   │   │   ├── FlashcardDao.kt
│   │   │   ├── TestResultDao.kt
│   │   │   └── PronunciationProgressDao.kt
│   │   └── entity/                           # Database entities
│   │
│   ├── remote/                               # ⭐ Gemini AI Services
│   │   ├── GeminiWordLookupService.kt       # 🔍 Smart Word Lookup
│   │   ├── PronunciationScoringService.kt   # 🎤 AI Pronunciation Scoring
│   │   └── dto/                              # Data Transfer Objects
│   │       ├── GeminiWordResponse.kt
│   │       └── PronunciationScoreResponse.kt
│   │
│   └── repository/                           # Repository pattern
│       ├── VocabRepository.kt
│       ├── FlashcardRepository.kt
│       ├── TestRepository.kt
│       └── PronunciationRepository.kt
│
├── 🎯 domain/
│   ├── model/                                # Domain models
│   │   ├── Vocabulary.kt
│   │   ├── Flashcard.kt
│   │   ├── TestResult.kt
│   │   └── PronunciationResult.kt
│   └── usecase/                              # Business logic
│
└── 🛠️ util/
    ├── Constants.kt                          # ⚠️ Gemini API Key config
    ├── SpacedRepetitionAlgorithm.kt          # SM-2 algorithm
    └── TestGenerator.kt                      # Test generation logic
```

### 🌟 Highlight: File Gemini quan trọng

| File | Chức năng |
|------|-----------|
| `GeminiWordLookupService.kt` | Service tra từ với Gemini AI, model `gemini-2.5-flash` |
| `PronunciationScoringService.kt` | Service chấm điểm phát âm với AI |
| `Constants.kt` | **Nơi cấu hình GEMINI_API_KEY** ⚠️ |
| `SearchViewModel.kt` | Orchestrates word lookup với Gemini |
| `PronunciationViewModel.kt` | Quản lý pronunciation scoring flow |

---

## 🚀 Cài đặt & Chạy

### Yêu cầu hệ thống
- ✅ Android Studio Flamingo hoặc mới hơn
- ✅ JDK 11 hoặc 17
- ✅ Android SDK 26+ (Android 8.0+)
- ✅ **Google Gemini API Key** (bắt buộc)

### 1️⃣ Clone Repository
```bash
git clone https://github.com/ashine92/EnglishApp.git
cd EnglishApp
```

### 2️⃣ ⭐ Cấu hình Gemini API Key (QUAN TRỌNG)

Đây là bước **bắt buộc** để ứng dụng hoạt động!

#### Cách 1: Sửa trực tiếp trong Constants.kt (Đơn giản)
1. Lấy API key tại: **https://makersuite.google.com/app/apikey**
2. Mở file: `app/src/main/java/com/example/englishapp/util/Constants.kt`
3. Thay thế:
   ```kotlin
   const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
   ```
   Thành:
   ```kotlin
   const val GEMINI_API_KEY = "AIza...your_actual_key"
   ```

#### Cách 2: Sử dụng local.properties (Bảo mật hơn)
1. Tạo/mở file `local.properties` (ở thư mục gốc)
2. Thêm dòng:
   ```properties
   GEMINI_API_KEY=AIza...your_actual_key
   ```
3. Cập nhật `app/build.gradle.kts`:
   ```kotlin
   android {
       defaultConfig {
           val geminiKey = project.findProperty("GEMINI_API_KEY") as String? ?: ""
           buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
       }
       buildFeatures {
           buildConfig = true
       }
   }
   ```
4. Sửa `Constants.kt`:
   ```kotlin
   const val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY
   ```

⚠️ **Lưu ý:** API key miễn phí có giới hạn. Xem tại [Google AI Studio](https://ai.google.dev/pricing)

### 3️⃣ Build & Run

#### Từ Android Studio:
1. Mở project bằng Android Studio
2. Đợi Gradle sync hoàn tất
3. Chọn device/emulator
4. Click **Run** ▶️

#### Từ Command Line:
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (cần emulator/device)
./gradlew connectedAndroidTest
```

APK sẽ nằm ở: `app/build/outputs/apk/debug/app-debug.apk`

### 4️⃣ Cấp quyền

Ứng dụng cần các quyền sau:
- ✅ **INTERNET** - Gọi Gemini API (tự động)
- ✅ **RECORD_AUDIO** - Ghi âm phát âm (yêu cầu runtime)
- ✅ **ACCESS_NETWORK_STATE** - Kiểm tra kết nối (tự động)

---

## 📱 Hướng dẫn sử dụng

### 🔍 Tra từ với Gemini AI

1. Mở tab **"Tra từ"** 🔍
2. Nhập từ tiếng Anh cần tra
3. Nhấn **Search**
4. Xem kết quả từ Gemini AI:
   - ✅ Định nghĩa (meaning)
   - ✅ Phiên âm IPA (phonetic)
   - ✅ Ví dụ (example)
   - ✅ Từ đồng nghĩa (synonyms)
5. Nhấn **"Lưu từ vựng"** để thêm vào database

**Mẹo:** Chọn level A1-C2 để nhận định nghĩa phù hợp trình độ!

### 🎤 Luyện phát âm với AI

1. Mở **"Luyện phát âm"** 🎤 từ Home
2. Chọn từ muốn luyện từ danh sách
3. Xem từ, phiên âm và nghĩa
4. Nhấn **🔊** để nghe phát âm chuẩn
5. Nhấn **🎤** để ghi âm
6. Nói rõ ràng vào microphone
7. Nhấn **⏹️** để dừng ghi
8. Nhấn **"Score My Pronunciation"**
9. Xem kết quả:
   - **Score:** 0-100
   - **Similarity:** High/Medium/Low
   - **Mistakes:** Danh sách lỗi cụ thể
   - **Feedback:** Gợi ý cải thiện
10. Nhấn **"Chọn từ khác"** để luyện từ tiếp

**Mẹo:** Luyện trong môi trường yên tĩnh để AI chấm điểm chính xác!

### 🎴 Flashcards

1. Mở tab **"Flashcards"** 🎴
2. Nhấn **✨** (sparkle icon) ở góc trên
3. Chọn loại deck muốn tạo:
   - 🆕 **Unknown Words** - Từ mới (NEW)
   - 📖 **Learning Words** - Đang học (LEARNING)
   - ✅ **Known Words** - Đã thuộc (MASTERED)
   - 🔄 **Review All** - Ôn tất cả
4. Deck tự động được tạo với từ vựng phù hợp
5. Nhấn vào deck để bắt đầu học
6. **Vuốt trái/phải** để chuyển thẻ
7. **Chạm** để lật thẻ xem nghĩa
8. Đánh giá độ khó:
   - ❌ Again - Quên hoàn toàn
   - 😓 Hard - Khó nhớ
   - ✅ Good - Nhớ tốt
   - ⭐ Easy - Rất dễ

### 📝 Bài kiểm tra

1. Mở tab **"Kiểm tra"** 📝
2. Chọn loại bài kiểm tra:
   - **Multiple Choice** - Trắc nghiệm
   - **Fill in the Blank** - Điền từ
   - **Matching** - Ghép từ-nghĩa (cần ≥10 từ)
3. Chọn số lượng câu hỏi
4. Làm bài theo hướng dẫn
5. Xem kết quả và thống kê

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

Tests bao gồm:
- `TestGeneratorTest` - Logic sinh câu hỏi
- `SpacedRepetitionAlgorithmTest` - SM-2 algorithm
- Repository tests

### Manual Testing Checklist

#### ✅ Gemini Word Lookup
- [ ] Tra từ đơn giản (ví dụ: "hello")
- [ ] Tra cụm từ (ví dụ: "break down")
- [ ] Chọn level A1-C2
- [ ] Kiểm tra synonyms trả về
- [ ] Kiểm tra IPA phonetic format
- [ ] Test khi mất mạng

#### ✅ AI Pronunciation Scoring
- [ ] Cấp quyền microphone
- [ ] Nghe TTS phát âm chuẩn
- [ ] Ghi âm phát âm
- [ ] Kiểm tra score 0-100
- [ ] Xem mistakes list
- [ ] Đọc feedback
- [ ] Test với nhiều từ khác nhau

#### ✅ Auto-Generated Flashcards
- [ ] Tạo deck "Unknown Words"
- [ ] Tạo deck "Learning Words"
- [ ] Tạo deck "Known Words"
- [ ] Tạo deck "Review All"
- [ ] Kiểm tra nội dung deck đúng status

---

## 📊 API Costs & Limits

### Gemini API (Free Tier)
- **Requests per minute:** 60 RPM
- **Requests per day:** 1,500 RPD
- **Tokens per minute:** 32,000 TPM
- **Model:** gemini-2.5-flash

**💡 Mẹo tiết kiệm:**
- Cache kết quả tra từ phổ biến
- Limit số lần chấm phát âm/phút
- Sử dụng batch requests khi có thể

📖 Chi tiết: [Google AI Pricing](https://ai.google.dev/pricing)

---

## 🔐 Security & Privacy

### API Key Security
⚠️ **Quan trọng:**
- KHÔNG commit API key lên GitHub
- Sử dụng `local.properties` hoặc environment variables
- Rotate key định kỳ
- Monitor usage tại Google AI Studio

### Data Privacy
- ✅ Tất cả dữ liệu lưu local (Room Database)
- ✅ Không gửi thông tin cá nhân lên server
- ✅ Speech recognition text không được lưu vĩnh viễn
- ✅ Không thu thập analytics

---

## 🗺️ Roadmap

### ✅ Đã hoàn thành
- [x] Gemini AI integration cho tra từ
- [x] AI pronunciation scoring
- [x] Auto-generated flashcard decks
- [x] Pronunciation progress tracking
- [x] Level-based word lookup (A1-C2)
- [x] Matching test improvements (3 words max)

### 🚧 Đang phát triển
- [ ] Cache Gemini API responses
- [ ] Offline mode với cached data
- [ ] Export/Import flashcard decks

### 📋 Kế hoạch tương lai
- [ ] User accounts & cloud sync
- [ ] Multi-language UI support
- [ ] Advanced pronunciation analytics
- [ ] Phoneme-level pronunciation feedback
- [ ] Custom pronunciation practice sentences
- [ ] Social features (share decks)
- [ ] Gamification (achievements, streaks)
- [ ] Vocabulary difficulty levels
- [ ] Review wrong answers screen

---

## 📚 Tài liệu tham khảo

### 📄 Documentation Files
- 📘 [**NEW_FEATURES.md**](./NEW_FEATURES.md) - Chi tiết 3 tính năng mới (Gemini, Flashcards, Pronunciation)
- 📗 [**IMPLEMENTATION_SUMMARY.md**](./IMPLEMENTATION_SUMMARY.md) - Tổng kết implementation, architecture
- 📙 [**IMPROVEMENTS_GUIDE.md**](./IMPROVEMENTS_GUIDE.md) - Hướng dẫn cải tiến UI/UX
- 📕 [**PRONUNCIATION_IMPROVEMENTS.md**](./PRONUNCIATION_IMPROVEMENTS.md) - Cải tiến pronunciation feature
- 📔 [**USER_GUIDE_PRONUNCIATION.md**](./USER_GUIDE_PRONUNCIATION.md) - Hướng dẫn sử dụng pronunciation chi tiết

### 🔗 External Links
- [Google Gemini AI](https://ai.google.dev/) - Official documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI framework
- [Room Database](https://developer.android.com/training/data-storage/room) - Local persistence
- [Koin](https://insert-koin.io/) - Dependency Injection
- [Material Design 3](https://m3.material.io/) - Design system

---

## 🙏 Credits

### Powered By
- **Google Gemini AI** (`gemini-2.5-flash`) - Smart word lookup & pronunciation scoring
- **Android SpeechRecognizer** - Voice input
- **Android TextToSpeech** - Pronunciation playback
- **Jetpack Compose** - Modern UI framework
- **Room Database** - Local data persistence
- **Koin** - Dependency injection framework

### Open Source Libraries
- Retrofit - HTTP client
- Gson - JSON parsing
- Kotlin Coroutines - Async programming
- Material Components - UI components

---

## 📄 License

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

<div align="center">

**Made with ❤️ using Google Gemini AI**

⭐ Star this repo if you find it helpful!

[Report Bug](https://github.com/ashine92/EnglishApp/issues) • [Request Feature](https://github.com/ashine92/EnglishApp/issues)

</div>
