package com.openclassrooms.hexagonal.games.screen.addPost

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
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

/**
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data.
 */
@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow(userRepository.getCurrentUser())
    val user: StateFlow<User?> = _user

    private val _error = MutableStateFlow<FormError?>(null)
    val error = _error.asStateFlow()

    private val _uiState = MutableStateFlow<AddPostUiState>(AddPostUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Internal mutable state flow representing the current post being edited.
     */
    private var _post = MutableStateFlow(
        Post(
            id = UUID.randomUUID().toString(),
            title = "",
            description = "",
            photoUrl = null,
            timestamp = System.currentTimeMillis(),
            author = null
        )
    )

    /**
     * Public state flow representing the current post being edited.
     * This is immutable for consumers.
     */
    val post: StateFlow<Post>
        get() = _post

    /**
     * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
     */
    val isPostValid = post.map { currentPost ->
        currentPost.title.isNotBlank() && (
                !currentPost.description.isNullOrBlank() || currentPost.photoUrl != null
                )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    /**
     * Handles form events like title and description changes.
     *
     * @param formEvent The form event to be processed.
     */
    fun onAction(formEvent: FormEvent) {
        when (formEvent) {
            is FormEvent.DescriptionChanged -> {
                _post.value = _post.value.copy(
                    description = formEvent.description
                )
            }

            is FormEvent.TitleChanged -> {
                _post.value = _post.value.copy(
                    title = formEvent.title
                )
            }

            is FormEvent.PhotoChanged -> {
                _post.value = _post.value.copy(
                    photoUrl = formEvent.photoUrl
                )
            }
        }
    }

    /**
     * Attempts to add the current post to the repository after setting the author.
     */
    fun addPost() {
        viewModelScope.launch {
            try {
                _uiState.value = AddPostUiState.Loading

                val currentPost = post.value.copy(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    author = user.value
                )

                postRepository.addPost(currentPost)

                _uiState.value = AddPostUiState.Success
                _error.value = null

                Log.d("AddPostViewModel", "Post added successfully: $currentPost")

            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("AddPostViewModel", "Error while adding the post", e)
                    _uiState.value = AddPostUiState.Error(e.message)
                }
            }
        }
    }

    /**
     * Verifies mandatory fields of the post
     * and returns a corresponding FormError if so.
     *
     * @return A FormError.TitleError if title is empty, null otherwise.
     */
    private fun verifyPost(post: Post = _post.value): FormError? {
        return when {
            post.title.isBlank() -> FormError.TitleError
            post.description.isNullOrBlank() && post.photoUrl == null -> FormError.DescriptionError
            else -> null
        }
    }


    fun onSaveClicked() {
        Log.d("AddPostViewModel", ">>> Post before validation: ${_post.value}")
        val validationError = verifyPost()
        if (validationError != null) {
            Log.d("AddPostViewModel", ">>> Validation failed: $validationError")
            _error.value = validationError
        } else {
            Log.d("AddPostViewModel", ">>> Validation OK, addPost() called")
            addPost()
        }
    }

}
