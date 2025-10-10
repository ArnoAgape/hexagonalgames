package com.openclassrooms.hexagonal.games.screen.detailPost

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
fun DetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailPostViewModel,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.action_account))
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
        },
    ) { contentPadding ->
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        when (state) {
            is DetailUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Error -> {
                val message = (state as DetailUiState.Error).message
                Text("Erreur : $message", color = MaterialTheme.colorScheme.error)
            }

            is DetailUiState.Success -> {
                val post = (state as DetailUiState.Success).post

                Column(Modifier.padding(16.dp)) {
                    post.author?.displayName?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(post.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    post.photoUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = post.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(post.description ?: "", style = MaterialTheme.typography.bodyMedium)
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
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.img_profile_default),
                contentDescription = stringResource(R.string.profile_picture),
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Text( // author
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.action_sign_out)
                )
                Text( // title
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.action_sign_out)
                )
                Text( // description
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.action_sign_out)
                )
                if (post.photoUrl != null) {
                    AsyncImage(
                        // photo
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