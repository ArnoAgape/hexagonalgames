package com.openclassrooms.hexagonal.games.screen.addPost

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data.
 */
@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPostUiState.initial())
    val uiState: StateFlow<AddPostUiState> = _uiState.asStateFlow()
    private val _user = MutableStateFlow(userRepository.getCurrentUser())
    private val _error = MutableStateFlow<FormError?>(null)
    private val _post = MutableStateFlow(
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
    val post: StateFlow<Post> = _post.asStateFlow()

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
                    photoUrl = formEvent.photoUrl?.toString()
                )
            }

            else -> Unit
        }
    }

    /**
     * Attempts to add the current post to the repository after setting the author.
     */
    fun addPost() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _uiState.update { it.copy(isSaving = false, error = FormError.NetworkError) }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, isSuccess = false, error = null) }

            try {
                // Add the author to the post
                val postToSave = _post.value.copy(author = _user.value)
                // Call the repository to add the post
                postRepository.addPost(postToSave)

                _uiState.update { it.copy(isSaving = false, isSuccess = true) }

                Log.d("AddPostViewModel", "Post added successfully: $postToSave")

            } catch (e: IOException) {
                Log.e("AddPostViewModel", "Network error", e)
                _uiState.update { it.copy(isSaving = false, error = FormError.NetworkError) }

            } catch (e: Exception) {
                Log.e("AddPostViewModel", "Error while adding the post", e)
                _uiState.update { it.copy(isSaving = false, error = FormError.GenericError) }
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

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }

}