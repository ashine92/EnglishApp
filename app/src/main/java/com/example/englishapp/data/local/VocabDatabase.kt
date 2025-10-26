package com.example.englishapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.englishapp.data.local.dao.TestResultDao
import com.example.englishapp.data.local.dao.VocabDao
import com.example.englishapp.data.local.entity.TestResultEntity
import com.example.englishapp.data.local.entity.VocabEntity

@Database(
    entities = [VocabEntity::class, TestResultEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VocabDatabase : RoomDatabase() {
    abstract fun vocabDao(): VocabDao
    abstract fun testResultDao(): TestResultDao
}