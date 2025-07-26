package com.cgfay.caincamera.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cgfay.media.command.CommandBuilder
import com.cgfay.media.command.CommandExecutor
import com.cgfay.media.command.AVOperations
import com.cgfay.media.VideoEditorUtil
import com.cgfay.uitls.bean.MusicData
import com.cgfay.uitls.utils.FileUtils
import com.cgfay.video.activity.VideoEditActivity
import kotlinx.coroutines.launch
import java.net.URLEncoder
import kotlin.math.min

@Composable
fun MusicMergeNavGraph(videoPath: String, onFinish: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "picker") {
        composable("picker") {
            MusicPickerScreen(
                onClose = onFinish,
                onMusicSelected = { data ->
                    val encoded = URLEncoder.encode(data.path, "UTF-8")
                    navController.navigate("merge/$encoded/${data.duration}")
                }
            )
        }
        composable(
            route = "merge/{path}/{duration}",
            arguments = listOf(
                navArgument("path") { type = NavType.StringType },
                navArgument("duration") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            val duration = backStackEntry.arguments?.getLong("duration") ?: 0L
            MusicMergeScreen(
                videoPath = videoPath,
                musicPath = path,
                musicDuration = duration,
                onFinish = onFinish
            )
        }
    }
}


@Composable
fun MusicMergeScreen(
    videoPath: String,
    musicPath: String,
    musicDuration: Long,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoVolume by remember { mutableStateOf(0.5f) }
    var musicVolume by remember { mutableStateOf(0.5f) }
    var progress by remember { mutableStateOf(0) }
    var merging by remember { mutableStateOf(false) }

    val videoView = remember {
        VideoView(context).apply { setVideoPath(videoPath) }
    }

    DisposableEffect(Unit) {
        videoView.start()
        onDispose { videoView.stopPlayback() }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        AndroidView(factory = { videoView }, modifier = Modifier.fillMaxWidth())
        Text(text = "music path:\n$musicPath", modifier = Modifier.padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "video volume", modifier = Modifier.padding(end = 8.dp))
            Slider(value = videoVolume, onValueChange = { videoVolume = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "music volume", modifier = Modifier.padding(end = 8.dp))
            Slider(value = musicVolume, onValueChange = { musicVolume = it })
        }
        Button(
            onClick = {
                scope.launch {
                    merging = true
                    musicMergeCommand(
                        context = context,
                        videoPath = videoPath,
                        musicPath = musicPath,
                        musicDuration = musicDuration,
                        videoVolume = videoVolume,
                        musicVolume = musicVolume,
                        onProgress = { progress = it },
                        onComplete = { success, output ->
                            merging = false
                            if (success && output != null) {
                                val intent = Intent(context, VideoEditActivity::class.java)
                                intent.putExtra(VideoEditActivity.VIDEO_PATH, output)
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                            }
                        }
                    )
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) { Text(text = "Merge") }
        if (merging) {
            Text(text = "processing $progress%", modifier = Modifier.padding(top = 8.dp))
        }
        Button(onClick = onFinish, modifier = Modifier.padding(top = 8.dp)) { Text("Close") }
    }
}

fun musicMergeCommand(
    context: Context,
    videoPath: String,
    musicPath: String,
    musicDuration: Long,
    videoVolume: Float,
    musicVolume: Float,
    onProgress: (Int) -> Unit,
    onComplete: (Boolean, String?) -> Unit
) {
    val handler = Handler(Looper.getMainLooper())
    val editor = CommandExecutor()
    val duration = min(AVOperations.getDuration(videoPath) / 1000, musicDuration.toInt())
    val tmpAACPath = VideoEditorUtil.createPathInBox(context, "aac")
    editor.execCommand(CommandBuilder.audioCut(musicPath, tmpAACPath, 10 * 1000, duration)) { result ->
        if (result < 0) {
            FileUtils.deleteFile(tmpAACPath)
            handler.post { onComplete(false, null) }
        } else {
            val output = VideoEditorUtil.createFileInBox(context, "mp4")
            editor.execCommand(
                CommandBuilder.audioVideoMix(videoPath, tmpAACPath, output, videoVolume, musicVolume)
            ) { ret ->
                FileUtils.deleteFile(tmpAACPath)
                handler.post { onComplete(ret == 0 && FileUtils.fileExists(output), output) }
            }
        }
    }
}
