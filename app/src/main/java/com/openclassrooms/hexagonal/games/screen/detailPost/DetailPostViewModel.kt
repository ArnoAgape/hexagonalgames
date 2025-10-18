package com.openclassrooms.hexagonal.games.screen.detailPost

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val userId = userRepository.getCurrentUser()
    private val _uiPostState = MutableStateFlow<DetailPostUiState>(DetailPostUiState.Loading)
    val uiPostState: StateFlow<DetailPostUiState> = _uiPostState.asStateFlow()
    private val _uiCommentState = MutableStateFlow<DetailCommentUiState>(DetailCommentUiState.Loading)
    val uiCommentState: StateFlow<DetailCommentUiState> = _uiCommentState.asStateFlow()

    init {
        Log.d("DetailPostViewModel", ">>> postId = $postId")
        observePost()
        observeComments(postId, userId)
    }

    private fun observePost() {
        viewModelScope.launch {
            postRepository.getPostById(postId)
                .onStart { _uiPostState.value = DetailPostUiState.Loading }
                .catch { e ->
                    _uiPostState.value = DetailPostUiState.Error(e.message ?: "Unknown error")
                }
                .collect { post ->
                    if (post != null) {
                        _uiPostState.value = DetailPostUiState.Success(post)
                    } else {
                        _uiPostState.value = DetailPostUiState.Error("Impossible to find the post")
                    }
                }
        }
    }
    private fun observeComments(postId: String, user: User?) {
        viewModelScope.launch {
            commentRepository.observeComments(postId)
                .onStart { _uiCommentState.value = DetailCommentUiState.Loading }
                .catch { e ->
                    _uiCommentState.value = DetailCommentUiState.Error(e.message ?: "Unknown error")
                }
                .collect { comments ->
                    Log.d("DetailPostViewModel", ">>> Received comments = $comments")
                    _uiCommentState.value = if (comments.isNotEmpty()) {
                        DetailCommentUiState.Success(comments, user)
                    } else {
                        DetailCommentUiState.Error("No comments found")
                    }
                }
        }
    }
}