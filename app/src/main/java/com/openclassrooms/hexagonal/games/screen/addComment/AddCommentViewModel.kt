package com.openclassrooms.hexagonal.games.screen.addComment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.addPost.FormEvent
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel responsible for handling comment creation logic.
 *
 * This ViewModel manages the process of adding comments to a specific post, handling
 * both local validation and repository operations. It interacts with:
 * - [CommentRepository] for storing comments.
 * - [UserRepository] for verifying if the user is signed in.
 * - [NetworkUtils] for ensuring network availability.
 *
 * It exposes a [StateFlow] representing the current [AddCommentUiState],
 * and emits [Event]s (e.g., toast messages) through a [Channel] for UI feedback.
 *
 * The ViewModel also holds user state information to determine if comment
 * actions are permitted based on authentication status.
 */
@HiltViewModel
class AddCommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCommentUiState>(AddCommentUiState.Idle)
    val uiState: StateFlow<AddCommentUiState> = _uiState.asStateFlow()
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    /**
     * Internal mutable state flow representing the current comment being edited.
     */
    private var _comment = MutableStateFlow(
        Comment(
            id = UUID.randomUUID().toString(),
            content = "",
            timestamp = System.currentTimeMillis(),
            author = null
        )
    )

    /**
     * Public state flow representing the current post being edited.
     * This is immutable for consumers.
     */
    val comment: StateFlow<Comment> = _comment.asStateFlow()

    /**
     * StateFlow derived from the post that emits a FormError if the content is empty, null otherwise.
     */
    val isCommentValid = comment.map { currentComment ->
        currentComment.content.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    init {
        viewModelScope.launch {
            _user.value = userRepository.getCurrentUser()
        }
    }

    /**
     * Handles an empty comment.
     *
     * @param formEvent The comment event to be processed.
     */
    fun onAction(formEvent: FormEvent) {
        when (formEvent) {
            is FormEvent.CommentChanged -> {
                _comment.value = _comment.value.copy(
                    content = formEvent.comment
                )
            }

            else -> Unit
        }
    }

    /**
     * Attempts to add the current comment to the repository after setting the author.
     */
    fun addComment(postId: String) {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowToast(R.string.no_network))
                return@launch
            }

            _uiState.value = AddCommentUiState.Loading

            val currentUser = _user.value
            if (currentUser == null) {
                _uiState.value = AddCommentUiState.Error.NoAccount()
                _events.trySend(Event.ShowToast(R.string.error_no_account_comment))
                return@launch
            }

            try {
                val commentToSave = _comment.value.copy(author = currentUser)
                commentRepository.addComment(postId, commentToSave)

                _uiState.value = AddCommentUiState.Success(commentToSave)
                _events.trySend(Event.ShowToast(R.string.comment_success))

            } catch (e: IOException) {
                _uiState.value = AddCommentUiState.Error.Generic("Network error: ${e.message}")
                _events.trySend(Event.ShowToast(R.string.no_network))
            } catch (_: Exception) {
                _uiState.value = AddCommentUiState.Error.Generic()
                _events.trySend(Event.ShowToast(R.string.error_generic))
            }
        }
    }

    /**
     * Handles validation and submission logic for saving a new Comment.
     *
     * This function verifies that the required fields are not empty before attempting
     * to add the comment. If any validation fails, it emits a toast event with a corresponding
     * error message. Otherwise, it proceeds to call [addComment].
     *
     * Validation rules:
     * - Title cannot be blank.
     * - Either a description or a photo must be provided.
     */
    fun onSaveClicked(postId: String) {
        val comment = _comment.value
        when {
            comment.content.isEmpty() -> {
                _events.trySend(Event.ShowToast(R.string.error_comment))
            }

            else -> {
                addComment(postId)
            }
        }
    }
}
