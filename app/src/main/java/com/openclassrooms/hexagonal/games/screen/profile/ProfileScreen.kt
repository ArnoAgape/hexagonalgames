package com.openclassrooms.hexagonal.games.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {

    val user by viewModel.user.collectAsStateWithLifecycle()

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
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileContent(
                modifier = Modifier.fillMaxWidth(),
                userName = user?.displayName ?: "",
                onTitleChanged = { },
                emailAddress = user?.email ?: "",
                onDescriptionChanged = { },
                onSignOutClick = {
                    viewModel.signOut()
                    onBackClick()
                },
                onDeleteAccountClick = { viewModel.deleteAccount() }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    userName: String,
    onTitleChanged: (String) -> Unit,
    emailAddress: String,
    onDescriptionChanged: (String) -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
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
                Text(userName)
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    value = userName,
                    onValueChange = { onTitleChanged(it) },
                    label = { Text(stringResource(id = R.string.user_name)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    value = emailAddress,
                    onValueChange = { onDescriptionChanged(it) },
                    label = { Text(stringResource(id = R.string.user_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
            }
            Button(
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.action_sign_out)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDeleteAccountClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.action_delete_account)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun ProfileScreenPreview() {
    HexagonalGamesTheme {
        ProfileContent(
            userName = "Aretha Franklin",
            onTitleChanged = { },
            emailAddress = "aretha.franklin@mail.com",
            onDescriptionChanged = { },
            onSignOutClick = { },
            onDeleteAccountClick = { }
        )
    }
}
