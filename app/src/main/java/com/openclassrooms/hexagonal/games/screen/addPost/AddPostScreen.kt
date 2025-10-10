package com.openclassrooms.hexagonal.games.screen.addPost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    modifier: Modifier = Modifier,
    viewModel: AddPostViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.add_fragment_label))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        val post by viewModel.post.collectAsStateWithLifecycle()
        val error by viewModel.error.collectAsStateWithLifecycle()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val isPostValid by viewModel.isPostValid.collectAsStateWithLifecycle()

        when (uiState) {
            is AddPostUiState.Loading -> {
                // Loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.publishing),
                        modifier = Modifier.padding(top = 80.dp)
                    )
                }
            }

            is AddPostUiState.Success -> {
                LaunchedEffect(uiState) {
                    onSaveClick()
                }
            }

            is AddPostUiState.Error -> {
                val message = (uiState as AddPostUiState.Error).message
                    ?: stringResource(R.string.unknown_error)
                Text(
                    text = "Error : $message",
                    color = MaterialTheme.colorScheme.error
                )
            }

            AddPostUiState.Idle -> {
                CreatePost(
                    modifier = Modifier.padding(contentPadding),
                    error = error,
                    title = post.title,
                    onTitleChanged = { viewModel.onAction(FormEvent.TitleChanged(it)) },
                    description = post.description ?: "",
                    onDescriptionChanged = { viewModel.onAction(FormEvent.DescriptionChanged(it)) },
                    onPhotoSelected = { viewModel.onAction(FormEvent.PhotoChanged(it)) },
                    onSaveClicked = {
                        viewModel.onSaveClicked()
                    },
                    isPostValid = isPostValid,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun CreatePost(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChanged: (String) -> Unit,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onSaveClicked: () -> Unit,
    error: FormError?,
    forceValidation: Boolean = false,
    isPostValid: Boolean,
    uiState: AddPostUiState
) {
    var titleFieldHasBeenTouched by remember { mutableStateOf(false) }
    var wasFocused by remember { mutableStateOf(false) }

    var descriptionFieldTouched by remember { mutableStateOf(false) }
    var descriptionFocused by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        onPhotoSelected(uri)
    }

    val showTitleError =
        (titleFieldHasBeenTouched || forceValidation) && error is FormError.TitleError
    val showDescriptionError =
        (descriptionFieldTouched || forceValidation) && error is FormError.DescriptionError
    val showPhotoError = (forceValidation) && error is FormError.PhotoError

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (wasFocused && !focusState.isFocused) {
                                titleFieldHasBeenTouched = true
                            }
                            wasFocused = focusState.isFocused
                        },
                    value = title,
                    isError = showTitleError,
                    onValueChange = { onTitleChanged(it) },
                    label = { Text(stringResource(id = R.string.hint_title)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )

                if (showTitleError) {
                    Text(
                        text = stringResource(id = error.messageRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (descriptionFocused && !focusState.isFocused) {
                                descriptionFieldTouched = true
                            }
                            descriptionFocused = focusState.isFocused
                        },
                    value = description,
                    isError = showDescriptionError,
                    onValueChange = { onDescriptionChanged(it) },
                    label = { Text(stringResource(id = R.string.hint_description)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                if (showDescriptionError) {
                    Text(
                        text = stringResource(id = error.messageRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 🖼️ Image picker
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = stringResource(R.string.preview_photo),
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (selectedImageUri == null)
                                stringResource(R.string.select_photo)
                            else
                                stringResource(R.string.change_photo),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (showPhotoError) {
                        Text(
                            text = stringResource(id = error.messageRes),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Button(
                onClick = onSaveClicked,
                enabled = isPostValid && uiState !is AddPostUiState.Loading
            ) {
                if (uiState is AddPostUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(id = R.string.action_save))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CreatePostPreview() {
    HexagonalGamesTheme {
        CreatePost(
            title = "test",
            onTitleChanged = { },
            description = "description",
            onDescriptionChanged = { },
            onSaveClicked = { },
            error = null,
            onPhotoSelected = { },
            isPostValid = true,
            uiState = AddPostUiState.Idle
        )
    }
}

@PreviewLightDark
@Composable
private fun CreatePostErrorPreview() {
    HexagonalGamesTheme {
        CreatePost(
            title = "test",
            onTitleChanged = { },
            description = "description",
            onDescriptionChanged = { },
            onSaveClicked = { },
            error = FormError.TitleError,
            onPhotoSelected = { },
            isPostValid = true,
            uiState = AddPostUiState.Idle
        )
    }
}