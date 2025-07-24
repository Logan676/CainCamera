package com.cgfay.picker.compose

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cgfay.picker.model.MediaData

/** Import screens */
import com.cgfay.picker.compose.AlbumListScreen

@Composable
fun PickerNavHost(viewModel: PickerViewModel) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "picker") {
        composable("picker") {
            MediaPickerScreen(
                onPreview = { media ->
                    navController.currentBackStackEntry?.arguments?.putParcelable("media", media)
                    navController.navigate("preview")
                },
                onShowAlbums = { navController.navigate("albums") },
                viewModel = viewModel
            )
        }
        composable("preview") { backStackEntry ->
            val media = backStackEntry.arguments?.getParcelable<MediaData>("media")
            MediaPreviewScreen(media) { navController.popBackStack() }
        }
        composable("albums") {
            AlbumListScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
    }
}
