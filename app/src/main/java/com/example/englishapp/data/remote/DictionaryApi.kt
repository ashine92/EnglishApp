package com.example.englishapp.data.remote

import com.example.englishapp.data.remote.dto.DictionaryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("entries/en/{word}")
    suspend fun searchWord(@Path("word") word: String): List<DictionaryResponse>
}