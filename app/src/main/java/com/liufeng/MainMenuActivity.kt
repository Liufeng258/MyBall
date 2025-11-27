package com.liufeng.ballfight

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.liufeng.ballfight.databinding.ActivityMainBinding

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 加载背景图
        Glide.with(this)
            .load(R.drawable.main_bg)
            .into(binding.backgroundImage)

        // 更新按钮文本
        binding.btnNewGame.text = getString(R.string.btn_new_game)
        binding.btnContinue.text = getString(R.string.btn_continue)
    }
}