package com.openclassrooms.hexagonal.games.screen.detailPost

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val userId = userRepository.getCurrentUser()
    private val _uiPostState = MutableStateFlow<DetailPostUiState>(DetailPostUiState.Loading)
    val uiPostState: StateFlow<DetailPostUiState> = _uiPostState.asStateFlow()
    private val _uiCommentState = MutableStateFlow<DetailCommentUiState>(DetailCommentUiState.Loading)
    val uiCommentState: StateFlow<DetailCommentUiState> = _uiCommentState.asStateFlow()

    // refresh data
    private var postJob: Job? = null
    private var commentJob: Job? = null

    val isUserSignedIn: StateFlow<Boolean> =
        userRepository.isUserSignedIn()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        observePost()
        observeComments(postId, userId)
    }

    private fun observePost() {
        postJob?.cancel()
        postJob = viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _uiPostState.value = DetailPostUiState.Error.Network()
                return@launch
            }
            postRepository.getPostById(postId)
                .onStart { _uiPostState.value = DetailPostUiState.Loading }
                .catch { e ->
                    val message = e.message ?: "Unknown error"
                    _uiPostState.value = if (message.contains("network", true)) {
                        DetailPostUiState.Error.Network()
                    } else {
                        DetailPostUiState.Error.Generic(message)
                    }
                }
                .collect { post ->
                    if (post != null) {
                        _uiPostState.value = DetailPostUiState.Success(post)
                    } else {
                        _uiPostState.value = DetailPostUiState.Error.Empty("Impossible to find the post")
                    }
                }
        }
    }
    private fun observeComments(postId: String, user: User?) {
        commentJob?.cancel()
        commentJob = viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _uiCommentState.value = DetailCommentUiState.Error.Network()
                return@launch
            }
            commentRepository.observeComments(postId)
                .onStart { _uiCommentState.value = DetailCommentUiState.Loading }
                .catch { e ->
                    val message = e.message ?: "Unknown error"
                    _uiCommentState.value = if (message.contains("network", true)) {
                        DetailCommentUiState.Error.Network()
                    } else {
                        DetailCommentUiState.Error.Generic(message)
                    }
                }
                .collect { comments ->
                    Log.d("DetailPostViewModel", ">>> Received comments = $comments")
                    _uiCommentState.value = if (comments.isNotEmpty()) {
                        DetailCommentUiState.Success(comments, user)
                    } else {
                        DetailCommentUiState.Error.Empty("No comments found")
                    }
                }
        }
    }

    fun refreshData() {
        observePost()
        observeComments(postId, userId)
    }

}