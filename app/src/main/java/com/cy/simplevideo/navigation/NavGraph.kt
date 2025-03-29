package com.cy.simplevideo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cy.simplevideo.ui.screen.DetailScreen
import com.cy.simplevideo.ui.screen.MainScreen
import com.cy.simplevideo.ui.screen.PlayerScreen
import com.cy.simplevideo.ui.viewmodel.VideoViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Detail : Screen("detail")
    object Player : Screen("player/{url}") {
        fun createRoute(url: String): String {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            return "player/$encodedUrl"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: VideoViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onVideoClick = { url ->
                    viewModel.getVideoDetail(url)
                    navController.navigate(Screen.Detail.route)
                }
            )
        }
        
        composable(Screen.Detail.route) {
            DetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEpisodeClick = { url ->
                    navController.navigate(Screen.Player.createRoute(url))
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("url") { 
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: return@composable
            val url = java.net.URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            PlayerScreen(
                videoUrl = url,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 