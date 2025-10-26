package com.example.englishapp.di

import androidx.room.Room
import com.example.englishapp.data.local.VocabDatabase
import com.example.englishapp.data.remote.DictionaryApi
import com.example.englishapp.data.remote.RetrofitClient
import com.example.englishapp.data.repository.TestRepository
import com.example.englishapp.data.repository.VocabRepository
import com.example.englishapp.ui.screens.home.HomeViewModel
import com.example.englishapp.ui.screens.search.SearchViewModel
import com.example.englishapp.ui.screens.test.TestViewModel
import com.example.englishapp.ui.screens.vocabulary.VocabViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            VocabDatabase::class.java,
            "vocab_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<VocabDatabase>().vocabDao() }
    single { get<VocabDatabase>().testResultDao() }

    // Network
    single<DictionaryApi> { RetrofitClient.api }

    // Repositories
    single { VocabRepository(get(), get()) }
    single { TestRepository(get()) }

    // ViewModels
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { VocabViewModel(get()) }
    viewModel { TestViewModel(get(), get()) }
}