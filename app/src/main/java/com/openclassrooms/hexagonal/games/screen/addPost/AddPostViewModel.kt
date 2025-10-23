package com.openclassrooms.hexagonal.games.screen.addPost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
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
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data.
 */
@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddPostUiState>(AddPostUiState.Idle)
    val uiState: StateFlow<AddPostUiState> = _uiState.asStateFlow()
    private val _user = MutableStateFlow<User?>(null)
    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()
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
                _events.trySend(Event.ShowToast(R.string.no_network))
                return@launch
            }

            _uiState.value = AddPostUiState.Loading

            try {
                // Add the author to the post
                val postToSave = _post.value.copy(author = _user.value)
                // Call the repository to add the post
                postRepository.addPost(postToSave)

                _uiState.value = AddPostUiState.Success(postToSave)
                _events.trySend(Event.ShowToast(R.string.post_success))

            } catch (e: Exception) {
                when (e) {
                    is IllegalStateException -> {
                        _uiState.value = AddPostUiState.Error.NoAccount()
                        _events.trySend(Event.ShowToast(R.string.error_no_account_post))
                    }

                    is IOException -> {
                        _uiState.value = AddPostUiState.Error.Generic("Network error: ${e.message}")
                        _events.trySend(Event.ShowToast(R.string.no_network))
                    }

                    else -> {
                        _uiState.value = AddPostUiState.Error.Generic()
                        _events.trySend(Event.ShowToast(R.string.error_generic))
                    }
                }
            }
        }
    }
        fun onSaveClicked() {
            val post = _post.value
            when {
                post.title.isBlank() -> {
                    _events.trySend(Event.ShowToast(R.string.error_title))
                }

                post.description.isNullOrBlank() && post.photoUrl == null -> {
                    _events.trySend(Event.ShowToast(R.string.error_description))
                }

                else -> {
                    addPost()
                }
            }
        }


    }