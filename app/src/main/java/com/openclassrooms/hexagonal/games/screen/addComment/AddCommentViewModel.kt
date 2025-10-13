package com.openclassrooms.hexagonal.games.screen.addComment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.addPost.FormError
import com.openclassrooms.hexagonal.games.screen.addPost.FormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddCommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow(userRepository.getCurrentUser())
    val user: StateFlow<User?> = _user

    private val _error = MutableStateFlow<FormError?>(null)
    val error = _error.asStateFlow()

    private val _uiState = MutableStateFlow<AddCommentUiState>(AddCommentUiState.Idle)
    val uiState = _uiState.asStateFlow()

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
    val comment: StateFlow<Comment>
        get() = _comment

    /**
     * Attempts to add the current comment to the repository after setting the author.
     */
    fun addComment() {
        viewModelScope.launch {
            try {
                _uiState.value = AddCommentUiState.Loading

                val currentComment = comment.value.copy(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    author = user.value
                )

                commentRepository.addComment(currentComment)

                _uiState.value = AddCommentUiState.Success
                _error.value = null

                Log.d("AddCommentViewModel", "Comment added successfully: $currentComment")

            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("AddCommentViewModel", "Error while adding the comment", e)
                    _uiState.value = AddCommentUiState.Error(e.message)
                }
            }
        }
    }

    /**
     * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
     */
    val isCommentValid = comment.map { currentComment ->
        currentComment.content.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    /**
     * Handles an empty comment.
     *
     * @param formEvent The comment event to be processed.
     */
    fun onAction(formEvent: FormEvent) {
        when (formEvent) {
            is FormEvent.DescriptionChanged -> {
                _comment.value = _comment.value.copy(
                    content = formEvent.description
                )
            }

            else -> null
        }
    }

    /**
     * Verifies mandatory fields of the comment
     * and returns a corresponding CommentError if so.
     *
     * @return A CommentError.EmptyComment if comment is empty, null otherwise.
     */
    private fun verifyComment(comment: Comment = _comment.value): FormError? {
        return when {
            comment.content.isBlank() -> FormError.CommentError
            else -> null
        }
    }

    fun onSaveClicked() {
        Log.d("AddPostViewModel", ">>> Post before validation: ${_comment.value}")
        val validationError = verifyComment()
        if (validationError != null) {
            Log.d("AddCommentViewModel", ">>> Validation failed: $validationError")
            _error.value = validationError
        } else {
            Log.d("AddCommentViewModel", ">>> Validation OK, addComment() called")
            addComment()
        }
    }
}
