package com.example.decorassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

@Suppress("UNCHECKED_CAST")
val MainViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val config = generationConfig {
            temperature = 0.5f
        }
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.0-pro-vision-latest",
            apiKey = BuildConfig.apiKey,
            generationConfig = config
        )
        return MainViewModel(generativeModel) as T
    }
}