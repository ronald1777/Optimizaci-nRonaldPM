package com.example.tareaoptimizacion.data.repository

import com.example.tareaoptimizacion.data.model.Photo
import kotlinx.coroutines.delay
import kotlin.random.Random

class PhotoRepository {
    
    private val baseUrls = listOf(
        "https://picsum.photos/seed/",
        "https://loremflickr.com/"
    )
    
    // Simula latencia de red
    private val networkDelay = 500L..1500L
    
    // Simula errores aleatorios
    private val errorRate = 0.1
    
    companion object {
        private const val PAGE_SIZE = 20
    }
    

    suspend fun loadPhotos(page: Int): Result<List<Photo>> {
        return try {
            // Simula latencia de red
            delay(Random.nextLong(networkDelay.first, networkDelay.last))
            
            // Simula error de red ocasional
            if (Random.nextDouble() < errorRate) {
                throw Exception("Network error: Failed to fetch photos")
            }
            
            val photos = generatePhotos(page)
            Result.success(photos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generatePhotos(page: Int): List<Photo> {
        val startId = page * PAGE_SIZE
        
        return List(PAGE_SIZE) { index ->
            val id = startId + index
            val seed = "photo$id"
            
            // Dimensiones variables para crear efecto masonry
            val widthOptions = listOf(800, 1000, 1200)
            val heightOptions = listOf(600, 800, 1000, 1200, 1400)
            
            val width = widthOptions.random()
            val height = heightOptions.random()
            
            // Usamos Picsum Photos
            val url = "https://picsum.photos/seed/$seed/$width/$height"
            
            Photo(
                id = id.toString(),
                url = url,
                width = width,
                height = height,
                title = generateTitle(id),
                author = generateAuthor()
            )
        }
    }
    
    private fun generateTitle(id: Int): String {
        val titles = listOf(
            "Beautiful Landscape",
            "City Architecture",
            "Nature Photography",
            "Abstract Art",
            "Street Photography",
            "Portrait Session",
            "Minimalist Design",
            "Urban Exploration",
            "Natural Beauty",
            "Creative Composition",
            "Sunset Views",
            "Mountain Adventure",
            "Ocean Waves",
            "Forest Path",
            "Desert Landscape"
        )
        return "${titles.random()} #$id"
    }
    
    private fun generateAuthor(): String {
        val authors = listOf(
            "Alex Johnson",
            "Maria Garcia",
            "John Smith",
            "Emma Wilson",
            "David Lee",
            "Sofia Martinez",
            "James Brown",
            "Olivia Taylor"
        )
        return authors.random()
    }
}
