# EnglishApp

EnglishApp là một ứng dụng Android viết bằng Kotlin (Jetpack Compose) giúp học và ôn từ vựng tiếng Anh thông minh. Ứng dụng hỗ trợ tra từ, flashcards (kèm spaced-repetition), tạo bài kiểm tra (trắc nghiệm, điền từ, ghép từ) và lưu lịch sử kết quả.

---

## Tính năng chính

- Tra nghĩa từ & hiển thị phát âm (sử dụng Dictionary API).
- Quản lý danh sách từ vựng (thêm, sửa, xóa).
- Flashcards với thuật toán Spaced Repetition (SM-2) để lên lịch ôn tập tự động.
- Tạo bài kiểm tra từ vựng:
  - Trắc nghiệm (multiple choice)
  - Điền từ (fill in the blank)
  - Ghép từ - nghĩa (matching)
- Lưu lịch sử kết quả bài kiểm tra và hiển thị thống kê học tập (điểm trung bình, số từ đang học...).

---

## Kiến trúc & công nghệ

- Ngôn ngữ: Kotlin
- UI: Jetpack Compose
- DI: Koin
- Database: Room (Room database)
- Network: Retrofit (Dictionary API)
- Coroutine + Flow cho concurrency và data stream
- Unit tests: JUnit (có tests cho TestGenerator)

---

## Cấu trúc dự án (điểm qua các package/ module quan trọng)

- `com.example.englishapp`
  - `VocabApplication.kt` — Entry point, khởi tạo Koin.
  - `di/AppModule.kt` — Cấu hình dependency injection (DB, DAOs, Repositories, ViewModels).
- `com.example.englishapp.util`
  - `Constants.kt` — Hằng số (tên DB, API base url, cấu hình test).
  - `SpacedRepetitionAlgorithm.kt` — Thực thi SM-2 để tính lịch ôn flashcard.
- `com.example.englishapp.data`
  - `local` — Room database, DAOs, entities (vocab, test result, flashcard).
  - `remote` — Retrofit client và `DictionaryApi`.
  - `repository` — Lớp repository:
    - `VocabRepository` — Lấy/ cập nhật từ vựng, getRandomVocabs(...), updateVocabReview(...).
    - `TestRepository` — Lưu và truy vấn TestResult.
    - `FlashcardRepository` — Quản lý flashcard, áp dụng spaced repetition.
- `com.example.englishapp.domain.model`
  - Model domain: `Vocab`, `TestQuestion`, `TestResult`, `TestType`, `FlashcardDeck`, `FlashcardProgress`, v.v.
- `com.example.englishapp.ui.navigation`
  - `NavGraph.kt` — Định nghĩa các route/navigation Compose.
- `com.example.englishapp.ui.screens`
  - `home` — `HomeScreen.kt`, `HomeViewModel.kt` (thống kê, quick actions).
  - `search` — `SearchScreen.kt`, `SearchViewModel.kt` (tra từ).
  - `vocabulary` — Danh sách từ, quản lý từ vựng.
  - `flashcard` — Các màn quản lý và học flashcard, ViewModels tương ứng.
  - `test` — Các file chính liên quan test:
    - `TestScreen.kt` — UI cho chọn loại test, hiển thị câu hỏi (multiple choice / fill blank / matching) và màn kết quả.
    - `TestViewModel.kt` — Orchestrator: generateTest(), submitAnswer(), submitMatchingAnswers(), finishTest(), resetTest().
    - `TestResultScreen.kt` — Giao diện hiển thị kết quả và hành động (Làm lại / Về Trang chủ).
  - `TestGenerator` (util) — Sinh câu hỏi từ danh sách vocab (được unit test).

---

## File & chức năng nổi bật (quick reference)

- `app/src/main/java/.../TestViewModel.kt`
  - Sinh bài: `generateTest(testType, questionCount)` → lấy vocabs từ repository → gọi `TestGenerator` để sinh câu hỏi → cập nhật UI state (Testing).
  - Submit đáp án: `submitAnswer()` / `submitMatchingAnswers()` → kiểm tra đúng/sai → cập nhật `VocabRepository.updateVocabReview(...)` → tiến câu tiếp theo hoặc finish.
  - Khi kết thúc: tạo `TestResult`, lưu qua `TestRepository.insertTestResult(...)`.
- `app/src/main/java/.../TestScreen.kt`
  - Compose UI: chọn loại test, các composable cho từng loại câu, hiển thị progress & điều hướng.
- `app/src/main/java/.../TestResultScreen.kt`
  - Hiển thị tổng hợp kết quả, thời gian, điểm, label điểm (Xuất sắc / Giỏi / ...).
- `app/src/main/java/.../SpacedRepetitionAlgorithm.kt`
  - Tính interval, easiness factor, repetitions dựa trên rating của người dùng (AGAIN / HARD / GOOD / EASY).
- `app/src/main/java/.../AppModule.kt`
  - Nơi cấu hình DB, Retrofit, repositories, viewmodels (Koin).

---

## Cài đặt & chạy

Yêu cầu:
- Android Studio Flamingo (hoặc phiên bản mới) + JDK 11/17.
- Gradle wrapper (đã kèm trong repo).

Clone repository:
```bash
git clone https://github.com/ashine92/EnglishApp.git
cd EnglishApp
```

Mở dự án bằng Android Studio, đợi Gradle sync & build.

Build bằng command-line:
```bash
# build debug APK
./gradlew assembleDebug

# chạy unit tests
./gradlew test
```

Chạy trên emulator hoặc thiết bị thật từ Android Studio (Run 'app').

Lưu ý:
- Ứng dụng dùng `Dictionary API` (https://dictionaryapi.dev/). Mạng phải bật để tra từ.
- Database: Room database tên được cấu hình trong `Constants.DATABASE_NAME`.

---

## Phát triển & test

- Dependency Injection: Koin - cấu hình trong `AppModule.kt`.
- Thêm/ sửa ViewModel: đăng ký trong `AppModule.kt` để Koin inject được.
- Unit tests cho `TestGenerator` nằm trong `app/src/test/.../TestGeneratorTest.kt`.
- Kiểm tra logic SM-2 trong `SpacedRepetitionAlgorithm.kt`.

---

## Roadmap / ý tưởng mở rộng (gợi ý)
- Thêm màn xem lại chi tiết các câu hỏi đã làm (review wrong answers).
- Đồng bộ với backend / user account để lưu tiến độ xuyên thiết bị.
- Hỗ trợ nhiều ngôn ngữ UI.
- Tùy chỉnh mức khó / loại distractors cho test.

---
