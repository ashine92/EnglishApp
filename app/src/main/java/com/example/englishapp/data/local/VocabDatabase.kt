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
    version = 2,  // Tăng version từ 1 lên 2
    exportSchema = false
)
abstract class VocabDatabase : RoomDatabase() {
    abstract fun vocabDao(): VocabDao
    abstract fun testResultDao(): TestResultDao
    abstract fun flashcardDao(): FlashcardDao  // Thêm dòng này
}