package com.liufeng.ballfight

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.liufeng.ballfight.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    lateinit var gameView: GameView
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        setContentView(gameView)
        setupPauseButton()
    }

    private fun setupPauseButton() {
        val params = FrameLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.pause_btn_size),
            resources.getDimensionPixelSize(R.dimen.pause_btn_size)
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = resources.getDimensionPixelSize(R.dimen.pause_btn_margin)
            rightMargin = resources.getDimensionPixelSize(R.dimen.pause_btn_margin)
        }

        val pauseButton = ImageButton(this).apply {
            setImageResource(R.drawable.ic_pause_small)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { togglePause() }
        }

        addContentView(pauseButton, params)
    }

    fun togglePause() {
        isPaused = !isPaused
        gameView.gameThread.paused = isPaused
        if (isPaused) {
            gameView.pauseGame()
        } else {
            gameView.resumeGame()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isPaused) gameView.pauseGame()
    }

    override fun onResume() {
        super.onResume()
        if (!isPaused) gameView.resumeGame()
    }

	private fun setupScoreStyle() {
        binding.tvScore.apply {
            // 添加渐变效果
            val gradient = object : ShaderFactory() {
                override fun resize(width: Int, height: Int): Shader {
                    return LinearGradient(
                        0f, 0f, width.toFloat(), height.toFloat(),
                        intArrayOf(
                            ContextCompat.getColor(context, R.color.title_gradient_start),
                            ContextCompat.getColor(context, R.color.title_gradient_end)
                        ),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
            }
            paint.shader = gradient.resize(width, height)
        }
    }
}