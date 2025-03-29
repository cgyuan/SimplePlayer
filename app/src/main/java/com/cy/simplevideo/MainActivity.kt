package com.cy.simplevideo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.cy.simplevideo.navigation.NavGraph
import com.cy.simplevideo.ui.theme.SimpleVideoTheme
import com.cy.simplevideo.ui.viewmodel.VideoViewModel
import com.cy.simplevideo.ui.viewmodel.VideoViewModelFactory
import com.cy.simplevideo.utils.StatusBarUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏 - 只保留透明设置
        StatusBarUtil.setTransparentStatusBar(this)
        
        setContent {
            SimpleVideoTheme {
                // 设置内容填充到状态栏
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: VideoViewModel = viewModel(
                        factory = VideoViewModelFactory(this)
                    )
                    
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}