package com.cy.simplevideo.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import com.cy.simplevideo.R
import com.cy.simplevideo.databinding.LayoutDetailVideoPlayerBinding
import com.cy.simplevideo.receiver.TimeBatteryReceiver
import com.cy.simplevideo.utils.gone
import com.cy.simplevideo.utils.visible
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import java.text.SimpleDateFormat
import java.util.Date

open class FullScreenVideoPlayer : StandardGSYVideoPlayer {

    companion object {
        private const val TAG = "DetailVideoPlayer"
        private val timeFormat = SimpleDateFormat("HH:mm")
    }

    private lateinit var receiver: TimeBatteryReceiver
    private lateinit var showMenuAnim: Animation
    private lateinit var hideMenuAnim: Animation

    //是否是第一次加载视频，第一次加载时隐藏底部进度条和播放按钮
    private var isFirstLoad = true

    private var isLongPressSpeedUp = false
    private var normalSpeed = 1.0f
    private val longPressSpeed = 3.0f
    private var longPressRunnable: Runnable? = null
    private var lastX = 0f
    private var lastY = 0f
    private val touchSlop = 10 // 触摸阈值，用于判断是否触发其他手势
    private var arrowAnimators = mutableListOf<android.animation.ObjectAnimator>()

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private lateinit var binding: LayoutDetailVideoPlayerBinding

    val canPlayBackground: Boolean
        get() = binding.menu.playBackgroundBtn.isSelected

    override fun initInflate(context: Context?) {
        binding = LayoutDetailVideoPlayerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        dismissControlTime = 5000
        longPressRunnable = Runnable {
            if (!isLongPressSpeedUp) {
                isLongPressSpeedUp = true
                speed = longPressSpeed
                showSpeedIndicator()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun init(context: Context?) {
        super.init(context)
        receiver = TimeBatteryReceiver.register(context!!)
        showMenuAnim = AnimationUtils.loadAnimation(context, R.anim.dialog_right_in)
        hideMenuAnim = AnimationUtils.loadAnimation(context, R.anim.dialog_right_out)
        binding.settingMore.setOnClickListener {
            changeUiToClear()
            binding.menu.root.visible(showMenuAnim)
        }

        binding.speedUp.setOnClickListener {
            changeUiToClear()
            binding.speedList.root.visible(showMenuAnim)
        }

        binding.speedList.speedGroup.check(R.id.normal)
        binding.speedList.speedGroup.setOnCheckedChangeListener { _, checkedId ->
            speed =
                (binding.speedList.speedGroup.findViewById<View>(checkedId).tag as String).toFloat()
            normalSpeed = speed
        }

        binding.menu.playBackgroundBtn.setOnClickListener {
            it.isSelected = it.isSelected.not()
        }

        binding.menu.mirrorFlipBtn.setOnClickListener {
            if (mTextureView == null) {
                return@setOnClickListener
            }
            it.isSelected = it.isSelected.not()
            val xScale = if (it.isSelected) {
                -1F
            } else {
                1F
            }
            val transform = Matrix()
            transform.setScale(xScale, 1F, mTextureView.width / 2F, 0F)
            mTextureView.setTransform(transform)
            mTextureView.invalidate()
        }
        binding.menu.screenAspectGroup.check(R.id.screen_default)
        binding.menu.screenAspectGroup.setOnCheckedChangeListener { _, checkedId ->
            val aspect =
                (binding.menu.screenAspectGroup.findViewById<View>(checkedId).tag as String).toInt()
            GSYVideoType.setShowType(aspect)
            changeTextureViewShowType()
            if (mTextureView != null) {
                mTextureView.requestLayout()
            }
        }

        binding.screenRotate.setOnClickListener {
            val activity = context as androidx.activity.ComponentActivity
            if (activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                // 如果当前是横屏，切换到竖屏
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                binding.settingMore.gone()
            } else {
                // 如果当前是竖屏，切换到横屏
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                binding.settingMore.visible(true)
            }
        }

        post {
            upTime()
        }

        receiver.setOnTimeUpdateListener {
            upTime()
        }
        receiver.setOnBatteryChangedListener { level ->
            setBatteryLevel(level)
        }
    }

    private fun upTime() {
        binding.sysTime.text = timeFormat.format(Date(System.currentTimeMillis()))
    }

    private fun setBatteryLevel(level: Int) {
        binding.ivBattery.setImageLevel(level)
    }

    override fun onClickUiToggle(e: MotionEvent?) {
        if (binding.menu.root.isShown) {
            binding.menu.root.gone(hideMenuAnim)
            return
        }
        if (binding.speedList.root.isShown) {
            binding.speedList.root.gone(hideMenuAnim)
            return
        }
        if (binding.epList.root.isShown) {
            binding.epList.root.gone(hideMenuAnim)
            return
        }
        super.onClickUiToggle(e)
    }

    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            val iv = mStartButton as ImageView
            when (mCurrentState) {
                GSYVideoView.CURRENT_STATE_PLAYING -> iv.setImageResource(R.drawable.ic_video_play)
                GSYVideoView.CURRENT_STATE_ERROR -> iv.setImageResource(R.drawable.ic_video_pause)
                GSYVideoView.CURRENT_STATE_AUTO_COMPLETE -> iv.setImageResource(R.drawable.ic_video_refresh)
                else -> iv.setImageResource(R.drawable.ic_video_pause)
            }
        } else {
            super.updateStartImage()
        }
    }

    //显示
    override fun changeUiToNormal() {
        super.changeUiToNormal()
        isFirstLoad = true
    }

    //准备
    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        mBottomContainer.visibility = View.GONE
        mStartButton.visibility = View.GONE
    }

    //开始播放
    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        if (isFirstLoad) {
            mBottomContainer.visibility = View.GONE
            mStartButton.visibility = View.GONE
        }
        isFirstLoad = false
    }

    //缓冲
    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
    }

    //暂停播放
    override fun changeUiToPauseShow() {
        super.changeUiToPauseShow()
    }

    //播放完成
    override fun changeUiToCompleteShow() {
        super.changeUiToCompleteShow()
        mBottomContainer.visibility = View.GONE
    }

    //播放出错
    override fun changeUiToError() {
        super.changeUiToError()
    }

    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        if (binding.menu.root.isShown || binding.speedList.root.isShown
            || binding.epList.root.isShown || isLongPressSpeedUp
        ) {
            return
        }
        super.touchSurfaceMove(deltaX, deltaY, y)
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(receiver)
    }

    override fun onVideoPause() {
        super.onVideoPause()
    }

    fun switchToBackground() {
        if(canPlayBackground){
            return
        }
        super.onVideoPause()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        super.onProgressChanged(seekBar, progress, fromUser)
        
        // 计算当前时间（毫秒）
        val duration = duration
        val currentPosition = progress * duration / 100
        
        // 转换为时分秒格式
        val hours = currentPosition / 3600000
        val minutes = (currentPosition % 3600000) / 60000
        val seconds = (currentPosition % 60000) / 1000
        
        // 格式化时间字符串
        val timeStr = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
        
        binding.current.text = timeStr
    }

    private fun showSpeedIndicator() {
        binding.speedIndicator.root.visible()
        
        // 清除之前的动画
        arrowAnimators.forEach { it.cancel() }
        arrowAnimators.clear()

        // 为每个箭头创建动画
        val arrows = listOf(
            binding.speedIndicator.arrow1,
            binding.speedIndicator.arrow2,
            binding.speedIndicator.arrow3
        )

        arrows.forEachIndexed { index, arrow ->
            // 创建透明度动画
            val alphaAnimator = android.animation.ObjectAnimator.ofFloat(arrow, "alpha", 0.3f, 1f)
            alphaAnimator.duration = 300
            alphaAnimator.repeatCount = android.animation.ObjectAnimator.INFINITE
            alphaAnimator.repeatMode = android.animation.ObjectAnimator.REVERSE
            alphaAnimator.startDelay = (index * 100).toLong()
            
            // 创建位移动画
            val translateAnimator = android.animation.ObjectAnimator.ofFloat(arrow, "translationX", -10f, 0f)
            translateAnimator.duration = 300
            translateAnimator.repeatCount = android.animation.ObjectAnimator.INFINITE
            translateAnimator.repeatMode = android.animation.ObjectAnimator.REVERSE
            translateAnimator.startDelay = (index * 100).toLong()
            
            alphaAnimator.start()
            translateAnimator.start()
            
            arrowAnimators.add(alphaAnimator)
            arrowAnimators.add(translateAnimator)
        }
    }

    private fun hideSpeedIndicator() {
        binding.speedIndicator.root.gone()
        arrowAnimators.forEach { it.cancel() }
        arrowAnimators.clear()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                if (mCurrentState == CURRENT_STATE_PLAYING) {
                    postDelayed(longPressRunnable, 500) // 500ms 长按触发
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLongPressSpeedUp) {
                    // 在长按加速状态下，直接返回true，阻止其他手势
                    return true
                } else {
                    // 检测是否触发其他手势
                    val dx = Math.abs(event.x - lastX)
                    val dy = Math.abs(event.y - lastY)
                    if (dx > touchSlop || dy > touchSlop) {
                        // 如果移动距离超过阈值，取消长按检测
                        removeCallbacks(longPressRunnable)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                if (isLongPressSpeedUp) {
                    isLongPressSpeedUp = false
                    speed = normalSpeed
                    hideSpeedIndicator()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}