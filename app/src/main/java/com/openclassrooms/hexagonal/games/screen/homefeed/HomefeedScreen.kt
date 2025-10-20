package com.openclassrooms.hexagonal.games.screen.homefeed

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.util.DebugLogger
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomefeedScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomefeedViewModel,
    onPostClick: (Post) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFABClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val isSignedIn by homeViewModel.isUserSignedIn.collectAsStateWithLifecycle()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val refreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.homefeed_fragment_label))
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.contentDescription_more)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                onSettingsClick()
                            },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.action_settings)
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                onProfileClick()
                            },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.action_account)
                                )
                            }
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
                            context, context.getString(R.string.error_no_account),
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
            isRefreshing = uiState is PostUiState.Loading,
            onRefresh = { homeViewModel.refreshPosts() }
        ) {
            when (uiState) {
                is PostUiState.Success ->
                    HomefeedList(
                        posts = (uiState as PostUiState.Success).posts,
                        onPostClick = onPostClick
                    )

                is PostUiState.Error.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_posts),
                            modifier = Modifier.fillMaxSize(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is PostUiState.Error.Network -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Toast.makeText(
                            context, context.getString(R.string.no_network),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                else -> {}
            }
        }
    }
}


@Composable
private fun HomefeedList(
    modifier: Modifier = Modifier,
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(posts) { post ->
            HomefeedCell(
                post = post,
                onPostClick = onPostClick
            )
        }
    }
}

@Composable
private fun HomefeedCell(
    post: Post,
    onPostClick: (Post) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onPostClick(post)
        }) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            Text(
                text = stringResource(
                    id = R.string.by,
                    post.author?.displayName ?: ""
                ),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge
            )
            if (post.photoUrl != null) {
                AsyncImage(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .aspectRatio(ratio = 16 / 9f),
                    model = post.photoUrl,
                    imageLoader = LocalContext.current.imageLoader.newBuilder()
                        .logger(DebugLogger())
                        .build(),
                    placeholder = ColorPainter(Color.DarkGray),
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(Modifier.height(16.dp))
            if (!post.description.isNullOrEmpty()) {
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomefeedCellPreview() {
    HexagonalGamesTheme {
        HomefeedCell(
            post = Post(
                id = "1",
                title = "title",
                description = "description",
                photoUrl = null,
                timestamp = 1,
                author = User(
                    id = "1",
                    displayName = "displayName",
                    email = "test@mail.fr",
                    photoUrl = null
                )
            ),
            onPostClick = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun HomefeedCellImagePreview() {
    HexagonalGamesTheme {
        HomefeedCell(
            post = Post(
                id = "1",
                title = "title",
                description = null,
                photoUrl = "https://picsum.photos/id/85/1080/",
                timestamp = 1,
                author = User(
                    id = "1",
                    displayName = "displayName",
                    email = "test@mail.fr",
                    photoUrl = null
                )
            ),
            onPostClick = {}
        )
    }
}