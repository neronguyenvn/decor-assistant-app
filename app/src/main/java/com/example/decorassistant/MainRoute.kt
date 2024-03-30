package com.example.decorassistant

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.example.decorassistant.ui.component.MyModalBottomSheet
import com.example.decorassistant.util.ComposeFileProvider
import kotlinx.coroutines.launch

@Composable
fun MainRoute(
    viewModel: MainViewModel = viewModel(factory = MainViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MainScreen(uiState = uiState) { selectedImage ->
        coroutineScope.launch {
            val bitmap = selectedImage.let {
                val imageRequest = imageRequestBuilder
                    .data(it)
                    .precision(Precision.EXACT)
                    .build()
                try {
                    val result = imageLoader.execute(imageRequest)
                    if (result is SuccessResult) {
                        return@let (result.drawable as BitmapDrawable).bitmap
                    } else {
                        return@let null
                    }
                } catch (e: Exception) {
                    return@let null
                }
            }
            bitmap?.let { viewModel.onSuggestClick(bitmap) }
        }
    }
}

@Composable
private fun MainScreen(
    uiState: MainUiState,
    onSuggestClick: (Uri) -> Unit
) {
    var imageUri by rememberSaveable { mutableStateOf(Uri.EMPTY) }

    Scaffold { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(paddingValues)
                .padding(vertical = 16.dp)
        ) {
            Text("Select a picture of your interior design")

            ImageSelectUi(imageUri = imageUri) {
                imageUri = it
            }

            Button(
                onClick = { onSuggestClick(imageUri) },
                modifier = Modifier.animateContentSize(),
            ) {
                if (uiState == MainUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "Give me suggestions")
                }
            }

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                when (uiState) {
                    is MainUiState.Success -> Text(uiState.outputText)
                    is MainUiState.Error -> Text(uiState.errorMessage)
                    else -> Unit
                }
            }
        }
    }
}

@Composable
fun ImageSelectUi(
    imageUri: Uri,
    changeImageUri: (Uri) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf(Uri.EMPTY) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { changeImageUri(uri) }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) changeImageUri(tempUri)
        }
    )

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        onClick = { showBottomSheet = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (imageUri == Uri.EMPTY) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = ""
                )
            } else {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showBottomSheet) {
        MyModalBottomSheet(
            onDismiss = {
                showBottomSheet = false
            },
            onTakePhotoClick = {
                showBottomSheet = false
                tempUri = ComposeFileProvider.getImageUri(context)
                cameraLauncher.launch(tempUri)
            },
            onPhotoGalleryClick = {
                showBottomSheet = false
                imagePicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        )
    }
}
