package com.liufeng.ballfight

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import com.liufeng.ballfight.databinding.DialogSettingsBinding

class SettingsDialog(context: Context, private val gameView: GameView) : Dialog(context) {
    private lateinit var binding: DialogSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化音量控制
        binding.volumeSeekBar.progress = (gameView.currentVolume * 100).toInt()
        binding.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                gameView.currentVolume = progress / 100f
            }
            dismiss()
        })
    }
}