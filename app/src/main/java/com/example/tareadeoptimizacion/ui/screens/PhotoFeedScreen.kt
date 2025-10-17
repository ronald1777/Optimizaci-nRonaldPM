package com.example.tareaoptimizacion.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tareaoptimizacion.data.model.Photo
import com.example.tareaoptimizacion.ui.viewmodel.PhotoViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhotoFeedScreen(
    viewModel: PhotoViewModel = viewModel(),
    onPhotoClick: (Photo) -> Unit
) {
    val photos = viewModel.photosFlow.collectAsLazyPagingItems()
    val scrollState = viewModel.scrollState.collectAsState()
    val gridState = rememberLazyStaggeredGridState(
        initialFirstVisibleItemIndex = scrollState.value.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = scrollState.value.firstVisibleItemScrollOffset
    )
    val scope = rememberCoroutineScope()
    
    // Guarda el estado de scroll cuando cambia
    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        viewModel.saveScrollPosition(
            gridState.firstVisibleItemIndex,
            gridState.firstVisibleItemScrollOffset
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Feed") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (photos.loadState.refresh is LoadState.Loading && photos.itemCount == 0) {
                // Skeleton loader inicial
                LoadingPlaceholder()
            } else if (photos.loadState.refresh is LoadState.Error && photos.itemCount == 0) {
                // Error en carga inicial
                ErrorView(
                    message = (photos.loadState.refresh as LoadState.Error).error.message 
                        ?: "Unknown error",
                    onRetry = { photos.retry() }
                )
            } else {
                // Grid con fotos
                PhotoGrid(
                    photos = photos,
                    gridState = gridState,
                    onPhotoClick = onPhotoClick,
                    onRetry = { photos.retry() }
                )
            }
            
            // Botón de scroll to top cuando se desplaza hacia abajo
            val showScrollToTop by remember {
                derivedStateOf { gridState.firstVisibleItemIndex > 10 }
            }
            
            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            gridState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("↑")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGrid(
    photos: LazyPagingItems<Photo>,
    gridState: androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState,
    onPhotoClick: (Photo) -> Unit,
    onRetry: () -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = photos.itemCount,
            key = { index -> photos[index]?.id ?: "item_$index" }
        ) { index ->
            photos[index]?.let { photo ->
                PhotoItem(
                    photo = photo,
                    onClick = { onPhotoClick(photo) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
        
        // Estados de paginación
        when (photos.loadState.append) {
            is LoadState.Loading -> {
                item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                    LoadingItem()
                }
            }
            is LoadState.Error -> {
                item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                    ErrorItem(
                        message = (photos.loadState.append as LoadState.Error).error.message 
                            ?: "Error loading more",
                        onRetry = onRetry
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun PhotoItem(
    photo: Photo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photo.url)
                    .crossfade(true)
                    .size(600) // Tamaño objetivo para optimización
                    .build(),
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(photo.aspectRatio)
            )
            
            Text(
                text = photo.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun ErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
