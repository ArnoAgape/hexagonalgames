package com.openclassrooms.hexagonal.games.screen.detailPost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state of the Post detail screen.
 *
 * This ViewModel combines data from both [PostRepository] and [CommentRepository]
 * to display a single Post and its associated comments. It also observes the
 * user's sign-in status through [UserRepository].
 *
 * Key responsibilities:
 * - Observes the Post details in real time.
 * - Observes and updates the list of comments related to the Post.
 * - Handles data refresh operations while ensuring network availability.
 * - Emits [Event]s (e.g., toast messages) to the UI for feedback.
 *
 * The current UI state is represented as a [DetailUiState], which encapsulates
 * both [DetailPostUiState] and [DetailCommentUiState].
 */
@HiltViewModel
class DetailPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
    private val networkUtils: NetworkUtils
) :
    ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    // refresh data
    private var postJob: Job? = null
    private var commentJob: Job? = null

    private val _uiState = MutableStateFlow(
        DetailUiState(
            DetailPostUiState.Loading,
            DetailCommentUiState.Loading
        )
    )
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isUserSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        observePost()
        observeComments(postId)
    }

    private fun observePost() {
        postJob?.cancel()
        postJob = viewModelScope.launch {
            postRepository.getPostById(postId)
                .onStart {
                    _uiState.update {
                        it.copy(postState = DetailPostUiState.Loading)
                    }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            postState = DetailPostUiState.Error.Generic(
                                e.message ?: "Unknown error"
                            )
                        )
                    }
                }
                .collect { post ->
                    val newState = if (post != null) {
                        DetailPostUiState.Success(post)
                    } else {
                        DetailPostUiState.Error.Empty("Impossible to find the post")
                    }
                    _uiState.update { it.copy(postState = newState) }
                }
        }
    }

    private fun observeComments(postId: String) {

        commentJob?.cancel()
        commentJob = viewModelScope.launch {
            commentRepository.observeComments(postId)
                .onStart {
                    _uiState.update {
                        it.copy(commentState = DetailCommentUiState.Loading)
                    }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            commentState = DetailCommentUiState.Error.Generic(
                                e.message ?: "Unknown error"
                            )
                        )
                    }
                }
                .collect { comments ->
                    val newState = if (comments.isNotEmpty()) {
                        DetailCommentUiState.Success(comments)
                    } else {
                        DetailCommentUiState.Error.Empty("No comments found")
                    }
                    _uiState.update { it.copy(commentState = newState) }
                }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowToast(R.string.no_network))
                return@launch
            }
            observePost()
            observeComments(postId)
        }
    }

}

/**
 * Represents the combined UI state for the Post detail screen.
 *
 * This data class holds two distinct UI states:
 * - [postState]: The current state of the Post being displayed.
 * - [commentState]: The current state of the associated comments.
 *
 * It provides a unified structure for managing and rendering both pieces
 * of information in a single reactive stream.
 */
data class DetailUiState(
    val postState: DetailPostUiState,
    val commentState: DetailCommentUiState
)