package com.example.tareaoptimizacion.data.model

data class Photo(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val title: String,
    val author: String = "Unknown"
) {
    // Calcula la altura proporcional para un ancho fijo
    fun getProportionalHeight(targetWidth: Int): Int {
        return (targetWidth.toFloat() / width * height).toInt()
    }
    
    // Ratio de aspecto
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()
}
