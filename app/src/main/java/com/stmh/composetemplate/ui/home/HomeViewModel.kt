package com.stmh.composetemplate.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stmh.composetemplate.data.model.Post
import com.stmh.composetemplate.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val posts: List<Post>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            postRepository.getPosts()
                .onSuccess { posts ->
                    _uiState.value = HomeUiState.Success(posts)
                }
                .onFailure { exception ->
                    _uiState.value = HomeUiState.Error(
                        exception.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
        }
    }
}
