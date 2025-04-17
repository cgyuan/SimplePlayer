package com.cy.simplevideo.ui.screen

import android.content.pm.ActivityInfo
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cy.simplevideo.utils.StatusBarUtil
import com.cy.simplevideo.widgets.AdFilteringVideoPlayer
import com.cy.simplevideo.widgets.FullScreenVideoPlayer
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager

@Composable
fun PlayerScreen(
    videoUrl: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as androidx.activity.ComponentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    
    Log.d("PlayerScreen", "videoUrl: $videoUrl")
    
    // 设置使用ExoPlayer
    SideEffect {
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        
        // 保持屏幕常亮
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 进入全屏模式
        StatusBarUtil.setFullImmersive(activity)
    }
    
    // 保存播放器和方向工具实例
    val playerRef = remember { mutableStateOf<FullScreenVideoPlayer?>(null) }
    val orientationUtilsRef = remember { mutableStateOf<OrientationUtils?>(null) }
    
    // 设置为横屏模式
    LaunchedEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                AdFilteringVideoPlayer(context).apply {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    
                    // 保存播放器实例
                    playerRef.value = this
                    
                    // 创建方向工具，用于自动旋转和全屏
                    val orientationUtils = OrientationUtils(activity, this)
                    orientationUtils.isEnable = true
                    orientationUtilsRef.value = orientationUtils
                    
                    // 设置返回按钮功能
                    backButton.setOnClickListener {
                        // 退出全屏模式
                        StatusBarUtil.exitFullImmersive(activity)
                        onNavigateBack()
                    }
                    
                    // 设置播放器参数
                    setVideoAllCallBack(object : GSYSampleCallBack() {
                        override fun onPrepared(url: String, vararg objects: Any) {
                            super.onPrepared(url, *objects)
                            orientationUtils.isEnable = true
                        }
                    })

                    // 设置播放器配置
                    GSYVideoOptionBuilder()
                        .setIsTouchWiget(true)
                        .setRotateViewAuto(false)
                        .setLockLand(false)
                        .setAutoFullWithSize(true)
                        .setShowFullAnimation(false)
                        .setNeedLockFull(false)
                        .setSeekRatio(1f)
                        .setUrl(videoUrl)
                        .setCacheWithPlay(false)
                        .build(this)
                    
                    // 播放视频
                    startPlayLogic()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    // 生命周期管理
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    playerRef.value?.switchToBackground()
                }
                Lifecycle.Event.ON_RESUME -> {
                    playerRef.value?.onVideoResume(false)
                    // 确保在恢复时仍然是全屏
                    StatusBarUtil.setFullImmersive(activity)
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            // 退出全屏模式
            StatusBarUtil.exitFullImmersive(activity)
            // 取消屏幕常亮
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            orientationUtilsRef.value?.releaseListener()
            GSYVideoManager.releaseAllVideos()
        }
    }
} 