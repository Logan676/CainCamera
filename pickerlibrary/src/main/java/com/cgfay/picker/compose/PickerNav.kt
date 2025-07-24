package com.cgfay.picker.compose

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** Import screens */
import com.cgfay.picker.compose.AlbumListScreen

@Composable
fun PickerNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "picker") {
        composable("picker") {
            MediaPickerScreen(
                onPreview = { id ->
                    navController.currentBackStackEntry?.arguments?.putInt("media", id)
                    navController.navigate("preview")
                },
                onShowAlbums = { navController.navigate("albums") }
            )
        }
        composable("preview") { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("media")
            MediaPreviewScreen(id) { navController.popBackStack() }
        }
        composable("albums") {
            AlbumListScreen { navController.popBackStack() }
        }
    }
}
