package com.cy.simplevideo.utils

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

/**
 * 状态栏工具类，用于设置沉浸式状态栏
 */
object StatusBarUtil {

    /**
     * 设置状态栏为透明，并根据当前主题设置状态栏文字颜色
     */
    fun setTransparentStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 根据当前主题设置状态栏
            val isNightMode = activity.resources.configuration.uiMode and 
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            
            if (isNightMode) {
                // 夜间模式
                activity.window.statusBarColor = Color.parseColor("#121212") // Material深色主题的标准背景色
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 深色模式下状态栏文字为白色
                    activity.window.decorView.systemUiVisibility = 
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            } else {
                // 日间模式
//                activity.window.statusBarColor = Color.WHITE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 浅色模式下状态栏文字为黑色
                    activity.window.decorView.systemUiVisibility = 
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or 
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    /**
     * 设置深色状态栏（状态栏文字图标为深色）
     */
    fun setDarkStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    /**
     * 设置浅色状态栏（状态栏文字图标为浅色）
     */
    fun setLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
        }
    }

    /**
     * 全沉浸式模式（隐藏状态栏和导航栏）
     */
    fun setFullImmersive(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = activity.window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            decorView.systemUiVisibility = uiOptions
        }
    }

    /**
     * 退出全沉浸式模式
     */
    fun exitFullImmersive(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = activity.window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
        }
    }

    /**
     * 为状态栏和底部导航条设置指定颜色
     */
    fun setStatusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = color
        }
    }
} 