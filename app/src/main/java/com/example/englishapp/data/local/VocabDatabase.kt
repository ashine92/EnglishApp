package com.example.englishapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.englishapp.data.local.dao.FlashcardDao
import com.example.englishapp.data.local.dao.TestResultDao
import com.example.englishapp.data.local.dao.VocabDao
import com.example.englishapp.data.local.entity.*

@Database(
    entities = [
        VocabEntity::class,
        TestResultEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        FlashcardProgressEntity::class
    ],
    version = 3,  // Increased from 2 to 3 for learning status migration
    exportSchema = false
)
abstract class VocabDatabase : RoomDatabase() {
    abstract fun vocabDao(): VocabDao
    abstract fun testResultDao(): TestResultDao
    abstract fun flashcardDao(): FlashcardDao  // Thêm dòng này
}