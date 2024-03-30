package com.example.decorassistant

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MainUiState {

    data object Initial : MainUiState

    data object Loading : MainUiState

    data class Success(
        val outputText: String
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
        val prompt = "Give me suggestions for furniture or decor that would fit well here"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputContent = content {
                    image(selectedImage)
                    text(prompt)
                }

                var outputContent = ""

                generativeModel.generateContentStream(inputContent)
                    .collect { response ->
                        outputContent += response.text
                        _uiState.value = MainUiState.Success(outputContent)
                    }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}