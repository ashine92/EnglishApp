package com.example.englishapp.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.englishapp.data.local.VocabDatabase
import com.example.englishapp.data.remote.GeminiWordLookupService
import com.example.englishapp.data.remote.PronunciationScoringService
import com.example.englishapp.data.repository.FlashcardRepository
import com.example.englishapp.data.repository.PronunciationRepository
import com.example.englishapp.data.repository.TestRepository
import com.example.englishapp.data.repository.VocabRepository
import com.example.englishapp.ui.screens.flashcard.FlashcardStudyViewModel
import com.example.englishapp.ui.screens.flashcard.FlashcardViewModel
import com.example.englishapp.ui.screens.home.HomeViewModel
import com.example.englishapp.ui.screens.pronunciation.PronunciationViewModel
import com.example.englishapp.ui.screens.search.SearchViewModel
import com.example.englishapp.ui.screens.test.TestViewModel
import com.example.englishapp.ui.screens.vocabulary.VocabViewModel
import com.example.englishapp.util.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Migration from version 2 to 3: Convert learning status from 3 states to 2 states
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Convert old status values to new ones
        // NEW -> NOT_LEARNED
        // LEARNING -> NOT_LEARNED
        // MASTERED -> LEARNED
        database.execSQL("UPDATE vocabulary SET learningStatus = 'NOT_LEARNED' WHERE learningStatus = 'NEW'")
        database.execSQL("UPDATE vocabulary SET learningStatus = 'NOT_LEARNED' WHERE learningStatus = 'LEARNING'")
        database.execSQL("UPDATE vocabulary SET learningStatus = 'LEARNED' WHERE learningStatus = 'MASTERED'")
    }
}

// Migration from version 3 to 4: Add pronunciation_progress table
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS pronunciation_progress (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                vocabId INTEGER NOT NULL,
                word TEXT NOT NULL,
                userText TEXT NOT NULL,
                score INTEGER NOT NULL,
                similarity TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            VocabDatabase::class.java,
            "vocab_database"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<VocabDatabase>().vocabDao() }
    single { get<VocabDatabase>().testResultDao() }
    single { get<VocabDatabase>().flashcardDao() }
    single { get<VocabDatabase>().pronunciationProgressDao() }

    // Network - Gemini API Services
    single { GeminiWordLookupService(Constants.GEMINI_API_KEY) }
    single { PronunciationScoringService(Constants.GEMINI_API_KEY) }

    // Repositories
    single { VocabRepository(get(), get()) }
    single { TestRepository(get()) }
    single { FlashcardRepository(get(), get()) }
    single { PronunciationRepository(get(), get()) }

    // ViewModels
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { VocabViewModel(get()) }
    viewModel { TestViewModel(get(), get()) }
    viewModel { FlashcardViewModel(get(), get()) }
    viewModel { FlashcardStudyViewModel(get(), get()) }
    viewModel { PronunciationViewModel(get(), get()) }
}