package com.openclassrooms.hexagonal.games.screen.detailPost

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailPostViewModel,
    onFABClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val state by viewModel.uiPostState.collectAsStateWithLifecycle()
    val commentState by viewModel.uiCommentState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    when (state) {
                        is DetailPostUiState.Success -> {
                            val post = (state as DetailPostUiState.Success).post
                            Text(
                                text = post.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        is DetailPostUiState.Loading -> {
                            Text(stringResource(R.string.loading))
                        }

                        is DetailPostUiState.Error -> {
                            Text(stringResource(R.string.error))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onFABClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.description_button_add)
                )
            }
        }
    ) { contentPadding ->
        when (state) {
            is DetailPostUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailPostUiState.Error -> {
                val message = (state as DetailPostUiState.Error).message
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: $message",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is DetailPostUiState.Success -> {
                val post = (state as DetailPostUiState.Success).post
                PostContent(
                    modifier = Modifier.padding(contentPadding),
                    post = post
                )
            }
        }
        when (commentState) {
            is DetailCommentUiState.Loading -> {
                CircularProgressIndicator()
            }

            is DetailCommentUiState.Error -> {
                val message = (commentState as DetailCommentUiState.Error).message
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: $message",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is DetailCommentUiState.Success -> {
                LazyColumn {
                    items((commentState as DetailCommentUiState.Success).comments) { comment ->
                        CommentContent(
                            modifier = Modifier.padding(contentPadding),
                            comment = comment
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostContent(
    modifier: Modifier = Modifier,
    post: Post
) {
    val scrollState = rememberScrollState()

    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Author
            post.author?.displayName?.let {
                Text(
                    text = stringResource(R.string.by, it),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
            }

            // Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))

            // Description
            if (!post.description.isNullOrBlank()) {
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
            }

            // Photo
            post.photoUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
private fun CommentContent(
    modifier: Modifier = Modifier,
    comment: Comment
) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.img_profile_default),
                contentDescription = stringResource(R.string.profile_picture),
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Comment section
            Row {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun PostScreenPreview() {
    HexagonalGamesTheme {
        PostContent(
            modifier = Modifier.fillMaxWidth(),
            post = Post(
                id = "1",
                title = "Songs",
                description = "Classical music from this talented singer",
                photoUrl = null,
                timestamp = 1,
                author = User(
                    id = "1",
                    displayName = "Aretha Franklin",
                    email = "test@mail.fr",
                    photoUrl = null
                )
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun CommentScreenPreview() {
    HexagonalGamesTheme {
        CommentContent(
            modifier = Modifier.fillMaxWidth(),
            comment = Comment(
                id = "1",
                content = "I love that song",
                timestamp = 1,
                author = User(
                    id = "1",
                    displayName = "Aretha Franklin",
                    email = "test@mail.fr",
                    photoUrl = null
                )
            ),
        )
    }
}