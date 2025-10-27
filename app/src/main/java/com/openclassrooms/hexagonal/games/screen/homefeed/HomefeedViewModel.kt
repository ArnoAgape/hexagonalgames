package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
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
) : ViewModel() {

    /** Holds the current state of the home feed UI (loading, success, or error). */
    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Loading)

    /** Publicly exposed immutable flow for observing post-related UI states. */
    val uiState: StateFlow<PostUiState> = _uiState

    /** Channel used for one-time UI events such as displaying toasts. */
    private val _events = Channel<Event>()

    /** Flow that emits UI events (e.g., toast messages). */
    val eventsFlow = _events.receiveAsFlow()

    /** Observes whether a user is currently signed in. */
    val isUserSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Initializes the ViewModel by fetching all posts from the repository.
     */
    init {
        getAllPosts()
    }

    /**
     * Fetches all posts from [PostRepository] and updates the [_uiState] accordingly.
     *
     * - Sets the UI state to [PostUiState.Loading] before starting.
     * - If the repository emits an error, switches to [PostUiState.Error.Generic].
     * - If the post list is empty, switches to [PostUiState.Error.Empty].
     * - Otherwise, emits [PostUiState.Success] with the loaded posts.
     */
    private fun getAllPosts() {
        viewModelScope.launch {
            postRepository.posts
                .onStart { _uiState.value = PostUiState.Loading }
                .catch { e ->
                    _uiState.value = PostUiState.Error.Generic(e.message ?: "Unknown error")
                }
                .collect { posts ->
                    _uiState.value = if (posts.isEmpty()) {
                        PostUiState.Error.Empty()
                    } else {
                        PostUiState.Success(posts)
                    }
                }
        }
    }

    /**
     * Refreshes the list of posts from the repository.
     *
     * If no network connection is available, it emits an [Event.ShowToast] with
     * a "no network" message and cancels the refresh operation.
     */
    fun refreshPosts() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowToast(R.string.no_network))
                return@launch
            }
            getAllPosts()
        }
    }
}