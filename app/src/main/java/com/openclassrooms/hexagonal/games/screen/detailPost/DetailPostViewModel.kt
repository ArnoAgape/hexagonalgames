package com.openclassrooms.hexagonal.games.screen.detailPost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
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
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val _uiPostState = MutableStateFlow<DetailPostUiState>(DetailPostUiState.Loading)
    val uiPostState: StateFlow<DetailPostUiState> = _uiPostState.asStateFlow()
    private val _uiCommentState = MutableStateFlow(DetailCommentUiState())
    val uiCommentState: StateFlow<DetailCommentUiState> = _uiCommentState.asStateFlow()

    init {
        observePost()
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

    private fun observeComments(postId: String) {
        viewModelScope.launch {
            commentRepository.observeComments(postId)
                .onStart { _uiCommentState.value = _uiCommentState.value.copy(isLoading = true) }
                .catch { e ->
                    _uiCommentState.value = _uiCommentState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
                .collect { comments ->
                    _uiCommentState.value = DetailCommentUiState(
                        isLoading = false,
                        comments = comments,
                        error = null
                    )
                }
        }
    }

}