package com.example.tareaoptimizacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.tareaoptimizacion.data.model.Photo
import com.example.tareaoptimizacion.data.paging.PhotoPagingSource
import com.example.tareaoptimizacion.data.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class PhotoViewModel : ViewModel() {
    
    private val repository = PhotoRepository()
    
    // Estado del scroll para restauración
    private val _scrollState = MutableStateFlow(ScrollState())
    val scrollState: StateFlow<ScrollState> = _scrollState.asStateFlow()
    
    // Flow de paginación
    val photosFlow: Flow<PagingData<Photo>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 10,
            enablePlaceholders = false,
            initialLoadSize = 20
        ),
        pagingSourceFactory = { PhotoPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)
    

    fun saveScrollPosition(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        _scrollState.value = ScrollState(
            firstVisibleItemIndex = firstVisibleItemIndex,
            firstVisibleItemScrollOffset = firstVisibleItemScrollOffset
        )
    }

    fun clearScrollState() {
        _scrollState.value = ScrollState()
    }
}


data class ScrollState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0
)
