package com.cgfay.picker.compose

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun PickerNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "picker") {
        composable("picker") {
            MediaPickerScreen { id ->
                navController.currentBackStackEntry?.arguments?.putInt("media", id)
                navController.navigate("preview")
            }
        }
        composable("preview") { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("media")
            MediaPreviewScreen(id) { navController.popBackStack() }
        }
    }
}
