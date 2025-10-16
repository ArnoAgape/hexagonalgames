package com.openclassrooms.hexagonal.games.screen.addComment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.screen.addPost.FormError
import com.openclassrooms.hexagonal.games.screen.addPost.FormEvent
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentScreen(
    modifier: Modifier = Modifier,
    viewModel: AddCommentViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.add_comment_label))
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

        val comment by viewModel.comment.collectAsStateWithLifecycle()
        val error by viewModel.error.collectAsStateWithLifecycle()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val isCommentValid by viewModel.isCommentValid.collectAsStateWithLifecycle()

        when (uiState) {
            is AddCommentUiState.Loading -> {
                // Saving
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

            is AddCommentUiState.Success -> {
                LaunchedEffect(uiState) {
                    onSaveClick()
                }
            }

            is AddCommentUiState.Error -> {
                val message = (uiState as AddCommentUiState.Error).message
                    ?: stringResource(R.string.unknown_error)
                Text(
                    text = "Error : $message",
                    color = MaterialTheme.colorScheme.error
                )
            }

            AddCommentUiState.Idle -> {
                CreateComment(
                    modifier = Modifier.padding(contentPadding),
                    error = error,
                    comment = comment.content,
                    onTitleChanged = { viewModel.onAction(FormEvent.CommentChanged(it)) },
                    onSaveClicked = {
                    },
                    isCommentValid = isCommentValid,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun CreateComment(
    modifier: Modifier = Modifier,
    comment: String,
    onTitleChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    error: FormError?,
    forceValidation: Boolean = false,
    isCommentValid: Boolean,
    uiState: AddCommentUiState
) {
    var titleFieldHasBeenTouched by remember { mutableStateOf(false) }
    var wasFocused by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val showTitleError =
        (titleFieldHasBeenTouched || forceValidation) && error is FormError.TitleError

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
                    value = comment,
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
            }
            Button(
                onClick = onSaveClicked,
                enabled = isCommentValid && uiState !is AddCommentUiState.Loading
            ) {
                if (uiState is AddCommentUiState.Loading) {
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
private fun CreateCommentPreview() {
    HexagonalGamesTheme {
        CreateComment(
            comment = "test",
            onTitleChanged = { },
            onSaveClicked = { },
            error = null,
            isCommentValid = true,
            uiState = AddCommentUiState.Idle
        )
    }
}