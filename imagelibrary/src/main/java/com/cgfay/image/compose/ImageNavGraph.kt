package com.cgfay.image.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun ImageNavGraph(imagePath: String?, deleteInput: Boolean) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "editor") {
        composable("editor") {
            ImageEditorScreen(
                imagePath = imagePath,
                deleteInputFile = deleteInput,
                navController = navController
            )
        }
        composable(
            route = "preview/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path")
            ImagePreviewScreen(path = path)
        }
    }
}
