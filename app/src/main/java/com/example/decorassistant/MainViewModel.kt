package com.example.decorassistant

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asImageOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MainUiState {

    data object Initial : MainUiState

    data object Loading : MainUiState

    data class Success(
        val outputText: String,
        val outputImages: List<Bitmap>
    ) : MainUiState

    data class Error(
        val errorMessage: String
    ) : MainUiState
}

class MainViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun onSuggestClick(selectedImage: Bitmap) {
        _uiState.value = MainUiState.Loading
        val prompt = "Give me suggestions for furniture or decor that would fit well here and " +
                "an example of how the space coule be furnished and decorated"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputContent = content {
                    image(selectedImage)
                    text(prompt)
                }

                var outputContent = ""
                val outputImages = mutableListOf<Bitmap>()

                generativeModel.generateContentStream(inputContent)
                    .collect { response ->
                        outputContent += response.text
                        response.candidates.forEach { candidate ->
                            candidate.content.parts.forEach { part ->
                                part.asImageOrNull()?.let { outputImages.add(it) }
                            }
                        }
                        _uiState.value = MainUiState.Success(outputContent, outputImages)
                    }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}