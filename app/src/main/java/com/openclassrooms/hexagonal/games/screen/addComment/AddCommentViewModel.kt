package com.openclassrooms.hexagonal.games.screen.addComment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.screen.addPost.FormError
import com.openclassrooms.hexagonal.games.screen.addPost.FormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCommentUiState.initial())
    val uiState: StateFlow<AddCommentUiState> = _uiState.asStateFlow()
    private val _user = MutableStateFlow(userRepository.getCurrentUser())
    private val _error = MutableStateFlow<FormError?>(null)


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
            _uiState.update { it.copy(isSaving = true, isSuccess = false, error = null) }

            try {

                val commentToSave = _comment.value.copy(author = _user.value)

                commentRepository.addComment(postId, commentToSave)

                _uiState.update { it.copy(isSaving = false, isSuccess = true) }

                Log.d("AddCommentViewModel", "Comment added successfully: $commentToSave")

            } catch (e: Exception) {
                Log.e("AddCommentViewModel", "Error while adding the comment", e)
                _uiState.update { it.copy(isSaving = false, error = FormError.GenericError) }
            }
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
    fun onSaveClicked(postId: String) {
        _error.value = null
        if (!isCommentValid.value) {
            _error.value = FormError.CommentError
        } else {
            addComment(postId)
        }
    }
}
