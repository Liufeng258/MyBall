package com.liufeng.ballfight

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    // 界面元素
    private lateinit var binding: ActivityGameBinding
    private var pauseMenu: PauseMenuDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkFirstLaunch()
        setupUI()
    }

    private fun checkFirstLaunch() {
        if (UserProfile.nickname.isEmpty()) {
            showNicknameDialog()
        }
    }

    private fun showNicknameDialog() {
        NicknameDialog().apply {
            setOnDismissListener {
                UserProfile.uid = generateNewUID()
            }
        }.show(supportFragmentManager, "nickname")
    }

    private fun setupUI() {
        // 分数显示样式
        binding.tvScore.typeface = ResourcesCompat.getFont(this, R.font.digital)
        binding.tvScore.setTextColor(ContextCompat.getColor(this, R.color.score_default))
        
        // 背景加载
        Glide.with(this)
            .load(R.drawable.game_bg)
            .into(binding.ivBackground)
    }

    // 返回键处理
    override fun onBackPressed() {
        if (binding.gameView.isPaused) {
            binding.gameView.resumeGame()
        } else {
            super.onBackPressed()
        }
    }
}