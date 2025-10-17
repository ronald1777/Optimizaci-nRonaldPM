package com.example.tareaoptimizacion.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tareaoptimizacion.data.model.Photo
import com.example.tareaoptimizacion.data.repository.PhotoRepository


class PhotoPagingSource(
    private val repository: PhotoRepository
) : PagingSource<Int, Photo>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        return try {
            val page = params.key ?: 0
            
            val result = repository.loadPhotos(page)
            
            result.fold(
                onSuccess = { photos ->
                    LoadResult.Page(
                        data = photos,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (photos.isEmpty()) null else page + 1
                    )
                },
                onFailure = { exception ->
                    LoadResult.Error(exception)
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        // Intenta encontrar la página cercana al ancla de scroll
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
