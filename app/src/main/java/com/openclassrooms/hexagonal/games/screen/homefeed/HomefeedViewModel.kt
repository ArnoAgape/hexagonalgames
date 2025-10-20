package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing data and events related to the Homefeed.
 * This ViewModel retrieves posts from the PostRepository and exposes them as a Flow<List<Post>>,
 * allowing UI components to observe and react to changes in the posts data.
 */
@HiltViewModel
class HomefeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) :
    ViewModel() {

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Loading)
    val uiState: StateFlow<PostUiState> = _uiState

    val isUserSignedIn: StateFlow<Boolean> =
        userRepository.isUserSignedIn()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        getAllPosts()
    }

    private fun getAllPosts() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _uiState.value = PostUiState.Error.Network("No internet connection")
                return@launch
            }
            postRepository.posts
                .onStart { _uiState.value = PostUiState.Loading }
                .catch { e -> _uiState.value = PostUiState.Error.Generic(e.message ?: "Unknown error") }
                .collect { posts ->
                    _uiState.value = if (posts.isEmpty()) {
                        PostUiState.Error.Empty()
                    } else {
                        PostUiState.Success(posts)
                    }
                }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _uiState.value = PostUiState.Error.Network("No internet connection")
                return@launch
            }

            postRepository.posts
                .catch { e -> _uiState.value = PostUiState.Error.Generic(e.message ?: "Error") }
                .collect { posts ->
                    _uiState.value = if (posts.isEmpty()) {
                        PostUiState.Error.Empty()
                    } else {
                        PostUiState.Success(posts)
                    }
                }
        }
    }
}
