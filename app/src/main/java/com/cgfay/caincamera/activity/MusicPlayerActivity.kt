package com.cgfay.caincamera.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cgfay.caincamera.ui.MusicPickerScreen
import com.cgfay.caincamera.ui.MusicPlayerScreen
import com.cgfay.uitls.bean.MusicData
import java.net.URLDecoder
import java.net.URLEncoder

class MusicPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MusicPlayerNavGraph(onFinish = { finish() }) }
    }
}

@Composable
fun MusicPlayerNavGraph(onFinish: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "picker") {
        composable("picker") {
            MusicPickerScreen(
                onClose = onFinish,
                onMusicSelected = { data: MusicData ->
                    val encoded = URLEncoder.encode(data.path, "UTF-8")
                    navController.navigate("player/$encoded")
                }
            )
        }
        composable(
            route = "player/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("path") ?: ""
            val path = URLDecoder.decode(encoded, "UTF-8")
            MusicPlayerScreen(path)
        }
    }
}

