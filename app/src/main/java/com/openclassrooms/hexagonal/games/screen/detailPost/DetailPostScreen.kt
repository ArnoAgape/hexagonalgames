package com.openclassrooms.hexagonal.games.screen.detailPost

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    viewModel: DetailPostViewModel,
    onFABClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val state by viewModel.uiPostState.collectAsStateWithLifecycle()
    val commentState by viewModel.uiCommentState.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val refreshState = rememberPullToRefreshState()

    LaunchedEffect(state) {
        if (state is DetailPostUiState.Error.Network) {
            Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show()
        }
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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

                        is DetailPostUiState.Error -> {
                            Text(stringResource(R.string.error))
                        }

                        else -> {}
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
                    if (isSignedIn) {
                        onFABClick()
                    } else {
                        Toast.makeText(
                            context, context.getString(R.string.error_no_account_comment),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.description_button_add)
                )
            }
        }
    ) { contentPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            state = refreshState,
            isRefreshing = state is DetailPostUiState.Loading,
            onRefresh = { viewModel.refreshData() }
        ) {
            when (state) {
                is DetailPostUiState.Success -> {
                    val post = (state as DetailPostUiState.Success).post
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        item {
                            PostContent(post = post)
                        }

                        when (commentState) {
                            is DetailCommentUiState.Success -> {
                                val comments =
                                    (commentState as DetailCommentUiState.Success).comments
                                val user = (commentState as DetailCommentUiState.Success).user
                                if (comments.isEmpty()) {
                                    item {
                                        Text(
                                            text = stringResource(R.string.no_comment),
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(comments) { comment ->
                                        CommentContent(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            comment = comment,
                                            user = user
                                        )
                                    }
                                }
                            }

                            is DetailCommentUiState.Error -> {
                                item {
                                    Text(
                                        text = stringResource(R.string.no_comment),
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            else -> {
                                item {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }

                is DetailPostUiState.Error -> {
                    when (val errorState = state as DetailPostUiState.Error) {
                        is DetailPostUiState.Error.Network -> {
                            Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                        }

                        is DetailPostUiState.Error.Empty -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_post),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }


                        is DetailPostUiState.Error.Generic -> {
                            Toast.makeText(context, errorState.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                is DetailPostUiState.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun PostContent(
    post: Post
) {
    Surface(
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
            }
            Spacer(Modifier.height(8.dp))

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
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.title_comments),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable

private fun CommentContent(
    modifier: Modifier = Modifier,
    comment: Comment,
    user: User?
) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .padding(16.dp)
        ) {
            user?.displayName?.let {
                Text(
                    text = stringResource(R.string.by, it),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Comment section
                Row {
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailScreenContentPreviewable(
    post: Post,
    user: User,
    comments: List<Comment>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Post section
        item {
            PostContent(
                post = post
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Comment section
        items(comments) { comment ->
            CommentContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                comment = comment,
                user = user
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DetailScreenPreview() {
    HexagonalGamesTheme {
        DetailScreenContentPreviewable(
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
            comments = listOf(
                Comment(
                    id = "1",
                    content = "I love that song",
                    timestamp = 1,
                    author = User(
                        id = "1",
                        displayName = "Aretha Franklin",
                        email = "test@mail.fr",
                        photoUrl = null
                    )
                )
            ),
            user = User(
                id = "1",
                displayName = "Toto toto",
                email = "test@mail.fr",
                photoUrl = null
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun PostScreenPreview() {
    HexagonalGamesTheme {
        PostContent(
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
            user = User(
                id = "1",
                displayName = "Toto toto",
                email = "test@mail.fr",
                photoUrl = null
            )
        )
    }
}