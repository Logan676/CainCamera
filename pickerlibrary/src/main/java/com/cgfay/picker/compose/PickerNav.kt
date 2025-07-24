package com.cgfay.picker.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** Import screens */
import com.cgfay.picker.compose.AlbumListScreen

@Composable
fun PickerNavHost(activity: androidx.fragment.app.FragmentActivity, pickerParam: com.cgfay.picker.MediaPickerParam) {
    val navController = rememberNavController()
    val viewModel: PickerViewModel = viewModel(factory = PickerViewModel.Factory(activity, pickerParam))
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
            val media = backStackEntry.arguments?.getParcelable<com.cgfay.picker.model.MediaData>("media")
            MediaPreviewScreen(media) { navController.popBackStack() }
        }
        composable("albums") {
            AlbumListScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
    }
}
