package com.example.tareaoptimizacion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tareaoptimizacion.data.model.Photo
import com.example.tareaoptimizacion.ui.screens.PhotoDetailScreen
import com.example.tareaoptimizacion.ui.screens.PhotoFeedScreen
import com.example.tareaoptimizacion.ui.viewmodel.PhotoViewModel

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Detail : Screen("detail/{photoId}/{photoUrl}/{photoWidth}/{photoHeight}/{photoTitle}/{photoAuthor}") {
        fun createRoute(photo: Photo): String {
            // Codifica los parámetros para URLs seguras
            val encodedUrl = java.net.URLEncoder.encode(photo.url, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(photo.title, "UTF-8")
            val encodedAuthor = java.net.URLEncoder.encode(photo.author, "UTF-8")
            
            return "detail/${photo.id}/$encodedUrl/${photo.width}/${photo.height}/$encodedTitle/$encodedAuthor"
        }
    }
}
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // ViewModel compartido para mantener el estado de scroll
    val photoViewModel: PhotoViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Feed.route
    ) {
        // Pantalla de feed
        composable(Screen.Feed.route) {
            PhotoFeedScreen(
                viewModel = photoViewModel,
                onPhotoClick = { photo ->
                    navController.navigate(Screen.Detail.createRoute(photo))
                }
            )
        }
        
        // Pantalla de detalle
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("photoId") { type = NavType.StringType },
                navArgument("photoUrl") { type = NavType.StringType },
                navArgument("photoWidth") { type = NavType.IntType },
                navArgument("photoHeight") { type = NavType.IntType },
                navArgument("photoTitle") { type = NavType.StringType },
                navArgument("photoAuthor") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
            val photoUrl = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("photoUrl") ?: "", 
                "UTF-8"
            )
            val photoWidth = backStackEntry.arguments?.getInt("photoWidth") ?: 0
            val photoHeight = backStackEntry.arguments?.getInt("photoHeight") ?: 0
            val photoTitle = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("photoTitle") ?: "", 
                "UTF-8"
            )
            val photoAuthor = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("photoAuthor") ?: "", 
                "UTF-8"
            )
            
            val photo = Photo(
                id = photoId,
                url = photoUrl,
                width = photoWidth,
                height = photoHeight,
                title = photoTitle,
                author = photoAuthor
            )
            
            PhotoDetailScreen(
                photo = photo,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
