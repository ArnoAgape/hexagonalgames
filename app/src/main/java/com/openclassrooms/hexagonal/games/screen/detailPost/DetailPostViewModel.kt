package com.openclassrooms.hexagonal.games.screen.detailPost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        observePost()
    }

    private fun observePost() {
        viewModelScope.launch {
            postRepository.getPostById(postId)
                .onStart { _uiState.value = DetailUiState.Loading }
                .catch { e ->
                    _uiState.value = DetailUiState.Error(e.message ?: "Unknown error")
                }
                .collect { post ->
                    if (post != null) {
                        _uiState.value = DetailUiState.Success(post)
                    } else {
                        _uiState.value = DetailUiState.Error("Impossible to find the post")
                    }
                }
        }
    }
}