package com.example.englishapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.englishapp.data.local.dao.FlashcardDao
import com.example.englishapp.data.local.dao.PronunciationProgressDao
import com.example.englishapp.data.local.dao.TestResultDao
import com.example.englishapp.data.local.dao.VocabDao
import com.example.englishapp.data.local.entity.*

@Database(
    entities = [
        VocabEntity::class,
        TestResultEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        FlashcardProgressEntity::class,
        PronunciationProgressEntity::class
    ],
    version = 4,  // Increased from 3 to 4 for pronunciation progress
    exportSchema = false
)
abstract class VocabDatabase : RoomDatabase() {
    abstract fun vocabDao(): VocabDao
    abstract fun testResultDao(): TestResultDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun pronunciationProgressDao(): PronunciationProgressDao
}