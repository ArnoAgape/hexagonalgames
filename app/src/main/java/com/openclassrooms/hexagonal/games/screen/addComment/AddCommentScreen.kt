package com.openclassrooms.hexagonal.games.screen.addComment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.screen.addPost.FormEvent
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.common.EventsEffect
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentScreen(
    viewModel: AddCommentViewModel,
    postId: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val comment by viewModel.comment.collectAsStateWithLifecycle()
    val isCommentValid by viewModel.isCommentValid.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Any toast (comment added, no network...)
    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                onSaveClick()
            }
        }
    }

    Scaffold(
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

        when (uiState) {
            is AddCommentUiState.Idle, is AddCommentUiState.Success -> {
                val commentToDisplay =
                    if (uiState is AddCommentUiState.Success) (uiState as AddCommentUiState.Success).comment else comment

                CreateComment(
                    modifier = Modifier.padding(contentPadding),
                    comment = commentToDisplay.content,
                    onContentChanged = { viewModel.onAction(FormEvent.CommentChanged(it)) },
                    onSaveClicked = { viewModel.onSaveClicked(postId) },
                    isCommentValid = isCommentValid,
                    isLoading = false
                )
            }

            is AddCommentUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.publishing),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is AddCommentUiState.Error -> {
                val errorState = uiState as AddCommentUiState.Error
                val message = when (errorState) {
                    is AddCommentUiState.Error.NoAccount -> (uiState as AddCommentUiState.Error.NoAccount).message
                    is AddCommentUiState.Error.Generic -> (uiState as AddCommentUiState.Error.Generic).message
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CreateComment(
    modifier: Modifier = Modifier,
    comment: String,
    onContentChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    isCommentValid: Boolean,
    isLoading: Boolean
) {
    val scrollState = rememberScrollState()


    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
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
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    value = comment,
                    onValueChange = { onContentChanged(it) },
                    label = { Text(stringResource(id = R.string.hint_comment)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            Button(
                onClick = onSaveClicked,
                enabled = isCommentValid && !isLoading
            ) {
                if (isLoading) {
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
            onContentChanged = { },
            onSaveClicked = { },
            isCommentValid = true,
            isLoading = false
        )
    }
}